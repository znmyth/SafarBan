package ir.khu.safarban;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class LoginActivity extends AppCompatActivity {

    private EditText login_etEmail, login_etPassword;
    private Button login_btnLogin, login_btnGoogle, login_btnBack;
    private TextView login_tvSignup, login_tvForgot;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ویوها
        login_etEmail = findViewById(R.id.login_etEmail);
        login_etPassword = findViewById(R.id.login_etPassword);
        login_btnLogin = findViewById(R.id.login_btnLogin);
        login_btnGoogle = findViewById(R.id.login_btnGoogleLogin);
        login_btnBack = findViewById(R.id.login_btnBack);
        login_tvSignup = findViewById(R.id.login_tvSignup);
        login_tvForgot = findViewById(R.id.login_tvForgotPassword);

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Google Sign-In Options
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // ورود با ایمیل
        login_btnLogin.setOnClickListener(v -> {
            String email = login_etEmail.getText().toString().trim();
            String password = login_etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "لطفاً همه فیلدها را پر کنید.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "ایمیل معتبر وارد کنید.", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Toast.makeText(this, "خوش آمدید " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            goToHome();
                        } else {
                            String errorMessage = "ورود ناموفق!";
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                errorMessage = "کاربری با این ایمیل یافت نشد.";
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "رمز عبور اشتباه است.";
                            }
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // ورود با گوگل
        login_btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // دکمه بازگشت
        login_btnBack.setOnClickListener(v -> finish());

        // فراموشی رمز عبور
        login_tvForgot.setOnClickListener(v -> {
            String email = login_etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "لطفاً ابتدا ایمیل خود را وارد کنید.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "ایمیل معتبر وارد کنید.", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "لینک بازیابی رمز عبور به ایمیلتان ارسال شد.", Toast.LENGTH_LONG).show();
                        } else {
                            String errorMsg = "خطا در ارسال ایمیل بازیابی!";
                            if (task.getException() != null) {
                                errorMsg += " (" + task.getException().getMessage() + ")";
                            }
                            Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // رفتن به ثبت‌نام
        login_tvSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class))
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Toast.makeText(this, "دریافت اطلاعات از گوگل ناموفق بود.", Toast.LENGTH_SHORT).show();
                }
            } catch (ApiException e) {
                Toast.makeText(this, "ورود با گوگل ناموفق بود: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        Toast.makeText(this, "سلام " + user.getDisplayName(), Toast.LENGTH_SHORT).show();
                        goToHome();
                    } else {
                        Toast.makeText(this, "ورود با گوگل شکست خورد.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToHome() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
