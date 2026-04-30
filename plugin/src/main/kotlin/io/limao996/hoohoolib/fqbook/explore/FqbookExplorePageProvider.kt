package io.limao996.hoohoolib.fqbook.explore

import io.nightfish.lightnovelreader.api.web.explore.ExploreExpandedPageDataSource
import io.nightfish.lightnovelreader.api.web.explore.ExplorePageProvider
import io.nightfish.lightnovelreader.api.web.explore.ExploreTapPageDataSource

object FqbookExplorePageProvider : ExplorePageProvider.DefaultExplorePageProvider {
    object ExplorePageMap {
        val home = mapOf(
            "📚 海量书库" to FqbookExploreLoader.Order(),
            "🔥 热门完本" to FqbookExploreLoader.Order(
                updT = FqbookExploreLoader.Order.updateTime["三月内"]!!,
                isFinish = FqbookExploreLoader.Order.finishStatus["已完结"]!!,
                orderBy = FqbookExploreLoader.Order.orderBy["点击"]!!
            ),
            "⏳ 限时追更" to FqbookExploreLoader.Order(
                updT = FqbookExploreLoader.Order.updateTime["七日内"]!!,
                isFinish = FqbookExploreLoader.Order.finishStatus["连载中"]!!,
                orderBy = FqbookExploreLoader.Order.orderBy["更新"]!!
            ),
            "💎 口碑神作" to FqbookExploreLoader.Order(
                isFinish = FqbookExploreLoader.Order.finishStatus["已完结"]!!,
                orderBy = FqbookExploreLoader.Order.orderBy["点击"]!!
            ),
            "🌱 新书速递" to FqbookExploreLoader.Order(
                size = FqbookExploreLoader.Order.wordCount["30万以下"]!!,
                updT = FqbookExploreLoader.Order.updateTime["一月内"]!!,
                isFinish = FqbookExploreLoader.Order.finishStatus["连载中"]!!,
                orderBy = FqbookExploreLoader.Order.orderBy["发布"]!!
            ),
        )
        val categories = mapOf(
            "📖 穿越" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["穿越"]!!),
            "⚡ 异能" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["异能"]!!),
            "💖 言情" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["言情"]!!),
            "✨ 玄幻" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["玄幻"]!!),
            "🏫 校园" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["校园"]!!),
            "⛰️ 仙侠" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["仙侠"]!!),
            "🌾 乡土" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["乡土"]!!),
            "⚔️ 武侠" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["武侠"]!!),
            "🎮 网游" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["网游"]!!),
            "🎭 同人" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["同人"]!!),
            "👑 女尊" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["女尊"]!!),
            "🏯 历史" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["历史"]!!),
            "👻 惊悚" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["惊悚"]!!),
            "🏺 古典" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["古典"]!!),
            "🏛️ 官场" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["官场"]!!),
            "🏙️ 都市" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["都市"]!!),
            "📄 单篇" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["单篇"]!!),
            "🌈 耽美" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["耽美"]!!),
            "💼 职场" to FqbookExploreLoader.Order(catId = FqbookExploreLoader.Order.category["职场"]!!),
        )
    }

    override val explorePageIdList = listOf(
        "Home", "Categories"
    )
    override val exploreTapPageDataSourceMap: Map<String, ExploreTapPageDataSource> = mapOf(
        "Home" to FqbookCustomExploreTapPageDataSource(
            "首页", ExplorePageMap.home
        ),
        "Categories" to FqbookCustomExploreTapPageDataSource(
            "分类", ExplorePageMap.categories
        ),
    )
    override val exploreExpandedPageDataSourceMap: Map<String, ExploreExpandedPageDataSource> =
        run {
            val map = ExplorePageMap.home + ExplorePageMap.categories
            map.map { (title, order) ->
                title to FqbookCustomExploreExpandedPageDataSource(title, order)
            }.toMap()
        }

}