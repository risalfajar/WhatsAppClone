package com.example.whatsappclone;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainPageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Button mLogout = findViewById(R.id.btn_logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //logout user
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                //this flags will tell the activity to clear everything on top of it (another activity that still running)
                //so that user can only access the login activity
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}
