package androidx.constraintlayout.motion.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.constraintlayout.motion.utils.Easing;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.constraintlayout.widget.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class ViewTransition {
    private static String TAG = "ViewTransition";
    ConstraintSet set;
    public static final String VIEW_TRANSITION_TAG = "ViewTransition";
    public static final String KEY_FRAME_SET_TAG = "KeyFrameSet";
    public static final String CONSTRAINT_OVERRIDE = "ConstraintOverride";
    private static final int UNSET = -1;
    private int mId;
    // Transition can be up or down of manually fired
    private final int ONSTATETRANSITION_ACTION_DOWN = 1;
    private final int ONSTATETRANSITION_ACTION_UP = 2;
    private int mOnStateTransition = UNSET;
    private boolean mTransitionDisable;
    private boolean mPathMotionArc;
    private int mViewTransitionMode;
    private static final int VIEWTRANSITIONMODE_CURRENTSTATE = 0;
    private static final int VIEWTRANSITIONMODE_ALLSTATES = 1;
    private static final int VIEWTRANSITIONMODE_NOSTATE = 2;
    KeyFrames mKeyFrames;
    ConstraintSet.Constraint mConstraintDelta;
    private int mDuration = UNSET;
    private int mTargetId;
    private String mTargetString;

    // interpolator code
    private static final int SPLINE_STRING = -1;
    private static final int INTERPOLATOR_REFRENCE_ID = -2;
    private int mDefaultInterpolator = 0;
    private String mDefaultInterpolatorString = null;
    private int mDefaultInterpolatorID = -1;
    static final int EASE_IN_OUT = 0;
    static final int EASE_IN = 1;
    static final int EASE_OUT = 2;
    static final int LINEAR = 3;
    static final int ANTICIPATE = 4;
    static final int BOUNCE = 5;
    Context mContext;

    public String toString() {
        return "ViewTransition(" + Debug.getName(mContext, mId) + ")";
    }

    public Interpolator getInterpolator(Context context) {
        switch (mDefaultInterpolator) {
            case SPLINE_STRING:
                final Easing easing = Easing.getInterpolator(mDefaultInterpolatorString);
                return new Interpolator() {
                    @Override
                    public float getInterpolation(float v) {
                        return (float) easing.get(v);
                    }
                };
            case INTERPOLATOR_REFRENCE_ID:
                return AnimationUtils.loadInterpolator(context,
                        mDefaultInterpolatorID);
            case EASE_IN_OUT:
                return new AccelerateDecelerateInterpolator();
            case EASE_IN:
                return new AccelerateInterpolator();
            case EASE_OUT:
                return new DecelerateInterpolator();
            case LINEAR:
                return null;
            case ANTICIPATE:
                return new AnticipateInterpolator();
            case BOUNCE:
                return new BounceInterpolator();
        }
        return null;
    }

    public ViewTransition(Context context, XmlPullParser parser) {
        mContext = context;
        String tagName = null;
        try {
            Key key = null;
            for (int eventType = parser.getEventType();
                 eventType != XmlResourceParser.END_DOCUMENT;
                 eventType = parser.next()) {
                switch (eventType) {
                    case XmlResourceParser.START_DOCUMENT:
                        break;
                    case XmlResourceParser.START_TAG:
                        tagName = parser.getName();
                        switch (tagName) {
                            case VIEW_TRANSITION_TAG:
                                parseViewTransitionTags(context, parser);
                                break;
                            case KEY_FRAME_SET_TAG:
                                mKeyFrames = new KeyFrames(context, parser);
                                break;
                            case CONSTRAINT_OVERRIDE:
                                mConstraintDelta = ConstraintSet.buildDelta(context, parser);
                                break;
                        }

                        break;
                    case XmlResourceParser.END_TAG:
                        if (VIEW_TRANSITION_TAG.equals(parser.getName())) {
                            return;
                        }
                        break;
                    case XmlResourceParser.TEXT:
                        break;
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseViewTransitionTags(Context context, XmlPullParser parser) {
        AttributeSet attrs = Xml.asAttributeSet(parser);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewTransition);
        final int count = a.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.ViewTransition_android_id) {
                mId = a.getResourceId(attr, mId);
            } else if (attr == R.styleable.ViewTransition_motionTarget) {
                if (MotionLayout.IS_IN_EDIT_MODE) {
                    mTargetId = a.getResourceId(attr, mTargetId);
                    if (mTargetId == -1) {
                        mTargetString = a.getString(attr);
                    }
                } else {
                    if (a.peekValue(attr).type == TypedValue.TYPE_STRING) {
                        mTargetString = a.getString(attr);
                    } else {
                        mTargetId = a.getResourceId(attr, mTargetId);
                    }
                }
            } else if (attr == R.styleable.ViewTransition_onStateTransition) {
                mOnStateTransition = a.getInt(attr, mOnStateTransition);
            } else if (attr == R.styleable.ViewTransition_transitionDisable) {
                mTransitionDisable = a.getBoolean(attr, mTransitionDisable);
            } else if (attr == R.styleable.ViewTransition_pathMotionArc) {
                mPathMotionArc = a.getBoolean(attr, mPathMotionArc);
            } else if (attr == R.styleable.ViewTransition_duration) {
                mDuration = a.getInt(attr, mDuration);
            } else if (attr == R.styleable.ViewTransition_viewTransitionMode) {
                mViewTransitionMode = a.getInt(attr, mViewTransitionMode);
            } else if (attr == R.styleable.ViewTransition_motionInterpolator) {
                TypedValue type = a.peekValue(attr);
                if (type.type == TypedValue.TYPE_REFERENCE) {
                    mDefaultInterpolatorID = a.getResourceId(attr, -1);
                    if (mDefaultInterpolatorID != UNSET) {
                        mDefaultInterpolator = INTERPOLATOR_REFRENCE_ID;
                    }
                } else if (type.type == TypedValue.TYPE_STRING) {
                    mDefaultInterpolatorString = a.getString(attr);
                    if (mDefaultInterpolatorString.indexOf("/") > 0) {
                        mDefaultInterpolatorID = a.getResourceId(attr, UNSET);
                        mDefaultInterpolator = INTERPOLATOR_REFRENCE_ID;
                    } else {
                        mDefaultInterpolator = SPLINE_STRING;
                    }
                } else {
                    mDefaultInterpolator = a.getInteger(attr, mDefaultInterpolator);
                }
            }
        }
        a.recycle();
    }

    public void applyIndependentTransition(ViewTransitionController controller, MotionLayout motionLayout, int fromId, ConstraintSet current, View view) {
        MotionController motionController = new MotionController(view);
        motionController.setBothStates(view);
        mKeyFrames.addAllFrames(motionController);
        motionController.setup(motionLayout.getWidth(), motionLayout.getHeight(), mDuration, System.nanoTime());
        new Animate(controller, motionController, mDuration);
    }

    static class Animate {
        long mStart;
        MotionController mMC;
        int mDuration;
        KeyCache mCache = new KeyCache();
        ViewTransitionController mVtController;

        Animate(ViewTransitionController controller, MotionController motionController, int duration) {
            mVtController = controller;
            mMC = motionController;
            mDuration = duration;
            mStart = System.nanoTime();
            mVtController.addAnimation(this);
            mutate();
        }

        void mutate() {
            long current = System.nanoTime();
            long elapse = current - mStart;
            float position = ((float) (elapse * 1E-6)) / mDuration;

            if (position > 1) {
                mVtController.removeAnimation(this);
                return;
            }
            boolean repaint = mMC.interpolate(mMC.mView, position, current, mCache);
            if (position < 1f || repaint) {
                mVtController.invalidate();
            }

        }
    }


    public void applyTransition(ViewTransitionController controller,
                                MotionLayout layout,
                                int fromId,
                                ConstraintSet current,
                                View... views) {
        if (mViewTransitionMode == VIEWTRANSITIONMODE_NOSTATE) {
            applyIndependentTransition(controller, layout, fromId, current, views[0]);
            return;
        }
        if (mViewTransitionMode == VIEWTRANSITIONMODE_ALLSTATES) {
            int[] ids = layout.getConstraintSetIds();
            for (int i = 0; i < ids.length; i++) {
                int id = ids[i];
                if (id == fromId) {
                    continue;
                }
                ConstraintSet cset = layout.getConstraintSet(id);
                for (View view : views) {
                    ConstraintSet.Constraint constraint = cset.getConstraint(view.getId());
                    if (mConstraintDelta != null) {
                        mConstraintDelta.applyDelta(constraint);
                    }
                }
            }
        }

        ConstraintSet transformedState = new ConstraintSet();
        transformedState.clone(current);
        for (View view : views) {
            ConstraintSet.Constraint constraint = transformedState.getConstraint(view.getId());
            if (mConstraintDelta != null) {
                mConstraintDelta.applyDelta(constraint);
            }
        }

        layout.updateState(fromId, transformedState);
        layout.updateState(R.id.view_transition, current);
        layout.setState(R.id.view_transition, -1, -1);
        MotionScene.Transition tmpTransition = new MotionScene.Transition(-1, layout.mScene, R.id.view_transition, fromId);
        for (View view : views) {
            updateTransition(tmpTransition, view);
        }
        layout.setTransition(tmpTransition);
        layout.transitionToEnd();
    }

    private void updateTransition(MotionScene.Transition transition, View view) {
        if (mDuration != -1) {
            transition.setDuration(mDuration);
        }
        int id = view.getId();
        if (mKeyFrames != null) {
            ArrayList<Key> keys = mKeyFrames.getKeyFramesForView(KeyFrames.UNSET);
            KeyFrames keyFrames = new KeyFrames();
            for (Key key : keys) {
                keyFrames.addKey(key.clone().setViewId(id));
            }

            transition.addtKeyFrame(keyFrames);
        }
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    boolean matchesView(View view) {
        if (view == null) {
            return false;
        }
        if (mTargetId == -1 && mTargetString == null) {
            return false;
        }

        if (view.getId() == mTargetId) {
            return true;
        }
        if (mTargetString == null) {
            return false;
        }
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof ConstraintLayout.LayoutParams) {
            String tag = ((ConstraintLayout.LayoutParams) (view.getLayoutParams())).constraintTag;
            if (tag != null && tag.matches(mTargetString)) {
                return true;
            }
        }
        return false;
    }

    public boolean supports(int action) {
        if (mOnStateTransition == ONSTATETRANSITION_ACTION_DOWN) {
            return action == MotionEvent.ACTION_DOWN;
        }
        if (mOnStateTransition == ONSTATETRANSITION_ACTION_UP) {
            return action == MotionEvent.ACTION_UP;
        }
        return false;
    }
}
