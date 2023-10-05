package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.model.PostModel
import net.glazov.data.response.SimplePostResponse
import net.glazov.database.*

private const val APIKEY = "test_api_key_123"
private const val PATH = "/api/posts"

fun Route.postRoutes() {

    get("$PATH/getall") {
        val posts = getAllPosts()
        call.respond(
            SimplePostResponse(
                status = true,
                message = "${posts.size} posts retrieved",
                data = posts
            )
        )
    }

    get("$PATH/getposts") {
        val postsLimit = call.request.queryParameters["limit"]
        val startIndex = call.request.queryParameters["start_index"]

        val posts = (getPostsList(limit = postsLimit, startIndex = startIndex))
        call.respond(
            SimplePostResponse(
                status = true,
                message = "${posts.size} posts retrieved",
                data = posts
            )
        )
    }

    get("$PATH/get") {
        val id = call.request.queryParameters["post_id"]
        val post = getPostById(id.toString())
        val status = (post !== null)
        call.respond(
            SimplePostResponse(
                status = status,
                message = if (status) "post retrieved" else "no post with id found",
                data = listOf(post)
            )
        )
    }

    put("$PATH/edit") {
        val api = call.request.queryParameters["api_key"]
        if (api == APIKEY) {
            val newPost = try {
                call.receive<PostModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val status = updatePostByRef(newPost)
            call.respond(
                SimplePostResponse(
                    status = status,
                    message = if (status) "post updated" else "error while updating the post",
                    data = emptyList()
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    post("$PATH/add") {
        val api = call.request.queryParameters["api_key"]
        if (api == APIKEY) {
            val newPost = try {
                call.receive<PostModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val status = addNewPost(newPost)
            call.respond(
                SimplePostResponse(
                    status = status,
                    message = if (status) "post added" else "error while adding the post",
                    data = emptyList()
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    delete("$PATH/delete") {
        val api = call.request.queryParameters["api_key"]
        if (api == APIKEY) {
            val postId = call.request.queryParameters["post_id"]
            val status = deletePostById(postId.toString())
            call.respond(
                SimplePostResponse(
                    status = status,
                    message = if (status) "post deleted" else "error while deleting the post",
                    data = emptyList()
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }
}