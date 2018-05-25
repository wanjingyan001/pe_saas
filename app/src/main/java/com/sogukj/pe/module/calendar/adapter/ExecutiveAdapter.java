package com.sogukj.pe.module.calendar.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.sogukj.pe.R;
import com.sogukj.pe.bean.UserBean;
import com.sogukj.pe.interf.AddPersonListener;
import com.sogukj.pe.peUtils.MyGlideUrl;
import com.sogukj.pe.widgets.CircleImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2017/12/10.
 */

public class ExecutiveAdapter extends RecyclerView.Adapter<ExecutiveAdapter.ExecutiveHolder> {
    private Context context;
    private List<UserBean> exPersons;

    private AddPersonListener listener;

    public void setListener(AddPersonListener listener) {
        this.listener = listener;
    }

    public ExecutiveAdapter(Context context, List<UserBean> exPersons) {
        this.context = context;
        this.exPersons = exPersons;
    }

    @NonNull
    @Override
    public ExecutiveHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExecutiveHolder(LayoutInflater.from(context).inflate(R.layout.item_copy_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ExecutiveHolder holder, int position) {
        if (position == exPersons.size()) {
            holder.userHeader.setImageResource(R.drawable.add_cc_pserson);
            holder.userName.setText("添加");
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.addPerson(ExecutiveAdapter.class.getSimpleName());
                    }
                }
            });
        } else {
            final UserBean userBean = exPersons.get(position);
            holder.userName.setText(userBean.getName());
            if (TextUtils.isEmpty(userBean.getUrl())) {
                char ch = userBean.getName().charAt(0);
                holder.userHeader.setChar(ch);
            } else {
                Glide.with(context)
                        .load(new MyGlideUrl(userBean.getUrl()))
                        .apply(new RequestOptions().error(R.drawable.nim_avatar_default).fallback(R.drawable.nim_avatar_default))
                        .into(holder.userHeader);
            }
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.remove(ExecutiveAdapter.class.getSimpleName(),userBean);
                    }
                    exPersons.remove(userBean);
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return exPersons.size() + 1;
    }

    public void addData(UserBean userBean) {
        if (!exPersons.contains(userBean)) {
            exPersons.add(userBean);
            notifyDataSetChanged();
        }
    }

    public void addAllData(ArrayList<UserBean> selects) {
        exPersons.clear();
        for (UserBean bean : selects) {
            if (!exPersons.contains(bean)) {
                exPersons.add(bean);
                notifyDataSetChanged();
            }
        }
    }

    class ExecutiveHolder extends RecyclerView.ViewHolder {
        private CircleImageView userHeader;
        private TextView userName;
        private View view;

        public ExecutiveHolder(View itemView) {
            super(itemView);
            view = itemView;
            userHeader = itemView.findViewById(R.id.userHeader);
            userName = itemView.findViewById(R.id.userName);
        }
    }
}
