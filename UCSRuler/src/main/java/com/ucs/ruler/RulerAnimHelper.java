package com.ucs.ruler;/**
 * Created by ttarfall on 2017/6/15.
 */

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.lang.ref.WeakReference;

/**
 * 刻度尺滚动处理动画
 *
 * @author ttarfall
 * @date 2017-06-15 10:12
 */
public class RulerAnimHelper implements ValueAnimator.AnimatorUpdateListener, Animator.AnimatorListener {

    private WeakReference<RulerView> mRuler;
    private long mDuration;//动画执行事件
    private float mDistance;//动画平移距离
    private Interpolator mInterpolator;//动画减速器
    private ValueAnimator mValueAnimator;

    public RulerAnimHelper(RulerView rulerView) {
        mRuler = new WeakReference<>(rulerView);
        mInterpolator = new DecelerateInterpolator();
    }


    public void fling(float distance, long duration) {
        if (distance != 0) {
            mDistance = distance;
            mDuration = duration;
            mValueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            mValueAnimator.setDuration(mDuration);
            mValueAnimator.setInterpolator(mInterpolator);
            mValueAnimator.addUpdateListener(this);
            mValueAnimator.addListener(this);
            mValueAnimator.start();
        } else {
            RulerView rulerView = mRuler.get();
            if (rulerView != null) {
                rulerView.onAnimationEnd(0);
            }
        }
    }

    public long getDuration() {
        return mDuration;
    }

    public float getDistance() {
        return mDistance;
    }

    public Interpolator getInterpolator() {
        return mInterpolator;
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float value = (float) animation.getAnimatedValue();
        float dis = value * mDistance;
        RulerView rulerView = mRuler.get();
        if (rulerView != null) {
            rulerView.onAnimationUpdate(dis);
        }
    }


    public boolean isRunning() {
        return mValueAnimator == null ? false : mValueAnimator.isRunning();
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        RulerView rulerView = mRuler.get();
        if (rulerView != null) {
            rulerView.onAnimationEnd(mDistance);
        }

    }

    @Override
    public void onAnimationCancel(Animator animation) {
        if (animation instanceof ValueAnimator) {
            float value = (float) ((ValueAnimator) animation).getAnimatedValue();
            float dis = value * mDistance;
            RulerView rulerView = mRuler.get();
            if (rulerView != null) {
                rulerView.onAnimationEnd(dis);
            }
        }

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    public void cancel() {
        if (mValueAnimator != null && isRunning()) {
            mValueAnimator.cancel();
        }
    }
}
