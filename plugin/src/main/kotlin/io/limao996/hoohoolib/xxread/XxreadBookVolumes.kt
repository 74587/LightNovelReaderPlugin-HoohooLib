package io.limao996.hoohoolib.xxread

import cxhttp.CxHttp
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.ChapterInformation
import io.nightfish.lightnovelreader.api.book.Volume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

suspend fun XxreadBookVolumes(id: String): BookVolumes {
    val sourceUuid = extractSourceUuid(id) ?: return BookVolumes(id, emptyList())

    val apiResponse = withContext(Dispatchers.IO) {
        CxHttp.get("$XXREAD_HOST/getBook.php?b=$sourceUuid") {
            header("user-agent", UserAgentGenerator().generateAndroidUA())
        }.await().body?.string()
    } ?: return BookVolumes(id, emptyList())

    val json = JSONObject(apiResponse)
    val portions = json.optJSONArray("portions") ?: return BookVolumes(id, emptyList())

    val chapters = mutableListOf<ChapterInformation>()
    for (i in 1 until portions.length()) {
        val portion = portions.optJSONObject(i) ?: continue
        val title = portion.optString("title", "")
        val articleUuid = portion.optString("id", "")
        val bigContentId = portion.optString("bigContentId", "")
        val chapterId = "$sourceUuid|$articleUuid|$bigContentId"

        if (title.isNotEmpty()) {
            chapters.add(
                ChapterInformation(
                    id = chapterId,
                    title = title,
                )
            )
        }
    }

    return BookVolumes(
        id, listOf(
            Volume(
                volumeId = sourceUuid,
                volumeTitle = "正文",
                chapters = chapters
            )
        )
    )
}
