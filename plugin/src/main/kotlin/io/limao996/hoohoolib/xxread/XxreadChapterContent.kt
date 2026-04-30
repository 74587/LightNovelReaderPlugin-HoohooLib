package io.limao996.hoohoolib.xxread

import android.util.Base64
import cxhttp.CxHttp
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.MutableChapterContent
import io.nightfish.lightnovelreader.api.content.builder.ContentBuilder
import io.nightfish.lightnovelreader.api.content.builder.simpleText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

suspend fun XxreadChapterContent(
    chapterId: String
): ChapterContent {
    val parts = chapterId.split("|")
    if (parts.size < 3) return ChapterContent.empty(chapterId)

    val sourceUuid = parts[0]
    val articleUuid = parts[1]
    val bigContentId = parts[2]

    val params = "sourceUuid=$sourceUuid&articleUuid=$articleUuid&bigContentId=$bigContentId"
    val apiResponse = withContext(Dispatchers.IO) {
        CxHttp.get("$XXREAD_HOST/getArticleContent.php?$params") {
            header("user-agent", UserAgentGenerator().generateAndroidUA())
        }.await().body?.string()
    } ?: return ChapterContent.empty(chapterId)

    val json = JSONObject(apiResponse)
    val encodedContent = json.optString("content", "")

    if (encodedContent.isEmpty()) return ChapterContent.empty(chapterId)

    val decodedContent = try {
        String(Base64.decode(encodedContent, Base64.DEFAULT))
    } catch (e: Exception) {
        encodedContent
    }

    return MutableChapterContent(
        id = chapterId,
        title = "",
        content = ContentBuilder().apply {
            simpleText(decodedContent)
        }.build(),
        lastChapter = "",
        nextChapter = ""
    )
}
