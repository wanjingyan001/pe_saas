package com.netease.nim.uikit.business.session.module.input;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.UIKitOptions;
import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nim.uikit.business.ait.AitTextChangeListener;
import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nim.uikit.business.session.emoji.MoonUtil;
import com.netease.nim.uikit.business.session.helper.SendImageHelper;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.media.picker.activity.PickImageActivity;
import com.netease.nim.uikit.common.media.picker.activity.PreviewImageFromCameraActivity;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialogHelper;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.media.record.AudioRecorder;
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback;
import com.netease.nimlib.sdk.media.record.RecordType;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomNotification;
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;
import java.util.List;

import jp.wasabeef.glide.transformations.internal.Utils;

/**
 * Created by admin on 2018/1/15.
 */

public class InputPanel2 implements IAudioRecordCallback, AitTextChangeListener, View.OnClickListener {
    protected Container container;
    protected View view;
    protected Handler uiHandler;
    // adapter
    private List<BaseAction> actions;
    private EditText editTextMessage;
    private TextView buttonSendMessage;
    private ImageView audio;
    private ImageView pic;
    private ImageView camera;
    private ImageView file;
    private TextWatcher aitTextWatcher;
    private SessionCustomization customization;
    private AudioRecorder audioMessageHelper;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean touched = false; // 是否按着

    // data
    private long typingTime = 0;
    private Runnable showTextRunnable = new Runnable() {
        @Override
        public void run() {
            showInputMethod(editTextMessage);
        }
    };
    private boolean isKeyboardShowed = true;
    private RelativeLayout recordLayout;
    private TextView recordTv;
    private ImageView recordBtn;
    private ImageView deleteRecord;
    private ImageView record_gif1;
    private ImageView record_gif2;

    public InputPanel2(Container container, View view, List<BaseAction> actions) {
        this.container = container;
        this.view = view;
        this.actions = actions;
        this.uiHandler = new Handler();
        init();
    }


    public void addAitTextWatcher(TextWatcher watcher) {
        aitTextWatcher = watcher;
    }


    public void setCustomization(SessionCustomization customization) {
        this.customization = customization;
    }

    public void reload(Container container, SessionCustomization customization) {
        this.container = container;
        setCustomization(customization);
    }

    public void onPause() {
        // 停止录音
        if (audioMessageHelper != null) {
            onEndAudioRecord(true);
        }
    }

    public void onDestroy() {
        // release
        if (audioMessageHelper != null) {
            audioMessageHelper.destroyAudioRecorder();
        }
    }


    private void init() {
        initViews();
        initListener();
        initTextEdit();
        for (int i = 0; i < actions.size(); ++i) {
            actions.get(i).setIndex(i);
            actions.get(i).setContainer(container);
        }
    }

    private void initViews() {
        editTextMessage = (EditText) view.findViewById(R.id.editTextMessage);
        buttonSendMessage = (TextView) view.findViewById(R.id.buttonSendMessage);
        audio = (ImageView) view.findViewById(R.id.audio);
        pic = (ImageView) view.findViewById(R.id.pic);
        camera = (ImageView) view.findViewById(R.id.camera);
        file = (ImageView) view.findViewById(R.id.file);
        recordLayout = (RelativeLayout) view.findViewById(R.id.record_layout);
        record_gif1 = ((ImageView) view.findViewById(R.id.record_gif1));
        record_gif2 = ((ImageView) view.findViewById(R.id.record_gif2));
        recordTv = (TextView) view.findViewById(R.id.record_tv);
        recordBtn = (ImageView) view.findViewById(R.id.record_btn);
        deleteRecord = (ImageView) view.findViewById(R.id.record_delete);
    }

