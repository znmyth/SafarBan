package ir.khu.safarban;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // تأخیر ۳ ثانیه‌ای برای نمایش لوگو
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // رفتن به صفحه اصلی برنامه بعد از اسپلش
                Intent i = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(i);
                finish(); // صفحه اسپلش بسته شود
            }
        }, 3000); // 3000 میلی‌ثانیه = ۳ ثانیه
    }
}
