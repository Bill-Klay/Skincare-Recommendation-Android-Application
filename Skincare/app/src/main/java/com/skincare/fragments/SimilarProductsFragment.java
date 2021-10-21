package com.skincare.fragments;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skincare.R;
import com.skincare.fragments.brands.BrandProductsAdapter;
import com.skincare.fragments.brands.BrandProductsModel;
import com.skincare.retrofit.RetrofitClient;
import com.skincare.utilities.Loader;
import com.skincare.utilities.RecyclerViewItemInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

public class SimilarProductsFragment extends Fragment implements RecyclerViewItemInterface {

    private FirebaseUser currentUser;

    private boolean contentBased = false, collaborativeBased = false;

    private ProgressDialog loader;

    private FirebaseFirestore db;

    private List<BrandProductsModel> contentBasedList;
    private BrandProductsAdapter contentBasedAdapter;

    private List<BrandProductsModel> collaborativeBasedList;
    private BrandProductsAdapter collaborativeBasedAdapter;

    private NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_similar_products, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        RecyclerView content_based_product_list = view.findViewById(R.id.content_based_product_list);
        content_based_product_list.setLayoutManager(new LinearLayoutManager(requireContext()));
        contentBasedList = new ArrayList<>();
        contentBasedAdapter = new BrandProductsAdapter(contentBasedList, requireContext(), this);
        content_based_product_list.setAdapter(contentBasedAdapter);

        RecyclerView collaborative_based_product_list = view.findViewById(R.id.collaborative_based_product_list);
        collaborative_based_product_list.setLayoutManager(new LinearLayoutManager(requireContext()));
        collaborativeBasedList = new ArrayList<>();
        collaborativeBasedAdapter = new BrandProductsAdapter(collaborativeBasedList, requireContext(), this);
        collaborative_based_product_list.setAdapter(collaborativeBasedAdapter);

        if (currentUser != null)
            callApi();
    }

    private void callApi() {

        loader = Loader.show(requireContext());

        Call<ResponseBody> contentBasedFiltering = RetrofitClient
                .getInstance()
                .getApi()
                .contentBasedFiltering(
                        currentUser.getUid()
                );

        Call<ResponseBody> collaborativeBasedFiltering = RetrofitClient
                .getInstance()
                .getApi()
                .collaborativeBasedFiltering(
                        currentUser.getUid()
                );

        contentBasedFiltering.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                try {
                    if (response.body() != null) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        long responseCode = jsonObject.getLong("response");

                        if (responseCode == 200) {
                            contentBased = true;
                        }

                        collaborativeBasedFiltering.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {
                                try {
                                    if (response.body() != null) {
                                        JSONObject jsonObject = new JSONObject(response.body().string());
                                        long responseCode = jsonObject.getLong("response");

                                        if (responseCode == 200) {
                                            collaborativeBased = true;
                                        }

                                        db.collection("recommendations")
                                                .document(currentUser.getUid())
                                                .get()
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot documentSnapshot = task.getResult();
                                                        if (documentSnapshot.exists()) {
                                                            if (collaborativeBased) {
                                                                List<Long> collaborativeList = (List<Long>) documentSnapshot.get("collaborative");
                                                                if (collaborativeList != null && collaborativeList.size() > 0) {
                                                                    for (int i = 0; i < collaborativeList.size(); i++) {
                                                                        db.collection("products")
                                                                                .document(String.valueOf(collaborativeList.get(i)))
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
                                                                                    collaborativeBasedList.add(item);
                                                                                    collaborativeBasedAdapter.notifyDataSetChanged();
                                                                                });
                                                                    }

                                                                }
                                                            }

                                                            if (contentBased) {
                                                                List<Long> contentList = (List<Long>) documentSnapshot.get("content");
                                                                if (contentList != null && contentList.size() > 0) {
                                                                    for (int i = 0; i < contentList.size(); i++) {
                                                                        db.collection("products")
                                                                                .document(String.valueOf(contentList.get(i)))
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
                                                                                    contentBasedList.add(item);
                                                                                    contentBasedAdapter.notifyDataSetChanged();
                                                                                });
                                                                    }

                                                                }
                                                            }

                                                            if (loader != null && loader.isShowing())
                                                                loader.dismiss();
                                                        }
                                                    }
                                                });

                                    }

                                } catch (JSONException | IOException e) {

                                    if (loader != null && loader.isShowing())
                                        loader.dismiss();

                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

                                if (loader != null && loader.isShowing())
                                    loader.dismiss();

                                Toast.makeText(requireContext(), t.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });

                    }

                } catch (JSONException | IOException e) {

                    if (loader != null && loader.isShowing())
                        loader.dismiss();

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

                if (loader != null && loader.isShowing())
                    loader.dismiss();

                Toast.makeText(requireContext(), t.getMessage(),
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
        navController.navigate(R.id.action_nav_similar_products_to_nav_brand_product_details, args);
    }
}