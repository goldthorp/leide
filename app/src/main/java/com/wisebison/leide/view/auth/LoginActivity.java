package com.wisebison.leide.view.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.wisebison.leide.R;

public class LoginActivity extends AppCompatActivity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
  }

  public void onClickSignIn(final View view) {
    final EditText emailEditText = findViewById(R.id.email_edit_text);
    final EditText passwordEditText = findViewById(R.id.password_edit_text);
    final String email = emailEditText.getText().toString();
    final String password = passwordEditText.getText().toString();
    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            setResult(RESULT_OK);
            finish();
          } else {
            Log.d("LOGIN", "fail", task.getException());
          }
        });
  }
}
