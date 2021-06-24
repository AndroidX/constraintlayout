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
package androidx.constraintlayout.core.parser;

public class CLKey extends CLContainer {

  public CLKey(char[] content) {
    super(content);
  }

  public static CLElement allocate(char[] content) {
    return new CLKey(content);
  }

  public static CLElement allocate(String name, CLElement value) {
    CLKey key = new CLKey(name.toCharArray());
    key.setStart(0);
    key.setEnd(name.length() -1);
    key.set(value);
    return key;
  }

  protected String toJSON() {
    if (mElements.size() > 0) {
      return getDebugName() + content() + ": " + mElements.get(0).toJSON();
    }
    return getDebugName() + content() + ": <> ";
  }

  public void set(CLElement value) {
    if (mElements.size() > 0) {
      mElements.set(0, value);
    } else {
      mElements.add(value);
    }
  }

  public CLElement getValue() {
    if (mElements.size() > 0) {
      return mElements.get(0);
    }
    return null;
  }
}
