package com.circleupinc.productandorder.Product.UploderUtils;


import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploaderApi {

    @Multipart
    @POST("imageupload.php")
    Call<String> uploadFile(@Part MultipartBody.Part file);

    


}

