package io.limao996.hoohoolib.yiyechunxiao

import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume

suspend fun YiYeChunXiaoBookVolumes(id: String): BookVolumes {
    val soup = httpGet("$YIYECHUNXIAO_HOST$id")

    val tocUrl = soup?.select(".btns a")?.let { elements ->
        if (elements.size >= 2) elements[elements.size - 2].attr("href") else null
    }

    val chapterSoup = if (tocUrl != null) {
        val fullUrl = if (tocUrl.startsWith("http")) tocUrl else "$YIYECHUNXIAO_HOST$tocUrl"
        httpGet(fullUrl)
    } else {
        soup
    }

    val chapterElements = chapterSoup?.select(".chapter-list a") ?: emptyList()
    val chapters = chapterElements.map { el ->
        ChapterInformation(
            id = el.attr("href"),
            title = el.selectFirst("h4")?.text() ?: el.text(),
        )
    }

    val volumeId = id

    return BookVolumes(
        id, listOf(
            Volume(
                volumeId = volumeId, volumeTitle = "正文", chapters = chapters
            )
        )
    )
}
