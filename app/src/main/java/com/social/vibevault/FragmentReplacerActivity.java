package com.social.vibevault;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.FrameLayout;
import com.social.vibevault.fragments.LoginFragment;
import com.social.vibevault.fragments.RegisterFragment;

public class FragmentReplacerActivity extends AppCompatActivity {
    private FrameLayout frameLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_replacer);

        frameLayout=findViewById(R.id.frameLayout);
        setFragment(new LoginFragment());
    }

    //user defined function
    public void setFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        if(fragment instanceof RegisterFragment) {
            fragmentTransaction.addToBackStack(null);     // if user presses back button then
        }                                                       // the registration will be cancelled
        fragmentTransaction.replace(frameLayout.getId(),fragment);
        fragmentTransaction.detach(fragment).attach(fragment);    //detach and then attach to update the fragment view
        fragmentTransaction.commit();
    }
}