package org.fnives.test.showcase.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import org.fnives.test.showcase.R
import org.fnives.test.showcase.databinding.ActivityMainBinding
import org.fnives.test.showcase.model.content.ContentId
import org.fnives.test.showcase.ui.auth.AuthActivity
import org.fnives.test.showcase.ui.shared.VerticalSpaceItemDecoration
import org.fnives.test.showcase.ui.shared.getThemePrimaryColor
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.toolbar.menu?.findItem(R.id.logout_cta)?.setOnMenuItemClickListener {
            viewModel.onLogout()
            true
        }
        binding.swipeRefreshLayout.setColorSchemeColors(binding.swipeRefreshLayout.getThemePrimaryColor())
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.onRefresh()
        }

        val adapter = FavouriteContentAdapter(viewModel.mapToAdapterListener())
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.addItemDecoration(VerticalSpaceItemDecoration(resources.getDimensionPixelOffset(R.dimen.padding)))
        binding.recycler.adapter = adapter

        viewModel.content.observe(this) {
            adapter.submitList(it.orEmpty())
        }
        viewModel.errorMessage.observe(this) {
            binding.errorMessage.isVisible = it == true
        }
        viewModel.navigateToAuth.observe(this) {
            it.consume() ?: return@observe
            startActivity(AuthActivity.getStartIntent(this))
            finishAffinity()
        }
        viewModel.loading.observe(this) {
            if (binding.swipeRefreshLayout.isRefreshing != it) {
                binding.swipeRefreshLayout.isRefreshing = it == true
            }
        }

        setContentView(binding.root)
    }

    companion object {
        fun getStartIntent(context: Context): Intent = Intent(context, MainActivity::class.java)

        private fun MainViewModel.mapToAdapterListener(): FavouriteContentAdapter.OnFavouriteItemClicked =
            object : FavouriteContentAdapter.OnFavouriteItemClicked {
                override fun onFavouriteToggleClicked(contentId: ContentId) {
                    this@mapToAdapterListener.onFavouriteToggleClicked(contentId)
                }
            }
    }
}
