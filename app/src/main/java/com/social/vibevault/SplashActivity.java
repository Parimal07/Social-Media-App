package com.social.vibevault;

import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);           //used for activities, sets the layout

        FirebaseAuth auth = FirebaseAuth.getInstance();     //class to check authentication
        FirebaseUser user = auth.getCurrentUser();          // gets authenticated user
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(user == null){
                    startActivity(new Intent(SplashActivity.this,FragmentReplacerActivity.class));
                }
                else{
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                }
                finish();
            }
        }, 2500);
    }
}