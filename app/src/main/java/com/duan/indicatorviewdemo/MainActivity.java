package com.duan.indicatorviewdemo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private IndicatorView indicator1;
    private IndicatorView indicator2;
    private IndicatorView indicator3;
    private IndicatorView indicator4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        IndicatorView indicator = (IndicatorView) findViewById(R.id.main2_indicator);
        indicator.setOnIndicatorSeekListener(new IndicatorView.OnIndicatorSeekListener() {
            @Override
            public void onSeekChange(IndicatorView view, int distance, int dotPos) {
                Log.i(TAG, "onSeekChange: distance=" + distance + " dot=" + dotPos);
            }

            @Override
            public void onStartTrackingTouch(IndicatorView view) {
                Toast.makeText(MainActivity.this, "摸到了", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSopTrackingTouch(IndicatorView view) {
                Toast.makeText(MainActivity.this, "不摸了", Toast.LENGTH_SHORT).show();
            }
        });

        indicator.setOnIndicatorChangeListener(new IndicatorView.OnIndicatorChangeListener() {
            @Override
            public void onIndicatorChange(int currentPos, int oldPos) {
                Log.i(TAG, "onIndicatorChange: cuPos=" + currentPos + " oldPos=" + oldPos);
            }
        });

//        setContentView(R.layout.activity_main);
//        indicator1 = (IndicatorView) findViewById(R.id.indicator1);
//        indicator2 = (IndicatorView) findViewById(R.id.indicator2);
//        indicator3 = (IndicatorView) findViewById(R.id.indicator3);
//        indicator4 = (IndicatorView) findViewById(R.id.indicator4);
//        execuInV1();
//        execuInV2();
//        execuInV3();
//        execuInV4();

    }

    //代码动态指定属性
    private void execuInV1() {

        new Timer().schedule(new TimerTask() {
            Random random = new Random();

            @Override
            public void run() {
                indicator1.post(() -> {
                    indicator1.setIndicatorPos(random.nextInt(indicator1.getDotCount()));
                    indicator1.setIndicatorColor(Utils.getRandomColor());
                    indicator1.setDotColor(Utils.getRandomColor_d());
                    indicator1.setIndicatorSwitchAnim(random.nextInt(IndicatorView.INDICATOR_SWITCH_ANIM_SQUEEZE + 1));
                });
            }
        }, 2000, 2000);

    }

    //不可点击+自定义指示点触摸动画
    private void execuInV2() {
        indicator2.setOnIndicatorPressAnimator((IndicatorView view, IndicatorView.IndicatorHolder target) -> {
            int terminalColor = indicator2.getIndicatorColor();
            int centerColor = Color.RED;
            int centerColor2 = Color.BLUE;
            ValueAnimator animator = ObjectAnimator.ofArgb(target, "color", terminalColor, centerColor, centerColor2, terminalColor);

            int terminalSize = indicator2.getIndicatorPixeSize();
            int centerSize = indicator2.getIndicatorPixeSize() * 2;
            ValueAnimator animatorH = ObjectAnimator.ofInt(target, "height", terminalSize, centerSize, terminalSize);
            ValueAnimator animatorW = ObjectAnimator.ofInt(target, "width", terminalSize, centerSize, terminalSize);

            AnimatorSet set = new AnimatorSet();
            set.play(animator).with(animatorH).with(animatorW);
            set.setDuration(1000); //播放时为700

            return set;
        });

    }

    //线条不可见(线条长度仍需指定)+自定义指示点切换动画
    private void execuInV3() {

        indicator3.setOnIndicatorSwitchAnimator((IndicatorView view, IndicatorView.IndicatorHolder target) -> {
            int terminalAlpha = 255;
            int centerAlpha = 0;
            ValueAnimator alphaAnim = ObjectAnimator.ofInt(target, "alpha", terminalAlpha, centerAlpha, terminalAlpha);

            int terminalSize = indicator3.getIndicatorPixeSize();
            int centerSize = 0;
            ValueAnimator animatorH = ObjectAnimator.ofInt(target, "height", terminalSize, centerSize, terminalSize);
            ValueAnimator animatorW = ObjectAnimator.ofInt(target, "width", terminalSize, centerSize, terminalSize);

            AnimatorSet set = new AnimatorSet();
            set.play(alphaAnim).with(animatorH).with(animatorW);
            set.setDuration(500);

            return set;
        });

    }

    //自定义指示点切换动画+监听点击事件+不可拖拽
    private void execuInV4() {
        indicator4.setOnDotClickListener((View v, int position) -> Toast.makeText(this, "点击了 " + position, Toast.LENGTH_SHORT).show());
        indicator4.setOnIndicatorSwitchAnimator((IndicatorView view, IndicatorView.IndicatorHolder target) -> {

            int terminalColor = indicator4.getIndicatorColor();
            int centerColor = indicator4.getDotColor();
            ValueAnimator colorAnim = ObjectAnimator.ofArgb(target, "color", terminalColor, centerColor, terminalColor);

            int terminalSize = indicator4.getIndicatorPixeSize();
            int centerSize = indicator4.getIndicatorPixeSize() * 3 / 2;
            ValueAnimator animatorH = ObjectAnimator.ofInt(target, "height", terminalSize, centerSize, terminalSize);
            ValueAnimator animatorW = ObjectAnimator.ofInt(target, "width", terminalSize, centerSize, terminalSize);

            AnimatorSet set = new AnimatorSet();
            set.play(colorAnim).with(animatorH).with(animatorW);
            set.setDuration(500);

            return set;
        });

    }

}
