package io.limao996.hoohoolib.fqbook

import android.net.Uri
import io.limao996.hoohoolib.utils.httpGet
import io.nightfish.lightnovelreader.api.book.MutableBookInformation
import io.nightfish.lightnovelreader.api.book.WordCount
import io.nightfish.lightnovelreader.api.util.local
import io.nightfish.lightnovelreader.api.web.search.SearchProvider
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import io.nightfish.lightnovelreader.api.web.search.SearchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.URLEncoder
import java.time.LocalDateTime

object FqbookSearchProvider : SearchProvider {
    override val searchTypes: List<SearchType> = listOf(
        SearchType("_all", "综合搜索".local(), "请输入关键词".local()),
    )

    override fun search(
        searchType: SearchType, keyword: String
    ): Flow<SearchResult> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")
        val soup = httpGet("$FQBOOK_HOST/search.html?searchword=$q", useWindowsUA = true)

        if (soup == null) {
            emit(SearchResult.Error("网页请求失败！"))
            return@flow
        }

        // Try primary search result layout
        val items = soup.selectFirst(".sousuojieguo ul")
            ?.children()
            ?: soup.select(".yuepiaobang tbody tr")

        if (items.isEmpty()) {
            emit(SearchResult.Empty())
            return@flow
        }

        for (item in items) {
            val link = item.selectFirst("a") ?: item.selectFirst(".shuming a")
            val title = link?.text() ?: continue
            val bookUrl = link.attr("href")
            if (bookUrl.isEmpty()) continue

            val author = item.selectFirst(".author")
                ?: item.select("a").let { if (it.size > 1) it[1] else null }
            val authorText = author?.text() ?: ""

            val kind = item.selectFirst(".fenlei")?.text() ?: item.ownText()

            emit(
                SearchResult.MultipleBook(
                    MutableBookInformation(
                        id = bookUrl,
                        title = title,
                        subtitle = "",
                        coverUrl = Uri.EMPTY,
                        author = authorText,
                        description = "",
                        tags = listOfNotNull(kind.takeIf { it.isNotBlank() }),
                        publishingHouse = "疯情书库🔞",
                        wordCount = WordCount(0),
                        lastUpdated = LocalDateTime.now(),
                        isComplete = false
                    )
                )
            )
        }
        emit(SearchResult.End())
    }.flowOn(Dispatchers.IO)
}
