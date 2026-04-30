package io.limao996.hoohoolib.seseclub

import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume

suspend fun SeseclubBookVolumes(id: String): BookVolumes {
    val soup = httpGet("$SESECLUB_HOST$id")

    val chapterElements = soup?.select("#chapterlsit a") ?: emptyList()
    val chapters = chapterElements.map { el ->
        ChapterInformation(
            id = el.attr("href"),
            title = el.text(),
        )
    }

    return BookVolumes(
        id, listOf(
            Volume(
                volumeId = id, volumeTitle = "正文", chapters = chapters
            )
        )
    )
}
