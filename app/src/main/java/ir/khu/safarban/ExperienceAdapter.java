package ir.khu.safarban;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExperienceAdapter extends RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder> {

    private List<Experience> experienceList;

    public ExperienceAdapter(List<Experience> experienceList) {
        this.experienceList = experienceList;
    }

    @NonNull
    @Override
    public ExperienceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experience, parent, false);
        return new ExperienceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperienceViewHolder holder, int position) {
        Experience experience = experienceList.get(position);
        holder.tvTitle.setText(experience.getTitle());
        holder.tvChecklist.setText("✅ " + experience.getChecklist());
        holder.tvComments.setText("💬 " + experience.getComments());

        holder.itemView.setOnClickListener(v -> {
            if (holder.detailsLayout.getVisibility() == View.GONE) {
                holder.detailsLayout.setVisibility(View.VISIBLE);
            } else {
                holder.detailsLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return experienceList.size();
    }

    static class ExperienceViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvChecklist, tvComments;
        LinearLayout detailsLayout;

        public ExperienceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvChecklist = itemView.findViewById(R.id.tvChecklist);
            tvComments = itemView.findViewById(R.id.tvComments);
            detailsLayout = itemView.findViewById(R.id.detailsLayout);
        }
    }
}
