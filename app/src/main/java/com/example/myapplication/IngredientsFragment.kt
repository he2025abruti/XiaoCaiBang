package com.example.myapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class IngredientsFragment : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var addIngredientButton: FloatingActionButton
    private lateinit var ingredientBrief: TextView
    private lateinit var ingredientDetailList: ListView
    private lateinit var emptyText: TextView
    private lateinit var dbHelper: IngredientDatabaseHelper
    private var selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ingredients, container, false)

        calendarView = view.findViewById(R.id.calendar_view)
        addIngredientButton = view.findViewById(R.id.add_ingredient_button)
        ingredientBrief = view.findViewById(R.id.ingredient_brief)
        ingredientDetailList = view.findViewById(R.id.ingredient_detail_list)
        emptyText = view.findViewById(R.id.empty_text)
        dbHelper = IngredientDatabaseHelper(requireContext())

        addIngredientButton.setOnClickListener {
            showAddIngredientDialog()
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
            refreshIngredients()
        }

        ingredientDetailList.setOnItemClickListener { _, _, position, _ ->
            val ingredients = dbHelper.getIngredientsByDate(selectedDate)
            if (position in ingredients.indices) {
                showEditIngredientDialog(ingredients[position])
            }
        }

        refreshIngredients()

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshIngredients()
    }

    private fun refreshIngredients() {
        val ingredients = dbHelper.getIngredientsByDate(selectedDate)
        displayIngredients(ingredients)
    }

    private fun displayIngredients(ingredients: List<Ingredient>) {
        if (ingredients.isEmpty()) {
            ingredientBrief.visibility = View.GONE
            ingredientDetailList.visibility = View.GONE
            emptyText.visibility = View.VISIBLE
        } else {
            ingredientBrief.visibility = View.VISIBLE
            ingredientDetailList.visibility = View.VISIBLE
            emptyText.visibility = View.GONE

            // Build brief summary: "名称 数量; 名称 数量; ..."
            val briefParts = ingredients.map { "${it.name} ${it.quantity}${it.unit}" }
            ingredientBrief.text = briefParts.joinToString("; ")

            // Gray out brief if all expired, normal otherwise
            val allExpired = ingredients.all { isExpired(it.expireDate) }
            ingredientBrief.setTextColor(
                if (allExpired) 0xFF999999.toInt()
                else resources.getColor(android.R.color.primary_text_light, null)
            )

            ingredientDetailList.adapter = IngredientDetailAdapter(ingredients)
        }
    }

    private fun isExpired(expireDate: String?): Boolean {
        if (expireDate.isNullOrEmpty()) return false
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val expire = sdf.parse(expireDate)
            val today = sdf.parse(sdf.format(Date()))
            expire != null && today != null && today.after(expire)
        } catch (e: Exception) {
            false
        }
    }

    private inner class IngredientDetailAdapter(private val ingredients: List<Ingredient>) : BaseAdapter() {

        override fun getCount(): Int = ingredients.size
        override fun getItem(position: Int): Any = ingredients[position]
        override fun getItemId(position: Int): Long = ingredients[position].id.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.ingredient_detail_item, parent, false)

            val ingredient = ingredients[position]
            val nameView = view.findViewById<TextView>(R.id.item_name)
            val quantityView = view.findViewById<TextView>(R.id.item_quantity)
            val expireView = view.findViewById<TextView>(R.id.item_expire)
            val statusView = view.findViewById<TextView>(R.id.item_status)

            nameView.text = ingredient.name
            quantityView.text = "采购: ${ingredient.quantity} ${ingredient.unit}"

            val expired = isExpired(ingredient.expireDate)

            if (ingredient.expireDate.isNotEmpty()) {
                expireView.text = "保质期至: ${ingredient.expireDate}"
                expireView.visibility = View.VISIBLE
            } else {
                expireView.visibility = View.GONE
            }

            if (expired) {
                statusView.text = "已过期"
                statusView.setTextColor(0xFFFF4444.toInt())
                nameView.setTextColor(0xFF999999.toInt())
                quantityView.setTextColor(0xFF999999.toInt())
                expireView.setTextColor(0xFF999999.toInt())
            } else {
                statusView.text = "正常"
                statusView.setTextColor(0xFF4CAF50.toInt())
                nameView.setTextColor(0xDD000000.toInt())
                quantityView.setTextColor(0x8A000000.toInt())
                expireView.setTextColor(0x8A000000.toInt())
            }

            return view
        }
    }

    private fun showAddIngredientDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_ingredient, null)
        val ingredientName = dialogView.findViewById<EditText>(R.id.ingredient_name)
        val ingredientQuantity = dialogView.findViewById<EditText>(R.id.ingredient_quantity)
        val unitSpinner = dialogView.findViewById<Spinner>(R.id.unit_spinner)
        val shelfLifeDays = dialogView.findViewById<EditText>(R.id.shelf_life_days)

        val units = arrayOf("个", "g", "斤")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle("添加食材到 $selectedDate")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val name = ingredientName.text.toString()
                val quantity = ingredientQuantity.text.toString()
                val unit = unitSpinner.selectedItem.toString()
                val daysText = shelfLifeDays.text.toString()
                val days = if (daysText.isNotEmpty()) daysText.toIntOrNull() ?: 7 else 7

                if (name.isNotEmpty()) {
                    val expireDate = calculateExpireDate(selectedDate, days)
                    dbHelper.addIngredient(name, quantity, unit, selectedDate, expireDate)
                    refreshIngredients()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun showEditIngredientDialog(ingredient: Ingredient) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_ingredient, null)
        val editName = dialogView.findViewById<EditText>(R.id.edit_name)
        val editQuantity = dialogView.findViewById<EditText>(R.id.edit_quantity)
        val editUnitSpinner = dialogView.findViewById<Spinner>(R.id.edit_unit_spinner)
        val editExpireDate = dialogView.findViewById<EditText>(R.id.edit_expire_date)

        editName.setText(ingredient.name)
        editQuantity.setText(ingredient.quantity)
        editExpireDate.setText(ingredient.expireDate)

        val units = arrayOf("个", "g", "斤")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        editUnitSpinner.adapter = adapter
        val unitIndex = units.indexOf(ingredient.unit)
        if (unitIndex >= 0) editUnitSpinner.setSelection(unitIndex)

        // 保质期日期选择
        editExpireDate.setOnClickListener {
            val cal = Calendar.getInstance()
            if (ingredient.expireDate.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val d = sdf.parse(ingredient.expireDate)
                    if (d != null) cal.time = d
                } catch (e: Exception) { }
            }
            DatePickerDialog(requireContext(), { _, year, month, day ->
                editExpireDate.setText(String.format("%d-%02d-%02d", year, month + 1, day))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("编辑食材")
            .setView(dialogView)
            .setPositiveButton("保存") { _, _ ->
                val name = editName.text.toString()
                val quantity = editQuantity.text.toString()
                val unit = editUnitSpinner.selectedItem.toString()
                val expireDate = editExpireDate.text.toString()

                if (name.isNotEmpty()) {
                    dbHelper.updateIngredient(ingredient.id, name, quantity, unit, expireDate)
                    refreshIngredients()
                }
            }
            .setNegativeButton("取消", null)
            .setNeutralButton("删除") { _, _ ->
                AlertDialog.Builder(requireContext())
                    .setTitle("确认删除")
                    .setMessage("确定删除「${ingredient.name}」吗？")
                    .setPositiveButton("删除") { _, _ ->
                        dbHelper.deleteIngredient(ingredient.id)
                        refreshIngredients()
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
            .show()
    }

    private fun calculateExpireDate(date: String, shelfLifeDays: Int): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(date) ?: Date()
            calendar.add(Calendar.DAY_OF_MONTH, shelfLifeDays)
            sdf.format(calendar.time)
        } catch (e: Exception) {
            ""
        }
    }
}
