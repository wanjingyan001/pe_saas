package com.netease.nim.uikit.business.session.viewholder;

import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.audio.MessageAudioControl;
import com.netease.nim.uikit.common.media.audioplayer.Playable;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * Created by zhoujianghua on 2015/8/5.
 */
public class MsgViewHolderAudio extends MsgViewHolderBase {


    private View content;

    public MsgViewHolderAudio(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    public static final int CLICK_TO_PLAY_AUDIO_DELAY = 500;

    private TextView durationLabel;
    private TextView durationLabel1;
    private View containerView;
    private View unreadIndicator;
    private ImageView animationView;

    private MessageAudioControl audioControl;

    @Override
    protected int getContentResId() {
        return R.layout.nim_message_item_audio;
    }

    @Override
    protected void inflateContentView() {
        content = findViewById(R.id.audio_content_layout);
        durationLabel = findViewById(R.id.message_item_audio_duration);
        durationLabel1 = findViewById(R.id.message_item_audio_duration1);
        containerView = findViewById(R.id.message_item_audio_container);
        unreadIndicator = findViewById(R.id.message_item_audio_unread_indicator);
        animationView = findViewById(R.id.message_item_audio_playing_animation);
        animationView.setBackgroundResource(0);
        audioControl = MessageAudioControl.getInstance(context);
    }

    @Override
    protected void bindContentView() {
        layoutByDirection();

        refreshStatus();

        controlPlaying();
    }

    @Override
    protected void onItemClick() {
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

    private void layoutByDirection() {
        if (isReceivedMessage()) {
            setGravity(animationView, Gravity.LEFT | Gravity.CENTER_VERTICAL);
            content.setBackgroundResource(NimUIKitImpl.getOptions().messageLeftBackground);
            content.setPadding(ScreenUtil.dip2px(15), ScreenUtil.dip2px(8), ScreenUtil.dip2px(10), ScreenUtil.dip2px(8));
            animationView.setBackgroundResource(R.drawable.nim_audio_animation_list_left);
            durationLabel.setVisibility(View.GONE);
            durationLabel1.setVisibility(View.VISIBLE);
            durationLabel.setTextColor(Color.parseColor("#282828"));

        } else {
            setGravity(animationView, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            unreadIndicator.setVisibility(View.GONE);
            content.setBackgroundResource(NimUIKitImpl.getOptions().messageRightBackground);
            content.setPadding(ScreenUtil.dip2px(10), ScreenUtil.dip2px(8), ScreenUtil.dip2px(15), ScreenUtil.dip2px(8));
            animationView.setBackgroundResource(R.drawable.nim_audio_animation_list_right);
            durationLabel1.setVisibility(View.GONE);
            durationLabel.setVisibility(View.VISIBLE);
            durationLabel1.setTextColor(Color.parseColor("#282828"));
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
        setAudioBubbleWidth(duration);

        durationLabel.setTag(message.getUuid());
        durationLabel1.setTag(message.getUuid());

        if (!isMessagePlaying(audioControl, message)) {
            if (audioControl.getAudioControlListener() != null &&
                    audioControl.getAudioControlListener().equals(onPlayListener)) {
                audioControl.changeAudioControlListener(null);
            }

            updateTime(duration);
            stop();
        } else {
            audioControl.changeAudioControlListener(onPlayListener);
            play();
        }
    }

    public static int getAudioMaxEdge() {
        return (int) (0.72 * ScreenUtil.screenMin);
    }

    public static int getAudioMinEdge() {
        return (int) (0.13 * ScreenUtil.screenMin);
    }

    private void setAudioBubbleWidth(long milliseconds) {
        long seconds = TimeUtil.getSecondsByMilliseconds(milliseconds);

        int currentBubbleWidth = calculateBubbleWidth(seconds, NimUIKitImpl.getOptions().audioRecordMaxTime);
        ViewGroup.LayoutParams layoutParams = containerView.getLayoutParams();
        layoutParams.width = currentBubbleWidth;
        containerView.setLayoutParams(layoutParams);
    }

    private int calculateBubbleWidth(long seconds, int MAX_TIME) {
        int maxAudioBubbleWidth = getAudioMaxEdge();
        int minAudioBubbleWidth = getAudioMinEdge();

        int currentBubbleWidth;
        if (seconds <= 0) {
            currentBubbleWidth = minAudioBubbleWidth;
        } else if (seconds > 0 && seconds <= MAX_TIME) {
            currentBubbleWidth = (int) ((maxAudioBubbleWidth - minAudioBubbleWidth) * (2.0 / Math.PI)
                    * Math.atan(seconds / 10.0) + minAudioBubbleWidth);
        } else {
            currentBubbleWidth = maxAudioBubbleWidth;
        }

        if (currentBubbleWidth < minAudioBubbleWidth) {
            currentBubbleWidth = minAudioBubbleWidth;
        } else if (currentBubbleWidth > maxAudioBubbleWidth) {
            currentBubbleWidth = maxAudioBubbleWidth;
        }

        return currentBubbleWidth;
    }

    private void updateTime(long milliseconds) {
        long seconds = TimeUtil.getSecondsByMilliseconds(milliseconds);
        String minute = TimeUtil.change(seconds);
        if (seconds >= 0) {
            durationLabel.setText(minute + " ");
            durationLabel1.setText(minute + " ");
        } else {
            durationLabel.setText("");
            durationLabel1.setText("");
        }
    }



    protected boolean isMessagePlaying(MessageAudioControl audioControl, IMMessage message) {
        if (audioControl.getPlayingAudio() != null && audioControl.getPlayingAudio().isTheSame(message)) {
            return true;
        } else {
            return false;
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

            play();
        }

        @Override
        public void onEndPlay(Playable playable) {
            if (!isTheSame(message.getUuid())) {
                return;
            }

            updateTime(playable.getDuration());

            stop();
        }


    };

    private void play() {
        if (animationView.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) animationView.getBackground();
            animation.start();
        }
    }

    private void stop() {
        if (animationView.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animation = (AnimationDrawable) animationView.getBackground();
            animation.stop();

            endPlayAnim();
        }
    }

    private void initPlayAnim() {
        if (isReceivedMessage()) {
            animationView.setBackgroundResource(R.drawable.nim_audio_animation_list_left);
        } else {
            animationView.setBackgroundResource(R.drawable.nim_audio_animation_list_right);
        }
    }

    private void endPlayAnim() {
        if (isReceivedMessage()) {
            animationView.setBackgroundResource(R.drawable.nim_audio_animation_list_left_3);
        } else {
            animationView.setBackgroundResource(R.drawable.nim_audio_animation_list_right_3);
        }
    }

    private boolean isTheSame(String uuid) {
        String current = durationLabel.getTag().toString();
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
