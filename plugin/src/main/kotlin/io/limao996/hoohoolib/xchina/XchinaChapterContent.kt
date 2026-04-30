package io.limao996.hoohoolib.xchina

import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.simpleText

suspend fun XchinaChapterContent(
    chapterId: String
): ChapterContent {
    val fullUrl = if (chapterId.startsWith("http")) chapterId else "$XCHINA_HOST$chapterId"
    val soup = httpGet(fullUrl)

    val title = soup?.title() ?: "未知"
    val contentHtml = soup?.selectFirst(".fiction-body")?.html() ?: ""

    val cleaned = contentHtml
        .replace(Regex("（看精彩成人小说上《小黄书》.*?[a-z]*）"), "")
        .replace("导览", "")
        .replace("下一章", "")

    return MutableChapterContent(
        id = chapterId,
        title = title,
        content = ContentBuilder().apply {
            simpleText(cleaned)
        }.build(),
        lastChapter = "",
        nextChapter = ""
    )
}
