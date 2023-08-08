package com.social.vibevault;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.social.vibevault.FragmentReplacerActivity;
import com.social.vibevault.adapter.ViewPagerAdapter;
import com.social.vibevault.fragments.Add;
import com.social.vibevault.fragments.EditProfileFragment;
import com.social.vibevault.fragments.ForgotPasswordFragment;
import com.social.vibevault.fragments.Home;
import com.social.vibevault.fragments.Profile;
import com.social.vibevault.fragments.RegisterFragment;
import com.social.vibevault.fragments.Search;
import com.social.vibevault.model.Users;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements Search.OnDataPass {
    public static String USER_ID;
    public static boolean IS_SEARCHED_USER = false;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private ImageButton profile,search,addBtn;
    private Fragment add_fragment,search_fragment,profile_fragment,home;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getSupportActionBar().hide();
        init();
        clickListener();
    }
    private void hideToolbarButtons() {
        search.setVisibility(View.GONE);
        profile.setVisibility(View.GONE);
        addBtn.setVisibility(View.GONE);
    }
//    private void showToolbarButtons() {
//        profile.setVisibility(View.VISIBLE);
//        search.setVisibility(View.VISIBLE);
//        addBtn.setVisibility(View.VISIBLE);
//    }
    private void init() {
        profile = findViewById(R.id.profile);
        search = findViewById(R.id.search);
        addBtn = findViewById(R.id.add);
        add_fragment = new Add();
        search_fragment=new Search();
        profile_fragment=new Profile();
        home = new Home();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, home)
                .commit();
    }

    private void clickListener() {
        final MainActivity activity = this;

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(profile_fragment);
                hideToolbarButtons();
            }
        });


        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(search_fragment);
                hideToolbarButtons();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(add_fragment);
                hideToolbarButtons();
            }
        });
    }


    private Bitmap loadProfileImage(String directory) {

        try {
            File file = new File(directory, "profile.png");

            return BitmapFactory.decodeStream(new FileInputStream(file));     //Bitmap object from the image file.
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Function to load profile fragment of searched user.
    @Override
    public void onChange(String uid) {
        USER_ID = uid;
        IS_SEARCHED_USER = true;
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(USER_ID);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Users user = documentSnapshot.toObject(Users.class);
                    replaceFragment(profile_fragment);
                }
            }
        });

    }
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (currentFragment instanceof Profile) {
            replaceFragment(home);
            IS_SEARCHED_USER = false;
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        updateStatus(true);
    }

    @Override
    protected void onPause() {
        updateStatus(false);
        super.onPause();
    }

    void updateStatus(boolean status) {              // function for user status

        Map<String, Object> map = new HashMap<>();
        map.put("online", status);

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(user.getUid())
                .update(map);
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}


