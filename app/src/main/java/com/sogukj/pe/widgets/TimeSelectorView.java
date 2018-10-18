package com.sogukj.pe.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.adapter.WheelAdapter;
import com.bigkoo.pickerview.lib.WheelView;
import com.bigkoo.pickerview.listener.OnItemSelectedListener;
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
 * Created by CH-ZH on 2018/9/30.
 */
public class TimeSelectorView extends LinearLayout {

    private View rootView;
    private MonthPager mCalendarDateView;
    private CalendarViewAdapter calendarAdapter;
    private TabLayout tabs;
    private WheelView mHour;
    private WheelView mMinute;
    private LinearLayout mMain, mSub;
    private int[] data = new int[5];
    private TextView mOK;
    private Context mContext;
    public TimeSelectorView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public TimeSelectorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public TimeSelectorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        rootView = View.inflate(mContext,R.layout.calendar_select, this);
        mCalendarDateView = rootView.findViewById(R.id.calendarDateView);
        tabs = rootView.findViewById(R.id.tabs);
        mHour = rootView.findViewById(R.id.hour);
        mMinute = rootView.findViewById(R.id.minute);
        mMain = rootView.findViewById(R.id.main);
        mSub = rootView.findViewById(R.id.sub);
        mOK = rootView.findViewById(R.id.confirm);
        mOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss(false);
            }
        });

        //时分秒
        final ArrayList<Integer> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(i);
        }
        mHour.setCyclic(true);
        mHour.setAdapter(new TimeSelectorView.MyAdapter(hours));
        mHour.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                data[3] = hours.get(index);
                tabs.getTabAt(1).setText(getDisPlayNumber(data[3]) + ":" + getDisPlayNumber(data[4]));
            }
        });
        final ArrayList<Integer> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutes.add(i);
        }
        mMinute.setCyclic(true);
        mMinute.setAdapter(new TimeSelectorView.MyAdapter(minutes));
        mMinute.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                data[4] = minutes.get(index);
                tabs.getTabAt(1).setText(getDisPlayNumber(data[3]) + ":" + getDisPlayNumber(data[4]));
            }
        });

        //年月日
        OnSelectDateListener onSelectDateListener = new OnSelectDateListener() {
            @Override
            public void onSelectDate(CalendarDate date) {
                data[0] = date.year;
                data[1] = date.month;
                data[2] = date.day;
                tabs.getTabAt(0).setText(data[0] + "-" + getDisPlayNumber(data[1]) + "-" + getDisPlayNumber(data[2]));

                tabs.getTabAt(1).select();
                mMain.setVisibility(View.GONE);
                mSub.setVisibility(View.VISIBLE);

            }

            @Override
            public void onSelectOtherMonth(int offset) {
                //偏移量 -1表示上一个月 ， 1表示下一个月
                mCalendarDateView.selectOtherMonth(offset);
            }
        };
        CustomDayView dayView = new CustomDayView(mContext, R.layout.custom_day);
        calendarAdapter = new CalendarViewAdapter(mContext,
                onSelectDateListener,
                CalendarAttr.CalendayType.MONTH, dayView);
        CalendarViewAdapter.weekArrayType = 1;
        mCalendarDateView.setAdapter(calendarAdapter);
        mCalendarDateView.setCurrentItem(MonthPager.CURRENT_DAY_INDEX);
        mCalendarDateView.addOnPageChangeListener(new MonthPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                //mCurrentPage = position;
                ArrayList<Calendar> currentCalendars = calendarAdapter.getPagers();
                if (currentCalendars.get(position % currentCalendars.size()) instanceof Calendar) {
                    CalendarDate date = currentCalendars.get(position % currentCalendars.size()).getSeedDate();
                    data[0] = date.year;
                    data[1] = date.month;
                    data[2] = date.day;
                    tabs.getTabAt(0).setText(data[0] + "-" + getDisPlayNumber(data[1]) + "-" + getDisPlayNumber(data[2]));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        //calendarAdapter.notifyDataChanged(new CalendarDate());

        //切换
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    mMain.setVisibility(View.VISIBLE);
                    mSub.setVisibility(View.GONE);
                } else if (position == 1) {
                    mMain.setVisibility(View.GONE);
                    mSub.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        data = Utils.getYMDHMInCalendar(new Date());
        tabs.getTabAt(0).setText(data[0] + "-" + getDisPlayNumber(data[1]) + "-" + getDisPlayNumber(data[2]));

        mHour.setCurrentItem(data[3]);
        mMinute.setCurrentItem(data[4]);
    }

    private void dismiss(final boolean isEmpty) {

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(data[0], data[1] - 1, data[2], data[3], data[4], 0);
        Date mDate = calendar.getTime();
        if (isEmpty) {
            mListener.onClick(null);
        } else {
            mListener.onClick(mDate);
        }

    }

    //type    1天数   2时分
    public void show(Date date, OnTimeClick listener) {

        mListener = listener;

        data = Utils.getYMDHMInCalendar(date);
        tabs.getTabAt(0).setText(data[0] + "-" + getDisPlayNumber(data[1]) + "-" + getDisPlayNumber(data[2]));

        mHour.setCurrentItem(data[3]);
        mMinute.setCurrentItem(data[4]);

        tabs.getTabAt(0).select();
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.GONE);

        tabs.getTabAt(1).setText(getDisPlayNumber(data[3]) + ":" + getDisPlayNumber(data[4]));

    }

    public void show(java.util.Calendar cal, OnTimeClick listener) {

        mListener = listener;

        data = new int[]{cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DATE),
                cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE)};
        tabs.getTabAt(0).setText(data[0] + "-" + getDisPlayNumber(data[1]) + "-" + getDisPlayNumber(data[2]));

        mHour.setCurrentItem(data[3]);
        mMinute.setCurrentItem(data[4]);

        tabs.getTabAt(0).select();
        mMain.setVisibility(View.VISIBLE);
        mSub.setVisibility(View.GONE);


        tabs.getTabAt(1).setText(getDisPlayNumber(data[3]) + ":" + getDisPlayNumber(data[4]));

    }

    private OnTimeClick mListener;

    private String getDisPlayNumber(int num) {
        return num < 10 ? "0" + num : "" + num;
    }

    class MyAdapter implements WheelAdapter<Integer> {
        private ArrayList<Integer> data = new ArrayList<>();

        public MyAdapter(ArrayList<Integer> list) {
            data.addAll(list);
        }

        @Override
        public int getItemsCount() {
            return data.size();
        }

        @Override
        public Integer getItem(int index) {
            return data.get(index);
        }

        @Override
        public int indexOf(Integer o) {
            return o.intValue();
        }
    }

    public interface OnTimeClick {
        void onClick(Date date);
    }
}