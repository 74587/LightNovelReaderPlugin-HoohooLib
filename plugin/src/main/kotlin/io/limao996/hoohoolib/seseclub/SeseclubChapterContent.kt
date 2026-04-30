package io.limao996.hoohoolib.seseclub

import io.limao996.hoohoolib.utils.FontDecoder
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.simpleText

suspend fun SeseclubChapterContent(
    chapterId: String
): ChapterContent {
    val fullUrl = if (chapterId.startsWith("http")) chapterId else "$SESECLUB_HOST$chapterId"
    val soup = httpGet(fullUrl)

    val title = soup?.title() ?: "未知"
    val contentHtml = soup?.selectFirst(".chapter-content-wrap")?.html() ?: ""

    // Clean up navigation text and site address
    val cleaned = contentHtml
        .replace(Regex("[上|下]一章"), "")
        .replace(Regex("本站地址[^\n\t]*"), "")
        .replace("\t", "")

    // Decode font-scrambled content
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
