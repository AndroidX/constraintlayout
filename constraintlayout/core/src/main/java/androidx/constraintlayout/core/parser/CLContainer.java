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

import java.util.ArrayList;

public class CLContainer extends CLElement {
  ArrayList<CLElement> mElements = new ArrayList<>();

  public CLContainer(char[] content) {
    super(content);
  }

  public static CLElement allocate(char[] content) {
    return new CLContainer(content);
  }

  public void add(CLElement element) {
    mElements.add(element);
    if (CLParser.DEBUG) {
      System.out.println("added element " + element + " to " + this);
    }
  }

  @Override
  public String toString() {
    StringBuilder list = new StringBuilder();
    for (CLElement element : mElements) {
      if (list.length() > 0) {
        list.append("; ");
      }
      list.append(element);
    }
    return super.toString() + " = <" + list + " >";
  }

  public int size() {
    return mElements.size();
  }

  public ArrayList<String> names() {
    ArrayList<String> names = new ArrayList<>();
    for (CLElement element : mElements) {
      if (element instanceof CLKey) {
        CLKey key = (CLKey) element;
        names.add(key.content());
      }
    }
    return names;
  }

  public boolean has(String name) {
    for (CLElement element : mElements) {
      CLKey key = (CLKey) element;
      if (key.content().equals(name)) {
        return true;
      }
    }
    return false;
  }

  public void put(String name, CLElement value) {
    for (CLElement element : mElements) {
      CLKey key = (CLKey) element;
      if (key.content().equals(name)) {
        key.set(value);
        return;
      }
    }
    CLKey key = (CLKey) CLKey.allocate(name, value);
    mElements.add(key);
  }

  public void putNumber(String name, float value) {
    put(name, new CLNumber(value));
  }

  public void remove(String name) {
    ArrayList<CLElement> toRemove = new ArrayList<>();
    for (CLElement element : mElements) {
      CLKey key = (CLKey) element;
      if (key.content().equals(name)) {
        toRemove.add(element);
      }
    }
    for (CLElement element : toRemove) {
      mElements.remove(element);
    }
  }

  /////////////////////////////////////////////////////////////////////////
  // By name
  /////////////////////////////////////////////////////////////////////////

  public CLElement get(String name) throws CLParsingException {
    for (CLElement element : mElements) {
      CLKey key = (CLKey) element;
      if (key.content().equals(name)) {
        return key.getValue();
      }
    }
    throw new CLParsingException("no element for key <" + name + ">", this);
  }

  public int getInt(String name) throws CLParsingException {
    CLElement element = get(name);
    if (element != null) {
      return element.getInt();
    }
    throw new CLParsingException("no int found for key <" + name + ">," +
            " found [" + element.getStrClass() + "] : " + element, this);
  }

  public float getFloat(String name) throws CLParsingException {
    CLElement element = get(name);
    if (element != null) {
      return element.getFloat();
    }
    throw new CLParsingException("no float found for key <" + name + ">," +
            " found [" + element.getStrClass() + "] : " + element, this);
  }

  public CLArray getArray(String name) throws CLParsingException {
    CLElement element = get(name);
    if (element instanceof CLArray) {
      return (CLArray) element;
    }
    throw new CLParsingException("no array found for key <" + name + ">," +
            " found [" + element.getStrClass() + "] : " + element, this);
  }

  public CLObject getObject(String name) throws CLParsingException {
    CLElement element = get(name);
    if (element instanceof CLObject) {
      return (CLObject) element;
    }
    throw new CLParsingException("no object found for key <" + name + ">," +
            " found [" + element.getStrClass() + "] : " + element, this);
  }

  public String getString(String name) throws CLParsingException {
    CLElement element = get(name);
    if (element instanceof CLString) {
      return  element.content();
    }
    String strClass = null;
    if (element != null) {
      strClass = element.getStrClass();
    }
    throw new CLParsingException("no string found for key <" + name + ">," +
            " found [" + strClass + "] : " + element, this);
  }

  public boolean getBoolean(String name) throws CLParsingException {
    CLElement element = get(name);
    if (element instanceof CLToken) {
      return ((CLToken) element).getBoolean();
    }
    throw new CLParsingException("no boolean found for key <" + name + ">," +
            " found [" + element.getStrClass() + "] : " + element, this);
  }

  /////////////////////////////////////////////////////////////////////////
  // Optional
  /////////////////////////////////////////////////////////////////////////

  public CLElement getOrNull(String name) {
    for (CLElement element : mElements) {
      CLKey key = (CLKey) element;
      if (key.content().equals(name)) {
        return key.getValue();
      }
    }
    return null;
  }

  public CLObject getObjectOrNull(String name) {
    CLElement element = getOrNull(name);
    if (element instanceof CLObject) {
      return (CLObject) element;
    }
    return null;
  }

  public CLArray getArrayOrNull(String name) {
    CLElement element = getOrNull(name);
    if (element instanceof CLArray) {
      return (CLArray) element;
    }
    return null;
  }

  public String getStringOrNull(String name) {
    CLElement element = getOrNull(name);
    if (element instanceof CLString) {
      return  element.content();
    }
    return null;
  }

  public float getFloatOrNaN(String name) {
    CLElement element = getOrNull(name);
    if (element instanceof CLNumber) {
      return element.getFloat();
    }
    return Float.NaN;
  }

  /////////////////////////////////////////////////////////////////////////
  // By index
  /////////////////////////////////////////////////////////////////////////

  public CLElement get(int index) throws CLParsingException {
    if (index >= 0 && index < mElements.size()) {
      return mElements.get(index);
    }
    throw new CLParsingException("no element at index " + index, this);
  }

  public int getInt(int index) throws CLParsingException {
    CLElement element = get(index);
    if (element != null) {
      return element.getInt();
    }
    throw new CLParsingException("no int at index " + index, this);
  }

  public float getFloat(int index) throws CLParsingException {
    CLElement element = get(index);
    if (element != null) {
      return element.getFloat();
    }
    throw new CLParsingException("no float at index " + index, this);
  }

  public CLArray getArray(int index) throws CLParsingException {
    CLElement element = get(index);
    if (element instanceof CLArray) {
      return (CLArray) element;
    }
    throw new CLParsingException("no array at index " + index, this);
  }

  public CLObject getObject(int index) throws CLParsingException {
    CLElement element = get(index);
    if (element instanceof CLObject) {
      return (CLObject) element;
    }
    throw new CLParsingException("no object at index " + index, this);
  }

  public String getString(int index) throws CLParsingException {
    CLElement element = get(index);
    if (element instanceof CLString) {
      return  element.content();
    }
    throw new CLParsingException("no string at index " + index, this);
  }

  public boolean getBoolean(int index) throws CLParsingException {
    CLElement element = get(index);
    if (element instanceof CLToken) {
      return ((CLToken) element).getBoolean();
    }
    throw new CLParsingException("no boolean at index " + index, this);
  }

  /////////////////////////////////////////////////////////////////////////
  // Optional
  /////////////////////////////////////////////////////////////////////////

  public CLElement getOrNull(int index) {
    if (index >= 0 && index < mElements.size()) {
      return mElements.get(index);
    }
    return null;
  }

  public String getStringOrNull(int index) {
    CLElement element = getOrNull(index);
    if (element instanceof CLString) {
      return  element.content();
    }
    return null;
  }
}
