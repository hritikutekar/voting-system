package com.hritik.utekar.votingsystem;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class VotingActivity extends AppCompatActivity {
    public static final ArrayList<PartyModel> vote = new ArrayList<>();
    private static final String TAG = "VotingActivity";
    private Button buttonVote;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView textViewTitle;
    private ConstraintLayout container;

    private FirebaseUser mUser;
    private UserModel mUserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = findViewById(R.id.recycler_view);
        buttonVote = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);
        textViewTitle = findViewById(R.id.text_view_title);
        container = findViewById(R.id.container);

        final GroupAdapter adapter = new GroupAdapter();
        recyclerView.setAdapter(adapter);

        updateUI();

        FirebaseFirestore.getInstance().collection("voting").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                PartyModel partyModel = document.toObject(PartyModel.class);

                                adapter.add(new PartyCard(partyModel));
                            }
                        } else {
                            Log.d(TAG, "onComplete:" + task.getException());
                        }
                    }
                });

        buttonVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeVote();
            }
        });
    }

    private void updateUI() {
        progressBarVisibility(true);

        FirebaseFirestore.getInstance().collection("users").document(mUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                mUserModel = documentSnapshot.toObject(UserModel.class);

                                if (mUserModel != null && mUserModel.isVote()) {
                                    buttonVote.setText(R.string.alread_voted);
                                    buttonVote.setEnabled(false);
                                }

                                progressBarVisibility(false);
                            }
                        } else {
                            Log.d(TAG, "onComplete:", task.getException());
                            progressBarVisibility(false);
                        }
                    }
                });
    }

    void updateButton(PartyModel partyModel) {
        if (buttonVote.isEnabled()) {
            buttonVote.setText(String.format(getResources().getString(R.string.vote_to), partyModel.getParty()));
        }
    }

    void makeVote() {
        if (!vote.isEmpty()) {
            final PartyModel partyModel = vote.get(0);

            new AlertDialog.Builder(this)
                    .setTitle("Vote")
                    .setMessage("Are you sure you want to vote " + partyModel.getParty() + "?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with vote operation
                            DocumentReference userRef = FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(mUser.getUid());
                            HashMap<String, DocumentReference> userDetails = new HashMap<>();
                            userDetails.put("user", userRef);

                            // Add user to party vote collection.
                            FirebaseFirestore.getInstance().collection("voting")
                                    .document(partyModel.getKey())
                                    .collection("votes")
                                    .document(mUser.getUid())
                                    .set(userDetails);

                            // Set the vote property of user to be true.
                            userRef.update("vote", true);

                            updateUI();
                        }
                    })

                    .setNegativeButton(android.R.string.no, null)
                    .show();
        } else {
            Snackbar.make(container, "Please tap on party you want to vote", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    void progressBarVisibility(boolean visibility) {
        if (visibility) {
            // Show progress bar and hide other layouts.
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
            buttonVote.setVisibility(View.INVISIBLE);
            textViewTitle.setVisibility(View.INVISIBLE);
        } else {
            // Hide progress bar and show other layouts.
            progressBar.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            buttonVote.setVisibility(View.VISIBLE);
            textViewTitle.setVisibility(View.VISIBLE);
        }
    }

    class PartyCard extends Item<GroupieViewHolder> {
        PartyModel partyModel;

        PartyCard(PartyModel partyModel) {
            this.partyModel = partyModel;
        }

        @Override
        public void bind(@NonNull final GroupieViewHolder viewHolder, int position) {
            TextView textViewParty = viewHolder.itemView.findViewById(R.id.text_view_party);
            TextView textViewLeader = viewHolder.itemView.findViewById(R.id.text_view_leader);
            ImageView imageViewLogo = viewHolder.itemView.findViewById(R.id.image_view_logo);
            final ConstraintLayout cardContainer = viewHolder.itemView.findViewById(R.id.card_container);

            textViewParty.setText(partyModel.getParty());
            textViewLeader.setText(partyModel.getLeader());
            Picasso.get().load(partyModel.getLogo()).into(imageViewLogo);

            cardContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    vote.clear();
                    vote.add(partyModel);
                    updateButton(partyModel);
                }
            });
        }

        @Override
        public int getLayout() {
            return R.layout.party_card_view;
        }
    }
}