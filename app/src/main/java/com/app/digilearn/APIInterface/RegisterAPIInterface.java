package com.app.digilearn.APIInterface;


import com.app.digilearn.model.DeviceId;
import com.app.digilearn.model.ModelNotif;
import com.app.digilearn.model.PostContact;
import com.app.digilearn.model.ResponseWebview;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface RegisterAPIInterface {


    @GET("/api/WebViews")
    Call<ResponseWebview> mGetWebview(@Header("Authorization") String Authorization);

    @Headers({"Content-Type: application/json", "X-Requested-With: XMLHttpRequest"})
    @POST("/api/Contacts")
    Call<Void> mPostContactList(@Header("Authorization") String Authorization, @Body List<PostContact> postContactModel);


    @Headers({"Content-Type: application/json", "X-Requested-With: XMLHttpRequest"})
    @POST("/api/Phones")
    Call<Void> mPostDeviceID(@Header("Authorization") String Authorization, @Body DeviceId DeviceID);


    @Headers({"Content-Type: application/json", "X-Requested-With: XMLHttpRequest"})
    @POST("/api/Contacts")
    Call<Void> mPostContact(@Header("Authorization") String Authorization, @Body PostContact postContactModel);


    @GET("/api/Notifications")
    Call<ModelNotif> mGetDetailNotif(@Header("Authorization") String Authorization, @Query("id") String id, @Query("message") String message);


}
