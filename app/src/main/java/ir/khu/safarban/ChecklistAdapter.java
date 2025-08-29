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
        holder.bind(item);

        // دکمه حذف
        holder.btnDelete.setOnClickListener(v -> {
            checklistItems.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
            if (checkChangedListener != null) {
                checkChangedListener.onCheckChanged(getUncheckedItemCount());
            }
        });

        // دکمه ویرایش
        holder.btnEdit.setOnClickListener(v -> holder.toggleEditMode());
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

    class ChecklistViewHolder extends RecyclerView.ViewHolder {
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

        void bind(ChecklistItem item) {
            // جلوگیری از چرخه ناخواسته
            checkBox.setOnCheckedChangeListener(null);

            // مقداردهی اولیه
            checkBox.setText(item.getText());
            checkBox.setChecked(item.isChecked());
            editText.setText(item.getText());

            // نمایش حالت معمولی
            editText.setVisibility(View.GONE);
            checkBox.setVisibility(View.VISIBLE);

            // تغییر تیک
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                item.setChecked(isChecked);
                if (checkChangedListener != null) {
                    checkChangedListener.onCheckChanged(getUncheckedItemCount());
                }
            });

            // تغییر متن
            editText.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void afterTextChanged(Editable s) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.setText(s.toString());
                    checkBox.setText(s.toString());
                }
            });
        }

        void toggleEditMode() {
            if (editText.getVisibility() == View.GONE) {
                editText.setVisibility(View.VISIBLE);
                checkBox.setVisibility(View.GONE);
                editText.requestFocus();
            } else {
                editText.setVisibility(View.GONE);
                checkBox.setVisibility(View.VISIBLE);
            }
        }
    }
}
