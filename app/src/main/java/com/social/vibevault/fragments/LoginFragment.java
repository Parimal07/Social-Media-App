package com.social.vibevault.fragments;

import static com.social.vibevault.fragments.RegisterFragment.EMAIL_REGEX;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.social.vibevault.FragmentReplacerActivity;
import com.social.vibevault.MainActivity;
import com.social.vibevault.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginFragment extends Fragment {

    private EditText mail,password;
    private Button login,register,google;
    private TextView forgotPasswordTv;
    private ProgressBar progressBar;
    private static final int RC_SIGN_IN = 1;
    GoogleSignInClient mGoogleSignInClient;

    private FirebaseAuth auth;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);   //used for fragments returns a view
    }                                                                                  //object with hierarchy

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

    }
    private void init(View view){
        mail=view.findViewById(R.id.email);
        password=view.findViewById(R.id.password);
        login=view.findViewById(R.id.login);
        forgotPasswordTv=view.findViewById(R.id.forgotTv);
        register=view.findViewById(R.id.register1);
        progressBar=view.findViewById(R.id.progressBar1);
        google=view.findViewById(R.id.google);

        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        clickListener();

    }

    private void clickListener() {

        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new ForgotPasswordFragment());
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mail.getText().toString();
                String pass = password.getText().toString();

                if (email.isEmpty() || !email.matches(EMAIL_REGEX)) {
                    mail.setError("Invalid email!");
                    return;
                }
                if (pass.isEmpty() || pass.length() < 6) {
                    password.setError("Password should be more than 6 characters");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                auth.signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();

                                    if (!user.isEmailVerified()) {
                                        Toast.makeText(getContext(), "Please verify Email first!", Toast.LENGTH_SHORT).show();
                                    }

                                    sendUserToMainActivity();
                                } else {
                                    String exception = "Error: " + Objects.requireNonNull(task.getException()).getMessage();
                                    Toast.makeText(getContext(), exception, Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);

                                }
                            }
                        });
            }
        });
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FragmentReplacerActivity) getActivity()).setFragment(new RegisterFragment());
            }
        });
    }
        private void sendUserToMainActivity(){
            if(getActivity() == null)
                return;
            progressBar.setVisibility(View.VISIBLE);
            startActivity(new Intent(getContext().getApplicationContext(), MainActivity.class));
            getActivity().finish();
        }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);       //send result back to the calling activity with a status code and some data.
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
                assert account != null;
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();
                            updateUi(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                        }

                    }
                });
    }

    private void updateUi(FirebaseUser user) {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());

        Map<String,Object> map = new HashMap<>();
        //fetched from firebase
        map.put("uname", user.getUid());
        map.put("uid",user.getUid());
        assert account != null;
        //fetched from google
        map.put("fname",account.getDisplayName());
        map.put("email", account.getEmail());
        map.put("profile",String.valueOf(account.getPhotoUrl()));
        //additional attributed
        map.put("following", 0);
        map.put("followers", 0);
        map.put("status", " ");


        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {     // used to check if writing to firebase is done.
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressBar.setVisibility(View.GONE);
                            assert getActivity() != null;
                            sendUserToMainActivity();
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            String exception = task.getException().getMessage();
                            Toast.makeText(getContext(),"Error!" + exception,Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }
}
