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
import com.utkarsh.rentmanagement.model.Shop;

import java.util.ArrayList;
import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ItemViewHolder> {

    private List<Shop> shopList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Shop item);
    }

    public ShopAdapter(List<Shop> shopList, OnItemClickListener listener) {
        this.shopList = new ArrayList<>(shopList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop_horizontal, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Shop shop = shopList.get(position);
        holder.bind(shop, listener);
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    // Search filter method
//    public void filter(String query) {
//        List<Customer> filteredList = new ArrayList<>();
//
//        if (query == null || query.isEmpty()) {
//            filteredList.addAll(customerListFull);
//        } else {
//            String filterPattern = query.toLowerCase().trim();
//            for (Customer item : customerListFull) {
//                if (item.getName().contains(filterPattern) ||
//                        item.getAddress().toLowerCase().contains(filterPattern)) {
//                    filteredList.add(item);
//                }
//            }
//        }
//
//        customerList.clear();
//        customerList.addAll(filteredList);
//        notifyDataSetChanged();
//    }

    // Update data method
//    public void updateData(List<Customer> newItems) {
//        customerList.clear();
//        customerListFull.clear();
//        customerList.addAll(newItems);
//        customerListFull.addAll(newItems);
//        notifyDataSetChanged();
//    }

    // Clear all data
    public void clearData() {

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

        public void bind(final Shop item, final OnItemClickListener listener) {
            name.setText(item.getShopName());
            address.setText(item.getShopId());
            //tvPrice.setText(String.format("$%.2f", item.getAadhaarNo()));
            Glide.with(itemView.getContext()).load(item.getBgImg()).into(profile);


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