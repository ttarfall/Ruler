package com.ucs.ruler;
/**
 * Created by ttarfall on 2017/6/5.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ttarfall
 * @date 2017-06-05 13:46
 */
public class RulerView extends View {

    /**
     * 刻度尺类型
     */
    private Type mType;
    /**
     * 刻度尺mode
     */
    private Mode mMode;
    /**
     * 刻度间距
     */
    private float mUnitSpacing;
    /**
     * 设置长度单表示
     */
    private float mUnitLegend;
    private int mUnitLegendTextColor;
    private float mUnitLegendTextSize;
    private float mUnitLegendWidth;
    private float mUnitLegendHeight;
    private RulerVisibility mUnitLegendVisibility;
    private LegendGravity mLegendGravity;
    private Rect mLegendRect;
    private float mLegendMargin;
    /**
     * 刻度的宽度和高度
     */
    private float mUnitWidth;
    private float mUnitHeight;
    private int mUnitColor;
    private float mMidUnitWidth;
    private float mMidUnitHeight;
    private int mMidUnitColor;
    private float mMinUnitWidth;
    private float mMinUnitHeight;
    private int mMinUnitColor;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    //基本刻度坐标
    private float[] mMaxPts;
    private float[] mMidPts;
    private float[] mMinPts;

    /**
     * 最少显示刻度数量=>此属性主要是在视图属性配置为wrap_content来测量视图的宽度使用
     */
    private int mMinUnitMum;
    /**
     * 开始刻度值=》刻度开始的最小值
     */
    private int mUnitStartNum;
    /**
     * 结束刻度值
     */
    private int mUnitEndNum;
    /**
     * 刻度尺显示位置
     */
    private Gravity mGravity;
    /**
     * 刻度尺文字尺寸
     */
    private float mTextSize;
    private int mTextColor;
    private RulerVisibility mRulerUnitMidVisibility;
    //基本刻度框
    private Rect[] mMaxUnitRect;
    private Rect[] mMidUnitRect;
    //基本刻度值
    private int[] mMaxUnitText;
    private int[] mMidUnitText;
    /**
     * 刻度尺文字画笔
     */
    private Paint mPainText = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 刻度尺文字高度
     */
    private float mUnitTextHeight;
    /**
     * 刻度尺文字与刻度之间的距离
     */
    private float mUnitTextSpacing;
    /**
     * 标记相关资源
     */
    private int mResource;
    private Drawable mMarkDrawable;
    private BitmapDrawable mMarkBitmapDrawable;
    /**
     * 标记位置Rect
     */
    private Rect mMarkBitmapDrawableRect;
    /**
     * 标记的颜色=》仅仅当标记是path路径时生效
     */
    private int mMarkColor;
    /**
     * 标记的宽度
     */
    private float mMarkWidth;
    /**
     * 标记的高度
     */
    private float mMarkHeight;
    /**
     * 标记及路径
     */
    private Path mMarkPath;
    /**
     * 初始化标记坐标
     */
    private float[] mMarkPst;
    /**
     * 水平方向标记的颜色
     */
    private int mMarkHColor;
    /**
     * 水平方向标记的宽度
     */
    private float mMarkHWidth;
    /**
     * 水平方向标记的高度
     */
    private float mMarkHHeight;
    private float[] mMarkHPst = new float[4];
    /**
     * 水平线颜色
     */
    private int mHorizontalLineColor;
    /**
     * 水平线的高度
     */
    private float mHorizontalLineHeight;
    /**
     * 水平线起始和结束坐标
     */
    private float[] mHorizontalLinePst = new float[4];

    /**
     * 刻度值格式化
     */
    private OnFormatUnitTextListener mOnFormatUnitTextListener = new DefaultFormatUnitTextListener(0);
    private OnFormatUnitTextListener mOnUnitLegendFormatTextListener = new DefaultLegendFormatUnitTextListener(0);

    /**
     * 刻度尺当前偏移量
     */
    private float mUnitCurrentOffset;

    private int mTouchSlop;//有效滑动距离
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    //手指按下位置
    private float startY;
    private float startX;
    private float mUnitStartOffset;
    private float mUnitMinOffset;//最小偏移量
    private float mUnitMaxOffset;//最大偏移量

    private Builder mBuilder = new Builder();
    private VelocityTracker mVelocityTracker;
    private RulerAnimHelper mAnimHelper;

    public RulerView(Context context) {
        this(context, null);
    }

    public RulerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mAnimHelper = new RulerAnimHelper(this);

