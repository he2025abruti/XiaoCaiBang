package com.example.myapplication

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var buyFragment: BuyFragment
    private lateinit var ingredientsFragment: IngredientsFragment
    private lateinit var recipeFragment: RecipeFragment
    private lateinit var myFragment: MyFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        // 夜间模式初始化，必须在 super.onCreate() 之前
        val prefs = getSharedPreferences("user_settings", 0)
        val isNightMode = prefs.getBoolean("night_mode", false)
        AppCompatDelegate.setDefaultNightMode(
            if (isNightMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 初始化Fragment
        buyFragment = BuyFragment()
        ingredientsFragment = IngredientsFragment()
        recipeFragment = RecipeFragment()
        myFragment = MyFragment()

        // 默认显示买菜页面
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, buyFragment)
            .commit()

        // 设置底部导航栏点击事件
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_buy -> {
                    replaceFragment(buyFragment)
                    true
                }
                R.id.nav_ingredients -> {
                    replaceFragment(ingredientsFragment)
                    true
                }
                R.id.nav_recipe -> {
                    replaceFragment(recipeFragment)
                    true
                }
                R.id.nav_my -> {
                    replaceFragment(myFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
