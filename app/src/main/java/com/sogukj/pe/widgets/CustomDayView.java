package com.sogukj.pe.widgets;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ldf.calendar.CalendarUtils;
import com.ldf.calendar.LunarBean;
import com.ldf.calendar.LunarUtils;
import com.ldf.calendar.Utils;
import com.ldf.calendar.component.State;
import com.ldf.calendar.interf.IDayRenderer;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.DayView;
import com.sogukj.pe.R;

import java.util.Calendar;

/**
 * Created by ldf on 17/6/26.
 */

public class CustomDayView extends DayView {

    private final CalendarDate today = new CalendarDate();
    //private final View selectedBg;
    private final LinearLayout mRoot;
    private final TextView dateTv, mNL, mState;
    private final ImageView marker;
    private CalendarDate currentDay;

    /**
     * 构造器
     *
     * @param context        上下文
     * @param layoutResource 自定义DayView的layout资源
     */
    public CustomDayView(Context context, int layoutResource) {
        super(context, layoutResource);
        //selectedBg = findViewById(R.id.selected_bg);
        mRoot = findViewById(R.id.root);
        dateTv = findViewById(R.id.date);
        mNL = findViewById(R.id.nongli);
        mState = findViewById(R.id.state);
        marker = findViewById(R.id.maker);
    }

    @Override
    public void refreshContent() {
        renderToday(day.getDate());
        renderSelect(day.getState());
        renderMarker(day.getDate(), day.getState());
        super.refreshContent();
    }

    private void renderMarker(CalendarDate date, State state) {
        Calendar instance = Calendar.getInstance();
        instance.set(date.year, date.month - 1, date.day);
        String time =  com.sogukj.pe.baselibrary.utils.Utils.getTime(instance.getTime(), "yyyy-MM-dd");
        if (Utils.loadMarkData().containsKey(time)) {
//            if (state == State.SELECT || date.toString().equals(today.toString())) {
//                marker.setVisibility(GONE);
//            } else {
//            marker.setVisibility(VISIBLE);
//                if (Utils.loadMarkData().get(date.toString()).equals("0")) {
//                    marker.setEnabled(true);
//                } else {
//                    marker.setEnabled(false);
//                }
//            }
            marker.setVisibility(VISIBLE);
        } else {
            marker.setVisibility(INVISIBLE);
        }
    }

    private void renderSelect(State state) {
        if (state == State.SELECT) {
            //selectedBg.setVisibility(VISIBLE);
            mRoot.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            dateTv.setTextColor(Color.WHITE);
            mNL.setTextColor(Color.WHITE);
        } else if (state == State.NEXT_MONTH || state == State.PAST_MONTH) {
            //selectedBg.setVisibility(GONE);
            mRoot.setBackgroundColor(getResources().getColor(R.color.white));
            dateTv.setTextColor(Color.parseColor("#ffdcdee6"));
            mNL.setTextColor(Color.parseColor("#ffdcdee6"));
        } else {
            //selectedBg.setVisibility(GONE);
            mRoot.setBackgroundColor(getResources().getColor(R.color.white));
            if (currentDay != null && currentDay.equals(today)) {
                dateTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                mNL.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                dateTv.setTextColor(getResources().getColor(R.color.text_1));
                mNL.setTextColor(Color.parseColor("#ffdcdee6"));
            }
        }
    }

    private void renderToday(CalendarDate date) {
        if (date != null) {
            currentDay = date;
            dateTv.setText(date.day + "");

            final Calendar instance = Calendar.getInstance();
            instance.set(date.year, date.month - 1, date.day);
            String time = com.sogukj.pe.baselibrary.utils.Utils.getTime(instance.getTime(), "yyyyMMdd");
            //法定节假日---休
            //周六周日---班
            LunarBean bean = new LunarUtils().getLunarDate(time, false);
            mNL.setText(bean.desc);

            int[] mHolidays = CalendarUtils.getInstance().initAllHolidays(getContext(), date.year).getHolidays(date.month);
            if (mHolidays[date.day - 1] == 1) {
                //canvas.drawBitmap(mRestBitmap, rect, rectF, null);
                //节假日
                mState.setVisibility(VISIBLE);
                mState.setText("休");
                mState.setTextColor(Color.parseColor("#ff50d59d"));
            } else if (mHolidays[date.day - 1] == 2) {
                //canvas.drawBitmap(mWorkBitmap, rect, rectF, null);
                mState.setVisibility(VISIBLE);
                mState.setText("班");
                mState.setTextColor(Color.parseColor("#fff7b62b"));
            } else {
                mState.setVisibility(INVISIBLE);
            }
        }
    }

    @Override
    public IDayRenderer copy() {
        return new CustomDayView(context, layoutResource);
    }
}
