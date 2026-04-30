package io.limao996.hoohoolib.seseclub2

import io.limao996.hoohoolib.utils.FontDecoder
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.simpleText

suspend fun Seseclub2ChapterContent(
    chapterId: String,
    bookId: String,
    localBookDataSourceApi: LocalBookDataSourceApi,
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

    val volumes = localBookDataSourceApi.getBookVolumes(bookId)!!.volumes
    val flatChapter = volumes.flatMap { volume -> volume.chapters }
    val flatChapterIds = flatChapter.map { it.id }
    val currentIndex = flatChapterIds.indexOf(chapterId)
    val prevId = flatChapterIds.getOrNull(currentIndex - 1)
    val nextId = flatChapterIds.getOrNull(currentIndex + 1)

    return MutableChapterContent(
        id = chapterId,
        title = title,
        content = ContentBuilder().apply {
            simpleText(decoded)
        }.build(),
        lastChapter = prevId ?: "",
        nextChapter = nextId ?: ""
    )
}
