package com.example.spaceflightapp.ui.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spaceflightapp.R
import com.example.spaceflightapp.core.utils.helpers.UiError
import com.example.spaceflightapp.core.utils.extensions.iconRes
import com.example.spaceflightapp.core.utils.extensions.message
import com.example.spaceflightapp.core.utils.extensions.titleRes
import com.example.spaceflightapp.databinding.FragmentArticleListBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArticleListFragment : Fragment(R.layout.fragment_article_list) {

    private val articleListViewModel: ArticleListViewModel by viewModel()
    private var _binding: FragmentArticleListBinding? = null
    private val binding get() = _binding!!
    private var originalSearchHint: CharSequence? = null

    private lateinit var adapter: ArticleListAdapter
    private lateinit var linearLayOutManager: LinearLayoutManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentArticleListBinding.bind(view)
        setupRecycler()
        setupSearch()
        setupSwipeToRefresh()
        setupRetry()
        setupKeyboardDismiss()
        setupEmptyStateActions()
        setupBackPressHandling()
        collectState()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupRecycler() {
        linearLayOutManager = LinearLayoutManager(requireContext())
        adapter = ArticleListAdapter(onClick = { article ->
            findNavController().navigate(
                R.id.action_list_to_detail,
                bundleOf("articleId" to article.id)
            )
        })
        binding.recycler.layoutManager = linearLayOutManager
        binding.recycler.adapter = adapter

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy <= 0) return
                val visible = linearLayOutManager.childCount
                val total = linearLayOutManager.itemCount
                val first = linearLayOutManager.findFirstVisibleItemPosition()
                if (first + visible >= total - 6) articleListViewModel.loadNextPage()
            }
        })
    }

    private fun clearSearch() {
        val et = binding.inputSearch
        et.setText("")
        et.clearFocus()
        et.hint = originalSearchHint ?: getString(R.string.search_text)
        binding.recycler.scrollToPosition(0)
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(et.windowToken, 0)
    }

    private fun setupEmptyStateActions() {
        binding.btnClearSearch.setOnClickListener { clearSearch() }
        binding.emptyContainer.setOnClickListener { clearSearch() }
    }

    private fun setupSearch() {
        val et = binding.inputSearch
        val til = binding.tilSearch
        if (originalSearchHint == null) originalSearchHint = et.hint

        et.doAfterTextChanged {
            articleListViewModel.onQueryChanged(it?.toString().orEmpty())
        }

        et.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                et.hint = ""
            } else {
                v.hideKeyboard()
                if (et.text.isNullOrEmpty()) et.hint = originalSearchHint
            }
        }

        et.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                clearSearchFocusAndHideKeyboard()
                true
            } else false
        }

        til.setEndIconOnClickListener { clearSearch() }
    }

    private fun setupSwipeToRefresh() {
        binding.swipe.setColorSchemeResources(R.color.primary_color)
        binding.swipe.setOnRefreshListener { articleListViewModel.refresh() }
    }

    private fun setupRetry() {
        binding.btnRetry.setOnClickListener { articleListViewModel.refresh() }
    }

    private fun renderError(e: UiError) {
        binding.errorView.isVisible = true
        binding.ivError.setImageResource(e.iconRes())
        binding.errorTitle.setText(e.titleRes())
        val msg = e.message(requireContext())
        binding.errorMsg.isVisible = msg.isNotBlank()
        binding.errorMsg.text = msg
    }

    private fun clearError() {
        binding.errorView.isVisible = false
    }

    private fun collectState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                articleListViewModel.state.collect { st ->
                    binding.progress.isVisible = st.isLoading && st.items.isEmpty()
                    binding.swipe.isRefreshing = st.isRefreshing
                    binding.recycler.isVisible = st.items.isNotEmpty()

                    adapter.submitList(st.items)

                    val showError = st.error != null && st.items.isEmpty()
                    if (showError) renderError(st.error!!) else clearError()

                    val showEmpty = st.hasLoadedOnce &&
                            st.query.isNotBlank() &&
                            st.items.isEmpty() &&
                            st.error == null &&
                            !st.isLoading &&
                            !st.isRefreshing
                    binding.emptyContainer.isVisible = showEmpty
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupKeyboardDismiss() {
        binding.swipe.setOnTouchListener { v, ev ->
            when (ev.action) {
                android.view.MotionEvent.ACTION_DOWN -> clearSearchFocusAndHideKeyboard()
                android.view.MotionEvent.ACTION_UP -> v.performClick()
            }
            false
        }
        binding.recycler.setOnTouchListener { _, ev ->
            if (ev.action == android.view.MotionEvent.ACTION_DOWN) {
                clearSearchFocusAndHideKeyboard()
            }
            false
        }
        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    clearSearchFocusAndHideKeyboard()
                }
            }
        })
        binding.errorView.setOnClickListener { clearSearchFocusAndHideKeyboard() }
    }

    private fun clearSearchFocusAndHideKeyboard() {
        val et = binding.inputSearch
        et.clearFocus()
        val imm = requireContext().getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(et.windowToken, 0)
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun isKeyboardVisible(): Boolean =
        ViewCompat.getRootWindowInsets(binding.root)
            ?.isVisible(WindowInsetsCompat.Type.ime()) == true

    private fun setupBackPressHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val hasQuery = !binding.inputSearch.text.isNullOrBlank()
                    val imeVisible = isKeyboardVisible()
                    val showEmpty = binding.emptyContainer.isVisible

                    when {
                        hasQuery && imeVisible -> {
                            clearSearchFocusAndHideKeyboard()
                        }
                        hasQuery -> {
                            clearSearch()
                        }
                        showEmpty -> {
                            clearSearch()
                        }
                        else -> {
                            isEnabled = false
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                            isEnabled = true
                        }
                    }
                }
            }
        )
    }
}
