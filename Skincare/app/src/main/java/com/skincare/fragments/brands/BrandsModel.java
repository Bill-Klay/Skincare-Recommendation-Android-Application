package com.skincare.fragments.brands;

public class BrandsModel {

    private String id;
    private String brand;
    private String image_url;

    public BrandsModel(String id, String brand, String image_url) {
        this.id = id;
        this.brand = brand;
        this.image_url = image_url;
    }

    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getImage_url() {
        return image_url;
    }
}
