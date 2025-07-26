package ir.khu.safarban;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder> {

    private final ArrayList<ChecklistItem> checklistItems;
    private final Context context;
    private OnCheckChangedListener checkChangedListener;

    public ChecklistAdapter(Context context, ArrayList<ChecklistItem> items) {
        this.context = context;
        this.checklistItems = items;
    }

    // اینترفیس برای اطلاع از تغییر وضعیت چک‌باکس‌ها
    public interface OnCheckChangedListener {
        void onCheckChanged(int uncheckedCount);
    }

    public void setOnCheckChangedListener(OnCheckChangedListener listener) {
        this.checkChangedListener = listener;
    }

    @NonNull
    @Override
    public ChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checklist, parent, false);
        return new ChecklistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ChecklistItem item = checklistItems.get(position);
        holder.checkBox.setText(item.getText());
        holder.checkBox.setChecked(item.isChecked());
        holder.editText.setText(item.getText());
        holder.editText.setVisibility(View.GONE);
        holder.checkBox.setVisibility(View.VISIBLE);

        // هندل کردن تیک زدن
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setChecked(isChecked);
            if (checkChangedListener != null) {
                checkChangedListener.onCheckChanged(getUncheckedItemCount());
            }
        });

        // حذف آیتم
        holder.btnDelete.setOnClickListener(v -> {
            checklistItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, checklistItems.size());
            if (checkChangedListener != null) {
                checkChangedListener.onCheckChanged(getUncheckedItemCount());
            }
        });

        // ویرایش آیتم
        holder.btnEdit.setOnClickListener(v -> {
            if (holder.editText.getVisibility() == View.GONE) {
                holder.editText.setText(item.getText());
                holder.editText.setVisibility(View.VISIBLE);
                holder.checkBox.setVisibility(View.GONE);
                holder.editText.requestFocus();
            } else {
                String newText = holder.editText.getText().toString();
                item.setText(newText);
                holder.checkBox.setText(newText);
                holder.editText.setVisibility(View.GONE);
                holder.checkBox.setVisibility(View.VISIBLE);
            }
        });

        // تایپ داخل EditText
        holder.editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                item.setText(s.toString());
            }
        });
    }

    private int getUncheckedItemCount() {
        int count = 0;
        for (ChecklistItem item : checklistItems) {
            if (!item.isChecked()) count++;
        }
        return count;
    }

    @Override
    public int getItemCount() {
        return checklistItems.size();
    }

    static class ChecklistViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        EditText editText;
        ImageButton btnDelete, btnEdit;

        ChecklistViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkBox);
            editText = itemView.findViewById(R.id.editTextItem);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }

    // کلاس آیتم چک‌لیست
    public static class ChecklistItem {
        private String text;
        private boolean isChecked;

        public ChecklistItem(String text) {
            this.text = text;
            this.isChecked = false;
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public boolean isChecked() { return isChecked; }
        public void setChecked(boolean checked) { isChecked = checked; }
    }
}
