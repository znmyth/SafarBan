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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        searchEditText = findViewById(R.id.searchEditText);
        fabAddTrip = findViewById(R.id.fabAddTrip);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        recyclerView = findViewById(R.id.recyclerViewTrips);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tripList = new ArrayList<>();
        tripAdapter = new TripAdapter(this, tripList);
        recyclerView.setAdapter(tripAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(tripAdapter, this, recyclerView));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        firestore = FirebaseFirestore.getInstance();

        fabAddTrip.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, NewTripActivity.class);
            startActivity(intent);
        });

        bottomNavigation.setSelectedItemId(R.id.nav_home);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            if (id == R.id.nav_social) {
                startActivity(new Intent(this, SocialActivity.class));
                return true;
            }
            return false;
        });

        // اضافه کردن TextWatcher برای جستجو
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tripAdapter.filterTrips(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        loadTripsFromFirestore();
    }

    private void loadTripsFromFirestore() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "کاربر وارد نشده است", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("users").document(userId).collection("trips")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Trip> tripsFromFirestore = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Trip trip = doc.toObject(Trip.class);
                        trip.setId(doc.getId());
                        tripsFromFirestore.add(trip);
                    }
                    // به روز رسانی آداپتور با لیست جدید
                    tripAdapter.updateTrips(tripsFromFirestore);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "خطا در بارگیری سفرها", Toast.LENGTH_SHORT).show();
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
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
