package com.skincare.fragments.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skincare.R;
import com.skincare.fragments.brands.BrandProductsModel;
import com.skincare.utilities.RecyclerViewItemInterface;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> implements Filterable {

    List<BrandProductsModel> list;
    List<BrandProductsModel> searchList;
    Context context;
    RecyclerViewItemInterface itemInterface;

    public SearchAdapter(List<BrandProductsModel> list, Context context, RecyclerViewItemInterface itemInterface) {
        this.list = list;
        this.context = context;
        this.itemInterface = itemInterface;
        searchList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_brand_products, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BrandProductsModel item = list.get(position);

        holder.tv_product_name.setText(item.getProduct_name());
        String price = "$" + item.getPrice();
        holder.tv_product_price.setText(price);

        Picasso
                .get()
                .load(item.getImage_url())
                .resize(200, 200)
                .placeholder(R.drawable.no_image)
                .into(holder.img_brand_product);

        holder.layout.setOnClickListener(v -> itemInterface.itemClick(
                item.getProduct_id(),
                item.getProduct_name(),
                item.getDescription(),
                item.getImage_url()
        ));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView img_brand_product;
        TextView tv_product_name, tv_product_price;
        LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.layout);
            img_brand_product = itemView.findViewById(R.id.img_brand_product);
            tv_product_name = itemView.findViewById(R.id.tv_product_name);
            tv_product_price = itemView.findViewById(R.id.tv_product_price);
        }
    }

    @Override
    public Filter getFilter() {
        return searchFilter;
    }

    private final Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<BrandProductsModel> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(searchList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (BrandProductsModel item : searchList) {
                    if (item.getProduct_name().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
