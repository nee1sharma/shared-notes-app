package com.hitstudio.apps.netbook.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hitstudio.apps.netbook.R;
import com.hitstudio.apps.netbook.data.remote.NetBookApi;

import java.util.ArrayList;
import java.util.List;

/** Renders devices returned by the authenticated household registry, not mDNS service names. */
public final class ConnectedDeviceAdapter extends RecyclerView.Adapter<ConnectedDeviceAdapter.ViewHolder> {
    private final List<NetBookApi.DeviceView> devices = new ArrayList<>();

    public void setDevices(List<NetBookApi.DeviceView> nextDevices) {
        devices.clear();
        if (nextDevices != null) devices.addAll(nextDevices);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_household_service, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NetBookApi.DeviceView device = devices.get(position);
        holder.name.setText(device.deviceName);
        holder.detail.setText(device.memberName + " · " + device.status);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView detail;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.household_name);
            detail = itemView.findViewById(R.id.household_host);
        }
    }
}
