package com.test.devanshk.proximitytest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nineoldandroids.animation.Animator;


public class DemoActivity extends ActionBarActivity {
    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private View continueButton, finishButton;
    public static Activity parent;

    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        parent = this;
        getSupportActionBar().hide();
        mViewPager = (ViewPager)findViewById(R.id.demo_viewpager);
        mPagerAdapter = new MyPagerAdapter();
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        continueButton = findViewById(R.id.demo_continue_button);
        finishButton = findViewById(R.id.demo_finish_button);

        finishButton.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.RotateIn).duration(200).playOn(continueButton);
        YoYo.with(Techniques.RotateIn).duration(200)
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        finishButton.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).playOn(finishButton);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 3) {
                    YoYo.with(Techniques.RotateOut)
                            .duration(500)
                            .withListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    if (mViewPager.getCurrentItem() == 3)
                                        continueButton.setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            })
                            .playOn(continueButton);
                    YoYo.with(Techniques.RotateIn)
                            .duration(500)
                            .withListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animation) {
                                    finishButton.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onAnimationEnd(Animator animation) {

                                }

                                @Override
                                public void onAnimationCancel(Animator animation) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animation) {

                                }
                            })
                            .playOn(finishButton);
                } else {
                    if (finishButton.getVisibility() == View.VISIBLE) {
                        YoYo.with(Techniques.RotateOut)
                                .duration(500)
                                .withListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        if (mViewPager.getCurrentItem() < 3)
                                            finishButton.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                })
                                .playOn(finishButton);
                        YoYo.with(Techniques.RotateIn)
                                .duration(500)
                                .withListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                        continueButton.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                })
                                .playOn(continueButton);
                    }
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cur = mViewPager.getCurrentItem();
                if (cur != mPagerAdapter.getCount() - 1) {
                    mViewPager.setCurrentItem(++cur, true);
                }
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.editor.putBoolean("firstOpen", false);
                MainActivity.editor.commit();
                setResult(1);
                finish();
            }
        });
    }

    private class MyPagerAdapter extends PagerAdapter {

        public int getCount() {
            return 4;
        }

        public Object instantiateItem(View collection, int position) {

            LayoutInflater inflater = (LayoutInflater) collection.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            int resId = 0;
            switch (position) {
                case 0:
                    resId = R.layout.demo_1;
                    break;
                case 1:
                    resId = R.layout.demo_2;
                    break;
                case 2:
                    resId = R.layout.demo_3;
                    break;
                case 3:
                    resId = R.layout.demo_4;
                    break;
            }

            View view = inflater.inflate(resId, null);

            ((ViewPager) collection).addView(view, 0);

            return view;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);

        }


        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == ((View) arg1);

        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }
}