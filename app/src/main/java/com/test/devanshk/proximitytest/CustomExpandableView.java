package com.test.devanshk.proximitytest;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by devanshk on 7/17/15.
 */
public class CustomExpandableView extends RelativeLayout {
    private static final int DURATION = 400;
    private float DEGREES;
    private TextView textView;
    private RelativeLayout clickableLayout;
    private LinearLayout contentLayout;
    private List<ViewGroup> outsideContentLayoutList;
    private int outsideContentHeight = 0;
    private ImageView leftIcon;
    private ImageView rightIcon;
    private ValueAnimator animator;
    private RotateAnimation rotateAnimator;

    public CustomExpandableView(Context context) {
        super(context);
        this.init();
    }

    public CustomExpandableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public CustomExpandableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    private void init() {
        inflate(this.getContext(), com.expandable.view.R.layout.expandable_view, this);
        this.outsideContentLayoutList = new ArrayList();
        this.textView = (TextView)this.findViewById(com.expandable.view.R.id.expandable_view_title);
        this.clickableLayout = (RelativeLayout)this.findViewById(com.expandable.view.R.id.expandable_view_clickable_content);
        this.contentLayout = (LinearLayout)this.findViewById(com.expandable.view.R.id.expandable_view_content_layout);
        this.leftIcon = (ImageView)this.findViewById(com.expandable.view.R.id.expandable_view_image);
        this.rightIcon = (ImageView)this.findViewById(com.expandable.view.R.id.expandable_view_right_icon);
        this.contentLayout.setVisibility(GONE);
        this.clickableLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(CustomExpandableView.this.contentLayout.isShown()) {
                    CustomExpandableView.this.collapse();
                } else {
                    CustomExpandableView.this.expand();
                    //Hide all the other views
                    for (CustomExpandableView cev : MainActivity.expandableViews)
                        if (cev!=null && cev.clickableLayout != v && cev.contentLayout.isShown())
                            cev.collapse();
                }
            }
        });
        this.contentLayout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                CustomExpandableView.this.contentLayout.getViewTreeObserver().removeOnPreDrawListener(this);
                CustomExpandableView.this.contentLayout.setVisibility(GONE);
                int widthSpec = MeasureSpec.makeMeasureSpec(0, 0);
                int heightSpec = MeasureSpec.makeMeasureSpec(0, 0);
                CustomExpandableView.this.contentLayout.measure(widthSpec, heightSpec);
                CustomExpandableView.this.animator = CustomExpandableView.this.slideAnimator(0, CustomExpandableView.this.contentLayout.getMeasuredHeight());
                return true;
            }
        });
    }

    public void setVisibleLayoutHeight(int newHeight) {
        this.clickableLayout.getLayoutParams().height = newHeight;
    }

    public void setOutsideContentLayout(ViewGroup outsideContentLayout) {
        this.outsideContentLayoutList.add(outsideContentLayout);
    }

    public void setOutsideContentLayout(ViewGroup... params) {
        for(int i = 0; i < params.length; ++i) {
            this.outsideContentLayoutList.add(params[i]);
        }

    }

    public TextView getTextView() {
        return this.textView;
    }

    public LinearLayout getContentLayout() {
        return this.contentLayout;
    }

    public void addContentView(View newContentView) {
        this.contentLayout.addView(newContentView);
        this.contentLayout.invalidate();
    }

    public void fillData(int leftResId, String text, boolean useChevron) {
        this.textView.setText(text);
        if(leftResId == 0) {
            this.leftIcon.setVisibility(GONE);
        } else {
            this.leftIcon.setImageResource(leftResId);
        }

        if(useChevron) {
            this.DEGREES = 180.0F;
            this.rightIcon.setImageResource(com.expandable.view.R.drawable.ic_expandable_view_chevron);
        } else {
            this.DEGREES = -225.0F;
            this.rightIcon.setImageResource(com.expandable.view.R.drawable.ic_expandable_view_plus);
        }

    }

    public void fillData(int leftResId, int stringResId, boolean useChevron) {
        this.fillData(leftResId, this.getResources().getString(stringResId), useChevron);
    }

    public void fillData(int leftResId, String text) {
        this.fillData(leftResId, text, false);
    }

    public void fillData(int leftResId, int stringResId) {
        this.fillData(leftResId, this.getResources().getString(stringResId), false);
    }

    public void expand() {
        this.contentLayout.setVisibility(VISIBLE);
        int x = this.rightIcon.getMeasuredWidth() / 2;
        int y = this.rightIcon.getMeasuredHeight() / 2;
        this.rotateAnimator = new RotateAnimation(0.0F, this.DEGREES, (float)x, (float)y);
        this.rotateAnimator.setInterpolator(new LinearInterpolator());
        this.rotateAnimator.setRepeatCount(0);
        this.rotateAnimator.setFillAfter(true);
        this.rotateAnimator.setDuration(400L);
        this.rightIcon.startAnimation(this.rotateAnimator);
        this.animator.start();
    }

    public void collapse() {
        int finalHeight = this.contentLayout.getHeight();
        int x = this.rightIcon.getMeasuredWidth() / 2;
        int y = this.rightIcon.getMeasuredHeight() / 2;
        this.rotateAnimator = new RotateAnimation(this.DEGREES, 0.0F, (float)x, (float)y);
        this.rotateAnimator.setInterpolator(new LinearInterpolator());
        this.rotateAnimator.setRepeatCount(0);
        this.rotateAnimator.setFillAfter(true);
        this.rotateAnimator.setDuration(400L);
        ValueAnimator mAnimator = this.slideAnimator(finalHeight, 0);
        mAnimator.addListener(new Animator.AnimatorListener() {
            public void onAnimationEnd(Animator animator) {
                CustomExpandableView.this.contentLayout.setVisibility(GONE);
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }
        });
        this.rightIcon.startAnimation(this.rotateAnimator);
        mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(new int[]{start, end});
        animator.setDuration(400L);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = ((Integer)valueAnimator.getAnimatedValue()).intValue();
                LayoutParams layoutParams = (LayoutParams) CustomExpandableView.this.contentLayout.getLayoutParams();
                layoutParams.height = value;
                CustomExpandableView.this.contentLayout.setLayoutParams(layoutParams);
                CustomExpandableView.this.contentLayout.invalidate();
                if(CustomExpandableView.this.outsideContentLayoutList != null && !CustomExpandableView.this.outsideContentLayoutList.isEmpty()) {
                    Iterator i$ = CustomExpandableView.this.outsideContentLayoutList.iterator();

                    while(i$.hasNext()) {
                        ViewGroup outsideParam = (ViewGroup)i$.next();
                        LayoutParams params = (LayoutParams) outsideParam.getLayoutParams();
                        if(CustomExpandableView.this.outsideContentHeight == 0) {
                            CustomExpandableView.this.outsideContentHeight = params.height;
                        }

                        params.height = CustomExpandableView.this.outsideContentHeight + value;
                        outsideParam.setLayoutParams(params);
                        outsideParam.invalidate();
                    }
                }

            }
        });
        return animator;
    }
}
