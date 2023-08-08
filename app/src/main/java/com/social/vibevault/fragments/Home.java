package com.social.vibevault.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.social.vibevault.FragmentReplacerActivity;
import com.social.vibevault.MainActivity;
import com.social.vibevault.R;
import com.social.vibevault.adapter.HomeAdapter;
import com.social.vibevault.model.HomeModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends Fragment {
    HomeAdapter adapter;
    private RecyclerView recyclerView;
    private ImageButton profile,search,addBtn;
    private List<HomeModel> list;
    private FirebaseUser user;
    Activity activity;
    public Home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = getActivity();

        init(view);

        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        loadDataFromFirestore();

        adapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onLiked(int position, String id, String uid, List<String> likeList, boolean isChecked) {

                DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                        .document(uid)
                        .collection("Post Images")
                        .document(id);

                if (likeList.contains(user.getUid())) {
                    likeList.remove(user.getUid()); // unlike
                } else {
                    likeList.add(user.getUid()); // like
                }

                Map<String, Object> map = new HashMap<>();
                map.put("likes", likeList);

                reference.update(map);

            }
        });
    }
    private void init(View view) {

        profile = view.findViewById(R.id.profile);
        search = view.findViewById(R.id.search);
        addBtn = view.findViewById(R.id.add);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        clickListener();
    }

    public void clickListener(){
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).replaceFragment(new Profile());
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).replaceFragment(new Search());
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).replaceFragment(new Add());
            }
        });
    }
    private void loadDataFromFirestore() {
        final DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());

        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");

        reference.addSnapshotListener((value, error) -> {

            if (error != null) {
                Log.d("Error: ", error.getMessage());
                return;
            }

            if (value == null)
                return;

            List<String> uidList = (List<String>) value.get("following");

            if (uidList == null || uidList.isEmpty())
                return;

            collectionReference.whereIn("uid", uidList)
                    .addSnapshotListener((value1, error1) -> {

                        if (error1 != null) {
                            Log.d("Error: ", error1.getMessage());
                        }

                        if (value1 == null)
                            return;

                        list.clear();

                        for (QueryDocumentSnapshot snapshot : value1) {

                            snapshot.getReference().collection("Post Images")
                                    .addSnapshotListener((value11, error11) -> {

                                        if (error11 != null) {
                                            Log.d("Error: ", error11.getMessage());
                                        }

                                        if (value11 == null)
                                            return;

                                        for (final QueryDocumentSnapshot snapshot1 : value11) {

                                            if (!snapshot1.exists())
                                                return;

                                            HomeModel model = snapshot1.toObject(HomeModel.class);

                                            list.add(new HomeModel(
                                                    model.getName(),
                                                    model.getProfileImage(),
                                                    model.getImageUrl(),
                                                    model.getUid(),
                                                    model.getDescription(),
                                                    model.getId(),
                                                    model.getTimestamp(),
                                                    model.getLikes()));
                                        }
                                        adapter.notifyDataSetChanged();

                                    });
                        }
                    });

        });
    }
}