package com.example.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

data class RecipeWithStatus(
    val recipe: Recipe,
    val matchedCount: Int,
    val totalCount: Int
) {
    val isAllMatched: Boolean get() = totalCount > 0 && matchedCount == totalCount
    val missingCount: Int get() = totalCount - matchedCount
}

class RecipeAdapter(
    private val context: Context,
    private var items: List<RecipeWithStatus>
) : BaseAdapter() {

    fun updateData(newItems: List<RecipeWithStatus>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): RecipeWithStatus = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_recipe, parent, false)

        val item = items[position]
        val recipe = item.recipe

        val nameView = view.findViewById<TextView>(R.id.recipe_name)
        val statusView = view.findViewById<TextView>(R.id.recipe_status)
        val cookTimeView = view.findViewById<TextView>(R.id.recipe_cook_time)
        val descView = view.findViewById<TextView>(R.id.recipe_desc)
        val imageView = view.findViewById<ImageView>(R.id.recipe_image)
        val addBtn = view.findViewById<TextView>(R.id.recipe_add_btn)

        nameView.text = recipe.name

        if (item.totalCount == 0) {
            statusView.text = "暂无食材数据"
            statusView.setBackgroundColor(0xFFAAAAAA.toInt())
        } else if (item.isAllMatched) {
            statusView.text = "食材齐全"
            statusView.setBackgroundColor(0xFF4CAF50.toInt())
        } else {
            statusView.text = "缺${item.missingCount}样"
            statusView.setBackgroundColor(0xFFFF9800.toInt())
        }

        cookTimeView.text = "${recipe.cookTime}分钟"

        val desc = if (recipe.description.isNotEmpty()) {
            recipe.description
        } else {
            "${recipe.gongyi} · ${recipe.kouwei}"
        }
        descView.text = desc

        if (recipe.imageUrl.isNotEmpty()) {
            ImageLoader().loadImage(recipe.imageUrl, imageView, circular = false)
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        addBtn.setOnClickListener {
            onAddClickListener?.invoke(recipe)
        }

        return view
    }

    private var onAddClickListener: ((Recipe) -> Unit)? = null

    fun setOnAddClickListener(listener: (Recipe) -> Unit) {
        onAddClickListener = listener
    }
}
