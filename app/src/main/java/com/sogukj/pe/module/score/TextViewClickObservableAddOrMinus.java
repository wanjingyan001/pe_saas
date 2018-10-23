package com.sogukj.pe.module.score;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.sogukj.pe.R;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * Created by sogubaby on 2017/12/15.
 */
//加减分项
class TextViewClickObservableAddOrMinus extends Observable<Integer> {
    private TextView view;
    private ProgressBar bar;
    private Context context;
    private OptionsPickerView pvOptions;
    private ArrayList<Integer> mSelected;
    private int drawableId;

    TextViewClickObservableAddOrMinus(Context context, TextView view, ProgressBar bar, int total, int offset, int drawablwId) {
        this.context = context;
        this.view = view;
        this.bar = bar;
        mSelected = new ArrayList<Integer>();
        for (int i = 0; i <= total; i += offset) {
            mSelected.add(i);
        }
        bar.setMax(total);
        drawableId = drawablwId;
    }

    @Override
    protected void subscribeActual(Observer<? super Integer> observer) {
        Listener listener = new Listener(view, observer, drawableId);
        observer.onSubscribe(listener);
        view.setOnClickListener(listener);
    }

    class Listener extends MainThreadDisposable implements View.OnClickListener {
        private final TextView view;
        private final Observer<? super Integer> observer;
        private int drawableId;

        Listener(TextView view, Observer<? super Integer> observer, int drawablwId) {
            this.view = view;
            this.observer = observer;
            drawableId = drawablwId;
        }

        @Override
        public void onClick(View v) {
            if (!isDisposed()) {
                pvOptions = new OptionsPickerBuilder(context, (options1, option2, options3, v1) -> {
                    //返回的分别是三个级别的选中位置
                    int pro = mSelected.get(options1);
                    bar.setProgress(pro);
                    bar.setProgressDrawable(context.getResources().getDrawable(drawableId));
                    if (drawableId == R.drawable.pb_min) {
                        view.setText(-pro + "");
                    } else {
                        view.setText(pro + "");
                    }
                    view.setTextColor(Color.parseColor("#ffa0a4aa"));
                    view.setTextSize(16);
                    view.setBackgroundDrawable(null);

                    observer.onNext(pro);
                }).setDecorView(((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content)).build();
                pvOptions.setPicker(mSelected);
                pvOptions.show();
            }
        }

        @Override
        protected void onDispose() {
            view.setOnClickListener(null);
        }
    }
}