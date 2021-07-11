/*
 * Copyright 2021 The Android Open Source Project
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
package androidx.constraintLayout.desktop.scan;

import androidx.constraintLayout.desktop.ui.utils.Debug;
import androidx.constraintlayout.core.motion.utils.TypedValues;
import androidx.constraintlayout.core.parser.*;

import java.util.*;

// Todo: move to core

/**
 * Code that scans the CL text providing a callback to for each of the important words
 */
public class CLScan {
    static HashSet<String> sSectionKeyWord = new HashSet<>(Arrays.asList("Variables", "ConstraintSets", "Debug", "Generate", "KeyFrames", "Transitions", "KeyAttributes", "KeyPositions"));
    static HashSet<String> sAttributesKeyWord = new HashSet<>(Arrays.asList(
            "start",
            "end",
            "bottom",
            "top",
            "width",
            "height",
            "center",
            "parent",
            "centerHorizontally",
            "parent",
            "centerVertically",
            "parent",
            "alpha",
            "visible",
            "invisible",
            "gone",
            "Extends",
            "circular",
            "frames",
            "from",
            "to",
            "step",
            "tag",
            "pathMotionArc",
            "default",
            "custom"));

    static {
        sAttributesKeyWord.addAll(Arrays.asList(TypedValues.Attributes.KEY_WORDS));
        sAttributesKeyWord.addAll(Arrays.asList(TypedValues.Position.KEY_WORDS));
        sAttributesKeyWord.addAll(Arrays.asList(TypedValues.Custom.KEY_WORDS));
        sAttributesKeyWord.addAll(Arrays.asList(TypedValues.Cycle.KEY_WORDS));
    }

    static String[] sConstraintList = {
            "width", "height",
            "start", "end", "bottom", "top",
            "circular", "pivotX", "pivotY",
            "translationX", "translationY", "translationZ",
            "rotationX", "rotationY", "rotationZ",
            "centerHorizontally", "centerVertically"
    };
    public static Map<String, String[]> creationMap = new HashMap<>();

    static {
        creationMap.put("KeyPositions", TypedValues.Position.KEY_WORDS);
        creationMap.put("KeyCycles", TypedValues.Position.KEY_WORDS);
        creationMap.put("KeyAttributes", TypedValues.Position.KEY_WORDS);
        creationMap.put("KeyFrames", new String[]{"KeyAttributes", "KeyPositions", "KeyCycles"});
        creationMap.put("Transitions", new String[]{"default", "KeyFrames"});
        creationMap.put("default", new String[]{"from", "to", "pathMotionArc"});
        creationMap.put("Generate*", sConstraintList);
        creationMap.put("ConstraintSets**", sConstraintList);
        creationMap.put("root", new String[]{"ConstraintSets", "Transitions"});
        creationMap.put("ConstraintSets", new String[]{"Generate", "*"});
    }

    static void parse(String str, Scan scan) {

        try {
            CLObject parsedContent = CLParser.parse(str);
            scan(parsedContent, 0, scan);
        } catch (CLParsingException e) {
            e.printStackTrace();
        }
    }

    interface Scan {
        int UNKNOWN_ELEMENT = 0;
        int SECTION_ELEMENT = 1;
        int ATTRIBUTE_ELEMENT = 2;

        void object(String object, int type, int offset, int lenght);
    }

    static void scan(CLObject obj, int level, Scan scan) {
        int n = obj.size();
        try {
            for (int i = 0; i < n; i++) {
                CLElement tmp = obj.get(i);
                if (!(tmp instanceof CLKey)) {
                    continue;
                }
                CLKey clkey = ((CLKey) tmp);
                String attr = clkey.content();
                CLElement value = clkey.getValue();
                int attStart = (int) clkey.getStart();
                int attrLen = 1 + (int) clkey.getEnd() - attStart;
                if (value != null) {
                    if (value instanceof CLArray) {
                        CLArray array = (CLArray) value;
                        int count = array.size();
                        for (int j = 0; j < count; j++) {
                            CLElement v = array.get(j);
                            if (v instanceof CLObject) {
                                scan((CLObject) v, level + 1, scan);
                            } else {
                                int valStart = (int) v.getStart();
                                int valLen = 1 + (int) v.getEnd() - valStart;
                                scan.object(v.getClass().getSimpleName(), 0, valStart, valLen);
                            }
                        }
                        scan.object(attr, getElementClass(attr), attStart, attrLen);

                    } else if (value instanceof CLObject) {
                        scan.object(attr, getElementClass(attr), attStart, attrLen);
                        scan((CLObject) value, level, scan);
                    } else {
                        int valStart = (int) value.getStart();
                        int valLen = 1 + (int) value.getEnd() - valStart;
                        scan.object(attr, getElementClass(attr), attStart, attrLen);
                        scan.object(value.getClass().getSimpleName(), 0, valStart, valLen);
                    }
                }

            }
        } catch (CLParsingException e) {
            e.printStackTrace();
        }
    }

