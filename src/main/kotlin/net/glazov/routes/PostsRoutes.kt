package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.PostsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.model.posts.IncomingPostModel
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.UrlChanger
import net.glazov.data.utils.EmployeeRoles
import net.glazov.data.utils.notificationsmanager.*

private const val PATH = "/api/posts"

fun Route.postRoutes(
    posts: PostsDataSource,
    notificationsManager: NotificationsManager,
    employees: EmployeesDataSource
) {

    authenticate {

        get(PATH) {
            val postsList = posts.getAllPosts()

            //TODO: remove after set up stable server
            val postsListWithLocalImages = postsList.map {
                if (it.image != null) {
                    val newImageUrl = UrlChanger.toCurrentUrl(it.image.imageUrl)
                    it.copy(image = it.image.copy(imageUrl = newImageUrl))
                } else it
            }

            call.respond(
                SimpleResponse(
                    status = true,
                    message = "posts retrieved",
                    data = postsListWithLocalImages
                )
            )
        }

        get("$PATH/list") {
            val postsLimit = call.request.queryParameters["limit"]
            val startIndex = call.request.queryParameters["start_index"]

            val postsList = (posts.getPostsList(limit = postsLimit, offset = startIndex))
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "posts retrieved",
                    data = postsList
                )
            )
        }

        get("$PATH/{post_id}") {
            val id = call.parameters["post_id"] ?: ""
            val post = posts.getPostById(id)

            //TODO: remove later
            val postWithLocalImage = post?.copy(
                image = post.image?.copy(imageUrl = UrlChanger.toCurrentUrl(post.image.imageUrl))
            )

            val status = (post !== null)
            call.respond(
                SimpleResponse(
                    status = true,
                    message = if (status) "post retrieved" else "no post with id found",
                    data = if (status) postWithLocalImage!! else null
                )
            )
        }
    }

    authenticate("employee") {

        put("$PATH/edit") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.NEWS)) {
                call.respond(HttpStatusCode.Forbidden)
                return@put
            }
            val newPost = call.receiveNullable<IncomingPostModel>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            newPost.let {
                if (it.id == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }
            }
            val status = posts.updatePost(
                id = newPost.id!!,
                title = newPost.title,
                text = newPost.text,
                image = newPost.image
            )
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "post updated" else "error while updating the post",
                    data = Unit
                )
            )
        }

        post("$PATH/add") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.NEWS)) {
                call.respond(HttpStatusCode.Forbidden)
                return@post
            }
            val newPost = call.receiveNullable<IncomingPostModel>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val post = posts.addNewPost(
                title = newPost.title,
                text = newPost.text,
                image = newPost.image
            )
            val status = post != null
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "post added" else "error while adding the post",
                    data = post
                )
            )
            if (post != null) {
                notificationsManager.sendTranslatableNotificationByTopic(
                    topic = NotificationsTopicsCodes.NEWS,
                    translatableData = TranslatableNotificationData.NewPost(postTitle = post.title, postBody = post.text),
                    imageUrl = post.image?.imageUrl,
                    notificationChannel = NotificationChannel.News,
                    deepLink = Deeplink.Post(post.id)
                )
            }
        }

        delete("$PATH/delete") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.NEWS)) {
                call.respond(HttpStatusCode.Forbidden)
                return@delete
            }
            val postId = call.request.queryParameters["post_id"]
            val status = posts.deletePost(postId.toString())
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "post deleted" else "error while deleting the post",
                    data = Unit
                )
            )
        }
    }
}