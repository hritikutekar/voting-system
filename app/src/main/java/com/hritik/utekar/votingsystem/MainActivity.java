package com.hritik.utekar.votingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    static final String TAG = "MainActivity";

    private EditText editTextPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextPhone = findViewById(R.id.edit_text_phone);
        Button buttonLogin = findViewById(R.id.button_login);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone = "+91" + editTextPhone.getText().toString();
                Intent intent = new Intent(MainActivity.this, OtpVerificationActivity.class);
                intent.putExtra("phone", phone);
                startActivity(intent);
            }
        });
    }
}
