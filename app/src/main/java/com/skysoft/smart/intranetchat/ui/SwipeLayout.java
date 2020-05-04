/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-60][Intranet Chat] [APP][UI] contact list page
 */
package com.skysoft.smart.intranetchat.ui;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

@SuppressLint("ClickableViewAccessibility")
public class SwipeLayout extends FrameLayout {

	private ViewDragHelper dragHelper;
	private OnSwipeChangeListener swipeChangeListener;
	private Status status= Status.CLOSE;

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public static enum Status {
		OPEN, CLOSE, DRAGING
	}

	public static interface OnSwipeChangeListener {
		void onDraging(SwipeLayout mSwipeLayout);

		void onOpen(SwipeLayout mSwipeLayout);

		void onClose(SwipeLayout mSwipeLayout);
		
		void onStartOpen(SwipeLayout mSwipeLayout);
		
		void onStartClose(SwipeLayout mSwipeLayout);
		
	}

	public SwipeLayout(Context context) {
		this(context, null);
	}

	public SwipeLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		dragHelper = ViewDragHelper.create(this, callback);
	}

	private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {

		public boolean tryCaptureView(View child, int pointerId) {
			return true;
		}

		public int clampViewPositionHorizontal(View child, int left, int dx) {//
			if (child == frontView) {
				if (left > 0) {
					return 0;
				} else if (left < -range) {
					return -range;
				}
			} else if (child == backView) {
				if (left > width) {
					return width;
				} else if (left < width - range) {
					return width - range;
				}
			}
			return left;
		}

		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
			if (changedView == frontView) {
				backView.offsetLeftAndRight(dx);
			} else if (changedView == backView) {
				frontView.offsetLeftAndRight(dx);
			}
			dispatchSwipeEvent();
			invalidate();
		};

		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			if (xvel == 0 && frontView.getLeft() < -range * 0.5f) {
				open();
			} else if (xvel < 0) {
				open();
			} else {
				close();
			}
		};

		public int getViewHorizontalDragRange(View child) {
			return 1;
		}

		public int getViewVerticalDragRange(View child) {
			return 1;
		}


	};

	protected void dispatchSwipeEvent() {
		Status preStatus=status;
		status=updateStatus();
		
		if(swipeChangeListener!=null){
			swipeChangeListener.onDraging(this);
		}
		
		if(preStatus!=status&&swipeChangeListener!=null){
			if(status== Status.CLOSE){
				swipeChangeListener.onClose(this);
			}else if(status== Status.OPEN){
				swipeChangeListener.onOpen(this);
			}else if(status== Status.DRAGING){
				if(preStatus== Status.CLOSE){
					swipeChangeListener.onStartOpen(this);
				}else if(preStatus== Status.OPEN){
					swipeChangeListener.onStartClose(this);
				}
			}
		}
	}

	private Status updateStatus() {
		int left=frontView.getLeft();
		if(left==0){
			return Status.CLOSE;
		}else if(left==-range){
			return Status.OPEN;
		}
		return Status.DRAGING;
	}

	private View backView;
	private View frontView;
	private int height;
	private int width;
	private int range;

	public void computeScroll() {
		if (dragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	};

	public void open() {
		open(true);
	}

	public void open(boolean isSmooth) {
		int finalLeft = -range;
		if (isSmooth) {
			if (dragHelper.smoothSlideViewTo(frontView, finalLeft, 0)) {
				ViewCompat.postInvalidateOnAnimation(this);
			}
		} else {
			layoutContent(true);
		}
	}

	public void close() {
		close(true);
	}

	public void close(boolean isSmooth) {
		int finalLeft = 0;
		if (isSmooth) {
			if (dragHelper.smoothSlideViewTo(frontView, finalLeft, 0)) {
				ViewCompat.postInvalidateOnAnimation(this);
			}
		} else {
			layoutContent(false);
		}
	}

	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		layoutContent(false);

	}

	private void layoutContent(boolean isOpen) {
		Rect frontRect = computeFrontViewRect(isOpen);
		frontView.layout(frontRect.left, frontRect.top, frontRect.right, frontRect.bottom);

		Rect backRect = computeBackViewRect(frontRect);
		backView.layout(backRect.left, backRect.top, backRect.right, backRect.bottom);

	}

	private Rect computeBackViewRect(Rect frontRect) {
		int left = frontRect.right;
		return new Rect(left, 0, left + range, height);
	}

	private Rect computeFrontViewRect(boolean isOpen) {
		int left = 0;
		if (isOpen) {
			left = -range;
		}
		return new Rect(left, 0, left + width, height);
	}

	protected void onFinishInflate() {
		super.onFinishInflate();

		int childCount = getChildCount();
		if (childCount < 2) {
			throw new IllegalStateException("you need 2 children view");
		}
		if (!(getChildAt(0) instanceof ViewGroup) || !(getChildAt(1) instanceof ViewGroup)) {
			throw new IllegalArgumentException("your children must be instance of ViewGroup");
		}

		backView = getChildAt(0);
		frontView = getChildAt(1);

	}

protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		height = frontView.getMeasuredHeight();
		width = frontView.getMeasuredWidth();
		range = backView.getMeasuredWidth();
	}

	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return dragHelper.shouldInterceptTouchEvent(ev);
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		dragHelper.processTouchEvent(event);
		return true;
	}

	public void setSwipeChangeListener(OnSwipeChangeListener swipeChangeListener) {
		this.swipeChangeListener = swipeChangeListener;
	}

}
