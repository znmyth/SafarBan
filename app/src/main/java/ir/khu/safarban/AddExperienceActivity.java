package ir.khu.safarban;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddExperienceActivity extends AppCompatActivity {

    private EditText etTitle, etChecklist, etComments;
    private Button btnSave;

    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_experience);

        etTitle = findViewById(R.id.etTitle);
        etChecklist = findViewById(R.id.etChecklist);
        etComments = findViewById(R.id.etComments);
        btnSave = findViewById(R.id.btnSave);

        databaseRef = FirebaseDatabase.getInstance().getReference("experiences");

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveExperience();
            }
        });
    }

    private void saveExperience() {
        String title = etTitle.getText().toString().trim();
        String checklist = etChecklist.getText().toString().trim();
        String comments = etComments.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("عنوان را وارد کنید");
            return;
        }

        String id = databaseRef.push().getKey();
        Experience experience = new Experience(title, checklist, comments);

        if (id != null) {
            databaseRef.child(id).setValue(experience);
            Toast.makeText(this, "تجربه ثبت شد", Toast.LENGTH_SHORT).show();
            finish(); // برمی‌گردیم به اکتیویتی قبلی
        } else {
            Toast.makeText(this, "خطا در ذخیره تجربه", Toast.LENGTH_SHORT).show();
        }
    }
}
