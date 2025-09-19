package com.seekho.animeapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.seekho.animeapp.R
import com.seekho.animeapp.databinding.ActivityMainBinding
import com.seekho.animeapp.ui.detail.DetailActivity
import com.seekho.animeapp.util.NetworkMonitor
import com.seekho.animeapp.util.Settings
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private val vm by lazy { HomeViewModel.provide(this, applicationContext) }
    private val net by lazy { NetworkMonitor(applicationContext) }
    private lateinit var adapter: AnimeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupToolbar()

        adapter = AnimeAdapter { item ->
            startActivity(
                Intent(this, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_MAL_ID, item.malId)
                    putExtra(DetailActivity.EXTRA_TITLE, item.title)
                }
            )
        }

        val lm = LinearLayoutManager(this)
        b.recycler.layoutManager = lm
        b.recycler.adapter = adapter

        b.swipeRefresh.setColorSchemeResources(R.color.purple_500)
        b.swipeRefresh.setOnRefreshListener { vm.load(page = 1) }

        b.retryButton.setOnClickListener { vm.load(page = 1) }

        b.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val last = lm.findLastVisibleItemPosition()
                val total = adapter.itemCount
                val threshold = 5
                val st = vm.state.value
                if (last >= total - threshold && !st.isLoading && st.canLoadMore) {
                    vm.loadNextPage()
                }
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    b.progress.visibility =
                        if (st.isLoading && st.items.isEmpty()) View.VISIBLE else View.GONE

                    b.swipeRefresh.isEnabled = !(st.isLoading && st.items.isEmpty())
                    b.swipeRefresh.isRefreshing = st.isLoading && st.items.isNotEmpty()

                    if (st.error != null && st.items.isEmpty()) {
                        b.errorText.visibility = View.VISIBLE
                        b.retryButton.visibility = View.VISIBLE
                        b.errorText.text =
                            if (st.error.contains("offline", true)) {
                                "You're offline. Connect to the internet and tap Retry."
                            } else {
                                "Something went wrong. Please try again."
                            }
                    } else {
                        b.errorText.visibility = View.GONE
                        b.retryButton.visibility = View.GONE
                    }

                    adapter.submitList(st.items)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                net.online.collect { isOnline ->
                    val st = vm.state.value
                    val needsInitial = st.items.isEmpty() && !st.isLoading
                    if (isOnline && needsInitial) vm.load(page = 1)
                }
            }
        }
    }

    private fun setupToolbar() {
        var toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        if (toolbar == null) {
            val include = findViewById<View>(R.id.include_toolbar)
            toolbar = include?.findViewById(R.id.toolbar)
        }
        toolbar ?: return

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = getString(R.string.first_screen)
            setDisplayHomeAsUpEnabled(false)
        }

        toolbar.setTitleTextColor(android.graphics.Color.WHITE)
        toolbar.navigationIcon?.setTint(android.graphics.Color.WHITE)
        toolbar.overflowIcon?.setTint(android.graphics.Color.WHITE)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        menu.findItem(R.id.action_toggle_images)?.isChecked = Settings.hideImages.value
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_toggle_images -> {
                val newValue = !item.isChecked
                Settings.setHideImages(newValue)
                item.isChecked = newValue
                adapter.notifyDataSetChanged()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}