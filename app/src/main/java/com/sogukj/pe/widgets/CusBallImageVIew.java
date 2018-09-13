package com.sogukj.pe.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by CH-ZH on 2018/9/13.
 */
public class CusBallImageVIew extends android.support.v7.widget.AppCompatImageView {
    private Context context;
    public CusBallImageVIew(Context context) {
        this(context,null);
    }

    public CusBallImageVIew(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,-1);
    }

    public CusBallImageVIew(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
          super.onTouchEvent(event);
          switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN :

                    break;
                case MotionEvent.ACTION_MOVE :

                    break;
                case MotionEvent.ACTION_UP :

                    break;
            }

        return true;
    }
}
