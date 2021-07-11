/*
 * Copyright 2021 The Android Open Source Project
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

package androidx.constraintLayout.desktop.scan;

import androidx.constraintlayout.core.parser.CLArray;
import androidx.constraintlayout.core.parser.CLKey;
import androidx.constraintlayout.core.parser.CLParsingException;

import java.awt.*;

public class KeyFrameNodes {
    int mCount = 0;
    float[] mX;
    float[] mY;
    int[] mMode;
    int[] mProgress;

    public void setKeyFramesPos(CLKey key) {

        CLArray array = ((CLArray) key.getValue());

        int size = array.size();
        mCount = size / 2;
        mX = new float[mCount];
        mY = new float[mCount];

        for (int i = 0; i < mCount; i++) {
            try {
                mX[i] = array.get(i * 2).getFloat();
                mY[i] = array.get(i * 2 + 1).getFloat();

            } catch (CLParsingException e) {
                e.printStackTrace();
            }
        }
    }

    public void setKeyFramesTypes(CLKey key) {

        CLArray array = ((CLArray) key.getValue());

        int size = array.size();
        mCount = size;
        mMode = new int[size];
        for (int i = 0; i < size; i++) {
            try {
                mMode[i] = array.get(i).getInt();

            } catch (CLParsingException e) {
                e.printStackTrace();
            }
        }
    }

    public void setKeyFramesProgress(CLKey key) {

        CLArray array = ((CLArray) key.getValue());

        int size = array.size();
        mCount = size;
        mProgress = new int[size];
        for (int i = 0; i < size; i++) {
            try {
                mProgress[i] = array.get(i).getInt();

            } catch (CLParsingException e) {
                e.printStackTrace();
            }
        }
    }

    public void render(Graphics2D g2d) {
        if (mX == null) {
            return;
        }

        int rad = 5;
        int diameter = rad * 2;
        for (int i = 1; i < mX.length - 1; i++) {
            int x = (int) (0.5 + mX[i]);
            int y = (int) (0.5 + mY[i]);

            g2d.drawString(Integer.toString(mProgress[i - 1]), x + 2, y - 2);

            g2d.rotate(45, x, y);
            g2d.fillRect(x - rad, y - rad, diameter, diameter);
            g2d.rotate(-45, x, y);

        }
        {
            int x = (int) (0.5 + mX[0]);
            int y = (int) (0.5 + mY[0]);
            g2d.rotate(45, x, y);
            g2d.drawRect(x - rad, y - rad, diameter, diameter);
            g2d.rotate(-45, x, y);
        }
        {
            int x = (int) (0.5 + mX[mX.length - 1]);
            int y = (int) (0.5 + mY[mX.length - 1]);
            g2d.rotate(45, x, y);
            g2d.drawRect(x - rad, y - rad, diameter, diameter);
            g2d.rotate(-45, x, y);
        }
    }
}

