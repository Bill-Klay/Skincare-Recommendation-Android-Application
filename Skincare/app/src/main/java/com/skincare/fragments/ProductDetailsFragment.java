package com.skincare.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skincare.R;
import com.skincare.utilities.Loader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailsFragment extends Fragment implements View.OnClickListener {

    private long id;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ProgressDialog loader;

    private ImageView img_product;
    private TextView tv_product_name;
    private TextView tv_product_price;
    private TextView tv_product_desc;
    private TextView tv_avg_rating;
    private TextView tv_comments_count;
    private TextView tv_no_comments_msg;

    private RatingBar ratingBar;
    private int stars;

    private TextInputLayout layout_comment;
    private TextInputEditText et_comment;

    private RecyclerView comment_list;
    private List<CommentsModel> list;
    private CommentsAdapter adapter;

    private List<Long> likedProducts;

    private ImageButton btn_like_product;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            id = getArguments().getLong("id");
        }

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_details, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        img_product = view.findViewById(R.id.img_product);
        tv_product_name = view.findViewById(R.id.tv_product_name);
        tv_product_price = view.findViewById(R.id.tv_product_price);
        tv_product_desc = view.findViewById(R.id.tv_product_desc);
        tv_avg_rating = view.findViewById(R.id.tv_avg_rating);
        tv_comments_count = view.findViewById(R.id.tv_comments_count);
        tv_no_comments_msg = view.findViewById(R.id.tv_no_comments_msg);

        btn_like_product = view.findViewById(R.id.btn_like_product);
        btn_like_product.setOnClickListener(this);

        ImageButton btn_send_comment = view.findViewById(R.id.btn_send_comment);
        btn_send_comment.setOnClickListener(this);

        ratingBar = view.findViewById(R.id.ratingBar);
        ratingBar.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float touchPositionX = event.getX();
                float width = ratingBar.getWidth();
                float starsf = (touchPositionX / width) * 5.0f;
                stars = (int) starsf + 1;
                ratingBar.setRating(stars);
                updateUserRating(id, stars);

                v.setPressed(false);
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setPressed(true);
            }

            if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setPressed(false);
            }
            return true;
        });

        layout_comment = view.findViewById(R.id.layout_comment);
        et_comment = view.findViewById(R.id.et_comment);
        et_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout_comment.isErrorEnabled())
                    layout_comment.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        comment_list = view.findViewById(R.id.comment_list);
        comment_list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list = new ArrayList<>();
        adapter = new CommentsAdapter(list, requireContext());
        comment_list.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        comment_list.addItemDecoration(dividerItemDecoration);
        getProductComments();

        getProductDetails();

        likedProducts = new ArrayList<>();
    }

    private void updateUserRating(long id, int stars) {

        loader = Loader.show(requireContext());

        Map<String, Object> ratingMap = new HashMap<>();
        ratingMap.put("productId", id);
        ratingMap.put("user_id", currentUser.getUid());
        ratingMap.put("rating", stars);

        db.collection("Users")
                .document(currentUser.getUid())
                .collection("ratings")
                .document()
                .set(ratingMap)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        Toast.makeText(requireContext(), "Product Rating Updated!",
                                Toast.LENGTH_LONG).show();
                    } else {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        Toast.makeText(requireContext(), task1.getException().getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateUserComment(String comment) {

        loader = Loader.show(requireContext());

        Map<String, Object> commentsMap = new HashMap<>();
        commentsMap.put("productId", id);
        commentsMap.put("comment", comment);

        db.collection("Users")
                .document(currentUser.getUid())
                .collection("comments")
                .document()
                .set(commentsMap)
                .addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        if (tv_no_comments_msg.getVisibility() == View.VISIBLE)
                            tv_no_comments_msg.setVisibility(View.GONE);

                        if (comment_list.getVisibility() == View.GONE)
                            comment_list.setVisibility(View.VISIBLE);

                        CommentsModel item = new CommentsModel(
                                comment,
                                id
                        );

                        list.add(item);
                        adapter.notifyDataSetChanged();

                        String count = "Comments (" + list.size() + ")";
                        tv_comments_count.setText(count);

                        et_comment.setText("");

                        Toast.makeText(requireContext(), "Product Comment Added!",
                                Toast.LENGTH_LONG).show();
                    } else {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        Toast.makeText(requireContext(), task1.getException().getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void getProductDetails() {

        loader = Loader.show(requireContext());

        db.collection("products")
                .document(String.valueOf(id))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        DocumentSnapshot document = task.getResult();

                        String image_url = document.getString("image_url");
                        Picasso.get().load(image_url).placeholder(R.drawable.no_image).into(img_product);

                        String description = document.getString("description");
                        tv_product_desc.setText(description);

                        String product_name = document.getString("product_name");
                        tv_product_name.setText(product_name);

                        long price = document.getLong("price");
                        String textPrice = "$" + price;
                        tv_product_price.setText(textPrice);

                        long rating = document.getLong("rating");
                        String textRating = "(" + rating + ")";
                        tv_avg_rating.setText(textRating);

                        db.collection("Users")
                                .document(currentUser.getUid())
                                .collection("ratings")
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful() && task1.getResult().size() > 0) {
                                        for (DocumentSnapshot documentSnapshot : task1.getResult()) {
                                            long productId = documentSnapshot.getLong("productId");
                                            if (productId == id) {
                                                long ratingStars = documentSnapshot.getLong("rating");
                                                ratingBar.setRating(ratingStars);
                                            }
                                        }
                                    }
                                });

                        db.collection("Users")
                                .document(currentUser.getUid())
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task1.getResult();
                                        List<Long> likedProducts = (List<Long>) documentSnapshot.get("likedProducts");
                                        if (likedProducts != null && likedProducts.contains(id)) {
                                            btn_like_product.setEnabled(false);
                                            btn_like_product.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_like_red));
                                        }
                                    }
                                });

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                    } else {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        Toast.makeText(requireContext(), task.getException().getLocalizedMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void getProductComments() {

        //loader = Loader.show(requireContext());

        db.collection("Users")
                .document(currentUser.getUid())
                .collection("comments")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().size() > 0) {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();

                        for (DocumentSnapshot documentSnapshot : task.getResult()) {

                            long productId = documentSnapshot.getLong("productId");

                            if (productId == id) {
                                if (tv_no_comments_msg.getVisibility() == View.VISIBLE)
                                    tv_no_comments_msg.setVisibility(View.GONE);

                                if (comment_list.getVisibility() == View.GONE)
                                    comment_list.setVisibility(View.VISIBLE);

                                String comment = documentSnapshot.getString("comment");

                                CommentsModel item = new CommentsModel(
                                        comment,
                                        productId
                                );

                                list.add(item);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        String count = "Comments (" + list.size() + ")";
                        tv_comments_count.setText(count);

                    } else {

                        if (loader != null && loader.isShowing())
                            loader.dismiss();
                    }
                });

    }

    @Override
    public void onClick(View v) {

        int id = v.getId();

        if (id == R.id.btn_like_product) {

            if (currentUser != null) {

                loader = Loader.show(requireContext());

                db.collection("Users")
                        .document(currentUser.getUid())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                DocumentSnapshot document = task.getResult();

                                likedProducts = (List<Long>) document.get("likedProducts");

                                if (likedProducts == null)
                                    likedProducts = new ArrayList<>();

                                likedProducts.add(this.id);

                                Map<String, Object> docData = new HashMap<>();
                                docData.put("likedProducts", likedProducts);

                                db.collection("Users")
                                        .document(currentUser.getUid())
                                        .update(docData)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                if (loader != null && loader.isShowing())
                                                    loader.dismiss();

                                                btn_like_product.setEnabled(false);
                                                btn_like_product.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_like_red));
                                            }
                                        });

                            } else {

                                if (loader != null && loader.isShowing())
                                    loader.dismiss();

                                Toast.makeText(requireContext(), task.getException().getLocalizedMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
            }
        } else if (id == R.id.btn_send_comment) {
            String comment = et_comment.getText().toString();

            if (comment.isEmpty()) {
                layout_comment.setErrorEnabled(true);
                layout_comment.setError("Enter Comment");
                layout_comment.requestFocus();
            } else {
                updateUserComment(comment);
            }
        }
    }
}