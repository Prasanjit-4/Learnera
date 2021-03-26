package com.semicolon.learnera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements RecyclerViewAdapter.OnPostClickListener {

    public static final String TAG= "TAG";
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private RecyclerView post_list_view;
    private List<UserPost>course_list;

    private BottomNavigationView bottomNavigationView_home;

    private FirebaseFirestore firebaseFirestore;
    private RecyclerViewAdapter recyclerViewAdapter;
    private DocumentSnapshot lastVisible;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        MaterialToolbar homeToolbar = findViewById(R.id.homeToolbar);
        mAuth=FirebaseAuth.getInstance();

        setSupportActionBar(homeToolbar);

        bottomNavigationView_home=findViewById(R.id.bottomNavigationView_home);
        post_list_view=findViewById(R.id.post_list_view);
        course_list=new ArrayList<>();
        firebaseFirestore=FirebaseFirestore.getInstance();
        recyclerViewAdapter=new RecyclerViewAdapter(course_list,this);
        post_list_view.setLayoutManager(new LinearLayoutManager(this));
        post_list_view.setAdapter(recyclerViewAdapter);

        post_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean bottom = !recyclerView.canScrollVertically(1);

                if(bottom){
                    loadQuery();
                }


            }
        });

        Query arrangeQuery= firebaseFirestore.collection("Posts").orderBy("postTimeStamp", Query.Direction.DESCENDING).limit(3);
        arrangeQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {


            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                   Log.d(TAG,"Error:"+e.getMessage());
                }
                else{

                    if(!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                UserPost userPost = doc.getDocument().toObject(UserPost.class);
                                course_list.add(userPost);

                                recyclerViewAdapter.notifyDataSetChanged();
                            }

                        }
                    }
                }
            }
        });





        //checks if user logged in or not
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);




        bottomNavigationView_home.setSelectedItemId(R.id.bottom_home);
        bottomNavigationView_home.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.bottom_home:
                        return true;
                    case R.id.bottom_acc:
                        sendToAccSetup();
                        return true;
                    case R.id.bottom_post:
                        sendToPost();
                        return true;

                }

                return false;
            }
        });



    }

    private void sendToPost() {
        Intent postIntent= new Intent(HomeActivity.this,PostCourse.class);
        startActivity(postIntent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //adds menu button and inflates menu layout to toolbar.
     getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logOutBtn:
                mAuth.signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful()){
                           sendToLogin();
                       }
                       else{
                           Toast.makeText(HomeActivity.this,"SignOut failed",Toast.LENGTH_LONG).show();
                       }
                    }
                });

                return true;


            default:
                return false;
        }
    }

    private void sendToAccSetup() {
        Intent acc_intent = new Intent(HomeActivity.this,ProfileSetup.class);
        startActivity(acc_intent);

    }

    private void sendToLogin() {

        Intent log_intent = new Intent(HomeActivity.this,LoginActivity.class);
        startActivity(log_intent);
        finish();

    }

    public void loadQuery(){

        Query nxtQuery= firebaseFirestore
                        .collection("Posts")
                        .orderBy("postTimeStamp", Query.Direction.DESCENDING)
                        .startAfter(lastVisible)
                        .limit(2);
        nxtQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {


            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e==null) {

                    if (!queryDocumentSnapshots.isEmpty()) {
                        if (e != null) {
                            Log.d(TAG, "Error:" + e.getMessage());
                        } else {

                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    UserPost userPost = doc.getDocument().toObject(UserPost.class);
                                    course_list.add(userPost);

                                    recyclerViewAdapter.notifyDataSetChanged();
                                }

                            }
                        }

                    }
                }
            }
        });
    }


    @Override
    public void onPostClick(int position) {
        Intent webIntent= new Intent(HomeActivity.this,LinkActivity.class);
        webIntent.putExtra("webLink",course_list.get(position).getCourseLink());
        startActivity(webIntent);

    }
}
