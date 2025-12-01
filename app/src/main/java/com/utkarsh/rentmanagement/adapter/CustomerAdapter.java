package com.utkarsh.rentmanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.utkarsh.rentmanagement.model.Customer;
import com.utkarsh.rentmanagement.R;
import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ItemViewHolder> {

    private List<Customer> customerList;
    private List<Customer> customerListFull;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Customer item);
    }

    public CustomerAdapter(List<Customer> customerList, OnItemClickListener listener) {
        this.customerList = new ArrayList<>(customerList);
        this.customerListFull = new ArrayList<>(customerList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Customer Customer = customerList.get(position);
        holder.bind(Customer, listener);
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }

    // Search filter method
    public void filter(String query) {
        List<Customer> filteredList = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            filteredList.addAll(customerListFull);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Customer item : customerListFull) {
                if (item.getName().contains(filterPattern) ||
                        item.getAddress().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }

        customerList.clear();
        customerList.addAll(filteredList);
        notifyDataSetChanged();
    }

    // Update data method
    public void updateData(List<Customer> newItems) {
        customerList.clear();
        customerListFull.clear();
        customerList.addAll(newItems);
        customerListFull.addAll(newItems);
        notifyDataSetChanged();
    }

    // Clear all data
    public void clearData() {
        customerList.clear();
        customerListFull.clear();
        notifyDataSetChanged();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView name, address, tvPrice;
        private ImageView profile;


        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            profile = itemView.findViewById(R.id.profile);
        }

        public void bind(final Customer item, final OnItemClickListener listener) {
            name.setText(item.getName());
            address.setText(item.getAddress());
            //tvPrice.setText(String.format("$%.2f", item.getAadhaarNo()));
            Glide.with(itemView.getContext()).load(item.getImgUrl()).into(profile);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                }
            });
        }
    }
}