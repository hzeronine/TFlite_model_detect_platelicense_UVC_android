package com.example.platedetect2.ScanQR;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("api/emaillocation")
    Call<List<EmailLocation>> getEmailLocations();
    @GET("api/users/email/{email}")
    Call<User> getUserByEmail(@Path("email") String email);

    @GET("api/users/token/{user_id}")
    Call<TokenResponse> getUserToken(@Path("user_id") String userId);

    @POST("api/checkin")
    Call<CheckIn> postCheckIn(@Body CheckIn requestBody);

    @POST("api/checkout")
    Call<CheckOut> postCheckOut(@Body CheckOut requestBody);

}

