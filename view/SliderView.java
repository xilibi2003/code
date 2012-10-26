package com.ijinshan.kbatterydoctor.screensaver;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ijinshan.kbatterydoctor.R;

public class SliderView extends View {
	protected boolean isDraging = false;// 是否正在拖动中

	protected ColorFilter mColorFilter;

	protected Drawable mBackgroundDrawable;
	protected Drawable mLeftDrawable, mRightDrawable, mSliderDrawable;
	protected Rect mBackgroundRect, mDefLeftRect, mDefRightRect,
			mDefSliderRect;// 默认左右图标的显示区域和中间滑，一般不做改变
	protected Rect mSliderRect;// 当前滑块的显示区域

	protected Point mStartPoint;// 用户第一次按下时的点
	protected int[] stateSet = new int[1];

	protected OnSlideListener mSlideListener;
	protected SlideEvent mEvent;

	public SliderView(Context context) {
		this(context, null);
	}

	public SliderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SliderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.SliderView, defStyle, 0);
		setLeftTargetDrawable(a.getDrawable(R.styleable.SliderView_left_target));
		setRightTargetDrawable(a
				.getDrawable(R.styleable.SliderView_right_target));
		setSliderDrawable(a.getDrawable(R.styleable.SliderView_slider));
		a.recycle();

		mStartPoint = new Point();
		mEvent = new SlideEvent();// 初始化事件，一个SliderView只有唯一一个SlideEvent
		mBackgroundRect = new Rect();
		mDefLeftRect = new Rect();
		mDefRightRect = new Rect();
		mDefSliderRect = new Rect();
		mSliderRect = new Rect();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int maxWidth = 0, maxHeight = 0;
		if (null != mLeftDrawable) {
			maxWidth += mLeftDrawable.getIntrinsicWidth();
			maxHeight = Math.max(maxWidth, mLeftDrawable.getIntrinsicHeight());
		}
		if (null != mRightDrawable) {
			maxWidth += mRightDrawable.getIntrinsicWidth();
			maxHeight = Math
					.max(maxHeight, mRightDrawable.getIntrinsicHeight());
		}
		if (null != mSliderDrawable) {
			maxWidth += mSliderDrawable.getIntrinsicWidth();
			maxHeight = Math.max(maxHeight,
					mSliderDrawable.getIntrinsicHeight());
		}

		setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
				resolveSize(maxHeight, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		mBackgroundRect.set(0, 0, getRight() - getLeft(), getBottom()
				- getTop());
		if (null != mBackgroundDrawable)
			if (null != mLeftDrawable) {
				mDefLeftRect.set(getPaddingLeft(),
						(getHeight() - mLeftDrawable.getIntrinsicHeight()) / 2,
						getPaddingLeft() + mLeftDrawable.getIntrinsicWidth(),
						(getHeight() + mLeftDrawable.getIntrinsicHeight()) / 2);
				mLeftDrawable.setBounds(mDefLeftRect);
			} else
				mDefLeftRect.setEmpty();

		if (null != mRightDrawable) {
			mDefRightRect.set(getWidth() - mRightDrawable.getIntrinsicWidth()
					- getPaddingRight(),
					(getHeight() - mRightDrawable.getIntrinsicHeight()) / 2,
					getWidth() - getPaddingRight(),
					(getHeight() + mRightDrawable.getIntrinsicHeight()) / 2);
			mRightDrawable.setBounds(mDefRightRect);
		} else
			mDefRightRect.setEmpty();

		if (null != mSliderDrawable)
			mDefSliderRect.set(
					(getWidth() - mSliderDrawable.getIntrinsicWidth()) / 2,
					(getHeight() - mSliderDrawable.getIntrinsicHeight()) / 2,
					(getWidth() + mSliderDrawable.getIntrinsicWidth()) / 2,
					(getHeight() + mSliderDrawable.getIntrinsicHeight()) / 2);
		else
			mDefSliderRect.setEmpty();

		mSliderRect.set(mDefSliderRect);// 默认滑块显示区域
	}

	/** 验证drawable是否由本类控制，主要用于AnimationDrawable的刷新回调、drawable的state变化回调等 */
	@Override
	protected boolean verifyDrawable(Drawable who) {
		return who == mBackgroundDrawable || who == mSliderDrawable
				|| who == mLeftDrawable || who == mRightDrawable;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(0, PorterDuff.Mode.CLEAR);
		if (null != mBackgroundDrawable && mBackgroundDrawable.isVisible()) {
			mBackgroundDrawable.setBounds(mBackgroundRect);
			mBackgroundDrawable.draw(canvas);
		}

		if (null != mLeftDrawable && mLeftDrawable.isVisible())
			mLeftDrawable.draw(canvas);

		if (null != mRightDrawable && mRightDrawable.isVisible())
			mRightDrawable.draw(canvas);

		if (null != mSliderDrawable) {
			mSliderDrawable.setBounds(mSliderRect);
			mSliderDrawable.draw(canvas);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mDefSliderRect.contains(x, y)) {
				isDraging = true;// 设置开始拖动标记
				mStartPoint.set(x, y);// 记录手指按下起始点

				stateSet[0] = android.R.attr.state_pressed;
				if (null != mSliderDrawable)
					mSliderDrawable.setState(stateSet);
				if (null != mLeftDrawable)
					mLeftDrawable.setVisible(true, true);
				if (null != mRightDrawable)
					mRightDrawable.setVisible(true, true);
				if (null != mBackgroundDrawable)
					mBackgroundDrawable.setVisible(true, true);
				/* 开始背景上向两边滑开的箭头动画 */
				if (mBackgroundDrawable instanceof AnimationDrawable) {
					((AnimationDrawable) mBackgroundDrawable).start();
				}
				/* 触发中间滑块按下事件 */
				mEvent.setAction(SlideEvent.ACTION_SLIDERDOWN);
				if (null != mSlideListener)
					mSlideListener.OnSlide(this, mEvent);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (isDraging) {
				mSliderRect.set(mDefSliderRect);
				mSliderRect.offset(x - mStartPoint.x, 0);
				if (mSliderRect.left < 0)
					mSliderRect.offset(Math.abs(mSliderRect.left), 0);
				if (mSliderRect.right > getWidth())
					mSliderRect.offset(getWidth() - mSliderRect.right, 0);

				if (null != mLeftDrawable) {
					if (Rect.intersects(mDefLeftRect, mSliderRect)) {
						stateSet[0] = android.R.attr.state_active;
						mLeftDrawable.setState(stateSet);
					} else {
						mLeftDrawable.setState(null);
					}
				}

				if (null != mRightDrawable) {
					if (Rect.intersects(mDefRightRect, mSliderRect)) {
						stateSet[0] = android.R.attr.state_active;
						mRightDrawable.setState(stateSet);
					} else {
						mRightDrawable.setState(null);
					}
				}
				invalidate();
				/* 触发拖着滑块移动事件 */
				mEvent.setAction(SlideEvent.ACTION_SLIDING);
				if (null != mSlideListener)
					mSlideListener.OnSlide(this, mEvent);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			/* 还原各个Drawable的状态 */
			if (null != mSliderDrawable)
				mSliderDrawable.setState(null);
			if (null != mLeftDrawable) {
				mLeftDrawable.setState(null);
				mLeftDrawable.setVisible(false, true);
			}
			if (null != mRightDrawable) {
				mRightDrawable.setState(null);
				mRightDrawable.setVisible(false, true);
			}
			if (null != mBackgroundDrawable) {
				mBackgroundDrawable.setVisible(false, false);
			}
			/* 清除背景上的动画 */
			if (mBackgroundDrawable instanceof AnimationDrawable) {
				((AnimationDrawable) mBackgroundDrawable).stop();
			}
			invalidate();
			if (isDraging) {
				isDraging = false;
				/* 触发滑到左边响应区域事件 */
				if (Rect.intersects(mDefLeftRect, mSliderRect)
						&& null != mSlideListener) {
					mEvent.setAction(SlideEvent.ACTION_TRIGLEFT);
					mSlideListener.OnSlide(this, mEvent);
				}
				/* 触发滑到右边响应区域事件 */
				else if (Rect.intersects(mDefRightRect, mSliderRect)
						&& null != mSlideListener) {
					mEvent.setAction(SlideEvent.ACTION_TRIGRIGHT);
					mSlideListener.OnSlide(this, mEvent);
				} else {
					mSliderRect.set(mDefSliderRect);
				}
				/* 触发滑块弹起事件 */
				mEvent.setAction(SlideEvent.ACTION_SLIDERUP);
				if (null != mSlideListener)
					mSlideListener.OnSlide(this, mEvent);
			}
			break;
		}

		super.dispatchTouchEvent(event);
		return true;
	}

	/** 故意覆盖父类此方法，以实现仅在按下是有背景效果 */
	@Override
	public void setBackgroundDrawable(Drawable d) {
		mBackgroundDrawable = d;
		if (null != mBackgroundDrawable) {
			mBackgroundDrawable.setCallback(this);
			mBackgroundDrawable.setVisible(false, true);
			mBackgroundDrawable.setColorFilter(mColorFilter);
		}
	}

	/** 故意覆盖父类此方法 */
	@Override
	public void setBackgroundResource(int resid) {
		setBackgroundDrawable(getResources().getDrawable(resid));
	}

	@Override
	public Drawable getBackground() {
		return mBackgroundDrawable;
	}

	/** 设置左边目标区域图片 */
	public void setLeftTargetDrawable(Drawable d) {
		mLeftDrawable = d;
		if (null != mLeftDrawable) {
			mLeftDrawable.setCallback(this);
			mLeftDrawable.setVisible(false, true);
			mLeftDrawable.setColorFilter(mColorFilter);
		}
		requestLayout();
	}

	public void setLeftTargetResource(int resid) {
		setLeftTargetDrawable(getResources().getDrawable(resid));
	}

	public Drawable getLeftTargetDrawable() {
		return mLeftDrawable;
	}

	/** 设置右边目标区域图片 */
	public void setRightTargetDrawable(Drawable d) {
		mRightDrawable = d;
		if (null != mRightDrawable) {
			mRightDrawable.setCallback(this);
			mRightDrawable.setVisible(false, true);
			mRightDrawable.setColorFilter(mColorFilter);
		}
		requestLayout();
	}

	public void setRightTargetResource(int resid) {
		setRightTargetDrawable(getResources().getDrawable(resid));
	}

	public Drawable getRightTargetDrawable() {
		return mRightDrawable;
	}

	/** 设置中间滑块图片 */
	public void setSliderDrawable(Drawable d) {
		mSliderDrawable = d;
		if (null != mSliderDrawable) {
			mSliderDrawable.setCallback(this);
			mSliderDrawable.setVisible(false, true);
			mSliderDrawable.setColorFilter(mColorFilter);
		}
		requestLayout();
	}

	public void setSliderResource(int resid) {
		setSliderDrawable(getResources().getDrawable(resid));
	}

	public Drawable getSliderDrawable() {
		return mRightDrawable;
	}

	public void setOnSlideListener(OnSlideListener listener) {
		mSlideListener = listener;
	}

	public void setColorFilter(ColorFilter cf) {
		mColorFilter = cf;
		if (null != mLeftDrawable) {
			mLeftDrawable.setColorFilter(cf);
			invalidateDrawable(mLeftDrawable);
		}
		if (null != mRightDrawable) {
			mRightDrawable.setColorFilter(cf);
			invalidateDrawable(mLeftDrawable);
		}
		if (null != mSliderDrawable) {
			mSliderDrawable.setColorFilter(cf);
			invalidateDrawable(mLeftDrawable);
		}
		if (null != mBackgroundDrawable) {
			mBackgroundDrawable.setColorFilter(cf);
			invalidateDrawable(mLeftDrawable);
		}
	}

	public void setColorFilter(int color, Mode mode) {
		setColorFilter(new PorterDuffColorFilter(color, mode));
	}

	public static class SlideEvent {
		public static final int ACTION_SLIDERDOWN = 0;
		public static final int ACTION_SLIDERUP = 1;
		public static final int ACTION_SLIDING = 2;
		public static final int ACTION_TRIGLEFT = 3;
		public static final int ACTION_TRIGRIGHT = 4;

		private int mAction;

		void setAction(int action) {
			this.mAction = action;
		}

		public int getAction() {
			return mAction;
		}
	}

	public static interface OnSlideListener {
		public void OnSlide(SliderView view, SlideEvent event);
	}
}
