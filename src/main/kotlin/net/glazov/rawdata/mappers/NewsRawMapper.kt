package net.glazov.rawdata.mappers

import net.glazov.data.model.posts.InnerPostModel
import net.glazov.rawdata.dto.InnerNewsDto

fun InnerNewsDto.toInnerPostModel(): InnerPostModel {
    return InnerPostModel(
        id = this.id,
        title = this.title,
        text = this.text,
        creationDate = this.dateTimestamp
    )
}