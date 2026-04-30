package io.limao996.hoohoolib.bcshuku

import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume

suspend fun BcshukuBookVolumes(id: String): BookVolumes {
    val soup = httpGet("$BCSHUKU_HOST$id")

    val chapterElements = soup?.select("#list-chapter ul.list-chapter li a") ?: emptyList()
    val chapters = chapterElements.map { el ->
        val href = el.attr("href")
        val fullHref = if (href.startsWith("http")) href else "$BCSHUKU_HOST$href"
        val name = el.selectFirst("span.chapter-text")?.text() ?: el.text()
        ChapterInformation(
            id = fullHref,
            title = name,
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
