package io.limao996.hoohoolib.bcshuku.explore

import io.limao996.hoohoolib.bcshuku.BCSHUKU_HOST
import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource

data class ExploreCategory(
    val name: String, val path: String, val isSpecial: Boolean = false
) {
    fun getUrl(page: Int = 1): String {
        if (!isSpecial) return "$BCSHUKU_HOST/$path/${page - 1}.html"
        if (page == 1) return "$BCSHUKU_HOST/$path"
        return "$BCSHUKU_HOST/$path/index_$page.html"
    }
}

object BcshukuExplorePageProvider : ExplorePageProvider.DefaultExplorePageProvider {
    val categories = listOf(
        // 首页 (索引 0-3)
        ExploreCategory("📊 热门排行", "popular", true),   // 0
        ExploreCategory("⏰ 最新更新", "latest", true),    // 1
        ExploreCategory("📦 最新入库", "release", true),   // 2
        ExploreCategory("🏁 完本小说", "completed", true), // 3

        // 分类 (索引 4-12)
        ExploreCategory("📚 长篇", "booklist1"),  // 4
        ExploreCategory("🔀 综合", "booklist2"),  // 5
        ExploreCategory("🗡️ 武侠", "booklist3"),  // 6
        ExploreCategory("📜 历史", "booklist4"),  // 7
        ExploreCategory("🌆 都市", "booklist5"),  // 8
        ExploreCategory("🌙 玄幻", "booklist6"),  // 9
        ExploreCategory("💕 女生", "booklist7"),  // 10
        ExploreCategory("🎯 其他", "booklist8"),  // 11
        ExploreCategory("📲 现代", "booklist9"),  // 12
    )

    override val explorePageIdList: List<String> = listOf(
        "Home", "Categories"
    ) + categories.map { it.name }
    override val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource> = mapOf(
        "Home" to BcshukuCustomExploreTapPageDataSource("首页", 0, 3),
        "Categories" to BcshukuCustomExploreTapPageDataSource("分类", 4, 12),
    )
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> =
        run {
            val map = LinkedHashMap<String, ExploreExpandedPageDataSource>()
            categories.forEach {
                map[it.name] = BcshukuExploreExpandedPageDataSource(it)
            }
            map.toMap()
        }
}