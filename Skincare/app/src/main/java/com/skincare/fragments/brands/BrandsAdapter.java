package com.skincare.fragments.brands;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.skincare.R;
import com.skincare.utilities.RecyclerViewItemInterface;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BrandsAdapter extends RecyclerView.Adapter<BrandsAdapter.ViewHolder> {

    List<BrandsModel> list;
    Context context;
    RecyclerViewItemInterface itemInterface;

    public BrandsAdapter(List<BrandsModel> list, Context context, RecyclerViewItemInterface itemInterface) {
        this.list = list;
        this.context = context;
        this.itemInterface = itemInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_brands, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        BrandsModel item = list.get(position);

        holder.tv_brand_name.setText(item.getBrand());

        Picasso.get().load(item.getImage_url()).placeholder(R.drawable.no_image).into(holder.img_brand);

        holder.cv_brand.setOnClickListener(v -> itemInterface.itemClick(item.getBrand()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView img_brand;
        TextView tv_brand_name;
        CardView cv_brand;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            img_brand = itemView.findViewById(R.id.img_brand);
            tv_brand_name = itemView.findViewById(R.id.tv_brand_name);

            cv_brand = itemView.findViewById(R.id.cv_brand);
        }
    }
}
