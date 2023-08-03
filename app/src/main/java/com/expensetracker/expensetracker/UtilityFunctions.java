package com.expensetracker.expensetracker;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;


public class UtilityFunctions {

    public static String getRandomKey()
    {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String randomKey = "";

        for (int i = 0; i < 20; i++) {
            randomKey += CHARS.charAt( (int) ( Math.floor(Math.random() * CHARS.length())));
        }
        return randomKey;
    }

    public static void showSnackbar(String message)
    {
        Snackbar snackbar = Snackbar.make(MainActivity.RootLayout, message, Snackbar.LENGTH_SHORT);
        // snackbar.setAnchorView(MainActivity.BottomNavigationBar);
        snackbar.getView().setBackgroundResource(R.color.appDark);
        snackbar.show();
    }

    public static void animateButton(Context context, RecyclerView recyclerView, View Save, boolean show_button)
    {

        //Initial Animation
        if(show_button) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.slide_up_delayed);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    Save.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Save.clearAnimation();
                    Save.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            animation.setFillAfter(true);
            Save.startAnimation(animation);
        }


        //Animation on Scroll
        final boolean[] animation_started = {false};
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (show_button) {
                    Animation animation;

                    if (dy > 0 && !animation_started[0]) {

                        if(Save.getVisibility() == View.VISIBLE) {
                            animation = AnimationUtils.loadAnimation(context, R.anim.slide_down);
                            animation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    animation_started[0] = true;
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    Save.setVisibility(View.INVISIBLE);
                                    Save.setEnabled(false);
                                    Save.clearAnimation();
                                    animation_started[0] = false;
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            animation.setFillAfter(true);
                            Save.startAnimation(animation);
                        }

                    }
                    else if (dy < 0) {
                        if (Save.getVisibility() == View.INVISIBLE || Save.getVisibility() == View.GONE && !animation_started[0]) {
                            animation = AnimationUtils.loadAnimation(context, R.anim.slide_up);
                            animation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    Save.setVisibility(View.VISIBLE);
                                    animation_started[0] = true;
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    Save.clearAnimation();
                                    Save.setEnabled(true);
                                    animation_started[0] = false;
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            animation.setFillAfter(true);
                            Save.startAnimation(animation);
                        }

                    }
                }
            }
        });
    }
}
