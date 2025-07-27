package ir.khu.safarban;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExperienceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ADD_NEW = 0;
    private static final int TYPE_EXPERIENCE = 1;

    private List<Experience> experiences;
    private Context context;

    public ExperienceAdapter(Context context, List<Experience> experiences) {
        this.context = context;
        this.experiences = experiences;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_ADD_NEW;
        } else {
            return TYPE_EXPERIENCE;
        }
    }

    @Override
    public int getItemCount() {
        return experiences.size() + 1; // +1 برای کارت ثابت "ثبت تجربه من"
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ADD_NEW) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_add_experience, parent, false);
            return new AddExperienceViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_experience, parent, false);
            return new ExperienceViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AddExperienceViewHolder) {
            ((AddExperienceViewHolder) holder).bind();
        } else if (holder instanceof ExperienceViewHolder) {
            Experience experience = experiences.get(position - 1); // -1 به خاطر کارت ثابت
            ((ExperienceViewHolder) holder).bind(experience);
        }
    }

    // ViewHolder برای کارت "ثبت تجربه من"
    class AddExperienceViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddNew;

        public AddExperienceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAddNew = itemView.findViewById(R.id.tvAddNewExperience);
        }

        public void bind() {
            tvAddNew.setText("ثبت تجربه من +");
            itemView.setOnClickListener(v -> {
                if (onAddExperienceClickListener != null) {
                    onAddExperienceClickListener.onAddExperienceClick();
                }
            });
        }
    }

    // ViewHolder برای کارت تجربه‌های عادی
    class ExperienceViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        public ExperienceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            // اگر ویوهای بیشتری داری، اینجا findViewById کن
        }

        public void bind(Experience experience) {
            tvTitle.setText(experience.getTitle());
            // اینجا می‌تونی باز و بسته شدن کارت، لایک، کامنت و ... رو اضافه کنی
        }
    }

    // اینترفیس کلیک روی کارت "ثبت تجربه من"
    public interface OnAddExperienceClickListener {
        void onAddExperienceClick();
    }

    private OnAddExperienceClickListener onAddExperienceClickListener;

    public void setOnAddExperienceClickListener(OnAddExperienceClickListener listener) {
        this.onAddExperienceClickListener = listener;
    }
}
