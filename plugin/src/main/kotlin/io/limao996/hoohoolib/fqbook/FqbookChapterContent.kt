package io.limao996.hoohoolib.fqbook

import cxhttp.CxHttp
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.simpleText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun FqbookChapterContent(
    chapterId: String
): ChapterContent {
    val fullChapterUrl = if (chapterId.startsWith("http")) chapterId else "$FQBOOK_HOST$chapterId"

    val ua = UserAgentGenerator().generateWindowsUA()

    // Step 1: Fetch chapter page to extract the v parameter
    val chapterResponse = withContext(Dispatchers.IO) {
        CxHttp.get(fullChapterUrl) {
            header("user-agent", ua)
        }.await()
    }
    val chapterHtml = chapterResponse.body?.string() ?: return ChapterContent.empty(chapterId)

    // Extract v parameter: &v=(some_value)"
    val vMatch = Regex("&v=([^\"]+)").find(chapterHtml)
    val vParam = vMatch?.groupValues?.get(1) ?: return ChapterContent.empty(chapterId)

    // Extract book id from the URL: read-XXXX.html → XXXX
    val bookIdMatch = Regex("read-(\\d+)\\.html").find(fullChapterUrl)
    val contentId = bookIdMatch?.groupValues?.get(1) ?: return ChapterContent.empty(chapterId)

    // Step 2: Fetch actual content from _getcontent.php
    val contentUrl = "$FQBOOK_HOST/_getcontent.php?id=$contentId&v=$vParam"
    val contentResponse = withContext(Dispatchers.IO) {
        CxHttp.get(contentUrl) {
            header("user-agent", ua)
        }.await()
    }
    val rawContent = contentResponse.body?.string() ?: ""

    // Clean up the content
    val cleaned = rawContent
        .replace(Regex("<style[^>]*>[\\s\\S]*?</style>"), "")
        .replace(Regex("<([^<]*?)class[^>]*?>"), "")

    return MutableChapterContent(
        id = chapterId,
        title = "",
        content = ContentBuilder().apply {
            simpleText(cleaned)
        }.build(),
        lastChapter = "",
        nextChapter = ""
    )
}
