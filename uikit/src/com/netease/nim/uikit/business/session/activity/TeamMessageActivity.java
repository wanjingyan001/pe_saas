package com.netease.nim.uikit.business.session.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.contact.ContactChangedObserver;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.nim.uikit.api.model.team.TeamMemberDataChangedObserver;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.fragment.MessageFragment;
import com.netease.nim.uikit.business.session.fragment.TeamMessageFragment;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.util.log.sdk.util.FileUtils;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 群聊界面
 * <p/>
 * Created by huangjun on 2015/3/5.
 */
public class TeamMessageActivity extends BaseMessageActivity {

    // model
    private Team team;

    private View invalidTeamTipView;

    private TextView invalidTeamTipText;

    private TeamMessageFragment fragment;

    private Class<? extends Activity> backToClass;
    private TextView bigTitle;
    private TextView smallTitle;
    private String flag;
    private ImageView iv_personal;

    public static void start(Context context, String tid, SessionCustomization customization,
                             Class<? extends Activity> backToClass, IMMessage anchor) {
        Intent intent = new Intent();
        intent.putExtra(Extras.EXTRA_ACCOUNT, tid);
        intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization);
        intent.putExtra(Extras.EXTRA_BACK_TO_CLASS, backToClass);
        if (anchor != null) {
            intent.putExtra(Extras.EXTRA_ANCHOR, anchor);
        }
        intent.setClass(context, TeamMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }

    public static void start(Context context, String tid, SessionCustomization customization,
                             Class<? extends Activity> backToClass, IMMessage anchor, String tip) {
        Intent intent = new Intent();
        intent.putExtra(Extras.EXTRA_ACCOUNT, tid);
        intent.putExtra(Extras.EXTRA_CUSTOMIZATION, customization);
        intent.putExtra(Extras.EXTRA_BACK_TO_CLASS, backToClass);
        intent.putExtra(Extras.EXTRA_TIP, tip);
        if (anchor != null) {
            intent.putExtra(Extras.EXTRA_ANCHOR, anchor);
        }
        intent.setClass(context, TeamMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
    }

    protected void findViews() {
        invalidTeamTipView = findView(R.id.invalid_team_tip);
        invalidTeamTipText = findView(R.id.invalid_team_text);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileUtils.setWindowStatusBarColor(this, R.color.color_blue_0888ff);
        backToClass = (Class<? extends Activity>) getIntent().getSerializableExtra(Extras.EXTRA_BACK_TO_CLASS);
        findViews();

        registerTeamUpdateObserver(true);

        iv_personal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != customization) {
                    customization.personalButton.onClick(TeamMessageActivity.this, v, sessionId);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        registerTeamUpdateObserver(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestTeamInfo();
        // 以测试帐号 testAccount，查询100条消息
    }

    /**
     * 请求群基本信息
     */
    private void requestTeamInfo() {
        // 请求群基本信息
        Team t = NimUIKit.getTeamProvider().getTeamById(sessionId);
        if (t != null) {
            updateTeamInfo(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(sessionId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateTeamInfo(result);
                    } else {
                        onRequestTeamInfoFailed();
                    }
                }
            });
        }
    }

