package com.example.myapplication

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Date

class IngredientsFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var addIngredientButton: FloatingActionButton
    private lateinit var dbHelper: IngredientDatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ingredients, container, false)

        calendarView = view.findViewById(R.id.calendar_view)
        addIngredientButton = view.findViewById(R.id.add_ingredient_button)
        dbHelper = IngredientDatabaseHelper(requireContext())

        // 设置添加食材按钮的点击事件
        addIngredientButton.setOnClickListener {
            showAddIngredientDialog()
        }

        // 设置日历控件的日期选择事件
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            // 处理日期选择事件
            val date = "$year-${month + 1}-$dayOfMonth"
            val ingredients = dbHelper.getIngredientsByDate(date)
            // 显示该日期的食材
            showIngredientsByDate(ingredients)
        }

        return view
    }

    private fun showAddIngredientDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_ingredient, null)
        val ingredientName = dialogView.findViewById<EditText>(R.id.ingredient_name)
        val ingredientQuantity = dialogView.findViewById<EditText>(R.id.ingredient_quantity)
        val unitSpinner = dialogView.findViewById<Spinner>(R.id.unit_spinner)

        // 设置单位选择器的选项
        val units = arrayOf("个", "g", "斤")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle("添加食材")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val name = ingredientName.text.toString()
                val quantity = ingredientQuantity.text.toString()
                val unit = unitSpinner.selectedItem.toString()
                // 保存食材信息
                saveIngredient(name, quantity, unit)
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun saveIngredient(name: String, quantity: String, unit: String) {
        // 获取当前日期
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = dateFormat.format(Date())
        // 保存到数据库
        dbHelper.addIngredient(name, quantity, unit, date)
        // 显示保存成功的提示
        AlertDialog.Builder(requireContext())
            .setTitle("保存成功")
            .setMessage("食材已添加到今日")
            .setPositiveButton("确定", null)
            .show()
    }

    private fun showIngredientsByDate(ingredients: List<Ingredient>) {
        if (ingredients.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle("无食材")
                .setMessage("该日期没有添加食材")
                .setPositiveButton("确定", null)
                .show()
        } else {
            val message = StringBuilder()
            for (ingredient in ingredients) {
                message.append("${ingredient.name}: ${ingredient.quantity} ${ingredient.unit}\n")
            }
            AlertDialog.Builder(requireContext())
                .setTitle("食材列表")
                .setMessage(message.toString())
                .setPositiveButton("确定", null)
                .show()
        }
    }
}
