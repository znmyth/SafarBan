package ir.khu.safarban;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CompanionAdapter extends RecyclerView.Adapter<CompanionAdapter.CompanionViewHolder> {

    private final List<String> companions;

    public CompanionAdapter(List<String> companions) {
        this.companions = companions;
    }

    @NonNull
    @Override
    public CompanionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_companion, parent, false);
        return new CompanionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompanionViewHolder holder, int position) {
        String name = companions.get(position);
        holder.tvName.setText(name);

        // حذف همسفر
        holder.btnDelete.setOnClickListener(v -> {
            companions.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, companions.size());
        });

        // ویرایش همسفر با کلیک روی نام
        holder.tvName.setOnClickListener(v -> {
            Context context = v.getContext();
            EditText input = new EditText(context);
            input.setText(name);

            new AlertDialog.Builder(context)
                    .setTitle("ویرایش نام همسفر")
                    .setView(input)
                    .setPositiveButton("ذخیره", (dialog, which) -> {
                        String newName = input.getText().toString().trim();
                        if (!newName.isEmpty()) {
                            companions.set(position, newName);
                            notifyItemChanged(position);
                        } else {
                            Toast.makeText(context, "نام نمی‌تواند خالی باشد", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("لغو", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return companions.size();
    }

    static class CompanionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnDelete;

        public CompanionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvCompanionName);
            btnDelete = itemView.findViewById(R.id.btnDeleteCompanion);
        }
    }
}
