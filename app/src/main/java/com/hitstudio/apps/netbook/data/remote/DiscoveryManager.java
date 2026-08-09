package com.hitstudio.apps.netbook.data.remote;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.hitstudio.apps.netbook.domain.model.HouseholdService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public final class DiscoveryManager {
    private static final String TAG = "DiscoveryManager";
    private static final String SERVICE_TYPE = "_netbook._tcp.";

    private final NsdManager nsdManager;
    private final MutableLiveData<List<HouseholdService>> discoveredServices = new MutableLiveData<>(Collections.emptyList());
    private final List<HouseholdService> currentServices = new ArrayList<>();

    private NsdManager.DiscoveryListener discoveryListener;

    @Inject
    public DiscoveryManager(@ApplicationContext Context context) {
        this.nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    public LiveData<List<HouseholdService>> getDiscoveredServices() {
        return discoveredServices;
    }

    public void startDiscovery() {
        if (discoveryListener != null) {
            return;
        }

        discoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else {
                    nsdManager.resolveService(service, new NsdManager.ResolveListener() {
                        @Override
                        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                            Log.e(TAG, "Resolve failed" + errorCode);
                        }

                        @Override
                        public void onServiceResolved(NsdServiceInfo serviceInfo) {
                            Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                            HouseholdService household = new HouseholdService(
                                    serviceInfo.getServiceName(),
                                    serviceInfo.getHost().getHostAddress(),
                                    serviceInfo.getPort()
                            );
                            addService(household);
                        }
                    });
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                removeService(service.getServiceName());
            }
        };

        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    public void stopDiscovery() {
        if (discoveryListener != null) {
            nsdManager.stopServiceDiscovery(discoveryListener);
            discoveryListener = null;
            currentServices.clear();
            discoveredServices.postValue(Collections.emptyList());
        }
    }

    private synchronized void addService(HouseholdService service) {
        if (!currentServices.contains(service)) {
            currentServices.add(service);
            discoveredServices.postValue(new ArrayList<>(currentServices));
        }
    }

    private synchronized void removeService(String serviceName) {
        currentServices.removeIf(service -> service.getHouseholdName().equals(serviceName));
        discoveredServices.postValue(new ArrayList<>(currentServices));
    }
}
