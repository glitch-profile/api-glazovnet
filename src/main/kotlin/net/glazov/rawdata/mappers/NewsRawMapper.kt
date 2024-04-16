package net.glazov.rawdata.mappers

import net.glazov.data.model.posts.InnerPostModel
import net.glazov.rawdata.dto.InnerNewsDto

fun InnerNewsDto.toInnerPostModel(): InnerPostModel {
    val formattedTitle = if (this.title.isEmpty()) null
        else this.title.replace("<br/>", "\n")
    val formattedText = this.text.replace("<br/>", "\n")
    return InnerPostModel(
        id = this.id,
        title = formattedTitle,
        text = formattedText,
        creationDate = this.dateTimestamp
    )
}