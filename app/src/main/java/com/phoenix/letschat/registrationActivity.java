package com.phoenix.letschat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class registrationActivity extends AppCompatActivity {

    EditText emailET,userIDET,passET;
    Button registerButton;
    FirebaseAuth auth1;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        userIDET = findViewById(R.id.userIDText);
        emailET = findViewById(R.id.emailText);
        passET = findViewById(R.id.passwordText);
        registerButton = findViewById(R.id.registerbutton);


        auth1 = FirebaseAuth.getInstance();

        //adding event listener to registration button

        registerButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String username_text = userIDET.getText().toString();
                String email_text = emailET.getText().toString();
                String pass_text = passET.getText().toString();

                if(TextUtils.isEmpty(username_text) || TextUtils.isEmpty(email_text) || TextUtils.isEmpty(pass_text)) {
                    Toast.makeText(registrationActivity.this,"Please fill in all fields",Toast.LENGTH_SHORT).show();
                } else {
                    registerNow(username_text,email_text,pass_text);
                }
            }
        });

    }

    private void registerNow(final String username,String email,String password) {
        auth1.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth1.getCurrentUser();
                            String userID = firebaseUser.getUid();

                            myRef = FirebaseDatabase.getInstance()
                                    .getReference("My Users")
                                    .child(userID);

                            //Hashmaps

                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userID);
                            hashMap.put("username", username);
                            hashMap.put("imageURL", "default");
                            hashMap.put("status","Offline");

                            //Opening mainactivity after successful registration

                            myRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(registrationActivity.this, MainActivity.class);
                                        Toast.makeText(registrationActivity.this, "Registered successfully", Toast.LENGTH_SHORT).show();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }


                                }
                            });
                        } else {
                            Toast.makeText(registrationActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }
}
