package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

//test area
fun Route.testRoutes() {
    //query arguments
    get("/") {
        val param1 = call.request.queryParameters["param1"]
        println(param1)
        println("URI: ${call.request.uri}")
        println("user-agent: ${call.request.userAgent()}")
        println("query-param: ${call.request.queryParameters.names()}")

        call.respondText("Hello there")
    }
    //url-arguments
    get("/posts/{id}") {
        println(call.parameters.names())
        val postId = call.parameters["id"]

        call.respondText("You are at post number: $postId")
    }
    //post request
    post ("/post_configurator") {
        val newPost = call.receive<PostExample>()
        println("title: ${newPost.title}")

        call.respondText("Wanting to create new post?")
    }
    //json respond
    get("/post") {
        val postResponse = PostResponse(id = 15, title = "Hey i'm a post")

        //adding headers
        call.response.headers.append("serverName", "ktor-server")
        call.respond(postResponse)
    }

    get("/post/download") {
        val file = File("src/main/resources/static/images/error_incomplete_state.png")

        //скачивание файла
//        call.response.header(
//            HttpHeaders.ContentDisposition,               //Тег хедера, отвечающего за тип файла
//            ContentDisposition.Attachment.withParameter(  //Значение хедера, отвечающего за тип файла
//                ContentDisposition.Parameters.FileName, "incomplete_state.jpg"            //параметры
//            ).toString()
//        ) //Отмечаем файл, как то, что нужно загрузить
        //открытие файла в браузере
        call.response.header(
            HttpHeaders.ContentDisposition, //Тег хедера, отвечающего за тип файла
            ContentDisposition.Inline.withParameter( //Значение хедера, отвечающего за тип файла
                ContentDisposition.Parameters.FileName, "incomplete_state.jpg" //параметры
            ).toString()
        )
        call.respondFile(file)
    }
}

@Serializable
data class PostExample(
    val id: Int,
    val title: String
)

@Serializable
data class PostResponse(
    val id: Int,
    val title: String
)
