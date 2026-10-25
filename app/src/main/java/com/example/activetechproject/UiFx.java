package com.example.activetechproject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

/** Small helper that adds entrance animations and press feedback to a screen. */
public final class UiFx {

    private static final long STEP_MS = 55;
    private static final int MAX_STAGGER = 10;

    private UiFx() {}

    /** Call right after setContentView. */
    public static void setup(Activity activity) {
        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) return;
        View root = content.getChildAt(0);

        List<View> blocks = new ArrayList<>();
        collect(root, root, blocks);
        for (int i = 0; i < blocks.size(); i++) {
            enter(blocks.get(i), Math.min(i, MAX_STAGGER) * STEP_MS);
        }
        addPressEffects(root);
    }

    /** Fade + slide up. */
    public static void enter(View v, long delay) {
        float dist = 28 * v.getResources().getDisplayMetrics().density;
        v.setAlpha(0f);
        v.setTranslationY(dist);
        v.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(420)
                .setInterpolator(new DecelerateInterpolator(1.6f))
                .start();
    }

    /** Scale down on press with a light haptic tick, springs back on release. */
    @SuppressLint("ClickableViewAccessibility")
    public static void press(View v) {
        v.setOnTouchListener((view, e) -> {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(110)
                            .setInterpolator(new DecelerateInterpolator()).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.animate().scaleX(1f).scaleY(1f).setDuration(260)
                            .setInterpolator(new OvershootInterpolator(2.2f)).start();
                    break;
                default:
                    break;
            }
            return false; // never consume: clicks keep working
        });
    }

    private static void addPressEffects(View v) {
        if (v instanceof RecyclerView || v instanceof EditText || v instanceof BottomNavigationView) return;
        boolean isButton = v instanceof Button || v instanceof ImageButton;
        if (isButton) {
            View target = v;
            View parent = (View) v.getParent();
            if (v instanceof ImageButton && parent instanceof FrameLayout
                    && ((FrameLayout) parent).getChildCount() > 1) {
                target = parent; // card: scale the whole card, not only its background
            }
            press(target);
            return;
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) addPressEffects(g.getChildAt(i));
        }
    }

    /** Picks the views that animate in: styled containers as one unit, otherwise leaf views. */
    private static void collect(View root, View v, List<View> out) {
        if (v == null || v.getVisibility() != View.VISIBLE) return;
        if (v instanceof RecyclerView || v instanceof BottomNavigationView) return;
        if (v != root && v instanceof ViewGroup && v.getBackground() != null) {
            out.add(v);
            return;
        }
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) collect(root, g.getChildAt(i), out);
        } else if (!(v instanceof android.widget.ProgressBar)) {
            out.add(v);
        }
    }
}
