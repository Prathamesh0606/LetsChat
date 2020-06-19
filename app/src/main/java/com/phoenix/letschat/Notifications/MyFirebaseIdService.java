package com.phoenix.letschat.Notifications;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseIdService extends FirebaseMessagingService {

    public void FirebaseInstanceId() {

    }


    @Override
    public void onNewToken(@NonNull String s) {
      super.onNewToken(s);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String refreshtoken = FirebaseInstanceId.getInstance().getToken();

        if(firebaseUser!=null) {
            updateToken(refreshtoken);
        }
    }

    private void updateToken(String refreshtoken) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(refreshtoken);
        reference.child(firebaseUser.getUid()).setValue(token);
    }
}
