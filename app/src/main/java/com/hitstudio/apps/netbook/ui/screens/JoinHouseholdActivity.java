package com.hitstudio.apps.netbook.ui.screens;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hitstudio.apps.netbook.R;
import com.hitstudio.apps.netbook.data.remote.DiscoveryManager;
import com.hitstudio.apps.netbook.data.remote.RegistrationManager;
import com.hitstudio.apps.netbook.domain.model.HouseholdService;
import com.hitstudio.apps.netbook.ui.adapter.HouseholdAdapter;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class JoinHouseholdActivity extends AppCompatActivity {
    @Inject
    DiscoveryManager discoveryManager;
    @Inject
    RegistrationManager registrationManager;

    private HouseholdAdapter adapter;
    private View joinButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_household);

        findViewById(R.id.back_button).setOnClickListener(view -> finish());

        RecyclerView recyclerView = findViewById(R.id.services_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HouseholdAdapter(service -> joinButton.setEnabled(true));
        recyclerView.setAdapter(adapter);

        joinButton = findViewById(R.id.join_household_button);
        joinButton.setOnClickListener(view -> showRegistrationDialog());

        discoveryManager.getDiscoveredServices().observe(this, services -> {
            adapter.setServices(services);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        discoveryManager.startDiscovery();
    }

    @Override
    protected void onStop() {
        super.onStop();
        discoveryManager.stopDiscovery();
    }

    private void showRegistrationDialog() {
        HouseholdService selectedService = adapter.getSelectedService();
        if (selectedService == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_registration, null);
        EditText nameInput = dialogView.findViewById(R.id.member_name_input);
        EditText emailInput = dialogView.findViewById(R.id.member_email_input);

        new AlertDialog.Builder(this)
                .setTitle("Register Member")
                .setView(dialogView)
                .setPositiveButton("Join", (dialog, which) -> {
                    String name = nameInput.getText().toString();
                    String email = emailInput.getText().toString();
                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    performRegistration(selectedService, name, email);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performRegistration(HouseholdService service, String name, String email) {
        registrationManager.register(service, name, email, new RegistrationManager.RegistrationCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(JoinHouseholdActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onPending() {
                Toast.makeText(JoinHouseholdActivity.this, "Registration pending admin approval", Toast.LENGTH_LONG).show();
                // In a real app, you'd move to a pending screen
            }

            @Override
            public void onError(String message) {
                Toast.makeText(JoinHouseholdActivity.this, "Error: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}
