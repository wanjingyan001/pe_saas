package com.sogukj.pe.module.calendar.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.ToastUtils;
import com.sogukj.pe.baselibrary.utils.Utils;
import com.sogukj.pe.bean.TaskItemBean;
import com.sogukj.pe.bean.TodoDay;
import com.sogukj.pe.interf.ScheduleItemClickListener;
import com.sogukj.pe.service.CalendarService;
import com.sogukj.pe.service.Payload;
import com.sogukj.service.SoguApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by admin on 2017/12/7.
 */

public class TaskAdapter extends RecyclerView.Adapter {
    private int day = 1, info = 2;
    private Context context;
    private List<Object> data;

    public TaskAdapter(Context context, List<Object> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == day) {
            return new DayHolder(LayoutInflater.from(context).inflate(R.layout.item_clander_dayofmonth, parent, false));
        } else if (viewType == info) {
            return new InfoHolder(LayoutInflater.from(context).inflate(R.layout.item_task_list, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Object o = data.get(position);
        if (holder instanceof DayHolder) {
            TodoDay day = (TodoDay) o;
            try {
                Date parse = new SimpleDateFormat("yyyy-MM-dd").parse(day.getDayTime());
                if (Utils.getTime(parse, "yyyy年MM月dd日")
                        .equals(Utils.getTime(System.currentTimeMillis(), "yyyy年MM月dd日"))) {
                    ((DayHolder) holder).day.setText("今天");
                    ((DayHolder) holder).month.setVisibility(View.GONE);
                } else {
                    ((DayHolder) holder).day.setText(Utils.getTime(parse, "dd"));
                    ((DayHolder) holder).month.setVisibility(View.VISIBLE);
                    ((DayHolder) holder).month.setText(Utils.getWeek(parse.getTime()) + "\n" + Utils.getTime(parse.getTime(), "yyyy年MM月"));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else if (holder instanceof InfoHolder) {
            final TaskItemBean.ItemBean bean = (TaskItemBean.ItemBean) o;
            ((InfoHolder) holder).time.setText(bean.getEnd_time());
            ((InfoHolder) holder).content.setText(bean.getTitle());
            if (bean.getLeader() == null) {
                ((InfoHolder) holder).typeTv.setVisibility(View.GONE);
            } else {
                ((InfoHolder) holder).typeTv.setVisibility(View.VISIBLE);
                ((InfoHolder) holder).typeTv.setText(bean.getLeader());
            }
            if (bean.is_finish() == 1) {
                ((InfoHolder) holder).finishBox.setSelected(true);
                ((InfoHolder) holder).content.setPaintFlags(((InfoHolder) holder).content.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                ((InfoHolder) holder).finishBox.setSelected(false);
                ((InfoHolder) holder).content.setPaintFlags(((InfoHolder) holder).content.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
            ((InfoHolder) holder).finishBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishTask(bean.getId(), ((InfoHolder) holder).finishBox, ((InfoHolder) holder).content);
                }
            });


            ((InfoHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(v, position);
                    }
                }
            });
        }
    }

    private void finishTask(int id, final ImageView finishBox, final TextView content) {
        SoguApi.Companion.getService(((Activity) context).getApplication(), CalendarService.class)
                .finishTask(id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Payload<Integer>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Payload<Integer> objectPayload) {
                        if (objectPayload.isOk()) {
                            if (listener != null && objectPayload.getPayload() != null) {
                                listener.finishCheck(objectPayload.getPayload() == 1, 0);
                                finishBox.setSelected(objectPayload.getPayload() == 1);
                                if (objectPayload.getPayload() == 1) {
                                    content.setPaintFlags(
                                            content.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                } else {
                                    content.setPaintFlags(
                                            content.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                }
                            }
                        } else {
//                            Toast.makeText(context, objectPayload.getMessage(), Toast.LENGTH_SHORT).show();
                            if (objectPayload.getMessage() != null) {
                                ToastUtils.showErrorToast(objectPayload.getMessage(),context);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object o = data.get(position);
        if (o instanceof TodoDay) {
            return day;
        } else if (o instanceof TaskItemBean.ItemBean) {
            return info;
        }
        return info;
    }

    public List<Object> getData() {
        return data;
    }

    class DayHolder extends RecyclerView.ViewHolder {
        private TextView day;
        private TextView month;

        public DayHolder(View itemView) {
            super(itemView);
            day = itemView.findViewById(R.id.day);
            month = itemView.findViewById(R.id.month);
            itemView.findViewById(R.id.line).setVisibility(View.GONE);
        }
    }

    class InfoHolder extends RecyclerView.ViewHolder {
        private TextView typeTv;
        //        private TextView confirmStatus;
//        private TextView reason;
//        private TextView canAttend;
//        private TextView notAttend;
        private TextView time;
        private ImageView finishBox;
        private TextView content;
        private View view;

        public InfoHolder(View itemView) {
            super(itemView);
            view = itemView;
            time = itemView.findViewById(R.id.timeTv);
            finishBox = itemView.findViewById(R.id.finishBox);
            content = itemView.findViewById(R.id.content);
            typeTv = itemView.findViewById(R.id.typeTv);
//            confirmStatus = ((TextView) itemView.findViewById(R.id.confirmStatus));
//            reason = ((TextView) itemView.findViewById(R.id.reason));
//            canAttend = ((TextView) itemView.findViewById(R.id.canAttend));
//            notAttend = ((TextView) itemView.findViewById(R.id.notAttend));
        }
    }

    private ScheduleItemClickListener listener;

    public void setListener(ScheduleItemClickListener listener) {
        this.listener = listener;
    }
}
