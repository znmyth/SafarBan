package ir.khu.safarban;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import java.util.Calendar;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.api.PersianPickerDate;
import ir.hamsaa.persiandatepicker.api.PersianPickerListener;
import ir.hamsaa.persiandatepicker.util.PersianCalendar;

public class NewTripActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST = 1001;

    private AutoCompleteTextView etDestination;
    private EditText etStartDate, etEndDate;
    private CheckBox cbAlarmDayBefore, cbAlarmDayOf;
    private AutoCompleteTextView spinnerTransportType, spinnerTripType;
    private Button btnSaveTrip, btnAddCompanion, btnBackToMain;
    private RecyclerView rvCompanions;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private final List<String> companions = new ArrayList<>();
    private CompanionAdapter companionAdapter;
    private EditText currentDateTarget;

    private final String[] transportOptions = {"هواپیما", "قطار", "اتوبوس", "ماشین شخصی", "کشتی", "تاکسی", "اسب و شتر", "پای پیاده"};
    private final String[] tripTypes = {"تفریحی", "زیارتی", "کاری", "تحصیلی", "خانوادگی", "ولگردی"};

    private String tripId = null;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
            }
        }

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etDestination = findViewById(R.id.etDestination);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        cbAlarmDayBefore = findViewById(R.id.cbAlarmDayBefore);
        cbAlarmDayOf = findViewById(R.id.cbAlarmDayOf);
        spinnerTransportType = findViewById(R.id.spinnerTransportType);
        spinnerTripType = findViewById(R.id.spinnerTripType);
        btnSaveTrip = findViewById(R.id.btnSaveTrip);
        btnAddCompanion = findViewById(R.id.btnAddCompanion);
        btnBackToMain = findViewById(R.id.btnBack); // دکمه برگشت
        rvCompanions = findViewById(R.id.rvCompanions);

        spinnerTransportType.setAdapter(new ArrayAdapter<>(this, R.layout.simple_dropdown_item_1, transportOptions));
        spinnerTripType.setAdapter(new ArrayAdapter<>(this, R.layout.simple_dropdown_item_1, tripTypes));

        spinnerTransportType.setOnClickListener(v -> spinnerTransportType.showDropDown());
        spinnerTripType.setOnClickListener(v -> spinnerTripType.showDropDown());

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        companionAdapter = new CompanionAdapter(companions);
        rvCompanions.setLayoutManager(new LinearLayoutManager(this));
        rvCompanions.setAdapter(companionAdapter);

        btnAddCompanion.setOnClickListener(v -> showAddCompanionDialog());

        // دکمه برگشت به MainActivity
        btnBackToMain.setOnClickListener(v -> {
            Intent intent = new Intent(NewTripActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        Intent intent = getIntent();
        tripId = intent.getStringExtra("trip_id");
        if (tripId != null && !tripId.isEmpty()) {
            isEditMode = true;
            loadTripData(tripId);
        }

        btnSaveTrip.setOnClickListener(v -> checkPermissionAndSaveTrip());
    }

    private void loadTripData(String tripId) {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            Toast.makeText(this, "کاربر شناسایی نشد!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DocumentReference tripRef = db.collection("users").document(userId)
                .collection("trips").document(tripId);

        tripRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        etDestination.setText(snapshot.getString("destination"));
                        etStartDate.setText(snapshot.getString("startDate"));
                        etEndDate.setText(snapshot.getString("endDate"));
                        spinnerTransportType.setText(snapshot.getString("transport"), false);
                        spinnerTripType.setText(snapshot.getString("tripType"), false);
                        cbAlarmDayBefore.setChecked(Boolean.TRUE.equals(snapshot.getBoolean("alarmDayBefore")));
                        cbAlarmDayOf.setChecked(Boolean.TRUE.equals(snapshot.getBoolean("alarmDayOf")));

                        companions.clear();
                        List<String> savedCompanions = (List<String>) snapshot.get("companions");
                        if (savedCompanions != null) {
                            companions.addAll(savedCompanions);
                        }
                        companionAdapter.notifyDataSetChanged();

                    } else {
                        Toast.makeText(this, "اطلاعات سفر یافت نشد", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "خطا در دریافت اطلاعات سفر", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void showDatePicker(EditText target) {
        currentDateTarget = target;

        PersianDatePickerDialog picker = new PersianDatePickerDialog(this)
                .setPositiveButtonString("باشه")
                .setNegativeButton("بیخیال")
                .setTodayButton("امروز")
                .setTodayButtonVisible(true)
                .setMinYear(1300)
                .setMaxYear(PersianDatePickerDialog.THIS_YEAR)
                .setActionTextColor(Color.GRAY)
                .setListener(new PersianPickerListener() {
                    @Override
                    public void onDateSelected(PersianPickerDate persianPickerDate) {
                        String dateStr = String.format(Locale.getDefault(), "%04d/%02d/%02d",
                                persianPickerDate.getPersianYear(),
                                persianPickerDate.getPersianMonth(),
                                persianPickerDate.getPersianDay());
                        currentDateTarget.setText(dateStr);
                    }

                    @Override
                    public void onDismissed() {
                    }
                });

        picker.show();
    }

    private void showAddCompanionDialog() {
        EditText input = new EditText(this);
        input.setHint("نام همسفر");

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("افزودن همسفر")
                .setView(input)
                .setPositiveButton("➕ اضافه کن", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        companions.add(name);
                        companionAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "نام همسفر را وارد کنید", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("بیخیال", null)
                .show();
    }

    private void checkPermissionAndSaveTrip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST);
            } else {
                saveTrip();
            }
        } else {
            saveTrip();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveTrip();
            } else {
                Toast.makeText(this, "برای ارسال یادآوری باید اجازه نوتیفیکیشن را بدهید", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveTrip() {
        String destination = etDestination.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String transport = spinnerTransportType.getText().toString().trim();
        String tripType = spinnerTripType.getText().toString().trim();
        boolean alarmDayBefore = cbAlarmDayBefore.isChecked();
        boolean alarmDayOf = cbAlarmDayOf.isChecked();

        if (destination.isEmpty() || startDate.isEmpty()) {
            Toast.makeText(this, "مقصد و تاریخ شروع اجباری است", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!endDate.isEmpty() && !isEndDateValid(startDate, endDate)) {
            Toast.makeText(this, "تاریخ برگشت نمی‌تواند قبل از تاریخ رفت باشد", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> trip = new HashMap<>();
        trip.put("destination", destination);
        trip.put("startDate", startDate);
        trip.put("endDate", endDate);
        trip.put("transport", transport);
        trip.put("tripType", tripType);
        trip.put("alarmDayBefore", alarmDayBefore);
        trip.put("alarmDayOf", alarmDayOf);
        trip.put("companions", new ArrayList<>(companions));

        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "unknown";

        if (isEditMode && tripId != null) {
            db.collection("users").document(userId).collection("trips")
                    .document(tripId)
                    .set(trip)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "سفر به‌روزرسانی شد ✅", Toast.LENGTH_SHORT).show();
                        scheduleAlarms(startDate, destination, alarmDayBefore, alarmDayOf);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "خطا در به‌روزرسانی سفر", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("users").document(userId).collection("trips")
                    .add(trip)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "سفر ذخیره شد ✅", Toast.LENGTH_SHORT).show();
                        scheduleAlarms(startDate, destination, alarmDayBefore, alarmDayOf);
                        Intent intent = new Intent(NewTripActivity.this, TripDetailsActivity.class);
                        intent.putExtra("trip_id", documentReference.getId());
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "خطا در ذخیره", Toast.LENGTH_SHORT).show());
        }
    }

    private boolean isEndDateValid(String startDate, String endDate) {
        try {
            String[] startParts = startDate.split("/");
            String[] endParts = endDate.split("/");

            PersianCalendar startCal = new PersianCalendar();
            startCal.setPersianDate(
                    Integer.parseInt(startParts[0]),
                    Integer.parseInt(startParts[1]),
                    Integer.parseInt(startParts[2])
            );

            PersianCalendar endCal = new PersianCalendar();
            endCal.setPersianDate(
                    Integer.parseInt(endParts[0]),
                    Integer.parseInt(endParts[1]),
                    Integer.parseInt(endParts[2])
            );

            return !endCal.before(startCal);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void scheduleAlarms(String startDateStr, String destination, boolean dayBefore, boolean dayOf) {
        try {
            String[] parts = startDateStr.split("/");
            int persianYear = Integer.parseInt(parts[0]);
            int persianMonth = Integer.parseInt(parts[1]);
            int persianDay = Integer.parseInt(parts[2]);

            PersianCalendar persianCalendar = new PersianCalendar();
            persianCalendar.setPersianDate(persianYear, persianMonth, persianDay);

            Calendar gregorianCalendar = Calendar.getInstance();
            gregorianCalendar.set(
                    persianCalendar.get(Calendar.YEAR),
                    persianCalendar.get(Calendar.MONTH),
                    persianCalendar.get(Calendar.DAY_OF_MONTH)
            );

            // نوتیف روز قبل سفر (ساعت ۱۰ شب)
            if (dayBefore) {
                Calendar before = (Calendar) gregorianCalendar.clone();
                before.add(Calendar.DAY_OF_MONTH, -1);
                before.set(Calendar.HOUR_OF_DAY, 22);
                before.set(Calendar.MINUTE, 0);
                before.set(Calendar.SECOND, 0);
                before.set(Calendar.MILLISECOND, 0);

                setAlarm(before, "فردا به " + destination + " سفر داری! 🎒");
            }

            // نوتیف روز سفر (ساعت ۸ صبح)
            if (dayOf) {
                Calendar dayOfCal = (Calendar) gregorianCalendar.clone();
                dayOfCal.set(Calendar.HOUR_OF_DAY, 8);
                dayOfCal.set(Calendar.MINUTE, 0);
                dayOfCal.set(Calendar.SECOND, 0);
                dayOfCal.set(Calendar.MILLISECOND, 0);

                setAlarm(dayOfCal, "امروز راهی " + destination + " هستی! 🚀");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setAlarm(Calendar calendar, String message) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "اجازه‌ی تنظیم آلارم دقیق را ندارید", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            Intent intent = new Intent(this, TripReminderReceiver.class);
            intent.putExtra("message", message);

            int requestCode = (int) (calendar.getTimeInMillis() / 1000);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "خطا در تنظیم آلارم: اجازه‌ی کافی وجود ندارد", Toast.LENGTH_SHORT).show();
        }
    }

}
