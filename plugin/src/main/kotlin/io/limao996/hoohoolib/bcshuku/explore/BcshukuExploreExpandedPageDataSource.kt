package io.limao996.hoohoolib.bcshuku.explore

import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.filter.SingleChoiceFilter
import io.nightfish.lightnovelreader.api.web.search.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

class BcshukuExploreExpandedPageDataSource(val category: io.limao996.hoohoolib.bcshuku.explore.ExploreCategory) :
    ExploreExpandedPageDataSource {
    override val title = category.name
    override val filters: List<SingleChoiceFilter> = emptyList()

    var targetPage = 1
    override fun loadMore() {
        targetPage += 1
    }

    override fun getResultFlow(): Flow<SearchResult> = flow {
        targetPage = 1
        var currentPage = 1

        while (currentCoroutineContext().isActive) {
            if (targetPage < currentPage) {
                delay(100)
                continue
            }
            val bookList = loadSimpleBookList(
                category,
                currentPage,
            )
            if (bookList.isEmpty()) break
            bookList.forEach {
                emit(SearchResult.MultipleBook(it))
            }
            currentPage++
            delay(1.seconds)
        }
        emit(SearchResult.End())
    }.flowOn(Dispatchers.IO)


}