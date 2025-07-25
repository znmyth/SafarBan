package ir.khu.safarban;

import android.app.DatePickerDialog;
import android.graphics.Color;
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

import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

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

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        spinnerTransportType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, transportOptions));
        spinnerTripType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, tripTypes));

        btnSaveTrip.setOnClickListener(v -> saveTrip());

        btnAddCompanion.setOnClickListener(v -> showAddCompanionDialog());
    }

    private void showDatePicker(EditText targetEditText) {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd = new DatePickerDialog(this, (DatePicker view, int y, int m, int d) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(y, m, d);

            PersianDate pdate = new PersianDate(selectedDate.getTimeInMillis());
            PersianDateFormat pdformater = new PersianDateFormat("yyyy/MM/dd");

            String persianDateStr = pdformater.format(pdate);
            targetEditText.setText(persianDateStr);
        }, year, month, day);

        dpd.show();
    }

    private void showAddCompanionDialog() {
        EditText input = new EditText(this);
        input.setHint("نام همسفر");

        new AlertDialog.Builder(this)
                .setTitle("افزودن همسفر")
                .setView(input)
                .setPositiveButton("افزودن", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty() && !companions.contains(name)) {
                        companions.add(name);
                        Toast.makeText(this, "همسفر اضافه شد ✅", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "نام معتبر یا تکراری وارد شده", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("انصراف", null)
                .show();
    }

    private void saveTrip() {
        String uid = auth.getUid();
        if (uid == null) {
            showToast("ابتدا وارد حساب کاربری شو!");
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
            showToast("مقصد و تاریخ رفت ضروری‌ان!");
            return;
        }

        if (!endDate.isEmpty()) {
            if (!isEndDateAfterOrEqualStartDate(startDate, endDate)) {
                showToast("تاریخ پایان نمی‌تونه قبل از شروع باشه!");
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
        trip.put("companions", companions);
        trip.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(uid).collection("trips")
                .add(trip)
                .addOnSuccessListener(doc -> {
                    showToast("سفر ذخیره شد! 🎉");
                    finish();
                })
                .addOnFailureListener(e -> {
                    showToast("خطا در ذخیره‌سازی 😢");
                });
    }

    private boolean isEndDateAfterOrEqualStartDate(String start, String end) {
        try {
            String[] s = start.split("/");
            String[] e = end.split("/");

            int sy = Integer.parseInt(s[0]);
            int sm = Integer.parseInt(s[1]);
            int sd = Integer.parseInt(s[2]);

            int ey = Integer.parseInt(e[0]);
            int em = Integer.parseInt(e[1]);
            int ed = Integer.parseInt(e[2]);

            if (ey < sy) return false;
            if (ey == sy && em < sm) return false;
            if (ey == sy && em == sm && ed < sd) return false;

            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
