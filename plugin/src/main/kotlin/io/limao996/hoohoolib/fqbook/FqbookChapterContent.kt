package io.limao996.hoohoolib.fqbook

import androidx.core.net.toUri
import io.limao996.hoohoolib.utils.httpGet
import io.limao996.hoohoolib.utils.infoLog
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.image
import io.nightfish.lightnovelreader.api.content.builder.simpleText

suspend fun FqbookChapterContent(
    chapterId: String,
    bookId: String,
    localBookDataSourceApi: LocalBookDataSourceApi,
): ChapterContent {
    val doc = httpGet(
        "$FQBOOK_HOST/read-$chapterId.html", true
    ) ?: return ChapterContent.empty()
    val sign = Regex("""&v=([^"]+)""").find(doc.toString())?.groupValues?.get(
        1
    ) ?: return ChapterContent.empty()

    val soup = httpGet(
        "$FQBOOK_HOST/_getcontent.php?id=$chapterId&v=$sign", true
    ) ?: return ChapterContent.empty()

    val volumes = localBookDataSourceApi.getBookVolumes(bookId)!!.volumes
    val flatChapter = volumes.flatMap { volume -> volume.chapters }
    val flatChapterIds = flatChapter.map { it.id }
    val currentIndex = flatChapterIds.indexOf(chapterId)
    val prevId = flatChapterIds.getOrNull(currentIndex - 1)
    val nextId = flatChapterIds.getOrNull(currentIndex + 1)

    val title = flatChapter[currentIndex].title
    val content = soup.body().children()
    content.removeAt(0)

    return MutableChapterContent(
        id = chapterId, title = title, content = ContentBuilder().apply {
            val buffer = ArrayList<String>()
            content.forEach {
                when (it.tag().name) {
                    "p" -> it.wholeText().trim().split("\n").filter { it.isNotBlank() }
                        .also { if (it.isEmpty()) return@forEach }.joinToString("\n\n") {
                            "ㅤㅤ${it.trim()}"
                        }.let(buffer::add)

                    "img" -> {
                        if (buffer.isNotEmpty()) {
                            simpleText(buffer.joinToString("\n\n"))
                            buffer.clear()
                        }
                        image(it.attr("src").toUri())
                    }
                }

            }
            if (buffer.isNotEmpty()) {
                simpleText(buffer.joinToString("\n\n"))
                buffer.clear()
            }
        }.build(), lastChapter = prevId ?: "", nextChapter = nextId ?: ""
    )
}
