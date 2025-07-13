package com.example.she;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView appText = findViewById(R.id.AppTextID);
        appText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,Login_Check.class));
            }
        });
       new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {
               startActivity(new Intent(MainActivity.this,Login_Check.class));
           }
       },800);

    }
}
