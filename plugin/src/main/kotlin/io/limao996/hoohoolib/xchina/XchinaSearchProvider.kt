package io.limao996.hoohoolib.xchina

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

object XchinaSearchProvider : SearchProvider {
    override val searchTypes: List<SearchType> = listOf(
        SearchType("_all", "综合搜索".local(), "请输入关键词".local()),
    )

    override fun search(
        searchType: SearchType, keyword: String
    ): Flow<SearchResult> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")
        var currentPage = 0
        while (currentCoroutineContext().isActive) {
            val soup = httpGet("$XCHINA_HOST/fictions/keyword-$q/${++currentPage}.html")

            if (soup == null) {
                emit(SearchResult.Error("网页请求失败！"))
                return@flow
            }

            val items = soup.select(".list.fiction-list .item.fiction")
            if (items.isEmpty()) {
                if (currentPage == 1) emit(SearchResult.Empty())
                break
            }

            for (item in items) {
                val titleEl = item.selectFirst(".text > .title > a") ?: continue
                var title = titleEl.text()
                title = title.replace(Regex("【完】|[|　\"()/（）《》〈〉【】〖〗［］\\\\[\\\\]]|阅读全文|作者.*|\\d{2}-\\d{2}"), "").trim()
                val bookUrl = titleEl.attr("href")
                val intro = item.selectFirst(".text > .brief")?.text() ?: ""

                emit(
                    SearchResult.MultipleBook(
                        MutableBookInformation(
                            id = bookUrl,
                            title = title.ifEmpty { "暂无标题" },
                            subtitle = "",
                            coverUrl = Uri.EMPTY,
                            author = "",
                            description = intro,
                            tags = emptyList(),
                            publishingHouse = "小黄书🔞",
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
