package com.seekho.animeapp.ui.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.MaterialToolbar
import com.seekho.animeapp.R
import com.seekho.animeapp.databinding.ActivityDetailBinding
import com.seekho.animeapp.util.NetworkMonitor
import com.seekho.animeapp.util.Settings
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MAL_ID = "extra_mal_id"
        const val EXTRA_TITLE = "extra_title"
        private const val TAG = "DetailActivity"
    }

    private lateinit var b: ActivityDetailBinding
    private val vm by lazy { DetailViewModel.provide(this, applicationContext) }
    private val net by lazy { NetworkMonitor(applicationContext) }
    private lateinit var castAdapter: CastAdapter

    private var posterTarget: CustomTarget<Drawable>? = null
    private var refreshedAfterOnlineOnce = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbar()

        // Cast list
        castAdapter = CastAdapter()
        b.castRecycler.apply {
            adapter = castAdapter
            layoutManager =
                LinearLayoutManager(this@DetailActivity, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
        }

        // Pull-to-refresh
        b.swipeRefresh.setOnRefreshListener {
            val id = intent.getIntExtra(EXTRA_MAL_ID, -1)
            if (id != -1) vm.load(id)
        }

        val id = intent.getIntExtra(EXTRA_MAL_ID, -1)
        if (id == -1) {
            b.pageProgress.visibility = View.GONE
            b.errorText.visibility = View.VISIBLE
            b.errorText.text = "Invalid anime id"
            b.pageContent.visibility = View.GONE
            b.swipeRefresh.isRefreshing = false
            return
        }

        setupWebView()

        // Collector 1: VM state → UI
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    supportActionBar?.title = st.detail?.title ?: supportActionBar?.title

                    val firstLoad = st.isLoadingDetails && st.detail == null
                    b.swipeRefresh.isEnabled = !firstLoad
                    b.swipeRefresh.isRefreshing = st.isLoadingDetails && st.detail != null

                    val mediaLoading = b.mediaProgress.visibility == View.VISIBLE
                    b.pageProgress.visibility =
                        if (firstLoad && !mediaLoading) View.VISIBLE else View.GONE

                    if (st.error != null && st.detail == null) {
                        b.errorText.visibility = View.VISIBLE
                        b.errorText.text =
                            "Failed to load details. Please check your connection."
                        b.pageContent.visibility = View.GONE
                        b.swipeRefresh.isRefreshing = false
                        return@collect
                    } else {
                        b.errorText.visibility = View.GONE
                        b.pageContent.visibility = View.VISIBLE
                    }

                    val d = st.detail
                    if (d != null) {
                        b.title.text = d.title ?: "—"
                        b.meta.text =
                            "Episodes: ${d.episodes ?: "—"}   •   Rating: ${d.score ?: "—"}"
                        val genres =
                            d.genres.orEmpty().mapNotNull { it.name }.joinToString(", ")
                        b.genres.text = if (genres.isEmpty()) "Genres: —" else "Genres: $genres"
                        b.synopsis.text = d.synopsis ?: "—"

                        val youtubeId = d.trailer?.youtubeId
                        if (!youtubeId.isNullOrBlank()) {
                            tryInlineTrailer(youtubeId, d)
                        } else {
                            showPoster(d)
                        }
                    }

                    b.castProgress.visibility =
                        if (st.isLoadingCast) View.VISIBLE else View.GONE
                    castAdapter.submit(st.characters)
                    val showCastHeader = st.characters.isNotEmpty() || st.isLoadingCast
                    b.castLabel.visibility =
                        if (showCastHeader) View.VISIBLE else View.GONE
                    b.castRecycler.visibility =
                        if (st.characters.isNotEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        // Collector 2: network status changes
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                net.online.collect { isOnline ->
                    val st = vm.state.value
                    if (isOnline && st.detail != null && !st.isLoadingDetails && !refreshedAfterOnlineOnce) {
                        refreshedAfterOnlineOnce = true
                        vm.load(st.detail.malId)
                    }
                    if (!isOnline) refreshedAfterOnlineOnce = false
                }
            }
        }

        vm.load(id)
    }

    private fun setupToolbar() {
        var toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        if (toolbar == null) {
            val include = findViewById<View>(R.id.include_toolbar)
            toolbar = include?.findViewById(R.id.toolbar)
        }
        toolbar ?: return

        setSupportActionBar(toolbar)

        val animeTitle = intent.getStringExtra(EXTRA_TITLE) ?: "Details"

        supportActionBar?.apply {
            title = animeTitle
            setDisplayHomeAsUpEnabled(true)
        }

        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupWebView() {
        b.trailerWeb.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
        }
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(b.trailerWeb, true)
        }
        b.trailerWeb.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                if (b.trailerWeb.visibility == View.VISIBLE) {
                    b.mediaProgress.visibility =
                        if (newProgress in 0..99) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun tryInlineTrailer(
        youtubeId: String,
        detail: com.seekho.animeapp.data.model.AnimeDetail
    ) {
        b.watchOnYoutube.visibility = View.GONE
        b.pageProgress.visibility = View.GONE
        b.mediaProgress.visibility = View.VISIBLE
        b.poster.visibility = View.GONE
        b.posterPlaceholder.visibility = View.GONE
        b.trailerWeb.visibility = View.GONE

        val html = """
            <html>
            <body style="margin:0;padding:0;">
              <iframe 
                width="100%" height="100%" 
                src="https://www.youtube.com/embed/$youtubeId?autoplay=0&modestbranding=1&rel=0" 
                frameborder="0" allowfullscreen>
              </iframe>
            </body>
            </html>
        """.trimIndent()

        var retriedOnce = false
        b.trailerWeb.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                b.mediaProgress.visibility = View.GONE
                b.trailerWeb.visibility = View.VISIBLE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (!retriedOnce) {
                    retriedOnce = true
                    b.trailerWeb.reload()
                } else {
                    fallbackToPoster(detail)
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                if (!retriedOnce) {
                    retriedOnce = true
                    b.trailerWeb.reload()
                } else {
                    fallbackToPoster(detail)
                }
            }
        }

        b.trailerWeb.postDelayed({
            if (b.trailerWeb.visibility != View.VISIBLE) fallbackToPoster(detail)
        }, 4000)

        b.trailerWeb.loadDataWithBaseURL(
            "https://www.youtube.com",
            html,
            "text/html",
            "utf-8",
            null
        )
    }

    private fun showPoster(detail: com.seekho.animeapp.data.model.AnimeDetail) {
        val hide = Settings.hideImages.value

        b.pageProgress.visibility = View.GONE
        b.mediaProgress.visibility = if (hide) View.GONE else View.VISIBLE
        b.trailerWeb.visibility = View.GONE

        if (hide) {
            val initial =
                (detail.title?.trim()?.firstOrNull()?.uppercaseChar() ?: '•').toString()
            b.posterPlaceholder.visibility = View.VISIBLE
            b.posterPlaceholder.text = initial
            b.poster.visibility = View.INVISIBLE
            return
        } else {
            b.posterPlaceholder.visibility = View.GONE
            b.poster.visibility = View.VISIBLE
        }

        b.poster.alpha = 0f
        val posterUrl = detail.images?.webp?.largeImageUrl
            ?: detail.images?.jpg?.largeImageUrl
            ?: detail.images?.webp?.imageUrl
            ?: detail.images?.jpg?.imageUrl

        posterTarget?.let { Glide.with(b.poster).clear(it) }
        posterTarget = object : CustomTarget<Drawable>() {
            override fun onResourceReady(
                resource: Drawable,
                transition: Transition<in Drawable>?
            ) {
                b.poster.setImageDrawable(resource)
                b.mediaProgress.visibility = View.GONE
                b.poster.animate().cancel()
                b.poster.animate().alpha(1f).setDuration(200).start()
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                b.mediaProgress.visibility = View.GONE
                b.poster.alpha = 1f
            }
        }
        Glide.with(b.poster)
            .load(posterUrl)
            .centerCrop()
            .into(posterTarget!!)

        val externalUrl = detail.trailer?.url
            ?: detail.trailer?.youtubeId?.let { "https://www.youtube.com/watch?v=$it" }
        b.watchOnYoutube.visibility =
            if (!externalUrl.isNullOrBlank()) View.VISIBLE else View.GONE
        if (!externalUrl.isNullOrBlank()) {
            b.watchOnYoutube.setOnClickListener { openExternal(externalUrl) }
        }
    }

    private fun fallbackToPoster(detail: com.seekho.animeapp.data.model.AnimeDetail) {
        b.trailerWeb.stopLoading()
        b.trailerWeb.visibility = View.GONE
        showPoster(detail)
    }

    private fun openExternal(url: String) {
        val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.google.android.youtube")
        }
        try {
            startActivity(ytIntent)
        } catch (_: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    override fun onDestroy() {
        (b.trailerWeb.parent as? android.view.ViewGroup)?.removeView(b.trailerWeb)
        b.trailerWeb.removeAllViews()
        b.trailerWeb.destroy()
        posterTarget?.let { Glide.with(this).clear(it) }
        posterTarget = null
        super.onDestroy()
    }
}