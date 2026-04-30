package io.limao996.hoohoolib.yiyechunxiao

import android.net.Uri
import androidx.core.net.toUri
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import java.time.LocalDateTime

suspend fun YiYeChunXiaoBookInformation(
    id: String
): BookInformation {
    val soup = httpGet("$YIYECHUNXIAO_HOST$id")

    val title = soup?.selectFirst(".info h1")?.text() ?: "暂无标题"
    val author = soup?.selectFirst(".info dl")?.text() ?: "未知"
    val description = soup?.selectFirst(".desc-more")?.text() ?: soup?.selectFirst(".desc-content-over")?.text() ?: "暂无简介"

    return MutableBookInformation(
        id = id,
        title = title,
        subtitle = "",
        coverUrl = Uri.EMPTY,
        author = author,
        description = description,
        tags = emptyList(),
        publishingHouse = "辣肉文🔞",
        wordCount = WordCount(0),
        lastUpdated = LocalDateTime.now(),
        isComplete = false
    )
}
