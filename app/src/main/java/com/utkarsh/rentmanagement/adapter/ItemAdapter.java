package com.utkarsh.rentmanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.utkarsh.rentmanagement.model.Item;
import com.utkarsh.rentmanagement.R;
import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;
    private List<Item> itemListFull;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public ItemAdapter(List<Item> itemList, OnItemClickListener listener) {
        this.itemList = new ArrayList<>(itemList);
        this.itemListFull = new ArrayList<>(itemList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // Search filter method
    public void filter(String query) {
        List<Item> filteredList = new ArrayList<>();

        if (query == null || query.isEmpty()) {
            filteredList.addAll(itemListFull);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Item item : itemListFull) {
                if (item.getTitle().toLowerCase().contains(filterPattern) ||
                        item.getDescription().toLowerCase().contains(filterPattern) ||
                        item.getCategory().toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
        }

        itemList.clear();
        itemList.addAll(filteredList);
        notifyDataSetChanged();
    }

    // Update data method
    public void updateData(List<Item> newItems) {
        itemList.clear();
        itemListFull.clear();
        itemList.addAll(newItems);
        itemListFull.addAll(newItems);
        notifyDataSetChanged();
    }

    // Clear all data
    public void clearData() {
        itemList.clear();
        itemListFull.clear();
        notifyDataSetChanged();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvDescription, tvPrice;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        public void bind(final Item item, final OnItemClickListener listener) {
            tvTitle.setText(item.getTitle());
            tvDescription.setText(item.getDescription());
            tvPrice.setText(String.format("$%.2f", item.getPrice()));

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