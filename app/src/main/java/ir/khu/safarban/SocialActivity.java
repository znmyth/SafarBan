package ir.khu.safarban;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Random;

public class SocialActivity extends AppCompatActivity {

    private TextView tvFunMessage;
    private BottomNavigationView bottomNavigation;

    private String[] funMessages = {
            "دنیا یک کتاب است و آن‌هایی که سفر نمی‌کنند فقط یک صفحه‌اش را می‌خوانند. 🌍",
            "سفر انسان را فروتن می‌کند؛ می‌بینی در مقابل عظمت جهان، چه کوچک هستی. ✨",
            "مقصد مهم نیست؛ مهم این است که در مسیر چه یاد می‌گیری. 🛤️",
            "هیچ راهی بیهوده نیست؛ هر جاده‌ای چیزی برای گفتن دارد. 🚶",
            "گاهی باید از شهر خودت دور شوی تا زیبایی آن را ببینی. 🏙️",
            "خاطرات سفر ماندگارتر از هر سوغاتی هستند. 📖",
            "سفر، بهترین سرمایه‌گذاری روی خودت است. 💼",
            "هر توقف فرصتی برای دیدن دنیاست. ⏸️",
            "در سفر یاد می‌گیری که خوشبختی در سادگی‌هاست. 🌸",
            "دنیا منتظر توست؛ فقط باید قدم اول را برداری. 👣",
            "سفر تو را از روزمرگی نجات می‌دهد. 🔑",
            "هر کجا بروی، بخشی از آنجا درونت می‌ماند. 🪞",
            "آدم‌ها را در سفر می‌شناسی، نه در حرف. 🧑‍🤝‍🧑",
            "گاهی لازم است گم شوی تا خودت را پیدا کنی. 🧭",
            "سفر یعنی جرأت ترک راحتی‌ها. 🚀",
            "سفر یعنی زندگی در لحظه. ⏳",
            "هر مقصد تازه، شروعی دوباره است. 🌅",
            "دنیا را نمی‌توان در خانه شناخت؛ باید سفر کرد. 🏡",
            "بهترین راه شناخت خودت، قدم گذاشتن در جاده است. 🛣️",
            "سفر یعنی فهمیدن اینکه یک چمدان کافی است. 🎒",
            "به مقصد فکر نکن؛ قشنگی وسط راه است. 🌟",
            "زندگی هم مثل سفر است؛ ناگهان می‌فهمی رسیدی. ⌛",
            "راه طولانی فرصتی برای فکر کردن می‌دهد. 🤔",
            "سفر یعنی بی‌برنامه‌ترین لحظه، بهترین لحظه است. 🎭",
            "بهترین هم‌سفر کسی است که حال خودت را خوب کنی. 🪞",
            "خسته‌ای؟ یک لبخند بزن، کار می‌کند. 🙂",
            "خسته شدی؟ ولی هنوز زنده‌ای؛ همین کافی است برای ادامه دادن. 💫",
            "حالا نه، ولی روزی برای همین لحظه دلت تنگ می‌شود. 🕰️",
            "بعضی جاها را باید با دل رفت، نه با GPS. ❤️",
            "برو جایی که موبایل آنتن نمی‌دهد، ولی قلبت آره! 📵",
            "کار ناتمام را فردا نگذار؛ چون فردا خودش هزار کار دارد. 📌",
            "زندگی مثل دوچرخه است؛ برای حفظ تعادل باید حرکت کنی. 🚲",
            "امید، چیز خوبی است؛ شاید بهترین چیز. 🌈",
            "هیچ‌وقت نگذار کسی به تو بگوید نمی‌توانی کاری را انجام دهی. 🚫",
            "دنیا مال آدم‌هایی است که زودتر از خواب بیدار می‌شوند؛ حتی اگر بعدش دوباره بخوابند. 😅",
            "خوشبختی در مقصد نیست؛ در راه است. 🚶",
            "همیشه برای چیزی که ارزش دارد، بجنگ. ⚔️",
            "ما تصمیم می‌گیریم چه کنیم با زمانی که به ما داده شده. ⏳",
            "گاهی سفر یعنی فرار از تکرار. ✈️",
            "به یاد داشته باش: حتی تاریک‌ترین شب هم به طلوع ختم می‌شود. 🌅",
            "روزی به عقب نگاه می‌کنی و می‌خندی به همین روزها. 🙂",
            "وقتی خسته‌ای، استراحت کن؛ جا نزن. 🛌",
            "باور کن می‌توانی؛ نصف راه را رفته‌ای. 🚀"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        tvFunMessage = findViewById(R.id.tvFunMessage);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // بارگذاری فونت از res/font/vazirbold.ttf
        try {
            Typeface vazirBold = ResourcesCompat.getFont(this, R.font.vazirbold);
            if (vazirBold != null) {
                tvFunMessage.setTypeface(vazirBold);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "فونت بارگذاری نشد", Toast.LENGTH_SHORT).show();
        }

        // انتخاب جمله تصادفی برای نمایش
        Random random = new Random();
        int index = random.nextInt(funMessages.length);
        tvFunMessage.setText(funMessages[index]);

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_social); // آیتم فعلی

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_social) {
                return true; // همین صفحه است
            }

            return false;
        });
    }
}
