package ir.khu.safarban;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewTripActivity extends AppCompatActivity {

    private AutoCompleteTextView etDestination;
    private EditText etStartDate, etEndDate;
    private CheckBox cbAlarmDayBefore, cbAlarmDayOf;
    private Spinner spinnerTransportType, spinnerTripType;
    private Button btnSaveTrip, btnAddCompanion;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private final List<String> companions = new ArrayList<>();

    private final String[] transportOptions = {"هواپیما", "قطار", "اتوبوس", "ماشین شخصی", "کشتی", "تاکسی"};
    private final String[] tripTypes = {"تفریحی", "زیارتی", "کاری", "تحصیلی", "خانوادگی"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Find Views
        etDestination = findViewById(R.id.etDestination);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        cbAlarmDayBefore = findViewById(R.id.cbAlarmDayBefore);
        cbAlarmDayOf = findViewById(R.id.cbAlarmDayOf);
        spinnerTransportType = findViewById(R.id.spinnerTransportType);
        spinnerTripType = findViewById(R.id.spinnerTripType);
        btnSaveTrip = findViewById(R.id.btnSaveTrip);
        btnAddCompanion = findViewById(R.id.btnAddCompanion);

        // Setup DatePicker
        etStartDate.setOnClickListener(v -> showDateDialog(etStartDate));
        etEndDate.setOnClickListener(v -> showDateDialog(etEndDate));

        // Setup Spinners
        ArrayAdapter<String> transportAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, transportOptions);
        spinnerTransportType.setAdapter(transportAdapter);

        ArrayAdapter<String> tripTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tripTypes);
        spinnerTripType.setAdapter(tripTypeAdapter);

        // Save trip
        btnSaveTrip.setOnClickListener(v -> saveTrip());

        // Add Companion
        btnAddCompanion.setOnClickListener(v -> {
            EditText input = new EditText(this);
            input.setHint("نام همسفر");

            new AlertDialog.Builder(this)
                    .setTitle("افزودن همسفر")
                    .setView(input)
                    .setPositiveButton("افزودن", (dialog, which) -> {
                        String name = input.getText().toString().trim();
                        if (!name.isEmpty()) {
                            companions.add(name);
                            Toast.makeText(this, "همسفر اضافه شد ✅", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("انصراف", null)
                    .show();
        });
    }

    private void showDateDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String date = year + "/" + (month + 1) + "/" + dayOfMonth;
                    editText.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.show();
    }

    private void saveTrip() {
        String uid = auth.getUid();
        if (uid == null) {
            Toast.makeText(this, "ابتدا وارد حساب کاربری شو!", Toast.LENGTH_SHORT).show();
            return;
        }

        String destination = etDestination.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String transport = spinnerTransportType.getSelectedItem().toString();
        String tripType = spinnerTripType.getSelectedItem().toString();
        boolean notifyBefore = cbAlarmDayBefore.isChecked();
        boolean notifyDayOf = cbAlarmDayOf.isChecked();

        if (destination.isEmpty() || startDate.isEmpty()) {
            Toast.makeText(this, "مقصد و تاریخ رفت ضروری‌ان!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Check endDate is not before startDate
        if (!endDate.isEmpty()) {
            try {
                String[] startParts = startDate.split("/");
                String[] endParts = endDate.split("/");

                Calendar startCal = Calendar.getInstance();
                startCal.set(Integer.parseInt(startParts[0]), Integer.parseInt(startParts[1]) - 1, Integer.parseInt(startParts[2]));

                Calendar endCal = Calendar.getInstance();
                endCal.set(Integer.parseInt(endParts[0]), Integer.parseInt(endParts[1]) - 1, Integer.parseInt(endParts[2]));

                if (endCal.before(startCal)) {
                    Toast.makeText(this, "تاریخ پایان نمی‌تونه قبل از شروع باشه!", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (Exception e) {
                Toast.makeText(this, "فرمت تاریخ معتبر نیست!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Map<String, Object> trip = new HashMap<>();
        trip.put("destination", destination);
        trip.put("startDate", startDate);
        trip.put("endDate", endDate);
        trip.put("transport", transport);
        trip.put("tripType", tripType);
        trip.put("notifyBefore", notifyBefore);
        trip.put("notifyDayOf", notifyDayOf);
        trip.put("companions", companions); // ✅ new
        trip.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid).collection("trips")
                .add(trip)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "سفر ذخیره شد! 🎉", Toast.LENGTH_SHORT).show();
                    finish(); // go back
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "خطا در ذخیره‌سازی 😢", Toast.LENGTH_SHORT).show();
                });
    }
}
