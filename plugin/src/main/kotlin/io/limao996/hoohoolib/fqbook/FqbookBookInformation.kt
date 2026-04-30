package io.limao996.hoohoolib.fqbook

import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime

suspend fun FqbookBookInformation(
    id: String
): BookInformation {
    val soup = httpGet("$FQBOOK_HOST$id", useWindowsUA = true)

    val title = soup?.title() ?: "暂无标题"
    val author = ""
    val description = soup?.selectFirst(".jianjieneirong")?.text() ?: "暂无简介"
    val coverUrl = soup?.selectFirst(".book_info_top_l img")?.attr("src")?.toUri() ?: Uri.EMPTY
    val lastChapter = soup?.selectFirst(".new_zhangjie1 a")?.text() ?: ""

    return MutableBookInformation(
        id = id,
        title = title,
        subtitle = "",
        coverUrl = coverUrl,
        author = author,
        description = description,
        tags = emptyList(),
        publishingHouse = "疯情书库🔞",
        wordCount = WordCount(0),
        lastUpdated = LocalDateTime.now(),
        isComplete = false
    )
}
