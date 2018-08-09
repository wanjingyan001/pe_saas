package com.sogukj.pe.module.im;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.sogukj.pe.Extras;
import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Utils;
import com.sogukj.pe.bean.ChatFileBean;
import com.sogukj.pe.peUtils.FileTypeUtils;
import com.sogukj.pe.service.ImService;
import com.sogukj.pe.service.Payload;
import com.sogukj.service.SoguApi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import qdx.stickyheaderdecoration.NormalDecoration;

public class TeamHistoryFileActivity extends AppCompatActivity implements TeamMenuWindow.onItemClickListener {
    public static int Month = 1;
    public static int File = 2;
    private Toolbar toolbar;
    private RecyclerView historyList;
    private TeamMenuWindow window;
    private static int type = 8;//(1=>图片，2=>视频，3=>压缩包，4=>Excel档，5=>TXT，6=>PDF，7=>DOC，8=>全部，9=>其他)
    private int tid;
    private HistoryFileAdapter adapter;
    private List<ChatFileBean> files = new ArrayList<>();
    private ImageView iv_empty, iv_loading;

    public static void start(Context context, int tid) {
        Intent intent = new Intent(context, TeamHistoryFileActivity.class);
        intent.putExtra(Extras.INSTANCE.getID(), tid);
        context.startActivity(intent);
    }

    public static void start(Context context, int tid, int type) {
        Intent intent = new Intent(context, TeamHistoryFileActivity.class);
        intent.putExtra(Extras.INSTANCE.getID(), tid);
        intent.putExtra(Extras.INSTANCE.getTYPE(), type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_history_file);
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff);
        toolbar = findViewById(R.id.team_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        TextView title = findViewById(R.id.team_tool);
        toolbar.setNavigationIcon(R.drawable.sogu_ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        window = new TeamMenuWindow(this);
        window.setListener(this);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                window.showAsDropDown(toolbar);
                return true;
            }
        });
        tid = getIntent().getIntExtra(Extras.INSTANCE.getID(), 0);
        type = getIntent().getIntExtra(Extras.INSTANCE.getTYPE(), 8);
        if (type == 2) {
            title.setText("视频");
        }
        historyList = findViewById(R.id.history_file_list);
        iv_empty = findViewById(R.id.iv_empty);
        historyList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryFileAdapter(files);
        NormalDecoration decoration = new NormalDecoration() {
            @Override
            public String getHeaderName(int i) {
                String s = files.get(i).getTime().substring(0, 8);
                s = s.replaceFirst("/", "年");
                s = s.replace("/", "月");
                return s;
            }
        };
        historyList.addItemDecoration(decoration);
        historyList.setAdapter(adapter);

        iv_loading = findViewById(R.id.iv_loading);
        Glide.with(this)
                .asGif()
                .load(Uri.parse("file:///android_asset/img_loading_xh.gif"))
                .into(iv_loading);
        iv_loading.setVisibility(View.VISIBLE);

        requestChatFile();
        //暂时去掉,以后还要加回来
//        adapter.setListener(new onItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                IMDialog dialog = new IMDialog(TeamHistoryFileActivity.this);
//                dialog.setTitle(files.get(position).getFile_name());
//                dialog.setOnItemClickListener(new IMDialog.IMItemClickListener() {
//
//                    @Override
//                    public void itemClick(int position) {
//                        switch (position) {
//                            case 1:
//                                TeamSelectActivity.Companion.startForResult(TeamHistoryFileActivity.this,
//                                        true, null, null, false, null);
//                                break;
//                            case 2:
//
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                });
//                dialog.show();
//            }
//        });

    }

    private void requestChatFile() {
        SoguApi.Companion.getService(getApplication(), ImService.class)
                .chatFile(type, tid)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Payload<List<ChatFileBean>>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Payload<List<ChatFileBean>> listPayload) {
                        iv_loading.setVisibility(View.GONE);
                        Log.d("WJY", new Gson().toJson(listPayload));
                        if (listPayload.getPayload() != null && !listPayload.getPayload().isEmpty()) {
                            files.clear();
                            files.addAll(listPayload.getPayload());
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        iv_loading.setVisibility(View.GONE);
                        if (adapter.getItemCount() == 0) {
                            iv_empty.setVisibility(View.VISIBLE);
                            historyList.setVisibility(View.GONE);
                        } else {
                            iv_empty.setVisibility(View.GONE);
                            historyList.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onComplete() {
                        iv_loading.setVisibility(View.GONE);
                        if (adapter.getItemCount() == 0) {
                            iv_empty.setVisibility(View.VISIBLE);
                            historyList.setVisibility(View.GONE);
                        } else {
                            iv_empty.setVisibility(View.GONE);
                            historyList.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (type != 5 && type != 2) {
            getMenuInflater().inflate(R.menu.team_filter, menu);
        }
        return true;
    }

    @Override
    public void onItemClick(int position) {
        switch (position) {
            case 0:
                type = 7;
                break;
            case 1:
                type = 3;
                break;
            case 2:
                type = 4;
                break;
            case 3:
                type = 5;
                break;
            case 4:
                type = 6;
                break;
            case 5:
                type = 9;
                break;
            default:
                break;
        }
        window.dismiss();
        requestChatFile();
    }

    class HistoryFileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<ChatFileBean> datas;
        private onItemClickListener listener;

        public HistoryFileAdapter(List<ChatFileBean> datas) {
            this.datas = datas;
        }

        public void setListener(onItemClickListener listener) {
            this.listener = listener;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FileHolder(LayoutInflater.from(TeamHistoryFileActivity.this)
                    .inflate(R.layout.item_history_child, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof FileHolder) {
                ChatFileBean fileBean = datas.get(position);
                if (listener != null) {
                    ((FileHolder) holder).view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onItemClick(v, position);
                        }
                    });
                }
                ((FileHolder) holder).fileName.setText(fileBean.getFile_name());
                ((FileHolder) holder).fileInfo.setText(fileBean.getSize() + "   " + fileBean.getUser_name() + "   " + fileBean.getTime());
                ((FileHolder) holder).fileIcon.setImageResource(FileTypeUtils.getFileType(new File(fileBean.getFile_name())).getIcon());
//                (1=>图片，2=>视频，3=>压缩包，4=>Excel档，5=>TXT，6=>PDF，7=>DOC，8=>全部，9=>其他)
//                switch (fileBean.getType()) {
//                    case 3:
//                        break;
//                    case 4:
//                        break;
//                    case 5:
//                        break;
//                    case 6:
//                        break;
//                    case 7:
//                        break;
//                    case 9:
//                        break;
//                    default:
//                        break;
//                }
            }
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }


        class FileHolder extends RecyclerView.ViewHolder {
            private TextView fileName;
            private TextView fileInfo;
            private ImageView fileIcon;
            private View view;

            public FileHolder(View itemView) {
                super(itemView);
                view = itemView;
                fileIcon = itemView.findViewById(R.id.file_img);
                fileName = itemView.findViewById(R.id.file_name);
                fileInfo = itemView.findViewById(R.id.file_info);
            }
        }
    }

    interface onItemClickListener {
        void onItemClick(View view, int position);
    }

}
