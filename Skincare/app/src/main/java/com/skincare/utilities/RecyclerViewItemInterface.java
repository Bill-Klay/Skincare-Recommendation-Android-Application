package com.skincare.utilities;

public interface RecyclerViewItemInterface {
    void itemClick(String name);
    void itemClick(long product_id, String name, String desc, String image_url);
}
