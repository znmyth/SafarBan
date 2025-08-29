package ir.khu.safarban;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "trip_channel";

    private EditText searchEditText;
    private ImageButton fabAddTrip;
    private BottomNavigationView bottomNavigation;

    private RecyclerView recyclerView;
    private TripAdapter tripAdapter;
    private List<Trip> tripList;
    private FirebaseFirestore firestore;
    private ListenerRegistration tripsListener; // Listener زنده Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        searchEditText = findViewById(R.id.searchEditText);
        fabAddTrip = findViewById(R.id.fabAddTrip);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        recyclerView = findViewById(R.id.recyclerViewTrips);

        // RecyclerView setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tripList = new ArrayList<>();
        tripAdapter = new TripAdapter(this, tripList);
        recyclerView.setAdapter(tripAdapter);

        // Swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(
                new SwipeToDeleteCallback(tripAdapter, this, recyclerView));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        firestore = FirebaseFirestore.getInstance();

        // FAB add trip
        fabAddTrip.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, NewTripActivity.class)));

        // BottomNavigation item select
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                recyclerView.setVisibility(RecyclerView.VISIBLE);
                searchEditText.setVisibility(EditText.VISIBLE);
                fabAddTrip.setVisibility(ImageButton.VISIBLE);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else if (id == R.id.nav_social) {
                startActivity(new Intent(this, SocialActivity.class));
                return true;
            }
            return false;
        });

        bottomNavigation.setSelectedItemId(R.id.nav_home);

        // Search listener
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tripAdapter.filterTrips(s.toString());
            }
        });

        attachTripsListener(); // Listener زنده Firestore
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchEditText.setText("");          // ریست فیلتر جستجو
        tripAdapter.filterTrips("");         // نمایش کل لیست
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // قطع Listener هنگام خروج از اکتیویتی
        if (tripsListener != null) tripsListener.remove();
    }

    // Listener زنده برای بروزرسانی خودکار کارت‌ها
    private void attachTripsListener() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tripsListener = firestore.collection("users").document(userId)
                .collection("trips")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "خطا در دریافت داده‌ها", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        List<Trip> tripsFromFirestore = new ArrayList<>();
                        snapshots.getDocuments().forEach(doc -> {
                            Trip trip = doc.toObject(Trip.class);
                            if (trip != null) {
                                trip.setId(doc.getId());
                                tripsFromFirestore.add(trip);
                            }
                        });
                        tripAdapter.updateTrips(tripsFromFirestore);
                    } else {
                        tripAdapter.updateTrips(new ArrayList<>()); // اگر لیست خالی شد
                    }
                });
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "یادآوری سفر";
            String description = "یادآوری نزدیک بودن تاریخ سفر";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) notificationManager.createNotificationChannel(channel);
        }
    }
}
