/*
 * Copyright (C) 2019 The Android Open Source Project
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

package androidx.constraintLayout.desktop.ui.adapters.vg;

import java.awt.geom.Path2D;
import java.util.Arrays;

public class VDPath {
    Node[] mNode = null;
    String mId;
    int mStrokeColor = 0;
    int mFillColor = 0;
    float mStrokeWidth = 0;
    float mRotate = 0;
    float mShiftX = 0;
    float mShiftY = 0;
    public float mRotateX = 0;
    public float mRotateY = 0;
    public float trimPathStart = 0;
    public float trimPathEnd = 1;
    public float trimPathOffset = 0;
    int mStrokelineCap = -1;
    int mStrokelineJoin = -1;
    float mStrokeMiterlimit;
    int mFillType;
    int fill_rule;
    boolean mClip = false;
    float mStrokeOpacity = Float.NaN;
    float mFillOpacity = Float.NaN;
    float mTrimPathStart = 0;
    float mTrimPathEnd = 1;
    float mTrimPathOffset = 0;

    /**
     * @return the name of the path
     */
    public String getName() {
        return mId;
    }

    public void toPath(Path2D path) {
        path.reset();
        if (mNode != null) {
            VDNodeRender.creatPath(mNode, path);
        }
    }

    public static class Node {
        char type;
        float[] params;

        public Node(char type, float[] params) {
            this.type = type;
            this.params = params;
        }

        public Node(Node n) {
            this.type = n.type;
            this.params = Arrays.copyOf(n.params, n.params.length);
        }

        public Node(Node n1, Node n2, float t) {
            this.type = n1.type;
            this.params = new float[n1.params.length];
            interpolate(n1, n2, t);
        }

        boolean match(Node n) {
            if (n.type != type) {
                return false;
            }
            return (params.length == n.params.length);
        }

        public void interpolate(Node n1, Node n2, float t) {
            for (int i = 0; i < n1.params.length; i++) {
                params[i] = n1.params[i] * (1 - t) + n2.params[i] * t;
            }
        }

        public static String NodeListToString(Node[] nodes) {
            String s = "";
            for (int i = 0; i < nodes.length; i++) {
                Node n = nodes[i];
                s += n.type;
                int len = n.params.length;
                for (int j = 0; j < len; j++) {
                    if (j > 0) {
                        s += ((j & 1) == 1) ? "," : " ";
                    }
                    float value = n.params[j];
                    if (value == (long) value) {
                        s += String.valueOf((long) value);
                    } else {
                        s += String.valueOf(value);
                    }

                }
            }
            return s;
        }

        public static void transform(float a,
                float b,
                float c,
                float d,
                float e,
                float f,
                Node[] nodes) {
            float[] pre = new float[2];
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].transform(a, b, c, d, e, f, pre);
            }

        }

        public void transform(float a,
                float b,
                float c,
                float d,
                float e,
                float f,
                float[] pre) {
            int incr = 0;
            float[] tempParams;
            float[] origParams;
            switch (type) {

                case 'z':
                case 'Z':
                    return;
                case 'M':
                case 'L':
                case 'T':
                    incr = 2;
                    pre[0] = params[params.length - 2];
                    pre[1] = params[params.length - 1];
                    for (int i = 0; i < params.length; i += incr) {
                        matrix(a, b, c, d, e, f, i, i + 1);
                    }
                    break;
                case 'm':
                case 'l':
                case 't':
                    incr = 2;
                    pre[0] += params[params.length - 2];
                    pre[1] += params[params.length - 1];
                    for (int i = 0; i < params.length; i += incr) {
                        matrix(a, b, c, d, 0, 0, i, i + 1);
                    }
                    break;
                case 'h':
                    type = 'l';
                    pre[0] += params[params.length - 1];

                    tempParams = new float[params.length * 2];
                    origParams = params;
                    params = tempParams;
                    for (int i = 0; i < params.length; i += 2) {
                        params[i] = origParams[i / 2];
                        params[i + 1] = 0;
                        matrix(a, b, c, d, 0, 0, i, i + 1);
                    }

                    break;
                case 'H':
                    type = 'L';
                    pre[0] = params[params.length - 1];
                    tempParams = new float[params.length * 2];
                    origParams = params;
                    params = tempParams;
                    for (int i = 0; i < params.length; i += 2) {
                        params[i] = origParams[i / 2];
                        params[i + 1] = pre[1];
                        matrix(a, b, c, d, e, f, i, i + 1);
                    }
                    break;
                case 'v':
                    pre[1] += params[params.length - 1];
                    type = 'l';
                    tempParams = new float[params.length * 2];
                    origParams = params;
                    params = tempParams;
                    for (int i = 0; i < params.length; i += 2) {
                        params[i] = 0;
                        params[i + 1] = origParams[i / 2];
                        matrix(a, b, c, d, 0, 0, i, i + 1);
                    }
                    break;
                case 'V':
                    type = 'L';
                    pre[1] = params[params.length - 1];
                    tempParams = new float[params.length * 2];
                    origParams = params;
                    params = tempParams;
                    for (int i = 0; i < params.length; i += 2) {
                        params[i] = pre[0];
                        params[i + 1] = origParams[i / 2];
                        matrix(a, b, c, d, e, f, i, i + 1);
                    }
                    break;
                case 'C':
                case 'S':
                case 'Q':
                    pre[0] = params[params.length - 2];
                    pre[1] = params[params.length - 1];
                    for (int i = 0; i < params.length; i += 2) {
                        matrix(a, b, c, d, e, f, i, i + 1);
                    }
                    break;
                case 's':
                case 'q':
                case 'c':
                    pre[0] += params[params.length - 2];
                    pre[1] += params[params.length - 1];
                    for (int i = 0; i < params.length; i += 2) {
                        matrix(a, b, c, d, 0, 0, i, i + 1);
                    }
                    break;
                case 'a':
                    incr = 7;
                    pre[0] += params[params.length - 2];
                    pre[1] += params[params.length - 1];
                    for (int i = 0; i < params.length; i += incr) {
                        matrix(a, b, c, d, 0, 0, i, i + 1);
                        double ang = Math.toRadians(params[i + 2]);
                        params[i + 2] = (float) Math.toDegrees(ang + Math.atan2(b, d));
                        matrix(a, b, c, d, 0, 0, i + 5, i + 6);
                    }
                    break;
                case 'A':
                    incr = 7;
                    pre[0] = params[params.length - 2];
                    pre[1] = params[params.length - 1];
                    for (int i = 0; i < params.length; i += incr) {
                        matrix(a, b, c, d, e, f, i, i + 1);
                        double ang = Math.toRadians(params[i + 2]);
                        params[i + 2] = (float) Math.toDegrees(ang + Math.atan2(b, d));
                        matrix(a, b, c, d, e, f, i + 5, i + 6);
                    }
                    break;

            }
        }

        void matrix(float a,
                float b,
                float c,
                float d,
                float e,
                float f,
                int offx,
                int offy) {
            float inx = (offx < 0) ? 1 : params[offx];
            float iny = (offy < 0) ? 1 : params[offy];
            float x = inx * a + iny * c + e;
            float y = inx * b + iny * d + f;
            if (offx >= 0) {
                params[offx] = x;
            }
            if (offy >= 0) {
                params[offy] = y;
            }
        }
    }

    public VDPath() {
        mId = this.toString(); // to ensure paths have unique names
    }

    public VDPath(VDPath p) {
        copyFrom(p);
    }

    public void copyFrom(VDPath p1) {
        mNode = new Node[p1.mNode.length];
        for (int i = 0; i < mNode.length; i++) {
            mNode[i] = new Node(p1.mNode[i]);
        }
        mId = p1.mId;
        mStrokeColor = p1.mStrokeColor;
        mFillColor = p1.mFillColor;
        mStrokeWidth = p1.mStrokeWidth;
        mRotate = p1.mRotate;
        mShiftX = p1.mShiftX;
        mShiftY = p1.mShiftY;
        mRotateX = p1.mRotateX;
        mRotateY = p1.mRotateY;
        trimPathStart = p1.trimPathStart;
        trimPathEnd = p1.trimPathEnd;
        trimPathOffset = p1.trimPathOffset;
        mStrokelineCap = p1.mStrokelineCap;
        mFillType = p1.mFillType;
        mStrokelineJoin = p1.mStrokelineJoin;
        mStrokeMiterlimit = p1.mStrokeMiterlimit;
        fill_rule = p1.fill_rule;

        mClip = p1.mClip;
        mStrokeOpacity = p1.mStrokeOpacity;
        mFillOpacity = p1.mFillOpacity;
        mTrimPathStart = p1.mTrimPathStart;
        mTrimPathEnd = p1.mTrimPathEnd;
        mTrimPathOffset = p1.mTrimPathOffset;
    }

    public static VDPath interpolate(float t, VDPath p1, VDPath p2, VDPath mReturnPath) {

        if (mReturnPath.mNode == null || mReturnPath.mNode.length != p1.mNode.length) {
            mReturnPath.mNode = new Node[p1.mNode.length];
        }
        for (int i = 0; i < mReturnPath.mNode.length; i++) {
            if (mReturnPath.mNode[i] == null) {
                mReturnPath.mNode[i] = new Node(p1.mNode[i], p2.mNode[i], t);
            } else {
                mReturnPath.mNode[i].interpolate(p1.mNode[i], p2.mNode[i], t);
            }
        }
        float t1 = 1 - t;
        mReturnPath.mShiftX = t1 * p1.mShiftX + t * p2.mShiftX;
        mReturnPath.mShiftX = t1 * p1.mShiftX + t * p2.mShiftX;
        mReturnPath.mRotate = t1 * p1.mRotate + t * p2.mRotate;
        mReturnPath.mRotateX = t1 * p1.mRotateX + t * p2.mRotateX;
        mReturnPath.mRotateY = t1 * p1.mRotateY + t * p2.mRotateY;

        mReturnPath.trimPathStart = t1 * p1.trimPathStart + t * p2.trimPathStart;
        mReturnPath.trimPathEnd = t1 * p1.trimPathEnd + t * p2.trimPathEnd;
        mReturnPath.trimPathOffset = t1 * p1.trimPathOffset + t * p2.trimPathOffset;
        mReturnPath.mStrokeMiterlimit = t1 * p1.mStrokeMiterlimit + t * p2.mStrokeMiterlimit;
        mReturnPath.mStrokelineCap = p1.mStrokelineCap;
        if (mReturnPath.mStrokelineCap == -1) {
            mReturnPath.mStrokelineCap = p2.mStrokelineCap;
        }
        mReturnPath.mFillType = p1.mFillType;
        mReturnPath.mStrokelineJoin = p1.mStrokelineJoin;
        if (mReturnPath.mStrokelineJoin == -1) {
            mReturnPath.mStrokelineJoin = p2.mStrokelineJoin;
        }
        mReturnPath.fill_rule = p1.fill_rule;

        mReturnPath.mStrokeColor = rgbInterpolate(t, p1.mStrokeColor, p2.mStrokeColor);
        mReturnPath.mFillColor = rgbInterpolate(t, p1.mFillColor, p2.mFillColor);
        mReturnPath.mStrokeWidth = t1 * p1.mStrokeWidth + t * p2.mStrokeWidth;
        return mReturnPath;
    }

    private static int rgbInterpolate(float t, int color1, int color2) {
        int ret;
        if (color1 == color2) {
            return color2;
        }
        if (color1 == 0) {
            return color2;
        }
        if (color2 == 0) {
            return color1;
        }

        float t1 = 1 - t;
        ret = 0xFF & (((int) ((color1 & 0xFF) * t1 + (color2 & 0xFF) * t)));
        color1 >>= 8;
        color2 >>= 8;

        ret |= 0xFF00 & (((int) ((color1 & 0xFF) * t1 + (color2 & 0xFF) * t)) << 8);
        color1 >>= 8;
        color2 >>= 8;
        ret |= 0xFF0000 & (((int) ((color1 & 0xFF) * t1 + (color2 & 0xFF) * t)) << 16);
        color1 >>= 8;
        color2 >>= 8;
        ret |= 0xFF000000 & (((int) ((color1 & 0xFF) * t1 + (color2 & 0xFF) * t)) << 24);

        return ret;
    }

    public boolean isVisible(Object mCurrentState) {
        return true;
    }

    /**
     * Does not support rotation attribute
     */
    public void transform(float a, float b, float c, float d, float e, float f) {
        mStrokeWidth *= Math.hypot(a + b, c + d);
        Node.transform(a, b, c, d, e, f, mNode);

    }
}
