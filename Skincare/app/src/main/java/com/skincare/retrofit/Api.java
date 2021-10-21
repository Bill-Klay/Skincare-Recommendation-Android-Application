package com.skincare.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Api {

    @GET("contentBasedFiltering")
    Call<ResponseBody> contentBasedFiltering(
            @Query("user_id") String user_id
    );

    @GET("collaborativeBasedFiltering")
    Call<ResponseBody> collaborativeBasedFiltering(
            @Query("user_id") String user_id
    );
}
