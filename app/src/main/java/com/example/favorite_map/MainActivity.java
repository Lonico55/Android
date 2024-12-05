package com.example.favorite_map;

import static androidx.navigation.ActivityKt.findNavController;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.ui.NavigationUI;
import androidx.navigation.fragment.NavHostFragment;


import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BottomNavigationViewの取得
        bottomNavView = findViewById(R.id.bottom_nav);

        // ナビゲーションフラグメントの取得
        //NavController navController = findNavController(R.id.nav_fragment);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navController = navHostFragment.getNavController();

        // 下部メニューとナビゲーションを関連付け
        NavigationUI.setupWithNavController(bottomNavView, navController);
    }
}
