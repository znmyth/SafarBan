package ir.khu.safarban;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import saman.zamani.persiandate.PersianDate;

public class TripDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_TRIP = 1001;

    TextView tvTripTitle, tvTripDates, tvTripDestination, tvTripType, tvTransportType, tvDaysRemaining;
    EditText etNewItem, etNotes;
    Button btnAddItem, btnSave, btnEdit, btnBack, btnStartTrip;
    RecyclerView recyclerChecklist;

    ChecklistAdapter adapter;
    List<ChecklistItem> checklistItems;

    FirebaseFirestore db;
    String tripId, userId;

    PersianDate tripStartDate, tripEndDate;

    TextView tvCompanion, tvUncheckedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        // init views
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
        btnEdit = findViewById(R.id.btnEditTrip);
        btnBack = findViewById(R.id.btnBack);
        btnStartTrip = findViewById(R.id.btnSave); // دکمه جدید

        tvCompanion = findViewById(R.id.tvCompanion);
        tvUncheckedItems = findViewById(R.id.tvUncheckedItems);

        recyclerChecklist = findViewById(R.id.recyclerChecklist);
        recyclerChecklist.setLayoutManager(new LinearLayoutManager(this));

        checklistItems = new ArrayList<>();
        adapter = new ChecklistAdapter(checklistItems);
        recyclerChecklist.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        tripId = getIntent().getStringExtra("trip_id");
        if (tripId == null || userId == null) {
            Toast.makeText(this, "شناسه سفر یا کاربر یافت نشد!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadTripData();
        loadChecklist();
        loadNotes();

        adapter.setOnCheckChangedListener(this::updateUncheckedItemsCount);

        btnAddItem.setOnClickListener(v -> {
            String newItemText = etNewItem.getText().toString().trim();
            if (!TextUtils.isEmpty(newItemText)) {
                checklistItems.add(0, new ChecklistItem(newItemText, false));
                adapter.notifyItemInserted(0);
                recyclerChecklist.scrollToPosition(0);
                etNewItem.setText("");
                updateUncheckedItemsCount();
            } else {
                Toast.makeText(this, "لطفاً یک آیتم وارد کنید", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> saveAndReturnHome());
        btnStartTrip.setOnClickListener(v -> saveAndReturnHome());

        btnEdit.setOnClickListener(v -> {
            Intent editIntent = new Intent(this, NewTripActivity.class);
            editIntent.putExtra("trip_id", tripId);
            startActivityForResult(editIntent, REQUEST_EDIT_TRIP);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void saveAndReturnHome() {
        saveNotes();
        saveChecklist();
        Toast.makeText(this, "اطلاعات ذخیره شد", Toast.LENGTH_SHORT).show();

        // بازگشت به MainActivity
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(mainIntent);
        finish();
    }

    private void updateUncheckedItemsCount() {
        int count = 0;
        for (ChecklistItem item : checklistItems) {
            if (!item.checked) count++;
        }
        tvUncheckedItems.setText("آیتم‌های باقی‌مانده: " + count);
    }

    private void loadTripData() {
        DocumentReference tripRef = db.collection("users").document(userId)
                .collection("trips").document(tripId);

        tripRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Toast.makeText(this, "خطا در دریافت داده‌ها", Toast.LENGTH_SHORT).show();
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                String destination = snapshot.getString("destination");
                String tripType = snapshot.getString("tripType");
                String transport = snapshot.getString("transport");
                String startDateStr = snapshot.getString("startDate");
                String endDateStr = snapshot.getString("endDate");
                List<String> companions = snapshot.get("companions") instanceof List ? (List<String>) snapshot.get("companions") : new ArrayList<>();

                tvTripTitle.setText(destination != null ? destination : "نامشخص");
                tvTripDestination.setText("مقصد: " + (destination != null ? destination : "نامشخص"));
                tvTripType.setText("نوع سفر: " + (tripType != null ? tripType : "نامشخص"));
                tvTransportType.setText("وسیله: " + (transport != null ? transport : "نامشخص"));
                tvCompanion.setText(companions.isEmpty() ? "همسفر: نامشخص" : "همسفر: " + TextUtils.join("، ", companions));

                tripStartDate = parsePersianDate(startDateStr);
                tripEndDate = parsePersianDate(endDateStr);

                if (tripStartDate != null && tripEndDate != null) {
                    tvTripDates.setText("تاریخ: " + startDateStr + " تا " + endDateStr);
                    updateDaysRemaining();
                } else {
                    tvTripDates.setText("تاریخ: نامشخص");
                    tvDaysRemaining.setText("روز باقی‌مانده: -");
                }
            }
        });
    }

    private PersianDate parsePersianDate(String dateStr) {
        try {
            if (TextUtils.isEmpty(dateStr)) return null;
            String[] parts = dateStr.split("/");
            if (parts.length != 3) return null;
            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int d = Integer.parseInt(parts[2]);
            PersianDate pd = new PersianDate();
            pd.setShYear(y);
            pd.setShMonth(m);
            pd.setShDay(d);
            return pd;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateDaysRemaining() {
        PersianDate today = new PersianDate();
        long millisDiff = tripStartDate.getTime() - today.getTime();
        int days = (int) (millisDiff / (1000 * 60 * 60 * 24));
        if (days < 0) days = 0;
        tvDaysRemaining.setText(days + " روز مانده تا سفر");
    }

    private void saveNotes() {
        String notes = etNotes.getText().toString();
        db.collection("users").document(userId)
                .collection("trips").document(tripId)
                .update("notes", notes);
    }

    private void loadNotes() {
        db.collection("users").document(userId)
                .collection("trips").document(tripId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        etNotes.setText(snapshot.getString("notes") != null ? snapshot.getString("notes") : "");
                    }
                });
    }

    private void saveChecklist() {
        List<Map<String, Object>> listMap = new ArrayList<>();
        for (ChecklistItem item : checklistItems) {
            Map<String, Object> map = new HashMap<>();
            map.put("text", item.text);
            map.put("checked", item.checked);
            listMap.add(map);
        }
        db.collection("users").document(userId)
                .collection("trips").document(tripId)
                .update("checklist", listMap);
    }

    private void loadChecklist() {
        db.collection("users").document(userId)
                .collection("trips").document(tripId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        checklistItems.clear();
                        List<Map<String, Object>> listMap = snapshot.get("checklist") instanceof List ? (List<Map<String, Object>>) snapshot.get("checklist") : new ArrayList<>();
                        for (Map<String, Object> map : listMap) {
                            String text = map.get("text") != null ? map.get("text").toString() : "";
                            boolean checked = map.get("checked") != null && (boolean) map.get("checked");
                            checklistItems.add(new ChecklistItem(text, checked));
                        }
                        adapter.notifyDataSetChanged();
                        updateUncheckedItemsCount();
                    }
                });
    }

    class ChecklistItem {
        String text;
        boolean checked;
        ChecklistItem(String text, boolean checked) { this.text = text; this.checked = checked; }
    }

    static class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {
        List<ChecklistItem> items;
        OnCheckChangedListener listener;

        ChecklistAdapter(List<ChecklistItem> items) { this.items = items; }

        void setOnCheckChangedListener(OnCheckChangedListener listener) { this.listener = listener; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChecklistItem item = items.get(position);
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setText(item.text);
            holder.checkBox.setChecked(item.checked);
            holder.checkBox.setTextColor(item.checked ? Color.GRAY : Color.BLACK);

            holder.checkBox.setOnCheckedChangeListener((btn, isChecked) -> {
                item.checked = isChecked;
                notifyDataSetChanged();
                if (listener != null) listener.onCheckChanged();
            });

            holder.btnEdit.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                EditText input = new EditText(v.getContext());
                input.setText(item.text);
                builder.setTitle("ویرایش آیتم")
                        .setView(input)
                        .setPositiveButton("ذخیره", (dialog, which) -> {
                            item.text = input.getText().toString();
                            notifyItemChanged(holder.getAdapterPosition());
                        })
                        .setNegativeButton("لغو", null)
                        .show();
            });

            holder.btnDelete.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                items.remove(pos);
                notifyItemRemoved(pos);
                if (listener != null) listener.onCheckChanged();
            });
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            ImageButton btnEdit, btnDelete;
            ViewHolder(@NonNull android.view.View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkBox);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }

        interface OnCheckChangedListener { void onCheckChanged(); }
    }
}
