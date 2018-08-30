package com.sogukj.pe.bean;

/**
 * Created by CH-ZH on 2018/8/30.
 */
public class DzhToken extends DzhResp {
    Data Data;

    public static final class Data {
        int Id;
        RepDataToken RepDataToken[];

        public static final class RepDataToken {
            int result;
            String token;
            long create_time;
            long duration;
            String appid;
        }
    }

    public String getToken(){
        return Err == 0 ? Data.RepDataToken[0].token : "";
    }
}