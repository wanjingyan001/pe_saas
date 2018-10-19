package com.sogukj.pe.module.score;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.listener.OnOptionsSelectListener;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.sogukj.pe.R;
import com.sogukj.pe.bean.PFBZ;

import java.lang.reflect.Field;
import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.MainThreadDisposable;

/**
 * Created by sogubaby on 2017/12/15.
 */

class TextViewClickObservable extends Observable<Integer> {
    private TextView view;
    private ProgressBar bar;
    private Context context;
    private OptionsPickerView pvOptions;
    private ArrayList<Integer> optionsItems_A;
    private ArrayList<Integer> optionsItems_B;
    private ArrayList<Integer> optionsItems_C;
    private ArrayList<Integer> optionsItems_D;
    private ArrayList<Integer> mSelected;
    private ArrayList<PFBZ> biaozhun;

    TextViewClickObservable(Context context, TextView view, ProgressBar bar, ArrayList<PFBZ> biaozhun) {
        this.context = context;
        this.view = view;
        this.bar = bar;
        optionsItems_A = new ArrayList<Integer>();
        for (int i = Integer.parseInt(biaozhun.get(0).getSs()); i <= Integer.parseInt(biaozhun.get(0).getEs()); i++) {
            optionsItems_A.add(i);
        }
        optionsItems_B = new ArrayList<Integer>();
        for (int i = Integer.parseInt(biaozhun.get(1).getSs()); i <= Integer.parseInt(biaozhun.get(1).getEs()); i++) {
            optionsItems_B.add(i);
        }
        optionsItems_C = new ArrayList<Integer>();
        for (int i = Integer.parseInt(biaozhun.get(2).getSs()); i <= Integer.parseInt(biaozhun.get(2).getEs()); i++) {
            optionsItems_C.add(i);
        }
        optionsItems_D = new ArrayList<Integer>();
        for (int i = Integer.parseInt(biaozhun.get(3).getSs()); i <= Integer.parseInt(biaozhun.get(3).getEs()); i++) {
            optionsItems_D.add(i);
        }
        mSelected = optionsItems_D;
        this.biaozhun = biaozhun;
    }

    @Override
    protected void subscribeActual(Observer<? super Integer> observer) {
        Listener listener = new Listener(view, observer);
        observer.onSubscribe(listener);
        view.setOnClickListener(listener);
    }

    class Listener extends MainThreadDisposable implements View.OnClickListener {
        private final TextView view;
        private final Observer<? super Integer> observer;

        Listener(TextView view, Observer<? super Integer> observer) {
            this.view = view;
            this.observer = observer;
        }

        @Override
        public void onClick(View v) {
            if (!isDisposed()) {
                pvOptions = new OptionsPickerBuilder(context, (options1, option2, options3, v1) -> {
                    //返回的分别是三个级别的选中位置
                    int pro = mSelected.get(options1);
                    bar.setProgress(pro);
                    if (pro >= Integer.parseInt(biaozhun.get(0).getSs()) && pro <= Integer.parseInt(biaozhun.get(0).getEs())) {
                        bar.setProgressDrawable(context.getResources().getDrawable(R.drawable.pb_a));
                    } else if (pro >= Integer.parseInt(biaozhun.get(1).getSs()) && pro <= Integer.parseInt(biaozhun.get(1).getEs())) {
                        bar.setProgressDrawable(context.getResources().getDrawable(R.drawable.pb_b));
                    } else if (pro >= Integer.parseInt(biaozhun.get(2).getSs()) && pro <= Integer.parseInt(biaozhun.get(2).getEs())) {
                        bar.setProgressDrawable(context.getResources().getDrawable(R.drawable.pb_c));
                    } else if (pro >= Integer.parseInt(biaozhun.get(3).getSs()) && pro <= Integer.parseInt(biaozhun.get(3).getEs())) {
                        bar.setProgressDrawable(context.getResources().getDrawable(R.drawable.pb_d));
                    }
                    view.setText(pro + "");
                    view.setTextColor(Color.parseColor("#ffa0a4aa"));
                    view.setTextSize(16);
                    view.setBackgroundDrawable(null);

                    observer.onNext(pro);

                }).setDecorView(((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content)).build();
                pvOptions.setPicker(optionsItems_D);
                pvOptions.show();

                try {
                    //
                    Field btn_field = pvOptions.getClass().getDeclaredField("btnCancel");
                    btn_field.setAccessible(true);
                    Button btn = (Button) btn_field.get(pvOptions);
                    btn.setVisibility(View.GONE);

                    Field field = pvOptions.getClass().getDeclaredField("rv_top_bar");
                    field.setAccessible(true);
                    RelativeLayout layout = (RelativeLayout) field.get(pvOptions);
                    layout.removeViewAt(1);

                    final TabLayout tabs = (TabLayout) LayoutInflater.from(context).inflate(R.layout.picker_title, null);
                    layout.addView(tabs, 1);
                    tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            int pos = tab.getPosition();
                            if (pos == 0) {
                                pvOptions.setPicker(optionsItems_D);
                                mSelected = optionsItems_D;
                            } else if (pos == 1) {
                                pvOptions.setPicker(optionsItems_C);
                                mSelected = optionsItems_C;
                            } else if (pos == 2) {
                                pvOptions.setPicker(optionsItems_B);
                                mSelected = optionsItems_B;
                            } else if (pos == 3) {
                                pvOptions.setPicker(optionsItems_A);
                                mSelected = optionsItems_A;
                            }
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {
                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });
                    tabs.getTabAt(0).select();
                    mSelected = optionsItems_D;
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onDispose() {
            view.setOnClickListener(null);
        }
    }
}