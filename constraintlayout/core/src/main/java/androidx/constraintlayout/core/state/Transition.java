/*
 * Copyright (C) 2021 The Android Open Source Project
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

package androidx.constraintlayout.core.state;

import androidx.constraintlayout.core.motion.Motion;
import androidx.constraintlayout.core.motion.MotionWidget;
import androidx.constraintlayout.core.motion.key.MotionKeyAttributes;
import androidx.constraintlayout.core.motion.key.MotionKeyCycle;
import androidx.constraintlayout.core.motion.key.MotionKeyPosition;
import androidx.constraintlayout.core.motion.utils.KeyCache;
import androidx.constraintlayout.core.motion.utils.TypedBundle;
import androidx.constraintlayout.core.motion.utils.TypedValues;
import androidx.constraintlayout.core.widgets.ConstraintWidget;
import androidx.constraintlayout.core.widgets.ConstraintWidgetContainer;

import java.util.ArrayList;
import java.util.HashMap;

public class Transition {
    HashMap<String, WidgetState> state = new HashMap<>();
    HashMap<Integer, HashMap<String, KeyPosition>> keyPositions = new HashMap<>();

    public final static int START = 0;
    public final static int END = 1;
    public final static int INTERPOLATED = 2;

    private int pathMotionArc = -1;

    public KeyPosition findPreviousPosition(String target, int frameNumber) {
        while (frameNumber >= 0) {
            HashMap<String, KeyPosition> map = keyPositions.get(frameNumber);
            if (map != null) {
                KeyPosition keyPosition = map.get(target);
                if (keyPosition != null) {
                    return keyPosition;
                }
            }
            frameNumber--;
        }
        return null;
    }

    public KeyPosition findNextPosition(String target, int frameNumber) {
        while (frameNumber <= 100) {
            HashMap<String, KeyPosition> map = keyPositions.get(frameNumber);
            if (map != null) {
                KeyPosition keyPosition = map.get(target);
                if (keyPosition != null) {
                    return keyPosition;
                }
            }
            frameNumber++;
        }
        return null;
    }

    public int getNumberKeyPositions(WidgetFrame frame) {
        int numKeyPositions = 0;
        int frameNumber = 0;
        while (frameNumber <= 100) {
            HashMap<String, KeyPosition> map = keyPositions.get(frameNumber);
            if (map != null) {
                KeyPosition keyPosition = map.get(frame.widget.stringId);
                if (keyPosition != null) {
                    numKeyPositions++;
                }
            }
            frameNumber++;
        }
        return numKeyPositions;
    }

    public Motion getMotion(String id) {
        return getWidgetState(id, null, 0).motionControl;
    }

    public void fillKeyPositions(WidgetFrame frame, float[] x, float[] y, float[] pos) {
        int numKeyPositions = 0;
        int frameNumber = 0;
        while (frameNumber <= 100) {
            HashMap<String, KeyPosition> map = keyPositions.get(frameNumber);
            if (map != null) {
                KeyPosition keyPosition = map.get(frame.widget.stringId);
                if (keyPosition != null) {
                    x[numKeyPositions] = keyPosition.x;
                    y[numKeyPositions] = keyPosition.y;
                    pos[numKeyPositions] = keyPosition.frame;
                    numKeyPositions++;
                }
            }
            frameNumber++;
        }
    }

    public boolean hasPositionKeyframes() {
        return keyPositions.size() > 0;
    }

    public void setTransitionProperties(TypedBundle bundle) {
        pathMotionArc = bundle.getInteger(TypedValues.Position.TYPE_PATH_MOTION_ARC);
    }

    static class WidgetState {
        WidgetFrame start;
        WidgetFrame end;
        WidgetFrame interpolated;
        Motion motionControl;
        MotionWidget motionWidgetStart;
        MotionWidget motionWidgetEnd;
        MotionWidget motionWidgetInterpolated;
        KeyCache myKeyCache = new KeyCache();
        int myParentHeight = -1;
        int myParentWidth = -1;

        public WidgetState() {
            start = new WidgetFrame();
            end = new WidgetFrame();
            interpolated = new WidgetFrame();
            motionWidgetStart = new MotionWidget(start);
            motionWidgetEnd = new MotionWidget(end);
            motionWidgetInterpolated = new MotionWidget(interpolated);
            motionControl = new Motion(motionWidgetStart);
            motionControl.setStart(motionWidgetStart);
            motionControl.setEnd(motionWidgetEnd);
        }

        public void setKeyPosition(TypedBundle prop) {
            MotionKeyPosition keyPosition = new MotionKeyPosition();
            prop.applyDelta(keyPosition);
            motionControl.addKey(keyPosition);
        }

        public void setKeyAttribute(TypedBundle prop) {
            MotionKeyAttributes keyAttributes = new MotionKeyAttributes();
            prop.applyDelta(keyAttributes);
            motionControl.addKey(keyAttributes);
        }

        public void setKeyCycle(TypedBundle prop) {
            MotionKeyCycle keyAttributes = new MotionKeyCycle();
            prop.applyDelta(keyAttributes);
            keyAttributes.printAttributes();
            motionControl.addKey(keyAttributes);
        }

        public void update(ConstraintWidget child, int state) {
            if (state == START) {
                start.update(child);
                motionControl.setStart(motionWidgetStart);
            } else if (state == END) {
                end.update(child);
                motionControl.setEnd(motionWidgetEnd);
            }
            myParentWidth = -1;
        }

        public WidgetFrame getFrame(int type) {
            if (type == START) {
                return start;
            } else if (type == END) {
                return end;
            }
            return interpolated;
        }

        public void interpolate(int parentWidth, int parentHeight, float progress, Transition transition) {
            if (true || parentHeight != myParentHeight || parentWidth != myParentWidth) {
                myParentHeight = parentHeight;
                myParentWidth = parentWidth;
                motionControl.setup(parentWidth, parentHeight, 1, System.nanoTime());
            }
            WidgetFrame.interpolate(parentWidth, parentHeight, interpolated, start, end, transition, progress);
            motionControl.interpolate(motionWidgetInterpolated, progress, System.nanoTime(), myKeyCache);
        }
    }

    static class KeyPosition {
        int frame;
        String target;
        int type;
        float x;
        float y;

        public KeyPosition(String target, int frame, int type, float x, float y) {
            this.target = target;
            this.frame = frame;
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    public boolean isEmpty() {
        return state.isEmpty();
    }

    public void clear() {
        state.clear();
    }

    public boolean contains(String key) {
        return state.containsKey(key);
    }

    public void addKeyPosition(String target, TypedBundle bundle) {
        getWidgetState(target, null, 0).setKeyPosition(bundle);
    }

    public void addKeyAttribute(String target, TypedBundle bundle) {
        getWidgetState(target, null, 0).setKeyAttribute(bundle);
    }

    public void addKeyCycle(String target, TypedBundle bundle) {
        getWidgetState(target, null, 0).setKeyCycle(bundle);
    }

    public void addKeyPosition(String target, int frame, int type, float x, float y) {
        TypedBundle bundle = new TypedBundle();
        bundle.add(TypedValues.Position.TYPE_POSITION_TYPE, 2);
        bundle.add(TypedValues.TYPE_FRAME_POSITION, frame);
        bundle.add(TypedValues.Position.TYPE_PERCENT_X, x);
        bundle.add(TypedValues.Position.TYPE_PERCENT_Y, y);
        getWidgetState(target, null, 0).setKeyPosition(bundle);

        KeyPosition keyPosition = new KeyPosition(target, frame, type, x, y);
        HashMap<String, KeyPosition> map = keyPositions.get(frame);
        if (map == null) {
            map = new HashMap<>();
            keyPositions.put(frame, map);
        }
        map.put(target, keyPosition);
    }

    public void addCustomFloat(int state, String widgetId, String property, float value) {
        WidgetState widgetState = getWidgetState(widgetId, null, state);
        WidgetFrame frame = widgetState.getFrame(state);
        frame.addCustomFloat(property, value);
    }

    public void addCustomColor(int state, String widgetId, String property, int color) {
        WidgetState widgetState = getWidgetState(widgetId, null, state);
        WidgetFrame frame = widgetState.getFrame(state);
        frame.addCustomColor(property, color);
    }

    public void updateFrom(ConstraintWidgetContainer container, int state) {
        final ArrayList<ConstraintWidget> children = container.getChildren();
        final int count = children.size();
        for (int i = 0; i < count; i++) {
            ConstraintWidget child = children.get(i);
            WidgetState widgetState = getWidgetState(child.stringId, null, state);
            widgetState.update(child, state);
        }
    }

    public void interpolate(int parentWidth, int parentHeight, float progress) {
        for (String key : state.keySet()) {
            WidgetState widget = state.get(key);
            widget.interpolate(parentWidth, parentHeight, progress, this);
        }
    }

    public WidgetFrame getStart(String id) {
        WidgetState widgetState = state.get(id);
        if (widgetState == null) {
            return null;
        }
        return widgetState.start;
    }

    public WidgetFrame getEnd(String id) {
        WidgetState widgetState = state.get(id);
        if (widgetState == null) {
            return null;
        }
        return widgetState.end;
    }

    public WidgetFrame getInterpolated(String id) {
        WidgetState widgetState = state.get(id);
        if (widgetState == null) {
            return null;
        }
        return widgetState.interpolated;
    }

    public float[] getPath(String id) {
        WidgetState widgetState = state.get(id);
        int duration = 1000;
        int frames = duration / 16;
        float[] mPoints = new float[frames * 2];
        widgetState.motionControl.buildPath(mPoints, frames);
        return mPoints;
    }

    public int getKeyFrames(String id, float[] rectangles, int[] pathMode, int[] position) {
        WidgetState widgetState = state.get(id);
        return widgetState.motionControl.buildKeyFrames(rectangles, pathMode, position);
    }

    private WidgetState getWidgetState(String widgetId, ConstraintWidget child, int transitionState) {
        WidgetState widgetState = this.state.get(widgetId);
        if (widgetState == null) {
            widgetState = new WidgetState();
            if (pathMotionArc != -1) {
                widgetState.motionControl.setPathMotionArc(pathMotionArc);
            }
            state.put(widgetId, widgetState);
            if (child != null) {
                widgetState.update(child, transitionState);
            }
        }
        return widgetState;
    }

    /**
     * Used in debug draw
     *
     * @param child
     * @return
     */
    public WidgetFrame getStart(ConstraintWidget child) {
        return getWidgetState(child.stringId, null, Transition.START).start;
    }

    /**
     * Used in debug draw
     *
     * @param child
     * @return
     */
    public WidgetFrame getEnd(ConstraintWidget child) {
        return getWidgetState(child.stringId, null, Transition.END).end;
    }

    /**
     * Used after the interpolation
     *
     * @param child
     * @return
     */
    public WidgetFrame getInterpolated(ConstraintWidget child) {
        return getWidgetState(child.stringId, null, Transition.INTERPOLATED).interpolated;
    }
}
