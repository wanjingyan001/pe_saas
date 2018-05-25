package com.sogukj.pe.module.im;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.lucene.LuceneService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.search.model.MsgIndexRecord;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Utils;
import com.sogukj.pe.widgets.CircleImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TeamSearchActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText searchEdt;
    private TextView searchHint;
    private ImageView searchIcon;
    private TextView tv_cancel;
    private ConstraintLayout quickSearchLayout;
    private ImageView searchPic;
    private ImageView searchFile;
    private ImageView searchLink;
    private LinearLayout resultLayout;
    private TextView resultNumber;
    private RecyclerView searchResultList;
    private String searchKey;
    private String sessionId;
    private List<MsgIndexRecord> datas = new ArrayList<>();
    private SearchAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_search);
        Utils.setWindowStatusBarColor(this, R.color.color_blue_0888ff);
        searchEdt = findViewById(R.id.search_edt);
        searchHint = findViewById(R.id.search_hint);
        searchIcon = findViewById(R.id.search_icon);
        tv_cancel = findViewById(R.id.tv_cancel);
        quickSearchLayout = findViewById(R.id.quick_search_layout);
        searchPic = findViewById(R.id.search_pic);
        searchFile = findViewById(R.id.search_file);
        searchLink = findViewById(R.id.search_link);
        resultLayout = findViewById(R.id.result_layout);
        resultNumber = findViewById(R.id.result_number);
        searchResultList = findViewById(R.id.search_result_list);
        sessionId = getIntent().getStringExtra("sessionId");

        searchEdt.setFilters(Utils.getFilter(this));
        searchEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    searchHint.setVisibility(View.GONE);
                    searchIcon.setVisibility(View.VISIBLE);
                } else {
                    searchHint.setVisibility(View.GONE);
                    searchIcon.setVisibility(View.VISIBLE);
                }
            }
        });
        searchEdt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    boolean isOpen = imm.isActive();
                    if (isOpen) {
                        imm.hideSoftInputFromWindow(searchEdt.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    searchKey = searchEdt.getText().toString();
                    searchSession();
                    return true;
                }
                return false;
            }
        });
        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchEdt.getText().toString().isEmpty()) {
                    quickSearchLayout.setVisibility(View.VISIBLE);
                    resultLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchPic.setOnClickListener(this);
        searchFile.setOnClickListener(this);
        searchLink.setOnClickListener(this);
        tv_cancel.setOnClickListener(this);

        adapter = new SearchAdapter(datas);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));
        searchResultList.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.search_pic) {
            TeamPictureActivity.start(this, Integer.valueOf(sessionId));
        } else if (id == R.id.search_file) {
            TeamHistoryFileActivity.start(this, Integer.valueOf(sessionId));
        } else if (id == R.id.search_link) {
            TeamHistoryFileActivity.start(this, Integer.valueOf(sessionId), 2);
        } else if (id == R.id.tv_cancel) {
            if (!searchEdt.getText().toString().isEmpty()){
                searchEdt.setText("");
            }else {
                finish();
            }
        }
    }

    private void searchSession() {
        if (searchKey.isEmpty()) {
            return;
        }
        NIMClient.getService(LuceneService.class).searchSession(searchKey, SessionTypeEnum.Team, sessionId)
                .setCallback(new RequestCallback<List<MsgIndexRecord>>() {
                    @Override
                    public void onSuccess(List<MsgIndexRecord> msgIndexRecords) {
                        resultNumber.setText(String.format("共%d条与\"%s\"相关的聊天记录", msgIndexRecords.size(), searchKey));
                        quickSearchLayout.setVisibility(View.GONE);
                        resultLayout.setVisibility(View.VISIBLE);
                        adapter.addData(msgIndexRecords);
                    }

                    @Override
                    public void onFailed(int i) {
                        Toast.makeText(TeamSearchActivity.this, "错误", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onException(Throwable throwable) {

                    }
                });
    }

    class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchHolder> {
        List<MsgIndexRecord> msgIndexRecords;

        public SearchAdapter(List<MsgIndexRecord> msgIndexRecords) {
            this.msgIndexRecords = msgIndexRecords;
        }

        public void addData(List<MsgIndexRecord> data) {
            msgIndexRecords.clear();
            msgIndexRecords.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public SearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(TeamSearchActivity.this).inflate(R.layout.item_search_result, parent, false);
            return new SearchHolder(inflate);
        }

        @SuppressLint("CheckResult")
        @Override
        public void onBindViewHolder(SearchHolder holder, int position) {
            final MsgIndexRecord record = msgIndexRecords.get(position);
            final IMMessage message = record.getMessage();
            UserInfo userInfo = NimUIKit.getUserInfoProvider().getUserInfo(message.getFromAccount());
            holder.resultName.setText(message.getFromNick());
            String text = record.getText();
            text = text.replace(searchKey, "<font color='#1787fb'>" + searchKey + "</font>");
            holder.resultContent.setText(Html.fromHtml(text));
            RequestOptions options = new RequestOptions();
            options.error(R.drawable.nim_avatar_default)
                    .fallback(R.drawable.nim_avatar_default);
            if(userInfo.getAvatar() == null || userInfo.getAvatar().equals("")){
                holder.resultAvatar.setChar(userInfo.getName().charAt(0));
            } else {
                Glide.with(TeamSearchActivity.this)
                        .load(userInfo.getAvatar())
                        .apply(options)
                        .into(holder.resultAvatar);
            }
            String format = new SimpleDateFormat("yyyy/MM/dd").format(new Date(record.getTime()));
            holder.resultTime.setText(format);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //需要跳转到一个新页面,TeamMessageActivity会默认移动到最下面
                    DisplayMessageActivity.Companion.start(TeamSearchActivity.this, message);
                }
            });
        }

        @Override
        public int getItemCount() {
            return msgIndexRecords.size();
        }

        class SearchHolder extends RecyclerView.ViewHolder {
            private CircleImageView resultAvatar;
            private TextView resultName;
            private TextView resultContent;
            private TextView resultTime;
            private View view;

            public SearchHolder(View itemView) {
                super(itemView);
                view = itemView;
                resultAvatar = itemView.findViewById(R.id.result_avatar);
                resultName = itemView.findViewById(R.id.result_name);
                resultContent = itemView.findViewById(R.id.result_content);
                resultTime = itemView.findViewById(R.id.result_time);

            }
        }
    }
}
