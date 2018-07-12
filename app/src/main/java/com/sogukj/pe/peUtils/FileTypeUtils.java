package com.sogukj.pe.peUtils;

import android.webkit.MimeTypeMap;

import com.sogukj.pe.R;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author admin
 * @date 2018/3/1
 */

public class FileTypeUtils {
    public enum FileType {
        DIRECTORY(R.drawable.icon_folder, R.string.type_directory),
        DOCUMENT(R.drawable.default_text, R.string.type_document),
        CERTIFICATE(R.drawable.default_text, R.string.type_certificate, "cer", "der", "pfx", "p12", "arm", "pem"),
        DRAWING(R.drawable.default_text, R.string.type_drawing, "ai", "cdr", "dfx", "eps", "svg", "stl", "wmf", "emf", "art", "xar"),
        EXCEL(R.drawable.icon_exl, R.string.type_excel, "xls", "xlk", "xlsb", "xlsm", "xlsx", "xlr", "xltm", "xlw", "numbers", "ods", "ots"),
        IMAGE(R.drawable.icon_pic, R.string.type_image, "bmp", "gif", "ico", "jpeg", "jpg", "pcx", "png", "psd", "tga", "tiff", "tif", "xcf"),
        MUSIC(R.drawable.icon_music, R.string.type_music, "aiff", "aif", "wav", "flac", "m4a", "wma", "amr", "mp2", "mp3", "wma", "aac", "mid", "m3u"),
        VIDEO(R.drawable.icon_video, R.string.type_video, "avi", "mov", "wmv", "mkv", "3gp", "f4v", "flv", "mp4", "mpeg", "webm"),
        PDF(R.drawable.icon_pdf, R.string.type_pdf, "pdf"),
        POWER_POINT(R.drawable.icon_ppt, R.string.type_power_point, "pptx", "keynote", "ppt", "pps", "pot", "odp", "otp"),
        WORD(R.drawable.icon_doc, R.string.type_word, "doc", "docm", "docx", "dot", "mcw", "rtf", "pages", "odt", "ott","txt"),
        ARCHIVE(R.drawable.icon_zip, R.string.type_archive, "cab", "7z", "alz", "arj", "bzip2", "bz2", "dmg", "gzip", "gz", "jar", "lz", "lzip", "lzma", "zip", "rar", "tar", "tgz"),
        APK(R.drawable.file_default, R.string.type_apk, "apk");

        private int icon;
        private int description;
        private String[] extensions;

        FileType(int icon, int description, String... extensions) {
            this.icon = icon;
            this.description = description;
            this.extensions = extensions;
        }

        public String[] getExtensions() {
            return extensions;
        }

        public int getIcon() {
            return icon;
        }

        public int getDescription() {
            return description;
        }
    }

    private static Map<String, FileType> fileTypeExtensions = new HashMap<>();

    static {
        for (FileType fileType : FileType.values()) {
            for (String extension : fileType.getExtensions()) {
                fileTypeExtensions.put(extension, fileType);
            }
        }
    }

    public static FileType getFileType(File file) {
        if (file.isDirectory()) {
            return FileType.DIRECTORY;
        }

        FileType fileType = fileTypeExtensions.get(getExtension(file.getName()));
        if (fileType != null) {
            return fileType;
        }

        return FileType.DOCUMENT;
    }
    /**
     * fileName
     * @param fileName   文件名加后缀
     * @return
     */
    public static FileTypeUtils.FileType getFileType(String fileName) {
        FileType fileType = fileTypeExtensions.get(getExtension(fileName));
        if (fileType != null) {
            return fileType;
        }
        return FileTypeUtils.FileType.DOCUMENT;
    }

    public static String getExtension(String fileName) {
        String encoded;
        try {
            encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            encoded = fileName;
        }
        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase();
    }
}
