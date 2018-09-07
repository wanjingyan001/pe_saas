package com.sogukj.pe.module.calendar.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Utils;
import com.sogukj.pe.bean.KeyNode;
import com.sogukj.pe.bean.TodoDay;
import com.sogukj.pe.bean.TodoYear;
import com.sogukj.pe.interf.ScheduleItemClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by admin on 2017/12/7.
 */

public class TodoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int YEAR = 1, DAY = 2, INFO = 3;
    private List<Object> data;
    private Context context;
    private ScheduleItemClickListener listener;

    public TodoAdapter(List<Object> data, Context context) {
        this.data = data;
        this.context = context;
    }

    public void setListener(ScheduleItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == YEAR) {
            return new YearHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_year, parent, false));
        } else if (viewType == DAY) {
            return new DayHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_dayofmonth, parent, false));
        } else if (viewType == INFO) {
            return new InfoHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_info, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Object o = data.get(position);
        if (holder instanceof YearHolder) {
            TodoYear year = (TodoYear) o;
            ((YearHolder) holder).Year.setText(year.getYear());
        } else if (holder instanceof DayHolder) {
            TodoDay day = (TodoDay) o;
            try {
                Date parse = new SimpleDateFormat("yyyy-MM-dd").parse(day.getDayTime());
                ((DayHolder) holder).day.setText(Utils.getTime(parse, "dd"));
                ((DayHolder) holder).month.setText(Utils.getTime(parse, "E \nyyyy年MM月"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else if (holder instanceof InfoHolder) {
            KeyNode info = (KeyNode) o;
            try {
                Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(info.getEnd_time());
                ((InfoHolder) holder).time.setText(Utils.getTime(parse, "HH:mm"));
                ((InfoHolder) holder).content.setText(info.getTitle());
                ((InfoHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(v, position);
                    }
                });
                if (info.getName() != null) {
                    ((InfoHolder) holder).todoPerson.setText(info.getName());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = data.get(position);
        if (o instanceof TodoYear) {
            return YEAR;
        } else if (o instanceof TodoDay) {
            return DAY;
        } else if (o instanceof KeyNode) {
            return INFO;
        } else {
            return INFO;
        }
    }

    class YearHolder extends RecyclerView.ViewHolder {
        private TextView Year;

        public YearHolder(View itemView) {
            super(itemView);
            Year = itemView.findViewById(R.id.yearTv);
        }
    }

    class DayHolder extends RecyclerView.ViewHolder {
        private TextView day;
        private TextView month;

        public DayHolder(View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.day);
            month = itemView.findViewById(R.id.month);
        }
    }

    class InfoHolder extends RecyclerView.ViewHolder {
        private TextView todoPerson;
        private TextView time;
        //        private ImageView finishBox;
        private TextView content;
        private View view;

        public InfoHolder(View itemView) {
            super(itemView);
            view = itemView;
            time = itemView.findViewById(R.id.timeTv);
//            finishBox = ((ImageView) itemView.findViewById(R.id.finishBox));
            content = itemView.findViewById(R.id.content);
            todoPerson = itemView.findViewById(R.id.todoPerson);
        }
    }
}
