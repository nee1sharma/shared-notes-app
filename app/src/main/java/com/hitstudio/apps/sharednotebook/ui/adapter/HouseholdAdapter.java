package com.hitstudio.apps.sharednotebook.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hitstudio.apps.sharednotebook.R;
import com.hitstudio.apps.sharednotebook.domain.model.HouseholdService;

import java.util.ArrayList;
import java.util.List;

public final class HouseholdAdapter extends RecyclerView.Adapter<HouseholdAdapter.ViewHolder> {
    private final List<HouseholdService> services = new ArrayList<>();
    private final OnServiceClickListener listener;
    private int selectedPosition = -1;

    public interface OnServiceClickListener {
        void onServiceClick(HouseholdService service);
    }

    public HouseholdAdapter(OnServiceClickListener listener) {
        this.listener = listener;
    }

    public void setServices(List<HouseholdService> newServices) {
        services.clear();
        services.addAll(newServices);
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public HouseholdService getSelectedService() {
        if (selectedPosition >= 0 && selectedPosition < services.size()) {
            return services.get(selectedPosition);
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_household_service, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HouseholdService service = services.get(position);
        holder.nameText.setText(service.getHouseholdName());
        holder.hostText.setText(service.getHost() + ":" + service.getPort());
        
        holder.itemView.setSelected(selectedPosition == position);
        holder.itemView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            listener.onServiceClick(service);
        });
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView nameText;
        final TextView hostText;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.household_name);
            hostText = view.findViewById(R.id.household_host);
        }
    }
}
