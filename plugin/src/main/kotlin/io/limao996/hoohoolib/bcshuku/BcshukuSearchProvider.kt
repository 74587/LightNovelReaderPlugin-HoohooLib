package io.limao996.hoohoolib.bcshuku

import android.net.Uri
import cxhttp.CxHttp
import cxhttp.response.body
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
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

object BcshukuSearchProvider : SearchProvider {
    override val searchTypes: List<SearchType> = listOf(
        SearchType("_all", "综合搜索".local(), "请输入关键词".local()),
    )

    override fun search(
        searchType: SearchType, keyword: String
    ): Flow<SearchResult> = flow {
        val q = URLEncoder.encode(keyword, "utf-8")
        val ua = UserAgentGenerator().generateAndroidUA()

        // Step 1: Get searchid from initial search request
        val initResponse = withContext(Dispatchers.IO) {
            CxHttp.get("$BCSHUKU_HOST/e/search/index.php?keyboard=$q&show=title,writer,byr&searchget=1") {
                header("user-agent", ua)
                header("referer", BCSHUKU_HOST)
            }.await()
        }

        val location = ""
        val searchidMatch = Regex("searchid=(\\d+)").find(location)
        val searchid = searchidMatch?.groupValues?.get(1)

        if (searchid == null) {
            // Try to extract from body if redirect wasn't followed
            val body = initResponse.body?.string() ?: ""
            val fallbackMatch = Regex("searchid=(\\d+)").find(body)
            val fallbackSearchid = fallbackMatch?.groupValues?.get(1)

            if (fallbackSearchid == null) {
                emit(SearchResult.Error("获取搜索ID失败！"))
                return@flow
            }

            searchAndParse(fallbackSearchid, keyword, ua)
        } else {
            searchAndParse(searchid, keyword, ua)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun kotlinx.coroutines.flow.FlowCollector<SearchResult>.searchAndParse(
        searchid: String, keyword: String, ua: String
    ) {
        var currentPage = 0
        while (currentCoroutineContext().isActive) {
            val url = if (currentPage == 0) {
                "$BCSHUKU_HOST/e/search/result/?searchid=$searchid"
            } else {
                "$BCSHUKU_HOST/e/search/result/index.php?page=$currentPage&searchid=$searchid"
            }

            val response = withContext(Dispatchers.IO) {
                CxHttp.get(url) {
                    header("user-agent", ua)
                    header("referer", BCSHUKU_HOST)
                }.await().body?.string()
            }

            if (response == null) {
                if (currentPage == 0) emit(SearchResult.Error("网页请求失败！"))
                break
            }

            val soup = Jsoup.parse(response)
            val items = soup.select(".one-row .col-md-3.col-sm-6.col-xs-6.home-truyendecu")

            if (items.isEmpty() && currentPage == 0) {
                emit(SearchResult.Empty())
                break
            }
            if (items.isEmpty()) break

            for (item in items) {
                val link = item.selectFirst(".each_truyen a")
                val bookUrl = link?.attr("href") ?: continue
                val title = item.selectFirst(".caption a")?.attr("title") ?: link?.attr("title")
                ?: "暂无标题"
                val coverUrl = item.selectFirst(".each_truyen a img")?.attr("src")?.let {
                    if (it.startsWith("http")) Uri.parse(it) else Uri.parse("$BCSHUKU_HOST$it")
                } ?: Uri.EMPTY

                emit(
                    SearchResult.MultipleBook(
                        MutableBookInformation(
                            id = bookUrl,
                            title = title,
                            subtitle = "",
                            coverUrl = coverUrl,
                            author = "",
                            description = "",
                            tags = emptyList(),
                            publishingHouse = "八叉书库🔞",
                            wordCount = WordCount(0),
                            lastUpdated = LocalDateTime.now(),
                            isComplete = false
                        )
                    )
                )
            }
            currentPage++
            delay(2.seconds)
        }
        emit(SearchResult.End())
    }
}
