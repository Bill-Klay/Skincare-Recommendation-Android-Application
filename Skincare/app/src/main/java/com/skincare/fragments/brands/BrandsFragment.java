package com.skincare.fragments.brands;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skincare.R;
import com.skincare.utilities.Loader;
import com.skincare.utilities.RecyclerViewItemInterface;

import java.util.ArrayList;
import java.util.List;

public class BrandsFragment extends Fragment implements RecyclerViewItemInterface {

    private FirebaseFirestore db;

    private List<BrandsModel> list;
    private BrandsAdapter adapter;

    private ProgressDialog loader;

    private NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_brands, container, false);
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

        RecyclerView list_brands = view.findViewById(R.id.list_brands);
        list_brands.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        list = new ArrayList<>();
        adapter = new BrandsAdapter(list, requireContext(), this);
        list_brands.setAdapter(adapter);

        getBrands();
    }

    private void getBrands() {

        loader = Loader.show(requireContext());

        db.collection("brands")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            if (documentSnapshot.exists()) {

                                String id = documentSnapshot.getId();
                                String brand = documentSnapshot.getString("brand");
                                String image_url = documentSnapshot.getString("image_url");

                                BrandsModel item = new BrandsModel(
                                        id,
                                        brand,
                                        image_url
                                );
                                list.add(item);
                            }
                            adapter.notifyDataSetChanged();
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
        Bundle args = new Bundle();
        args.putString("brand_name", name);
        navController.navigate(R.id.action_nav_brands_to_nav_brand_products, args);
    }

    @Override
    public void itemClick(long product_id, String name, String desc, String image_url) {

    }
}