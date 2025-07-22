package ir.khu.safarban;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private TextView tvTripTitle;
    private TextView tvTripRemaining;
    private TextView tvUncheckedItems;
    private EditText searchEditText;
    private ImageButton fabAddTrip;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // اطمینان از نام فایل xml

        // ویوها
        tvTripTitle = findViewById(R.id.tvTripTitle);
        tvTripRemaining = findViewById(R.id.tvTripRemaining);
        tvUncheckedItems = findViewById(R.id.tvUncheckedItems);
        searchEditText = findViewById(R.id.searchEditText);
        fabAddTrip = findViewById(R.id.fabAddTrip);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // کلیک روی دکمه افزودن سفر
        fabAddTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewTripActivity.class);
                startActivity(intent);
            }
        });

        // مقداردهی نمونه
        loadTripData();

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // همینجا هستی
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_social) {
                startActivity(new Intent(MainActivity.this, SocialActivity.class));
                return true;
            }

            return false;
        });
    }

    private void loadTripData() {
        // این داده‌ها رو بعداً می‌تونی از دیتابیس یا API بگیری

        String tripName = "سفر اربعین";
        int daysRemaining = 3;
        int uncheckedItems = 3;

        tvTripTitle.setText(tripName);
        tvTripRemaining.setText(daysRemaining + " روز مانده تا سفر");
        tvUncheckedItems.setText(uncheckedItems + " آیتم چک‌نشده");
    }
}
