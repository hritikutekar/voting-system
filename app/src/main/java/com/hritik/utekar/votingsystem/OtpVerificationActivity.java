package com.hritik.utekar.votingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OtpVerificationActivity extends AppCompatActivity implements View.OnClickListener {
    static final String TAG = "OtpVerificationActivity";

    String mVerificationId;

    private FirebaseAuth mAuth;

    private EditText editTextOtp;
    private ProgressBar progressBar;
    private ConstraintLayout container;
    private TextView textViewGuide;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        mAuth = FirebaseAuth.getInstance();

        editTextOtp = findViewById(R.id.edit_text_otp);
        Button buttonConfirm = findViewById(R.id.button_confirm);
        progressBar = findViewById(R.id.progressBar);
        container = findViewById(R.id.container);
        textViewGuide = findViewById(R.id.text_view_guide);
        TextView textViewResend = findViewById(R.id.text_view_resend);

        phone = getIntent().getStringExtra("phone");
        verifyPhoneNumber(phone);

        buttonConfirm.setOnClickListener(this);
        textViewResend.setOnClickListener(this);
        progressBar.setVisibility(View.INVISIBLE);
        textViewGuide.setText(String.format(getResources().getString(R.string.verification_guide), phone));
    }

    private void verifyPhoneNumber(String phone) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,
                60,
                TimeUnit.SECONDS,
                this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        editTextOtp.setText(mVerificationId);
                        progressBarVisibility(true);
                        Log.d(TAG, "onVerificationCompleted: " + phoneAuthCredential);
                        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser mUser = Objects.requireNonNull(task.getResult()).getUser();
                                    Log.d(TAG, "onComplete: FirebaseUser:" + task.getResult());

                                    startActivity(new Intent(OtpVerificationActivity.this, UserDetailsActivity.class));
                                } else {
                                    Log.e(TAG, "onComplete:" + task.getException());
                                    progressBarVisibility(false);
                                }
                            }
                        });
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException error) {
                        Log.e(TAG, "onVerificationFailed:" + error);
                    }

                    @Override
                    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        Log.d(TAG, "onCodeSent:" + verificationId);
                        mVerificationId = verificationId;

                        super.onCodeSent(verificationId, forceResendingToken);
                    }
                }
        );
    }

    private void signInWithPhone(String otp) {
        progressBarVisibility(true);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);

        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser mUser = Objects.requireNonNull(task.getResult()).getUser();
                    Log.d(TAG, "onComplete: FirebaseUser:" + task.getResult());

                    startActivity(new Intent(OtpVerificationActivity.this, UserDetailsActivity.class));
                } else {
                    Log.e(TAG, "onComplete Here:" + task.getException());
                    progressBarVisibility(false);

                    Snackbar.make(findViewById(R.id.parent_container),
                            "Invalid verification code", Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_confirm: {
                String otp = editTextOtp.getText().toString();
                signInWithPhone(otp);
                break;
            }

            case R.id.text_view_resend: {
                Log.d(TAG, "onClick: Resend");
                // Resend the code to the same phone.
                verifyPhoneNumber(phone);
                textViewGuide.setText(String.format(getResources().getString(R.string.verification_resend_guide), phone));
                break;
            }
        }
    }

    void progressBarVisibility(boolean visibility) {
        if (visibility) {
            // Show progress bar and hide container layout.
            progressBar.setVisibility(View.VISIBLE);
            container.setVisibility(View.INVISIBLE);
        } else {
            // Hide progress bar and show container layout.
            progressBar.setVisibility(View.INVISIBLE);
            container.setVisibility(View.VISIBLE);
        }
    }
}
