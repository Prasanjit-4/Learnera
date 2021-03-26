package com.semicolon.learnera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileSetup extends AppCompatActivity {

    private CircleImageView userImg;
    private EditText usernameField;
    private MaterialButton setupAccBtn;
    private MaterialToolbar toolbar;
    private ProgressBar setup_progressBar;
    private BottomNavigationView bottomNavigationView_profile;
    private ImageView rep_img;



    private Uri UimageUri=null;
    private Uri download_Uri;
    private String sDownloadUri;
    private String UserID;
    private Boolean isChanged=false;


    private FirebaseAuth pAuth;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            Intent intent= new Intent(this,HomeActivity.class );
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);
        setupAccBtn=findViewById(R.id.setupAccBtn);
        toolbar=findViewById(R.id.toolbar);
        userImg=findViewById(R.id.userImg);
        rep_img=findViewById(R.id.rep_img);
        usernameField=findViewById(R.id.usernameField);
        setup_progressBar=findViewById(R.id.setup_progressBar);
        bottomNavigationView_profile=findViewById(R.id.bottomNavigationView_profile);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);



        pAuth=FirebaseAuth.getInstance();
        UserID= Objects.requireNonNull(pAuth.getCurrentUser()).getUid();
        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();


        bottomNavigationView_profile.setSelectedItemId(R.id.bottom_acc);
        bottomNavigationView_profile.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.bottom_home:
                        sendToHome();
                        return true;
                    case R.id.bottom_acc:
                        return true;
                    case R.id.bottom_post:
                        sendToPost();
                        return true;

                }

                return false;
            }
        });


        setupAccBtn.setEnabled(false);

        //retrieve data
        setup_progressBar.setVisibility(View.VISIBLE);
        firebaseFirestore.collection("Users").document(UserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                if(task.isSuccessful()){
                    if(Objects.requireNonNull(task.getResult()).exists()){
                        setup_progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileSetup.this,"data exists",Toast.LENGTH_LONG).show();
                        String Uname = task.getResult().getString("name");//gets the user name that was uploaded
                        String UProfileImage= task.getResult().getString("image");//gets sDownloadUri val which was uploaded
                        usernameField.setText(Uname);
                        rep_img.setImageDrawable(null);
                        //prevents nullptr exception if user did not upolad image during image setup
                        if(UProfileImage!=null) {
                            UimageUri = Uri.parse(UProfileImage);

                            Glide.with(ProfileSetup.this).load(UProfileImage).placeholder(R.drawable.user_image_place).into(userImg);
                        }

                    }

                    else{
                        setup_progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileSetup.this,"!data exists",Toast.LENGTH_LONG).show();
                    }

                }

                else{
                    Toast.makeText(ProfileSetup.this,"error",Toast.LENGTH_LONG).show();
                }

                setupAccBtn.setEnabled(true);
            }
        });

        setupAccBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setup_progressBar.setVisibility(View.VISIBLE);
                final String username= usernameField.getText().toString().trim();
                if(isChanged) {
                    if (!TextUtils.isEmpty(username) && UimageUri != null) {
                        UserID = pAuth.getCurrentUser().getUid();//gets the logged in user and the user's id.
                        //creates image storage folder in firebase storage
                        final StorageReference imgPath = storageReference.child("Profile Image").child(UserID + ".jpg");
                        //puts the given user profile image in the folder created
                        imgPath.putFile(UimageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Get a URL to the uploaded content
                                imgPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        setup_progressBar.setVisibility(View.GONE);
                                        download_Uri = uri;
                                        sDownloadUri = download_Uri.toString();//convert to string first or its fine if done explicitly
                                        HashMap<String, String> user_map = new HashMap<>();
                                        user_map.put("name", username);//creates sub folder in users folder for username
                                        if (sDownloadUri != null) {
                                            //prevent nullpointer exception
                                            user_map.put("image", sDownloadUri);//same as username folder.
                                        }
                                        firebaseFirestore.collection("Users").document(UserID).set(user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    //for test adding toast
                                                    Toast.makeText(ProfileSetup.this, "User settings Updated", Toast.LENGTH_LONG).show();
                                                    Intent HomeToSetup=new Intent(ProfileSetup.this,HomeActivity.class);
                                                    startActivity(HomeToSetup);

                                                } else {
                                                    Toast.makeText(ProfileSetup.this, "Firestore error", Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        });
                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                        // ...
                                        setup_progressBar.setVisibility(View.GONE);
                                        Toast.makeText(ProfileSetup.this, "error", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                // ...
                                setup_progressBar.setVisibility(View.GONE);
                                Toast.makeText(ProfileSetup.this, "Could not upload profile image", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    //checks if username is filled and image is not added then only uploads username
                    else if (!TextUtils.isEmpty(username) && UimageUri == null) {
                        UserID = pAuth.getCurrentUser().getUid();//gets the logged in user and the user's id.

                        HashMap<String, String> user_map = new HashMap<>();
                        user_map.put("name", username);//creates sub folder in users folder for username

                        firebaseFirestore.collection("Users").document(UserID).set(user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    //for test adding toast
                                    setup_progressBar.setVisibility(View.GONE);
                                    Toast.makeText(ProfileSetup.this, "User settings Updated", Toast.LENGTH_LONG).show();

                                } else {
                                    setup_progressBar.setVisibility(View.GONE);
                                    Toast.makeText(ProfileSetup.this, "Firestore error", Toast.LENGTH_LONG).show();
                                }

                            }
                        });

                    }
                    else if (TextUtils.isEmpty(username)) {
                        setup_progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileSetup.this, "Please enter username", Toast.LENGTH_LONG).show();
                    }
                }

                else{
                    download_Uri=UimageUri;

                    UserID = pAuth.getCurrentUser().getUid();//gets the logged in user and the user's id.

                    HashMap<String, String> user_map = new HashMap<>();
                    sDownloadUri = download_Uri.toString();//convert to string first or its fine if done explicitly
                    user_map.put("name", username);//creates sub folder in users folder for username
                    if (sDownloadUri != null) {
                        //prevent nullpointer exception
                        user_map.put("image", sDownloadUri);//same as username folder.
                    }

                    firebaseFirestore.collection("Users").document(UserID).set(user_map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //for test adding toast
                                setup_progressBar.setVisibility(View.GONE);
                                Toast.makeText(ProfileSetup.this, "User settings Updated 1", Toast.LENGTH_LONG).show();

                            } else {
                                setup_progressBar.setVisibility(View.GONE);
                                Toast.makeText(ProfileSetup.this, "Firestore error", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }

            }
        });

        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(ProfileSetup.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){

                        ActivityCompat.requestPermissions(ProfileSetup.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else{
                        Toast.makeText(ProfileSetup.this,"Access Granted",Toast.LENGTH_LONG).show();
                        //if access is allowed then shows the cropping image activity

                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1,1)
                                .start(ProfileSetup.this);
                    }
                }
            }
        });




    }

    private void sendToPost() {
        Intent postIntent=new Intent(ProfileSetup.this,PostCourse.class);
        startActivity(postIntent);
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                UimageUri = result.getUri();
                userImg.setImageURI(UimageUri);//get and set image uri
                rep_img.setImageDrawable(null);
                isChanged=true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();

                //add toast if required
            }
        }
    }

    private void sendToHome() {
        Intent home_intent=new Intent(ProfileSetup.this,HomeActivity.class);
        startActivity(home_intent);
        finish();
    }
}
