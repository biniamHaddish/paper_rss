package com.biniam.rss.ui.base;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.biniam.rss.BuildConfig;
import com.biniam.rss.R;

public class BetaExpiredActivity extends AppCompatActivity {

    private Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beta_expired);

        updateButton = findViewById(R.id.updateButton);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri marketUri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(marketUri);
                startActivity(intent);
            }
        });
    }
}
