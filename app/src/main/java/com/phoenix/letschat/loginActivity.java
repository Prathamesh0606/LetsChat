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

public class loginActivity extends AppCompatActivity {
    EditText emailETlg,passETlg;
    Button loginButton,registerButton;

    FirebaseAuth auth;
    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //checking for user existance and saving current user
        if(firebaseUser != null) {
            Intent i = new Intent(loginActivity.this,MainActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailETlg = (EditText) findViewById(R.id.emailtext1);
        passETlg = (EditText)  findViewById(R.id.passText);
        loginButton = findViewById(R.id.loginbutton);
        registerButton = findViewById(R.id.registerbutton);

        //Firebase auth

        auth = FirebaseAuth.getInstance();


        //register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(loginActivity.this,registrationActivity.class);
                startActivity(i);
            }
        });



        //Login button:
         loginButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 String email_text = String.valueOf(emailETlg.getText());
                 String password_text = passETlg.getText().toString();

                 //checking if they are empty
                 if(TextUtils.isEmpty(email_text) || TextUtils.isEmpty(password_text)) {
                     Toast.makeText(loginActivity.this,"Please fill in all fields",Toast.LENGTH_SHORT).show();
                 }
                 else {
                     auth.signInWithEmailAndPassword(email_text,password_text)
                             .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                 @Override
                                 public void onComplete(@NonNull Task<AuthResult> task) {
                                     if(task.isSuccessful()) {
                                         Intent intent = new Intent(loginActivity.this,MainActivity.class);
                                         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                         startActivity(intent);
                                         finish();
                                     }
                                     else {
                                         Toast.makeText(loginActivity.this,"Login failed",Toast.LENGTH_SHORT).show();
                                     }
                                 }
                             });
                 }
             }
         });

    }
}
