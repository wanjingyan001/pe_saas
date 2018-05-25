package com.sogukj.pe.bean;

/**
 * Created by sogubaby on 2018/4/21.
 */

public class MessageEvent {
    private CreditInfo.Item item;

    public MessageEvent(CreditInfo.Item item) {
        this.item = item;
    }

    public CreditInfo.Item getMessage() {
        return item;
    }

    public void setMessage(CreditInfo.Item item) {
        this.item = item;
    }
}

