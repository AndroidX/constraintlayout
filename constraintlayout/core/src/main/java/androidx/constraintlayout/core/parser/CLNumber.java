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

public class CLNumber extends CLElement {

  float value = Float.NaN;
  public CLNumber(char[] content) {
    super(content);
  }

  public CLNumber(float value) {
    super(null);
    this.value = value;
  }

  public static CLElement allocate(char[] content) {
    return new CLNumber(content);
  }

  protected String toJSON() {
    float value = getFloat();
    int intValue = (int) value;
    if ((float) intValue == value) {
      return "" + intValue;
    }
    return "" + value;
  }

  protected String toFormattedJSON(int indent, int forceIndent) {
    StringBuilder json = new StringBuilder();
    addIndent(json, indent);
    float value = getFloat();
    int intValue = (int) value;
    if ((float) intValue == value) {
      json.append(intValue);
    } else {
      json.append(value);
    }
    return json.toString();
  }

  public boolean isInt() {
    float value = getFloat();
    int intValue = (int) value;
    return ((float) intValue == value);
  }

  @Override
  public int getInt() {
    if (Float.isNaN(value)) {
      value = Integer.parseInt(content());
    }
    return (int) value;
  }

  @Override
  public float getFloat() {
    if (Float.isNaN(value)) {
      value = Float.parseFloat(content());
    }
    return value;
  }

  public void putValue(float value) {
    this.value = value;
  }

}