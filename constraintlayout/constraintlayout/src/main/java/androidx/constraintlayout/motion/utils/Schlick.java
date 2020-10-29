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

package androidx.constraintlayout.motion.utils;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * This class translates the a series of floating point values into a continuous
 * curve for use in an easing function including quantize functions
 * it is used with the "spline(0,0.3,0.3,0.5,...0.9,1)" it should start at 0 and end at one 1
 */
public class Schlick extends Easing {
    private final static boolean DEBUG = false;
    double mS, mT;
    double eps;

    Schlick(String configString) {
        // done this way for efficiency

        str = configString;
        int start = configString.indexOf('(');
        int off1 = configString.indexOf(',', start);
        mS = Double.parseDouble(configString.substring(start + 1, off1).trim());
        int off2 = configString.indexOf(',', off1 + 1);
        mT = Double.parseDouble(configString.substring(off1 + 1, off2).trim());
    }

    private double func(double x) {
        if (x < mT) {
            return mT * x / (x + mS * (mT - x));
        }
        return ((1 - mT) * (x - 1)) / (1 - x - mS * (mT - x));
    }

    private double dfunc(double x) {
        if (x < mT) {
            return (mS * mT * mT) / ((mS * (mT - x) + x) * (mS * (mT - x) + x));
        }
        return (mS * (mT - 1) * (mT - 1)) / ((-mS * (mT - x) - x + 1) * (-mS * (mT - x) - x + 1));
    }

    public double getDiff(double x) {
        return dfunc(x);
    }

    public double get(double x) {
        return func(x);
    }
}
