package com.sogukj.pe.module.im;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.sogukj.pe.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2018/1/16.
 */

public class TeamMenuWindow extends PopupWindow {
    private RecyclerView conditionsList;
    private List<String> titles = new ArrayList<>();
    private int[] icons = {R.drawable.wd, R.drawable.ysb, R.drawable.xls, R.drawable.txt, R.drawable.icon_pdf, R.drawable.qt};
    private Context context;
    private View inflate;
    private onItemClickListener listener;

    public TeamMenuWindow(Context context) {
        super(context);
        init(context);
        setPopWindow();
    }

    public TeamMenuWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setPopWindow();
    }

    public void setListener(onItemClickListener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        this.context = context;
        inflate = LayoutInflater.from(context).inflate(R.layout.layout_team_menu_winodow, null);
        conditionsList = inflate.findViewById(R.id.filter_conditions);
        conditionsList.setLayoutManager(new GridLayoutManager(context, 3));
        titles.add("DOC");
        titles.add("压缩包");
        titles.add("XLS");
        titles.add("TXT");
        titles.add("PDF");
        titles.add("其他");
        FilterAdapter adapter = new FilterAdapter(titles);
        conditionsList.setAdapter(adapter);
    }

    private void setPopWindow() {
        this.setContentView(inflate);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        this.setOutsideTouchable(true);
    }


    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    private void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
        backgroundAlpha((Activity) context, 0.5f);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        backgroundAlpha((Activity) context, 1f);
    }

    class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder> {
        private List<String> titles;

        public FilterAdapter(List<String> titles) {
            this.titles = titles;
        }

        @Override
        public FilterHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FilterHolder(LayoutInflater.from(context).inflate(R.layout.item_team_filter_menu, parent, false));
        }

        @Override
        public void onBindViewHolder(FilterHolder holder, final int position) {
            holder.filterTv.setText(titles.get(position));
            holder.filterIcon.setImageResource(icons[position]);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onItemClick(position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return titles.size();
        }

        class FilterHolder extends RecyclerView.ViewHolder {
            private ImageView filterIcon;
            private TextView filterTv;
            private View view;

            public FilterHolder(View itemView) {
                super(itemView);
                view = itemView;
                filterTv = itemView.findViewById(R.id.filter_text);
                filterIcon = itemView.findViewById(R.id.filter_icon);
            }
        }
    }

    interface onItemClickListener{
        void onItemClick(int position);
    }

}
