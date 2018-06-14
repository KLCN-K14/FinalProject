package com.klcn.xuant.transporter.helper;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

import com.github.lzyzsd.circleprogress.ArcProgress;

public class ArcProgressAnimation extends Animation {
    private ArcProgress arcProgress;
    private float from;
    private float  to;

    public ArcProgressAnimation(ArcProgress arcProgress, float from, float to) {
        super();
        this.arcProgress = arcProgress;
        this.from = from;
        this.to = to;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        arcProgress.setProgress((int) value);
    }

}
