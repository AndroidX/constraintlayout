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

import androidx.constraintLayout.desktop.scan.CLScan;
import androidx.constraintLayout.desktop.ui.adapters.Annotations.Nullable;
import androidx.constraintlayout.core.motion.utils.TypedValues;
import androidx.constraintlayout.core.parser.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import static androidx.constraintLayout.desktop.ui.adapters.MotionLayoutAttrs.*;
import static androidx.constraintLayout.desktop.ui.adapters.MotionSceneAttrs.ATTR_ANDROID_ID;
import static androidx.constraintLayout.desktop.ui.adapters.MotionSceneAttrs.Tags.KEY_FRAME_SET;

/**
 * MTag implementation based on parsing a XML String
 */
public class KeyFramesTag implements MTag {

    private static final boolean DEBUG = false;
    String name;
    KeyFramesTag mParent;
    Object clientData;
    CLElement mClElement;
    HashMap<String, Attribute> mAttrList = new HashMap<>();
    ArrayList<MTag> mChildren = new ArrayList<>();


    public static KeyFramesTag parseForTimeLine(String str) {
        KeyFramesTag transition = new KeyFramesTag();
        KeyFramesTag tag = parse(str);
        if (tag != null) {
            transition.mChildren.add(tag);
        }
        transition.name = "Transition";
        return transition;
    }

