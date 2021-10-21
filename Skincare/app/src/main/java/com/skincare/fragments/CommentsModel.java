package com.skincare.fragments;

public class CommentsModel {

    private String comment;
    private long product_id;

    public CommentsModel(String comment, long product_id) {
        this.comment = comment;
        this.product_id = product_id;
    }

    public String getComment() {
        return comment;
    }

    public long getProduct_id() {
        return product_id;
    }
}
