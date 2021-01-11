/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.constraintlayout.motion.widget;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintAttribute;
import androidx.constraintlayout.motion.utils.CurveFit;

import android.util.AttributeSet;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Base class in an element in a KeyFrame
 *
 * @hide
 */

public abstract class Key {
    public static int UNSET = -1;
    int mFramePosition = UNSET;
    int mTargetId = UNSET;
    @Nullable
    String mTargetString = null;
    protected int mType;

    abstract void load(@NonNull Context context, @Nullable AttributeSet attrs);

    @NonNull
    HashMap<String, ConstraintAttribute> mCustomConstraints;

    abstract void getAttributeNames(@NonNull HashSet<String> attributes);

    public static final String ALPHA = "alpha";
    public static final String ELEVATION = "elevation";
    public static final String ROTATION = "rotation";
    public static final String ROTATION_X = "rotationX";
    public static final String ROTATION_Y = "rotationY";
    public static final String PIVOT_X = "transformPivotX";
    public static final String PIVOT_Y = "transformPivotY";
    public static final String TRANSITION_PATH_ROTATE = "transitionPathRotate";
    public static final String SCALE_X = "scaleX";
    public static final String SCALE_Y = "scaleY";
    public static final String WAVE_PERIOD = "wavePeriod";
    public static final String WAVE_OFFSET = "waveOffset";
    public static final String WAVE_PHASE = "wavePhase";
    public static final String WAVE_VARIES_BY = "waveVariesBy";
    public static final String TRANSLATION_X = "translationX";
    public static final String TRANSLATION_Y = "translationY";
    public static final String TRANSLATION_Z = "translationZ";
    public static final String PROGRESS = "progress";
    public static final String CUSTOM = "CUSTOM";
    public static final String CURVEFIT = "curveFit";
    public static final String MOTIONPROGRESS = "motionProgress";
    public static final String TRANSITIONEASING = "transitionEasing";
    public static final String VISIBILITY = "visibility";

    boolean matches(@Nullable String constraintTag) {
        if (mTargetString == null || constraintTag == null) return false;
        return constraintTag.matches(mTargetString);
    }

    /**
     * Defines method to add a a view to splines derived form this key frame.
     * The values are written to the spline
     *
     * @param splines splines to write values to
     * @hide
     */
    public abstract void addValues(@NonNull HashMap<String, SplineSet> splines);

    /**
     * Set the value associated with this tag
     *
     * @param tag
     * @param value
     * @hide
     */
    public abstract void setValue(@NonNull String tag, @NonNull Object value);

    /**
     * Return the float given a value. If the value is a "Float" object it is casted
     *
     * @param value
     * @return
     * @hide
     */
    float toFloat(@NonNull Object value) {
        return (value instanceof Float) ? (Float) value : Float.parseFloat(value.toString());
    }

    /**
     * Return the int version of an object if the value is an Integer object it is casted.
     *
     * @param value
     * @return
     * @hide
     */
    int toInt(@NonNull Object value) {
        return (value instanceof Integer) ? (Integer) value : Integer.parseInt(value.toString());
    }

    /**
     * Return the boolean version this object if the object is a Boolean it is casted.
     *
     * @param value
     * @return
     * @hide
     */
    boolean toBoolean(@NonNull Object value) {
        return (value instanceof Boolean) ? (Boolean) value : Boolean.parseBoolean(value.toString());
    }

    /**
     * Key frame can speify the type of interpolation it wants on various attributes
     * For each string it set it to -1, CurveFit.LINEAR or  CurveFit.SPLINE
     *
     * @param interpolation
     */
    public void setInterpolation(@NonNull HashMap<String, Integer> interpolation) {
    }

    @NonNull
    public Key copy(@NonNull Key src) {
        mFramePosition = src.mFramePosition;
        mTargetId = src.mTargetId;
        mTargetString = src.mTargetString;
        mType = src.mType;
        mCustomConstraints = src.mCustomConstraints;
        return this;
    }

    @NonNull
    abstract public Key clone();

    @NonNull
    public Key setViewId(int id) {
        mTargetId = id;
        return this;
    }

    /**
     * sets the frame position
     *
     * @param pos
     */
    public void setFramePosition(int pos) {
        mFramePosition = pos;
    }

    /**
     * Gets the current frame position
     *
     * @return
     */
    public int getFramePosition() {
        return mFramePosition;
    }

}
