package com.sogukj.pe.peUtils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.util.LruCache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sogukj.pe.baselibrary.utils.DiskLruCache;
import com.sogukj.pe.bean.CityArea;
import com.sogukj.pe.bean.MessageBean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


/**
 * Created by sogubaby on 2018/1/9.
 */

public class CacheUtils {

    public CacheUtils(Context mContext) {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, ArrayList<MessageBean>>(cacheSize) {
            @Override
            protected int sizeOf(String key, ArrayList<MessageBean> value) {
                //return value.getByteCount() / 1024;
                return value.size();
            }
        };


        // 初始化DiskLruCache
        File diskCacheDir = getDiskCacheDir(mContext, "data");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs();
        }

        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1,
                        DISK_CACHE_SIZE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public ArrayList<MessageBean> getMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    private LruCache<String, ArrayList<MessageBean>> mMemoryCache;

    public void addToMemoryCache(String key, ArrayList<MessageBean> data) {
        mMemoryCache.put(key, data);
    }

    private DiskLruCache mDiskLruCache;
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 10;// 10M
    private Gson gson = new Gson();

    public ArrayList<MessageBean> getDiskCache(String key) {
        ArrayList<MessageBean> retData = new ArrayList<MessageBean>();
        try {
            //若snapshot为空，表明该key对应的文件不在缓存中
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashKeyForDisk(key));
            if (snapshot != null) {
                FileInputStream fileInputStream = (FileInputStream) snapshot
                        .getInputStream(DISK_CACHE_INDEX);
                byte[] buffer = new byte[1024];//尽可能大
                int len = 0;
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                while ((len = fileInputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                String data = new String(outStream.toByteArray(), "UTF-8");
                outStream.close();
                snapshot.close();

                //snapshot.getString(DISK_CACHE_INDEX);

                retData = gson.fromJson(data, new TypeToken<ArrayList<MessageBean>>() {
                }.getType());//把JSON格式的字符串转为List
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return retData;
    }

    public void addToDiskCache(String key, ArrayList<MessageBean> data) {
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(hashKeyForDisk(key));
            if (editor != null) {
                OutputStream outputStream = editor
                        .newOutputStream(DISK_CACHE_INDEX);
                outputStream.write(gson.toJson(data).getBytes("UTF-8"));
                editor.commit();
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
        }
    }

    public void addToCityCache(String key, ArrayList<CityArea> data) {
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(hashKeyForDisk(key));
            if (editor != null) {
                OutputStream outputStream = editor
                        .newOutputStream(DISK_CACHE_INDEX);
                outputStream.write(gson.toJson(data).getBytes("UTF-8"));
                editor.commit();
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
        }
    }

    public ArrayList<CityArea> getCityCache(String key) {
        ArrayList<CityArea> retData = new ArrayList<CityArea>();
        try {
            //若snapshot为空，表明该key对应的文件不在缓存中
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashKeyForDisk(key));
            if (snapshot != null) {
                FileInputStream fileInputStream = (FileInputStream) snapshot
                        .getInputStream(DISK_CACHE_INDEX);
                byte[] buffer = new byte[1024];//尽可能大
                int len = 0;
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                while ((len = fileInputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                String data = new String(outStream.toByteArray(), "UTF-8");
                outStream.close();
                snapshot.close();

                //snapshot.getString(DISK_CACHE_INDEX);

                retData = gson.fromJson(data, new TypeToken<ArrayList<CityArea>>() {
                }.getType());//把JSON格式的字符串转为List
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return retData;
    }

    public void close() {
        if (mDiskLruCache != null && !mDiskLruCache.isClosed()) {
            try {
                mDiskLruCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static final int DISK_CACHE_INDEX = 0;

    private File getDiskCacheDir(Context context, String name) {
        boolean externalStorageAvailable = Environment
                .getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        String cachePath;
        if (externalStorageAvailable) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + name);
    }

    private long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        StatFs statFs = new StatFs(path.getPath());
        return statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
    }

    public String hashKeyForDisk(String key) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(key.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(key.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }


    public String getScript(String key) {
        try {
            //若snapshot为空，表明该key对应的文件不在缓存中
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(hashKeyForDisk(key));
            if (snapshot != null) {
                FileInputStream fileInputStream = (FileInputStream) snapshot
                        .getInputStream(DISK_CACHE_INDEX);
                byte[] buffer = new byte[1024];//尽可能大
                int len = 0;
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                while ((len = fileInputStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                String data = new String(outStream.toByteArray(), "UTF-8");
                outStream.close();
                snapshot.close();
                return data;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return "";
    }

    public void saveScript(String key, String script) {
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(hashKeyForDisk(key));
            if (editor != null) {
                OutputStream outputStream = editor
                        .newOutputStream(DISK_CACHE_INDEX);
                outputStream.write(script.getBytes("UTF-8"));
                editor.commit();
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
        }
    }
}