    public static KeyFramesTag parse(String str) {

        try {
            CLKey obj = CLScan.findCLKey(CLParser.parse(str), "KeyFrames");

            if (obj == null) {
                System.out.println("No Key frames found");
                System.out.println("-----------------------------------");
                System.out.println(str);
                System.out.println("-----------------------------------");
                return null;
            }


            KeyFramesTag root = new KeyFramesTag();
            root.name = KEY_FRAME_SET;
            CLObject object = (CLObject) obj.getValue();
            CLObject clo = (CLObject) obj.get(0);
            int numberOf_types = clo.size();

            for (int i = 0; i < numberOf_types; i++) {
                CLElement o = clo.get(0);
                System.out.println(o.getClass().getSimpleName() + " " + o.content());
                CLKey clKey = (CLKey) o;
                switch (clKey.content()) {
                    case "KeyPositions":
                        buildKeyPositions(clKey.getValue(), root);
                        break;
                    case "KeyCycles":
                        break;
                    case "KeyAttributes":
                        break;
                    default:
                        System.err.println("UNKNOWN ! " + clKey.content());
                        break;

                }
            }

            CLElement c = obj.getValue();

            return root;
        } catch (CLParsingException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void buildKeyPositions(CLElement element, KeyFramesTag parent) throws CLParsingException {
        if (element instanceof CLArray) {
            CLArray clArray = (CLArray) element;
            int size = clArray.size();
            for (int i = 0; i < size; i++) {
                CLObject obj = (CLObject) clArray.get(i);
                buildKeyPosition(obj, parent);
            }


        }
    }

    private void add(String name, String value) {
        if (value == null) {
            return;
        }
        Attribute a = new Attribute();
        a.mAttribute = name;
        a.mNamespace = "";
        a.mValue = value;
        mAttrList.put(name, a);
    }

    static void buildKeyPosition(CLObject obj, KeyFramesTag parent) throws CLParsingException {
        String[] targets = buildStringArray(obj.get("target"));
        int[] positions = buildIntArray(obj.get("frames"));
        String easing = null;
        String curveFit = null;
        float[] percentWidth = null;
        float[] percentHeight = null;
        float[] sizePercent = null;
        float[] percentX = null;
        float[] percentY = null;
        int size = obj.size();
        for (int i = 0; i < size; i++) {
            CLKey clKey = (CLKey) obj.get(i);
            switch (clKey.content()) {
                case TypedValues.Position.S_TRANSITION_EASING:
                    easing = clKey.getValue().content();
                    break;
                case TypedValues.Position.S_PERCENT_WIDTH:
                    percentWidth = buildFloatArray(clKey.getValue());
                    break;
                case TypedValues.Position.S_PERCENT_HEIGHT:
                    percentHeight = buildFloatArray(clKey.getValue());
                    break;
                case TypedValues.Position.S_SIZE_PERCENT:
                    sizePercent = buildFloatArray(clKey.getValue());
                    break;
                case TypedValues.Position.S_PERCENT_X:
                    percentX = buildFloatArray(clKey.getValue());
                    break;
                case TypedValues.Position.S_PERCENT_Y:
                    percentY = buildFloatArray(clKey.getValue());
                    break;
                case TypedValues.Attributes.S_CURVE_FIT:
                    curveFit = clKey.getValue().content();
                    break;
            }
        }
        for (int i = 0; i < targets.length; i++) {
            String target = targets[i];
            for (int j = 0; j < positions.length; j++) {
                String position = Integer.toString(positions[j]);
                KeyFramesTag kTag = new KeyFramesTag();
                kTag.name = MotionSceneAttrs.Tags.KEY_POSITION;
                kTag.add(KEY_POSITION_TYPE, "delta");
                kTag.add(FRAME_POSITION, position);
                kTag.add(MOTION_TARGET, target);

                kTag.add(PERCENT_X, strLookup(percentX, j));
                kTag.add(PERCENT_Y, strLookup(percentY, j));
                kTag.add(PERCENT_WIDTH, strLookup(percentWidth, j));
                kTag.add(PERCENT_HEIGHT, strLookup(percentHeight, j));
                kTag.add(SIZE_PERCENT, strLookup(sizePercent, j));
                kTag.mParent = parent;
                parent.mChildren.add(kTag);
            }

        }

    }

    private static String strLookup(float[] array, int index) {
        if (array == null) {
            return null;
        }
        return Float.toString(array[index % array.length]);
    }

    private static String[] buildStringArray(CLElement tObj) throws CLParsingException {
        if (tObj instanceof CLArray) {
            CLArray tArray = (CLArray) tObj;
            String[] str = new String[((CLArray) tObj).size()];
            for (int i = 0; i < str.length; i++) {
                str[i] = tArray.get(i).content();
            }
            ;
            return str;
        } else {
            return new String[]{tObj.content()};
        }
    }

    private static int[] buildIntArray(CLElement tObj) throws CLParsingException {
        if (tObj instanceof CLArray) {
            CLArray tArray = (CLArray) tObj;
            int[] str = new int[((CLArray) tObj).size()];
            for (int i = 0; i < str.length; i++) {
                str[i] = tArray.get(i).getInt();
            }
            ;
            return str;
        } else {
            return new int[]{tObj.getInt()};
        }
    }

    private static float[] buildFloatArray(CLElement tObj) throws CLParsingException {
        if (tObj instanceof CLArray) {
            CLArray tArray = (CLArray) tObj;
            float[] str = new float[((CLArray) tObj).size()];
            for (int i = 0; i < str.length; i++) {
                str[i] = tArray.get(i).getFloat();
            }
            ;
            return str;
        } else {
            return new float[]{tObj.getFloat()};
        }
    }

    private KeyFramesTag() {

    }

    private KeyFramesTag(CLObject clObject, KeyFramesTag parent, String name) throws CLParsingException {
        mClElement = clObject;
        this.name = name;
        int n = clObject.size();
        for (int i = 0; i < n; i++) {
            CLElement tmp = clObject.get(i);
            if (tmp instanceof CLKey) {
                mChildren.add(new KeyFramesTag((CLKey) tmp, this));
            }
        }
    }

    private KeyFramesTag(CLKey clkey, KeyFramesTag parent) throws CLParsingException {
        mParent = parent;
        name = clkey.content();
        mClElement = clkey;
        CLElement value = clkey.getValue();
        if (value == null) return;
        if (value instanceof CLArray) {
            CLArray clArray = (CLArray) value;
            int count = clArray.size();
            for (int i = 0; i < count; i++) {
                CLElement v = clArray.get(i);
                if (v instanceof CLObject) {
                    mChildren.add(new KeyFramesTag((CLObject) v, this, "[" + i + "]"));
                }
            }
        } else if (value instanceof CLObject) {
            mChildren.add(new KeyFramesTag((CLObject) value, this, "node"));
        }

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
    public KeyFramesTag[] getChildTags() {
        return (KeyFramesTag[]) mChildren.toArray(new KeyFramesTag[0]);
    }

    @Override
    public MTag getParent() {
        return mParent;
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
        for (Attribute value : mAttrList.values()) {
            if (value.mAttribute.equals(attribute)) {
                return value.mValue;
            }
        }
        return null;
    }

    @Override
    public void print(String space) {
        String str = space + name;
        for (String s : mAttrList.keySet()) {
            System.out.println(space + " " + s + " : " + mAttrList.get(s).mValue);
        }
        System.out.println(str);
        for (MTag child : mChildren) {
            child.print(space + " ");
        }
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
        } else {
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

    public static void main(String[] arg) throws CLParsingException {
        String str = "{\n" +
                "  Debug: {\n" +
                "    name: 'motion8'\n" +
                "  },\n" +
                "  ConstraintSets: {\n" +
                "    start: {\n" +
                "      a: {\n" +
                "        width: 40,\n" +
                "        height: 40,\n" +
                "        start: ['parent', 'start', 16],\n" +
                "        bottom: ['parent', 'bottom', 16]\n" +
                "      }\n" +
                "    },\n" +
                "    end: {\n" +
                "      a: {\n" +
                "        width: 150,\n" +
                "        height: 100,\n" +
                "        rotationZ: 390,\n" +
                "        end: ['parent', 'end', 16],\n" +
                "        top: ['parent', 'top', 16]\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  Transitions: {\n" +
                "    default: {\n" +
                "      from: 'start',\n" +
                "      to: 'end',\n" +
                "      KeyFrames: {\n" +
                "        KeyPositions: [\n" +
                "          {\n" +
                "            target: ['a'],\n" +
                "            frames: [25, 50, 75],\n" +
                "            percentX: [0.1, 0.8, 0.1],\n" +
                "            percentY: [0.4, 0.8, 0]\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        CLObject parsedContent = CLParser.parse(str);
        CLKey obj = CLScan.findCLKey(parsedContent, "KeyFrames");
        if (obj != null) {
            System.out.println(obj.content());
            System.out.println(obj.getValue().content());
        } else {
            System.out.println("not found");
        }
        KeyFramesTag tag = parse(str);
        if (tag != null) {
            tag.print(" ");
        }
    }

}
