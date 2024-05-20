package net.glazov.routes

import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.InnerDataSource
import net.glazov.data.datasource.InnerPostsDataSource
import net.glazov.data.model.posts.IncomingInnerPostData
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.notificationsmanager.*

private val PATH = "/api/inner-posts"
private val useProtectedInnerPosts = ApplicationConfig(null).tryGetString("inner_data.use_protected_inner_posts").toBoolean()

fun Route.innerPostsRoutes(
    innerDataSource: InnerDataSource,
    innerPostsDataSource: InnerPostsDataSource,
    notificationsManager: NotificationsManager
) {

    authenticate("employee") {

        get(PATH) {
            try {
                val result = if (useProtectedInnerPosts) innerDataSource.getAllInnerPosts()
                else innerPostsDataSource.getAllPosts()
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "inner posts retrieved",
                        data = result
                    )
                )
            } catch (e: ResponseException) {
                println("inner posts receive error - ${e.stackTrace}")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        get("$PATH/{post_id}") {
            val postId = call.parameters["post_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val result = innerPostsDataSource.getInnerPostById(postId)
            val status = result != null
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "post retrieved" else "unable to get post",
                    data = result
                )
            )
        }

        post("$PATH/create") {
            val incomingPost = call.receiveNullable<IncomingInnerPostData>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = innerPostsDataSource.addNewPost(
                postTitle = incomingPost.title,
                postText = incomingPost.text
            )
            val status = result != null
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "post added" else "unable to add post",
                    data = result
                )
            )
            if (status) {
                notificationsManager.sendTranslatableNotificationByTopic(
                    topic = NotificationsTopicsCodes.SERVICE_NEWS,
                    translatableData = TranslatableNotificationData.NewServicePost(
                        postTitle = result!!.title,
                        postBody = result!!.text
                    ),
                    notificationChannel = NotificationChannel.ServiceNews,
                    deepLink = Deeplink.ServicePosts
                )
            }
        }
    }

}