package ir.khu.safarban;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btnSignup, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // اتصال ویوها
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnBack = findViewById(R.id.btnBack);

        // دکمه ثبت نام
        btnSignup.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirmPassword = etConfirmPassword.getText().toString();

            // بررسی پر بودن فیلدها
            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "لطفاً همه فیلدها را پر کنید", Toast.LENGTH_SHORT).show();
                return;
            }

            // بررسی فرمت ایمیل
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "ایمیل معتبر وارد کنید", Toast.LENGTH_SHORT).show();
                return;
            }

            // بررسی حداقل طول رمز
            if (password.length() < 6) {
                Toast.makeText(this, "رمز عبور باید حداقل ۶ کاراکتر باشد", Toast.LENGTH_SHORT).show();
                return;
            }

            // بررسی تطابق رمز و تکرار آن
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "رمز عبور با تکرار آن یکسان نیست", Toast.LENGTH_SHORT).show();
                return;
            }

            // ثبت نام موفق (فعلاً فقط تست)
            Toast.makeText(this, "ثبت‌نام موفق بود", Toast.LENGTH_SHORT).show();

            // رفتن به صفحه لاگین
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // بستن صفحه ثبت‌نام تا با کلید بازگشت به آن برنگردند
        });

        // دکمه بازگشت
        btnBack.setOnClickListener(v -> finish());
    }
}
