package com.biniisu.leanrss.connectivity.inoreader.inoReaderApi;

import android.support.annotation.Keep;

import com.biniisu.leanrss.models.inoreader.TokenResponses;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by biniam on 2/17/18.
 */
@Keep
public interface OAuthApi {
    @FormUrlEncoded
    @POST("oauth2/token")
    Call<TokenResponses> oauthToken(@FieldMap Map<String, String> options);
}
