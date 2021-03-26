package com.semicolon.learnera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class Register extends AppCompatActivity {

    private EditText reg_emailField,reg_passwordField,confirm_passwordField;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        reg_emailField=findViewById(R.id.reg_emailField);
        reg_passwordField=findViewById(R.id.reg_passwordField);
        confirm_passwordField=findViewById(R.id.confirm_passwordField);
        MaterialButton signUpBtn = findViewById(R.id.signUpBtn);
        TextView backLogin = findViewById(R.id.backLogin);
        final ProgressBar reg_progressbar = findViewById(R.id.reg_progressbar);

        mAuth=FirebaseAuth.getInstance();

        backLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLogin();
            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reg_email= reg_emailField.getText().toString().trim();
                final String reg_password=reg_passwordField.getText().toString().trim();
                String confirmPassword= confirm_passwordField.getText().toString().trim();
                //checks if all fields empty
                if(TextUtils.isEmpty(reg_email) && TextUtils.isEmpty(reg_password) && TextUtils.isEmpty(confirmPassword)){
                    Toast.makeText(Register.this,"Please fill required fields",Toast.LENGTH_LONG).show();
                }
                //same as first if...
                else if(TextUtils.isEmpty(reg_email)){
                    Toast.makeText(Register.this,"Please enter Email ID",Toast.LENGTH_LONG).show();
                }
                //same as first if...
                else if(TextUtils.isEmpty(reg_password)){
                    Toast.makeText(Register.this,"Please enter password",Toast.LENGTH_LONG).show();
                }
                //same as first if...
                else if(TextUtils.isEmpty(confirmPassword)){
                    Toast.makeText(Register.this,"Please confirm password",Toast.LENGTH_LONG).show();
                }

                //checks password length
                else if(reg_password.length()<6){
                    Toast.makeText(Register.this,"Password too short",Toast.LENGTH_LONG).show();
                }

                // double checks parameters
                if(!TextUtils.isEmpty(reg_email) && !TextUtils.isEmpty(reg_password) && !TextUtils.isEmpty(confirmPassword) && reg_password.equals(confirmPassword))
                {
                    reg_progressbar.setVisibility(View.VISIBLE);

                    mAuth.createUserWithEmailAndPassword(reg_email, reg_password)
                            .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        reg_progressbar.setVisibility(View.GONE);
                                        // Sign in success, update UI with the signed-in user's information

                                        Toast.makeText(Register.this," Sign Up successful",Toast.LENGTH_LONG).show();
                                        sendToAccSetup();

                                    } else {
                                        reg_progressbar.setVisibility(View.GONE);
                                        // If sign in fails, display a message to the user.

                                        Toast.makeText(Register.this,"Authentication failed",Toast.LENGTH_LONG ).show();

                                    }

                                    // ...
                                }
                            });


                    }



            }
        });

    }
//method to send user to profile Activity
    private void sendToAccSetup() {
        Intent setup_intent = new Intent(Register.this,ProfileSetup.class);
        startActivity(setup_intent);
        finish();
    }
//method to send user to login pg
    private void sendToLogin() {
        Intent intent_login = new Intent(Register.this,LoginActivity.class);
        startActivity(intent_login);
        finish();
    }
}
