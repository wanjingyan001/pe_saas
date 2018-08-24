package com.sogukj.pe.module.im;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;

import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.dialog.EasyProgressDialog;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.sogukj.pe.Extras;
import com.sogukj.pe.module.fileSelector.FileMainActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by admin on 2018/2/5.
 */

public class FileAction extends BaseAction {

    /**
     * 构造函数
     *
     * @param iconResId 图标 res id
     * @param titleId   图标标题的string res id
     */
    public FileAction(int iconResId, int titleId) {
        super(iconResId, titleId);
    }

    @Override
    public void onClick() {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage("正在读取内存文件");
        dialog.show();
        FileMainActivity.start(getActivity(),9,false,makeRequestCode(RequestCode.GET_LOCAL_FILE));
        new Handler().postDelayed(dialog::dismiss,1500);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RequestCode.GET_LOCAL_FILE:
                ArrayList<String> paths = data.getStringArrayListExtra(Extras.INSTANCE.getLIST());
                for (String path : paths) {
                    File file = new File(path);
                    IMMessage message = MessageBuilder.createFileMessage(getAccount(),
                            getSessionType(), file, file.getName());
                    sendMessage(message);
                }
                break;
            default:
                break;
        }
    }
}
