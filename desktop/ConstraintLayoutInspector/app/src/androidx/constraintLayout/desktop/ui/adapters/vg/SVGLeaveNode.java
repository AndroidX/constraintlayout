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

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SVGLeaveNode {

  private static Logger logger = Logger.getLogger(SVGLeaveNode.class.getSimpleName());

  private String mName;

  public String d;
  public float rectW = -1;
  public float rectH = -1;
  public HashMap<String, String> prop = new HashMap<String, String>();

  public SVGLeaveNode() {
    mName = "No Name";
  }

  public SVGLeaveNode(String nodeName) {
    mName = nodeName;
  }

  public String propString(HashMap<String, String> presentationMap) {
    String ret = "/>\n";
    for (String key : prop.keySet()) {
      String avgkey = presentationMap.get(key);
      if (!"none".equals(prop.get(key))) {
        ret = "\n        " + avgkey + "=\"" + prop.get(key) + "\"" + ret;
      } else {
        ret = "\n        " + avgkey + "=\"#00000000\"" + ret;
      }
    }
    return ret;
  }

  boolean isGroupNode() {
    return false;
  }

  public void print(String indent) {
    logger.log(Level.FINE,
        indent + (d != null ? d : " null d ") + (mName != null ? mName : " null name "));

  }

  public void setPathData(String pathData) {
    d = pathData;

  }
}
