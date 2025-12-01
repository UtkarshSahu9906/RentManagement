package com.utkarsh.rentmanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.utkarsh.rentmanagement.model.user;
import com.utkarsh.rentmanagement.R;
import java.util.ArrayList;
import java.util.List;

public class userAdapter extends RecyclerView.Adapter<userAdapter.ItemViewHolder> {

    private List<user> userList;
    private List<user> userListFull;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(user item);
    }

    public userAdapter(List<user> userList, OnItemClickListener listener) {
        this.userList = new ArrayList<>(userList);
        this.userListFull = new ArrayList<>(userList);
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
        user user = userList.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // Search filter method
    public void filter(String query) {
        List<user> filteredList = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            filteredList.addAll(userListFull);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (user item : userListFull) {
                if (item.getName().contains(filterPattern) ||
                        item.getAddress().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }

        userList.clear();
        userList.addAll(filteredList);
        notifyDataSetChanged();
    }

    // Update data method
    public void updateData(List<user> newItems) {
        userList.clear();
        userListFull.clear();
        userList.addAll(newItems);
        userListFull.addAll(newItems);
        notifyDataSetChanged();
    }

    // Clear all data
    public void clearData() {
        userList.clear();
        userListFull.clear();
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

        public void bind(final user item, final OnItemClickListener listener) {
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