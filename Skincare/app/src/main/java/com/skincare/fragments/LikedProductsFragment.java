package com.skincare.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skincare.R;
import com.skincare.fragments.brands.BrandProductsAdapter;
import com.skincare.fragments.brands.BrandProductsModel;
import com.skincare.utilities.Loader;
import com.skincare.utilities.RecyclerViewItemInterface;

import java.util.ArrayList;
import java.util.List;

public class LikedProductsFragment extends Fragment implements RecyclerViewItemInterface {

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ProgressDialog loader;

    private NavController navController;

    private List<BrandProductsModel> list;
    private BrandProductsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liked_products, container, false);
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
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        RecyclerView liked_product_list = view.findViewById(R.id.liked_product_list);
        liked_product_list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list = new ArrayList<>();
        adapter = new BrandProductsAdapter(list, requireContext(), this);
        liked_product_list.setAdapter(adapter);

        getLikedProductList();
    }

    private void getLikedProductList(){

        loader = Loader.show(requireContext());

        db.collection("Users")
                .document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        DocumentSnapshot documentSnapshot = task.getResult();
                        List<Long> likedProducts = (List<Long>) documentSnapshot.get("likedProducts");
                        if (likedProducts != null && likedProducts.size() > 0) {

                            for (int i = 0; i < likedProducts.size(); i++){
                                db.collection("products")
                                        .document(String.valueOf(likedProducts.get(i)))
                                        .get()
                                        .addOnCompleteListener(task1 -> {
                                            DocumentSnapshot documentSnapshot1 = task1.getResult();

                                            String brand = documentSnapshot1.getString("brand");
                                                String category = documentSnapshot1.getString("category");
                                                String description = documentSnapshot1.getString("description");
                                                String image_url = documentSnapshot1.getString("image_url");
                                                long price = documentSnapshot1.getLong("price");
                                                long product_id = documentSnapshot1.getLong("product_id");
                                                String product_name = documentSnapshot1.getString("product_name");
                                                String product_url = documentSnapshot1.getString("product_url");
                                                long rating = documentSnapshot1.getLong("rating");

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
                                                adapter.notifyDataSetChanged();
                                        });
                            }

                        } else {

                            Toast.makeText(requireContext(), "No products found",
                                    Toast.LENGTH_LONG).show();
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
        navController.navigate(R.id.action_nav_liked_products_to_nav_brand_product_details, args);
    }
}