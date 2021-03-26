package com.semicolon.learnera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class PostCourse extends AppCompatActivity {


    private MaterialToolbar postToolbar;
    private ImageView post_image;
    private EditText descriptionText,linkText;
    private MaterialButton postBtn;
    private BottomNavigationView bottomNavigationView_post;
    private ProgressBar progressBar;
    private ImageView rep_img_post;

    private Uri imgPostUri=null;
    private Uri downloadImgPostUri;
    private String sDownloadImgPostUri;
    private Bitmap compressedImageFile;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private String currUserID;

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
        setContentView(R.layout.activity_post_course);

        postToolbar=findViewById(R.id.postToolbar);
        setSupportActionBar(postToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);






        post_image=findViewById(R.id.post_image);
        rep_img_post=findViewById(R.id.rep_img_post);
        descriptionText=findViewById(R.id.descriptionText);
        linkText=findViewById(R.id.linkText);
        postBtn=findViewById(R.id.postBtn);
        bottomNavigationView_post=findViewById(R.id.bottomNavigationView_post);
        progressBar=findViewById(R.id.progressBar);

        storageReference= FirebaseStorage.getInstance().getReference();
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        currUserID=firebaseAuth.getCurrentUser().getUid();

        //navigation bar default selected item
        bottomNavigationView_post.setSelectedItemId(R.id.bottom_post);
        //on click listener for navigation bar menu
        bottomNavigationView_post.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.bottom_home:
                        sendToHome();
                        return true;
                    case R.id.bottom_acc:
                        sendToAccSetup();
                        return true;
                    case R.id.bottom_post:
                       return true;

                }

                return false;
            }
        });

//on click listener for thumb nail pic placeholder
        post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //opens image cropper
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512,512)
                        .setAspectRatio(1,1)
                        .start(PostCourse.this);

            }
        });

        //post button onclick listener
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String Cdescription=descriptionText.getText().toString();
                final String cLink=linkText.getText().toString();
                progressBar.setVisibility(View.VISIBLE);

                //check method for description ,link and thumbnail
                if(!TextUtils.isEmpty(Cdescription)&& !TextUtils.isEmpty(cLink) && imgPostUri!=null){


                    final String randomImgName= FieldValue.serverTimestamp().toString();//generates random unique image id for user post
                    final StorageReference postPath=storageReference.child("image_posts").child(randomImgName+".jpg");//reference for saving images to firebase
                    //method to first compress image and then upload
                  postPath.putFile(imgPostUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                      @Override
                      public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                          File newImageFile= new File(imgPostUri.getPath());
                          try {
                              compressedImageFile=new Compressor(PostCourse.this)
                                      .setMaxHeight(128)
                                      .setMaxWidth(128)
                                      .setQuality(10)
                                      .compressToBitmap(newImageFile);
                          } catch (IOException e) {
                              e.printStackTrace();
                          }

                          ByteArrayOutputStream baos = new ByteArrayOutputStream();
                          compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                          byte[]thumbNailData=baos.toByteArray();

                          final UploadTask uploadTask=storageReference.child("image_posts/thumbs")
                                  .child(randomImgName+".jpg").putBytes(thumbNailData);
                          uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                              @Override
                              public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Task<Uri> downloadThumbUri=taskSnapshot.getStorage().getDownloadUrl();
                                final String SdownloadThumbUri=downloadThumbUri.toString();


                                  postPath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                      @Override
                                      public void onSuccess(Uri uri) {
                                          //gets image post uri
                                          downloadImgPostUri=uri;
                                          sDownloadImgPostUri=downloadImgPostUri.toString();

                                          Map<String,Object> postMap=new HashMap<>();
                                          postMap.put("description",Cdescription);
                                          postMap.put("thumb_uri",SdownloadThumbUri);
                                          postMap.put("courseLink",cLink);
                                          postMap.put("userID",currUserID);
                                          postMap.put("postTimeStamp",FieldValue.serverTimestamp());
                                          if(sDownloadImgPostUri!=null){
                                              postMap.put("thumbPost",sDownloadImgPostUri);
                                          }

                                          firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                              @Override
                                              public void onComplete(@NonNull Task<DocumentReference> task) {
                                                  if(task.isSuccessful()){
                                                      Toast.makeText(PostCourse.this,"Post Complete",Toast.LENGTH_SHORT).show();
                                                      progressBar.setVisibility(View.GONE);
                                                      Intent homepgIntent= new Intent(PostCourse.this,HomeActivity.class);
                                                      startActivity(homepgIntent);
                                                  }
                                                  else{
                                                      Toast.makeText(PostCourse.this,"error",Toast.LENGTH_SHORT).show();
                                                      progressBar.setVisibility(View.GONE);
                                                  }
                                              }
                                          });
                                      }
                                  }).addOnFailureListener(new OnFailureListener() {
                                      @Override
                                      public void onFailure(@NonNull Exception e) {
                                          Toast.makeText(PostCourse.this,"error",Toast.LENGTH_SHORT).show();
                                      }
                                  });


                                /**  Map<String,Object> postMap=new HashMap<>();
                                  postMap.put("description",Cdescription);
                                  postMap.put("thumb_uri",SdownloadThumbUri);
                                  postMap.put("courseLink",cLink);
                                  postMap.put("userID",currUserID);
                                  postMap.put("postTimeStamp",randomImgName);
                                  if(sDownloadImgPostUri!=null){
                                      postMap.put("thumbPost",sDownloadImgPostUri);
                                  }

                                  firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                      @Override
                                      public void onComplete(@NonNull Task<DocumentReference> task) {
                                          if(task.isSuccessful()){
                                              Toast.makeText(PostCourse.this,"Post Complete",Toast.LENGTH_SHORT).show();
                                              progressBar.setVisibility(View.GONE);
                                              Intent homepgIntent= new Intent(PostCourse.this,HomeActivity.class);
                                              startActivity(homepgIntent);
                                          }
                                          else{
                                              Toast.makeText(PostCourse.this,"error",Toast.LENGTH_SHORT).show();
                                              progressBar.setVisibility(View.GONE);
                                          }
                                      }
                                  });**/

                              }
                          }).addOnFailureListener(new OnFailureListener() {
                              @Override
                              public void onFailure(@NonNull Exception e) {

                              }
                          });

                      }
                  }).addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          Toast.makeText(PostCourse.this,"error",Toast.LENGTH_SHORT).show();
                      }
                  });
                }
            }
        });



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                imgPostUri=result.getUri();
                post_image.setImageURI(imgPostUri);
                rep_img_post.setImageDrawable(null);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                //add toast if required
            }
        }
    }

    private void sendToHome() {
        Intent home_intent=new Intent(PostCourse.this,HomeActivity.class);
        startActivity(home_intent);
        finish();
    }
    private void sendToAccSetup() {
        Intent acc_intent = new Intent(PostCourse.this,ProfileSetup.class);
        startActivity(acc_intent);
        finish();
    }

}
