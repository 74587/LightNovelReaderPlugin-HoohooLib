package io.limao996.hoohoolib.bcshuku

import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

suspend fun BcshukuBookInformation(
    id: String
): BookInformation {
    val soup = httpGet("$BCSHUKU_HOST$id")

    val title = soup?.selectFirst(".desc h3")?.text() ?: "暂无标题"
    val author = soup?.selectFirst(".info-chitiet span a[itemprop=author]")?.text() ?: "未知"
    val description = soup?.selectFirst("[itemprop=description]")?.text() ?: "暂无简介"
    val coverUrl = soup?.selectFirst(".book img")?.attr("src")?.let { src ->
        if (src.startsWith("http")) src.toUri() else "$BCSHUKU_HOST$src".toUri()
    } ?: Uri.EMPTY
    val kinds =
        soup?.select(".info-chitiet span a[itemprop=genre]")?.map { it.text() } ?: emptyList()

    val timeText = soup?.selectFirst(".info .info-chitiet .text-primary")?.text()

    val isComplete = timeText?.let {
        val list = it.split(' ')
        list[0] == "完结"
    } ?: true
    val lastUpdated = timeText?.let {
        val list = it.split(' ')
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        LocalDateTime.parse("${list[1]} ${list[2]}", formatter)
    } ?: LocalDateTime.MIN

    return MutableBookInformation(
        id = id,
        title = title,
        subtitle = "",
        coverUrl = coverUrl,
        author = author,
        description = description,
        tags = kinds,
        publishingHouse = "八叉书库🔞",
        wordCount = WordCount(0),
        lastUpdated = lastUpdated,
        isComplete = isComplete
    )
}
