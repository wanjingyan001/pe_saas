package com.netease.nim.uikit.business.session.viewholder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.MsgServiceObserve;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;
import java.util.Locale;

/**
 * Created by admin on 2018/1/18.
 */

public class MsgViewHolderFile extends MsgViewHolderBase implements View.OnClickListener, DialogInterface.OnCancelListener {
    private ImageView fileIcon;
    private TextView fileNameLabel;
    private TextView fileSize;
    private TextView download;
    private FileAttachment msgAttachment;
    private ProgressDialog dialog;
    private AbortableFuture<Void> future;
    private LinearLayout fileLayout;
    private View line;
    //建立一个文件类型与文件后缀名的匹配表
    private static final String[][] MATCH_ARRAY = {
            //{后缀名，    文件类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".prop", "text/plain"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
            {"", "*/*"}
    };

    public MsgViewHolderFile(BaseMultiItemFetchLoadAdapter adapter) {
        super(adapter);
    }

    @Override
    protected int getContentResId() {
        return R.layout.item_message_file;
    }

    @Override
    protected void inflateContentView() {
        fileLayout = ((LinearLayout) view.findViewById(R.id.file_item_layout));
        fileIcon = (ImageView) view.findViewById(R.id.message_item_file_icon_image);
        fileNameLabel = (TextView) view.findViewById(R.id.message_item_file_name_label);
        fileSize = (TextView) view.findViewById(R.id.message_item_file_size);
        download = (TextView) view.findViewById(R.id.download_file);
        line = view.findViewById(R.id.line);
        download.setOnClickListener(this);
        registerObservers(true);
    }

    @Override
    protected void bindContentView() {
        msgAttachment = (FileAttachment) message.getAttachment();
        String extension = msgAttachment.getExtension();
        if (extension != null) {
            fileIcon.setImageResource(FileUtil.getFileTypeByPath(msgAttachment.getDisplayName()).getIcon());
        }
        fileNameLabel.setText(msgAttachment.getDisplayName());
        String path = msgAttachment.getPath();
        if (!TextUtils.isEmpty(path)) {
            fileSize.setText(formatFileSize(msgAttachment.getSize(), SizeUnit.Auto));
            download.setCompoundDrawables(null, null, null, null);
            download.setText("打开");
        } else {
            AttachStatusEnum status = message.getAttachStatus();
            switch (status) {
                case def:
                    updateFileStatusLabel();
                    break;
                case transferring:
                    progressBar.setVisibility(View.VISIBLE);
                    int percent = (int) (getMsgAdapter().getProgress(message) * 100);
                    progressBar.setProgress(percent);
                    break;
                case transferred:
                case fail:
                    updateFileStatusLabel();
                    break;
                default:
                    break;
            }
        }
        if (isReceivedMessage()) {
            setGravity(fileLayout, Gravity.LEFT);
            contentContainer.setBackgroundResource(leftBackground());
        } else {
            setGravity(fileLayout, Gravity.RIGHT);
            contentContainer.setBackgroundResource(R.drawable.nim_message_right_white_bg2);
        }
        double width = 0.6 * ScreenUtil.screenMin;
        double height = 0.6 * 0.45 * ScreenUtil.screenMin;
        ViewGroup.LayoutParams layoutParams = fileLayout.getLayoutParams();
        layoutParams.width = (int) width;
        layoutParams.height = (int) height;
        fileLayout.setLayoutParams(layoutParams);
    }


    private void updateFileStatusLabel() {
        progressBar.setVisibility(View.GONE);
        fileSize.setText(formatFileSize(msgAttachment.getSize(), SizeUnit.Auto));
        // 下载状态
        String savePath = msgAttachment.getPathForSave() + "." + msgAttachment.getExtension();
        if (AttachmentStore.isFileExist(savePath)) {
            download.setCompoundDrawables(null, null, null, null);
            download.setText("打开");
//            download.setEnabled(false);
        } else {
            download.setText("下载");
            download.setEnabled(true);
        }
    }


