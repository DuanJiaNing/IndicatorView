package com.duan.indicatorviewdemo;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by DuanJiaNing on 2017/4/3.
 */

public class IndicatorView extends View {

    private Context mContext;

    private int mDotColor;

    private int mDotSize;
    private int mDotCount;

    private boolean mLineVisible;
    private int mLineColor;
    private int mLineHeight;
    private int mLineWidth;

    private int mIndicatorSize;
    private int mIndicatorColor;
    private int mIndicatorPos;
    private int mIndicatorSwitchAnim;

    private boolean mDotClickEnable;
    private boolean mIndicatorDragEnable;
    private boolean mTouchEnable;

    public static final int INDICATOR_SWITCH_ANIM_NONE = 0;
    public static final int INDICATOR_SWITCH_ANIM_TRANSLATION = 1;
    public static final int INDICATOR_SWITCH_ANIM_SQUEEZE = 2;

    private int mDuration;

    private int defaultDotSize = 8;
    private int defaultIndicatorSize = 15;
    private int defaultLineWidth = 40;
    private int minLineHeight = 1;
    private int maxDotCount = 30;
    private int minDotNum = 2;

    private Paint mPaint;

    /**
     * 保存所有小圆点的圆点坐标，用于在touch事件中判断触摸了哪个点
     */
    private int[][] clickableAreas;

    /**
     * 指示点，不断修改它的属性从而实现动画（属性动画）
     */
    private IndicatorHolder indicatorHolder;

    /**
     * 指示点要移动到的目标位置
     */
    private int switchTo = -1;

    /**
     * 手松开后根据该变量判断是否需要启动切换动画
     */
    private boolean haveIndicatorAniming = false;

    /**
     * 指示点是否被拖拽过，当指示点被拖拽了但没有超过当前指示点位置范围时使之回到原位
     */
    private boolean haveIndicatorDraged = false;

    /**
     * 保存转移动画开始时线的颜色
     */
    private int tempLineColor;

    @FunctionalInterface
    public interface OnDotClickListener {
        /**
         * 小圆点点击事件监听（点击的小圆点不是当前指示点所在位置时才会回调）
         *
         * @param v        view
         * @param position 点击的指示点 0 ~ mDotCount
         */
        void onDotClickChange(View v, int position);
    }

    @FunctionalInterface
    public interface OnIndicatorPressAnimator {
        /**
         * 自定义指示点挤压时的属性动画
         *
         * @param view   IndicatorView
         * @param target 属性动画操作的目标对象
         * @return 返回定义好的属性动画，动画的启动由IndicatorView自己控制，用户不应该调用Animator.start()
         */
        AnimatorSet onIndicatorPress(IndicatorView view, IndicatorHolder target);
    }

    @FunctionalInterface
    public interface OnIndicatorSwitchAnimator {
        /**
         * 自定义指示点切换时的属性动画
         *
         * @param view   IndicatorView
         * @param target 属性动画操作的目标对象
         * @return 返回定义好的属性动画，动画的启动由IndicatorView自己控制，用户不应该调用Animator.start()
         */
        AnimatorSet onIndicatorSwitch(IndicatorView view, IndicatorHolder target);
    }

    private OnDotClickListener mListener;
    private OnIndicatorPressAnimator mPressAnimator;
    private OnIndicatorSwitchAnimator mSwitchAnimator;

    public void setOnIndicatorPressAnimator(OnIndicatorPressAnimator pressAnimator) {
        this.mPressAnimator = pressAnimator;
    }

    public void setOnIndicatorSwitchAnimator(OnIndicatorSwitchAnimator switchAnimator) {
        this.mSwitchAnimator = switchAnimator;
    }

    public void setOnDotClickListener(OnDotClickListener listener) {
        this.mListener = listener;
    }

    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;

