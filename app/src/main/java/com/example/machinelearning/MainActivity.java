package com.example.machinelearning;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    BottomNavigationView navigation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadFragment(new TextRecognitionFragment());
        navigation = findViewById(R.id.bottom_navigation);
        navigation.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.AimageViewColor));
        getSupportActionBar().setTitle("Text Recognition");
        navigation.setOnNavigationItemSelectedListener(this);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;

        switch (menuItem.getItemId()) {
            case R.id.item_text_scanner:
                fragment = new TextRecognitionFragment();
                getSupportActionBar().setTitle("Text Recognition");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.AimageViewColor)));
                navigation.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.AimageViewColor));
                break;

            case R.id.item_image_classifier:
                fragment = new ImageClassifierFragment();
                getSupportActionBar().setTitle("Image Classifier");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.BimageViewColor)));
                navigation.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.BimageViewColor));
                break;

            case R.id.item_barcode_reader:
                fragment = new BarCodeScannerFragment();
                getSupportActionBar().setTitle("BarCode Scanner");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.CimageViewColor)));
                navigation.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.CimageViewColor));
                break;
        }

        return loadFragment(fragment);
    }

}
