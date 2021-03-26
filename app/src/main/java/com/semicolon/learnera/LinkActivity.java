package com.semicolon.learnera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

public class LinkActivity extends AppCompatActivity {

    String linkTosite;
    WebView webView;
    MaterialToolbar materialToolbar;


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
        setContentView(R.layout.activity_link);
        webView=findViewById(R.id.webLinkViewer);
        materialToolbar=findViewById(R.id.materialToolbar);
        setSupportActionBar(materialToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if(getIntent().hasExtra("webLink"))
        {
            String userPost = getIntent().getStringExtra("webLink");
            assert userPost != null;
            linkTosite=userPost;
            webView.loadUrl(linkTosite);

        }

        else {
            Toast.makeText(LinkActivity.this,"error",Toast.LENGTH_LONG).show();

        }



    }
}