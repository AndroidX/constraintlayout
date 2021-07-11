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

import java.awt.geom.Path2D;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class VDParser {

  private static Logger logger = Logger.getLogger(VDParser.class.getSimpleName());
  public static final String LOGTAG = "IconParser";

  private static final String PATH_SHIFT_X = "shift-x";
  private static final String PATH_SHIFT_Y = "shift-y";

  private static final String SHAPE_VECTOR = "vector";
  private static final String SHAPE_PATH = "path";
  private static final String SHAPE_GROUP = "group";

  private static final String ANIMATION_ID = "id";

  private static final String PATH_ID = "android:name";
  private static final String PATH_DESCRIPTION = "android:pathData";
  private static final String PATH_FILL = "android:fillColor";
  private static final String PATH_FILL_OPACTIY = "android:fillAlpha";
  private static final String PATH_STROKE = "android:strokeColor";
  private static final String PATH_STROKE_OPACTIY = "android:strokeAlpha";

  private static final String PATH_STROKE_WIDTH = "android:strokeWidth";
  private static final String PATH_ROTATION = "android:rotation";
  private static final String PATH_ROTATION_X = "android:pivotX";
  private static final String PATH_ROTATION_Y = "android:pivotY";
  private static final String PATH_TRIM_START = "android:trimPathStart";
  private static final String PATH_TRIM_END = "android:trimPathEnd";
  private static final String PATH_TRIM_OFFSET = "android:trimPathOffset";
  private static final String PATH_STROKE_LINECAP = "android:strokeLinecap";
  private static final String PATH_STROKE_LINEJOIN = "android:strokeLinejoin";
  private static final String PATH_STROKE_MITERLIMIT = "android:strokeMiterlimit";
  private static final String FILL_TYPE = "android:fillType";
  private static final String PATH_CLIP = "android:clipToPath";
  private static final String LINECAP_BUTT = "butt";
  private static final String LINECAP_ROUND = "round";
  private static final String LINECAP_SQUARE = "square";
  private static final String LINEJOIN_MITER = "miter";
  private static final String LINEJOIN_ROUND = "round";
  private static final String LINEJOIN_BEVEL = "bevel";

  interface ElemParser {

    public void parse(VDTree path, Attributes attributes);
  }

  ElemParser mParseSize = new ElemParser() {
    @Override
    public void parse(VDTree tree, Attributes attributes) {
      parseSize(tree, attributes);
    }
  };

  ElemParser mParsePath = new ElemParser() {
    @Override
    public void parse(VDTree tree, Attributes attributes) {
      VDPath p = parsePathAttributes(attributes);
      tree.add(p);
    }
  };

  ElemParser mParseGroup = new ElemParser() {
    @Override
    public void parse(VDTree tree, Attributes attributes) {
      VDGroup g = parseGroupAttributes(attributes);
      tree.add(g);
    }
  };

  HashMap<String, ElemParser> tagSwitch = new HashMap<String, ElemParser>();

  {
    tagSwitch.put(SHAPE_VECTOR, mParseSize);
    tagSwitch.put(SHAPE_PATH, mParsePath);
    tagSwitch.put(SHAPE_GROUP, mParseGroup);
    // TODO: add <g> tag and start to build the tree.
  }

  // Incoming file is the built XML file, not the SVG.
  public VDTree parse(InputStream is) {
    try {
      final VDTree tree = new VDTree();
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser sp = spf.newSAXParser();
      XMLReader xr = sp.getXMLReader();

      xr.setContentHandler(new ContentHandler() {
        String space = " ";

        @Override
        public void setDocumentLocator(Locator locator) {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startPrefixMapping(String s, String s2) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String s) throws SAXException {
        }

        @Override
        public void startElement(String s, String s2, String s3, Attributes attributes)
            throws SAXException {
          String name = s3;
          if (tagSwitch.containsKey(name)) {
            tagSwitch.get(name).parse(tree, attributes);
          }
          space += " ";
        }

        @Override
        public void endElement(String s, String s2, String s3) throws SAXException {
          space = space.substring(1);
        }

        @Override
        public void characters(char[] chars, int i, int i2) throws SAXException {
        }

        @Override
        public void ignorableWhitespace(char[] chars, int i, int i2) throws SAXException {
        }

        @Override
        public void processingInstruction(String s, String s2) throws SAXException {
        }

        @Override
        public void skippedEntity(String s) throws SAXException {
        }
      });
      xr.parse(new InputSource(is));
      tree.parseFinish();
      return tree;
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  int rgbInterpolate(float t, int color1, int color2) {
    int ret;
    if (color1 == color2) {
      return color2;
    }
    if (color1 == 0) {
      return color2;
    }
    if (color2 == 0) {
      return color1;
    }

    float t1 = 1 - t;
    ret = 0xFF & (((int) ((color1 & 0xFF) * t1 + (color2 & 0xFF) * t)));
    color1 >>= 8;
    color2 >>= 8;

    ret |= 0xFF00 & (((int) ((color1 & 0xFF) * t1 + (color2 & 0xFF) * t)) << 8);
    color1 >>= 8;
    color2 >>= 8;
    ret |= 0xFF0000 & (((int) ((color1 & 0xFF) * t1 + (color2 & 0xFF) * t)) << 16);
    color1 >>= 8;
    color2 >>= 8;
    ret |= 0xFF000000 & (((int) ((color1 & 0xFF) * t1 + (color2 & 0xFF) * t)) << 24);

    return ret;
  }

  public VDParser() {

  }

  ////////////////////////////////////////////////
  // Copy from PathParser.java
  private static int nextStart(String s, int end) {
    char c;

    while (end < s.length()) {
      c = s.charAt(end);
      // Note that 'e' or 'E' are not valid path commands, but could be
      // used for floating point numbers' scientific notation.
      // Therefore, when searching for next command, we should ignore 'e'
      // and 'E'.
      if ((((c - 'A') * (c - 'Z') <= 0) || ((c - 'a') * (c - 'z') <= 0))
          && c != 'e' && c != 'E') {
        return end;
      }
      end++;
    }
    return end;
  }

  /**
   * calculate the position of the next comma or space
   *
   * @param s the string to search
   * @param start the position to start searching
   * @return the position of the next comma or space or -1 if none found
   */
  private static int extract(String s, int start) {
    // All the spaces will be replaced as ','
    s = s.replaceAll("\\s", ",");

    int dot = s.indexOf('.', start);
    int nextdot = -1;
    if (dot != -1) {
      nextdot = s.indexOf('.', dot + 1);
    }
    int comma = s.indexOf(',', start);

    if (nextdot == -1) {
      return comma;
    }
    if (comma == -1) {
      return (nextdot - 1);
    }
    return (comma > nextdot) ? (nextdot - 1) : comma;
  }

  public static VDPath.Node[] parsePath(String value) {
    int start = 0;
    int end = 1;

    ArrayList<VDPath.Node> list = new ArrayList<VDPath.Node>();
    while (end < value.length()) {
      end = nextStart(value, end);
      String s = value.substring(start, end);
      float[] val = getFloats(s);

      addNode(list, s.charAt(0), val);

      start = end;
      end++;
    }
    if ((end - start) == 1 && start < value.length()) {

      addNode(list, value.charAt(start), new float[0]);
    }
    return list.toArray(new VDPath.Node[list.size()]);
  }

  private static class ExtractFloatResult {

    // We need to return the position of the next separator and whether the
    // next float starts with a '-' or a '.'.
    int mEndPosition;
    boolean mEndWithNegOrDot;
  }

  /**
   * Copies elements from {@code original} into a new array, from indexes start (inclusive) to end
   * (exclusive). The original order of elements is preserved. If {@code end} is greater than {@code
   * original.length}, the result is padded with the value {@code 0.0f}.
   *
   * @param original the original array
   * @param start the start index, inclusive
   * @param end the end index, exclusive
   * @return the new array
   * @throws ArrayIndexOutOfBoundsException if {@code start < 0 || start > original.length}
   * @throws IllegalArgumentException if {@code start > end}
   * @throws NullPointerException if {@code original == null}
   */
  private static float[] copyOfRange(float[] original, int start, int end) {
    if (start > end) {
      throw new IllegalArgumentException();
    }
    int originalLength = original.length;
    if (start < 0 || start > originalLength) {
      throw new ArrayIndexOutOfBoundsException();
    }
    int resultLength = end - start;
    int copyLength = Math.min(resultLength, originalLength - start);
    float[] result = new float[resultLength];
    System.arraycopy(original, start, result, 0, copyLength);
    return result;
  }

  /**
   * Calculate the position of the next comma or space or negative sign
   *
   * @param s the string to search
   * @param start the position to start searching
   * @param result the result of the extraction, including the position of the the starting position
   * of next number, whether it is ending with a '-'.
   */
  private static void extract(String s, int start, ExtractFloatResult result) {
    // Now looking for ' ', ',', '.' or '-' from the start.
    int currentIndex = start;
    boolean foundSeparator = false;
    result.mEndWithNegOrDot = false;
    boolean secondDot = false;
    boolean isExponential = false;
    for (; currentIndex < s.length(); currentIndex++) {
      boolean isPrevExponential = isExponential;
      isExponential = false;
      char currentChar = s.charAt(currentIndex);
      switch (currentChar) {
        case ' ':
        case ',':
          foundSeparator = true;
          break;
        case '-':
          // The negative sign following a 'e' or 'E' is not a separator.
          if (currentIndex != start && !isPrevExponential) {
            foundSeparator = true;
            result.mEndWithNegOrDot = true;
          }
          break;
        case '.':
          if (!secondDot) {
            secondDot = true;
          } else {
            // This is the second dot, and it is considered as a separator.
            foundSeparator = true;
            result.mEndWithNegOrDot = true;
          }
          break;
        case 'e':
        case 'E':
          isExponential = true;
          break;
      }
      if (foundSeparator) {
        break;
      }
    }
    // When there is nothing found, then we put the end position to the end
    // of the string.
    result.mEndPosition = currentIndex;
  }

  /**
   * parse the floats in the string this is an optimized version of parseFloat(s.split(",|\\s"));
   *
   * @param s the string containing a command and list of floats
   * @return array of floats
   */
  private static float[] getFloats(String s) {
    if (s.charAt(0) == 'z' | s.charAt(0) == 'Z') {
      return new float[0];
    }
    try {
      float[] results = new float[s.length()];
      int count = 0;
      int startPosition = 1;
      int endPosition = 0;

      ExtractFloatResult result = new ExtractFloatResult();
      int totalLength = s.length();

      // The startPosition should always be the first character of the
      // current number, and endPosition is the character after the current
      // number.
      while (startPosition < totalLength) {
        extract(s, startPosition, result);
        endPosition = result.mEndPosition;

        if (startPosition < endPosition) {
          results[count++] = Float.parseFloat(
              s.substring(startPosition, endPosition));
        }

        if (result.mEndWithNegOrDot) {
          // Keep the '-' or '.' sign with next number.
          startPosition = endPosition;
        } else {
          startPosition = endPosition + 1;
        }
      }
      return copyOfRange(results, 0, count);
    } catch (NumberFormatException e) {
      throw new RuntimeException("error in parsing \"" + s + "\"", e);
    }
  }

  // End of copy from PathParser.java
  ////////////////////////////////////////////////////////////////
  private static void addNode(ArrayList<VDPath.Node> list, char cmd, float[] val) {
    list.add(new VDPath.Node(cmd, val));
  }

  public VDTree parse(URL r) throws Exception {
    try {
      return parse(r.openStream());
    } catch (Exception e) {
      System.err.println(r);
      throw e;
    }
  }

  private void parsePortSize(VDTree apath, Attributes attributes) {
    int len = attributes.getLength();
    for (int i = 0; i < len; i++) {
      String name = attributes.getQName(i);
      String value = attributes.getValue(i);
      if ("android:viewportWidth".equals(name)) {
        apath.mPortWidth = Float.parseFloat(value);
        ;
      } else if ("android:viewportHeight".equals(name)) {
        apath.mPortHeight = Float.parseFloat(value);
        ;
      } else {
        continue;
      }

    }

  }

  private void parseSize(VDTree apath, Attributes attributes) {

    Pattern pattern = Pattern.compile("^\\s*(\\d+(\\.\\d+)*)\\s*([a-zA-Z]+)\\s*$");
    HashMap<String, Integer> m = new HashMap<String, Integer>();
    m.put("px", 1);
    m.put("dip", 1);
    m.put("dp", 1);
    m.put("sp", 1);
    m.put("pt", 1);
    m.put("in", 1);
    m.put("mm", 1);
    int len = attributes.getLength();

    for (int i = 0; i < len; i++) {
      String name = attributes.getQName(i);
      String value = attributes.getValue(i);
      Matcher matcher = pattern.matcher(value);
      float size = 0;
      if (matcher.matches()) {
        float v = Float.parseFloat(matcher.group(1));
        String unit = matcher.group(3).toLowerCase();
        size = v;
      }
      // -- Extract dimension units.

      if ("android:width".equals(name)) {
        apath.mBaseWidth = size;
      } else if ("android:height".equals(name)) {
        apath.mBaseHeight = size;
      } else if ("android:viewportWidth".equals(name)) {
        apath.mPortWidth = Float.parseFloat(value);
        ;
      } else if ("android:viewportHeight".equals(name)) {
        apath.mPortHeight = Float.parseFloat(value);
        ;
      } else {
        continue;
      }

    }
  }

  private VDPath parsePathAttributes(Attributes attributes) {
    int len = attributes.getLength();
    VDPath vgPath = new VDPath();

    for (int i = 0; i < len; i++) {
      String name = attributes.getQName(i);
      String value = attributes.getValue(i);
      logger.log(Level.FINE, "name " + name + "value " + value);
      setNameValue(vgPath, name, value);
    }
    return vgPath;
  }

  private VDGroup parseGroupAttributes(Attributes attributes) {
    int len = attributes.getLength();
    VDGroup vgGroup = new VDGroup();

    for (int i = 0; i < len; i++) {
      String name = attributes.getQName(i);
      String value = attributes.getValue(i);
      logger.log(Level.FINE, "name " + name + "value " + value);
      // setNameValue(vgGroup, name, value);
    }
    return vgGroup;
  }

  public void setNameValue(VDPath vgPath, String name, String value) {

    if (PATH_DESCRIPTION.equals(name)) {
      vgPath.mNode = parsePath(value);

    } else if (PATH_ID.equals(name)) {
      vgPath.mId = value;
    } else if (PATH_FILL.equals(name)) {
      vgPath.mFillColor = calculateColor(value);
      if (!Float.isNaN(vgPath.mFillOpacity)) {
        vgPath.mFillColor &= 0x00FFFFFF;
        vgPath.mFillColor |= ((int) (0xFF * vgPath.mFillOpacity)) << 24;
      }
    } else if (PATH_STROKE.equals(name)) {
      vgPath.mStrokeColor = calculateColor(value);
      if (!Float.isNaN(vgPath.mStrokeOpacity)) {
        vgPath.mStrokeColor &= 0x00FFFFFF;
        vgPath.mStrokeColor |= ((int) (0xFF * vgPath.mStrokeOpacity)) << 24;
      }
    } else if (PATH_FILL_OPACTIY.equals(name)) {
      vgPath.mFillOpacity = Float.parseFloat(value);
      vgPath.mFillColor &= 0x00FFFFFF;
      vgPath.mFillColor |= ((int) (0xFF * vgPath.mFillOpacity)) << 24;
    } else if (PATH_STROKE_OPACTIY.equals(name)) {
      vgPath.mStrokeOpacity = Float.parseFloat(value);
      vgPath.mStrokeColor &= 0x00FFFFFF;
      vgPath.mStrokeColor |= ((int) (0xFF * vgPath.mStrokeOpacity)) << 24;
    } else if (PATH_STROKE_WIDTH.equals(name)) {
      vgPath.mStrokeWidth = Float.parseFloat(value);
    } else if (PATH_ROTATION.equals(name)) {
      vgPath.mRotate = Float.parseFloat(value);
    } else if (PATH_SHIFT_X.equals(name)) {
      vgPath.mShiftX = Float.parseFloat(value);
    } else if (PATH_SHIFT_Y.equals(name)) {
      vgPath.mShiftY = Float.parseFloat(value);
    } else if (PATH_ROTATION_Y.equals(name)) {
      vgPath.mRotateY = Float.parseFloat(value);
    } else if (PATH_ROTATION_X.equals(name)) {
      vgPath.mRotateX = Float.parseFloat(value);
    } else if (PATH_CLIP.equals(name)) {

      vgPath.mClip = Boolean.parseBoolean(value);

    } else if (PATH_TRIM_START.equals(name)) {
      vgPath.mTrimPathStart = Float.parseFloat(value);
    } else if (PATH_TRIM_END.equals(name)) {
      vgPath.mTrimPathEnd = Float.parseFloat(value);
    } else if (PATH_TRIM_OFFSET.equals(name)) {
      vgPath.mTrimPathOffset = Float.parseFloat(value);
    } else if (PATH_STROKE_LINECAP.equals(name)) {
      if (LINECAP_BUTT.equals(value)) {
        vgPath.mStrokelineCap = 0;
      } else if (LINECAP_ROUND.equals(value)) {
        vgPath.mStrokelineCap = 1;
      } else if (LINECAP_SQUARE.equals(value)) {
        vgPath.mStrokelineCap = 2;
      }
    } else if (PATH_STROKE_LINEJOIN.equals(name)) {
      if (LINEJOIN_MITER.equals(value)) {
        vgPath.mStrokelineCap = 0;
      } else if (LINEJOIN_ROUND.equals(value)) {
        vgPath.mStrokelineCap = 1;
      } else if (LINEJOIN_BEVEL.equals(value)) {
        vgPath.mStrokelineCap = 2;
      }
    } else if (PATH_STROKE_MITERLIMIT.equals(name)) {
      vgPath.mStrokeMiterlimit = Float.parseFloat(value);
    } else if (FILL_TYPE.equals(name)) {
      vgPath.mFillType = ("evenOdd".equals(value) ? Path2D.WIND_EVEN_ODD : Path2D.WIND_NON_ZERO);
    } else {
      System.err.println(">>>>>> DID NOT UNDERSTAND ! \"" + name + "\" <<<<");
    }

  }

  private int calculateColor(String value) {
    int len = value.length();
    int ret;
    int k = 0;
    switch (len) {
      case 7: // #RRGGBB
        ret = (int) Long.parseLong(value.substring(1), 16);
        ret |= 0xFF000000;
        break;
      case 9: // #AARRGGBB
        ret = (int) Long.parseLong(value.substring(1), 16);
        break;
      case 4: // #RGB
        ret = (int) Long.parseLong(value.substring(1), 16);

        k |= ((ret >> 8) & 0xF) * 0x110000;
        k |= ((ret >> 4) & 0xF) * 0x1100;
        k |= ((ret) & 0xF) * 0x11;
        ret = k | 0xFF000000;
        break;
      case 5: // #ARGB
        ret = (int) Long.parseLong(value.substring(1), 16);
        k |= ((ret >> 16) & 0xF) * 0x11000000;
        k |= ((ret >> 8) & 0xF) * 0x110000;
        k |= ((ret >> 4) & 0xF) * 0x1100;
        k |= ((ret) & 0xF) * 0x11;
        break;
      default:
        return 0xFF000000;
    }

    logger.log(Level.FINE, "color = " + value + " = " + Integer.toHexString(ret));
    return ret;
  }
}
