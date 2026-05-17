package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class IngredientDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ingredient.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_INGREDIENTS = "ingredients"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_UNIT = "unit"
        private const val COLUMN_DATE = "date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE $TABLE_INGREDIENTS " +
                "($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_QUANTITY TEXT, " +
                "$COLUMN_UNIT TEXT, " +
                "$COLUMN_DATE TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INGREDIENTS")
        onCreate(db)
    }

    fun addIngredient(name: String, quantity: String, unit: String, date: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_QUANTITY, quantity)
        values.put(COLUMN_UNIT, unit)
        values.put(COLUMN_DATE, date)
        db.insert(TABLE_INGREDIENTS, null, values)
        db.close()
    }

    fun getAllIngredients(): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_INGREDIENTS", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val quantity = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
                val unit = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT))
                val date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE))
                ingredients.add(Ingredient(id, name, quantity, unit, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return ingredients
    }

    fun getIngredientsByDate(date: String): List<Ingredient> {
        val ingredients = mutableListOf<Ingredient>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_INGREDIENTS WHERE $COLUMN_DATE = ?", arrayOf(date))

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val quantity = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY))
                val unit = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIT))
                ingredients.add(Ingredient(id, name, quantity, unit, date))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return ingredients
    }

    fun deleteIngredient(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_INGREDIENTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }
}