    /**
     * For a given offset in the string find the CLKey nodes to the root
     *
     * @param str
     * @param offset
     * @return
     */
    public static CLKey[] getTreeAtLocation(String str, int offset) {
        try {
            CLObject parsedContent = CLParser.parse(str);
            return buildTree(parsedContent, offset).toArray(new CLKey[0]);
        } catch (CLParsingException e) {
            e.printStackTrace();
        }
        return new CLKey[0];
    }


    static ArrayList<CLKey> buildTree(CLObject obj, int offset) {
        ArrayList<CLKey> ret = new ArrayList<>();
        int n = obj.size();
        try {
            for (int i = 0; i < n; i++) {
                CLElement tmp = obj.get(i);
                if (!(tmp instanceof CLKey)) {
                    continue;
                }
                CLKey clkey = ((CLKey) tmp);
                String attr = clkey.content();
                CLElement value = clkey.getValue();
                int attStart = (int) clkey.getStart();
                int attrEnd = 1 + (int) clkey.getEnd();

                int valStart = (int) value.getStart();
                int valEnd = 1 + (int) value.getEnd();
                if ((valStart < offset && valEnd > offset)
                        || (attStart < offset && attrEnd > offset)) {
                    ret.add(clkey);
                }
                if (value != null) {

                    if (value instanceof CLArray) {
                        CLArray array = (CLArray) value;
                        int count = array.size();
                        for (int j = 0; j < count; j++) {
                            CLElement v = array.get(j);
                            if (v.getStart() < offset && v.getEnd() > offset) {
                                if (v instanceof CLObject) {
                                    ret.addAll(buildTree((CLObject) v, offset));
                                }
                            }
                        }
                    } else if (value instanceof CLObject) {

                        if (valStart < offset && valEnd > offset) {

                            ret.addAll(buildTree((CLObject) value, offset));
                        }
                    }
                }
            }
        } catch (CLParsingException e) {
            e.printStackTrace();
        }
        return ret;
    }

   public  static  CLKey findCLKey(CLObject obj, String name ) {

        int n = obj.size();
        try {
            for (int i = 0; i < n; i++) {
                CLElement tmp = obj.get(i);
                if (!(tmp instanceof CLKey)) {
                    continue;
                }
                CLKey clkey = ((CLKey) tmp);
                String attr = clkey.content();
                System.out.println(Debug.indent(0)+attr);
                if (name.equals(attr)) {
                    return clkey;
                }
                CLElement value = clkey.getValue();

                if (value != null) {
                    if (value instanceof CLArray) {
                        CLArray array = (CLArray) value;
                        int count = array.size();
                        for (int j = 0; j < count; j++) {
                            CLElement v = array.get(j);

                            if (v instanceof CLObject) {
                                CLKey found = findCLKey((CLObject) v, name);
                                if (found != null) {
                                    return found;
                                }
                            }
                        }
                    } else if (value instanceof CLObject) {

                        CLKey found = findCLKey((CLObject) value, name);
                        if (found != null) {
                            return found;
                        }
                    }
                }
            }

        } catch (CLParsingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static int getElementClass(String str) {
        if (sSectionKeyWord.contains(str)) {
            return Scan.SECTION_ELEMENT;
        }
        if (sAttributesKeyWord.contains(str)) {
            return Scan.ATTRIBUTE_ELEMENT;
        }
        return Scan.UNKNOWN_ELEMENT;
    }

}
