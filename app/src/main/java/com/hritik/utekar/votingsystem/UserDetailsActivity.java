package com.hritik.utekar.votingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class UserDetailsActivity extends AppCompatActivity {
    static final String TAG = "UserDetailsActivity";

    private FirebaseUser mUser;

    private EditText editTextName;
    private EditText editTextVoterId;
    private EditText editTextAadhar;
    private ProgressBar progressBar;
    private ConstraintLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        Button buttonContinue = findViewById(R.id.button_continue);
        editTextName = findViewById(R.id.edit_text_name);
        editTextVoterId = findViewById(R.id.edit_text_voterId);
        editTextAadhar = findViewById(R.id.edit_text_aadhar);
        progressBar = findViewById(R.id.progressBar);
        container = findViewById(R.id.container);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUserToDatabase();
            }
        });
    }

    void addUserToDatabase() {
        progressBarVisibility(true);
        final String name = editTextName.getText().toString();
        String voterId = editTextVoterId.getText().toString();
        String aadhar = editTextAadhar.getText().toString();

        HashMap<String, String> userObject = new HashMap<>();
        userObject.put("name", name);
        userObject.put("voterId", voterId);
        userObject.put("aadhar", aadhar);

        FirebaseFirestore.getInstance().collection("users")
                .document(mUser.getUid())
                .set(userObject).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    startActivity(new Intent(UserDetailsActivity.this, VotingActivity.class));
                } else {
                    Log.e(TAG, "onComplete:" + task.getException());
                    progressBarVisibility(false);
                }
            }
        });
    }

    void progressBarVisibility(boolean visibility) {
        if (visibility) {
            // Show progress bar and hide container.
            progressBar.setVisibility(View.VISIBLE);
            container.setVisibility(View.INVISIBLE);
        } else {
            // Hide progress bar and show container.
            progressBar.setVisibility(View.INVISIBLE);
            container.setVisibility(View.VISIBLE);
        }
    }
}