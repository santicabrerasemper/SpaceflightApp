package com.example.spaceflightapp.ui.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.spaceflightapp.R
import com.example.spaceflightapp.core.utils.helpers.DateFmt
import com.example.spaceflightapp.core.utils.helpers.UiError
import com.example.spaceflightapp.core.utils.extensions.iconRes
import com.example.spaceflightapp.core.utils.extensions.message
import com.example.spaceflightapp.core.utils.extensions.titleRes
import com.example.spaceflightapp.databinding.FragmentArticleDetailBinding
import com.example.spaceflightapp.domain.model.Article
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ArticleDetailFragment : Fragment(R.layout.fragment_article_detail) {

    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

    private val articleId: Long by lazy { requireArguments().getLong("articleId") }
    private val vm: ArticleDetailViewModel by viewModel { parametersOf(articleId) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentArticleDetailBinding.bind(view)
        setupToolbar()
        setupSwipeRefresh()
        setupButtons()
        collectState()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupSwipeRefresh() {
        binding.swipe.setColorSchemeResources(R.color.primary_color)
        binding.swipe.setOnRefreshListener { vm.refresh() }
    }

    private fun setupButtons() {
        binding.btnRetry.setOnClickListener {
            setLoading(true)
            binding.errorView.isVisible = false
            vm.retry()
        }
        binding.btnOpen.setOnClickListener {
            (vm.state.value as? ArticleDetailUiState.Content)?.article?.url?.let(::openInSite)
        }
        binding.btnShare.setOnClickListener { shareFromState() }
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.state.collect { st ->
                    when (st) {
                        is ArticleDetailUiState.Loading -> renderLoading()
                        is ArticleDetailUiState.Error   -> renderError(st.error)
                        is ArticleDetailUiState.Content -> renderContent(st)
                    }
                }
            }
        }
    }

    private fun setLoading(show: Boolean) {
        binding.progress.isVisible = show
        binding.loadingScreen.isVisible = show
        binding.errorView.isVisible = false
        binding.swipe.isVisible = !show
        binding.swipe.isRefreshing = false
    }

    private fun renderLoading() = setLoading(true)

    private fun renderError(err: UiError) {
        binding.progress.isVisible = false
        binding.loadingScreen.isVisible = false
        binding.swipe.isVisible = false
        binding.errorView.isVisible = true

        binding.ivError.setImageResource(err.iconRes())
        binding.errorTitle.setText(err.titleRes())

        val msg = err.message(requireContext())
        binding.errorMsg.isVisible = msg.isNotBlank()
        binding.errorMsg.text = msg
    }

    private fun renderContent(c: ArticleDetailUiState.Content) {
        val a = c.article
        binding.errorView.isVisible = false
        setLoading(false)
        binding.swipe.isRefreshing = c.isRefreshing

        binding.imgHero.load(a.imageUrl) {
            crossfade(true)
            placeholder(R.drawable.ic_placeholder)
            error(R.drawable.ic_placeholder)
        }
        binding.tvTitle.text = a.title
        binding.tvSite.text  = a.newsSite
        binding.tvDate.text  = DateFmt.formatIsoForCard(a.publishedAt)
        binding.chipFeatured.isVisible = a.featured
        binding.tvSummary.text = a.summary

        binding.chipsAuthors.removeAllViews()
        a.authors.forEach { author ->
            binding.chipsAuthors.addView(Chip(requireContext()).apply {
                text = author.name
                isClickable = false
            })
        }
    }

    private fun currentArticleOrNull(): Article? =
        (vm.state.value as? ArticleDetailUiState.Content)?.article

    private fun openInSite(url: String) {
        val safe = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safe)).addCategory(Intent.CATEGORY_BROWSABLE)
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Snackbar.make(requireView(), getString(R.string.no_browser_found), Snackbar.LENGTH_LONG).show()
        }
    }

    private fun shareFromState() {
        currentArticleOrNull()?.let { a ->
            val text = if (a.url.isBlank()) a.title else "${a.title}\n${a.url}"
            val share = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, a.title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(share, getString(R.string.btn_share_article_detail)))
        }
    }
}
