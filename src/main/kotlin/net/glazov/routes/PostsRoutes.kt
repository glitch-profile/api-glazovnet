package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.model.PostModel
import net.glazov.data.response.SimpleResponse
import net.glazov.database.*

private const val PATH = "/api/posts"

fun Route.postRoutes(
    apiKeyServer: String
) {

    get("$PATH/getall") {
        val posts = getAllPosts()
        call.respond(
            SimpleResponse(
                status = true,
                message = "posts retrieved",
                data = posts
            )
        )
    }

    get("$PATH/getposts") {
        val postsLimit = call.request.queryParameters["limit"]
        val startIndex = call.request.queryParameters["start_index"]

        val posts = (getPostsList(limit = postsLimit, offset = startIndex))
        call.respond(
            SimpleResponse(
                status = true,
                message = "posts retrieved",
                data = posts
            )
        )
    }

    get("$PATH/get") {
        val id = call.request.queryParameters["post_id"]
        val post = getPostById(id.toString())
        val status = (post !== null)
        call.respond(
            SimpleResponse(
                status = true,
                message = if (status) "post retrieved" else "no post with id found",
                data = if (status) listOf(post!!) else emptyList()
            )
        )
    }

    put("$PATH/edit") {
        val api = call.request.queryParameters["api_key"]
        if (api == apiKeyServer) {
            val newPost = try {
                call.receive<PostModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val status = updatePostByRef(newPost)
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "post updated" else "error while updating the post",
                    data = emptyList<PostModel>()
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    post("$PATH/add") {
        val api = call.request.queryParameters["api_key"]
        if (api == apiKeyServer) {
            val newPost = try {
                call.receive<PostModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val post = addNewPost(newPost)
            val status = post != null
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "post added" else "error while adding the post",
                    data = if (status) listOf(post!!) else emptyList()
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    delete("$PATH/delete") {
        val api = call.request.queryParameters["api_key"]
        if (api == apiKeyServer) {
            val postId = call.request.queryParameters["post_id"]
            val status = deletePostById(postId.toString())
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "post deleted" else "error while deleting the post",
                    data = emptyList<PostModel>()
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }
}