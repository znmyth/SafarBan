package ir.khu.safarban;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvWelcome, tvUsername;
    private Button btnLogin, btnSignup, btnLogout;
    private ImageView ivProfilePic;
    private LinearLayout layoutAuthButtons, layoutUserProfile;
    private BottomNavigationView bottomNavigation;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private Uri profileImageUri;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    profileImageUri = uri;
                    // به جای setImageURI از Glide استفاده می‌کنیم تا تصویر دایره‌ای شود
                    Glide.with(this)
                            .load(profileImageUri)
                            .circleCrop()
                            .into(ivProfilePic);

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        DocumentReference userDoc = firestore.collection("users").document(uid);

                        Map<String, Object> data = new HashMap<>();
                        data.put("imageUri", profileImageUri.toString());

                        userDoc.set(data)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "تصویر ذخیره شد", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "خطا در ذخیره تصویر", Toast.LENGTH_SHORT).show());
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvUsername = findViewById(R.id.tvUsername);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        btnLogout = findViewById(R.id.btnLogout);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        layoutAuthButtons = findViewById(R.id.layoutAuthButtons);
        layoutUserProfile = findViewById(R.id.layoutUserProfile);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setSelectedItemId(R.id.nav_profile);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_social) {
                startActivity(new Intent(ProfileActivity.this, SocialActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        checkUserStatus();

        btnLogin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        btnSignup.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        ivProfilePic.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnLogout.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Toast.makeText(this, "عه! خارج شدی.", Toast.LENGTH_SHORT).show();
            checkUserStatus();
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null) {
            tvWelcome.setText("این اولین قدم تو برای پخته شدنه! خوش اومدی!");
            layoutAuthButtons.setVisibility(LinearLayout.VISIBLE);
            layoutUserProfile.setVisibility(LinearLayout.GONE);
        } else {
            tvWelcome.setText("یک سفرمان نشود ناصر خسرو!");
            layoutAuthButtons.setVisibility(LinearLayout.GONE);
            layoutUserProfile.setVisibility(LinearLayout.VISIBLE);

            String username = user.getDisplayName();
            if (username == null || username.isEmpty()) {
                username = user.getEmail();
            }
            tvUsername.setText(username);

            firestore.collection("users").document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imageUri = documentSnapshot.getString("imageUri");
                            if (imageUri != null) {
                                Glide.with(this)
                                        .load(Uri.parse(imageUri))
                                        .placeholder(R.drawable.ic_launcher_foreground)
                                        .circleCrop() // دایره‌ای کردن عکس
                                        .into(ivProfilePic);
                            } else {
                                // اگر عکس نداشت، تصویر پیش‌فرض دایره‌ای شود
                                Glide.with(this)
                                        .load(R.drawable.ic_launcher_foreground)
                                        .circleCrop()
                                        .into(ivProfilePic);
                            }
                        }
                    });
        }
    }
}
