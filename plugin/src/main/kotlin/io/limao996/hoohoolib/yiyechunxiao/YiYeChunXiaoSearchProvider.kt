package io.limao996.hoohoolib.yiyechunxiao

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

object YiYeChunXiaoSearchProvider : SearchProvider {
    override val searchTypes: List<SearchType> = listOf(
        SearchType("_all", "综合搜索".local(), "请输入关键词".local()),
    )

    override fun search(
        searchType: SearchType, keyword: String
    ): Flow<SearchResult> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")
        val soup = httpGet("$YIYECHUNXIAO_HOST/search/?q=$q")

        if (soup == null) {
            emit(SearchResult.Error("网页请求失败！"))
            return@flow
        }

        val items = soup.select(".novel-item")
        if (items.isEmpty()) {
            emit(SearchResult.Empty())
            return@flow
        }

        for (item in items) {
            val titleEl = item.selectFirst(".title")
            val title = titleEl?.text() ?: "暂无标题"
            val bookUrl = titleEl?.attr("href") ?: continue

            val author = item.selectFirst(".author")?.text() ?: "未知"
            val intro = item.selectFirst(".desc")?.text() ?: "暂无简介"
            val coverUrl = item.selectFirst(".cover img")?.attr("data-src")?.let { Uri.parse(it) } ?: Uri.EMPTY

            emit(
                SearchResult.MultipleBook(
                    MutableBookInformation(
                        id = bookUrl,
                        title = title,
                        subtitle = "",
                        coverUrl = coverUrl,
                        author = author,
                        description = intro,
                        tags = emptyList(),
                        publishingHouse = "辣肉文🔞",
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
