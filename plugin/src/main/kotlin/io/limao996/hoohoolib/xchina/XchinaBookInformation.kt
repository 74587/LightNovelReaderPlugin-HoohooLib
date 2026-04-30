package io.limao996.hoohoolib.xchina

import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime

suspend fun XchinaBookInformation(
    id: String
): BookInformation {
    val soup = httpGet("$XCHINA_HOST$id")

    val title = soup?.selectFirst(".fiction-overview-info-item.title")?.text() ?: "暂无标题"
    val author = soup?.selectFirst(".fiction-overview-info-item.author a")?.text() ?: "未知"
    val description = soup?.selectFirst(".fiction-overview-brief")?.text() ?: "暂无简介"
    val kinds = soup?.select(".fiction-overview-info-item.tags .tag")?.map { it.text() } ?: emptyList()
    val lastChapter = soup?.selectFirst(".fiction-overview-info-item.chapter-count")?.text() ?: ""
    val wordCountText = soup?.selectFirst(".fiction-overview-info-item.word-count")?.text() ?: ""

    val wordCount = Regex("(\\d+(\\.\\d+)?)").find(wordCountText)?.groupValues?.get(1)
        ?.toFloatOrNull()?.toInt() ?: 0

    return MutableBookInformation(
        id = id,
        title = title,
        subtitle = "",
        coverUrl = Uri.EMPTY,
        author = author,
        description = description,
        tags = kinds,
        publishingHouse = "小黄书🔞",
        wordCount = WordCount(wordCount),
        lastUpdated = LocalDateTime.now(),
        isComplete = false
    )
}
