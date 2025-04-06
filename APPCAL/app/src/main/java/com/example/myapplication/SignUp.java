package com.example.myapplication;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.BaseProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUp extends AppCompatActivity {

    Button signUpBTN;
    TextInputEditText EditemailText, EditpasswordText,EditTextConfirmPasswordText;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView loginPageText;

    @Override
    public void onStart() {
        super.onStart();
        // Check if the user is signed in (non-null) and email is verified.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if (currentUser.isEmailVerified()) {
                // User is logged in and email is verified, proceed to HomeActivity.
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpBTN = findViewById(R.id.signUpButton);
        EditemailText = findViewById(R.id.EditTextEmailAddressSignUp);
        EditpasswordText = findViewById(R.id.EditTextPasswordSignUp);
        EditTextConfirmPasswordText = findViewById(R.id.EditTextConfirmPasswordSignUp);
        progressBar = findViewById(R.id.progressBar);
        loginPageText = findViewById(R.id.SignUpPageVerifyEmailText);
        mAuth = FirebaseAuth.getInstance();

        loginPageText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        signUpBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, confirmPassword;
                email = String.valueOf(EditemailText.getText());
                password = String.valueOf(EditpasswordText.getText());
                confirmPassword = String.valueOf(EditTextConfirmPasswordText.getText());

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                    Toast.makeText(SignUp.this, "Please enter both email, password, and confirm password.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUp.this, "Password and confirm password do not match.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                } else {
                    // Passwords match, create the user without signing in
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        sendEmailVerificationLink(user);
                                    } else {
                                        // If registration fails, display a message to the user.
                                        Toast.makeText(SignUp.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

    }

    private void sendEmailVerificationLink(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUp.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                            showSucessDialoug();
                            // After sending verification email, sign out the user
                            mAuth.signOut();
                            // Redirect the user to a page explaining email verification
                        } else {
                            Toast.makeText(SignUp.this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void showSucessDialoug(){
        ConstraintLayout sentedConstraintLayout = findViewById(R.id.popUpScreenConstraint);
        View view = LayoutInflater.from(SignUp.this).inflate(R.layout.pop_up_verification, sentedConstraintLayout);
        Button successDone = view.findViewById(R.id.successDone);
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        successDone.findViewById(R.id.successDone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                Intent intent = new Intent(SignUp.this,Login.class);
                startActivity(intent);
                finish();
            }
        });
        if (alertDialog.getWindow() != null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();


    }
}
