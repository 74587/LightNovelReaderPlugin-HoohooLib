package io.limao996.hoohoolib.xxread

import android.content.Context
import cxhttp.CxHttp
import cxhttp.CxHttpHelper
import io.limao996.hoohoolib.utils.KotlinSerializationCborConverter
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.nightfish.lightnovelreader.api.book.BookRepositoryApi
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfRepositoryApi
import io.nightfish.lightnovelreader.api.text.TextProcessingRepositoryApi
import io.nightfish.lightnovelreader.api.userdata.UserDataDaoApi
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi
import io.nightfish.lightnovelreader.api.web.WebDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val XXREAD_HOST = "https://xxread.net"

@Suppress("unused")
@WebDataSource(
    name = "肉肉阅读🔞", provider = "HoohooLib from xxread.net"
)
class XxreadWebDataSource(
    val context: Context,
    val userDataDaoApi: UserDataDaoApi,
    val userDataRepositoryApi: UserDataRepositoryApi,
    val webBookDataSourceManagerApi: WebBookDataSourceManagerApi,
    val textProcessingRepositoryApi: TextProcessingRepositoryApi,
    val localBookDataSourceApi: LocalBookDataSourceApi,
    val bookRepositoryApi: BookRepositoryApi,
    val bookshelfRepositoryApi: BookshelfRepositoryApi,
) : WebBookDataSource {
    override val id = "io.limao996.hoohoolib:xxread".hashCode()

    private var coroutineScope = CoroutineScope(Dispatchers.IO)

    override var offLine: Boolean = false
    override val isOffLineFlow = MutableStateFlow(false)
    override suspend fun isOffLine(): Boolean = withContext(Dispatchers.IO) {
        !CxHttp.get(XXREAD_HOST) {
            header("user-agent", UserAgentGenerator().generateAndroidUA())
        }.await().isSuccessful
    }

    override val cache = Cache(
        timeout = 2 * 60 * 60 * 1000
    )

    private inline fun <reified T : CanBeEmpty> ifCache(id: String, block: () -> T): T {
        val cacheData = cache.getCache<T>(id.hashCode())
        if (cacheData == null) {
            val data = block.invoke()
            if (data.isEmpty()) return data
            cache.cache(id.hashCode(), data)
            return data
        }
        return cacheData
    }

    override fun onLoad() {
        @Suppress("OPT_IN_USAGE") CxHttpHelper.init(
            scope = MainScope(), debugLog = true, converter = KotlinSerializationCborConverter()
        )

        coroutineScope.launch {
            while (currentCoroutineContext().isActive) {
                offLine = isOffLine()
                isOffLineFlow.emit(offLine)
                delay(if (offLine) 1000 else 60000)
            }
        }
    }

    override val searchProvider = XxreadSearchProvider
    override val explorePageProvider = object : ExplorePageProvider.DefaultExplorePageProvider {
        override val explorePageIdList: List<String> = emptyList()
        override val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource> = emptyMap()
        override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> =
            emptyMap()
    }

    override suspend fun getBookInformation(id: String) = ifCache(id) { XxreadBookInformation(id) }

    override suspend fun getBookVolumes(id: String) = ifCache(id) { XxreadBookVolumes(id) }

    override suspend fun getChapterContent(chapterId: String, bookId: String) =
        ifCache(chapterId + bookId) {
            XxreadChapterContent(chapterId)
        }
}
