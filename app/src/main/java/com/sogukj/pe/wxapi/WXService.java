package com.sogukj.pe.wxapi;


import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by admin on 2018/10/30.
 */

public interface WXService {
    @GET("sns/oauth2/access_token")
    Observable<WXAccToken> getAccountToken(@QueryMap Map<String,String> params);
}
