package com.sorashiro.calculator.util;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * @author Sora
 * @date 2016/11/16
 *
 * A util class about animation.
 * 动画相关处理类
 *
 */

public class AnimationUtil {

    public static void backgroundToGrey(View view) {
        ObjectAnimator animator = ObjectAnimator.ofInt(
                view, "backgroundColor",
                0xffffffff, 0xffbdbdbd);
        animator.setDuration(1);
        animator.start();
    }

    public static void backgroundToWhite(View view) {
        ObjectAnimator animator = ObjectAnimator.ofInt(
                view, "backgroundColor",
                0xffbdbdbd, 0xffffffff);
        animator.setDuration(1);
        animator.start();
    }

    public static void twinkle(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 1, 0.2f, 1);
        animator.setDuration(300);
        animator.start();
    }


}
