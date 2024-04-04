package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.PostsDataSource
import net.glazov.data.model.PostModel
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.notificationsmanager.*

private const val PATH = "/api/posts"

fun Route.postRoutes(
    posts: PostsDataSource,
    notificationsManager: NotificationsManager
) {

    authenticate {

        get("$PATH/") {
            val postsList = posts.getAllPosts()
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "posts retrieved",
                    data = postsList
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
            val status = (post !== null)
            call.respond(
                SimpleResponse(
                    status = true,
                    message = if (status) "post retrieved" else "no post with id found",
                    data = if (status) listOf(post!!) else emptyList()
                )
            )
        }
    }

    authenticate("admin") {

        put("$PATH/edit") {
            val newPost = try {
                call.receive<PostModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val status = posts.updatePost(newPost)
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "post updated" else "error while updating the post",
                    data = Unit
                )
            )
        }

        post("$PATH/add") {
            val newPost = call.receiveNullable<PostModel>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val post = posts.addNewPost(newPost)
            val status = post != null
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "post added" else "error while adding the post",
                    data = if (status) listOf(post) else emptyList()
                )
            )
            if (post != null) {
                notificationsManager.sendTranslatableNotificationToClientsByTopic(
                    topic = NotificationsTopicsCodes.NEWS,
                    translatableData = TranslatableNotificationData.NewPost(postTitle = post.title, postBody = post.text),
                    imageUrl = post.image?.imageUrl,
                    notificationChannel = NotificationChannel.News,
                    deepLink = Deeplink.Post(post.id)
                )
            }
        }

        delete("$PATH/delete") {
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