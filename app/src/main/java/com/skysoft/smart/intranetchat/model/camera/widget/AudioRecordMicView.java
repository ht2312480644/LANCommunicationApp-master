/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/4
 * Description: PG1-Smart Team-CT PT-68 [MM] Voice Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.skysoft.smart.intranetchat.R;


public class AudioRecordMicView extends View{

	private Drawable micBgDrawable;
	private Drawable micDrawable1;
	private Drawable micDrawable2;
	private Rect micBgDrawableRect=new Rect();
	private Rect micDrawableRect1=new Rect();
	private Rect micDrawableRect2=new Rect();
	private Rect spaceRect=new Rect();
	private int maxLevel=7;
	
	public AudioRecordMicView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public AudioRecordMicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public AudioRecordMicView(Context context) {
		super(context);
		init();
	}

	private void init()
	{
		micBgDrawable=getResources().getDrawable(R.drawable.tooltip_mic_bg);
		micDrawable1=getResources().getDrawable(R.drawable.tooltip_mic_1);
		micDrawable2=getResources().getDrawable(R.drawable.tooltip_mic_2);
	}

	public void setLevel(int level)
	{
		int progress=level*micDrawableRect2.height()/maxLevel;
		setProgress(progress);
	}
	
	public void setMaxLevel(int maxLevel)
	{
		this.maxLevel=maxLevel;
	}
	
	public int getMaxLevel()
	{
		return maxLevel;
	}
	 
	private void setProgress(int progress)
	{
		int bottom=0;
		if(progress>micDrawableRect2.height())
		{
			bottom=0;
		}
		else
		{
			bottom=micDrawableRect2.height()-progress;
		}
		spaceRect.set(micDrawableRect2.left, micDrawableRect2.top, micDrawableRect2.right, micDrawableRect2.top+bottom);
		invalidate();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(micBgDrawable.getIntrinsicWidth(), micBgDrawable.getIntrinsicHeight()); 
		
		micBgDrawableRect.set(0, 0, micBgDrawable.getIntrinsicWidth(), micBgDrawable.getIntrinsicHeight());
		micBgDrawable.setBounds(micBgDrawableRect);
		
		int left=(getMeasuredWidth()-micDrawable1.getIntrinsicWidth())/2;
		int top=(getMeasuredHeight()-micDrawable1.getIntrinsicHeight())/2;

		micDrawableRect1.set(left, top, left+micDrawable1.getIntrinsicWidth(), top+micDrawable1.getIntrinsicHeight());
		micDrawable1.setBounds(micDrawableRect1);

		micDrawableRect2.set(micDrawableRect1);
		micDrawable2.setBounds(micDrawableRect2);
		
		spaceRect.set(micDrawableRect1);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		micBgDrawable.draw(canvas);
		micDrawable1.draw(canvas);
		
        canvas.save();
        canvas.clipRect(micDrawableRect2);
        canvas.clipRect(spaceRect,Region.Op.DIFFERENCE);
		micDrawable2.draw(canvas);
        canvas.restore();
	}
}
