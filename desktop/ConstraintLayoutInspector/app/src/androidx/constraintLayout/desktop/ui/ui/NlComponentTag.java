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
package androidx.constraintLayout.desktop.ui.ui;


import androidx.constraintLayout.desktop.ui.adapters.Annotations.Nullable;
import androidx.constraintLayout.desktop.ui.adapters.MTag;
import androidx.constraintLayout.desktop.ui.utils.Debug;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class wraps a NL component and provides a uniform interface between it and other types of XML based objects
 */
public class NlComponentTag implements MTag {

  NlComponentTag mParent;
  private static final boolean DEBUG = false;

  public NlComponentTag( NlComponentTag parent) {

    mParent = parent;
  }

  @Override
  public String getTagName() {
    return""  ;
  }

  @Override
  public TagWriter deleteTag() {
    return null;
  }

  @Override
  public MTag getParent() {
    return mParent;
  }

  @Override
  public void setClientData(String type, Object motionAttributes) {
    if (DEBUG) {
      Debug.log("setClientData MOTION_LAYOUT_PROPERTIES setting " + motionAttributes );
    }

  }

  @Override
  public Object getClientData(String type) {
    return null;
  }

  @Override
  public ArrayList<MTag> getChildren() {
    ArrayList<MTag> ret = new ArrayList<>();

    return ret;
  }

  @Override
  public HashMap<String, Attribute> getAttrList() {
    HashMap<String, Attribute> ret = new  HashMap<>();

    return ret;
  }

  @Override
  public MTag[] getChildTags() {
    return   getChildren().toArray(new MTag[0]);
  }

  @Override
  public MTag[] getChildTags(String type) {
    ArrayList<MTag> ret = new ArrayList<>();
    for (MTag child : getChildren()) {
      if (child.getTagName().equals(type)) {
        ret.add(child);
      }
    }
    return ret.toArray(new MTag[0]);
  }

  @Override
  public MTag[] getChildTags(String attribute, String value) {
    ArrayList<MTag> ret = new ArrayList<>();
    for (MTag child : getChildren()) {
      if (value.equals(child.getAttributeValue(attribute))) {
        ret.add(child);
      }
    }
    return ret.toArray(new MTag[0]);
  }

  @Override
  public MTag[] getChildTags(String type, String attribute, String value) {
    ArrayList<MTag> ret = new ArrayList<>();
    for (MTag child : getChildren()) {
      if (child.getTagName().equals(type) && value.equals(child.getAttributeValue(attribute))) {
        ret.add(child);
      }
    }
    return ret.toArray(new MTag[0]);
  }

  @Override
  @Nullable
  public MTag getChildTagWithTreeId(String type, String treeId) {
    for (MTag child : getChildren()) {
      if (child.getTreeId().equals(treeId)) {
        return child;
      }
    }
    return null;
  }

  @Override
  @Nullable
  public String getTreeId() {
    return "";
  }

  @Override
  public String getAttributeValue(String attribute) {

    return null;
  }

  @Override
  public void print(String space) {
    System.out.println("\n" + space + "<" + getTagName() + ">");

    System.out.println(space + "</" + getTagName() + ">");
  }

  @Override
  public String toXmlString() {
    return toFormalXmlString("");
  }

  @Override
  public String toFormalXmlString(String space) {
    String ret = "";
    if (space == null) {
      ret = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
      space = "";
    }
    ret += "\n" + space + "<" + getTagName();


    for (MTag child : getChildTags()) {
      ret += child.toFormalXmlString(space + "  ");
    }
    ret += space + "</" + getTagName() + ">\n";
    return ret;
  }

  @Override
  public void printFormal(String space, PrintStream out) {
    if (space == null) {
      out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
      space = "";
    }
    out.print("\n" + space + "<" + getTagName());

    out.println(" >");

    for (MTag child : getChildTags()) {
      child.printFormal(space + "  ", out);
    }
    out.println(space + "</" + getTagName() + ">");
  }

  @Override
  public TagWriter getChildTagWriter(String name) {
    return null; // TODO WE NEED TagWriter. But currently we do not write NLComponents
  }

  @Override
  public TagWriter getTagWriter() {
    return null;
  }

}
