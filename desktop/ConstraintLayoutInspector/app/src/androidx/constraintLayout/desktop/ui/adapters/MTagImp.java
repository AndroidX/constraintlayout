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
package androidx.constraintLayout.desktop.ui.adapters;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MTagImp implements MTag {

  private static final boolean DEBUG = false;
  String name;
  MTagImp parent;
  Object mClientData;
  HashMap<String, Attribute> mAttrList = new HashMap<>();

  @Override
  public String toString() {
    return ("MTag (" + name + " )");
  }

  public String getTagName() {
    return name;
  }

  @Override
  public TagWriter deleteTag() {
    parent.children.remove(this);
    return null;
  }

  @Override
  public void setClientData(String type, Object clientData) {
    mClientData = clientData;
  }

  @Override
  public Object getClientData(String type) {
    return mClientData;
  }

  ArrayList<MTag> children = new ArrayList<>();

  public ArrayList<MTag> getChildren() {
    return children;
  }

  public HashMap<String, Attribute> getAttrList() {
    return mAttrList;
  }

  public MTagImp[] getChildTags() {
    return children.toArray(new MTagImp[0]);
  }

  @Override
  public MTag getParent() {
    return parent;
  }

  public MTag[] getChildTags(String type) {
    ArrayList<MTag> filter = new ArrayList<>();
    for (MTag child : children) {
      if (child.getTagName().equals(type)) {
        filter.add(child);
      }
    }
    return filter.toArray(new MTagImp[0]);
  }

  /**
   * Get children who attribute == value
   */
  public MTag[] getChildTags(String attribute, String value) {
    ArrayList<MTag> filter = new ArrayList<>();
    for (MTag child : children) {
      String childValue = child.getAttributeValue(attribute);
      if (childValue != null && childValue.endsWith(value)) {
        filter.add(child);
      }
    }
    return filter.toArray(new MTagImp[0]);
  }

  /**
   * Get children who attribute == value
   */
  public MTag[] getChildTags(String type, String attribute, String value) {
    ArrayList<MTag> filter = new ArrayList<>();
    for (MTag child : children) {
      if (child.getTagName().equals(type)) {
        String childValue = child.getAttributeValue(attribute);
        if (childValue != null && childValue.endsWith(value)) {
          filter.add(child);
        }
      }
    }
    return filter.toArray(new MTagImp[0]);
  }

  public String getAttributeValue(String attribute) {
    for (Attribute value : mAttrList.values()) {
      if (value.mAttribute.equals(attribute)) {
        return value.mValue;
      }
    }
    return null;
  }

  @Override
  public MTag getChildTagWithTreeId(String type, String treeId) {
    return null;
  }

  @Override
  public String getTreeId() {
    return null;
  }

  public void print(String space) {
    System.out.println("\n" + space + "<" + name + ">");
    for (Attribute value : mAttrList.values()) {
      System.out.println(space + "   " + value.mAttribute + "=\"" + value.mValue + "\"");
    }
    for (MTag child : children) {
      child.print(space + "   ");
    }
    System.out.println(space + "</" + name + ">");
  }

  public String toXmlString() {
    return toFormalXmlString("");
  }

  public String toFormalXmlString(String space) {
    String ret = "";
    if (space == null) {
      ret = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
      space = "";
    }
    ret += "\n" + space + "<" + name;
    for (Attribute value : mAttrList.values()) {
      ret += "\n" + space + "   " + value.mNamespace + ":" + value.mAttribute + "=\"" + value.mValue
        + "\"";
    }
    if (children.size() == 0) {
      ret += (" />\n");

    } else {
      ret += (" >\n");
    }
    for (MTag child : children) {
      ret += child.toFormalXmlString(space + "  ");
    }
    if (children.size() > 0) {
      ret += space + "</" + name + ">\n";
    }
    return ret;
  }

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

    for (MTag child : children) {
      child.printFormal(space + "  ", out);
    }
    out.println(space + "</" + name + ">");
  }

  public static void main(String[] str) {
    String dir = "/media/hoford/hofordssd/dtools/design-tools/examples/Dev/app/src/main/res/xml";
    String file = "motion_testscene_08_scene.xml";
    MTagImp mTag = parse(new File(dir + File.separator + file));
    mTag.printFormal(null, System.out);

  }

  public static MTagImp parse(String str) {
    if (DEBUG) {
      System.out.println(" parse " + str);
    }
    HashSet<MTagImp> props = new HashSet<>();
    try {
      InputStream inputStream = new ByteArrayInputStream(str.getBytes(Charset.forName("UTF-8")));
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(inputStream, new DefaultHandler() {
        MTagImp currentTag = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
          throws SAXException {
          MTagImp newTag = (currentTag == null) ? new RootTag(null) : new MTagImp();
          newTag.parent = currentTag;
          newTag.name = qName;
          if (currentTag != null) {
            currentTag.children.add(newTag);
          } else {
            props.add(newTag);
          }
          currentTag = newTag;
          if (DEBUG) {
            System.out.println("START " + currentTag.name);
          }

          for (int i = 0; i < attributes.getLength(); i++) {
            String aName = attributes.getQName(i);
            Attribute attribute = new Attribute();
            String[] sp = aName.split(":");
            if (sp.length != 2) {
              continue;
            }
            attribute.mAttribute = sp[1];
            attribute.mNamespace = sp[0];
            attribute.mValue = attributes.getValue(i);
            currentTag.mAttrList.put(aName, attribute);
            if (DEBUG) {

              System.out.println("     " + attribute.mAttribute);
            }
          }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
          if (currentTag != null) {
            if (DEBUG) {
              System.out.println("END " + currentTag.name + "  qName=" + qName);
            }
            currentTag = currentTag.parent;
          }
        }
      });

    } catch (Exception e) {
      e.printStackTrace();
    }
    return props.iterator().next();
  }

  static class RootTag extends MTagImp {
    File mSourceFile;

    RootTag(File file) {
      mSourceFile = file;
    }
  }

  public static MTagImp parse(File file) {
    if (DEBUG) {
      System.out.println(" parse " + file.getName() + (file.exists() ? "Exist" : "not found"));
    }
    HashSet<MTagImp> props = new HashSet<>();
    try {
      FileInputStream inputStream = new FileInputStream(file);
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(inputStream, new DefaultHandler() {
        MTagImp currentTag = null;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
          throws SAXException {
          MTagImp newTag = (currentTag == null) ? new RootTag(file) : new MTagImp();
          newTag.parent = currentTag;
          newTag.name = qName;
          if (currentTag != null) {
            currentTag.children.add(newTag);
          } else {
            props.add(newTag);
          }
          currentTag = newTag;
          if (DEBUG) {
            System.out.println("START " + currentTag.name);
          }

          for (int i = 0; i < attributes.getLength(); i++) {
            String aName = attributes.getQName(i);
            Attribute attribute = new Attribute();
            String[] sp = aName.split(":");
            if (sp.length != 2) {
              continue;
            }
            attribute.mAttribute = sp[1];
            attribute.mNamespace = sp[0];
            attribute.mValue = attributes.getValue(i);
            currentTag.mAttrList.put(aName, attribute);
            if (DEBUG) {

              System.out.println("     " + attribute.mAttribute);
            }
          }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
          if (currentTag != null) {
            if (DEBUG) {
              System.out.println("END " + currentTag.name + "  qName=" + qName);
            }
            currentTag = currentTag.parent;
          }
        }
      });

    } catch (Exception e) {
      e.printStackTrace();
    }
    return props.iterator().next();
  }

  class TagWriterImp extends MTagImp implements TagWriter {
    HashMap<String, Attribute> newTags = new java.util.HashMap<>();

    TagWriterImp(String name, MTagImp parent) {
      this.name = name;
      super.parent = parent;
    }

    TagWriterImp( MTagImp source) {
      this.name = source.name;
      this.parent = source.parent;
      this.mAttrList = source.mAttrList;
      source.parent.children.remove(source);
      source.parent.children.add(this);

    }
    @Override
    public void setAttribute(String nameSpace, String attributeName, String value) {
      Attribute attribute = new Attribute();
      attribute.mAttribute = attributeName;
      attribute.mNamespace = nameSpace;
      attribute.mValue = value;
      mAttrList.put(nameSpace + ":" + attributeName, attribute);
      newTags.put(nameSpace + ":" + attributeName, attribute);
    }

    @Override
    public MTag commit(String type ) {
      printFormal(type + " > ", System.out);
      RootTag rootTag = null;
      for (MTag tag = parent; tag != null; tag = ((MTagImp) tag).parent) {
        if (tag instanceof RootTag) {
          rootTag = (RootTag) tag;
        }
      }
      MTagImp ret = new MTagImp();
      ret.name = name;
      ret.parent = parent;
      ret.mAttrList = mAttrList;
      parent.children.add(ret);
      return ret;
    }
    CommitListener myListener;
    @Override
    public void addCommitListener(CommitListener listener) {
      myListener = listener;
    }

    @Override
    public void removeCommitListener(CommitListener listener) {
      myListener = null;
    }
  }

  @Override
  public TagWriter getChildTagWriter(String name) {
    return new TagWriterImp(name, this);
  }

  @Override
  public TagWriter getTagWriter() {
    return  new TagWriterImp(this);
  }
}
