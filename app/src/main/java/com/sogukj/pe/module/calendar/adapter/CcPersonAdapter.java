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
 * Created by admin on 2017/12/8.
 */

public class CcPersonAdapter extends RecyclerView.Adapter<CcPersonAdapter.CCHolder> {
    private Context context;
    private List<UserBean> ccPersons;

    public CcPersonAdapter(Context context, List<UserBean> ccPersons) {
        this.context = context;
        this.ccPersons = ccPersons;
    }

    @NonNull
    @Override
    public CCHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CCHolder(LayoutInflater.from(context).inflate(R.layout.item_copy_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CCHolder holder, final int position) {
        if (position == ccPersons.size()) {
            holder.userHeader.setImageResource(R.drawable.add_cc_pserson);
            holder.userName.setText("添加");
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.addPerson(CcPersonAdapter.class.getSimpleName());
                    }
                }
            });

        } else {
            final UserBean userBean = ccPersons.get(position);
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
                        listener.remove(CcPersonAdapter.class.getSimpleName(),userBean);
                    }
                    ccPersons.remove(userBean);
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return ccPersons.size() + 1;
    }

    public void addData(UserBean userBean) {
        if (!ccPersons.contains(userBean)) {
            ccPersons.add(userBean);
            notifyDataSetChanged();
        }
    }

    public void addAllData(ArrayList<UserBean> selects) {
        ccPersons.clear();
        for (UserBean bean : selects) {
            if (!ccPersons.contains(bean)) {
                ccPersons.add(bean);
                notifyDataSetChanged();
            }
        }
    }


    private AddPersonListener listener;

    public void setListener(AddPersonListener listener) {
        this.listener = listener;
    }

    class CCHolder extends RecyclerView.ViewHolder {
        private CircleImageView userHeader;
        private TextView userName;
        private View view;

        public CCHolder(View itemView) {
            super(itemView);
            view = itemView;
            userHeader = itemView.findViewById(R.id.userHeader);
            userName = itemView.findViewById(R.id.userName);
        }
    }
}
