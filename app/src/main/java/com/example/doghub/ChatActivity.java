package com.example.doghub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ChatActivity extends AppCompatActivity {

    DatabaseReference profileRef;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    RecyclerView recyclerView;
    EditText searchEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        searchEt = findViewById(R.id.search_userch);
        recyclerView = findViewById(R.id.rv_ch);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        profileRef = database.getReference("All Users");


        searchEt.addTextChangedListener((new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        }));


    }

    @Override
    protected void onStart() {
        super.onStart();
        String query = searchEt.getText().toString().toUpperCase();
        Query search = profileRef.orderByChild("name").startAt(query).endAt(query + "\uf0ff");

        FirebaseRecyclerOptions<All_User_Members> options1 =
                new FirebaseRecyclerOptions.Builder<All_User_Members
                        >()
                        .setQuery(search, All_User_Members.class)
                        .build();

        FirebaseRecyclerAdapter<All_User_Members, ProfileViewholder> firebaseRecyclerAdapter1 =
                new FirebaseRecyclerAdapter<All_User_Members, ProfileViewholder>(options1) {
                    @Override
                    protected void onBindViewHolder(@NonNull ProfileViewholder holder, int position, @NonNull All_User_Members model) {


                        final String postkey = getRef(position).getKey();

                        holder.setProfileInChat(getApplication(), model.getName(), model.getUid(), model.getUrl());


                        String name = getItem(position).getName();
                        String url = getItem(position).getUrl();
                        String uid = getItem(position).getUid();


                        holder.sendMessagebtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                // if (currentUserId.equals(uid)) {
                                //   Intent intent = new Intent(getActivity(),MainActivity.class);
                                // startActivity(intent);

                                //}else {
                                Intent intent = new Intent(ChatActivity.this, MessageActivity.class);
                                intent.putExtra("n", name);
                                intent.putExtra("u", url);
                                intent.putExtra("uid", uid);
                                startActivity(intent);
                                // }


                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ProfileViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.chat_profile_item, parent, false);

                        return new ProfileViewholder(view);
                    }
                };


        firebaseRecyclerAdapter1.startListening();
//

        recyclerView.setAdapter(firebaseRecyclerAdapter1);


    }

}