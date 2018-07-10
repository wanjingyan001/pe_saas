package com.ldf.calendar;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jimmy on 2016/10/6 0006.
 */
public class CalendarUtils {

    private static CalendarUtils sUtils;
    private Map<String, int[]> sAllHolidays = new HashMap<>();
    private int year = 0;

    public static synchronized CalendarUtils getInstance() {
        if (sUtils == null) {
            sUtils = new CalendarUtils();
        }
        return sUtils;
    }

    public CalendarUtils initAllHolidays(Context context, int year) {
        if (this.year == year) {
            return sUtils;
        }
        this.year = year;
        try {
            InputStream is = context.getAssets().open("holiday" + year + ".json");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int i;
            while ((i = is.read()) != -1) {
                baos.write(i);
            }
            sAllHolidays = new Gson().fromJson(baos.toString(), new TypeToken<Map<String, int[]>>() {
            }.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sUtils;
    }

    public int[] getHolidays(int month) {
        int holidays[];
        if (sUtils.sAllHolidays != null) {
            holidays = sUtils.sAllHolidays.get(year + "" + month);
            if (holidays == null) {
                holidays = new int[42];
            }
        } else {
            holidays = new int[42];
        }
        return holidays;
    }
}