    private void onRequestTeamInfoFailed() {
        Toast.makeText(TeamMessageActivity.this, "获取群组信息失败!", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * 更新群信息
     *
     * @param d
     */
    private void updateTeamInfo(final Team d) {
        if (d == null) {
            return;
        }

        team = d;
        fragment.setTeam(team);
        setTitle("");
        bigTitle.setText(team.getName());
        if (team.getExtension() == null || team.getExtension().isEmpty()) {
            smallTitle.setVisibility(View.GONE);
        } else {
            try {
                smallTitle.setVisibility(View.VISIBLE);
                JSONObject object = new JSONObject(team.getExtension());
                smallTitle.setText(object.getString("project_name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (null != team.getExtServer() && !team.getExtServer().isEmpty()) {
            try {
                JSONObject object = new JSONObject(team.getExtServer());
                flag = object.getString("grouptype");
                if ("0".equals(flag)) {
                    //全员
                    setDrawable(R.mipmap.ic_flag_wqy, bigTitle);
                } else if ("1".equals(flag)) {
                    //部门
                    setDrawable(R.mipmap.ic_flag_wbm, bigTitle);
                } else {
                    //内部
                    setDrawable(R.mipmap.ic_flag_wnb, bigTitle);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //内部
            setDrawable(R.mipmap.ic_flag_wnb, bigTitle);
        }

//        setTitle(team == null ? sessionId : team.getName() + "(" + team.getMemberCount() + "人)");

        invalidTeamTipText.setText(team.getType() == TeamTypeEnum.Normal ? R.string.normal_team_invalid_tip : R.string.team_invalid_tip);
        invalidTeamTipView.setVisibility(team.isMyTeam() ? View.GONE : View.VISIBLE);
    }

    private void setDrawable(int drawableId, TextView textView) {
        Drawable drawable = getDrawable(drawableId);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        textView.setCompoundDrawables(null, null, drawable, null);
    }

    /**
     * 注册群信息更新监听
     *
     * @param register
     */
    private void registerTeamUpdateObserver(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataChangedObserver, register);
        NimUIKit.getTeamChangedObservable().registerTeamMemberDataChangedObserver(teamMemberDataChangedObserver, register);
        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register);
    }

    /**
     * 群资料变动通知和移除群的通知（包括自己退群和群被解散）
     */
    TeamDataChangedObserver teamDataChangedObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            if (team == null) {
                return;
            }
            for (Team t : teams) {
                if (t.getId().equals(team.getId())) {
                    updateTeamInfo(t);
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {
            if (team == null) {
                return;
            }
            if (team.getId().equals(TeamMessageActivity.this.team.getId())) {
                updateTeamInfo(team);
            }
        }
    };

    /**
     * 群成员资料变动通知和移除群成员通知
     */
    TeamMemberDataChangedObserver teamMemberDataChangedObserver = new TeamMemberDataChangedObserver() {

        @Override
        public void onUpdateTeamMember(List<TeamMember> members) {
            fragment.refreshMessageList();
        }

        @Override
        public void onRemoveTeamMember(List<TeamMember> member) {
        }
    };

    ContactChangedObserver friendDataChangedObserver = new ContactChangedObserver() {
        @Override
        public void onAddedOrUpdatedFriends(List<String> accounts) {
            fragment.refreshMessageList();
        }

        @Override
        public void onDeletedFriends(List<String> accounts) {
            fragment.refreshMessageList();
        }

        @Override
        public void onAddUserToBlackList(List<String> account) {
            fragment.refreshMessageList();
        }

        @Override
        public void onRemoveUserFromBlackList(List<String> account) {
            fragment.refreshMessageList();
        }
    };

    @Override
    protected MessageFragment fragment() {
        // 添加fragment
        Bundle arguments = getIntent().getExtras();
        arguments.putSerializable(Extras.EXTRA_TYPE, SessionTypeEnum.Team);
        arguments.putString(Extras.EXTRA_TIP, getIntent().getStringExtra(Extras.EXTRA_TIP));
        IMMessage value = (IMMessage) getIntent().getSerializableExtra(Extras.EXTRA_ANCHOR);
        if (value != null) {
            arguments.putSerializable(Extras.EXTRA_ANCHOR, value);
        }
        if (flag != null) {
            arguments.putString(Extras.EXTRA_FLAG, flag);
        }
        fragment = new TeamMessageFragment();
        fragment.setArguments(arguments);
        fragment.setContainerId(R.id.message_fragment_container);
        return fragment;
    }

    @Override
    protected int getContentViewId() {
        return R.layout.nim_team_message_activity;
    }

    @Override
    protected void initToolBar() {
        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);
        bigTitle = ((TextView) findViewById(R.id.bigTitle));
        smallTitle = (TextView) findViewById(R.id.smallTitle);
        iv_personal = findViewById(R.id.iv_personal);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (backToClass != null) {
            Intent intent = new Intent();
            intent.setClass(this, backToClass);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }
}
