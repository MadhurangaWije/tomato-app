package io.kavith.tomato_app;

import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import okhttp3.MultipartBody;
import retrofit2.http.Part;


public interface APIMethods {
    @Multipart
    @POST("/predict")
    Call<TomatoAPIResponse> getTomatoAPIResponse(@Part MultipartBody.Part file);
}
