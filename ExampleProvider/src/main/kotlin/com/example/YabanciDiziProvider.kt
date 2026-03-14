package com.csmrkstevn

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.nodes.Element

class YabanciDiziProvider : MainAPI() {
    override var mainUrl = "https://yabancidizi.so"
    override var name = "YabanciDizi"
    override val hasMainPage = true
    override var lang = "tr"
    override val supportedTypes = setOf(TvType.TvSeries)

    override val mainPage = mainPageOf(
        "$mainUrl/diziler/sayfa/" to "Tüm Diziler",
        "$mainUrl/en-cok-izlenen-diziler/sayfa/" to "Popüler Diziler",
        "$mainUrl/yeni-bolumler/sayfa/" to "Yeni Bölümler"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val document = app.get(request.data + page).document
        val home = document.select("div.a-box, div.film-listesi-bolum, article.item").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("h2, h3, .title, .isim")?.text() ?: return null
        val href = this.selectFirst("a")?.attr("href") ?: return null
        val posterUrl = this.selectFirst("img")?.let {
            it.attr("data-src").ifEmpty { it.attr("src") }
        }
        return newTvSeriesSearchResponse(title, href, TvType.TvSeries) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/arama?q=$query").document
        return document.select("div.a-box, div.film-listesi-bolum, article.item").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title = document.selectFirst("h1, .dizi-adi, .title")?.text()?.trim() ?: ""
        val posterUrl = document.selectFirst("img.poster, .dizi-kapak img")?.let {
            it.attr("data-src").ifEmpty { it.attr("src") }
        }
        val plot = document.selectFirst(".ozet, .summary, p")?.text()?.trim()
        val year = document.selectFirst(".yil, .year, .date")?.text()?.toIntOrNull()

        val episodes = mutableListOf<Episode>()

        document.select(".sezon-listesi ul li a, .bolumler a, .episodes a").forEach { element ->
            val epHref = element.attr("href")
            val epTitle = element.text()

            val seasonNum = Regex("""(\d+)\.\s*Sezon""").find(epTitle)?.groupValues?.get(1)?.toIntOrNull()
            val episodeNum = Regex("""(\d+)\.\s*Bölüm""").find(epTitle)?.groupValues?.get(1)?.toIntOrNull()

            episodes.add(
                newEpisode(epHref) {
                    this.name = epTitle
                    this.season = seasonNum
                    this.episode = episodeNum
                }
            )
        }

        return newTvSeriesLoadResponse(title, url, TvType.TvSeries, episodes) {
            this.posterUrl = posterUrl
            this.plot = plot
            this.year = year
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val document = app.get(data).document

        document.select("iframe").forEach { iframe ->
            var iframeUrl = iframe.attr("src")

            if (iframeUrl.startsWith("//")) {
                iframeUrl = "https:$iframeUrl"
            }

            if (iframeUrl.isNotBlank()) {
                loadExtractor(iframeUrl, data, subtitleCallback, callback)
            }
        }

        return true
    }
}