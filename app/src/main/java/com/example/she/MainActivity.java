package com.example.she;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView appText = findViewById(R.id.AppTextID);

        runnable = new Runnable() {
            @Override
            public void run() {
                navigateToLoginCheck();
            }
        };

        appText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(runnable);
                navigateToLoginCheck();
            }
        });

        handler.postDelayed(runnable, 800);
    }

    private void navigateToLoginCheck() {
        startActivity(new Intent(MainActivity.this, Login_Check.class));
        finish();
    }
}