    private void initListener() {
        buttonSendMessage.setOnClickListener(this);
        audio.setOnClickListener(this);
        pic.setOnClickListener(this);
        camera.setOnClickListener(this);
        file.setOnClickListener(this);
        deleteRecord.setOnClickListener(this);
        recordBtn.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touched = true;
                    initAudioRecord();
                    onStartAudioRecord();
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL
                        || event.getAction() == MotionEvent.ACTION_UP) {
                    touched = false;
                    handler.removeCallbacks(runnable);
                    onEndAudioRecord(false);
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    touched = true;
                    cancelAudioRecord(isCancelled(v, event));
                }
                return true;
            }
        });
    }

    private void initTextEdit() {
        editTextMessage.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editTextMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && recordLayout.getVisibility() == View.VISIBLE) {
                    recordLayout.setVisibility(View.GONE);
                    audio.setSelected(false);
                }
            }
        });
        editTextMessage.addTextChangedListener(new TextWatcher() {
            private int start;
            private int count;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.start = start;
                this.count = count;
                if (aitTextWatcher != null) {
                    aitTextWatcher.onTextChanged(s, start, before, count);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (aitTextWatcher != null) {
                    aitTextWatcher.beforeTextChanged(s, start, count, after);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                MoonUtil.replaceEmoticons(container.activity, s, start, count);

                int editEnd = editTextMessage.getSelectionEnd();
                editTextMessage.removeTextChangedListener(this);
                while (StringUtil.counterChars(s.toString()) > NimUIKitImpl.getOptions().maxInputTextLength && editEnd > 0) {
                    s.delete(editEnd - 1, editEnd);
                    editEnd--;
                }
                editTextMessage.setSelection(editEnd);
                editTextMessage.addTextChangedListener(this);

                if (aitTextWatcher != null) {
                    aitTextWatcher.afterTextChanged(s);
                }
                sendTypingCommand();
            }
        });
    }

    /**
     * 发送“正在输入”通知
     */
    private void sendTypingCommand() {
        if (container.account.equals(NimUIKit.getAccount())) {
            return;
        }

        if (container.sessionType == SessionTypeEnum.Team || container.sessionType == SessionTypeEnum.ChatRoom) {
            return;
        }

        if (System.currentTimeMillis() - typingTime > 5000L) {
            typingTime = System.currentTimeMillis();
            CustomNotification command = new CustomNotification();
            command.setSessionId(container.account);
            command.setSessionType(container.sessionType);
            CustomNotificationConfig config = new CustomNotificationConfig();
            config.enablePush = false;
            config.enableUnreadCount = false;
            command.setConfig(config);

            JSONObject json = new JSONObject();
            json.put("id", "1");
            command.setContent(json.toString());

            NIMClient.getService(MsgService.class).sendCustomNotification(command);
        }
    }


    /**
     * 初始化AudioRecord
     */
    private void initAudioRecord() {
        if (audioMessageHelper == null) {
            UIKitOptions options = NimUIKitImpl.getOptions();
            audioMessageHelper = new AudioRecorder(container.activity, options.audioRecordType, options.audioRecordMaxTime, this);
        }
    }

    /**
     * 开始语音录制
     */
    private void onStartAudioRecord() {
        container.activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        audioMessageHelper.startRecord();
        cancelled = false;
    }

    /**
     * 结束语音录制
     *
     * @param cancel
     */
    private void onEndAudioRecord(boolean cancel) {
        started = false;
        container.activity.getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        audioMessageHelper.completeRecord(cancel);
        recordTv.setText(R.string.record_audio);
    }

    /**
     * 取消语音录制
     *
     * @param cancel
     */
    private void cancelAudioRecord(boolean cancel) {
        // reject
        if (!started) {
            return;
        }
        // no change
        if (cancelled == cancel) {
            return;
        }
        cancelled = cancel;
    }

    // 上滑取消录音判断
    private static boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth()
                || event.getRawY() < location[1] - 40) {
            return true;
        }

        return false;
    }

    public boolean isRecording() {
        return audioMessageHelper != null && audioMessageHelper.isRecording();
    }

    private static final String TAG = "MsgSendLayout";

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RequestCode.PICK_IMAGE:
                onPickImageActivityResult(requestCode, data);
                break;
            case RequestCode.PREVIEW_IMAGE_FROM_CAMERA:
                onPreviewImageActivityResult(requestCode, data);
                break;
            case RequestCode.GET_LOCAL_FILE:
                String path = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                File file = new File(path);
                IMMessage message = MessageBuilder.createFileMessage(container.account,
                        container.sessionType, file, file.getName());
                container.proxy.sendMessage(message);
                break;
            default:
                break;
        }
        int index = (requestCode << 16) >> 24;
        if (index != 0) {
            index--;
            if (index < 0 | index >= actions.size()) {
                LogUtil.d(TAG, "request code out of actions' range");
                return;
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && recordLayout.getVisibility() == View.VISIBLE) {
            audio.setSelected(false);
            recordLayout.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    /**
     * 图片选取回调
     */
    private void onPickImageActivityResult(int requestCode, Intent data) {
        if (data == null) {
            Toast.makeText(container.activity, R.string.picker_image_error, Toast.LENGTH_LONG).show();
            return;
        }
        boolean local = data.getBooleanExtra(Extras.EXTRA_FROM_LOCAL, false);
        if (local) {
            // 本地相册
            sendImageAfterSelfImagePicker(data);
        } else {
            // 拍照
            Intent intent = new Intent();
            if (!handleImagePath(intent, data)) {
                return;
            }

            intent.setClass(container.activity, PreviewImageFromCameraActivity.class);
            container.activity.startActivityForResult(intent, RequestCode.PREVIEW_IMAGE_FROM_CAMERA);
        }
    }

    /**
     * 是否可以获取图片
     */
    private boolean handleImagePath(Intent intent, Intent data) {
        String photoPath = data.getStringExtra(Extras.EXTRA_FILE_PATH);
        if (TextUtils.isEmpty(photoPath)) {
            Toast.makeText(container.activity, R.string.picker_image_error, Toast.LENGTH_LONG).show();
            return false;
        }

        File imageFile = new File(photoPath);
        intent.putExtra("OrigImageFilePath", photoPath);
        File scaledImageFile = ImageUtil.getScaledImageFileWithMD5(imageFile, "image/jpeg");

        boolean local = data.getExtras().getBoolean(Extras.EXTRA_FROM_LOCAL, true);
        if (!local) {
            // 删除拍照生成的临时文件
            AttachmentStore.delete(photoPath);
        }

        if (scaledImageFile == null) {
            Toast.makeText(container.activity, R.string.picker_image_error, Toast.LENGTH_LONG).show();
            return false;
        } else {
            ImageUtil.makeThumbnail(container.activity, scaledImageFile);
        }
        intent.putExtra("ImageFilePath", scaledImageFile.getAbsolutePath());
        return true;
    }

    /**
     * 从预览界面点击发送图片
     */
    private void sendImageAfterPreviewPhotoActivityResult(Intent data) {
        SendImageHelper.sendImageAfterPreviewPhotoActivityResult(data, new SendImageHelper.Callback() {

            @Override
            public void sendImage(File file, boolean isOrig) {
                onPicked(file);
            }
        });
    }

    /**
     * 发送图片
     */
    private void sendImageAfterSelfImagePicker(final Intent data) {
        SendImageHelper.sendImageAfterSelfImagePicker(container.activity, data, new SendImageHelper.Callback() {

            @Override
            public void sendImage(File file, boolean isOrig) {
                onPicked(file);

            }
        });
    }

    /**
     * 拍摄回调
     */
    private void onPreviewImageActivityResult(int requestCode, Intent data) {
        if (data.getBooleanExtra(PreviewImageFromCameraActivity.RESULT_SEND, false)) {
            sendImageAfterPreviewPhotoActivityResult(data);
        } else if (data.getBooleanExtra(PreviewImageFromCameraActivity.RESULT_RETAKE, false)) {
            String filename = StringUtil.get32UUID() + ".jpg";
            String path = StorageUtil.getWritePath(filename, StorageType.TYPE_TEMP);

            if (requestCode == RequestCode.PREVIEW_IMAGE_FROM_CAMERA) {
                PickImageActivity.start(container.activity, RequestCode.PREVIEW_IMAGE_FROM_CAMERA, PickImageActivity.FROM_CAMERA, path);
            }
        }
    }

    private void onPicked(File file) {
        IMMessage message = MessageBuilder.createImageMessage(container.account, container.sessionType, file, file.getName());
        container.proxy.sendMessage(message);
    }

    // 显示键盘布局
    private void showInputMethod(EditText editTextMessage) {
        editTextMessage.requestFocus();
        //如果已经显示,则继续操作时不需要把光标定位到最后
        if (!isKeyboardShowed) {
            editTextMessage.setSelection(editTextMessage.getText().length());
            isKeyboardShowed = true;
        }

        InputMethodManager imm = (InputMethodManager) container.activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editTextMessage, 0);

        container.proxy.onInputPanelExpand();
    }

    public int getEditSelectionStart() {
        return editTextMessage.getSelectionStart();
    }

    @Override
    public void onRecordReady() {
        Log.d("WJY", "AudioRecorder:onRecordReady");
    }

    int second;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            second++;
            recordTv.setText(timeParse(second * 1000));
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void onRecordStart(File file, RecordType recordType) {
        Log.d("WJY", "AudioRecorder:onRecordStart");
        started = true;
        if (!touched) {
            return;
        }
        second = 0;
        handler.post(runnable);
        record_gif1.setVisibility(View.VISIBLE);
        record_gif2.setVisibility(View.VISIBLE);
        Glide.with(container.activity)
                .asGif()
                .load(R.drawable.im_audio_gif)
                .into(record_gif1);
        Glide.with(container.activity)
                .asGif()
                .load(R.drawable.im_audio_gif)
                .into(record_gif2);
    }

    @Override
    public void onRecordSuccess(File file, long l, RecordType recordType) {
        Log.d("WJY", "AudioRecorder:onRecordSuccess");
        IMMessage audioMessage = MessageBuilder.createAudioMessage(container.account, container.sessionType, file, l);
        container.proxy.sendMessage(audioMessage);
        record_gif1.setVisibility(View.GONE);
        record_gif2.setVisibility(View.GONE);
    }

    @Override
    public void onRecordFail() {
        Log.d("WJY", "AudioRecorder:onRecordFail");
        if (started) {
            record_gif1.setVisibility(View.GONE);
            record_gif2.setVisibility(View.GONE);
            Toast.makeText(container.activity, R.string.recording_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRecordCancel() {
        Log.d("WJY", "AudioRecorder:onRecordCancel");
        record_gif1.setVisibility(View.GONE);
        record_gif2.setVisibility(View.GONE);
    }

    @Override
    public void onRecordReachedMaxTime(final int i) {
        EasyAlertDialogHelper.createOkCancelDiolag(container.activity, "", container.activity.getString(R.string.recording_max_time), false, new EasyAlertDialogHelper.OnDialogActionListener() {
            @Override
            public void doCancelAction() {
                record_gif1.setVisibility(View.GONE);
                record_gif2.setVisibility(View.GONE);
            }

            @Override
            public void doOkAction() {
                record_gif1.setVisibility(View.GONE);
                record_gif2.setVisibility(View.GONE);
                audioMessageHelper.handleEndRecord(true, i);
            }
        }).show();
    }

    @Override
    public void onTextAdd(String content, int start, int length) {
        if (editTextMessage.getVisibility() != View.VISIBLE) {
        } else {
            uiHandler.postDelayed(showTextRunnable, 200);
        }
        editTextMessage.getEditableText().insert(start, content);
    }

    @Override
    public void onTextDelete(int start, int length) {
        if (editTextMessage.getVisibility() != View.VISIBLE) {
        } else {
            uiHandler.postDelayed(showTextRunnable, 200);
        }
        int end = start + length - 1;
        editTextMessage.getEditableText().replace(start, end, "");
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buttonSendMessage) {
            onTextMessageSendButtonPressed();
        } else if (i == R.id.audio) {
            StringUtil.closeInput(container.activity, editTextMessage);
            editTextMessage.clearFocus();
            audio.setSelected(true);
            recordLayout.setVisibility(View.VISIBLE);
        } else if (i == R.id.pic) {
            StringUtil.closeInput(container.activity, editTextMessage);
            editTextMessage.clearFocus();
            audio.setSelected(false);
            recordLayout.setVisibility(View.GONE);
            PickImageHelper.PickImageOption option = getPickImageOption();
            PickImageActivity.start(container.activity, RequestCode.PICK_IMAGE, PickImageActivity.FROM_LOCAL,
                    option.outputPath, option.multiSelect,
                    option.multiSelectMaxCount, true, false, 0, 0);
        } else if (i == R.id.camera) {
            StringUtil.closeInput(container.activity, editTextMessage);
            editTextMessage.clearFocus();
            audio.setSelected(false);
            recordLayout.setVisibility(View.GONE);
            PickImageHelper.PickImageOption option = getPickImageOption();
            PickImageActivity.start(container.activity, RequestCode.PICK_IMAGE, PickImageActivity.FROM_CAMERA,
                    option.outputPath, option.multiSelect, 1,
                    true, false, 0, 0);
        } else if (i == R.id.file) {
            StringUtil.closeInput(container.activity, editTextMessage);
            editTextMessage.clearFocus();
            audio.setSelected(false);
            recordLayout.setVisibility(View.GONE);
            MaterialFilePicker picker = new MaterialFilePicker();
            picker.withActivity(container.activity)
                    .withTitle("内部存储")
                    .withFilterDirectories(true)
                    .withHiddenFiles(true)
                    .withCloseMenu(false)
                    .withRequestCode(RequestCode.GET_LOCAL_FILE)
                    .start();
        } else if (i == R.id.record_delete) {

        }
    }

    @NonNull
    private PickImageHelper.PickImageOption getPickImageOption() {
        PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
        option.titleResId = R.string.input_panel_photo;
        option.multiSelect = true;
        option.multiSelectMaxCount = 9;
        option.crop = false;
        option.cropOutputImageWidth = 720;
        option.cropOutputImageHeight = 720;
        option.outputPath = tempFile();
        return option;
    }


    // 发送文本消息
    private void onTextMessageSendButtonPressed() {
        String text = editTextMessage.getText().toString();
        if (text.trim().isEmpty()) {
            Toast.makeText(container.activity, "不能发送空消息", Toast.LENGTH_SHORT).show();
            return;
        }
        IMMessage textMessage = createTextMessage(text);
        if (container.proxy.sendMessage(textMessage)) {
            editTextMessage.setText("");
        }
    }


    protected IMMessage createTextMessage(String text) {
        return MessageBuilder.createTextMessage(container.account, container.sessionType, text);
    }

    private String tempFile() {
        String filename = StringUtil.get32UUID() + ".jpg";
        return StorageUtil.getWritePath(filename, StorageType.TYPE_TEMP);
    }

    public static String timeParse(long duration) {
        String time = "";
        long minute = duration / 60000;
        long seconds = duration % 60000;
        long second = Math.round((float) seconds / 1000);
        if (minute < 10) {
            time += "0";
        }
        time += minute + ":";
        if (second < 10) {
            time += "0";
        }
        time += second;
        return time;
    }
}
