package com.skincare.fragments.brands;

public class BrandProductsModel {

    private String brand;
    private String category;
    private String description;
    private String image_url;
    private long price;
    private long product_id;
    private String product_name;
    private String product_url;
    private long rating;

    public BrandProductsModel(String brand, String category, String description, String image_url, long price, long product_id, String product_name, String product_url, long rating) {
        this.brand = brand;
        this.category = category;
        this.description = description;
        this.image_url = image_url;
        this.price = price;
        this.product_id = product_id;
        this.product_name = product_name;
        this.product_url = product_url;
        this.rating = rating;
    }

    public String getBrand() {
        return brand;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getImage_url() {
        return image_url;
    }

    public long getPrice() {
        return price;
    }

    public String getProduct_name() {
        return product_name;
    }

    public String getProduct_url() {
        return product_url;
    }

    public long getProduct_id() {
        return product_id;
    }

    public long getRating() {
        return rating;
    }
}
