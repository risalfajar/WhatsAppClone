package com.example.whatsappclone;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private EditText mPhoneNumber, mCode;
    private Button mSend;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);

        //check if user is already logged in
        userIsLoggedIn();

        mPhoneNumber = findViewById(R.id.et_phonenumber);
        mCode = findViewById(R.id.et_code);
        mSend = findViewById(R.id.btn_verify);

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mVerificationId != null)
                    verifyPhoneNumberWithCode();
                else
                    startPhoneNumberVerification();
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) { //if user input is wrong, message is not sent. or something bad happens

            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) { //this is called whenever a code is sent to the user
                super.onCodeSent(verificationId, forceResendingToken);

                mVerificationId = verificationId;
                mSend.setText("Verify Code");
            }
        };
    }

    private void verifyPhoneNumberWithCode(){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, mCode.getText().toString().trim());
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) { //task tells us if sign in is successful or not
                if(task.isSuccessful())
                    userIsLoggedIn();
                else
                    Toast.makeText(getApplicationContext(), "Task is not successfull", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void userIsLoggedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){ //just a double check if user is really logged in
            startActivity(new Intent(getApplicationContext(), MainPageActivity.class));
            finish();
            return;
        }
    }

    private void startPhoneNumberVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mPhoneNumber.getText().toString().trim(),
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }
}
