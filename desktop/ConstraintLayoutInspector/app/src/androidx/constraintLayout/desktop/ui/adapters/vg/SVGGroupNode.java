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

import java.util.ArrayList;

public class SVGGroupNode {

  private String mName;
  ArrayList<Object> mChildren = new ArrayList<Object>();

  public SVGGroupNode(String name) {
    mName = name;
  }

  public void addChildren(Object child) {
    mChildren.add(child);
  }

  public int getSize() {
    return mChildren.size();
  }

  public Object getChildAt(int index) {
    return mChildren.get(index);
  }

  public String getName() {
    return mName;
  }
}