        final TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IndicatorView, defStyleAttr, 0);

        //默认动画为“挤扁”
        mIndicatorSwitchAnim = array.getInteger(R.styleable.IndicatorView_IndicatorSwitchAnimation, INDICATOR_SWITCH_ANIM_SQUEEZE);

        mTouchEnable = array.getBoolean(R.styleable.IndicatorView_touchEnable, true);
        if (!mTouchEnable) {
            mIndicatorDragEnable = false;
            mDotClickEnable = false;
        } else {
            mIndicatorDragEnable = array.getBoolean(R.styleable.IndicatorView_indicatorDragEnable, true);
            mDotClickEnable = array.getBoolean(R.styleable.IndicatorView_dotClickEnable, true);
        }

        mDotColor = array.getColor(R.styleable.IndicatorView_dotColor, Color.GRAY);
        mLineColor = array.getColor(R.styleable.IndicatorView_lineColor, Color.GRAY);
        mIndicatorColor = array.getColor(R.styleable.IndicatorView_indicatorColor, Color.LTGRAY);

        mDotSize = array.getDimensionPixelSize(R.styleable.IndicatorView_dotSize, defaultDotSize);
        mLineWidth = array.getDimensionPixelSize(R.styleable.IndicatorView_lineWidth, defaultLineWidth);
        mLineHeight = array.getDimensionPixelSize(R.styleable.IndicatorView_lineHeight, minLineHeight);
        mIndicatorSize = array.getDimensionPixelSize(R.styleable.IndicatorView_indicatorSize, defaultIndicatorSize);
        //因为在onDraw中绘制指示点时会通过 indicatorHolder.getWidth() / 2 使两点间切换动画播放过程中椭圆边界不超过 mLineWidth * Math.abs(switchTo - mIndicatorPos) + mIndicatorSize
        //因而这里乘以 2 否则绘制出来的大小会只有实际大小的一半
        mIndicatorSize *= 2;
        //默认动画时间为500ms
        mDuration = array.getInteger(R.styleable.IndicatorView_duration, 500);
        mDotCount = array.getInteger(R.styleable.IndicatorView_dotNum, 3);
        //判断小圆点个数是否超过上限30
        mDotCount = mDotCount > maxDotCount ? maxDotCount : mDotCount < minDotNum ? minDotNum : mDotCount;
        mIndicatorPos = array.getInteger(R.styleable.IndicatorView_indicatorPos, 0);
        mLineVisible = array.getBoolean(R.styleable.IndicatorView_lineVisible, true);

        mPaint = new Paint();
        clickableAreas = new int[mDotCount][2];
        indicatorHolder = new IndicatorHolder();


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width;
        int height;
        setPadding(getPaddingLeft() + mIndicatorSize / 3,getPaddingTop(),getPaddingRight() + mIndicatorSize / 3,getPaddingBottom());
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            //xml中宽度设为warp_content
            width = getPaddingLeft() + ((mDotCount - 1) * mLineWidth + mIndicatorSize) + getPaddingRight();
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = getPaddingTop() + mIndicatorSize + getPaddingBottom();
        }

        //若使用默认的指示点触摸动画（放大+渐变颜色）需要加上放大后指示点与放大前指示点的高度差
        //使用自定义时动画时则不加
        setMeasuredDimension(width, mPressAnimator == null ? height + mIndicatorSize / 2 : height);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //getHeight方法在onDraw方法中会取到错误的值
        if (indicatorHolder != null) {
            indicatorHolder.setColor(mIndicatorColor);
            indicatorHolder.setCenterX(mIndicatorPos * mLineWidth + getPaddingLeft() + mIndicatorSize / 2);
            indicatorHolder.setCenterY(getHeight() / 2);
            indicatorHolder.setHeight(mIndicatorSize);
            indicatorHolder.setWidth(mIndicatorSize);
            indicatorHolder.setAlpha(255);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //去锯齿
        mPaint.setAntiAlias(true);

        //画线（如果可见）
        if (mLineVisible) {
            mPaint.setColor(mLineColor);
            for (int i = 0; i < mDotCount - 1; i++) {
                int left = getPaddingLeft() + mIndicatorSize / 2 + mLineWidth * i;
                int top = (getHeight() - mLineHeight) / 2;
                int right = getPaddingLeft() + mIndicatorSize / 2 + mLineWidth * (i + 1);
                int bottom = (getHeight() + mLineHeight) / 2;
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }

        //画小圆点
        for (int i = 0; i < clickableAreas.length; i++) {
            int cx = i * mLineWidth + getPaddingLeft() + mIndicatorSize / 2;
            int cy = getHeight() / 2;
            if (switchTo != -1 && i == switchTo)
                mPaint.setColor(mIndicatorColor);
            else
                mPaint.setColor(mDotColor);
            canvas.drawCircle(cx, cy, mDotSize, mPaint);
            clickableAreas[i][0] = cx;
            clickableAreas[i][1] = cy;
        }

        //画指示点
        mPaint.setColor(indicatorHolder.getColor());
        mPaint.setAlpha(indicatorHolder.getAlpha());
        canvas.drawOval(
                indicatorHolder.getCenterX() - indicatorHolder.getWidth() / 2,
                indicatorHolder.getCenterY() - indicatorHolder.getHeight() / 2,
                indicatorHolder.getCenterX() + indicatorHolder.getWidth() / 2,
                indicatorHolder.getCenterY() + indicatorHolder.getHeight() / 2,
                mPaint
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!mTouchEnable)
            return true;

        //动画正在进行时不在响应点击事件
        if (haveIndicatorAniming)
            return true;

        int ex = (int) event.getX();
        int temp = mLineWidth / 2;
        switchTo = 0;
        //判断当前手指所在的小圆点是哪个
        for (; switchTo < mDotCount; switchTo++) {
            int[] xy = clickableAreas[switchTo];
            //只对x坐标位置进行判断，这样即使用户手指在控件外面（先在控件内触摸后不抬起而是滑到控件外面）滑动也能判断
            if (ex <= xy[0] + temp && ex >= xy[0] - temp) {
                break;
            }
        }

        if (switchTo != mIndicatorPos && !mDotClickEnable && !haveIndicatorDraged)
            return true;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            //按下且不是指示点所在的小圆点
            if (mIndicatorPos != switchTo) {
                startSwitchAnimation();
                if (mListener != null)
                    mListener.onDotClickChange(this, switchTo);
            } else {//按下且是指示点所在的小圆点
                if (mIndicatorDragEnable)
                    startPressAnimation();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) { //手抬起
            if (switchTo != mIndicatorPos || haveIndicatorDraged) {
                haveIndicatorDraged = false;
                if (mIndicatorDragEnable)
                    startSwitchAnimation();
            }
        } else { //按着+拖拽
            if (mIndicatorDragEnable) {
                haveIndicatorDraged = true;
                indicatorHolder.setCenterX(ex);
            }
        }

        return true;
    }

    /**
     * 指示点触摸（挤压）动画
     */
    private void startPressAnimation() {
        if (mPressAnimator == null) {
            //缩放
            int terminal = mIndicatorSize;
            int center = mIndicatorSize * 3 / 2;
            ValueAnimator scaleAnimH = ObjectAnimator.ofInt(indicatorHolder, "height", terminal, center, terminal);
            ValueAnimator scaleAnimW = ObjectAnimator.ofInt(indicatorHolder, "width", terminal, center, terminal);

            //颜色渐变
            int terminalColor = mIndicatorColor;
            int centerColor = mDotColor;
            ValueAnimator colorAnim = ObjectAnimator.ofArgb(indicatorHolder, "color", terminalColor, centerColor, terminalColor);

            AnimatorSet defaultIndicatorPressAnim = new AnimatorSet();
            defaultIndicatorPressAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    haveIndicatorAniming = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    haveIndicatorAniming = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    haveIndicatorAniming = false;

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            defaultIndicatorPressAnim.setDuration(500);
            defaultIndicatorPressAnim.play(scaleAnimH).with(scaleAnimW).with(colorAnim);
            defaultIndicatorPressAnim.start();
        } else { //自定义动画
            AnimatorSet customfAnim = mPressAnimator.onIndicatorPress(this, indicatorHolder);
            customfAnim.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                    haveIndicatorAniming = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    haveIndicatorAniming = false;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    haveIndicatorAniming = false;

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            //进行挤压动画时控件不再响应触摸事件，因而动画时间不能太长
            customfAnim.setDuration(customfAnim.getDuration() > 700 ? 700 : customfAnim.getDuration());
            customfAnim.start();
        }

    }

    /**
     * 指示点切换动画
     */
    private void startSwitchAnimation() {

        //平移
        int startX = indicatorHolder.getCenterX();
        int endX = switchTo * mLineWidth + getPaddingLeft() + mIndicatorSize / 2;
        ValueAnimator trainsAnim = ObjectAnimator.ofInt(indicatorHolder, "centerX", startX, endX);
        trainsAnim.setDuration(mDuration);

        tempLineColor = mLineColor;
        AnimatorSet defaultIndicatorSwitchAnim = new AnimatorSet();
        defaultIndicatorSwitchAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mLineColor = indicatorHolder.getColor();
                haveIndicatorAniming = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animEnd();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        if (mSwitchAnimator == null) {
            switch (mIndicatorSwitchAnim) {
                case INDICATOR_SWITCH_ANIM_NONE:
                    indicatorHolder.setCenterX(endX);
                    animEnd();
                    break;
                case INDICATOR_SWITCH_ANIM_SQUEEZE:
                    //“挤扁”
                    int centerH = mLineHeight * Math.abs(switchTo - mIndicatorPos);
                    int centerW = Math.abs(indicatorHolder.getCenterX() - clickableAreas[switchTo][0]);
                    ValueAnimator heightAnim = ObjectAnimator.ofInt(indicatorHolder, "height", mIndicatorSize, centerH, 0);
                    ValueAnimator widthAnim = ObjectAnimator.ofInt(indicatorHolder, "width", mIndicatorSize, centerW, 0);
                    heightAnim.setDuration(mDuration);
                    widthAnim.setDuration(mDuration);

                    //缩放
                    ValueAnimator scaleAnimH = ObjectAnimator.ofInt(indicatorHolder, "height", mDotSize, mIndicatorSize);
                    ValueAnimator scaleAnimW = ObjectAnimator.ofInt(indicatorHolder, "width", mDotSize, mIndicatorSize);
                    AnimatorSet scaleSet = new AnimatorSet();
                    scaleSet.play(scaleAnimH).with(scaleAnimW);
                    scaleSet.setDuration(500);

                    defaultIndicatorSwitchAnim.play(trainsAnim).with(heightAnim).with(widthAnim);
                    defaultIndicatorSwitchAnim.play(scaleSet).after(trainsAnim);
                    defaultIndicatorSwitchAnim.start();
                    break;
                case INDICATOR_SWITCH_ANIM_TRANSLATION:
                    defaultIndicatorSwitchAnim.play(trainsAnim);
                    defaultIndicatorSwitchAnim.start();
                    break;
            }

        } else { //自定义
            tempLineColor = mLineColor;
            AnimatorSet customAnim = mSwitchAnimator.onIndicatorSwitch(this, indicatorHolder);
            customAnim.play(trainsAnim);
            customAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    mLineColor = indicatorHolder.getColor();
                    haveIndicatorAniming = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    animEnd();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    animEnd();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            customAnim.start();
        }

    }

    /**
     * 指示点切换动画结束或取消时重置和恢复一些变量的值
     */
    private void animEnd() {
        mLineColor = tempLineColor;
        mIndicatorPos = switchTo;
        switchTo = -1;
        haveIndicatorAniming = false;
    }

    /**
     * 属性动画的目标对象类-指示点，属性动画通过不断调用该类的setXXX方法改变指示点的属性值并重绘控件以实现动画
     */
    public class IndicatorHolder {
        private int centerX;
        private int centerY;
        private int height;
        private int color;
        private int width;
        private int alpha;

        public void setAlpha(int alpha) {
            this.alpha = alpha;
            invalidate();
        }

        public int getAlpha() {

            return alpha;
        }

        public void setHeight(int height) {
            this.height = height;
            invalidate();
        }

        public void setWidth(int width) {
            this.width = width;
            invalidate();
        }

        public void setCenterY(int centerY) {
            this.centerY = centerY;
            invalidate();
        }

        public void setColor(int color) {
            this.color = color;
            invalidate();
        }

        public void setCenterX(int centerX) {
            this.centerX = centerX;
            invalidate();
        }

        public int getColor() {
            return color;
        }

        public int getHeight() {
            return height;
        }

        public int getWidth() {
            return width;
        }

        public int getCenterX() {
            return centerX;
        }

        public int getCenterY() {
            return centerY;
        }

    }

    public void setIndicatorPos(int indicatorPos) {
        if (indicatorPos != mIndicatorPos) {
            switchTo = indicatorPos;
            startSwitchAnimation();
        }
    }

    public void setDotColor(int dotColor) {
        this.mDotColor = dotColor;
        invalidate();
    }

    public void setLineColor(int lineColor) {
        this.mLineColor = lineColor;
        tempLineColor = mLineColor;
        invalidate();
    }

    public void setLineVisible(boolean lineVisible) {
        this.mLineVisible = lineVisible;
        invalidate();
    }

    public void setLineHeight(int lineHeight) {
        this.mLineHeight = lineHeight;
        invalidate();
    }

    public void setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
        this.indicatorHolder.setColor(indicatorColor);
        invalidate();
    }

    public void setIndicatorSwitchAnim(int anim) {
        if (anim >= INDICATOR_SWITCH_ANIM_NONE && anim <= INDICATOR_SWITCH_ANIM_SQUEEZE)
            this.mIndicatorSwitchAnim = anim;
    }


    public int getDotColor() {
        return mDotColor;
    }

    public int getLineColor() {
        return mLineColor;
    }

    public boolean isLineVisible() {
        return mLineVisible;
    }

    public int getDotPixelSize() {
        return mDotSize;
    }

    public int getLinePixelWidth() {
        return mLineWidth;
    }

    public int getLinePixelHeight() {
        return mLineHeight;
    }

    public int getIndicatorColor() {
        return mIndicatorColor;
    }

    public int getIndicatorPixeSize() {
        return mIndicatorSize;
    }

    public int getDotCount() {
        return mDotCount;
    }

    public int getIndicatorPos() {
        return mIndicatorPos;
    }

}
