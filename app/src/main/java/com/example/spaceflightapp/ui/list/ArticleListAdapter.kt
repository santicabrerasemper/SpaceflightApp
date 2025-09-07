package com.example.spaceflightapp.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spaceflightapp.core.utils.helpers.DateFmt
import com.example.spaceflightapp.core.utils.extensions.loadArticleThumb
import com.example.spaceflightapp.databinding.ItemArticleBinding
import com.example.spaceflightapp.domain.model.Article

class ArticleListAdapter(
    private val onClick: (Article) -> Unit
) : ListAdapter<Article, ArticleListAdapter.ViewHolder>(Diff) {

    init { setHasStableIds(true) }

    override fun getItemId(position: Int) = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemArticleBinding.inflate(inflater, parent, false)
        return ViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class ViewHolder(
        private val binding: ItemArticleBinding,
        private val onClick: (Article) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Article) = with(binding) {
            imgThumb.loadArticleThumb(item.imageUrl)
            tvTitle.text   = item.title
            tvSite.text    = item.newsSite
            tvDate.text    = DateFmt.formatIsoForCard(item.publishedAt)
            tvSummary.text = item.summary

            chipFeatured.isVisible = item.featured

            root.setOnClickListener { onClick(item) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(old: Article, new: Article) = old.id == new.id
        override fun areContentsTheSame(old: Article, new: Article) = old == new
    }
}
