package com.social.vibevault.fragments;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.social.vibevault.FragmentReplacerActivity;
import com.social.vibevault.MainActivity;
import com.social.vibevault.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private EditText username,firstName,lastName,email,gender,dob,pass,confirmPass;
    private Button profile,register;
    AppCompatImageButton back;
    private TextView forgot_p;
    private FirebaseAuth auth;

    private ProgressBar progressBar;

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();
    }
    private void init(View view) {
        username=view.findViewById(R.id.username);
        firstName=view.findViewById(R.id.firstName);
        lastName=view.findViewById(R.id.lastName);
        email=view.findViewById(R.id.email);
        gender=view.findViewById(R.id.gender);
        dob=view.findViewById(R.id.dob);
        pass=view.findViewById(R.id.pass);
        confirmPass=view.findViewById(R.id.confirmPass);
        profile=view.findViewById(R.id.profile);
        register=view.findViewById(R.id.register);
        back=view.findViewById(R.id.back);
        auth=FirebaseAuth.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        forgot_p = view.findViewById(R.id.forgotTv);

    }

    private void clickListener(){

                back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FragmentReplacerActivity)getActivity()).setFragment(new LoginFragment());
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = username.getText().toString();
                String Email = email.getText().toString();
                String fName = firstName.getText().toString();
                String lName = lastName.getText().toString();
                String Gender = gender.getText().toString();
                String DOB = dob.getText().toString();
                String password = pass.getText().toString();
                String confPass = confirmPass.getText().toString();

                if(userName.isEmpty() || userName.equals(" ")){
                    username.setError("Please input username");
                    return;
                }
                if(fName.isEmpty() || fName.equals(" ")){
                    firstName.setError("Name field cannot be empty!");
                    return;
                }
                if(lName.isEmpty() || lName.equals(" ")){
                    lastName.setError("Name field cannot be empty!");
                    return;
                }
                if(Email.isEmpty() || !Email.matches(EMAIL_REGEX)){
                    email.setError("Email field cannot be empty!");
                    return;
                }
                if(Gender.isEmpty() || Gender.equals(" ")){
                    gender.setError("Gender field cannot be empty!");
                    return;
                }
                if(DOB.isEmpty() || DOB.equals(" ")){
                    dob.setError("DOB field cannot be empty!");
                    return;
                }
                if(password.isEmpty() || password.length() < 6 ){
                    pass.setError("Enter Password greater than 6 characters!");
                    return;
                }
                if(!password.equals(confPass) ){
                    confirmPass.setError("Password not Matched!!");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                createAccount(userName,fName,lName,Email,Gender,DOB,password);
            }
        });

    }

    private void createAccount(final String u,final String f,final String l,final String e,final String g,final String d,final String p){
        auth.createUserWithEmailAndPassword(e,p)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser user = auth.getCurrentUser();
                            String image = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRwp--EwtYaxkfsSPIpoSPucdbxAo6PancQX1gw6ETSKI6_pGNCZY4ts1N6BV5ZcN3wPbA&usqp=CAU";

                            UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
                            request.setDisplayName(u);
                            request.setPhotoUri(Uri.parse(image));

                            user.updateProfile(request.build());
                            user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(getContext(),"Verification link sent on Email",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                            uploadUser(user,u,f,l,e,g,d);
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                            String exception = task.getException().getMessage();
                            Toast.makeText(getContext(),"Error!" + exception,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadUser(FirebaseUser user,String u,String f,String l,String e,String g,String d){
        List<String> list = new ArrayList<>();
        List<String> list1 = new ArrayList<>();

        Map<String,Object> map = new HashMap<>();
        map.put("uname",u);
        map.put("fname",f);
        map.put("lname",l);
        map.put("email",e);
        map.put("gender",g);
        map.put("dob",d);
        map.put("followers", list);
        map.put("following", list1);
        map.put("profile"," ");
        map.put("uid",user.getUid());
        map.put("status"," ");
        map.put("search", f.toLowerCase());



        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@androidx.annotation.NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            progressBar.setVisibility(View.GONE);
                            assert getActivity() != null;
                           startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                           getActivity().finish();
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