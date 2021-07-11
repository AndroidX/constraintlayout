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

import java.awt.Component;
import java.awt.Graphics;
import java.io.InputStream;
import java.net.URL;
import javax.swing.Icon;

public class VDIcon implements Icon {

  private VDTree mAPath;
  public VDIcon(InputStream is) {
    VDParser p = new VDParser();
    try {
      mAPath = p.parse(is);
    } catch (Exception e) {

      e.printStackTrace();
      System.exit(2);
    }
  }

  public VDIcon(URL url) {
    VDParser p = new VDParser();
    try {
      mAPath = p.parse(url);
    } catch (Exception e) {
      System.err.println("unable to parse \""+url+"\"" );
      e.printStackTrace();
      System.exit(2);
    }
  }

  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    if (mAPath != null) {
      mAPath.draw(g, c);
    }
  }

  @Override
  public int getIconWidth() {
    return mAPath.getWidth();
  }

  @Override
  public int getIconHeight() {
    return mAPath.getHeight();
  }
}
