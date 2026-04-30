package io.limao996.hoohoolib.seseclub

import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime

suspend fun SeseclubBookInformation(
    id: String
): BookInformation {
    val soup = httpGet("$SESECLUB_HOST$id")

    val title = soup?.selectFirst("#bookinfo div h2")?.text() ?: "暂无标题"
    val author = soup?.selectFirst("#bookinfo p")?.text() ?: "未知"
    val coverUrl = soup?.selectFirst(".photo img")?.attr("src")?.toUri() ?: Uri.EMPTY
    val kinds = soup?.select("#bookinfo a")?.map { it.text() } ?: emptyList()
    val description = ""

    return MutableBookInformation(
        id = id,
        title = title,
        subtitle = "",
        coverUrl = coverUrl,
        author = author,
        description = description,
        tags = kinds,
        publishingHouse = "18涩涩俱乐部🔞",
        wordCount = WordCount(0),
        lastUpdated = LocalDateTime.now(),
        isComplete = false
    )
}
