package com.netease.nim.uikit.business.session.viewholder;

import android.annotation.SuppressLint;
import android.graphics.drawable.AnimationDrawable;
import android.support.constraint.ConstraintLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.activity.BaseMessageActivity;
import com.netease.nim.uikit.business.session.audio.MessageAudioControl;
import com.netease.nim.uikit.common.media.audioplayer.Playable;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * Created by admin on 2018/8/31.
 */

public class MsgViewHolderAudio2 extends MsgViewHolderBase {
    public static final int CLICK_TO_PLAY_AUDIO_DELAY = 500;
    private ConstraintLayout itemLayout;//整体控件容器
    private LinearLayout audioContent;//内容控件容器
    private TextView leftDuration;//左边的语音时间
    private ImageView leftAnim;//左边的动画
    private TextView audioTranText;//语音文字
    private ImageView rightAnim;//右边的动画
    private TextView rightDuration;//右边的语音时间
    private ImageView unreadIndicator;

    private MessageAudioControl audioControl;

    public MsgViewHolderAudio2(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.layout_saas_audio_message;
    }

    @Override
    protected void inflateContentView() {
        itemLayout = findViewById(R.id.audio_content_layout);
        leftDuration = findViewById(R.id.audio_duration_left);
        audioContent = findViewById(R.id.message_item_audio_container);
        leftAnim = findViewById(R.id.audio_animation_left);
        audioTranText = findViewById(R.id.audio_tran_text);
        rightAnim = findViewById(R.id.audio_animation_right);
        rightDuration = findViewById(R.id.audio_duration_right);
        unreadIndicator = findViewById(R.id.audio_unread_indicator);
        audioControl = MessageAudioControl.getInstance(context);
    }

    @Override
    protected void bindContentView() {
        initView();
        refreshStatus();
        controlPlaying();
    }


    @Override
    protected void onItemClick() {
        super.onItemClick();
        if (audioControl != null) {
            if (message.getDirect() == MsgDirectionEnum.In && message.getAttachStatus() != AttachStatusEnum.transferred) {
                return;
            }

            if (message.getStatus() != MsgStatusEnum.read) {
                // 将未读标识去掉,更新数据库
                unreadIndicator.setVisibility(View.GONE);
            }

            initPlayAnim(); // 设置语音播放动画

            audioControl.startPlayAudioDelay(CLICK_TO_PLAY_AUDIO_DELAY, message, onPlayListener);

            audioControl.setPlayNext(!NimUIKitImpl.getOptions().disableAutoPlayNextAudio, adapter, message);
        }
    }

    private void initView() {
        if (isReceivedMessage()) {
            leftDuration.setVisibility(View.GONE);
            leftAnim.setVisibility(View.VISIBLE);
            rightAnim.setVisibility(View.GONE);
            rightDuration.setVisibility(View.VISIBLE);
            audioContent.setBackgroundResource(NimUIKitImpl.getOptions().messageLeftBackground);
        } else {
            leftDuration.setVisibility(View.VISIBLE);
            leftAnim.setVisibility(View.GONE);
            rightAnim.setVisibility(View.VISIBLE);
            rightDuration.setVisibility(View.GONE);
            audioContent.setBackgroundResource(NimUIKitImpl.getOptions().messageRightBackground);
        }
        if (message.getContent() != null) {
            audioTranText.setText(message.getContent());
        } else {
            if (context instanceof BaseMessageActivity) {

            }
        }
    }

    private void refreshStatus() {// 消息状态
        AudioAttachment attachment = (AudioAttachment) message.getAttachment();
        MsgStatusEnum status = message.getStatus();
        AttachStatusEnum attachStatus = message.getAttachStatus();

        // alert button
        if (TextUtils.isEmpty(attachment.getPath())) {
            if (attachStatus == AttachStatusEnum.fail || status == MsgStatusEnum.fail) {
                alertButton.setVisibility(View.VISIBLE);
            } else {
                alertButton.setVisibility(View.GONE);
            }
        }

        // progress bar indicator
        if (status == MsgStatusEnum.sending || attachStatus == AttachStatusEnum.transferring) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }

