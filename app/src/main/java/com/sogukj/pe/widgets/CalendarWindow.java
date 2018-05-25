package com.sogukj.pe.widgets;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.interf.OnSelectDateListener;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.Calendar;
import com.ldf.calendar.view.MonthPager;
import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by admin on 2017/12/9.
 */

public class CalendarWindow extends PopupWindow {
    private Context context;
    private MonthPager calendarView;
    private TextView monthAndYear;
    private CalendarDate seedDate;

    public CalendarWindow(Context context, CalendarSelectListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
        init();
    }


    private void init() {
        View inflate = LayoutInflater.from(context).inflate(R.layout.layout_calendar_window, null);
        monthAndYear = inflate.findViewById(R.id.monthAndYear);
        calendarView = inflate.findViewById(R.id.calendar_view);
        initCalendarView();
        this.setContentView(inflate);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.ShareDialogAnim);
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        this.setOutsideTouchable(true);
    }


    private void initCalendarView() {
        CustomDayView dayView = new CustomDayView(context, R.layout.custom_day);
        final CalendarViewAdapter calendarViewAdapter = new CalendarViewAdapter(context, new OnSelectDateListener() {
            @Override
            public void onSelectDate(CalendarDate date) {
                if (listener != null) {
                    dismiss();
                    listener.daySelect(date);
                }
            }

            @Override
            public void onSelectOtherMonth(int offset) {
                calendarView.selectOtherMonth(offset);
            }
        }, CalendarAttr.CalendayType.MONTH, dayView);
        CalendarViewAdapter.weekArrayType = 1;
        long millis = System.currentTimeMillis();
        monthAndYear.setText(Utils.getTime(new Date(millis), "yyyy年") + Utils.getTime(new Date(millis), "MM月"));
        calendarView.setAdapter(calendarViewAdapter);
        calendarView.setCurrentItem(MonthPager.CURRENT_DAY_INDEX);
        calendarView.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                page.setAlpha((float) Math.sqrt((1 - Math.abs(position))));
            }
        });
        calendarView.addOnPageChangeListener(new MonthPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ArrayList<Calendar> pagers = calendarViewAdapter.getPagers();
                if (pagers.get(position % pagers.size()) instanceof Calendar) {
                    seedDate = pagers.get(position % pagers.size()).getSeedDate();
                    monthAndYear.setText(seedDate.getYear() + "年" + seedDate.getMonth() + "月");
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        calendarView.setViewheight(Utils.dpToPx(context, 270));
    }


    private CalendarSelectListener listener;


    interface CalendarSelectListener {
        void daySelect(CalendarDate date);
    }

}
