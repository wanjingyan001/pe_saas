package com.sogukj.pe.baselibrary.widgets.snackbar;

import com.sogukj.pe.baselibrary.R;

/**
 * Created by admin on 2018/5/29.
 */

public enum Prompt {
    /**
     * 红色,错误
     */
    ERROR(R.drawable.icon_toast_fail, R.color.prompt_error),

    /**
     * 红色,警告
     */
    WARNING(R.drawable.icon_toast_fail, R.color.prompt_warning),

    /**
     * 绿色,成功
     */
    SUCCESS(R.drawable.icon_toast_success, R.color.prompt_success);

    private int resIcon;
    private int backgroundColor;

    Prompt(int resIcon, int backgroundColor) {
        this.resIcon = resIcon;
        this.backgroundColor = backgroundColor;
    }

    public int getResIcon() {
        return resIcon;
    }

    public void setResIcon(int resIcon) {
        this.resIcon = resIcon;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}