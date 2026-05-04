package io.limao996.hoohoolib.jm18

import android.content.Context
import android.net.Uri
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.limao996.hoohoolib.jm18.explore.Jm18ExplorePageProvider
import io.limao996.hoohoolib.jm18.utils.ImageDecryptServer
import io.limao996.hoohoolib.utils.UserAgentGenerator
import io.limao996.hoohoolib.utils.debugLog
import io.limao996.hoohoolib.utils.httpClient
import io.limao996.hoohoolib.utils.warnLog
import io.nightfish.lightnovelreader.api.book.BookInformation
import io.nightfish.lightnovelreader.api.book.BookRepositoryApi
import io.nightfish.lightnovelreader.api.book.BookVolumes
import io.nightfish.lightnovelreader.api.book.CanBeEmpty
import io.nightfish.lightnovelreader.api.book.ChapterContent
import io.nightfish.lightnovelreader.api.book.LocalBookDataSourceApi
import io.nightfish.lightnovelreader.api.book.Volume
import io.nightfish.lightnovelreader.api.bookshelf.BookshelfRepositoryApi
import io.nightfish.lightnovelreader.api.text.TextProcessingRepositoryApi
import io.nightfish.lightnovelreader.api.userdata.UserDataDaoApi
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi
import io.nightfish.lightnovelreader.api.util.Cache
import io.nightfish.lightnovelreader.api.web.WebBookDataSource
import io.nightfish.lightnovelreader.api.web.WebBookDataSourceManagerApi
import io.nightfish.lightnovelreader.api.web.WebDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

const val JM18_HOST = "https://18mh.net"
var JM18_HTTP_PORT = 37082


@Suppress("unused")
@WebDataSource(
    name = "禁漫天堂🔞🛩", provider = "HoohooLib from 18mh.net"
)
class Jm18WebDataSource(
    val context: Context,
    val userDataDaoApi: UserDataDaoApi,
    val userDataRepositoryApi: UserDataRepositoryApi,
    val webBookDataSourceManagerApi: WebBookDataSourceManagerApi,
    val textProcessingRepositoryApi: TextProcessingRepositoryApi,
    val localBookDataSourceApi: LocalBookDataSourceApi,
    val bookRepositoryApi: BookRepositoryApi,
    val bookshelfRepositoryApi: BookshelfRepositoryApi,
) : WebBookDataSource {
    val tag = "io.limao996.hoohoolib:jm18"
    override val id = tag.hashCode()

    private var coroutineScope = CoroutineScope(Dispatchers.IO)

    override var offLine: Boolean = false
    override val isOffLineFlow = MutableStateFlow(false)
    override suspend fun isOffLine(): Boolean = withContext(Dispatchers.IO) {
        try {
            httpClient.get(JM18_HOST) {
                header(
                    "user-agent", UserAgentGenerator().generateWindowsUA()
                )
            }.status != HttpStatusCode.OK
        } catch (_: Exception) {
            true
        }
    }

    override val cache = Cache(
        timeout = 2 * 60 * 60 * 1000
    )

    override fun onLoad() {

        coroutineScope.launch {
            while (currentCoroutineContext().isActive) {
                offLine = isOffLine()
                isOffLineFlow.emit(offLine)
                delay(if (offLine) 1000 else 60000)
            }
        }

        coroutineScope.launch {
            while (true) {
                try {
                    debugLog("ImageDecryptServer Start.", "Port: $JM18_HTTP_PORT")
                    ImageDecryptServer(JM18_HTTP_PORT).start()
                } catch (e: Exception) {
                    debugLog(
                        "ImageDecryptServer Start failed.", "The port is occupied!", e
                    )
                    JM18_HTTP_PORT = Random.nextInt(10000, 65535)
                    continue
                }
                val status = httpClient.get("http://127.0.0.1:$JM18_HTTP_PORT/ping").status
                debugLog("ImageDecryptServer Ping.", "Status code: $status")
                if (status == HttpStatusCode.OK) {
                    break
                }
                warnLog("ImageDecryptServer Ping failed.", "Status code: $status")
                JM18_HTTP_PORT = Random.nextInt(10000, 65535)
            }
        }
    }

    override suspend fun getCoverUriInVolume(
        bookId: String,
        volume: Volume,
        volumeChapterContentMap: MutableMap<String, ChapterContent>,
        context: Context
    ): Uri? {
        return super.getCoverUriInVolume(bookId, volume, volumeChapterContentMap, context)
    }

    override val searchProvider = Jm18SearchProvider
    override val explorePageProvider = Jm18ExplorePageProvider

    override suspend fun getBookInformation(id: String): BookInformation = Jm18BookInformation(id)

    override suspend fun getBookVolumes(id: String): BookVolumes = Jm18BookVolumes(id)


    override suspend fun getChapterContent(chapterId: String, bookId: String): ChapterContent =
        Jm18ChapterContent(
            chapterId, bookId, localBookDataSourceApi
        )

}
