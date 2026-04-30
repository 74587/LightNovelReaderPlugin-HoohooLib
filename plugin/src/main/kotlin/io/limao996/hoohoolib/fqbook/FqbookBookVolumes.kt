package io.limao996.hoohoolib.fqbook

import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume

suspend fun FqbookBookVolumes(id: String): BookVolumes {
    val soup = httpGet("$FQBOOK_HOST$id", useWindowsUA = true)

    val tocUrl = soup?.selectFirst(".xiaoshuomulu a")?.attr("href") ?: id

    val fullTocUrl = if (tocUrl.startsWith("http")) tocUrl else "$FQBOOK_HOST$tocUrl"
    val tocSoup = httpGet(fullTocUrl, useWindowsUA = true)

    val chapterElements = tocSoup?.select(".section_list li") ?: emptyList()
    val chapters = chapterElements.map { el ->
        val link = el.selectFirst("a") ?: el
        ChapterInformation(
            id = link.attr("href"),
            title = link.text(),
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
