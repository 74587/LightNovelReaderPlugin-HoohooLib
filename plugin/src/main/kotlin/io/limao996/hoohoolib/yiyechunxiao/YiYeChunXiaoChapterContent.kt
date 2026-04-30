package io.limao996.hoohoolib.yiyechunxiao

import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.simpleText

suspend fun YiYeChunXiaoChapterContent(
    chapterId: String,
    bookId: String,
    localBookDataSourceApi: LocalBookDataSourceApi,
): ChapterContent {
    val fullUrl = if (chapterId.startsWith("http")) chapterId else "$YIYECHUNXIAO_HOST$chapterId"
    val soup = httpGet(fullUrl)

    val title = soup?.selectFirst(".title")?.text() ?: "未知"
    val contentHtml = soup?.selectFirst(".content")?.html() ?: ""

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
            simpleText(contentHtml)
        }.build(),
        lastChapter = prevId ?: "",
        nextChapter = nextId ?: ""
    )
}
