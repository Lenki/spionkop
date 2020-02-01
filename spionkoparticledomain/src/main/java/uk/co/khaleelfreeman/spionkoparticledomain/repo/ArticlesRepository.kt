package uk.co.khaleelfreeman.spionkoparticledomain.repo

import io.reactivex.disposables.Disposable
import uk.co.khaleelfreeman.spionkoparticledomain.SpionkopArticle
import uk.co.khaleelfreeman.spionkoparticledomain.service.NetworkService
import uk.co.khaleelfreeman.spionkoparticledomain.util.mediaSources

class ArticleRepository(private val articleNetworkService: NetworkService) :
    Repository {
    private var articles: Array<SpionkopArticle> = emptyArray()
    private var filteredArticles = emptyArray<SpionkopArticle>()
    private lateinit var _sources: Set<String>
    private val articleFilters: MutableMap<String, Array<SpionkopArticle>> = mutableMapOf()
    private lateinit var d: Disposable
    var published: Long = 0L
        private set

    override fun getArticles(): Array<SpionkopArticle> {
        return if (filteredArticles.isEmpty()) {
            articles.clone()
        } else {
            filteredArticles.clone()
        }
    }

    override fun fetchArticles(callback: () -> Unit) {
        d = articleNetworkService.execute().subscribe { response ->
            val (published, articles) = response
            this.published = published
            this.articles = articles.toTypedArray()
            _sources = mediaSources(articles)
            //create separate lists from each source
            generateFilteredArticles(_sources, articles.toTypedArray())
            callback()
        }
    }

    private fun generateFilteredArticles(
        sources: Set<String>,
        articles: Array<SpionkopArticle>
    ) {
        sources.forEach { source: String ->
            val filteredArticle: Array<SpionkopArticle> =
                articles.filter { article -> article.url.contains(source) }.toTypedArray()
            articleFilters[source] = filteredArticle
        }
    }

    override fun addFilter(source: String) {
        filteredArticles += articleFilters[source]!!
    }

    override fun removeFilter(source: String) {
        filteredArticles =
            filteredArticles.filterNot { article -> articleFilters[source]!!.any { it == article } }
                .toTypedArray()
    }

    override fun getSources(): Set<String> = _sources

    override fun teardown() {
        d.dispose()
    }
}