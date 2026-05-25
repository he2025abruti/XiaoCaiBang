package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class IngredientDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ingredient.db"
        private const val DATABASE_VERSION = 4
        private const val TABLE_INGREDIENTS = "ingredients"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_QUANTITY = "quantity"
        private const val COLUMN_UNIT = "unit"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_EXPIRE_DATE = "expireDate"

        private const val TABLE_RECIPES = "recipes"
        private const val COLUMN_RECIPE_ID = "id"
        private const val COLUMN_RECIPE_NAME = "name"
        private const val COLUMN_RECIPE_CATEGORY = "category"
        private const val COLUMN_RECIPE_STEPS = "steps"
        private const val COLUMN_RECIPE_INGREDIENTS = "ingredients"
        private const val COLUMN_RECIPE_IMAGE_URL = "imageUrl"
        private const val COLUMN_RECIPE_DESCRIPTION = "description"

        private const val TABLE_FAVORITES = "favorites"
        private const val COLUMN_FAV_RECIPE_NAME = "recipe_name"
        private const val COLUMN_FAV_CATEGORY = "category"
        private const val COLUMN_FAV_STEPS = "steps"
        private const val COLUMN_FAV_INGREDIENTS = "ingredients"
        private const val COLUMN_FAV_IMAGE_URL = "imageUrl"
        private const val COLUMN_FAV_DESCRIPTION = "description"
        private const val COLUMN_FAV_COOK_TIME = "cookTime"
        private const val COLUMN_FAV_GONGYI = "gongyi"
        private const val COLUMN_FAV_KOUWEI = "kouwei"
        private const val COLUMN_FAV_SIDE_INGREDIENTS = "sideIngredients"
        private const val COLUMN_FAV_SEASONINGS = "seasonings"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createIngredients = "CREATE TABLE $TABLE_INGREDIENTS " +
                "($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_NAME TEXT, " +
                "$COLUMN_QUANTITY TEXT, " +
                "$COLUMN_UNIT TEXT, " +
                "$COLUMN_DATE TEXT, " +
                "$COLUMN_EXPIRE_DATE TEXT)"
        db.execSQL(createIngredients)

        val createRecipes = "CREATE TABLE $TABLE_RECIPES " +
                "($COLUMN_RECIPE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COLUMN_RECIPE_NAME TEXT, " +
                "$COLUMN_RECIPE_CATEGORY TEXT, " +
                "$COLUMN_RECIPE_STEPS TEXT, " +
                "$COLUMN_RECIPE_INGREDIENTS TEXT, " +
                "$COLUMN_RECIPE_IMAGE_URL TEXT, " +
                "$COLUMN_RECIPE_DESCRIPTION TEXT)"
        db.execSQL(createRecipes)

        val createFavorites = "CREATE TABLE $TABLE_FAVORITES " +
                "($COLUMN_FAV_RECIPE_NAME TEXT PRIMARY KEY, " +
                "$COLUMN_FAV_CATEGORY TEXT, " +
                "$COLUMN_FAV_GONGYI TEXT, " +
                "$COLUMN_FAV_KOUWEI TEXT, " +
                "$COLUMN_FAV_STEPS TEXT, " +
                "$COLUMN_FAV_INGREDIENTS TEXT, " +
                "$COLUMN_FAV_SIDE_INGREDIENTS TEXT, " +
                "$COLUMN_FAV_SEASONINGS TEXT, " +
                "$COLUMN_FAV_IMAGE_URL TEXT, " +
                "$COLUMN_FAV_DESCRIPTION TEXT, " +
                "$COLUMN_FAV_COOK_TIME INTEGER)"
        db.execSQL(createFavorites)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_INGREDIENTS ADD COLUMN $COLUMN_EXPIRE_DATE TEXT DEFAULT ''")
        }
        if (oldVersion < 3) {
            val createRecipes = "CREATE TABLE $TABLE_RECIPES " +
                    "($COLUMN_RECIPE_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_RECIPE_NAME TEXT, " +
                    "$COLUMN_RECIPE_CATEGORY TEXT, " +
                    "$COLUMN_RECIPE_STEPS TEXT, " +
                    "$COLUMN_RECIPE_INGREDIENTS TEXT, " +
                    "$COLUMN_RECIPE_IMAGE_URL TEXT, " +
                    "$COLUMN_RECIPE_DESCRIPTION TEXT)"
            db.execSQL(createRecipes)
        }
        if (oldVersion < 4) {
            val createFavorites = "CREATE TABLE $TABLE_FAVORITES " +
                    "($COLUMN_FAV_RECIPE_NAME TEXT PRIMARY KEY, " +
                    "$COLUMN_FAV_CATEGORY TEXT, " +
                    "$COLUMN_FAV_GONGYI TEXT, " +
                    "$COLUMN_FAV_KOUWEI TEXT, " +
                    "$COLUMN_FAV_STEPS TEXT, " +
                    "$COLUMN_FAV_INGREDIENTS TEXT, " +
                    "$COLUMN_FAV_SIDE_INGREDIENTS TEXT, " +
                    "$COLUMN_FAV_SEASONINGS TEXT, " +
                    "$COLUMN_FAV_IMAGE_URL TEXT, " +
                    "$COLUMN_FAV_DESCRIPTION TEXT, " +
                    "$COLUMN_FAV_COOK_TIME INTEGER)"
            db.execSQL(createFavorites)
        }
    }

    fun addIngredient(name: String, quantity: String, unit: String, date: String, expireDate: String = "") {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_QUANTITY, quantity)
        values.put(COLUMN_UNIT, unit)
        values.put(COLUMN_DATE, date)
        values.put(COLUMN_EXPIRE_DATE, expireDate)
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
                val expireDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRE_DATE))
                ingredients.add(Ingredient(id, name, quantity, unit, date, expireDate))
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
                val expireDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPIRE_DATE))
                ingredients.add(Ingredient(id, name, quantity, unit, date, expireDate))
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

    fun updateIngredient(id: Int, name: String, quantity: String, unit: String, expireDate: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_NAME, name)
        values.put(COLUMN_QUANTITY, quantity)
        values.put(COLUMN_UNIT, unit)
        values.put(COLUMN_EXPIRE_DATE, expireDate)
        db.update(TABLE_INGREDIENTS, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    // ── 菜谱相关方法 ──

    fun addRecipe(name: String, category: String, steps: String, ingredients: String, imageUrl: String, description: String) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_RECIPE_NAME, name)
        values.put(COLUMN_RECIPE_CATEGORY, category)
        values.put(COLUMN_RECIPE_STEPS, steps)
        values.put(COLUMN_RECIPE_INGREDIENTS, ingredients)
        values.put(COLUMN_RECIPE_IMAGE_URL, imageUrl)
        values.put(COLUMN_RECIPE_DESCRIPTION, description)
        db.insert(TABLE_RECIPES, null, values)
    }

    fun getAllCustomRecipes(): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_RECIPES", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_NAME))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_CATEGORY))
                val steps = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_STEPS))
                val ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_INGREDIENTS))
                val imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_IMAGE_URL)) ?: ""
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RECIPE_DESCRIPTION)) ?: ""
                recipes.add(Recipe(
                    id = id,
                    name = name,
                    category = category,
                    gongyi = "",
                    kouwei = "",
                    steps = steps,
                    mainIngredients = ingredients,
                    sideIngredients = "",
                    seasonings = "",
                    cookTime = 30,
                    isCustom = true,
                    imageUrl = imageUrl,
                    description = description
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return recipes
    }

    fun deleteRecipe(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_RECIPES, "$COLUMN_RECIPE_ID = ?", arrayOf(id.toString()))
    }

    // ── 收藏相关方法 ──

    fun addFavorite(recipe: Recipe) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_FAV_RECIPE_NAME, recipe.name)
        values.put(COLUMN_FAV_CATEGORY, recipe.category)
        values.put(COLUMN_FAV_GONGYI, recipe.gongyi)
        values.put(COLUMN_FAV_KOUWEI, recipe.kouwei)
        values.put(COLUMN_FAV_STEPS, recipe.steps)
        values.put(COLUMN_FAV_INGREDIENTS, recipe.mainIngredients)
        values.put(COLUMN_FAV_SIDE_INGREDIENTS, recipe.sideIngredients)
        values.put(COLUMN_FAV_SEASONINGS, recipe.seasonings)
        values.put(COLUMN_FAV_IMAGE_URL, recipe.imageUrl)
        values.put(COLUMN_FAV_DESCRIPTION, recipe.description)
        values.put(COLUMN_FAV_COOK_TIME, recipe.cookTime)
        db.insertWithOnConflict(TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun removeFavorite(recipeName: String) {
        val db = this.writableDatabase
        db.delete(TABLE_FAVORITES, "$COLUMN_FAV_RECIPE_NAME = ?", arrayOf(recipeName))
    }

    fun isFavorite(recipeName: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT 1 FROM $TABLE_FAVORITES WHERE $COLUMN_FAV_RECIPE_NAME = ?", arrayOf(recipeName))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }

    fun getAllFavorites(): List<Recipe> {
        val recipes = mutableListOf<Recipe>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FAVORITES", null)
        if (cursor.moveToFirst()) {
            do {
                recipes.add(Recipe(
                    id = 0,
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_RECIPE_NAME)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_CATEGORY)) ?: "",
                    gongyi = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_GONGYI)) ?: "",
                    kouwei = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_KOUWEI)) ?: "",
                    steps = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_STEPS)) ?: "",
                    mainIngredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_INGREDIENTS)) ?: "",
                    sideIngredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_SIDE_INGREDIENTS)) ?: "",
                    seasonings = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_SEASONINGS)) ?: "",
                    cookTime = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FAV_COOK_TIME)),
                    imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_IMAGE_URL)) ?: "",
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAV_DESCRIPTION)) ?: ""
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return recipes
    }

    fun getFavoriteNames(): Set<String> {
        val names = mutableSetOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_FAV_RECIPE_NAME FROM $TABLE_FAVORITES", null)
        if (cursor.moveToFirst()) {
            do {
                names.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return names
    }
}
