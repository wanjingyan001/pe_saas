package com.sogukj.pe.baselibrary.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ClipboardManager;
import android.content.ComponentCallbacks;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.NestedScrollView;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.sogukj.pe.baselibrary.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class Utils {

    public static final String TAG = Utils.class.getSimpleName();
    /**
     * 正则表达式:验证邮箱
     */
    public static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    public static final String CREDIT_CODE = "[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}";
    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int spToPx(Context context, int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

    public static float pxToDp(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, context.getResources().getDisplayMetrics());
    }

    /**
     * 获取字体高度
     */
    public static float getTextHeight(Paint p) {
        Paint.FontMetrics fm = p.getFontMetrics();// 获取字体高度
        return (float) ((Math.ceil(fm.descent - fm.top) + 2) / 2);
    }

    public static String stringFilter(String str) throws PatternSyntaxException {
        // 只允许字母、数字和汉字
        String regEx = "[^a-zA-Z0-9\u4E00-\u9FA5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }


    public static void closeInput(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isOpen = imm.isActive();
        if (isOpen) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void forceCloseInput(Context context,View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public static void showInput(Context context, View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInputFromInputMethod(view.getWindowToken(), InputMethodManager.SHOW_FORCED);
    }

    public static void toggleSoftInput(Context context, View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
    }

    public static void showSoftInputFromWindow(Context activity, EditText editText) {
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }

    /**
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getIMEI(Context context) {
        TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        return telephonemanage.getDeviceId();
    }

    /**
     * 实现文本复制功能
     */
    public static void copy(Context context, String content) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(content.trim());
    }

    /**
     * 实现粘贴功能
     */
    public static String paste(Context context) {
        ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        return cmb.getText().toString().trim();
    }

    /**
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    public static String getIMSI(Context context) {
        TelephonyManager telephonemanage = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        return telephonemanage.getSubscriberId();
    }

    public static boolean isMobile(final CharSequence str) {
        Pattern p = Pattern.compile("^[1][0-9]{10}$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static int[] getAndroidScreenProperty(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        float density = dm.density;
        int densityDpi = dm.densityDpi;
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);
        int screenHeight = (int) (height / density);
        int[] ints = new int[2];
        ints[0] = screenWidth;
        ints[1] = screenHeight;
        return ints;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void setWindowStatusBarColor(Activity activity, int colorResId) {
        StatusBarUtil.setColor(activity, activity.getResources().getColor(colorResId), 0);
        if (colorResId == R.color.white) {
            StatusBarUtil.setLightMode(activity);
        }
    }


    public static void hideNavigationBar(Activity activity) {
        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (!isImmersiveModeEnabled) {
            if (Build.VERSION.SDK_INT >= 14) {
                newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            if (Build.VERSION.SDK_INT >= 16) {
                newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
            if (Build.VERSION.SDK_INT >= 18) {
                newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        }
    }


    /**
     * 正则：手机号（精确）
     * <p>移动：134(0-8)、135、136、137、138、139、147、150、151、152、157、158、159、178、182、183、184、187、188</p>
     * <p>联通：130、131、132、145、155、156、171、175、176、185、186</p>
     * <p>电信：133、153、173、177、180、181、189</p>
     * <p>全球星：1349</p>
     * <p>虚拟运营商：170</p>
     */
    public static final String REGEX_MOBILE_EXACT = "^((13[0-9])|(14[5,7])|(15[0-3,5-9])|(17[0,1,3,5-8])|(18[0-9])|(147))\\d{8}$";
    /**
     * 正则：身份证号码18位
     */
    public static final String REGEX_ID_CARD18 = "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}([0-9Xx])$";

    /**
     * 验证手机号（精确）
     *
     * @param input 待验证文本
     * @return {@code true}: 匹配<br>{@code false}: 不匹配
     */
    public static boolean isMobileExact(final CharSequence input) {
        return isMatch(REGEX_MOBILE_EXACT, input);
    }

    /**
     * 验证身份证号码 18 位
     *
     * @param input 待验证文本
     * @return {@code true}: 匹配<br>{@code false}: 不匹配
     */
    public static boolean isIDCard18(final CharSequence input) {
        //return isMatch(REGEX_ID_CARD18, input);
        return isIDCard(input.toString());
    }

    /**
     * 判断是否匹配正则
     *
     * @param regex 正则表达式
     * @param input 要匹配的字符串
     * @return {@code true}: 匹配<br>{@code false}: 不匹配
     */
    public static boolean isMatch(final String regex, final CharSequence input) {
        return input != null && input.length() > 0 && Pattern.matches(regex, input);
    }


    @SuppressLint("SimpleDateFormat")
    public static String getTime(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM");
        return format.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTime_(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
        return format.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getYMD(Date date) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTime(long time) {//可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTime(long time, @NonNull String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getTime(Date date, @NonNull String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String getWeek(long time) {
        SimpleDateFormat format = new SimpleDateFormat("E", Locale.CHINA);
        return format.format(new Date(time));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDayFromDate(long time) {
        SimpleDateFormat format = new SimpleDateFormat("dd");
        return format.format(new Date(time));
    }

    public static long getTime(String time){
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd mm:HH");
        long timestamp = 0;
        try {
            timestamp = format.parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestamp;
    }

    public static String getTimeDate(long time) throws ParseException {
        if (IsYesterday(getYMD(new Date(time)))) {
            return "昨天";
        } else if (IsToday(getYMD(new Date(time)))) {
            return isAM(time) + "" + getTime(time, "hh:mm");
        } else {
            return getTime(time, "yyyy年MM月dd日");
        }
    }

    /**
     * 对情报页面的时间进行处理
     *
     * @param time
     * @return
     * @throws ParseException
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatDate(String time) throws ParseException {
        Date date;
        if (time.contains(" ")) {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time);
        } else {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(time);
        }
        if (IsYesterday(time)) {
            return "昨天  " + getTime(date, "HH:mm");
        } else if (IsToday(time)) {
            return getTime(date, "HH:mm");
        } else if (isThisYear(time)) {
            return new SimpleDateFormat("MM月dd日 HH:mm").format(date);
        } else {
            return new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(date);
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDingDate(String time) throws ParseException {
        Date date;
        if (time.contains(" ")) {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time);
        } else {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(time);
        }
        if (IsYesterday(time)) {
            return "昨天";
        } else if (IsToday(time)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int am = calendar.get(Calendar.AM_PM);
            String timeStr = getTime(date, "hh:mm");
            if (am == 0) {
                return "上午 " + (timeStr.startsWith("0") ? timeStr.substring(1, timeStr.length()) : timeStr);
            } else {
                return "下午 " + (timeStr.startsWith("0") ? timeStr.substring(1, timeStr.length()) : timeStr);
            }
        } else if (isThisYear(time)) {
            return new SimpleDateFormat("MM月dd日").format(date);
        } else {
            return new SimpleDateFormat("yyyy年MM月dd日").format(date);
        }
    }

    /**
     * 对情报页面的时间进行处理
     *
     * @param time
     * @return
     * @throws ParseException
     */
    @SuppressLint("SimpleDateFormat")
    public static String formatDate2(String time) throws ParseException {
        Date date;
        if (time.contains(" ")) {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(time);
        } else {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(time);
        }
        if (IsYesterday(time)) {
            return "昨天";
        } else if (IsToday(time)) {
            return "";
        } else if (isThisYear(time)) {
            return new SimpleDateFormat("MM月dd日").format(date);
        } else {
            return new SimpleDateFormat("yyyy年MM月dd日").format(date);
        }
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDate(String time, String format) throws ParseException {
        Date date = new SimpleDateFormat(format).parse(time);
        String formatStr;
        if (IsYesterday(time)) {
            if (time.contains(" ")) {
                formatStr = "昨天" + getTime(date, "HH:mm");
            } else {
                formatStr = "昨天";
            }
        } else if (IsToday(time)) {
            if (time.contains(" ")) {
                formatStr = "" + getTime(date, "HH:mm");
            } else {
                formatStr = "";
            }
        } else if (isThisYear(time)) {
            if (time.contains(" ")) {
                formatStr = new SimpleDateFormat("MM月dd日 HH:mm").format(date);
            } else {
                formatStr = new SimpleDateFormat("MM月dd日").format(date);
            }
        } else {
            if (time.contains(" ")) {
                formatStr = new SimpleDateFormat("yyyy年MM月dd日 HH:mm").format(date);
            } else {
                formatStr = new SimpleDateFormat("yyyy年MM月dd日").format(date);
            }
        }
        return formatStr;
    }


    public static int[] getYMDInCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)};
    }

    public static int[] getYMDHMInCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return new int[]{cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)};
    }

    public static String isAM(long time) {
        Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        int apm = mCalendar.get(Calendar.AM_PM);
        //apm=0 表示上午，apm=1表示下午
        return apm == 0 ? "上午" : "下午";
    }


    /**
     * 判断是否为今天(效率比较高)
     *
     * @param day 传入的 时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
     * @return true今天 false不是
     * @throws ParseException
     */
    public static boolean IsToday(String day) throws ParseException {

        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);
        Calendar cal = Calendar.getInstance();
        Date date = getDateFormat().parse(day);
        cal.setTime(date);
        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                    - pre.get(Calendar.DAY_OF_YEAR);

            if (diffDay == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为昨天(效率比较高)
     *
     * @param day 传入的 时间  "2016-06-28 10:10:30" "2016-06-28" 都可以
     * @return true今天 false不是
     * @throws ParseException
     */
    public static boolean IsYesterday(String day) throws ParseException {

        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        Date date = getDateFormat().parse(day);
        cal.setTime(date);

        if (cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR))) {
            int diffDay = cal.get(Calendar.DAY_OF_YEAR)
                    - pre.get(Calendar.DAY_OF_YEAR);

            if (diffDay == -1) {
                return true;
            }
        }
        return false;
    }

    public static boolean isThisYear(String day) throws ParseException {
        Calendar pre = Calendar.getInstance();
        Date predate = new Date(System.currentTimeMillis());
        pre.setTime(predate);

        Calendar cal = Calendar.getInstance();
        Date date = getDateFormat().parse(day);
        cal.setTime(date);
        return cal.get(Calendar.YEAR) == (pre.get(Calendar.YEAR));
    }

    public static SimpleDateFormat getDateFormat() {
        if (null == DateLocal.get()) {
            DateLocal.set(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA));
        }
        return DateLocal.get();
    }


    private static ThreadLocal<SimpleDateFormat> DateLocal = new ThreadLocal<SimpleDateFormat>();

    public static Long[] getSupportBeginDayofMonth(int year, int monthOfYear) {
        Calendar cal = Calendar.getInstance();
        // 不加下面2行，就是取当前时间前一个月的第一天及最后一天
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Date lastDate = cal.getTime();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDate = cal.getTime();

        return new Long[]{firstDate.getTime(), lastDate.getTime()};
    }

    public static void setUpIndicatorWidth(TabLayout tabLayout, int marginLeft, int marginRight, Context context) {
        Class<?> tabLayoutClass = tabLayout.getClass();
        Field tabStrip = null;
        try {
            tabStrip = tabLayoutClass.getDeclaredField("mTabStrip");
            tabStrip.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        LinearLayout layout = null;
        try {
            if (tabStrip != null) {
                layout = (LinearLayout) tabStrip.get(tabLayout);
            }
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                child.setPadding(0, 0, 0, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    params.setMarginStart(dpToPx(context, marginLeft));
                    params.setMarginEnd(dpToPx(context, marginRight));
                }
                child.setLayoutParams(params);
                child.invalidate();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void setTabWidth(TabLayout tabLayout, Context context, int newWidth) {
        try {
            //拿到tabLayout的mTabStrip属性
            Field mTabStripField = tabLayout.getClass().getDeclaredField("mTabStrip");
            mTabStripField.setAccessible(true);

            LinearLayout mTabStrip = (LinearLayout) mTabStripField.get(tabLayout);

            int dp10 = dpToPx(context, 10);

            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                View tabView = mTabStrip.getChildAt(i);

                //拿到tabView的mTextView属性
                Field mTextViewField = tabView.getClass().getDeclaredField("mTextView");
                mTextViewField.setAccessible(true);

                TextView mTextView = (TextView) mTextViewField.get(tabView);

                tabView.setPadding(0, 0, 0, 0);

                //因为我想要的效果是   字多宽线就多宽，所以测量mTextView的宽度
                int width = 0;
                width = mTextView.getWidth();
                if (width == 0) {
                    mTextView.measure(0, 0);
                    width = mTextView.getMeasuredWidth();
                }

                //设置tab左右间距为10dp  注意这里不能使用Padding 因为源码中线的宽度是根据 tabView的宽度来设置的
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tabView.getLayoutParams();
                params.width = newWidth;
                //params.leftMargin = dp10;
                //params.rightMargin = dp10;
                tabView.setLayoutParams(params);

                tabView.invalidate();
            }

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    //适配
    public static void changeTabIcon(TabLayout.Tab tab) throws Exception {
        Class<?> tabClass = tab.getClass();
        Field mTabView = tabClass.getDeclaredField("mView");
        mTabView.setAccessible(true);
        LinearLayout layout = (LinearLayout) mTabView.get(tab);//mView

        Class<?> iconClass = layout.getClass();
        Field mIconView = iconClass.getDeclaredField("mIconView");
        mIconView.setAccessible(true);
        ImageView img = (ImageView) mIconView.get(layout);//mIconView
        img.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    /**
     * 生成视图的预览
     *
     * @param activity
     * @param v
     * @return 视图生成失败返回null
     * 视图生成成功返回视图的绝对路径
     */
    public static boolean saveImage(Activity activity, View v, String path) {
        Bitmap bitmap;
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        bitmap = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        try {
            bitmap = Bitmap.createBitmap(bitmap, location[0], location[1], v.getWidth(), v.getHeight());
            FileOutputStream fout = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "生成预览图片失败：" + e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "width is <= 0, or height is <= 0");
        } finally {
            // 清理缓存
            view.destroyDrawingCache();
        }
        return false;

    }

    public static InputFilter[] getFilter(final Context context) {
        InputFilter filter = new InputFilter() {
            Pattern pattern = Pattern.compile("[^\\u0000-\\uFFFF]");

            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                if (charSequence == "") {
                    return null;
                } else {
                    Matcher matcher = pattern.matcher(charSequence);
                    if (!matcher.find()) {
                        return null;
                    } else {
                        Toast.makeText(context, "只能输入汉字,英文，数字和标点符号", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }
        };
        return new InputFilter[]{filter};
    }

    /**
     * 检测网络连接状态
     *
     * @param context
     * @return
     */
    public static boolean isNetworkError(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @param scrollView
     * @param filePath(不带后缀名)
     */
    public static boolean saveNestedScroolViewImage(NestedScrollView scrollView, String filePath) {
        int h = 0;
        Bitmap bitmap = null;
        // 获取scrollview实际高度
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
            scrollView.getChildAt(i).setBackgroundColor(
                    Color.parseColor("#ffffff"));
        }
        // 创建对应大小的bitmap
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.RGB_565);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);

        //保存bitmap
        try {
            File outfile = new File(filePath);
            // 如果文件不存在，则创建一个新文件
            if (!outfile.isDirectory()) {
                outfile.mkdirs();
            }
            String fname = outfile + "/EquityStructure.png";
            File file = new File(fname);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = null;
            fos = new FileOutputStream(fname);
            if (null != fos) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void saveImageToGallery(Context context, String path) {
        File file = new File(path);
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), "BusinessCard.png", null);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    }


    public static String getFileAbsolutePathByUri(Activity context, Uri fileUri) {
        if (context == null || fileUri == null) {
            return null;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, fileUri)) {
            if (isExternalStorageDocument(fileUri)) {
                String docId = DocumentsContract.getDocumentId(fileUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(fileUri)) {
                String id = DocumentsContract.getDocumentId(fileUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(fileUri)) {
                String docId = DocumentsContract.getDocumentId(fileUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(fileUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(fileUri)) {
                return fileUri.getLastPathSegment();
            }
            return getDataColumn(context, fileUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(fileUri.getScheme())) {
            return fileUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    //数字表达式
    private final static Pattern number_pattern = Pattern.compile("^[0-9]*$");

    /**
     * 验证是数字
     *
     * @param str 验证字符
     * @return boolean
     */
    public static boolean isNumber(String str) {
        return number_pattern.matcher(str).matches();
    }

    /**
     * 验证身份证号码是否正确
     * @param IDCardNo 身份证号码
     * @return boolean
     */
    public static boolean isIDCard(String IDCardNo) {
        //记录错误信息
        String errmsg = "";
        String[] ValCodeArr = {"1", "0", "x", "9", "8", "7", "6", "5", "4", "3", "2"};
        String[] Wi = {"7", "9", "10", "5", "8", "4", "2", "1", "6", "3", "7", "9", "10", "5", "8", "4", "2"};
        String Ai = "";

        //================ 身份证号码的长度 15位或18位 ================
        if (IDCardNo.length() != 15 && IDCardNo.length() != 18) {
            errmsg = "身份证号码长度应该为15位或18位!";
            return false;
        }

        //================ 数字 除最后以为都为数字 ================
        if (IDCardNo.length() == 18) {
            Ai = IDCardNo.substring(0, 17);
        } else if (IDCardNo.length() == 15) {
            Ai = IDCardNo.substring(0, 6) + "19" + IDCardNo.substring(6, 15);
        }
        if (isNumber(Ai) == false) {
            errmsg = "身份证15位号码都应为数字 ; 18位号码除最后一位外，都应为数字";
            return false;
        }

        //================ 出生年月是否有效 ================
        //年份
        String strYear = Ai.substring(6, 10);
        //月份
        String strMonth = Ai.substring(10, 12);
        //日
        String strDay = Ai.substring(12, 14);
        if (!getDateIsTrue(strYear, strMonth, strDay)) {
            errmsg = "身份证生日无效";
            return false;
        }
        GregorianCalendar gc = new GregorianCalendar();
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if ((gc.get(Calendar.YEAR) - Integer.parseInt(strYear)) > 150 || (gc.getTime().getTime() - s.parse(strYear + "-" + strMonth + "-" + strDay).getTime()) < 0) {
                errmsg = "身份证生日不在有效范围";
                return false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            errmsg = "身份证生日不在有效范围";
            return false;
        } catch (java.text.ParseException e1) {
            e1.printStackTrace();
            errmsg = "身份证生日不在有效范围";
            return false;
        }
        if (Integer.parseInt(strMonth) > 12 || Integer.parseInt(strMonth) == 0) {
            errmsg = "身份证月份无效";
            return false;
        }
        if (Integer.parseInt(strDay) > 31 || Integer.parseInt(strDay) == 0) {
            errmsg = "身份证日期无效";
            return false;
        }

        //================ 地区码时候有效 ================
        Hashtable hashtable = getAreaCodeAll();
        if (hashtable.get(Ai.substring(0, 2)) == null) {
            errmsg = "身份证地区编码错误";
            return false;
        }

        //================ 判断最后一位的值 ================
        int TotalmulAiWi = 0;
        for (int i = 0; i < 17; i++) {
            TotalmulAiWi = TotalmulAiWi + Integer.parseInt(String.valueOf(Ai.charAt(i))) * Integer.parseInt(Wi[i]);
        }
        int modValue = TotalmulAiWi % 11;
        String strVerifyCode = ValCodeArr[modValue];
        Ai = Ai + strVerifyCode;
        if (IDCardNo.length() == 18) {
            if (Ai.equals(IDCardNo) == false) {
                errmsg = "身份证无效，不是合法的身份证号码";
                return false;
            }
        } else {
            return true;
        }
        return true;
    }

    /**
     * 检查日期是否有效
     *
     * @param year  年
     * @param month 月
     * @param day   日
     * @return boolean
     */
    public static boolean getDateIsTrue(String year, String month, String day) {
        try {
            String data = year + month + day;
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
            simpledateformat.setLenient(false);
            simpledateformat.parse(data);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * 获取身份证号所有区域编码设置
     *
     * @return Hashtable
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Hashtable getAreaCodeAll() {
        Hashtable hashtable = new Hashtable();
        hashtable.put("11", "北京");
        hashtable.put("12", "天津");
        hashtable.put("13", "河北");
        hashtable.put("14", "山西");
        hashtable.put("15", "内蒙古");
        hashtable.put("21", "辽宁");
        hashtable.put("22", "吉林");
        hashtable.put("23", "黑龙江");
        hashtable.put("31", "上海");
        hashtable.put("32", "江苏");
        hashtable.put("33", "浙江");
        hashtable.put("34", "安徽");
        hashtable.put("35", "福建");
        hashtable.put("36", "江西");
        hashtable.put("37", "山东");
        hashtable.put("41", "河南");
        hashtable.put("42", "湖北");
        hashtable.put("43", "湖南");
        hashtable.put("44", "广东");
        hashtable.put("45", "广西");
        hashtable.put("46", "海南");
        hashtable.put("50", "重庆");
        hashtable.put("51", "四川");
        hashtable.put("52", "贵州");
        hashtable.put("53", "云南");
        hashtable.put("54", "西藏");
        hashtable.put("61", "陕西");
        hashtable.put("62", "甘肃");
        hashtable.put("63", "青海");
        hashtable.put("64", "宁夏");
        hashtable.put("65", "新疆");
        hashtable.put("71", "台湾");
        hashtable.put("81", "香港");
        hashtable.put("82", "澳门");
        hashtable.put("91", "国外");
        return hashtable;
    }

    /**
     * 金额分割，四舍五人金额
     *
     * @param s
     * @return
     */
    public static String formatMoney(BigDecimal s) {
        String retVal = "";
        String str = "";
        boolean is_positive_integer = false;
        if (null == s) {
            return "0.00";
        }

        if (0 == s.doubleValue()) {
            return "0.00";
        }
        //判断是否正整数
        is_positive_integer = s.toString().contains("-");
        //是负整数
        if (is_positive_integer) {
            //去掉 - 号
            s = new BigDecimal(s.toString().substring(1, s.toString().length()));
        }
        str = s.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        StringBuffer sb = new StringBuffer();
        String[] strs = str.split("\\.");
        int j = 1;
        for (int i = 0; i < strs[0].length(); i++) {
            char a = strs[0].charAt(strs[0].length() - i - 1);
            sb.append(a);
            if (j % 3 == 0 && i != strs[0].length() - 1) {
                sb.append(",");
            }
            j++;
        }
        String str1 = sb.toString();
        StringBuffer sb1 = new StringBuffer();
        for (int i = 0; i < str1.length(); i++) {
            char a = str1.charAt(str1.length() - 1 - i);
            sb1.append(a);
        }
        sb1.append(".");
        sb1.append(strs[1]);
        retVal = sb1.toString();

        if (is_positive_integer) {
            retVal = "-" + retVal;
        }
        return retVal;
    }

    private static float sNonCompatDensity;
    private static float sNonCompatScaledDensity;

    public static void setCustomDensity(@NonNull Activity activity, @NonNull final Application application) {
        final DisplayMetrics appDisplayMetrics = application.getResources().getDisplayMetrics();
        if (sNonCompatDensity == 0) {
            sNonCompatDensity = appDisplayMetrics.density;
            sNonCompatScaledDensity = appDisplayMetrics.scaledDensity;
            application.registerComponentCallbacks(new ComponentCallbacks() {
                @Override
                public void onConfigurationChanged(Configuration newConfig) {
                    if (newConfig != null && newConfig.fontScale > 0) {
                        sNonCompatScaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                    }
                }

                @Override
                public void onLowMemory() {

                }
            });
            final float targetDensity = appDisplayMetrics.widthPixels / 375;
            final float targetScaledDensity = targetDensity * (sNonCompatScaledDensity / sNonCompatDensity);
            int targetDensityDpi = (int) (targetDensity * 160);

            appDisplayMetrics.density = targetDensity;
            appDisplayMetrics.scaledDensity = targetScaledDensity;
            appDisplayMetrics.densityDpi = targetDensityDpi;

            final DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
            metrics.density = targetDensity;
            metrics.scaledDensity = targetScaledDensity;
            metrics.densityDpi = targetDensityDpi;
        }
    }

    /**
     * get Screen Width
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * get Screen height
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 sp,字体的转换
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 sp 的单位 转成为 px(像素),字体的转换
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    public static void saveBitmapFromShareView(View v, String name, Context context) {
        if (v.getWidth() <= 0 || v.getHeight() <= 0) {
            return;
        }
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())    //外部存储是否挂载
                || !Environment.isExternalStorageRemovable())   //外部存储是否移除
                ) {

            File file = new File(Environment.getExternalStorageDirectory(),
                    name);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream out = null;
            try {
                // 打开指定文件输出流
                out = new FileOutputStream(file);
                // 将位图输出到指定文件
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                        out);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * @param view 需要截取图片的view
     * @return 截图
     */
    public static void saveLocationImage(View view, String name, Activity context) throws Exception {

        View screenView = context.getWindow().getDecorView();
        screenView.setDrawingCacheEnabled(true);
        screenView.buildDrawingCache();

        //获取屏幕整张图片
        Bitmap bitmap = screenView.getDrawingCache();

        if (bitmap != null) {

            //需要截取的长和宽
            int outWidth = view.getWidth();
            int outHeight = view.getHeight();

            //获取需要截图部分的在屏幕上的坐标(view的左上角坐标）
            int[] viewLocationArray = new int[2];
            view.getLocationOnScreen(viewLocationArray);

            //从屏幕整张图片中截取指定区域
//            bitmap = Bitmap.createBitmap(bitmap, viewLocationArray[0], viewLocationArray[1], outWidth, outHeight);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())    //外部存储是否挂载
                    || !Environment.isExternalStorageRemovable())   //外部存储是否移除
                    ) {

                File file = new File(Environment.getExternalStorageDirectory(),
                        name);
                if (file.exists()) {
                    file.delete();
                }
                FileOutputStream out = null;
                try {
                    // 打开指定文件输出流
                    out = new FileOutputStream(file);
                    // 将位图输出到指定文件
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                            out);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }

    }

    public static void saveImageFromMap(Bitmap bitmap, String name, Context context, int status) {

        if ((Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())    //外部存储是否挂载
                || !Environment.isExternalStorageRemovable())   //外部存储是否移除
                ) {

            File file = new File(Environment.getExternalStorageDirectory(),
                    name);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream out = null;
            try {
                // 打开指定文件输出流
                out = new FileOutputStream(file);
                // 将位图输出到指定文件
                boolean b = bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
                        out);
                out.close();
                if (b) {
                    Toast.makeText(context, "截屏成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "截屏失败", Toast.LENGTH_SHORT).show();
                }
                if (status != 0) {
                    Toast.makeText(context, "地图渲染完成，截屏无网格", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "地图未渲染完成，截屏有网格", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                Toast.makeText(context, "截屏失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    /**
     * 得到json文件中的内容
     *
     * @param context
     * @param fileName
     * @return
     */
    public static String getJson(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        //获得assets资源管理器
        AssetManager assetManager = context.getAssets();
        //使用IO流读取json文件内容
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName), "utf-8"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /**
     * 将字符串转换为 对象
     *
     * @param json
     * @param type
     * @return
     */
    public static <T> T JsonToObject(String json, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

    public static String objToJson(Object obj) {
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public static String getStockCode(String code) {
        String resultCode = "";
        if (code.startsWith("6")) {
            resultCode = "SH" + code;
        } else {
            resultCode = "SZ" + code;
        }
        return resultCode;
    }

    public static void copyShareFile(String environment,Context context) {
        switch (environment) {
            case "civc":
                File civcFile = new File(Environment.getExternalStorageDirectory(), "ic_launcher_zd.png");
                if(!civcFile.exists()) {
                    copyAssets("ic_launcher_zd.png",context);
                }
                break;
            case "ht":
                File htFile = new File(Environment.getExternalStorageDirectory(), "ic_launcher_ht.png");
                if(!htFile.exists()) {
                    copyAssets("ic_launcher_ht.png",context);
                }
                break;
            case "kk":
                File kkFile = new File(Environment.getExternalStorageDirectory(), "ic_launcher_kk.png");
                if(!kkFile.exists()) {
                    copyAssets("ic_launcher_kk.png",context);
                }
                break;
            case "yge":
                File ygeFile = new File(Environment.getExternalStorageDirectory(), "ic_launcher_yge.png");
                if(!ygeFile.exists()) {
                    copyAssets("ic_launcher_yge.png",context);
                }
                break;
            case "sr":
                File srFile = new File(Environment.getExternalStorageDirectory(), "ic_launcher_sr.png");
                if(!srFile.exists()) {
                    copyAssets("ic_launcher_sr.png",context);
                }
                break;
            default:
                File file = new File(Environment.getExternalStorageDirectory(), "ic_launcher_pe.png");
                if(!file.exists()) {
                    copyAssets("ic_launcher_pe.png",context);
                }
                break;
        }
    }

    private static void copyAssets(String filename,Context context) {
        AssetManager assetManager = context.getAssets();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            File outFile = new File(Environment.getExternalStorageDirectory(), filename);
            out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            out.flush();
            out.close();
            Log.i("copyasset", "success " + outFile.getAbsolutePath());
        } catch (IOException e) {
            Log.e("tag", "Failed to copy asset file: " + filename, e);
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * 校验邮箱
     * @param email
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isEmail(String email) {
        return Pattern.matches(REGEX_EMAIL, email);
    }

    public static boolean isCreditCode(String code){
        return Pattern.matches(CREDIT_CODE,code);
    }


    //判断整数（int）
    public static boolean isInteger(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    //判断浮点数（double和float）
    public static boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }

    public static String reserveTwoDecimal(float number){
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        return decimalFormat.format(number);
    }
    public static String reserveTwoDecimal(double number){
        double value = new BigDecimal(number).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
        return String.valueOf(value);
    }

    public static double reserveTwoDecimal(double number,int count){
        double value = new BigDecimal(number).setScale(count, BigDecimal.ROUND_DOWN).doubleValue();
        return value;
    }

    public static void setBackgroundAlpha(Context mContext, float bgAlpha) {
//        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
//        lp.alpha = bgAlpha;
//        ((Activity) mContext).getWindow().setAttributes(lp);

        if (bgAlpha == 1f) {
            clearDim((Activity) mContext);
        }else{
            applyDim((Activity) mContext, bgAlpha);
        }
    }

    private static void applyDim(Activity activity, float bgAlpha) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ViewGroup parent = (ViewGroup) activity.getWindow().getDecorView().getRootView();
            //activity跟布局
//        ViewGroup parent = (ViewGroup) parent1.getChildAt(0);
            Drawable dim = new ColorDrawable(Color.BLACK);
            dim.setBounds(0, 0, parent.getWidth(), parent.getHeight());
            dim.setAlpha((int) (255 * bgAlpha));
            ViewGroupOverlay overlay = parent.getOverlay();
            overlay.add(dim);
        }
    }

    private static void clearDim(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ViewGroup parent = (ViewGroup) activity.getWindow().getDecorView().getRootView();
            //activity跟布局
//        ViewGroup parent = (ViewGroup) parent1.getChildAt(0);
            ViewGroupOverlay overlay = parent.getOverlay();
            overlay.clear();
        }
    }

}
