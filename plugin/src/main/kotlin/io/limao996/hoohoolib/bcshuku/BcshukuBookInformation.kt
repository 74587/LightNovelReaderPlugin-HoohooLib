package io.limao996.hoohoolib.bcshuku

import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.utils.httpGet
import io.limao996.hoohoolib.utils.infoLog
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

suspend fun BcshukuBookInformation(
    id: String
): BookInformation {
    val soup = httpGet("$BCSHUKU_HOST$id")

    val title = soup?.selectFirst(".desc h3")?.text() ?: return BookInformation.empty()
    val author = soup.selectFirst(".info-chitiet span a[itemprop=author]")?.text() ?: ""
    val description =
        soup.selectFirst("[itemprop=description]")?.wholeText()?.replace("小说介绍", "")?.trim()
            ?.split("\n")?.filter { it.isNotBlank() }?.joinToString("\n") {
                "ㅤㅤ${it.trim()}"
            } ?: ""
    val coverUrl = soup.selectFirst(".book img")?.attr("src")?.let { src ->
        if (src.startsWith("http")) src.toUri() else "$BCSHUKU_HOST$src".toUri()
    } ?: Uri.EMPTY
    val kinds = soup.select(".info-chitiet span a[itemprop=genre]").map { it.text() }

    val timeText = soup.selectFirst(".info .info-chitiet .text-primary")?.text()?.let {
        if (it.split(' ').size < 3) "连载中 $it" else it
    }

    val isComplete = timeText?.let {
        val list = it.split(' ')
        list[0].contains("完结")
    } ?: true

    val lastUpdated = timeText?.let {
        try {
            val list = it.split(' ')
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            LocalDateTime.parse("${list[1]} ${list[2]}", formatter)
        } catch (e: Exception) {
            LocalDateTime.now()
        }
    } ?: LocalDateTime.now()

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
