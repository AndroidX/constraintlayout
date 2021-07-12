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
package androidx.constraintLayout.desktop.ui.adapters;

import androidx.constraintLayout.desktop.ui.adapters.Annotations.Nullable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.*;

import static androidx.constraintLayout.desktop.ui.adapters.MotionSceneAttrs.ATTR_ANDROID_ID;

/**
 * MTag implementation used in programmatic creation
 */
public class DefaultMTag implements MTag {

  private static final boolean DEBUG = false;
  String name;
  DefaultMTag parent;
  Object clientData;
  HashMap<String, Attribute> mAttrList = new HashMap<>();
  ArrayList<MTag> mChildren = new ArrayList<>();

  public DefaultMTag(String tagName){
    name =  tagName;
  }

  public void addChild(MTag tag) {
    mChildren.add(tag);
  }

  public void addChild(DefaultMTag ...tags) {
    for (int i = 0; i < tags.length; i++) {
      DefaultMTag tag = tags[i];
      mChildren.add(tag);
      tag.parent = this;
    }
  }

  public void addAttribute(String name, String value) {
    if (value == null) {
      mAttrList.remove(name);
    }
    Attribute attr = new Attribute();
    attr.mValue = value;
    attr.mAttribute = name;
    attr.mNamespace = "";
    mAttrList.put(name, attr);
  }
  @Override
  public String toString() {
    return ("MTag (" + name + " )");
  }

  @Override
  public String getTagName() {
    return name;
  }

  @Override
  public TagWriter deleteTag() {
    return null;
  }

  @Override
  public void setClientData(String type, Object clientData) {
    this.clientData = clientData;
  }

  @Override
  public Object getClientData(String type) {
    return this.clientData;
  }

  @Override
  public ArrayList<MTag> getChildren() {
    return mChildren;
  }

  @Override
  public HashMap<String, Attribute> getAttrList() {
    return mAttrList;
  }

  @Override
  public DefaultMTag[] getChildTags() {
    return (DefaultMTag[])mChildren.toArray(new DefaultMTag[0]);
  }

  @Override
  public MTag getParent() {
    return parent;
  }

  @Override
  public MTag[] getChildTags(String type) {
    ArrayList<MTag> filter = new ArrayList<>();
    for (MTag child : mChildren) {
      if (child.getTagName().equals(type)) {
        filter.add(child);
      }
    }
    return filter.toArray(new MTag[0]);
  }

  /**
   * Get children who attribute == value
   */
  @Override
  public MTag[] getChildTags(String attribute, String value) {
    ArrayList<MTag> filter = new ArrayList<>();
    for (MTag child : mChildren) {
      String childValue = child.getAttributeValue(attribute);
      if (childValue != null && childValue.endsWith(value)) {
        filter.add(child);
      }
    }
    return filter.toArray(new MTag[0]);
  }

  /**
   * Get children who attribute == value
   */
  @Override
  public MTag[] getChildTags(String type, String attribute, String value) {
    ArrayList<MTag> filter = new ArrayList<>();
    for (MTag child : mChildren) {
      if (child.getTagName().equals(type)) {
        String childValue = child.getAttributeValue(attribute);
        if (childValue != null && childValue.endsWith(value)) {
          filter.add(child);
        }
      }
    }
    return filter.toArray(new MTag[0]);
  }

  @Override
  @Nullable
  public MTag getChildTagWithTreeId(String type, String treeId) {
    for (MTag child : mChildren) {
      if (treeId.equals(child.getTreeId())) {
        return child;
      }
    }
    return null;
  }

  @Override
  @Nullable
  public String getTreeId() {
    if (name.startsWith("Key")) {
      return getAttributeValue("framePosition") + "|" + name;
    }
    if (name.equals("Transition")) {
      return getAttributeValue("constraintSetStart") + "|" + getAttributeValue("constraintSetEnd");
    }
    return getAttributeValue(ATTR_ANDROID_ID);
  }

  @Override
  public String getAttributeValue(String attribute) {
    Attribute att = mAttrList.get(attribute);
    return (att==null)? null: att.mValue;
  }

  @Override
  public void print(String space) {
    System.out.println("\n" + space + "<" + name + ">");
    for (Attribute value : mAttrList.values()) {
      System.out.println(space + "   " + value.mAttribute + "=\"" + value.mValue + "\"");
    }
    for (MTag child : mChildren) {
      child.print(space + "   ");
    }
    System.out.println(space + "</" + name + ">");
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
    ret += "\n" + space + "<" + name;
    Attribute[] attr = mAttrList.values().toArray(new Attribute[0]);
    Arrays.sort(attr, new Comparator<Attribute>() {
      @Override
      public int compare(Attribute o1, Attribute o2) {
        return o1.mAttribute.compareTo(o2.mAttribute);
      }
    });
    for (Attribute value : attr) {
      String nameSpace = value.mNamespace;
      if (nameSpace.startsWith("http")) {
        if (nameSpace.endsWith("res-auto")) {
          nameSpace = "motion";
        }
        if (nameSpace.endsWith("android")) {
          nameSpace = "android";
        }
      }
      ret += "\n" + space + "   " + nameSpace + ":" + value.mAttribute + "=\"" + value.mValue
             + "\"";
    }
    if (mChildren.size() == 0) {
      ret += (" />\n");
    }
    else {
      ret += (" >\n");
    }
    for (MTag child : mChildren) {
      ret += child.toFormalXmlString(space + "  ");
    }
    if (mChildren.size() > 0) {
      ret += space + "</" + name + ">\n";
    }
    return ret;
  }

  @Override
  public void printFormal(String space, PrintStream out) {
    if (space == null) {
      out.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
      space = "";
    }
    out.print("\n" + space + "<" + name);
    for (Attribute value : mAttrList.values()) {
      out.print(
        "\n" + space + "   " + value.mNamespace + ":" + value.mAttribute + "=\"" + value.mValue
        + "\"");
    }
    out.println(" >");

    for (MTag child : mChildren) {
      child.printFormal(space + "  ", out);
    }
    out.println(space + "</" + name + ">");
  }

  @Override
  public TagWriter getChildTagWriter(String name) {
    return null;
  }

  @Override
  public TagWriter getTagWriter() {
    return null;
  }
}
