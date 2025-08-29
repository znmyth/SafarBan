package ir.khu.safarban;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword;
    private Button btnSignup, btnBack;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // اتصال ویوها
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnBack = findViewById(R.id.btnBack);

        // مقداردهی Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // دکمه ثبت نام
        btnSignup.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            // بررسی پر بودن فیلدها
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "لطفاً همه فیلدها را پر کن", Toast.LENGTH_SHORT).show();
                return;
            }

            // بررسی فرمت ایمیل
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "ایمیل معتبر وارد کن", Toast.LENGTH_SHORT).show();
                return;
            }

            // بررسی حداقل طول رمز
            if (password.length() < 6) {
                Toast.makeText(this, "رمز عبور باید حداقل ۶ کاراکتر باشه!", Toast.LENGTH_SHORT).show();
                return;
            }

            // بررسی تطابق رمز و تکرار آن
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "رمز عبور با تکرارش یکسان نیست", Toast.LENGTH_SHORT).show();
                return;
            }

            // ثبت کاربر در Firebase Auth
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "ثبت‌نام موفق بود", Toast.LENGTH_SHORT).show();
                            // رفتن به صفحه لاگین
                            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish(); // بستن صفحه ثبت‌نام
                        } else {
                            String error = task.getException() != null ? task.getException().getMessage() : "خطای نامشخص";
                            Toast.makeText(this, "خطا در ثبت‌نام: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // دکمه بازگشت
        btnBack.setOnClickListener(v -> finish());
    }
}
