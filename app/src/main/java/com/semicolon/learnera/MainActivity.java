package com.semicolon.learnera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Handler handler;
    private FirebaseFirestore firebaseFirestore;
    private String loggedInUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth=FirebaseAuth.getInstance();
        handler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        firebaseFirestore=FirebaseFirestore.getInstance();

        //if user not logged in
        if(currentUser==null){
            splash();
        }
        // if user is logged in
       else{
            updateUI();
       }


    }
//contains handler method to control splash activity if user is signed in
    private void updateUI() {

        loggedInUserID=mAuth.getCurrentUser().getUid();

//check method for user profile completion
        firebaseFirestore.collection("Users").document(loggedInUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(!task.getResult().exists()){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent ProfileSetupIntent=new Intent(MainActivity.this,ProfileSetup.class);
                                startActivity(ProfileSetupIntent);
                                finish();

                            }
                        },3000);
                    }
                    else if(task.getResult().exists()){
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent_home=new Intent(MainActivity.this,HomeActivity.class);
                                startActivity(intent_home);
                                finish();

                            }
                        },3000);
                    }


                }
                else{
                    Toast.makeText(MainActivity.this,"Error",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    //contains handler method to control splash activity if user is !signed in
    private void splash() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent_login=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent_login);
                finish();
            }
        },3000);
    }




}
