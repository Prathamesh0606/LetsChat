package com.phoenix.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.phoenix.letschat.Adapter.MessageAdapter;
import com.phoenix.letschat.Model.Chat;
import com.phoenix.letschat.Model.Users;
import com.phoenix.letschat.Notifications.Client;
import com.phoenix.letschat.Notifications.Data;
import com.phoenix.letschat.Notifications.MyResponse;
import com.phoenix.letschat.Notifications.Sender;
import com.phoenix.letschat.Notifications.Token;
import com.phoenix.letschat.fragments.APIService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    TextView username;
    ImageView imageView;


    EditText msg_editText;
    ImageButton sendBtn;
    MessageAdapter messageAdapter;
    List<Chat> mchat;
    String userid;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;

    FirebaseUser fuser;
    DatabaseReference reference;

    Intent intent;
    boolean notify = false;
    ValueEventListener seenListener;
    APIService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        imageView = findViewById(R.id.imageView_profile);
        username = findViewById(R.id.usernameye);

        sendBtn = findViewById(R.id.btn_send);
        msg_editText = findViewById(R.id.text_send);

        apiService = Client.getClient("https://fcm.googleeapis.com/").create(APIService.class);


        //Recyclerviewy
        recyclerView = findViewById(R.id.recyler_view);
        //recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("My Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                username.setText(user.getUsername());

                if(user.getImageURL().equals("default")) {
                    imageView.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(MessageActivity.this)
                            .load(user.getImageURL())
                            .into(imageView);
                }

                readMessages(fuser.getUid(),userid,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify = true;
                String msg = msg_editText.getText().toString();
                if(msg.equals("")) {
                    Toast.makeText(MessageActivity.this, "Please type something first", Toast.LENGTH_SHORT).show();
                }
                else {

                    sendMessage(fuser.getUid(),userid,msg);
                }

                msg_editText.setText("");

            }
        });
        SeenMessage(userid);

    }



    private void SeenMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    if (fuser.getUid().equals(Objects.requireNonNull(chat).getReceiver()) && userid.equals(chat.getSender())) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);

        reference.child("chats").push().setValue(hashMap);


        //Adding previous chats
        final DatabaseReference chatReference = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fuser.getUid())
                .child(userid);

        chatReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    chatReference.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final String msg = message;
        reference = FirebaseDatabase.getInstance().getReference("My Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                if(notify) {
                    sendNotification(receiver, user.getUsername(), msg);
                }
                notify = false;


            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(String receiver, final String username, final String msg) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(),R.mipmap.ic_launcher, username+": "+msg,"New message",userid);
                    Sender sender = new Sender(data,token.getToken());

                    apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                            if(response.code() == 200) {
                                if(response.body().success == 1) {

                                    Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void readMessages(final String myid, final String userid, final String imageurl) {
        mchat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);

                    if ((myid.equals(chat.getReceiver()) && userid.equals(chat.getSender()) )|| (userid.equals(chat.getReceiver()) && myid.equals(chat.getSender()))) {
                        mchat.add(chat);
                       // if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid))

                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this,mchat,imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkStatus(String status) {
        reference = FirebaseDatabase.getInstance().getReference("My Users").child(fuser.getUid());

        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStatus("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();

        reference.removeEventListener(seenListener);
        checkStatus("Offline");
    }



}
