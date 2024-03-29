package com.example.yousef.rbenoapplication;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ConnectionActivity extends AppCompatActivity {

    public static final int CONNECTION_RESULT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

//    final ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//    final int destination = getIntent().getIntExtra("destination",0);

        findViewById(R.id.retryBtn).setOnClickListener(v -> {


            if (WifiUtil.isConnectedToInternet(this)) {

                setResult(CONNECTION_RESULT);
                finish();

            } else {

                Toast.makeText(ConnectionActivity.this,
                        "الرجاء التحقق من الاتصال بالانترنت!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

}
