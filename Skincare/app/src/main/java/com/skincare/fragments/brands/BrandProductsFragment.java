package com.skincare.fragments.brands;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skincare.R;
import com.skincare.utilities.Loader;
import com.skincare.utilities.RecyclerViewItemInterface;

import java.util.ArrayList;
import java.util.List;

public class BrandProductsFragment extends Fragment implements RecyclerViewItemInterface {

    private String brand_name = "";

    private FirebaseFirestore db;

    private ProgressDialog loader;

    private NavController navController;

    private List<BrandProductsModel> list;
    private BrandProductsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            brand_name = getArguments().getString("brand_name");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_brand_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initializing Views...
        initViews(view);
    }

    private void initViews(View view) {

        navController = Navigation.findNavController(view);

        db = FirebaseFirestore.getInstance();

        TextView tv_brand_name = view.findViewById(R.id.tv_brand_name);
        tv_brand_name.setText(brand_name);

        RecyclerView brand_products_list = view.findViewById(R.id.brand_products_list);
        brand_products_list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list = new ArrayList<>();
        adapter = new BrandProductsAdapter(list, requireContext(), this);
        brand_products_list.setAdapter(adapter);

        getBrandProducts(brand_name.trim());
    }

    private void getBrandProducts(String brand_name) {

        loader = Loader.show(requireContext());

        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.exists()) {
                                String brand = documentSnapshot.getString("brand");
                                if (brand.equals(brand_name)) {
                                    String category = documentSnapshot.getString("category");
                                    String description = documentSnapshot.getString("description");
                                    String image_url = documentSnapshot.getString("image_url");
                                    long price = documentSnapshot.getLong("price");
                                    long product_id = documentSnapshot.getLong("product_id");
                                    String product_name = documentSnapshot.getString("product_name");
                                    String product_url = documentSnapshot.getString("product_url");
                                    long rating = documentSnapshot.getLong("rating");

                                    BrandProductsModel item = new BrandProductsModel(
                                            brand,
                                            category,
                                            description,
                                            image_url,
                                            price,
                                            product_id,
                                            product_name,
                                            product_url,
                                            rating
                                    );
                                    list.add(item);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }

                    } else {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        Toast.makeText(requireContext(), task.getException().getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void itemClick(String name) {

    }

    @Override
    public void itemClick(long product_id, String name, String desc, String image_url) {
        Bundle args = new Bundle();
        args.putLong("id", product_id);
        args.putString("name", name);
        args.putString("desc", desc);
        args.putString("image_url", image_url);
        navController.navigate(R.id.action_nav_brand_products_to_nav_brand_product_details, args);
    }
}