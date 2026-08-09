package com.hitstudio.apps.sharednotebook.ui.screens;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.hitstudio.apps.sharednotebook.R;

public final class AboutMeActivity extends AppCompatActivity {
    private static final String CONTACT_EMAIL = "neelsharma2004@gmail.com";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_me);

        findViewById(R.id.back_button).setOnClickListener(view -> finish());

        TextView versionValue = findViewById(R.id.version_value);
        versionValue.setText(getString(R.string.version_value, getAppVersion()));

        findViewById(R.id.contact_button).setOnClickListener(view -> openEmailComposer());
    }

    private String getAppVersion() {
        try {
            PackageInfo packageInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageInfo = getPackageManager().getPackageInfo(
                        getPackageName(),
                        PackageManager.PackageInfoFlags.of(0)
                );
            } else {
                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            }
            return packageInfo.versionName == null ? "—" : packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException exception) {
            return "—";
        }
    }

    private void openEmailComposer() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + CONTACT_EMAIL));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.contact_email_subject));

        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        } else {
            Snackbar.make(
                    findViewById(R.id.contact_button),
                    R.string.no_email_app,
                    Snackbar.LENGTH_SHORT
            ).show();
        }
    }
}
