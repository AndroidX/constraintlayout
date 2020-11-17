/*
 * Copyright (C) 2020 The Android Open Source Project
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

import android.graphics.Rect;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintSet;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Container for ViewTransitions. It dispatches the run of a ViewTransition.
 * It receives animate calls
 */
public class ViewTransitionController {
    private final MotionLayout mMotionLayout;
    private ArrayList<ViewTransition> viewTransitions = new ArrayList<>();
    private HashSet<View> mRelatedViews;
    private String TAG = "ViewTransitionController";

    public ViewTransitionController(MotionLayout layout) {
        mMotionLayout = layout;
    }

    public void add(ViewTransition viewTransition) {
        viewTransitions.add(viewTransition);
        mRelatedViews = null;
    }

    void remove(int id) {
        ViewTransition del = null;
        for (ViewTransition viewTransition : viewTransitions) {
            if (viewTransition.getId() == id) {
                del = viewTransition;
                break;
            }
        }
        if (del != null) {
            mRelatedViews = null;
            viewTransitions.remove(del);
        }
    }

    private void viewTransition(ViewTransition vt, View... view) {
        int currentId = mMotionLayout.getCurrentState();
        if (currentId == -1) {
            Log.w(TAG, "Dont support transition within transition yet");
            return;
        }
        ConstraintSet current = mMotionLayout.getConstraintSet(currentId);
        if (current == null) {
            return;
        }
        vt.applyTransition(this, mMotionLayout, currentId, current, view);

    }

    void enableViewTransition(int id, boolean enable) {
        for (ViewTransition viewTransition : viewTransitions) {
            if (viewTransition.getId() == id) {
                viewTransition.setEnable(enable);
                break;
            }
        }
    }

    boolean isViewTransitionEnabled(int id) {
        for (ViewTransition viewTransition : viewTransitions) {
            if (viewTransition.getId() == id) {
                return viewTransition.isEnabled();
            }
        }
        return false;
    }

    /**
     * Support call from MotionLayout.viewTransition
     *
     * @param id    the id of a ViewTransition
     * @param views the list of views to transition simultaneously
     */
    void viewTransition(int id, View... views) {
        ViewTransition vt = null;
        ArrayList<View> list = new ArrayList<>();
        for (ViewTransition viewTransition : viewTransitions) {
            if (viewTransition.getId() == id) {
                vt = viewTransition;
                for (View view : views) {
                    if (viewTransition.checkTags(view)) {
                        list.add(view);
                    }
                }
                if (!list.isEmpty()) {
                    viewTransition(vt, list.toArray(new View[0]));
                    list.clear();
                }
            }
        }
        if (vt == null) {
            Log.e(TAG, " Could not find ViewTransition");
            return;
        }
    }

    /**
     * this gets Touch events on the MotionLayout and can fire transitions on down or up
     *
     * @param event
     */
    void touchEvent(MotionEvent event) {
        int currentId = mMotionLayout.getCurrentState();
        if (currentId == -1) {
            return;
        }
        if (mRelatedViews == null) {
            mRelatedViews = new HashSet<>();
            for (ViewTransition viewTransition : viewTransitions) {
                int count = mMotionLayout.getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = mMotionLayout.getChildAt(i);
                    if (viewTransition.matchesView(view)) {
                        int id = view.getId();

                        mRelatedViews.add(view);
                    }
                }
            }
        }

        float x = event.getX();
        float y = event.getY();
        Rect rec = new Rect();
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_DOWN:

                ConstraintSet current = mMotionLayout.getConstraintSet(currentId);
                for (ViewTransition viewTransition : viewTransitions) {
                    if (viewTransition.supports(action)) {
                        for (View view : mRelatedViews) {
                            if (!viewTransition.matchesView(view)) {
                                continue;
                            }
                            view.getHitRect(rec);
                            if (rec.contains((int) x, (int) y)) {
                                viewTransition.applyTransition(this, mMotionLayout, currentId, current, view);
                            }

                        }
                    }
                }
                break;
        }
    }

    ArrayList<ViewTransition.Animate> animations;
    ArrayList<ViewTransition.Animate> removeList = new ArrayList<>();

    void addAnimation(ViewTransition.Animate animation) {
        if (animations == null) {
            animations = new ArrayList<>();
        }
        animations.add(animation);
    }

    void removeAnimation(ViewTransition.Animate animation) {
        removeList.add(animation);
    }

    /**
     * Called by motionLayout during draw to allow ViewTransitions to asynchronously animate
     */
    void animate() {
        if (animations == null) {
            return;
        }
        for (ViewTransition.Animate animation : animations) {
            animation.mutate();
        }
        animations.removeAll(removeList);
        removeList.clear();
        if (animations.isEmpty()) {
            animations = null;
        }
    }

    void invalidate() {
        mMotionLayout.invalidate();
    }
}
