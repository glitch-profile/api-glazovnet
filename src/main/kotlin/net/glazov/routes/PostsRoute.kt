package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.*
import net.glazov.data.model.PostModel

const val APIKEY = "J3gHkW9iLp7vQzXrE5NtFmAsCfYbDqUo"

fun Route.postRoutes() {

    get("/posts") {
        val postsLimit = call.request.queryParameters["limit"]
        val startIndex = call.request.queryParameters["start_index"]

        call.respond(getPostsList(limit = postsLimit, startIndex = startIndex))
    }

    get("/post") {
        val id = call.request.queryParameters["post_id"]
        val post = getPostById(id.toString())
        post?.let {
            call.respond(it)
        } ?: call.respondText(text = "No post found!", status = HttpStatusCode.BadRequest)
    }

    post("/editpost") {
        val api = call.request.queryParameters["api_key"]
        if (api == APIKEY) {
            val newPost = try {
                call.receive<PostModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val status = updatePostById(newPost)
            if (status) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.BadRequest)
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    post("/addpost") {
        val api = call.request.queryParameters["api_key"]
        if (api == APIKEY) {
            val newPost = try {
                call.receive<PostModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val status = addNewPost(newPost)
            if (status) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.Conflict)
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    post("/deletepost") {
        val api = call.request.queryParameters["apikey"]
        if (api == APIKEY) {
            val postId = call.request.queryParameters["post_id"]
            val status = deletePostById(postId.toString())
            if (status) call.respond(HttpStatusCode.OK)
            else call.respond(HttpStatusCode.BadRequest)
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }
}