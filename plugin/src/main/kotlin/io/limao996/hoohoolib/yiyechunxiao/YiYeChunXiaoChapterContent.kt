package io.limao996.hoohoolib.yiyechunxiao

import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.simpleText

suspend fun YiYeChunXiaoChapterContent(
    chapterId: String
): ChapterContent {
    val fullUrl = if (chapterId.startsWith("http")) chapterId else "$YIYECHUNXIAO_HOST$chapterId"
    val soup = httpGet(fullUrl)

    val title = soup?.selectFirst(".title")?.text() ?: "未知"
    val contentHtml = soup?.selectFirst(".content")?.html() ?: ""

    return MutableChapterContent(
        id = chapterId,
        title = title,
        content = ContentBuilder().apply {
            simpleText(contentHtml)
        }.build(),
        lastChapter = "",
        nextChapter = ""
    )
}
