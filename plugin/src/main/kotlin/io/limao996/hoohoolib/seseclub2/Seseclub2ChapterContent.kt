package io.limao996.hoohoolib.seseclub2

import io.limao996.hoohoolib.utils.FontDecoder
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.simpleText

suspend fun Seseclub2ChapterContent(
    chapterId: String
): ChapterContent {
    val fullUrl = if (chapterId.startsWith("http")) chapterId else "$SESECLUB2_HOST$chapterId"
    val soup = httpGet(fullUrl)

    val title = soup?.title() ?: "未知"
    val contentHtml = soup?.selectFirst(".chapter-content-wrap")?.html() ?: ""

    val cleaned = contentHtml
        .replace(Regex("[上|下]一章"), "")
        .replace(Regex("本站地址[^\n\t]*"), "")
        .replace("\t", "")

    val decoded = FontDecoder.decode(cleaned)

    return MutableChapterContent(
        id = chapterId,
        title = title,
        content = ContentBuilder().apply {
            simpleText(decoded)
        }.build(),
        lastChapter = "",
        nextChapter = ""
    )
}
