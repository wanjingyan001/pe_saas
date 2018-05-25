package com.sogukj.pe.module.calendar.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sogukj.pe.Extras;
import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Utils;
import com.sogukj.pe.bean.ProjectMatterCompany;
import com.sogukj.pe.bean.ProjectMatterMD;
import com.sogukj.pe.bean.ScheduleBean;
import com.sogukj.pe.interf.ScheduleItemClickListener;
import com.sogukj.pe.module.calendar.MatterDetailActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by admin on 2017/12/6.
 */

public class ProjectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int DATE = 1, COMPANY = 2, INFO = 3;
    private Context context;
    private List<Object> data;
    private View.OnClickListener listener;
    private ScheduleItemClickListener itemClickListener;


    public ProjectAdapter(Context context, List<Object> data) {
        this.context = context;
        this.data = data;
    }

    public void setListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public void setItemClickListener(ScheduleItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == DATE) {
            return new ProjectHolder(LayoutInflater.from(context).inflate(R.layout.item_project_matters_list, parent, false));
        } else if (viewType == COMPANY) {
            return new MatterHolder(LayoutInflater.from(context).inflate(R.layout.item_matter_list, parent, false));
        } else if (viewType == INFO) {
            return new BeanHolder(LayoutInflater.from(context).inflate(R.layout.item_bean_info, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        Object o = data.get(position);
        if (holder instanceof ProjectHolder) {
            ProjectMatterMD md = (ProjectMatterMD) o;
            try {
                Date parse = new SimpleDateFormat("yyyy-MM-dd").parse(md.getMDTime());
                ((ProjectHolder) holder).MDTime.setText(Utils.getTime(parse, "MM月dd日"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else if (holder instanceof MatterHolder) {
            final ProjectMatterCompany company = (ProjectMatterCompany) o;
            ((MatterHolder) holder).companyName.setText(company.getCompanyName());
            ((MatterHolder) holder).companyDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MatterDetailActivity.class);
                    intent.putExtra(Extras.INSTANCE.getDATA(), company);
                    context.startActivity(intent);
                }
            });
        } else if (holder instanceof BeanHolder) {
            ScheduleBean info = (ScheduleBean) o;
            ((BeanHolder) holder).timeTv.setText(info.getTiming());
            ((BeanHolder) holder).contentTv.setText(info.getTitle());
            if (info.is_collect() != null) {
                //noinspection ConstantConditions
                if (info.is_collect() == 1) {
//                    ((BeanHolder) holder).finishBox.setVisibility(View.INVISIBLE);
                    ((BeanHolder) holder).contentTv.setPaintFlags(
                            ((BeanHolder) holder).contentTv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                } else {
//                    ((BeanHolder) holder).finishBox.setVisibility(View.INVISIBLE);
                    if (info.is_finish() == 1) {
//                        ((BeanHolder) holder).finishBox.setSelected(true);
                        ((BeanHolder) holder).contentTv.setPaintFlags(
                                ((BeanHolder) holder).contentTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
//                        ((BeanHolder) holder).finishBox.setSelected(false);
                        ((BeanHolder) holder).contentTv.setPaintFlags(
                                ((BeanHolder) holder).contentTv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                }
            }
            if (info.getPublisher() != null) {
                ((BeanHolder) holder).icCreater.setVisibility(View.VISIBLE);
                ((BeanHolder) holder).creator.setVisibility(View.VISIBLE);
                ((BeanHolder) holder).creator.setText(info.getPublisher());
            } else {
                ((BeanHolder) holder).icCreater.setVisibility(View.GONE);
                ((BeanHolder) holder).creator.setVisibility(View.GONE);
            }
            ((BeanHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemClickListener.onItemClick(v, position);
                }
            });
//            ((BeanHolder) holder).finishBox.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    ((BeanHolder) holder).finishBox.setSelected(!v.isSelected());
//                    if (v.isSelected()) {
//                        ((BeanHolder) holder).contentTv.setPaintFlags(
//                                ((BeanHolder) holder).contentTv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                    } else {
//                        ((BeanHolder) holder).contentTv.setPaintFlags(
//                                ((BeanHolder) holder).contentTv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
//                    }
//                    itemClickListener.finishCheck(v.isSelected(), position);
//                }
//            });

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (data.get(position) instanceof ProjectMatterCompany) {
            return COMPANY;
        } else if (data.get(position) instanceof ScheduleBean) {
            return INFO;
        } else {
            return INFO;
        }
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    class ProjectHolder extends RecyclerView.ViewHolder {
        private ImageView Img1;
        private ImageView Img2;
        private TextView MDTime;

        public ProjectHolder(View itemView) {
            super(itemView);
            MDTime = itemView.findViewById(R.id.MDTime);
            Img1 = itemView.findViewById(R.id.matters_img1);
            Img2 = itemView.findViewById(R.id.matters_img2);
        }
    }

    class MatterHolder extends RecyclerView.ViewHolder {
        private TextView companyName;
        private ImageView companyDetails;
        private View view;

        public MatterHolder(View itemView) {
            super(itemView);
            view = itemView;
            companyName = itemView.findViewById(R.id.companyName);
            companyDetails = itemView.findViewById(R.id.companyDetails);
        }
    }

    class BeanHolder extends RecyclerView.ViewHolder {
        private ImageView icCreater;
        private TextView creator;
        private View view;
        private TextView timeTv;
        private TextView contentTv;
//        private ImageView finishBox;

        public BeanHolder(View itemView) {
            super(itemView);
            view = itemView;
            timeTv = itemView.findViewById(R.id.timeTv);
            contentTv = itemView.findViewById(R.id.contentTv);
//            finishBox = ((ImageView) itemView.findViewById(R.id.finishBox));
            icCreater = itemView.findViewById(R.id.ic_creater);
            creator = itemView.findViewById(R.id.creator);
        }
    }
}
