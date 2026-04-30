package io.limao996.hoohoolib.xxread

import android.net.Uri
import androidx.core.net.toUri
import cxhttp.CxHttp
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.LocalDateTime

suspend fun XxreadBookInformation(
    id: String
): BookInformation {
    val sourceUuid = extractSourceUuid(id) ?: run {
        return MutableBookInformation(
            id = id,
            title = "暂无标题",
            author = "未知",
            publishingHouse = "肉肉阅读🔞",
            wordCount = WordCount(0),
            lastUpdated = LocalDateTime.now(),
            isComplete = false,
            subtitle = "",
            coverUrl = Uri.EMPTY,
            description = "",
            tags = emptyList()
        )
    }

    val apiResponse = withContext(Dispatchers.IO) {
        CxHttp.get("$XXREAD_HOST/getBook.php?b=$sourceUuid") {
            header("user-agent", UserAgentGenerator().generateAndroidUA())
        }.await().body?.string()
    } ?: return MutableBookInformation(
        id = id,
        title = "暂无标题",
        author = "未知",
        publishingHouse = "肉肉阅读🔞",
        wordCount = WordCount(0),
        lastUpdated = LocalDateTime.now(),
        isComplete = false,
        subtitle = "",
        coverUrl = Uri.EMPTY,
        description = "",
        tags = emptyList()
    )

    val json = JSONObject(apiResponse)

    return MutableBookInformation(
        id = id,
        title = "暂无标题",
        subtitle = "",
        coverUrl = Uri.EMPTY,
        author = "未知",
        description = "",
        tags = emptyList(),
        publishingHouse = "肉肉阅读🔞",
        wordCount = WordCount(0),
        lastUpdated = LocalDateTime.now(),
        isComplete = false
    )
}

internal suspend fun extractSourceUuid(id: String): String? {
    val fullUrl = if (id.startsWith("http")) id else "$XXREAD_HOST$id"
    val response = withContext(Dispatchers.IO) {
        CxHttp.get(fullUrl) {
            header("user-agent", UserAgentGenerator().generateAndroidUA())
        }.await().body?.string()
    } ?: return null

    return Regex("sourceUuid=(\\d+)").find(response)?.groupValues?.get(1)
}