        // unread indicator
        if (!NimUIKitImpl.getOptions().disableAudioPlayedStatusIcon
                && isReceivedMessage()
                && attachStatus == AttachStatusEnum.transferred
                && status != MsgStatusEnum.read) {
            unreadIndicator.setVisibility(View.VISIBLE);
        } else {
            unreadIndicator.setVisibility(View.GONE);
        }
    }

    private void controlPlaying() {
        final AudioAttachment msgAttachment = (AudioAttachment) message.getAttachment();
        long duration = msgAttachment.getDuration();
        leftDuration.setTag(message.getUuid());
        rightDuration.setTag(message.getUuid());

        if (!isMessagePlaying(audioControl, message)) {
            if (audioControl.getAudioControlListener() != null &&
                    audioControl.getAudioControlListener().equals(onPlayListener)) {
                audioControl.changeAudioControlListener(null);
            }

            updateTime(duration);
            if (isReceivedMessage()) {
                stop(leftAnim);
            } else {
                stop(rightAnim);
            }
        } else {
            audioControl.changeAudioControlListener(onPlayListener);
            if (isReceivedMessage()) {
                play(leftAnim);
            } else {
                play(rightAnim);
            }
        }
    }

    private boolean isMessagePlaying(MessageAudioControl audioControl, IMMessage message) {
        return audioControl.getPlayingAudio() != null && audioControl.getPlayingAudio().isTheSame(message);
    }

    @SuppressLint("SetTextI18n")
    private void updateTime(long milliseconds) {
        long seconds = TimeUtil.getSecondsByMilliseconds(milliseconds);
        String minute = TimeUtil.change(seconds);
        if (seconds >= 0) {
            leftDuration.setText(minute + " ");
            rightDuration.setText(minute + " ");
        } else {
            leftDuration.setText("");
            rightDuration.setText("");
        }
    }

    private MessageAudioControl.AudioControlListener onPlayListener = new MessageAudioControl.AudioControlListener() {

        @Override
        public void updatePlayingProgress(Playable playable, long curPosition) {
            if (!isTheSame(message.getUuid())) {
                return;
            }

            if (curPosition > playable.getDuration()) {
                return;
            }
            updateTime(curPosition);
        }

        @Override
        public void onAudioControllerReady(Playable playable) {
            if (!isTheSame(message.getUuid())) {
                return;
            }
            if (isReceivedMessage()) {
                play(leftAnim);
            } else {
                play(rightAnim);
            }

        }

        @Override
        public void onEndPlay(Playable playable) {
            if (!isTheSame(message.getUuid())) {
                return;
            }

            updateTime(playable.getDuration());
            if (isReceivedMessage()) {
                stop(leftAnim);
            } else {
                stop(rightAnim);
            }
        }


    };

    private void play(ImageView animationView) {
        if (animationView.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) animationView.getBackground();
            animation.start();
        }
    }

    private void stop(ImageView animationView) {
        if (animationView.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) animationView.getBackground();
            animation.stop();

            endPlayAnim();
        }
    }

    private void initPlayAnim() {
        if (isReceivedMessage()) {
            leftAnim.setBackgroundResource(R.drawable.nim_audio_animation_list_left);
        } else {
            rightAnim.setBackgroundResource(R.drawable.nim_audio_animation_list_right);
        }
    }

    private void endPlayAnim() {
        if (isReceivedMessage()) {
            leftAnim.setBackgroundResource(R.drawable.nim_audio_animation_list_left_3);
        } else {
            rightAnim.setBackgroundResource(R.drawable.nim_audio_animation_list_right_3);
        }
    }

    private boolean isTheSame(String uuid) {
        String current = leftDuration.getTag().toString();
        return !TextUtils.isEmpty(uuid) && uuid.equals(current);
    }

    @Override
    protected int leftBackground() {
        return 0;
    }

    @Override
    protected int rightBackground() {
        return 0;
    }
}
