
package com.dianxinos.clock.view;

import com.dianxinos.clock.util.Log;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class ExpandAnimation extends Animation {
    private boolean isFirstTransforamtion = true;

    private LinearLayout.LayoutParams mLayoutParams;

    private int mMarginBottomFromY;

    private int mMarginBottomToY;

    private Resources mRes = null;

    String mTitle = null;

    private boolean mVanishAfter = false;

    private View mView;

    private boolean wasEndedAlready = false;

    public ExpandAnimation(int duration, View v, boolean vanishAfter, int height, Context context) {
        setDuration(duration);
        mRes = context.getResources();
        mView = v;
        mVanishAfter = vanishAfter;
        mLayoutParams = ((LinearLayout.LayoutParams) v.getLayoutParams());

        Log.i("bottonMargin:" + mLayoutParams.bottomMargin );

        if (mLayoutParams.bottomMargin == 0) {
            // for close.
            mMarginBottomFromY = mLayoutParams.bottomMargin;
            mMarginBottomToY = 0 - height;
        } else {
            mMarginBottomToY = 0;
            mMarginBottomFromY = 0 - height;
        }

        if (mMarginBottomFromY != 0)
            v.setVisibility(View.GONE);
    }

    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        if (interpolatedTime < 1.0F) {
            if (isFirstTransforamtion) {
                mView.setVisibility(View.VISIBLE);
                isFirstTransforamtion = false;
            }
            float f = TypedValue.applyDimension(1, mMarginBottomFromY
                    + (int) (interpolatedTime * (mMarginBottomToY - mMarginBottomFromY)),
                    mRes.getDisplayMetrics());
            mLayoutParams.bottomMargin = (int) f;
            mView.getParent().requestLayout();
        }
        else {
            if (wasEndedAlready)
                return;
            mLayoutParams.bottomMargin = mMarginBottomToY;
            mView.getParent().requestLayout();
            if (mVanishAfter)
                mView.setVisibility(View.GONE);
            wasEndedAlready = true;
        }
    }
}
