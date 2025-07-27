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
            "حالا فعلاً یه چایی بخوریم! شاید بعداً این بخشم یه چی اضافه کردیم 👻",
            "یه روزی می‌خندی به همه این روزا… اون روز شاید امروز باشه 🙂",
            "حال خوب ساختنیه، منتظر نمون! 💪",
            "چمدونتو ببند، بزن به دل زندگی ✈️",
            "دنیا همینه... یه روز بالا، یه روز پیاده وسط جاده! 🛣️😅",
            "خسته‌ای؟ یه لبخند بزن، کار می‌کنه 😄",
            "دل خوش سیری چند؟ بسه، همین الان بزن بریم 🎒",
            "نترس! بیشتر اتفاقای خوب از دل یه تصمیم عجیب شروع شدن 💥",
            "شاید راه دور باشه، ولی حال خوب همین نزدیکیاس 😊",
            "هر جا که لبخندت باشه، همون‌جا وطنه 🌍",
            "با یه چای و یه لبخند، روزتو بساز 🍵🙂",
            "خسته شدی؟ ولی هنوز زنده‌ای. همین کافیه واسه ادامه دادن 💫",
            "بزن بریم شمال… اگه دلت تنگه برای مه و بوی نم ☁️🌲",
            "آروم باش، آخرش همه‌چی درست میشه… یا نمی‌شه، ولی تو یاد می‌گیری 😄",
            "وقتی خندیدی، نصف راهو رفتی! 😁",
            "دنیا منتظر توئه که بجنبی! 🏃‍♂️🌍",
            "هر سفری یه قصه‌ست، حتی اگه مقصدش نونوایی باشه! 🥖",
            "برگرد عقب؟ نه، فقط واسه عکس گرفتن 📸",
            "استراحت حق مسلم توئه، حتی وسط یه زندگی شلوغ 🧘‍♀️",
            "زندگی همینه، قشنگ و غیرمنتظره 🎢",
            "سفر یعنی یه لحظه فرار از تکرار 🎒",
            "مهم نیست کجایی، مهم اینه چی توی دلت می‌گذره ❤️",
            "قرار نیست همه‌چی عالی باشه، فقط کافیِ واقعی باشه 🌟",
            "حالا نه، ولی یه روزی واسه همین لحظه دلت تنگ می‌شه 🕰️",
            "اگه بهت گفتن نمی‌تونی، یعنی باید دوبرابر بری جلو! 🚀",
            "جا نزن، جاده قشنگ‌تر از اونیه که فکر می‌کنی 🌄",
            "یه آدم خسته هم می‌تونه خوشحال باشه، اگه یه چایی داشته باشه ☕",
            "زنده بودن یعنی هنوز وقت داری رویا ببینی ✨",
            "خاطره‌هات از همه‌چی قشنگ‌ترن، فقط بنویسشون ✍️",
            "بعضی جاها رو باید با دل رفت، نه با GPS ❤️",
            "هر چی جلوتر بری، ترسات کوچیک‌تر می‌شن 🚶‍♂️",
            "یه لبخند بزن، شاید حال یکیو خوب کنی 😌",
            "برو جایی که موبایل آنتن نمی‌ده، ولی قلبت آره! 📴💓",
            "هر اشتباهی یه قدم نزدیک‌تر به یادگیریه 💡",
            "وقتی خسته‌ای، سفر بهترین درمانه 🎒🧳",
            "شاید راه گم کنی، ولی خودتو پیدا می‌کنی 🧭",
            "غصه نخور، یه مسافر خوب همیشه راهشو پیدا می‌کنه 🛤️",
            "زندگی اونیه که بین دو قسط اتفاق می‌افته! 😅",
            "بریم یه جای بکر… مثل یخچال بعد ساعت ۱۲ شب 😋",
            "هنوز زوده برای تسلیم شدن، دیرم هست برای شروع نکردن ⏳",
            "خودتو دریاب، نه بقیه رو 📍",
            "تو هر کی باشی، یه سفر خوب حقته! 😌",
            "انرژی خوب مثل آفتابه… باید پاشی بری دنبالش 😂",
            "بلند شو، دنیا نمی‌خوابه صبر کنه تا حال تو خوب شه 😴🌍",
            "ساده بگیر، زندگی همین سادگیاس ✨",
            "با یه کوله و کمی دل، می‌شه دنیا رو فتح کرد 🏕️",
            "دنیا همینه… پر از چالش، پر از چای 🍵🙂",
            "سفر، تمرینیه برای زندگی: بدون برگشت، فقط رفت 🚗",
            "هر چیزی رو نمی‌شه کنترل کرد، ولی می‌شه چای خورد و ادامه داد 🍵",
            "الان بخند، شاید بعداً وقت نکنی! 😄"
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
