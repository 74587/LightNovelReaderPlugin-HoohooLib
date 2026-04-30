package io.limao996.hoohoolib.xxread

import android.net.Uri
import cxhttp.CxHttp
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import io.nightfish.lightnovelreader.api.util.local
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import io.nightfish.lightnovelreader.api.web.search.SearchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

object XxreadSearchProvider : SearchProvider {
    override val searchTypes: List<SearchType> = listOf(
        SearchType("_all", "综合搜索".local(), "请输入关键词".local()),
    )

    override fun search(
        searchType: SearchType, keyword: String
    ): Flow<SearchResult> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")
        var currentPage = 0
        while (currentCoroutineContext().isActive) {
            val response = withContext(Dispatchers.IO) {
                CxHttp.get("$XXREAD_HOST/search.php?key=$q&page=${++currentPage}&type=book&docType=html") {
                    header("user-agent", UserAgentGenerator().generateAndroidUA())
                }.await().body?.string()
            }

            if (response == null) {
                if (currentPage == 1) emit(SearchResult.Error("网页请求失败！"))
                break
            }

            val json = JSONObject(response)
            val html = json.optString("html", "")
            if (html.isEmpty()) {
                if (currentPage == 1) emit(SearchResult.Empty())
                break
            }

            val soup = Jsoup.parse(html)
            val items = soup.select(".book")
            if (items.isEmpty()) break

            for (item in items) {
                val links = item.select("a")
                if (links.size < 2) continue
                val bookUrl = links[1].attr("href")
                val title = links[1].text()
                val author = item.selectFirst(".author")?.text() ?: ""
                val intro = item.selectFirst(".disc")?.text() ?: ""
                val isComplete = item.select(".wan").isNotEmpty()

                emit(
                    SearchResult.MultipleBook(
                        MutableBookInformation(
                            id = bookUrl,
                            title = title.ifEmpty { "暂无标题" },
                            subtitle = "",
                            coverUrl = Uri.EMPTY,
                            author = author,
                            description = intro,
                            tags = emptyList(),
                            publishingHouse = "肉肉阅读🔞",
                            wordCount = WordCount(0),
                            lastUpdated = LocalDateTime.now(),
                            isComplete = isComplete
                        )
                    )
                )
            }
            delay(2.seconds)
        }
        emit(SearchResult.End())
    }.flowOn(Dispatchers.IO)
}