    public static String formatFileSize(long size, SizeUnit unit) {
        if (size < 0) {
            return NimUIKit.getContext().getString(R.string.unknow_size);
        }

        final double KB = 1024;
        final double MB = KB * 1024;
        final double GB = MB * 1024;
        final double TB = GB * 1024;
        if (unit == SizeUnit.Auto) {
            if (size < KB) {
                unit = SizeUnit.Byte;
            } else if (size < MB) {
                unit = SizeUnit.KB;
            } else if (size < GB) {
                unit = SizeUnit.MB;
            } else if (size < TB) {
                unit = SizeUnit.GB;
            } else {
                unit = SizeUnit.TB;
            }
        }

        switch (unit) {
            case Byte:
                return size + "B";
            case KB:
                return String.format(Locale.US, "%.2fKB", size / KB);
            case MB:
                return String.format(Locale.US, "%.2fMB", size / MB);
            case GB:
                return String.format(Locale.US, "%.2fGB", size / GB);
            case TB:
                return String.format(Locale.US, "%.2fPB", size / TB);
            default:
                return size + "B";
        }
    }

    private boolean isOriginDataHasDownloaded(final IMMessage message) {
        return !TextUtils.isEmpty(((FileAttachment) message.getAttachment()).getPath());
    }

    private void registerObservers(boolean register) {
        MsgServiceObserve service = NIMClient.getService(MsgServiceObserve.class);
        service.observeAttachmentProgress(attachmentProgressObserver, register);
        service.observeMsgStatus(statusObserver, register);
    }

    private Observer<IMMessage> statusObserver = new Observer<IMMessage>() {
        @Override
        public void onEvent(IMMessage msg) {
            if (!msg.isTheSame(message)) {
                return;
            }
            if (msg.getAttachStatus() == AttachStatusEnum.transferred && isOriginDataHasDownloaded(msg)) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                download.setText("打开");
                download.setCompoundDrawables(null, null, null, null);
//                download.setEnabled(false);
                registerObservers(false);
            } else if (msg.getAttachStatus() == AttachStatusEnum.fail) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
                download.setText("下载");
                download.setEnabled(true);
            }
        }
    };


    private Observer<AttachmentProgress> attachmentProgressObserver = new Observer<AttachmentProgress>() {

        @Override
        public void onEvent(AttachmentProgress attachmentProgress) {
            long total = attachmentProgress.getTotal();
            long progress = attachmentProgress.getTransferred();
            float percent = (float) progress / (float) total;
            if (percent > 1.0) {
                // 消息中标识的文件大小有误，小于实际大小
                percent = (float) 1.0;
                progress = total;
            }
            if (dialog != null) {
                dialog.setMax((int) total);
                dialog.setProgress((int) progress);
            }
        }
    };


    @Override
    public void onClick(View v) {
        if (isOriginDataHasDownloaded(message)) {
            FileAttachment fileAttachment = (FileAttachment) message.getAttachment();
            String extension = fileAttachment.getExtension();
            if (extension != null) {
                openFileByPath(context, fileAttachment.getPathForSave(), extension);
            } else {
                Toast.makeText(context, "文件格式不正确,无法打开", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        dialog = ProgressDialog.show(context, "", "正在下载", false, true, this);
        future = NIMClient.getService(MsgService.class).downloadAttachment(message, false);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (future.abort()) {
            Toast.makeText(context, "下载取消", Toast.LENGTH_SHORT).show();
        }
    }

    public enum SizeUnit {
        Byte,
        KB,
        MB,
        GB,
        TB,
        Auto,
    }


    /**
     * 根据路径打开文件
     *
     * @param context 上下文
     * @param path    文件路径
     */
    public void openFileByPath(Context context, String path, String extension) {
        if (context == null || path == null || extension == null) {
            return;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //文件的类型
        String type = "";
        for (String[] amatchArray : MATCH_ARRAY) {
            //判断文件的格式
            if (amatchArray[0].contains(extension)) {
                type = amatchArray[1];
                break;
            }
        }
        try {
            Uri data;
            File file = new File(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(context, context.getApplicationInfo().packageName + ".generic.file.provider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                //设置intent的data和Type属性
                data = Uri.fromFile(file);
            }
            intent.setDataAndType(data, type);
            //跳转
            context.startActivity(intent);
        } catch (Exception e) { //当系统没有携带文件打开软件，提示
            Toast.makeText(context, "没有可以打开该文件的软件", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
