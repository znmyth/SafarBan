import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class TripCheckActivity extends AppCompatActivity {

    TextView tvTripTitle, tvTripDates, tvTripDestination, tvTripType, tvTransportType, tvDaysRemaining;
    EditText etNewItem, etNotes;
    Button btnAddItem, btnSave;
    RecyclerView recyclerChecklist;

    ChecklistAdapter adapter;
    List<ChecklistItem> checklistItems;

    SharedPreferences prefs;

    String prefNotesKey = "trip_notes";
    String prefChecklistKey = "trip_checklist";

    PersianDate tripStartDate, tripEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_check); // اسم فایل xml تو همین جا بذار

        // پیدا کردن ویوها
        tvTripTitle = findViewById(R.id.tvTripTitle);
        tvTripDates = findViewById(R.id.tvTripDates);
        tvTripDestination = findViewById(R.id.tvTripDestination);
        tvTripType = findViewById(R.id.tvTripType);
        tvTransportType = findViewById(R.id.tvTransportType);
        tvDaysRemaining = findViewById(R.id.tvDaysRemaining);

        etNewItem = findViewById(R.id.etNewItem);
        etNotes = findViewById(R.id.etNotes);

        btnAddItem = findViewById(R.id.btnAddItem);
        btnSave = findViewById(R.id.btnSave);

        recyclerChecklist = findViewById(R.id.recyclerChecklist);
        recyclerChecklist.setLayoutManager(new LinearLayoutManager(this));

        prefs = getSharedPreferences("trip_data", Context.MODE_PRIVATE);

        // گرفتن داده‌ها از Intent
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String destination = intent.getStringExtra("destination");
        String type = intent.getStringExtra("type");
        String transport = intent.getStringExtra("transport");
        String fromDateStr = intent.getStringExtra("from_date");  // مثل "1403/05/25"
        String toDateStr = intent.getStringExtra("to_date");

        // ست کردن اطلاعات
        if (title != null) tvTripTitle.setText(title);
        if (destination != null) tvTripDestination.setText("مقصد: " + destination);
        if (type != null) tvTripType.setText("نوع سفر: " + type);
        if (transport != null) tvTransportType.setText("وسیله: " + transport);

        // تبدیل رشته تاریخ به PersianDate
        tripStartDate = parsePersianDate(fromDateStr);
        tripEndDate = parsePersianDate(toDateStr);

        if (tripStartDate != null && tripEndDate != null) {
            PersianDateFormat pdFormat = new PersianDateFormat("yyyy/MM/dd");
            tvTripDates.setText("تاریخ: " + pdFormat.format(tripStartDate) + " تا " + pdFormat.format(tripEndDate));

            // محاسبه تعداد روز باقی مانده
            updateDaysRemaining();
        }

        // آماده‌سازی لیست چک‌لیست
        checklistItems = new ArrayList<>();

        // نمونه اولیه (اگر می‌خوای می‌تونی از SharedPreferences هم بارگذاری کنی)
        loadChecklist();

        adapter = new ChecklistAdapter(checklistItems);
        recyclerChecklist.setAdapter(adapter);

        // افزودن آیتم جدید
        btnAddItem.setOnClickListener(v -> {
            String newItemText = etNewItem.getText().toString().trim();
            if (!TextUtils.isEmpty(newItemText)) {
                checklistItems.add(0, new ChecklistItem(newItemText, false));
                adapter.notifyItemInserted(0);
                recyclerChecklist.scrollToPosition(0);
                etNewItem.setText("");
            } else {
                Toast.makeText(this, "لطفا یک آیتم وارد کنید", Toast.LENGTH_SHORT).show();
            }
        });

        // بارگذاری یادداشت از SharedPreferences
        String savedNotes = prefs.getString(prefNotesKey, "");
        etNotes.setText(savedNotes);

        // دکمه ثبت — ذخیره یادداشت و چک‌لیست
        btnSave.setOnClickListener(v -> {
            saveNotes();
            saveChecklist();
            Toast.makeText(this, "اطلاعات ثبت شد", Toast.LENGTH_SHORT).show();
        });
    }

    private PersianDate parsePersianDate(String persianDateStr) {
        try {
            if (persianDateStr == null) return null;
            String[] parts = persianDateStr.split("/");
            if (parts.length != 3) return null;
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int d = Integer.parseInt(parts[2]);
            return new PersianDate(y, m, d);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateDaysRemaining() {
        if (tripStartDate == null) return;

        PersianDate today = new PersianDate();
        long diffMillis = tripStartDate.getTime() - today.getTime();
        int daysLeft = (int) (diffMillis / (1000 * 60 * 60 * 24));

        if (daysLeft < 0) daysLeft = 0;

        tvDaysRemaining.setText(daysLeft + " روز مانده تا سفر");
    }

    private void saveNotes() {
        String notes = etNotes.getText().toString();
        prefs.edit().putString(prefNotesKey, notes).apply();
    }

    private void saveChecklist() {
        // ساده‌ترین روش ذخیره در SharedPreferences به صورت رشته با فرمت json ساده (یا جداکننده)
        // چون فقط متن و وضعیت داریم، می‌تونیم با جداکننده ذخیره کنیم
        StringBuilder sb = new StringBuilder();
        for (ChecklistItem item : checklistItems) {
            // فرمت: متن||true/false
            sb.append(item.text.replace("|", " ")).append("||").append(item.checked).append("##");
        }
        prefs.edit().putString(prefChecklistKey, sb.toString()).apply();
    }

    private void loadChecklist() {
        String saved = prefs.getString(prefChecklistKey, "");
        if (!TextUtils.isEmpty(saved)) {
            checklistItems.clear();
            String[] items = saved.split("##");
            for (String s : items) {
                if (TextUtils.isEmpty(s)) continue;
                String[] parts = s.split("\\|\\|");
                if (parts.length == 2) {
                    checklistItems.add(new ChecklistItem(parts[0], Boolean.parseBoolean(parts[1])));
                }
            }
        } else {
            // اگر چیزی ذخیره نشده بود، یک آیتم نمونه میذاریم (مثل همون چک‌باکس sampleItem در XML)
            checklistItems.add(new ChecklistItem("پاوربانک", false));
        }
    }

    // مدل آیتم چک‌لیست
    static class ChecklistItem {
        String text;
        boolean checked;

        ChecklistItem(String text, boolean checked) {
            this.text = text;
            this.checked = checked;
        }
    }

    // آداپتر RecyclerView
    class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {

        List<ChecklistItem> items;

        ChecklistAdapter(List<ChecklistItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_multiple_choice, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChecklistItem item = items.get(position);
            holder.checkBox.setText(item.text);
            holder.checkBox.setChecked(item.checked);

            // تغییر رنگ اگر تیک خورده باشد (خاکستری)
            holder.checkBox.setTextColor(item.checked ? Color.GRAY : Color.BLACK);

            holder.checkBox.setOnClickListener(v -> {
                boolean isChecked = holder.checkBox.isChecked();
                item.checked = isChecked;

                // اگر تیک خورد، به انتهای لیست می‌فرستیم و خاکستری می‌کنیم
                if (isChecked) {
                    items.remove(position);
                    items.add(item);
                    notifyDataSetChanged();
                } else {
                    // اگر از تیک خارج شد به اول لیست میاریم
                    items.remove(position);
                    items.add(0, item);
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                checkBox = (CheckBox) itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
