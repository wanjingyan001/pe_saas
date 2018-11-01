package com.sogukj.pe.peUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.nbsp.materialfilepicker.utils.FileComparator;
import com.netease.nim.uikit.api.NimUIKit;
import com.sogukj.pe.R;
import com.sogukj.pe.baselibrary.utils.Trace;
import com.sogukj.pe.module.fileSelector.DocFileFilter;
import com.sogukj.pe.module.fileSelector.ImageFileFilter;
import com.sogukj.pe.module.fileSelector.VideoFileFilter;
import com.sogukj.pe.module.fileSelector.ZipFileFilter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class FileUtil {
    private static final String TAG = FileUtil.class.getSimpleName();

    public enum FileType {
        IMAGE("jpg", "jpeg", "gif", "png", "bmp", "Webp"),
        VIDEO("rm", "rmvb", "mp4", "mov", "mtv", "wmv", "avi", "3gp", "flv"),
        DOC("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx"),
        ZIP("rar", "zip", "7z", "iso", "gz"),
        TXT("txt"),
        OTHER("pages", "keynote", "numbers", "cer", "der", "pfx", "p12", "arm", "pem",
                "ai", "cdr", "dfx", "eps", "svg", "stl", "wmf", "emf", "art", "xar",
                "wav", "flac", "m4a", "wma", "amr",  "mp3", "wma", "aac", "mid", "m3u");
        private String[] extensions;

        FileType(String... extensions) {
            this.extensions = extensions;
        }

        public String[] getExtensions() {
            return extensions;
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
        FileType fileType = fileTypeExtensions.get(getExtension(file.getName()));
        if (fileType != null) {
            return fileType;
        }
        return FileType.OTHER;
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

    /**
     * 获取项目目录
     *
     * @param context
     * @return
     */
    public static String getExternalFilesDir(Context context) {
        return context.getExternalFilesDir(null) + "/";
    }

    /**
     * 获取项目子目录
     *
     * @param context
     * @param name
     * @return
     */
    public static String getExternalFilesDir(Context context, String name) {
        return context.getExternalFilesDir(name) + "/";
    }

    public static byte[] file2Byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static File writeFile(InputStream is, String filePath)
            throws IOException {

        BufferedInputStream from = null;
        FileOutputStream to = null;
        File file = null;
        try {
            File dir = new File(filePath);
            createNewFile(dir);
            file = new File(filePath);
            from = new BufferedInputStream(is);
            to = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                    Trace.INSTANCE.e(TAG, "", e);
                }
            }
            if (to != null)
                try {
                    to.close();
                } catch (IOException e) {
                    Trace.INSTANCE.e(TAG, "", e);
                }
        }
        return file;
    }

    public static File byte2File(byte[] buf, String filePath) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(filePath);
            createNewFile(dir);
            file = new File(filePath);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file;
    }

    /**
     * 拷贝文件
     *
     * @param fromFile
     * @param toFile
     * @throws IOException
     */
    public static void copyFile(File fromFile, String toFile)
            throws IOException {

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1)
                to.write(buffer, 0, bytesRead); // write
        } finally {
            if (from != null)
                try {
                    from.close();
                } catch (IOException e) {
                }
            if (to != null)
                try {
                    to.close();
                } catch (IOException e) {
                }
        }
    }

    /**
     * 拷贝文件
     *
     * @param fromFile
     * @param toFile
     * @throws IOException
     */
    public static void copyFile(String fromFile, String toFile)
            throws IOException {

        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1)
                to.write(buffer, 0, bytesRead); // write
        } finally {
            if (from != null)
                try {
                    from.close();
                } catch (IOException e) {
                    Trace.INSTANCE.e(TAG, "", e);
                }
            if (to != null)
                try {
                    to.close();
                } catch (IOException e) {
                    Trace.INSTANCE.e(TAG, "", e);
                }
        }
    }

    // 目录拷贝
    public static int copy(File fromFile, String toFile) throws IOException {
        // 要复制的文件目录
        File[] currentFiles;
        // 如果不存在则 return出去
        if (!fromFile.exists()) {
            return -1;
        }
        // 如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = fromFile.listFiles();

        // 目标目录
        File targetDir = new File(toFile);
        // 创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // 遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory())// 如果当前项为子目录 进行递归
            {
                copy(currentFiles[i], toFile + currentFiles[i].getName() + "/");

            } else// 如果当前项为文件则进行文件拷贝
            {
                copyFile(currentFiles[i], toFile + currentFiles[i].getName());
            }
        }
        return 0;
    }

    //文件重命名
    public static void renameTo(String filePath, String newPath) {
        File file = new File(filePath);
        if (file.exists()) {
            File file1 = new File(newPath);
            file.renameTo(file1);
        }
    }

    /**
     * 创建文件
     *
     * @param file
     * @return
     */
    public static File createNewFile(File file) {

        try {
            if (file.exists()) {
                return file;
            }

            File dir = file.getParentFile();

            if (!dir.exists()) {
                dir.mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            Trace.INSTANCE.e(TAG, "", e);
            return null;
        }
        return file;
    }

    /**
     * 创建文件
     *
     * @param path
     */
    public static File createNewFile(String path) {
        File file = new File(path);
        return createNewFile(file);
    }


    /**
     * 删除文件
     *
     * @param file
     */
    public static void deleteFile(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i]);
            }
        }
        file.delete();
    }

    /**
     * 删除文件
     *
     * @param path
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        deleteFile(file);
    }

    /**
     * 向Text文件中写入内容
     *
     * @param content
     * @return
     */
    public static boolean write(String path, String content) {
        return write(path, content, false);
    }

    public static boolean write(String path, String content, boolean append) {
        return write(new File(path), content, append);
    }

    public static boolean write(File file, String content) {
        return write(file, content, false);
    }

    public static boolean write(File file, String content, boolean append) {

        if (file == null || content == null) {
            return false;
        }

        if (!file.exists()) {
            file = createNewFile(file);
        }

        FileOutputStream ops = null;
        try {
            ops = new FileOutputStream(file, append);
            ops.write(content.getBytes());
        } catch (Exception e) {
            Trace.INSTANCE.e(TAG, "", e);
            return false;
        } finally {
            try {
                ops.close();
            } catch (IOException e) {
                Trace.INSTANCE.e(TAG, "", e);
            }
            ops = null;
        }

        return true;
    }

    /**
     * 获得文件名
     *
     * @param path
     * @return
     */
    public static String getFileName(String path) {
        if (path == null) {
            return null;
        }
        File f = new File(path);
        String name = f.getName();
        f = null;
        return name;
    }

    /**
     * 读取文件内容，从第startLine行开始，读取lineCount行
     *
     * @param file
     * @param startLine
     * @param lineCount
     * @return 读到文字的list, 如果list.size<lineCount则说明读到文件末尾了
     */
    public static List<String> readFile(File file, int startLine, int lineCount) {
        if (file == null || startLine < 1 || lineCount < 1) {
            return null;
        }

        if (!file.exists()) {
            return null;
        }

        FileReader fileReader = null;
        List<String> list = null;
        try {
            list = new ArrayList<String>();
            fileReader = new FileReader(file);
            LineNumberReader lnr = new LineNumberReader(fileReader);
            boolean end = false;
            for (int i = 1; i < startLine; i++) {
                if (lnr.readLine() == null) {
                    end = true;
                    break;
                }
            }
            if (end == false) {
                for (int i = startLine; i < startLine + lineCount; i++) {
                    String line = lnr.readLine();
                    if (line == null) {
                        break;
                    }
                    list.add(line);

                }
            }
        } catch (Exception e) {
            Trace.INSTANCE.e(TAG, "read log error!", e);
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     *
     */
    public static int readFileCount(File file) {
        if (file == null) {
            return 0;
        }

        if (!file.exists()) {
            return 0;
        }

        FileReader fileReader = null;
        int count = 0;
        try {
            fileReader = new FileReader(file);
            LineNumberReader lnr = new LineNumberReader(fileReader);
            while (lnr.readLine() != null) {
                count++;
            }
        } catch (Exception e) {
            Trace.INSTANCE.e(TAG, "read log error!", e);
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return count;
    }

    /**
     * 创建文件夹
     *
     * @param dir
     * @return
     */
    public static boolean createDir(File dir) {
        try {
            if (!dir.exists()) {
                dir.mkdirs();
            }
            return true;
        } catch (Exception e) {
            Trace.INSTANCE.e(TAG, "create dir error", e);
            return false;
        }
    }

    /**
     * 在SD卡上创建目录
     *
     * @param dirName
     */
    public static File creatSDDir(String dirName) {
        File dir = new File(dirName);
        dir.mkdir();
        return dir;
    }

    /**
     * 判断SD卡上的文件是否存在
     */
    public static boolean isFileExist(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    /**
     * 将一个InputStream里面的数据写入到SD卡中
     */
    public static File write2SDFromInput(String path, String fileName,
                                         InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
            creatSDDir(path);
            file = createNewFile(path + "/" + fileName);
            output = new FileOutputStream(file);
            byte buffer[] = new byte[1024];
            int len = -1;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            output.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    // 从文件中一行一行的读取文件
    public static String readFile(File file) {
        Reader read = null;
        String content = "";
        String string = "";
        BufferedReader br = null;
        try {
            read = new FileReader(file);
            br = new BufferedReader(read);
            while ((content = br.readLine().toString().trim()) != null) {
                string += content + "\r\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                read.close();
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("string=" + string);
        return string.toString();
    }


    /**
     * 获取图片类型
     *
     * @param filePath
     * @return
     */
    public static String getFileType(String filePath) {
        HashMap<String, String> mFileTypes = new HashMap<>();
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E", "jpeg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("49492A00", "tif");
        mFileTypes.put("424D", "bmp");
        return mFileTypes.get(getFileHeader(filePath));
    }

    /**
     * 获取文件头信息
     *
     * @param filePath
     * @return
     */
    public static String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[3];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    /**
     * 将byte字节转换为十六进制字符串
     *
     * @param src
     * @return
     */
    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }

    /**
     * 获取文件大小
     *
     * @param size
     * @param unit
     * @return
     */
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

    public enum SizeUnit {
        Byte,
        KB,
        MB,
        GB,
        TB,
        Auto,
    }

    /**
     * 获取指定目录下的文件列表
     *
     * @param fileAbsolutePath
     * @return
     */
    public static List<File> getFiles(String fileAbsolutePath) {
        List<File> vecFile = new ArrayList<>();
        File file = new File(fileAbsolutePath);
        if (file.exists()) {
            File[] subFile = file.listFiles();
            if (subFile != null && subFile.length > 0) {
                for (File aSubFile : subFile) {
                    // 判断是否为文件夹
                    if (!aSubFile.isDirectory()) {
                        vecFile.add(aSubFile);
                    }
                }
            }
        }
        return vecFile;
    }

    /**
     * 获取指定目录下的文件地址列表
     * @param fileAbsolutePath
     * @return
     */
    public static Set<String> getFilePaths(String fileAbsolutePath){
        Set<String> paths = new HashSet<>();
        File file = new File(fileAbsolutePath);
        if (file.exists()) {
            File[] subFile = file.listFiles();
            if (subFile != null && subFile.length > 0) {
                for (File aSubFile : subFile) {
                    // 判断是否为文件夹
                    if (!aSubFile.isDirectory()) {
                        paths.add(aSubFile.getAbsolutePath());
                    }
                }
            }
        }
        return paths;
    }

    public static List<File> getFileListByDirPath(String path, FileFilter filter) {
        File directory = new File(path);
        File[] files = directory.listFiles(filter);

        if (files == null) {
            return new ArrayList<>();
        }

        List<File> result = Arrays.asList(files);
        Collections.sort(result, new FileComparator());
        return result;
    }

    public static String cutLastSegmentOfPath(String path) {
        if (path.length() - path.replace("/", "").length() <= 1) {
            return "/";
        }
        String newPath = path.substring(0, path.lastIndexOf("/"));
        // We don't need to list the content of /storage/emulated
        if (newPath.equals("/storage/emulated")) {
            newPath = "/storage";
        }
        return newPath;
    }

    public static String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }


    public static Map<FileType, List<File>> getFilesByType(List<File> files) {
        Map<FileType, List<File>> fileMap = new HashMap<>();
        List<File> images = new ArrayList<>();
        List<File> videos = new ArrayList<>();
        List<File> docs = new ArrayList<>();
        List<File> zips = new ArrayList<>();
        List<File> others = new ArrayList<>();
        for (File file : files) {
            FileType type = getFileType(file);
            switch (type) {
                case IMAGE:
                    images.add(file);
                    break;
                case VIDEO:
                    videos.add(file);
                    break;
                case DOC:
                    docs.add(file);
                    break;
                case ZIP:
                    zips.add(file);
                    break;
                default:
                    others.add(file);
                    break;
            }
        }
        fileMap.put(FileType.IMAGE, images);
        fileMap.put(FileType.VIDEO, videos);
        fileMap.put(FileType.DOC, docs);
        fileMap.put(FileType.ZIP, zips);
        fileMap.put(FileType.OTHER, others);
        return fileMap;
    }

    public static String getFileProvider(Context context) {
        return context.getApplicationInfo().packageName + ".fileProvider";
    }


    /**
     * 根据图片的路径得到该图片在表中的ID
     * @param context
     * @param fileName
     * @return
     */
    public static String getImageIdFromPath(Context context, String fileName) {
        String whereClause = MediaStore.Images.Media.DATA + " = '" + fileName + "'";
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.Media._ID}, whereClause, null, null);
        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        cursor.moveToFirst();
        String imageId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID));
        cursor.close();
        if (imageId == null) {
            return null;
        }
        return imageId;
    }

    /**
     * 根据图片的ID得到缩略图
     * @param context
     * @param imageId
     * @return
     */
    public static Bitmap getThumbnailsFromImageId(Context context, String imageId) {
        if (imageId == null || "".equals(imageId)) {
            return null;
        }
        Bitmap bitmap;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_4444;
        long imageIdLong = Long.parseLong(imageId);
        bitmap = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), imageIdLong, MediaStore.Images.Thumbnails.MINI_KIND, options);
        return bitmap;
    }

    /**
     * 获取指定目录下的图片文件
     * @param fileAbsolutePath
     * @return
     */
    public static List<File> getImageFile(String fileAbsolutePath){
        List<File> iamges = new ArrayList<>();
        File file = new File(fileAbsolutePath);
        if (file.exists()) {
            File[] subFile = file.listFiles(new ImageFileFilter());
            if (subFile != null && subFile.length > 0) {
                for (File aSubFile : subFile) {
                    // 判断是否为文件夹
                    if (!aSubFile.isDirectory()) {
                        iamges.add(aSubFile);
                    }
                }
            }
        }
        return iamges;
    }

    /**
     * 获取指定目录下的视频文件
     * @param fileAbsolutePath
     * @return
     */
    public static List<File> getVideoFile(String fileAbsolutePath){
        List<File> videos = new ArrayList<>();
        File file = new File(fileAbsolutePath);
        if (file.exists()) {
            File[] subFile = file.listFiles(new VideoFileFilter());
            if (subFile != null && subFile.length > 0) {
                for (File aSubFile : subFile) {
                    // 判断是否为文件夹
                    if (!aSubFile.isDirectory()) {
                        videos.add(aSubFile);
                    }
                }
            }
        }
        return videos;
    }

    /**
     *  获取指定目录下的文档文件
     * @param fileAbsolutePath
     * @return
     */
    public static List<File> getDocFile(String fileAbsolutePath){
        List<File> docs = new ArrayList<>();
        File file = new File(fileAbsolutePath);
        if (file.exists()) {
            File[] subFile = file.listFiles(new DocFileFilter());
            if (subFile != null && subFile.length > 0) {
                for (File aSubFile : subFile) {
                    // 判断是否为文件夹
                    if (!aSubFile.isDirectory()) {
                        docs.add(aSubFile);
                    }
                }
            }
        }
        return docs;
    }

    /**
     * 获取指定目录下的压缩包文件
     * @param fileAbsolutePath
     * @return
     */
    public static List<File> getZipFile(String fileAbsolutePath){
        List<File> zips = new ArrayList<>();
        File file = new File(fileAbsolutePath);
        if (file.exists()) {
            File[] subFile = file.listFiles(new ZipFileFilter());
            if (subFile != null && subFile.length > 0) {
                for (File aSubFile : subFile) {
                    // 判断是否为文件夹
                    if (!aSubFile.isDirectory()) {
                        zips.add(aSubFile);
                    }
                }
            }
        }
        return zips;
    }
}