package com.sogukj.pe.peUtils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;

import com.sogukj.pe.BuildConfig;
import com.sogukj.pe.service.DownLoadService;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PdfUtil {

    public static void opendPdf(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", file);
        } else {
            uri = Uri.fromFile(file);
        }
        FileTypeUtils.FileType type = FileTypeUtils.getFileType(file);
        switch (type) {
            case PDF:
                intent.setDataAndType(uri, "application/pdf");
                break;
            case WORD:
                intent.setDataAndType(uri, "application/msword");
                break;
            case EXCEL:
                intent.setDataAndType(uri, "application/vnd.ms-excel");
                break;
            case MUSIC:
                intent.setDataAndType(uri, "audio/*");
                break;
            case VIDEO:
                intent.setDataAndType(uri, "video/*");
                break;
            case IMAGE:
                intent.setDataAndType(uri, "image/*");
                break;
            case DOCUMENT:
            case CERTIFICATE:
            case DRAWING:
                intent.setDataAndType(uri, "text/plain");
                break;
            default:
                intent.setDataAndType(uri, "text/html");
                break;
        }
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context,
                    "没有找到可以打开该文件的应用!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void loadPdf(final Context context, final String urls, @Nullable String name) {
        URL url = null;

        try {
            url = new URL(urls);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (url == null) {
            return;
        }
        final String filePath;
        if (name != null) {
            filePath = FileUtil.getExternalFilesDir(context) + name;
        } else {
            filePath = FileUtil.getExternalFilesDir(context) + url.getPath().hashCode() + ".pdf";
        }
        if (FileUtil.isFileExist(filePath)) {
            opendPdf(context, new File(filePath));
        } else {
            Toast.makeText(context, "正在下载文件...", Toast.LENGTH_SHORT).show();
            String host = "http://" + url.getHost();
            new DownLoadService(host).getFile(urls.replace(host, "")).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.code() == 200) {
                            File file = FileUtil.writeFile(response.body().byteStream(), filePath);
                            opendPdf(context, file);
                        } else {
                            Toast.makeText(context, "下载失败!", Toast.LENGTH_SHORT).show();
                            if (!TextUtils.isEmpty(urls)) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(urls));
                                context.startActivity(intent);
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Toast.makeText(context, "连接失败!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
