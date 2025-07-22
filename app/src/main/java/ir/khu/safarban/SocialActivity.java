package ir.khu.safarban;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SocialActivity extends AppCompatActivity {

    private RecyclerView rvExperiences;
    private ExperienceAdapter adapter;
    private List<Experience> experienceList = new ArrayList<>();
    private EditText etSearch;

    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        rvExperiences = findViewById(R.id.rvExperiences);
        etSearch = findViewById(R.id.etSearch);

        rvExperiences.setLayoutManager(new LinearLayoutManager(this));

        ref = FirebaseDatabase.getInstance().getReference("experiences");

        // خواندن داده‌ها از Firebase
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                experienceList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Experience exp = data.getValue(Experience.class);
                    if (exp != null) {
                        experienceList.add(exp);
                    }
                }

                adapter = new ExperienceAdapter(experienceList);
                rvExperiences.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // در صورت خطا
            }
        });

        // سرچ
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterList(String query) {
        List<Experience> filtered = new ArrayList<>();
        for (Experience exp : experienceList) {
            if (exp.getTitle() != null &&
                    exp.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(exp);
            }
        }

        adapter = new ExperienceAdapter(filtered);
        rvExperiences.setAdapter(adapter);
    }
}
