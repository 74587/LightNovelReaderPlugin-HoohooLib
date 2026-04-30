package io.limao996.hoohoolib.seseclub2

import android.net.Uri
import io.limao996.hoohoolib.utils.httpGet
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
import java.net.URLEncoder
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

object Seseclub2SearchProvider : SearchProvider {
    override val searchTypes: List<SearchType> = listOf(
        SearchType("_all", "综合搜索".local(), "请输入关键词".local()),
    )

    override fun search(
        searchType: SearchType, keyword: String
    ): Flow<SearchResult> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")
        var currentPage = 0
        while (currentCoroutineContext().isActive) {
            val soup = httpGet("$SESECLUB2_HOST/book/search/$q/${++currentPage}")

            if (soup == null) {
                emit(SearchResult.Error("网页请求失败！"))
                return@flow
            }

            val items = soup.select(".aglike li")
            if (items.isEmpty()) {
                if (currentPage == 1) emit(SearchResult.Empty())
                break
            }

            for (item in items) {
                val link = item.selectFirst("a") ?: continue
                val bookUrl = link.attr("href")
                val title = item.selectFirst(".name")?.text() ?: link.text()
                val author = item.selectFirst("span")?.text() ?: ""
                val intro = item.selectFirst(".decs")?.text() ?: ""
                val coverUrl = item.selectFirst("img")?.attr("src")?.let { Uri.parse(it) } ?: Uri.EMPTY

                emit(
                    SearchResult.MultipleBook(
                        MutableBookInformation(
                            id = bookUrl,
                            title = title.ifEmpty { "暂无标题" },
                            subtitle = "",
                            coverUrl = coverUrl,
                            author = author,
                            description = intro,
                            tags = emptyList(),
                            publishingHouse = "涩涩俱乐部-九梦🔞",
                            wordCount = WordCount(0),
                            lastUpdated = LocalDateTime.now(),
                            isComplete = false
                        )
                    )
                )
            }
            delay(2.seconds)
        }
        emit(SearchResult.End())
    }.flowOn(Dispatchers.IO)
}
