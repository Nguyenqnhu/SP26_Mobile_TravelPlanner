package com.example.weathertrip_sep490.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weathertrip_sep490.R;
import com.example.weathertrip_sep490.model.Partner;

import java.util.List;

public class PartnerAdapter extends RecyclerView.Adapter<PartnerAdapter.ViewHolder> {

    private final List<Partner> list;

    public PartnerAdapter(List<Partner> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_partner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Partner item = list.get(position);
        holder.txtName.setText(item.getName());
        holder.txtDesc.setText(item.getDescription());
        holder.txtDiscount.setText(item.getDiscount());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtDesc, txtDiscount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPartnerName);
            txtDesc = itemView.findViewById(R.id.txtPartnerDesc);
            txtDiscount = itemView.findViewById(R.id.txtPartnerDiscount);
        }
    }
}