        initAttrs(context, attrs, defStyleAttr);
        initMarkPst();
        initBuilder();
        setCurrentUnit(mUnitStartNum);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RulerView, defStyleAttr, 0);
        mType = Type.getType(a.getInteger(R.styleable.RulerView_ruler_type, Type.HEX_10.getCode()));
        mMode = Mode.getMode(a.getInteger(R.styleable.RulerView_ruler_mode, Mode.FLOAT.getCode()));
        mUnitSpacing = a.getDimension(R.styleable.RulerView_ruler_unit_spacing, dp2px(12));
        mUnitLegend = a.getFloat(R.styleable.RulerView_ruler_unit_legend, 1);
        mUnitLegendTextColor = a.getColor(R.styleable.RulerView_ruler_unit_legend_text_color, Color.GRAY);
        mUnitLegendTextSize = a.getDimensionPixelSize(R.styleable.RulerView_ruler_unit_legend_text_size, (int) dp2px(12));
        mUnitLegendWidth = a.getDimension(R.styleable.RulerView_ruler_unit_legend_width, dp2px(48));
        mUnitLegendHeight = a.getDimension(R.styleable.RulerView_ruler_unit_legend_height, dp2px(32));
        mUnitLegendVisibility = RulerVisibility.getVisibility(
                a.getInteger(R.styleable.RulerView_ruler_unit_legend_visibility, RulerVisibility.VISIBLE.getCode()));
        mLegendGravity = LegendGravity.getGravity(a.getInteger(R.styleable.RulerView_ruler_unit_legend_gravity, LegendGravity.LEFT.getCode()));
        mLegendMargin = a.getDimension(R.styleable.RulerView_ruler_unit_legend_margin, dp2px(16));
        mUnitWidth = a.getDimension(R.styleable.RulerView_ruler_unit_width, dp2px(1));
        mUnitHeight = a.getDimension(R.styleable.RulerView_ruler_unit_height, dp2px(24));
        mUnitColor = a.getColor(R.styleable.RulerView_ruler_unit_color, Color.DKGRAY);
        mMidUnitWidth = a.getDimension(R.styleable.RulerView_ruler_unit_mid_width, mUnitWidth);
        mMidUnitHeight = a.getDimension(R.styleable.RulerView_ruler_unit_mid_height, dp2px(20));
        mMidUnitColor = a.getColor(R.styleable.RulerView_ruler_unit_mid_color, Color.DKGRAY);
        mMinUnitWidth = a.getDimension(R.styleable.RulerView_ruler_unit_min_width, mMidUnitWidth);
        mMinUnitHeight = a.getDimension(R.styleable.RulerView_ruler_unit_min_height, dp2px(12));
        mMinUnitColor = a.getColor(R.styleable.RulerView_ruler_unit_min_color, Color.DKGRAY);
        mMinUnitMum = a.getInt(R.styleable.RulerView_ruler_unit_min_num, 20);
        mUnitStartNum = a.getInt(R.styleable.RulerView_ruler_unit_start_num, 0);
        mUnitEndNum = a.getInt(R.styleable.RulerView_ruler_unit_end_num, 0);
        mGravity = Gravity.getGravity(a.getInteger(R.styleable.RulerView_ruler_gravity, Gravity.BOTTOM.getCode()));
        mTextSize = a.getDimensionPixelSize(R.styleable.RulerView_ruler_text_size, (int) dp2px(15));
        mTextColor = a.getColor(R.styleable.RulerView_ruler_text_color, Color.DKGRAY);
        mUnitTextHeight = a.getDimension(R.styleable.RulerView_ruler_text_height, dp2px(16));
        mUnitTextSpacing = a.getDimension(R.styleable.RulerView_ruler_unit_text_spacing, dp2px(8));
        mRulerUnitMidVisibility = RulerVisibility.getVisibility(a.getInteger(R.styleable.RulerView_ruler_unit_text_visibility, RulerVisibility.VISIBLE.getCode()));

        Drawable drawable = a.getDrawable(R.styleable.RulerView_ruler_mark);
        setMarkDrawable(drawable);
        mMarkColor = a.getColor(R.styleable.RulerView_ruler_mark_color, Color.RED);
        mMarkWidth = a.getDimension(R.styleable.RulerView_ruler_mark_width, mUnitSpacing * 2 / 3);
        mMarkHeight = a.getDimension(R.styleable.RulerView_ruler_mark_height, mUnitHeight * 3 / 4);
        mMarkHColor = a.getColor(R.styleable.RulerView_ruler_mark_h_color, Color.RED);
        mMarkHHeight = a.getDimension(R.styleable.RulerView_ruler_mark_h_height, dp2px(2));
        mMarkHWidth = a.getDimension(R.styleable.RulerView_ruler_mark_h_width, mUnitSpacing * Type.getHex(mType));
        mHorizontalLineColor = a.getColor(R.styleable.RulerView_ruler_horizontal_line_color, Color.DKGRAY);
        mHorizontalLineHeight = a.getDimension(R.styleable.RulerView_ruler_horizontal_line_height, dp2px(1));
        mUnitCurrentOffset = a.getFloat(R.styleable.RulerView_ruler_unit_offset, 0);
        a.recycle();
    }

    private void initBuilder() {
        mBuilder.setType(mType)
                .setMode(mMode)
                .setUnitSpacing(mUnitSpacing)
                .setUnitLegend(mUnitLegend)
                .setLegendTextColor(mUnitLegendTextColor)
                .setLegendTextSize(mUnitLegendTextSize)
                .setUnitLegendWidth(mUnitLegendWidth)
                .setUnitLegendHeight(mUnitLegendHeight)
                .setLegendVisibility(mUnitLegendVisibility)
                .setLegendGravity(mLegendGravity)
                .setLegendMargin(mLegendMargin)
                .setUnitWidth(mUnitWidth)
                .setUnitHeight(mUnitHeight)
                .setUnitColor(mUnitColor)
                .setMidUnitWidth(mMidUnitWidth)
                .setMidUnitHeight(mMidUnitHeight)
                .setMidUnitColor(mMidUnitColor)
                .setMinUnitWidth(mMinUnitWidth)
                .setMinUnitHeight(mMinUnitHeight)
                .setMinUnitColor(mMinUnitColor)
                .setUnitMaxCount(mUnitEndNum)
                .setGravity(mGravity)
                .setTextColor(mTextColor)
                .setTextSize(mTextSize)
                .setUnitTextHeight(mUnitTextHeight)
                .setUnitTextSpacing(mUnitTextSpacing)
                .setUnitMidVisibility(mRulerUnitMidVisibility)
                .setMarkColor(mMarkColor)
                .setMarkWidth(mMarkWidth)
                .setMarkPst(mMarkPst)
                .setMarkHeight(mMarkHeight)
                .setMarkHColor(mMarkHColor)
                .setMarkHHeight(mMarkHHeight)
                .setMarkHWidth(mMarkHWidth)
                .setmHorizontalLineColor(mHorizontalLineColor)
                .setHorizontalLineHeight(mHorizontalLineHeight);
    }

    /**
     * 初始化mark的坐标
     */
    private void initMarkPst() {
        if (mMarkBitmapDrawable != null) {
            mMarkBitmapDrawableRect = new Rect(0, 0, (int) mMarkWidth, (int) mMarkHeight);
        } else {
            mMarkPst = new float[5 * 2];
            mMarkPst[0] = -mMarkWidth / 2;
            mMarkPst[1] = 0;
            mMarkPst[2] = -mMarkWidth / 2;
            mMarkPst[3] = mMarkHeight * 3 / 4;
            mMarkPst[4] = 0;
            mMarkPst[5] = mMarkHeight;
            mMarkPst[6] = mMarkWidth / 2;
            mMarkPst[7] = mMarkHeight * 3 / 4;
            mMarkPst[8] = mMarkWidth / 2;
            mMarkPst[9] = 0;
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = 0;
        int height = 0;
        switch (mGravity) {
            case LEFT:
            case RIGHT:
                if (widthMode == MeasureSpec.AT_MOST) {
                    int rulerWidth = measureRulerHeight() + getPaddingLeft() + getPaddingRight();
                    width = Math.min(rulerWidth, widthSize);
                } else {
                    width = widthSize;
                }
                if (heightMode == MeasureSpec.AT_MOST) {
                    int rulerHeight = measuredRulerWidth() + getPaddingTop() + getPaddingBottom();
                    height = Math.min(rulerHeight, heightSize);
                } else {
                    height = heightSize;
                }
                break;
            case TOP:
            case BOTTOM:
                if (widthMode == MeasureSpec.AT_MOST) {
                    int rulerWidth = measuredRulerWidth() + getPaddingLeft() + getPaddingRight();
                    width = Math.min(rulerWidth, widthSize);
                } else {
                    width = widthSize;
                }
                if (heightMode == MeasureSpec.AT_MOST) {
                    int rulerHeight = measureRulerHeight() + getPaddingTop() + getPaddingBottom();
                    height = Math.min(rulerHeight, heightSize);
                } else {
                    height = heightSize;
                }
                break;
        }
        calUnitPts(width, height);
        calMark(width, height);
        calLegendPst(width, height);
        setMeasuredDimension(width, height);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawUnit(canvas);
        drawUnitText(canvas);
        drawMark(canvas);
        drawLegend(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mAnimHelper.isRunning()) {
                    mAnimHelper.cancel();
                }
                // 记录手指按下的位置
                startX = event.getX();
                startY = event.getY();
                //记录手机按下时的偏移量
                mUnitStartOffset = mUnitCurrentOffset;
                mUnitMinOffset = getMinOffset();
                mUnitMaxOffset = getMaxOffset();
                initOrResetVelocityTracker();
                return true;
            case MotionEvent.ACTION_MOVE:
                // 滑动距离
                float distanceX = event.getX() - startX;
                float distanceY = event.getY() - startY;
                // 如果滑动有效则处理该事件，否则不处理
                if (Math.abs(distanceX) > mTouchSlop || Math.abs(distanceY) > mTouchSlop) {
                    mVelocityTracker.addMovement(event);
                    switch (mGravity) {
                        case LEFT:
                        case RIGHT:
                            if (mUnitStartOffset <= mUnitMinOffset || distanceY <= 0) {
                                if (mUnitStartOffset + distanceY > mUnitMinOffset) {
                                    distanceY = mUnitMinOffset - mUnitStartOffset;
                                }
                                if (mUnitStartOffset > mUnitMaxOffset || distanceY <= 0) {
                                    if (mUnitStartOffset + distanceY < mUnitMaxOffset) {
                                        distanceY = mUnitMaxOffset - mUnitStartOffset;
                                    }
                                }
                                mUnitCurrentOffset = mUnitStartOffset + distanceY;
                                //重新计算刻度尺
                                calUnitPts(getWidth(), getHeight());
                                //刷新视图
                                postInvalidate();
                                performOnCurrentUnitTextListener();
                            }
                            break;
                        case TOP:
                        case BOTTOM:
                            if (mUnitStartOffset <= mUnitMinOffset || distanceX <= 0) {
                                if (mUnitStartOffset + distanceX > mUnitMinOffset) {
                                    distanceX = mUnitMinOffset - mUnitStartOffset;
                                }
                                if (mUnitStartOffset > mUnitMaxOffset || distanceX <= 0) {
                                    if (mUnitStartOffset + distanceX < mUnitMaxOffset) {
                                        distanceX = mUnitMaxOffset - mUnitStartOffset;
                                    }
                                }
                                mUnitCurrentOffset = mUnitStartOffset + distanceX;
                                //重新计算刻度尺
                                calUnitPts(getWidth(), getHeight());
                                //刷新视图
                                postInvalidate();
                                performOnCurrentUnitTextListener();
                            }
                            break;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (mGravity) {
                    case LEFT:
                    case RIGHT:
                        distanceY = event.getY() - startY;
                        if (mUnitStartOffset <= mUnitMinOffset || distanceY <= 0) {
                            if (mUnitStartOffset + distanceY > mUnitMinOffset) {
                                distanceY = mUnitMinOffset - mUnitStartOffset;
                            }
                            if (mUnitStartOffset != mUnitMaxOffset || distanceY <= 0) {
                                if (mUnitStartOffset + distanceY < mUnitMaxOffset) {
                                    distanceY = mUnitMaxOffset - mUnitStartOffset;
                                }
                            }
                            final VelocityTracker velocityTracker = mVelocityTracker;
                            velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                            final int yVelocity = (int) velocityTracker.getYVelocity();
                            if (Math.abs(yVelocity) > mMinimumVelocity) {
                                mUnitStartOffset = mUnitStartOffset + distanceY;
                                fling(distanceY, yVelocity);
                            } else {
                                mUnitCurrentOffset = formatCurrentOffset(mUnitStartOffset + distanceY);
                                //重新计算刻度尺
                                calUnitPts(getWidth(), getHeight());
                                //刷新视图
                                postInvalidate();
                                performOnCurrentUnitTextListener();
                            }
                        }
                        break;
                    case TOP:
                    case BOTTOM:
                        // 滑动距离
                        distanceX = event.getX() - startX;
                        if (mUnitStartOffset <= mUnitMinOffset || distanceX <= 0) {
                            if (mUnitStartOffset + distanceX > mUnitMinOffset) {
                                distanceX = mUnitMinOffset - mUnitStartOffset;
                            }
                            if (mUnitStartOffset != mUnitMaxOffset || distanceX <= 0) {
                                if (mUnitStartOffset + distanceX < mUnitMaxOffset) {
                                    distanceX = mUnitMaxOffset - mUnitStartOffset;
                                }
                            }
                            final VelocityTracker velocityTracker = mVelocityTracker;
                            velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                            final int xVelocity = (int) velocityTracker.getXVelocity();
                            if (Math.abs(xVelocity) > mMinimumVelocity) {
                                mUnitStartOffset = mUnitStartOffset + distanceX;
                                fling(distanceX, xVelocity);
                            } else {
                                mUnitCurrentOffset = formatCurrentOffset(mUnitStartOffset + distanceX);
                                //重新计算刻度尺
                                calUnitPts(getWidth(), getHeight());
                                //刷新视图
                                postInvalidate();
                                performOnCurrentUnitTextListener();
                            }
                        }
                        break;
                }
                recycleVelocityTracker();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }


    private void fling(float distance, float velocity) {
        final float vel = velocity * 0.3f;
        final float off = mUnitCurrentOffset + distance + vel;
        float dis = 0;//最大还可以滚动的距离
        final float minOffset = getMinOffset();
        final float maxOffset = getMaxOffset();
        if (distance > 0) {
            if (off > minOffset) {
                dis = minOffset - mUnitCurrentOffset;
            } else {
                dis = vel;
            }
        } else if (distance < 0) {
            if (off < maxOffset) {
                dis = maxOffset - mUnitCurrentOffset;
            } else {
                dis = vel;
            }
        }
        mAnimHelper.fling(dis, 300);
    }

    /**
     * 动画更新
     *
     * @param distance
     */
    public void onAnimationUpdate(float distance) {
        if (distance != 0.0f) {
            mUnitCurrentOffset = mUnitStartOffset + distance;
            //重新计算刻度尺
            calUnitPts(getWidth(), getHeight());
            //刷新视图
            postInvalidate();
            performOnCurrentUnitTextListener();
        }
    }

    /**
     * 动画更新结束
     *
     * @param distance
     */
    public void onAnimationEnd(float distance) {
        mUnitCurrentOffset = formatCurrentOffset(mUnitStartOffset + distance);
        //重新计算刻度尺
        calUnitPts(getWidth(), getHeight());
        //刷新视图
        postInvalidate();
        performOnCurrentUnitTextListener();
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    /**
     * 格式化当前offset
     *
     * @param currentOffset
     * @return
     */
    private float formatCurrentOffset(float currentOffset) {
        final float maxOffset = getMaxOffset();
        final float minOffset = getMinOffset();
        if (currentOffset < maxOffset) {
            currentOffset = maxOffset;
        } else if (currentOffset > minOffset) {
            currentOffset = minOffset;
        }
        switch (mMode) {
            case INTEGER:
                final float unitWidth = mUnitSpacing + mUnitWidth;
                float unitText = getFormatUnit(-currentOffset / unitWidth);
                return -unitWidth * unitText;
            default:
                return currentOffset;
        }
    }

    /**
     * 获取最大偏移量
     *
     * @return
     */
    private float getMaxOffset() {
        if (mUnitEndNum > 0) {
            return -mUnitEndNum * (mUnitSpacing + mUnitWidth);
        }
        return 0;
    }

    /**
     * 最小偏移量
     *
     * @return
     */
    private float getMinOffset() {
        if (mUnitStartNum > 0) {
            return -mUnitStartNum * (mUnitSpacing + mUnitWidth);
        }
        return 0;
    }


    /**
     * 绘制刻度
     *
     * @param canvas
     */
    private void drawUnit(Canvas canvas) {
        final float offset = mUnitCurrentOffset;//当前偏移量
        final float maxOffset = getMaxOffset();
        final float rulerWidth = getRulerWidth(getWidth(), getHeight());
        if (mMaxPts != null && mMaxPts.length > 0) {
            mPaint.setColor(mUnitColor);
            mPaint.setStrokeWidth(mUnitWidth);
            int count = mMaxPts.length / 4;
            for (int i = 0; i < count; i++) {
                float left = getLeft(mMaxPts[i * 4], mMaxPts[i * 4 + 1]);
                if (offset - left + rulerWidth / 2 < maxOffset) {
                    continue;
                }
                canvas.drawLine(mMaxPts[i * 4], mMaxPts[i * 4 + 1], mMaxPts[i * 4 + 2], mMaxPts[i * 4 + 3], mPaint);
            }
        }
        if (mMidPts != null && mMidPts.length > 0) {
            mPaint.setColor(mMidUnitColor);
            mPaint.setStrokeWidth(mMidUnitWidth);
            int count = mMidPts.length / 4;
            for (int i = 0; i < count; i++) {
                float left = getLeft(mMidPts[i * 4], mMidPts[i * 4 + 1]);
                if (offset - left + rulerWidth / 2 < maxOffset) {
                    continue;
                }
                canvas.drawLine(mMidPts[i * 4], mMidPts[i * 4 + 1], mMidPts[i * 4 + 2], mMidPts[i * 4 + 3], mPaint);
            }
        }
        if (mMinPts != null && mMinPts.length > 0) {
            mPaint.setColor(mMinUnitColor);
            mPaint.setStrokeWidth(mMinUnitWidth);
            int count = mMinPts.length / 4;
            for (int i = 0; i < count; i++) {
                float left = getLeft(mMinPts[i * 4], mMinPts[i * 4 + 1]);
                if (offset - left + rulerWidth / 2 < maxOffset) {
                    continue;
                }
                canvas.drawLine(mMinPts[i * 4], mMinPts[i * 4 + 1], mMinPts[i * 4 + 2], mMinPts[i * 4 + 3], mPaint);
            }
        }
    }

    /**
     * 获取坐标距离y轴的距离
     *
     * @param startX
     * @param startY
     * @return
     */
    private float getLeft(float startX, float startY) {
        switch (mGravity) {
            case LEFT:
            case RIGHT:
                return startY;
            case TOP:
            case BOTTOM:
                return startX;
        }
        return 0;
    }

    /**
     * 绘制标记
     *
     * @param canvas
     */
    private void drawMark(Canvas canvas) {
        if (mHorizontalLinePst != null) {
            mPaint.setColor(mHorizontalLineColor);
            mPaint.setStrokeWidth(mHorizontalLineHeight);
            canvas.drawLines(mHorizontalLinePst, mPaint);
        }
        if (mMarkPath != null) {
            mPaint.setColor(mMarkColor);
            canvas.drawPath(mMarkPath, mPaint);
        }
        if (mMarkHPst != null) {
            mPaint.setColor(mMarkHColor);
            mPaint.setStrokeWidth(mMarkHHeight);
            canvas.drawLines(mMarkHPst, mPaint);
        }
        if (mMarkBitmapDrawable != null && mMarkBitmapDrawableRect != null) {
            mMarkBitmapDrawable.setBounds(mMarkBitmapDrawableRect);
            mMarkBitmapDrawable.draw(canvas);
        }
    }

    private void drawUnitText(Canvas canvas) {
        if (mOnFormatUnitTextListener != null) {
            mPainText.setColor(mTextColor);
            mPainText.setTextAlign(Paint.Align.CENTER);
            mPainText.setTextSize(mTextSize);
            Paint.FontMetrics fontMetrics = mPainText.getFontMetrics();
            final float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
            final float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom
            if (mMaxUnitText != null && mMaxUnitRect != null && mMaxUnitText.length == mMaxUnitRect.length) {
                for (int i = 0; i < mMaxUnitText.length; i++) {
                    if (mMaxUnitText[i] > mUnitEndNum) {
                        continue;
                    }
                    int baseLineY = (int) (mMaxUnitRect[i].centerY() - top / 2 - bottom / 2);//基线中间点的y轴计算公式
                    String text = mOnFormatUnitTextListener.onFormatText(mMaxUnitText[i] * mUnitLegend, mMaxUnitText[i], mUnitLegend);
                    if (!TextUtils.isEmpty(text))
                        canvas.drawText(text, mMaxUnitRect[i].centerX(), baseLineY, mPainText);
                }
            }
            if (mRulerUnitMidVisibility == RulerVisibility.VISIBLE) {
                if (mMidUnitText != null && mMidUnitRect != null && mMidUnitText.length == mMidUnitRect.length) {
                    for (int i = 0; i < mMidUnitText.length; i++) {
                        if (mMidUnitText[i] > mUnitEndNum) {
                            continue;
                        }
                        int baseLineY = (int) (mMidUnitRect[i].centerY() - top / 2 - bottom / 2);//基线中间点的y轴计算公式
                        String text = mOnFormatUnitTextListener.onFormatText(mMidUnitText[i] * mUnitLegend, mMidUnitText[i], mUnitLegend);
                        canvas.drawText(text, mMidUnitRect[i].centerX(), baseLineY, mPainText);
                    }
                }
            }
        }
    }

    private void drawLegend(Canvas canvas) {
        if (RulerVisibility.VISIBLE == mUnitLegendVisibility) {
            if (mLegendRect != null) {
                mPainText.setColor(mUnitLegendTextColor);
                mPainText.setTextAlign(Paint.Align.CENTER);
                mPainText.setTextSize(mUnitLegendTextSize);
                Paint.FontMetrics fontMetrics = mPainText.getFontMetrics();
                final float top = fontMetrics.top;//为基线到字体上边框的距离,即上图中的top
                final float bottom = fontMetrics.bottom;//为基线到字体下边框的距离,即上图中的bottom

                //绘制标
                float[] legendPst = new float[3 * 4];
                final float h = mHorizontalLineHeight * 2;
                legendPst[0] = mLegendRect.centerX() - mUnitSpacing / 2;
                legendPst[1] = mLegendRect.centerY() - top / 2 + bottom / 2 - h + mLegendMargin;
                legendPst[2] = mLegendRect.centerX() - mUnitSpacing / 2;
                legendPst[3] = mLegendRect.centerY() - top / 2 + bottom / 2 + mLegendMargin;
                legendPst[4] = mLegendRect.centerX() - mUnitSpacing / 2;
                legendPst[5] = mLegendRect.centerY() - top / 2 + bottom / 2 - mHorizontalLineHeight / 2 + mLegendMargin;
                legendPst[6] = mLegendRect.centerX() + mUnitSpacing / 2;
                legendPst[7] = mLegendRect.centerY() - top / 2 + bottom / 2 - mHorizontalLineHeight / 2 + mLegendMargin;
                legendPst[8] = mLegendRect.centerX() + mUnitSpacing / 2;
                legendPst[9] = mLegendRect.centerY() - top / 2 + bottom / 2 - h + mLegendMargin;
                legendPst[10] = mLegendRect.centerX() + mUnitSpacing / 2;
                legendPst[11] = mLegendRect.centerY() - top / 2 + bottom / 2 + mLegendMargin;

                //绘制基本标注
                mPaint.setColor(mHorizontalLineColor);
                mPaint.setStrokeWidth(mHorizontalLineHeight);
                canvas.drawLines(legendPst, mPaint);

                //绘制文字
                if (mOnUnitLegendFormatTextListener != null) {
                    int baseLineY = (int) (mLegendRect.centerY() - top / 2 - bottom / 2);//基线中间点的y轴计算公式
                    String text = mOnUnitLegendFormatTextListener.onFormatText(mUnitLegend * 1, 1, mUnitLegend);
                    if (!TextUtils.isEmpty(text))
                        canvas.drawText(text, mLegendRect.centerX(), baseLineY, mPainText);
                }
            }
        }
    }


    /**
     * 测量刻度尺的宽度
     *
     * @return
     */
    private int measuredRulerWidth() {
        int unitNum = mMinUnitMum + 1;
        if (unitNum > 0) {
            return (int) (unitNum * mUnitWidth
                    + mMinUnitMum * mUnitSpacing + 0.5f);
        }
        return 0;
    }

    /**
     * 测量刻度尺的高度
     *
     * @return
     */
    private int measureRulerHeight() {
        float legendHeight = mUnitLegendHeight;
        if (mUnitLegendVisibility == RulerVisibility.GONE) {
            legendHeight = 0;
        }
        return (int) (legendHeight + mUnitTextHeight + mUnitTextSpacing * 2 + mUnitHeight + 0.5f);
    }

    /**
     * 计算要绘制的刻度线
     *
     * @param width  视图的宽度
     * @param height 视图的高度
     */
    private void calUnitPts(int width, int height) {
        final float sw = mUnitSpacing + mUnitWidth;
        final int rulerWidth = getRulerWidth(width, height);
        if (sw > 0.0f && rulerWidth > 0) {
            int count = (int) (rulerWidth / sw);
            //如果余数不为0，多增加一个刻度
            if (rulerWidth % sw > 0.0f) {
                count++;
            }
            final int hex = Type.getHex(mType);
            //根据最大显示的刻度数量增加多增加一个进度的刻度绘制,方便滚动绘制
            count = (count - count % hex + hex) * 3;
            if (hex > 0 && hex % 2 == 0) {
                int max = count / hex;
                int mid = (count + hex / 2 - 1) / hex;
                int min = count - max - mid;
                mMaxPts = new float[max * 4];
                mMidPts = new float[mid * 4];
                mMinPts = new float[min * 4];
                max = 0;
                mid = 0;
                min = 0;
                for (int i = 0; i < count; i++) {
                    int p = i % hex;
                    if (p == 0) {
                        mMaxPts[max * 4] = sw * i;
                        mMaxPts[max * 4 + 1] = 0;
                        mMaxPts[max * 4 + 2] = sw * i;
                        mMaxPts[max * 4 + 3] = mUnitHeight;
                        max++;
                    } else if (p == hex / 2) {
                        mMidPts[mid * 4] = sw * i;
                        mMidPts[mid * 4 + 1] = 0;
                        mMidPts[mid * 4 + 2] = sw * i;
                        mMidPts[mid * 4 + 3] = mMidUnitHeight;
                        mid++;
                    } else {
                        mMinPts[min * 4] = sw * i;
                        mMinPts[min * 4 + 1] = 0;
                        mMinPts[min * 4 + 2] = sw * i;
                        mMinPts[min * 4 + 3] = mMinUnitHeight;
                        min++;
                    }
                }
                final int rulerHeight = getRulerHeight(width, height);
                mMaxPts = changePst(mMaxPts, rulerWidth, rulerHeight, count, mUnitCurrentOffset);
                mMidPts = changePst(mMidPts, rulerWidth, rulerHeight, count, mUnitCurrentOffset);
                mMinPts = changePst(mMinPts, rulerWidth, rulerHeight, count, mUnitCurrentOffset);

                //创建基本刻度值
                mMaxUnitText = new int[max];
                mMaxUnitRect = new Rect[max];
                boolean bol = isUnitMidVisibility();
                final float swh = sw * hex;
                for (int i = 0; i < max; i++) {
                    //创建基本刻度文字
                    mMaxUnitText[i] = i * hex;
                    //创建基本刻度矩形绘制区域
                    if (bol) {
                        mMaxUnitRect[i] = new Rect((int) (swh * i - swh / 4),
                                (int) (mUnitHeight + mUnitTextSpacing),
                                (int) (swh * i + swh / 4),
                                (int) (mUnitHeight + mUnitTextSpacing + mUnitTextHeight));
                    } else {
                        mMaxUnitRect[i] = new Rect((int) (swh * i - swh / 2),
                                (int) (mUnitHeight + mUnitTextSpacing),
                                (int) (swh * i + swh / 2),
                                (int) (mUnitHeight + mUnitTextSpacing + mUnitTextHeight));
                    }
                }
                if (bol) {
                    //创建基本刻度文字
                    mMidUnitText = new int[mid];
                    //创建基本刻度矩形绘制区域
                    mMidUnitRect = new Rect[mid];
                    for (int i = 0; i < mid; i++) {
                        mMidUnitText[i] = i * hex + hex / 2;
                        mMidUnitRect[i] = new Rect((int) (swh * i + sw * hex / 2 - swh / 4),
                                (int) (mUnitHeight + mUnitTextSpacing),
                                (int) (swh * i + sw * hex / 2 + swh / 4),
                                (int) (mUnitHeight + mUnitTextSpacing + mUnitTextHeight));
                    }
                }
                //变换Rect坐标,变换UnitText
                mMaxUnitRect = changeUnitRect(mMaxUnitRect, mMaxUnitText, rulerWidth, rulerHeight, count, (int) mUnitCurrentOffset);
                mMidUnitRect = changeUnitRect(mMidUnitRect, mMidUnitText, rulerWidth, rulerHeight, count, (int) mUnitCurrentOffset);


            }
        }
    }

    /**
     * 中级刻度是否可见
     *
     * @return
     */
    private boolean isUnitMidVisibility() {
        return RulerVisibility.GONE != mRulerUnitMidVisibility;
    }


    /**
     * 改变坐标
     *
     * @param pst         原始数据坐标，默认是顶部对齐
     * @param rulerHeight 刻度尺的高度
     * @return
     */
    private float[] changePst(float[] pst, float rulerWidth, float rulerHeight, int maxUnit, float offset) {
        if (pst != null && pst.length % 4 == 0) {
            //根据位置变化坐标
            int count = pst.length / 4;
            //最大偏移量
            final float pageOffset = maxUnit * (mUnitSpacing + mUnitWidth);
            if (pageOffset > 0) {
                for (int i = count; i > 0; i--) {
                    switch (mGravity) {
                        case LEFT:
                            float p;
                            p = pst[(i - 1) * 4];
                            pst[(i - 1) * 4] = pst[(i - 1) * 4 + 1];
                            pst[(i - 1) * 4 + 1] = calRealPst(p, rulerWidth, pageOffset, offset);
                            p = pst[(i - 1) * 4 + 2];
                            pst[(i - 1) * 4 + 2] = pst[(i - 1) * 4 + 3];
                            pst[(i - 1) * 4 + 3] = calRealPst(p, rulerWidth, pageOffset, offset);
                            break;
                        case RIGHT:
                            p = pst[(i - 1) * 4];
                            pst[(i - 1) * 4] = -pst[(i - 1) * 4 + 1] + rulerHeight;
                            pst[(i - 1) * 4 + 1] = calRealPst(p, rulerWidth, pageOffset, offset);
                            p = pst[(i - 1) * 4 + 2];
                            pst[(i - 1) * 4 + 2] = -pst[(i - 1) * 4 + 3] + rulerHeight;
                            pst[(i - 1) * 4 + 3] = calRealPst(p, rulerWidth, pageOffset, offset);
                            break;
                        case BOTTOM:
                            pst[(i - 1) * 4] = calRealPst(pst[(i - 1) * 4], rulerWidth, pageOffset, offset);
                            pst[(i - 1) * 4 + 1] = -pst[(i - 1) * 4 + 1] + rulerHeight;
                            pst[(i - 1) * 4 + 2] = calRealPst(pst[(i - 1) * 4 + 2], rulerWidth, pageOffset, offset);
                            pst[(i - 1) * 4 + 3] = -pst[(i - 1) * 4 + 3] + rulerHeight;
                            break;
                        case TOP:
                            pst[(i - 1) * 4] = calRealPst(pst[(i - 1) * 4], rulerWidth, pageOffset, offset);
                            pst[(i - 1) * 4 + 2] = calRealPst(pst[(i - 1) * 4 + 2], rulerWidth, pageOffset, offset);
                            break;
                    }
                }
            }
        }
        return pst;
    }


    /**
     * 计算点真实的偏移量
     *
     * @param p
     * @param rulerWidth
     * @param pageOffset
     * @param offset
     * @return
     */
    private float calRealPst(float p, float rulerWidth, float pageOffset, float offset) {
        //求余偏移量
        float off = (offset + rulerWidth / 2) % pageOffset;
        //实际偏移量
        p = p + off;
        if (p < 0) {
            p = p + pageOffset;
        }
        return p;
    }

    /**
     * 改变矩形区域坐标
     *
     * @param rect
     * @return
     */
    private Rect[] changeUnitRect(Rect[] rect, int[] unitText, int rulerWidth, int rulerHeight, int maxUnit, int offset) {
        if (rect != null) {
            //最大偏移量
            final int pageOffset = (int) (maxUnit * (mUnitSpacing + mUnitWidth));
            if (pageOffset > 0) {
                int p;
                int textUnitWidth;
                for (int i = 0; i < rect.length; i++) {
                    textUnitWidth = rect[i].width();
                    unitText[i] = calRelUnitText(rect[i].left, unitText[i], rulerWidth, textUnitWidth, maxUnit, pageOffset, offset);
                    switch (mGravity) {
                        case LEFT:
                            p = rect[i].left;
                            rect[i].left = rect[i].top;
                            rect[i].top = calRelRect(p, rulerWidth, textUnitWidth, pageOffset, offset);
                            p = rect[i].right;
                            rect[i].right = rect[i].bottom;
                            rect[i].bottom = calRelRect(p, rulerWidth, textUnitWidth, pageOffset, offset);
                            break;
                        case RIGHT:
                            p = rect[i].left;
                            rect[i].left = rect[i].top;
                            rect[i].top = calRelRect(p, rulerWidth, textUnitWidth, pageOffset, offset);
                            p = rect[i].right;
                            rect[i].right = rect[i].bottom;
                            rect[i].bottom = calRelRect(p, rulerWidth, textUnitWidth, pageOffset, offset);
                            p = rect[i].left;
                            rect[i].left = rulerHeight - rect[i].right;
                            rect[i].right = rulerHeight - p;

                            break;
                        case TOP:
                            rect[i].left = calRelRect(rect[i].left, rulerWidth, textUnitWidth, pageOffset, offset);
                            rect[i].right = calRelRect(rect[i].right, rulerWidth, textUnitWidth, pageOffset, offset);
                            break;
                        case BOTTOM:
                            rect[i].left = calRelRect(rect[i].left, rulerWidth, textUnitWidth, pageOffset, offset);
                            p = rect[i].top;
                            rect[i].top = rulerHeight - rect[i].bottom;
                            rect[i].right = calRelRect(rect[i].right, rulerWidth, textUnitWidth, pageOffset, offset);
                            rect[i].bottom = rulerHeight - p;
                            break;
                    }
                }
            }
        }
        return rect;
    }

    /**
     * 计算真实的偏移位置
     *
     * @param p
     * @param rulerWidth
     * @param pageOffset
     * @param offset
     * @return
     */
    private int calRelRect(int p, int rulerWidth, int textUnitWidth, int pageOffset, int offset) {
        //求余偏移量
        int off = (offset + rulerWidth / 2 + textUnitWidth) % pageOffset;
        //实际偏移量
        p = p + off;
        if (p < 0) {
            p = p + pageOffset;
        }
        return p - textUnitWidth;
    }

    /**
     * 计算真实的刻度值
     *
     * @param text
     * @param rulerWidth
     * @param textUnitWidth
     * @param maxUnit
     * @param offset
     * @return
     */
    private int calRelUnitText(int p, int text, int rulerWidth, int textUnitWidth, int maxUnit, int maxOffset, int offset) {
        int off = p + offset + rulerWidth / 2 + textUnitWidth;
        if (off < 0) {
            int page = 1 - off / maxOffset;
            text = text + maxUnit * page;
        }
        return text;
    }

    /**
     * 初始化Mark
     *
     * @param width
     * @param height
     */
    private void calMark(int width, int height) {
        final int rulerWidth = getRulerWidth(width, height);
        final int rulerHeight = getRulerHeight(width, height);
        final float lineHeight2 = mHorizontalLineHeight / 2;
        final float markHWidth2 = mMarkHWidth / 2;
        final float markHHeight2 = mMarkHHeight / 2;
        switch (mGravity) {
            case LEFT:
                //水平直线位置计算
                mHorizontalLinePst[0] = lineHeight2;
                mHorizontalLinePst[1] = 0;
                mHorizontalLinePst[2] = lineHeight2;
                mHorizontalLinePst[3] = rulerWidth;
                //水平标记线
                mMarkHPst[0] = markHHeight2;
                mMarkHPst[1] = rulerWidth / 2 - markHWidth2;
                mMarkHPst[2] = markHHeight2;
                mMarkHPst[3] = rulerWidth / 2 + markHWidth2;
                break;
            case RIGHT:
                //水平直线位置计算
                mHorizontalLinePst[0] = rulerHeight - lineHeight2;
                mHorizontalLinePst[1] = 0;
                mHorizontalLinePst[2] = rulerHeight - lineHeight2;
                mHorizontalLinePst[3] = rulerWidth;
                //水平标记线
                mMarkHPst[0] = rulerHeight - markHHeight2;
                mMarkHPst[1] = rulerWidth / 2 - markHWidth2;
                mMarkHPst[2] = rulerHeight - markHHeight2;
                mMarkHPst[3] = rulerWidth / 2 + markHWidth2;

                break;
            case TOP:
                //水平直线位置计算
                mHorizontalLinePst[0] = 0;
                mHorizontalLinePst[1] = lineHeight2;
                mHorizontalLinePst[2] = rulerWidth;
                mHorizontalLinePst[3] = lineHeight2;
                //水平标记线
                mMarkHPst[0] = rulerWidth / 2 - markHWidth2;
                mMarkHPst[1] = markHHeight2;
                mMarkHPst[2] = rulerWidth / 2 + markHWidth2;
                mMarkHPst[3] = markHHeight2;
                break;
            case BOTTOM:
                //水平直线位置计算
                mHorizontalLinePst[0] = 0;
                mHorizontalLinePst[1] = rulerHeight - lineHeight2;
                mHorizontalLinePst[2] = rulerWidth;
                mHorizontalLinePst[3] = rulerHeight - lineHeight2;
                //水平标记线
                mMarkHPst[0] = rulerWidth / 2 - markHWidth2;
                mMarkHPst[1] = rulerHeight - markHHeight2;
                mMarkHPst[2] = rulerWidth / 2 + markHWidth2;
                mMarkHPst[3] = rulerHeight - markHHeight2;
                break;
        }

        //标记位置计算
        if (mMarkBitmapDrawable != null) {
            if (mMarkBitmapDrawableRect != null) {
                switch (mGravity) {
                    case LEFT:
                        mMarkBitmapDrawableRect.left = 0;
                        mMarkBitmapDrawableRect.top = (int) (rulerWidth - mMarkHeight) / 2;
                        mMarkBitmapDrawableRect.right = (int) mMarkWidth;
                        mMarkBitmapDrawableRect.bottom = (int) (rulerWidth + mMarkHeight) / 2;
                        break;
                    case RIGHT:
                        mMarkBitmapDrawableRect.left = rulerHeight - (int) mMarkWidth;
                        mMarkBitmapDrawableRect.top = (int) (rulerWidth - mMarkHeight) / 2;
                        mMarkBitmapDrawableRect.right = rulerHeight;
                        mMarkBitmapDrawableRect.bottom = (int) (rulerWidth + mMarkHeight) / 2;
                        break;
                    case TOP:
                        mMarkBitmapDrawableRect.left = (int) (rulerWidth - mMarkWidth) / 2;
                        mMarkBitmapDrawableRect.top = 0;
                        mMarkBitmapDrawableRect.right = (int) (rulerWidth + mMarkWidth) / 2;
                        mMarkBitmapDrawableRect.bottom = (int) mMarkHeight;
                        break;
                    case BOTTOM:
                        mMarkBitmapDrawableRect.left = (int) (rulerWidth - mMarkWidth) / 2;
                        mMarkBitmapDrawableRect.top = rulerHeight - (int) mMarkHeight;
                        mMarkBitmapDrawableRect.right = (int) (rulerWidth + mMarkWidth) / 2;
                        mMarkBitmapDrawableRect.bottom = rulerHeight;
                        break;
                }
            }
        } else {
            if (mMarkPst != null) {
                float[] pst = changeMarkPst(mMarkPst, rulerWidth, rulerHeight);
                int count = pst.length / 2;
                mMarkPath = new Path();
                for (int i = 0; i < count; i++) {
                    if (i == 0) {
                        mMarkPath.moveTo(pst[i * 2], pst[i * 2 + 1]);
                    }
                    mMarkPath.lineTo(pst[i * 2], pst[i * 2 + 1]);
                }
                mMarkPath.close();
            }
        }

        //水平标记位置计算
    }

    /**
     * 计算 Legend 的坐标
     *
     * @param width
     * @param height
     */
    private void calLegendPst(int width, int height) {
        final int rulerWidth = getRulerWidth(width, height);
        final int rulerHeight = getRulerHeight(width, height);
        final int legendWidth = (int) mUnitLegendWidth;
        final int legendHeight = (int) mUnitLegendHeight;
        switch (mLegendGravity) {
            case LEFT:
                switch (mGravity) {
                    case LEFT:
                        mLegendRect = new Rect(rulerHeight - legendWidth,
                                0,
                                rulerHeight,
                                legendHeight);
                        break;
                    case RIGHT:
                        mLegendRect = new Rect(0, 0, legendWidth, legendHeight);
                        break;
                    case TOP:
                        mLegendRect = new Rect(0,
                                rulerHeight - legendHeight,
                                legendWidth,
                                rulerHeight);
                        break;
                    case BOTTOM:
                        mLegendRect = new Rect(0, 0, legendWidth, legendHeight);
                        break;
                }
                break;
            case CENTER:
                switch (mGravity) {
                    case LEFT:
                        mLegendRect = new Rect(rulerHeight - legendWidth,
                                rulerWidth / 2 - legendHeight / 2,
                                rulerHeight,
                                rulerWidth / 2 + legendHeight / 2);
                        break;
                    case RIGHT:
                        mLegendRect = new Rect(0,
                                rulerWidth / 2 - legendHeight / 2,
                                legendWidth,
                                rulerWidth / 2 + legendHeight / 2);
                        break;
                    case TOP:
                        mLegendRect = new Rect(rulerWidth / 2 - legendWidth / 2,
                                rulerHeight - legendHeight,
                                rulerWidth / 2 + legendWidth / 2,
                                rulerHeight);
                        break;
                    case BOTTOM:
                        mLegendRect = new Rect(rulerWidth / 2 - legendWidth / 2,
                                0,
                                rulerWidth / 2 + legendWidth / 2,
                                legendHeight);
                        break;
                }
                break;
            case RIGHT:
                switch (mGravity) {
                    case LEFT:
                        mLegendRect = new Rect(rulerHeight - legendWidth,
                                rulerWidth - legendHeight,
                                rulerHeight,
                                rulerWidth);
                        break;
                    case RIGHT:
                        mLegendRect = new Rect(0,
                                rulerWidth - legendHeight,
                                legendWidth,
                                rulerWidth);
                        break;
                    case TOP:
                        mLegendRect = new Rect(rulerWidth - legendWidth,
                                rulerHeight - legendHeight,
                                rulerWidth,
                                rulerHeight);
                        break;
                    case BOTTOM:
                        mLegendRect = new Rect(rulerWidth - legendWidth,
                                0,
                                rulerWidth,
                                legendHeight);
                        break;
                }

                break;
        }
    }

    /**
     * 变换MarkPst的坐标
     *
     * @param pst
     * @param rulerWidth
     * @param rulerHeight
     */
    private float[] changeMarkPst(float[] pst, float rulerWidth, float rulerHeight) {
        float[] changePst = new float[pst.length];
        int count = pst.length / 2;
        for (int i = 0; i < count; i++)
            switch (mGravity) {
                case LEFT:
                    changePst[i * 2] = pst[i * 2 + 1];
                    changePst[i * 2 + 1] = pst[i * 2] + rulerWidth / 2;
                    break;
                case RIGHT:
                    changePst[i * 2] = -pst[i * 2 + 1] + rulerHeight;
                    changePst[i * 2 + 1] = pst[i * 2] + rulerWidth / 2;
                    break;
                case BOTTOM:
                    changePst[i * 2] = pst[i * 2] + rulerWidth / 2;
                    changePst[i * 2 + 1] = -pst[i * 2 + 1] + rulerHeight;
                    break;
                case TOP:
                    changePst[i * 2] = pst[i * 2] + rulerWidth / 2;
                    changePst[i * 2 + 1] = pst[i * 2 + 1];
                    break;
            }
        return changePst;
    }

    /**
     * 根据刻度尺的位置返回刻度尺的宽度
     *
     * @param width
     * @param height
     * @return
     */
    private int getRulerWidth(int width, int height) {
        switch (mGravity) {
            case LEFT:
            case RIGHT:
                return height;
            case TOP:
            case BOTTOM:
                return width;
        }
        return 0;
    }

    /**
     * 根据刻度尺的位置返回刻度尺的高度
     *
     * @param width
     * @param height
     * @return
     */
    private int getRulerHeight(int width, int height) {
        switch (mGravity) {
            case LEFT:
            case RIGHT:
                return width;
            case TOP:
            case BOTTOM:
                return height;
        }
        return 0;
    }

    public Type getType() {
        return mType;
    }

    public Gravity getGravity() {
        return mGravity;
    }

    /**
     * 设置标记资源resid
     *
     * @param resId
     */
    public void setMarkResource(@DrawableRes int resId) {
        if (mResource != resId) {
            mResource = resId;
            setMarkDrawable(ContextCompat.getDrawable(getContext(), resId));
        }
    }

    /**
     * 设置标记drawable
     *
     * @param drawable
     */
    public void setMarkDrawable(@Nullable Drawable drawable) {
        if (mMarkDrawable != drawable) {
            mMarkPath = null;
            updateMarkDrawable(drawable);
            invalidate();
        }
    }

    private void updateMarkDrawable(Drawable d) {
        mMarkDrawable = d;
        if (d != null) {
            int width = d.getIntrinsicWidth();
            int height = d.getIntrinsicHeight();
            width = width > 0 ? width : (int) mMarkWidth;
            height = height > 0 ? height : (int) mMarkHeight;
            // 取 drawable 的颜色格式
            Bitmap.Config config = d.getOpacity() != PixelFormat.OPAQUE ?
                    Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            // 建立对应 bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);
            // 建立对应 bitmap 的画布
            Canvas canvas = new Canvas(bitmap);
            //把 drawable 内容画到画布中
            d.setBounds(0, 0, width, height);
            d.draw(canvas);
            Matrix matrix = new Matrix();
            matrix.postScale(width, mMarkWidth, height, mMarkHeight);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            mMarkBitmapDrawable = new BitmapDrawable(getResources(), bitmap);
        } else {
            mMarkBitmapDrawable = null;
        }
    }

    /**
     * 设置单位刻度表示
     *
     * @param unitLegend
     */
    public void setUnitLegend(float unitLegend) {
        if (mUnitLegend != unitLegend) {
            mUnitLegend = unitLegend;
            invalidate();
        }
    }

    /**
     * 设置Legend 文字颜色
     *
     * @param legendTextColor
     */
    public void setUnitLegendTextColor(@ColorInt int legendTextColor) {
        if (mUnitLegendTextColor != legendTextColor) {
            mUnitLegendTextColor = legendTextColor;
            invalidate();
        }
    }

    /**
     * 设置Legend文字颜色
     *
     * @param legendTextSize
     */
    public void setmUnitLegendTextSize(float legendTextSize) {
        if (mUnitLegendTextSize != legendTextSize) {
            mUnitLegendTextSize = legendTextSize;
            invalidate();
        }
    }

    /**
     * 设置一级刻度的颜色
     *
     * @param unitColor
     */
    public void setUnitColor(@ColorInt int unitColor) {
        if (mUnitColor != unitColor) {
            mUnitColor = unitColor;
            invalidate();
        }
    }

    /**
     * 设置二级刻度的颜色
     *
     * @param midUnitColor
     */
    public void setMidUnitColor(int midUnitColor) {
        if (mMidUnitColor != midUnitColor) {
            mMidUnitColor = midUnitColor;
            invalidate();
        }
    }

    /**
     * 设置三级刻度的颜色
     *
     * @param minUnitColor
     */
    public void setMinUnitColor(int minUnitColor) {
        if (mMinUnitColor != minUnitColor) {
            mMinUnitColor = minUnitColor;
            invalidate();
        }
    }

    /**
     * 设置刻度监听
     *
     * @param onFormatUnitTextListener
     */
    public void setOnFormatUnitTextListener(OnFormatUnitTextListener onFormatUnitTextListener) {
        mOnFormatUnitTextListener = onFormatUnitTextListener;
    }

    /**
     * 设置标注单位监听
     *
     * @param onFormatUnitTextListener
     */
    public void setOnFormatUnitLegendTextListener(OnFormatUnitTextListener onFormatUnitTextListener) {
        mOnUnitLegendFormatTextListener = onFormatUnitTextListener;
    }

    private float dp2px(int dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return dp * scale;
    }

//    private float px2dp(int px) {
//        float scale = getContext().getResources().getDisplayMetrics().density;
//        return px / scale;
//    }

    public enum Type {
        HEX_2(0x0),
        HEX_8(0x1),
        HEX_10(0x2),
        HEX_16(0x3);
        private int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }


        public static int getHex(@NonNull Type e) {
            switch (e) {
                case HEX_2:
                    return 2;
                case HEX_8:
                    return 8;
                case HEX_10:
                    return 10;
                case HEX_16:
                    return 16;
            }
            return 10;
        }

        public static Type getType(int code) {
            for (Type t : values()) {
                if (t.getCode() == code) {
                    return t;
                }
            }
            return HEX_10;
        }
    }

    public enum Gravity {
        LEFT(0x1), TOP(0x2), RIGHT(0x3), BOTTOM(0x4);
        private int code;

        Gravity(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Gravity getGravity(int code) {
            for (Gravity g : values()) {
                if (g.getCode() == code) {
                    return g;
                }
            }
            return BOTTOM;
        }
    }

    public enum RulerVisibility {
        VISIBLE(0x0), GONE(0x1), INVISIBLE(0x2);
        private int code;

        RulerVisibility(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static RulerVisibility getVisibility(int code) {
            for (RulerVisibility v : values()) {
                if (v.getCode() == code) {
                    return v;
                }
            }
            return VISIBLE;
        }
    }

    public enum LegendGravity {
        LEFT(0x0), CENTER(0x1), RIGHT(0x2);
        private int code;

        LegendGravity(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static LegendGravity getGravity(int code) {
            for (LegendGravity g : values()) {
                if (g.getCode() == code) {
                    return g;
                }
            }
            return LEFT;
        }
    }

    public enum Mode {
        INTEGER(0x0), FLOAT(0x1);
        private int code;

        Mode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Mode getMode(int code) {
            for (Mode m : values()) {
                if (m.getCode() == code) {
                    return m;
                }
            }
            return FLOAT;
        }
    }

    public interface OnFormatUnitTextListener {

        String onFormatText(float text, float unit, float legend);

        int getDecimalDigits();
    }

    private List<OnCurrentUnitTextListener> mOnCurrentUnitTextListeners;

    /**
     * 添加当前刻度监听
     *
     * @param onCurrentUnitTextListener
     */
    public void addOnCurrentUnitTextListener(OnCurrentUnitTextListener onCurrentUnitTextListener) {
        if (mOnCurrentUnitTextListeners == null) {
            mOnCurrentUnitTextListeners = new ArrayList<>();
        }
        if (!mOnCurrentUnitTextListeners.contains(onCurrentUnitTextListener))
            mOnCurrentUnitTextListeners.add(onCurrentUnitTextListener);
    }

    /**
     * 移除监听
     *
     * @param onCurrentUnitTextListener
     */
    public void removeOnCurrentUnitTextListener(OnCurrentUnitTextListener onCurrentUnitTextListener) {
        if (onCurrentUnitTextListener != null && mOnCurrentUnitTextListeners != null) {
            if (mOnCurrentUnitTextListeners.contains(onCurrentUnitTextListener)) {
                mOnCurrentUnitTextListeners.remove(onCurrentUnitTextListener);
            }
        }
    }

    /**
     * 设置当前刻度text
     *
     * @param text
     */
    public void setCurrentUnitText(float text) {
        float unit = text / mUnitLegend;
        setCurrentUnit(unit);
    }

    /**
     * 设置当前刻度值Unit
     *
     * @param unit
     */
    public void setCurrentUnit(float unit) {
        if (unit < 0) {
            return;
        }
        if (unit < mUnitStartNum) {
            unit = mUnitStartNum;
        } else if (unit > mUnitEndNum) {
            unit = mUnitEndNum;
        }
        final float unitWidth = mUnitSpacing + mUnitWidth;
        mUnitCurrentOffset = formatCurrentOffset(-unit * unitWidth);
        performOnCurrentUnitTextListener();
        postInvalidate();
    }

    /**
     * 当前刻度监听
     */
    public interface OnCurrentUnitTextListener {

        /**
         * 当前刻度回调
         *
         * @param text   真实刻度
         * @param unit   刻度尺刻度
         * @param legend 基本单位
         */
        void onCurrentUnitText(float text, float unit, float legend);
    }

    /**
     * 执行刻度值监听回调
     */
    private void performOnCurrentUnitTextListener() {
        if (mOnCurrentUnitTextListeners != null) {
            final float offset = mUnitCurrentOffset;
            final float unitWidth = mUnitSpacing + mUnitWidth;
            if (unitWidth != 0) {
                float unit = getFormatUnit(-offset / unitWidth);
                for (OnCurrentUnitTextListener l : mOnCurrentUnitTextListeners) {
                    if (l != null) {
                        l.onCurrentUnitText(unit * mUnitLegend, unit, mUnitLegend);
                    }
                }
            }
        }
    }


    private float getFormatUnit(float unitText) {
        switch (mMode) {
            case INTEGER:
                DecimalFormat format = getUnitTextFormat();
                String unit = format.format(unitText);
                return new BigDecimal(unit).floatValue();
            case FLOAT:
            default:
                return unitText;
        }
    }

    private DecimalFormat mUnitTextFormat;

    private DecimalFormat getUnitTextFormat() {
        if (mUnitTextFormat == null) {
            mUnitTextFormat = new DecimalFormat("#0");
            mUnitTextFormat.setRoundingMode(RoundingMode.HALF_UP);
        }
        return mUnitTextFormat;
    }


    public Builder getBuilder() {
        return mBuilder;
    }

    public class Builder {

        private Type type;//刻度尺类型
        private Mode mode;//刻度尺mode
        private float unitSpacing;//刻度间距
        private float unitLegend;//Legend表示
        private float unitLegendWidth;//Legend区域的宽度
        private float unitLegendHeight;//Legend区域的高度
        private int legendTextColor;//Legend字体颜色
        private float legendTextSize;//Legend字体大小
        private RulerVisibility legendVisibility;//Legend的可见性
        private LegendGravity legendGravity;//Legend位置
        private float LegendMargin;//Legend的问题可单位刻度之间的距离
        private float unitWidth;//一级刻度的宽度
        private float unitHeight;//一级刻度的高度
        private int unitColor;//一级刻度颜色
        private float midUnitWidth;//二级刻度的宽度
        private float midUnitHeight;//二级刻度的高度
        private int midUnitColor;//二级刻度颜色
        private float minUnitWidth;//三级刻度的宽度
        private float minUnitHeight;//三级刻度的高度
        private int minUnitColor;//三级刻度颜色
        private int unitMaxCount;//最大刻度值
        private Gravity gravity;//刻度对齐方式
        private float textSize;//刻度尺文字大小
        private int textColor;//刻度尺文字颜色
        private RulerVisibility unitMidVisibility;//二级刻度的可见性
        private float unitTextHeight;//刻度尺文字区域高度
        private float unitTextSpacing;//刻度尺文字和刻度之间的距离
        private int markColor;//标记的宽度
        private float markWidth;//标记宽度
        private float markHeight;//标记的高度
        private float[] markPst;//标记path路径坐标
        private int markHColor;//水平方向标记的颜色
        private float markHWidth;//水平方向标记的宽度
        private float markHHeight;//水平方向标记的高度
        private int horizontalLineColor;//水平线颜色
        private float horizontalLineHeight;//水平线的高度

        private Builder() {
        }

        public Type getType() {
            return type;
        }

        public Builder setType(@NonNull Type type) {
            this.type = type;
            return this;
        }

        public Mode getMode() {
            return mode;
        }

        public Builder setMode(Mode mode) {
            this.mode = mode;
            return this;
        }

        public float getUnitSpacing() {
            return unitSpacing;
        }

        public Builder setUnitSpacing(float unitSpacing) {
            this.unitSpacing = unitSpacing;
            return this;
        }

        public float getUnitLegend() {
            return unitLegend;
        }

        public Builder setUnitLegend(float unitLegend) {
            this.unitLegend = unitLegend;
            return this;
        }

        public float getUnitLegendWidth() {
            return unitLegendWidth;
        }

        public Builder setUnitLegendWidth(float unitLegendWidth) {
            this.unitLegendWidth = unitLegendWidth;
            return this;
        }

        public float getUnitLegendHeight() {
            return unitLegendHeight;
        }

        public Builder setUnitLegendHeight(float unitLegendHeight) {
            this.unitLegendHeight = unitLegendHeight;
            return this;
        }

        public int getLegendTextColor() {
            return legendTextColor;
        }

        public Builder setLegendTextColor(@ColorInt int legendTextColor) {
            this.legendTextColor = legendTextColor;
            return this;
        }

        public float getLegendTextSize() {
            return legendTextSize;
        }

        public Builder setLegendTextSize(float legendTextSize) {
            this.legendTextSize = legendTextSize;
            return this;
        }

        public RulerVisibility getLegendVisibility() {
            return legendVisibility;
        }

        public Builder setLegendVisibility(@NonNull RulerVisibility legendVisibility) {
            this.legendVisibility = legendVisibility;
            return this;
        }

        public LegendGravity getLegendGravity() {
            return legendGravity;
        }

        public Builder setLegendGravity(@NonNull LegendGravity legendGravity) {
            this.legendGravity = legendGravity;
            return this;
        }

        public float getLegendMargin() {
            return LegendMargin;
        }

        public Builder setLegendMargin(float legendMargin) {
            LegendMargin = legendMargin;
            return this;
        }

        public float getUnitWidth() {
            return unitWidth;
        }

        public Builder setUnitWidth(float unitWidth) {
            this.unitWidth = unitWidth;
            return this;
        }

        public float getUnitHeight() {
            return unitHeight;
        }

        public Builder setUnitHeight(float unitHeight) {
            this.unitHeight = unitHeight;
            return this;
        }

        public int getUnitColor() {
            return unitColor;
        }

        public Builder setUnitColor(@ColorInt int unitColor) {
            this.unitColor = unitColor;
            return this;
        }

        public float getMidUnitWidth() {
            return midUnitWidth;
        }

        public Builder setMidUnitWidth(float midUnitWidth) {
            this.midUnitWidth = midUnitWidth;
            return this;
        }

        public float getMidUnitHeight() {
            return midUnitHeight;
        }

        public Builder setMidUnitHeight(float midUnitHeight) {
            this.midUnitHeight = midUnitHeight;
            return this;
        }

        public int getMidUnitColor() {
            return midUnitColor;
        }

        public Builder setMidUnitColor(@ColorInt int midUnitColor) {
            this.midUnitColor = midUnitColor;
            return this;
        }

        public float getMinUnitWidth() {
            return minUnitWidth;
        }

        public Builder setMinUnitWidth(float minUnitWidth) {
            this.minUnitWidth = minUnitWidth;
            return this;
        }

        public float getMinUnitHeight() {
            return minUnitHeight;
        }

        public Builder setMinUnitHeight(float minUnitHeight) {
            this.minUnitHeight = minUnitHeight;
            return this;
        }

        public int getMinUnitColor() {
            return minUnitColor;
        }

        public Builder setMinUnitColor(@ColorInt int minUnitColor) {
            this.minUnitColor = minUnitColor;
            return this;
        }

        public int getUnitMaxCount() {
            return unitMaxCount;
        }

        public Builder setUnitMaxCount(int unitMaxCount) {
            this.unitMaxCount = unitMaxCount;
            return this;
        }

        public Gravity getGravity() {
            return gravity;
        }

        public Builder setGravity(@NonNull Gravity gravity) {
            this.gravity = gravity;
            return this;
        }

        public float getTextSize() {
            return textSize;
        }

        public Builder setTextSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public int getTextColor() {
            return textColor;
        }

        public Builder setTextColor(@ColorInt int textColor) {
            this.textColor = textColor;
            return this;
        }

        public RulerVisibility getUnitMidVisibility() {
            return unitMidVisibility;
        }

        public Builder setUnitMidVisibility(RulerVisibility unitMidVisibility) {
            this.unitMidVisibility = unitMidVisibility;
            return this;
        }

        public float getUnitTextHeight() {
            return unitTextHeight;
        }

        public Builder setUnitTextHeight(float unitTextHeight) {
            this.unitTextHeight = unitTextHeight;
            return this;
        }

        public float getUnitTextSpacing() {
            return unitTextSpacing;
        }

        public Builder setUnitTextSpacing(float unitTextSpacing) {
            this.unitTextSpacing = unitTextSpacing;
            return this;
        }

        public int getMarkColor() {
            return markColor;
        }

        public Builder setMarkColor(@ColorInt int markColor) {
            this.markColor = markColor;
            return this;
        }

        public float getMarkWidth() {
            return markWidth;
        }

        public Builder setMarkWidth(float markWidth) {
            this.markWidth = markWidth;
            return this;
        }

        public float getMarkHeight() {
            return markHeight;
        }

        public Builder setMarkHeight(float markHeight) {
            this.markHeight = markHeight;
            return this;
        }

        public float[] getMarkPst() {
            return markPst;
        }

        public Builder setMarkPst(@Size(multiple = 2) @NonNull float[] markPst) {
            this.markPst = markPst;
            return this;
        }

        public int getMarkHColor() {
            return markHColor;
        }

        public Builder setMarkHColor(@ColorInt int markHColor) {
            this.markHColor = markHColor;
            return this;
        }

        public float getMarkHWidth() {
            return markHWidth;
        }

        public Builder setMarkHWidth(float markHWidth) {
            this.markHWidth = markHWidth;
            return this;
        }

        public float getMarkHHeight() {
            return markHHeight;
        }

        public Builder setMarkHHeight(float markHHeight) {
            this.markHHeight = markHHeight;
            return this;
        }

        public int getmHorizontalLineColor() {
            return horizontalLineColor;
        }

        public Builder setmHorizontalLineColor(@ColorInt int horizontalLineColor) {
            this.horizontalLineColor = horizontalLineColor;
            return this;
        }

        public float getmHorizontalLineHeight() {
            return horizontalLineHeight;
        }

        public Builder setHorizontalLineHeight(float horizontalLineHeight) {
            this.horizontalLineHeight = horizontalLineHeight;
            return this;
        }

        public void notifyDataSetChanged() {
            mType = type;//刻度尺类型
            mMode = mode;//刻度尺mode
            mUnitSpacing = unitSpacing;//刻度间距
            mUnitLegend = unitLegend;//Legend表示
            mUnitLegendWidth = unitLegendWidth;//Legend区域的宽度
            mUnitLegendHeight = unitLegendHeight;//Legend区域的高度
            mUnitLegendTextColor = legendTextColor;//Legend字体颜色
            mUnitLegendTextSize = legendTextSize;//Legend字体大小
            mUnitLegendVisibility = legendVisibility;//Legend的可见性
            mLegendGravity = legendGravity;//Legend位置
            mLegendMargin = LegendMargin;//Legend的问题可单位刻度之间的距离
            mUnitWidth = unitWidth;//一级刻度的宽度
            mUnitHeight = unitHeight;//一级刻度的高度
            mUnitColor = unitColor;//一级刻度颜色
            mMidUnitWidth = midUnitWidth;//二级刻度的宽度
            mMidUnitHeight = midUnitHeight;//二级刻度的高度
            mMidUnitColor = midUnitColor;//二级刻度颜色
            mMinUnitWidth = minUnitWidth;//三级刻度的宽度
            mMidUnitHeight = minUnitHeight;//三级刻度的高度
            mMinUnitColor = minUnitColor;//三级刻度颜色
            mUnitEndNum = unitMaxCount;//最大刻度值
            mGravity = gravity;//刻度对齐方式
            mTextSize = textSize;//刻度尺文字大小
            mTextColor = textColor;//刻度尺文字颜色
            mRulerUnitMidVisibility = unitMidVisibility;//二级刻度的可见性
            mUnitTextHeight = unitTextHeight;//刻度尺文字区域高度
            mUnitTextSpacing = unitTextSpacing;//刻度尺文字和刻度之间的距离
            mMarkColor = markColor;//标记的宽度
            mMarkWidth = markWidth;//标记宽度
            mMarkHeight = markHeight;//标记的高度
            mMarkPst = markPst;//标记path路径坐标
            mMarkHColor = markHColor;//水平方向标记的颜色
            mMarkHWidth = markHWidth;//水平方向标记的宽度
            mMarkHHeight = markHHeight;//水平方向标记的高度
            mHorizontalLineColor = horizontalLineColor;//水平线颜色
            mHorizontalLineHeight = horizontalLineHeight;//水平线的高度

            requestLayout();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mAnimHelper.cancel();
        super.onDetachedFromWindow();
    }
}
