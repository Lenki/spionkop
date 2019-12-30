package uk.co.khaleelfreeman.spion

import android.util.Log
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface Repository {
    fun getArticles(): Array<Article>
    fun fetchArticles(callback: () -> Unit)
}

object ArticleRepository : Repository {
    var published: Double = 0.0
        private set(value) {
            field = value
        }

    private var articles: Array<Article> = emptyArray()

    override fun getArticles(): Array<Article> {
        return articles.clone()
    }

    override fun fetchArticles(callback: () -> Unit) {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.khaleelfreeman.co.uk/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create<ArticlesService>(ArticlesService::class.java)

        service.getArticles().enqueue(object : Callback<Response> {
            override fun onFailure(call: Call<Response>, t: Throwable) {
                Log.e("", t.localizedMessage)
                articles =
                    arrayOf(Article("Sorry there was an error fetching the articles", Visual()))
            }

            override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                published = response.body()?.published ?: 0.0
                articles = response.body()?.articles
                    ?: arrayOf(Article("Sorry there was an error fetching the articles", Visual()))
                callback()
            }
        })
    }
}

interface ArticlesService {
    @GET("liverpoolfc/articles")
    fun getArticles(): Call<Response>
}


data class Response(
    @SerializedName("feed_last_published")
    val published: Double,
    val articles: Array<Article>
)

data class Article(
    val title: String = "",
    val visual: Visual = Visual(),
    @SerializedName("published")
    val timeStamp: String = "",
    @SerializedName("originId")
    val url: String = ""
)

data class Visual(val url: String = "")