package com.me.mymovies.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.me.mymovies.R;
import com.me.mymovies.data.User;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    private DatabaseReference users;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewAlreadyHaveAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");
        editTextEmail = findViewById(R.id.editTextEmailSignUp);
        editTextPassword = findViewById(R.id.editTextPasswordSignUp);
        textViewAlreadyHaveAccount = findViewById(R.id.textViewAlreadyHaveAccount);
        textViewAlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    public void onClickSignUp(View view) {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    User user = new User();
                    user.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    user.setEmail(email);
                    user.setPassword(password);
                    users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    System.out.println(task.getException());
                    Toast.makeText(SignUpActivity.this, "Ошибка: " + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}