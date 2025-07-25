package ir.khu.safarban;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import saman.zamani.persiandate.PersianDate;

import java.util.ArrayList;
import java.util.List;

public class TripDetailsActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_trip_details);

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

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String destination = intent.getStringExtra("destination");
        String type = intent.getStringExtra("type");
        String transport = intent.getStringExtra("transport");
        String fromDateStr = intent.getStringExtra("from_date");
        String toDateStr = intent.getStringExtra("to_date");

        if (title != null) tvTripTitle.setText(title);
        if (destination != null) tvTripDestination.setText("مقصد: " + destination);
        if (type != null) tvTripType.setText("نوع سفر: " + type);
        if (transport != null) tvTransportType.setText("وسیله: " + transport);

        tripStartDate = parsePersianDate(fromDateStr);
        tripEndDate = parsePersianDate(toDateStr);

        if (tripStartDate != null && tripEndDate != null) {
            tvTripDates.setText("تاریخ: " + fromDateStr + " تا " + toDateStr);
            updateDaysRemaining();
        }

        checklistItems = new ArrayList<>();
        loadChecklist();

        adapter = new ChecklistAdapter(checklistItems);
        recyclerChecklist.setAdapter(adapter);

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

        String savedNotes = prefs.getString(prefNotesKey, "");
        etNotes.setText(savedNotes);

        btnSave.setOnClickListener(v -> {
            saveNotes();
            saveChecklist();
            Toast.makeText(this, "اطلاعات ذخیره شد", Toast.LENGTH_SHORT).show();
        });
    }

    private PersianDate parsePersianDate(String dateStr) {
        try {
            if (dateStr == null) return null;
            String[] parts = dateStr.split("/");
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int d = Integer.parseInt(parts[2]);
            PersianDate pd = new PersianDate();
            pd.setShYear(y);
            pd.setShMonth(m);
            pd.setShDay(d);
            return pd;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void updateDaysRemaining() {
        PersianDate today = new PersianDate();
        long diff = tripStartDate.getTime() - today.getTime();
        int days = (int) (diff / (1000 * 60 * 60 * 24));
        if (days < 0) days = 0;
        tvDaysRemaining.setText(days + " روز مانده تا سفر");
    }

    private void saveNotes() {
        prefs.edit().putString(prefNotesKey, etNotes.getText().toString()).apply();
    }

    private void saveChecklist() {
        StringBuilder sb = new StringBuilder();
        for (ChecklistItem item : checklistItems) {
            sb.append(item.text.replace("|", "")).append("||").append(item.checked).append("##");
        }
        prefs.edit().putString(prefChecklistKey, sb.toString()).apply();
    }

    private void loadChecklist() {
        String saved = prefs.getString(prefChecklistKey, "");
        checklistItems.clear();
        if (!TextUtils.isEmpty(saved)) {
            String[] items = saved.split("##");
            for (String s : items) {
                if (TextUtils.isEmpty(s)) continue;
                String[] parts = s.split("\\|\\|");
                if (parts.length == 2) {
                    checklistItems.add(new ChecklistItem(parts[0], Boolean.parseBoolean(parts[1])));
                }
            }
        } else {
            checklistItems.add(new ChecklistItem("پاوربانک", false));
        }
    }

    static class ChecklistItem {
        String text;
        boolean checked;

        ChecklistItem(String text, boolean checked) {
            this.text = text;
            this.checked = checked;
        }
    }

    class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {
        List<ChecklistItem> items;

        ChecklistAdapter(List<ChecklistItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            CheckBox cb = new CheckBox(parent.getContext());
            return new ViewHolder(cb);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChecklistItem item = items.get(position);
            holder.checkBox.setText(item.text);
            holder.checkBox.setChecked(item.checked);
            holder.checkBox.setTextColor(item.checked ? Color.GRAY : Color.BLACK);

            holder.checkBox.setOnClickListener(v -> {
                item.checked = holder.checkBox.isChecked();
                items.remove(position);
                if (item.checked)
                    items.add(item); // ته لیست
                else
                    items.add(0, item); // سر لیست
                notifyDataSetChanged();
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
                checkBox = (CheckBox) itemView;
            }
        }
    }
}
