package com.skincare.fragments.brands;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skincare.R;
import com.skincare.utilities.RecyclerViewItemInterface;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BrandProductsAdapter extends RecyclerView.Adapter<BrandProductsAdapter.ViewHolder> {

    List<BrandProductsModel> list;
    Context context;
    RecyclerViewItemInterface itemInterface;

    public BrandProductsAdapter(List<BrandProductsModel> list, Context context, RecyclerViewItemInterface itemInterface) {
        this.list = list;
        this.context = context;
        this.itemInterface = itemInterface;
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
}
