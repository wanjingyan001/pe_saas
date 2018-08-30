package com.sogukj.pe.service.socket;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by CH-ZH on 2018/8/30.
 */
public class QidHelper implements Serializable {

    String name;
    Map<String, Long> map = new HashMap<>();

    public QidHelper(String name) {
        this.name = name;
    }

    public String getQid(String key) {
        for (String has : map.keySet()) {
            if (has.equals(name + key)) {
                return name + key + map.get(has);
            }
        }
        return name + key;
    }
}
