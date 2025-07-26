package ir.khu.safarban;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import saman.zamani.persiandate.PersianDate;

public class TripDetailsActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_TRIP = 1001;

    TextView tvTripTitle, tvTripDates, tvTripDestination, tvTripType, tvTransportType, tvDaysRemaining;
    EditText etNewItem, etNotes;
    Button btnAddItem, btnSave, btnEdit, btnBack;
    RecyclerView recyclerChecklist;

    ChecklistAdapter adapter;
    List<ChecklistItem> checklistItems;

    SharedPreferences prefs;

    PersianDate tripStartDate, tripEndDate;

    TextView tvCompanion;
    TextView tvUncheckedItems;

    FirebaseFirestore db;
    String tripId, userId;


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

        btnEdit = findViewById(R.id.btnEditTrip);
        etNewItem = findViewById(R.id.etNewItem);
        etNotes = findViewById(R.id.etNotes);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);  // اینجا دکمه Back رو میگیریم

        tvCompanion = findViewById(R.id.tvCompanion);
        tvUncheckedItems = findViewById(R.id.tvUncheckedItems);
        recyclerChecklist = findViewById(R.id.recyclerChecklist);
        recyclerChecklist.setLayoutManager(new LinearLayoutManager(this));

        checklistItems = new ArrayList<>();
        adapter = new ChecklistAdapter(checklistItems);
        recyclerChecklist.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        tripId = intent.getStringExtra("trip_id");
        userId = (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null)
                ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (tripId == null || userId == null) {
            Toast.makeText(this, "شناسه سفر یا کاربر یافت نشد!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        prefs = getSharedPreferences("trip_data_" + tripId, Context.MODE_PRIVATE);

        listenTripDataChanges();
        loadChecklist();
        loadNotes();

        updateUncheckedItemsCount();

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

        btnSave.setOnClickListener(v -> {
            saveNotes();
            saveChecklist();
            Toast.makeText(this, "اطلاعات ذخیره شد", Toast.LENGTH_SHORT).show();

            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });

        adapter.setOnCheckChangedListener(this::updateUncheckedItemsCount);

        btnEdit.setOnClickListener(v -> {
            Intent editIntent = new Intent(this, NewTripActivity.class);
            editIntent.putExtra("trip_id", tripId);
            startActivityForResult(editIntent, REQUEST_EDIT_TRIP);
        });

        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_TRIP && resultCode == RESULT_OK) {
            listenTripDataChanges();
            Toast.makeText(this, "اطلاعات با موفقیت بروز شد", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUncheckedItemsCount() {
        int count = 0;
        for (ChecklistItem item : checklistItems) {
            if (!item.checked) count++;
        }
        tvUncheckedItems.setText("آیتم‌های باقی‌مانده: " + count);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putInt("unchecked_count_" + tripId, count).apply();
    }

    private void listenTripDataChanges() {
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
                List<String> companions = (List<String>) snapshot.get("companions");

                tvTripTitle.setText(destination != null ? destination : "عنوان نامشخص");
                tvTripDestination.setText("مقصد: " + (destination != null ? destination : "نامشخص"));
                tvTripType.setText("نوع سفر: " + (tripType != null ? tripType : "نامشخص"));
                tvTransportType.setText("وسیله: " + (transport != null ? transport : "نامشخص"));

                if (companions != null && !companions.isEmpty()) {
                    String companionsText = TextUtils.join("، ", companions);
                    tvCompanion.setText("همسفر: " + companionsText);
                } else {
                    tvCompanion.setText("همسفر: نامشخص");
                }

                tripStartDate = parsePersianDate(startDateStr);
                tripEndDate = parsePersianDate(endDateStr);

                if (tripStartDate != null && tripEndDate != null) {
                    tvTripDates.setText("تاریخ: " + startDateStr + " تا " + endDateStr);
                    updateDaysRemaining();
                } else {
                    tvTripDates.setText("تاریخ: نامشخص");
                    tvDaysRemaining.setText("روز باقی‌مانده: -");
                }
            } else {
                Toast.makeText(this, "اطلاعات سفر یافت نشد", Toast.LENGTH_SHORT).show();
                finish();
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
        String encrypted = Base64.encodeToString(etNotes.getText().toString().getBytes(), Base64.DEFAULT);
        prefs.edit().putString("notes", encrypted).apply();
    }

    private void loadNotes() {
        String encrypted = prefs.getString("notes", "");
        try {
            String decrypted = new String(Base64.decode(encrypted, Base64.DEFAULT));
            etNotes.setText(decrypted);
        } catch (Exception e) {
            etNotes.setText("");
        }
    }

    private void saveChecklist() {
        StringBuilder sb = new StringBuilder();
        for (ChecklistItem item : checklistItems) {
            sb.append(item.text.replace("|", "")).append("||").append(item.checked).append("##");
        }
        prefs.edit().putString("checklist", sb.toString()).apply();
    }

    private void loadChecklist() {
        checklistItems.clear();
        String saved = prefs.getString("checklist", "");
        if (!TextUtils.isEmpty(saved)) {
            String[] items = saved.split("##");
            for (String s : items) {
                if (TextUtils.isEmpty(s)) continue;
                String[] parts = s.split("\\|\\|");
                if (parts.length == 2) {
                    checklistItems.add(new ChecklistItem(parts[0], Boolean.parseBoolean(parts[1])));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    class ChecklistItem {
        String text;
        boolean checked;

        ChecklistItem(String text, boolean checked) {
            this.text = text;
            this.checked = checked;
        }
    }


    static class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ViewHolder> {
        List<ChecklistItem> items;
        OnCheckChangedListener listener;

        ChecklistAdapter(List<ChecklistItem> items) {
            this.items = items;
        }

        void setOnCheckChangedListener(OnCheckChangedListener listener) {
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChecklistItem item = items.get(position);
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setText(item.text);
            holder.checkBox.setChecked(item.checked);
            holder.checkBox.setTextColor(item.checked ? Color.GRAY : Color.BLACK);

            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.checked = isChecked;
                notifyDataSetChanged();
                if (listener != null) {
                    listener.onCheckChanged();
                }
            });

            holder.btnEdit.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                final EditText input = new EditText(v.getContext());
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
                if (listener != null) {
                    listener.onCheckChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            ImageButton btnEdit, btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkBox);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnDelete = itemView.findViewById(R.id.btnDelete);
            }
        }

        interface OnCheckChangedListener {
            void onCheckChanged();
        }
    }
}
