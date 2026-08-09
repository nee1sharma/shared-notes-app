package com.hitstudio.apps.netbook.ui.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.hitstudio.apps.netbook.R;
import com.hitstudio.apps.netbook.data.remote.DiscoveryManager;
import com.hitstudio.apps.netbook.data.remote.NetBookApi;
import com.hitstudio.apps.netbook.data.remote.RegistrationManager;
import com.hitstudio.apps.netbook.ui.adapter.ConnectedDeviceAdapter;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class ConnectedDevicesActivity extends AppCompatActivity {
    @Inject
    DiscoveryManager discoveryManager;
    @Inject
    RegistrationManager registrationManager;

    private ConnectedDeviceAdapter adapter;
    private RecyclerView devicesList;
    private View emptyState;
    private TextView deviceCount;
    private MaterialButton refreshButton;
    private final Runnable restartDiscovery = () -> {
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            discoveryManager.startDiscovery();
        }
        refreshButton.setEnabled(true);
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected_devices);

        findViewById(R.id.back_button).setOnClickListener(view -> finish());
        refreshButton = findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(view -> refreshDiscovery());

        TextView registrationStatus = findViewById(R.id.registration_status);
        if (registrationManager.isRegistered()) {
            registrationStatus.setText(getString(
                    R.string.registered_to,
                    registrationManager.getControlPlaneUrl()
            ));
        } else {
            registrationStatus.setText(R.string.not_registered);
        }

        deviceCount = findViewById(R.id.device_count);
        devicesList = findViewById(R.id.devices_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        devicesList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConnectedDeviceAdapter();
        devicesList.setAdapter(adapter);

        discoveryManager.getDiscoveredServices().observe(this, services -> {
            if (!registrationManager.isRegistered() && !services.isEmpty()) {
                registrationStatus.setText(R.string.control_plane_found);
            }
        });
    }

    private void showDevices(List<NetBookApi.DeviceView> devices) {
            adapter.setDevices(devices);
            int count = devices.size();
            deviceCount.setText(getResources().getQuantityString(
                    R.plurals.devices_found,
                    count,
                    count
            ));
            emptyState.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
            devicesList.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
    }

    private void loadDevices() {
        registrationManager.loadConnectedDevices(new RegistrationManager.DeviceListCallback() {
            @Override
            public void onSuccess(List<NetBookApi.DeviceView> devices) {
                showDevices(devices);
            }

            @Override
            public void onError(String message) {
                showDevices(java.util.Collections.emptyList());
            }
        });
    }

    private void refreshDiscovery() {
        refreshButton.setEnabled(false);
        discoveryManager.stopDiscovery();
        refreshButton.postDelayed(restartDiscovery, 350L);
        loadDevices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        discoveryManager.startDiscovery();
        loadDevices();
    }

    @Override
    protected void onStop() {
        refreshButton.removeCallbacks(restartDiscovery);
        refreshButton.setEnabled(true);
        discoveryManager.stopDiscovery();
        super.onStop();
    }
}
