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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField,passwordField;
    private ProgressBar login_progressbar;

    private GoogleSignInClient mGoogleSignInClient;
    private  int RC_SIGN_IN = 4;
    private FirebaseAuth firebaseAuth;



    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
// the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account!=null){
            sendToHome();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField=findViewById(R.id.emailField);
        passwordField=findViewById(R.id.passwordField);
        MaterialButton loginBtn = findViewById(R.id.loginBtn);
        MaterialButton googleBtn = findViewById(R.id.googleBtn);
        TextView createAcc = findViewById(R.id.createAcc);
        login_progressbar=findViewById(R.id.login_progressbar);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        createAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegister();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {



                String email= emailField.getText().toString().trim();
                String password = passwordField.getText().toString().trim();
                //checks if both email and password are empty
                if(TextUtils.isEmpty(email) && TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this,"Please fill required fields",Toast.LENGTH_LONG).show();
                }
                //checks if only email is empty
                else if(TextUtils.isEmpty(email)){
                    Toast.makeText(LoginActivity.this,"Please enter Email ID",Toast.LENGTH_LONG).show();
                }
                //checks if only password is empty
                 else if(TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this,"Please enter password",Toast.LENGTH_LONG).show();
                }

                //checks if all parameters are met
               else if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && password.length()>=6){
                    login_progressbar.setVisibility(View.VISIBLE);
                    //method to sign in user
                 firebaseAuth.signInWithEmailAndPassword(email,password)
                         .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                             @Override
                             public void onComplete(@NonNull Task<AuthResult> task) {
                                 if(task.isSuccessful()){
                                     Toast.makeText(LoginActivity.this,"Login successful",Toast.LENGTH_LONG).show();
                                     sendToHome();

                                     login_progressbar.setVisibility(View.GONE);
                                 }

                                 else{
                                     login_progressbar.setVisibility(View.GONE);
                                     Toast.makeText(LoginActivity.this,"Login Failed",Toast.LENGTH_LONG).show();
                                 }


                             }
                         });

               }




            }
        });


        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_progressbar.setVisibility(View.VISIBLE);
                signIn();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if(account!=null){
                    firebaseAuthWithGoogle(account);
                
                }

            }
            catch (ApiException e) {
                // Google Sign In failed, update UI appropriately

                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {


        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            sendToSetup();
                            FirebaseUser user = firebaseAuth.getCurrentUser();


                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this,"Sign in failed",Toast.LENGTH_LONG).show();

                        }

                      login_progressbar.setVisibility(View.GONE);  // ...
                    }
                });
    }

    private void sendToSetup() {
        Intent setupIntent= new Intent(LoginActivity.this,ProfileSetup.class);
        startActivity(setupIntent);
        finish();
    }

    private void signIn() {


        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void sendToHome() {
        Intent intent_home= new Intent(LoginActivity.this,HomeActivity.class);
        startActivity(intent_home);
        finish();
    }

    private void sendToRegister() {
        Intent intent_register= new Intent(LoginActivity.this,Register.class);
        startActivity(intent_register);
    }


}
