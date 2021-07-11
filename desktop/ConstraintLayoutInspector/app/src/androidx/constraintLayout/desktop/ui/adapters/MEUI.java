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

import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;

public class MEUI {

  static float userScaleFactor = 1;

  public static int scale(int i) {
    return Math.round(userScaleFactor * i);
  }

  public static MEDimension size(int width, int height) {
    return new MEDimension(width, height);
  }

  static boolean dark = false;

  public static Color makeColor(int rgb, int darkRGB) {
    return !dark ? new Color(rgb) : new Color(darkRGB);
  }

  private static Color makeColor(String name, int rgb, int darkRGB) {
    return makeColor(rgb, darkRGB);
  }

  public static int ourLeftColumnWidth = scale(150);
  public static int ourHeaderHeight = scale(30);

  public static Insets insets(int top, int left, int bottom, int right) {
    return new Insets(top, left, bottom, right);
  }

  public static MEComboBox<String> makeComboBox(String[] a) {
    return new MEComboBox<String>(a);
  }

  public static void invokeLater(Runnable runnable) {
    SwingUtilities.invokeLater(runnable);
  }

  public static int ourGraphHeight = scale(60);

   public static Color ourMySelectedTextColor = makeColor("UIDesigner.motion.SelectedTextColor", 0xEAEAEA, 0xff333333);
//  public static Color myTimeCursorColor = makeColor("UIDesigner.motion.TimeCursorColor", 0xff3d81e1, 0xff3d81e1);
//  public static Color myTimeCursorStartColor = makeColor("UIDesigner.motion.TimeCursorStartColor", 0xff3da1f1, 0xff3dd1f1);
//  public static Color myTimeCursorEndColor = makeColor("UIDesigner.motion.TimeCursorEndColor", 0xff3da1f1, 0xff3dd1f1);
//  public static Color myGridColor = new Color(0xff838383);
//  public static Color myUnSelectedLineColor = new Color(0xe0759a);
//  public static Color ourMySelectedKeyColor = makeColor("UIDesigner.motion.SelectedKeyColor", 0xff3da1f1, 0xff3dd1f1);
//  public static Color ourMySelectedLineColor = new Color(0x3879d9);
//  public static Color ourPrimaryPanelBackground = makeColor("UIDesigner.motion.PrimaryPanelBackground", 0xf5f5f5, 0x2D2F31);
//  public static Color ourSecondaryPanelBackground = makeColor("UIDesigner.motion.ourSelectedLineColor", 0xfcfcfc, 0x313435);
//  public static Color ourAvgBackground = makeColor("UIDesigner.motion.ourAvgBackground", 0xf8f8f8, 0x2f3133);
//  public static Color ourBorder = makeColor("UIDesigner.motion.ourBorder", 0xc9c9c9, 0x242627);
//  public static Color ourBorderLight = makeColor("BorderLight", 0xe8e6e6, 0x3c3f41);
//  public static Color ourAddConstraintColor = makeColor("UIDesigner.motion.AddConstraintColor", 0xff838383, 0xff666666);
//  public static Color ourTextColor = makeColor("UIDesigner.motion.TextColor", 0x2C2C2C, 0x9E9E9E);
//  public static Color ourAddConstraintPlus = makeColor("UIDesigner.motion.AddConstraintPlus", 0xffc9c9c9, 0xff333333);
//  public static Color ourGraphColor = makeColor("UIDesigner.motion.GraphColor", 0x97b1c0, 0x97b1c0);
  public static final Color ourErrorColor = makeColor("UIDesigner.motion.Error.foreground", 0x8f831b, 0xffa31b);
  public static final Color ourBannerColor = makeColor("UIDesigner.motion.Notification.background", 0xfff8d1, 0x1d3857);
  public static final Color myTimeCursorColor = makeColor("UIDesigner.motion.TimeCursor.selectedForeground", 0xFF4A81FF, 0xFFB4D7FF);
  public static final Color myGridColor = makeColor("UIDesigner.motion.timeLine.disabledBorderColor", 0xDDDDDD, 0x555555);
  public static final Color ourMySelectedKeyColor = makeColor("UIDesigner.motion.Key.selectedForeground", 0xff3da1f1, 0xff3dd1f1);
  public static final Color ourPrimaryPanelBackground = makeColor("UIDesigner.motion.PrimaryPanel.background", 0xf5f5f5, 0x2D2F31);
  public static final Color ourSecondaryPanelBackground = makeColor("UIDesigner.motion.SecondaryPanel.background", 0xfcfcfc, 0x313435);
  public static final Color ourAvgBackground = makeColor("UIDesigner.motion.ourAvg.background", 0xf8f8f8, 0x2f3133);
  public static final Color ourBorder = makeColor("UIDesigner.motion.borderColor", 0xc9c9c9, 0x242627);
  public static final Color ourBorderLight = makeColor("UIDesigner.motion.light.borderColor", 0xe8e6e6, 0x3c3f41);
  public static final Color ourTextColor = makeColor("UIDesigner.motion.Component.foreground", 0x2C2C2C, 0x9E9E9E);
  public static final Color ourSecondaryPanelHeaderTitleColor = makeColor("UIDesigner.motion.SecondaryPanel.header.foreground", 0x000000, 0xbababa);
  public static final Color ourSecondaryHeaderBackgroundColor = makeColor("UIDesigner.motion.SecondaryPanel.header.background", 0xf2f2f2, 0x3c3f40);
  //Do we need these below?
  public static final Color myTimeCursorStartColor =
      makeColor("UIDesigner.motion.TimeCursor.Start.selectedForeground", 0xff3da1f1, 0xff3dd1f1);
  public static final Color myTimeCursorEndColor = makeColor("UIDesigner.motion.TimeCursor.End.selectedForeground", 0xff3da1f1, 0xff3dd1f1);
  public static final Color myUnSelectedLineColor = new Color(0xe0759a);
  public static final Color ourMySelectedLineColor = new Color(0x3879d9);
  public static final Color ourAddConstraintColor = makeColor("UIDesigner.motion.AddConstraintColor", 0xff838383, 0xff666666);
  public static final Color ourAddConstraintPlus = makeColor("UIDesigner.motion.AddConstraintPlus", 0xffc9c9c9, 0xff333333);


  public static void copy(MTag tag) {
  }

  public static void cut(MTag mSelectedKeyFrame) {
  }



  public static final int DIR_LEFT = 0;
  public static final int DIR_RIGHT = 1;
  public static final int DIR_TOP = 2;
  public static final int DIR_BOTTOM = 3;

  public static JButton createToolBarButton(Icon icon, String tooltip) {
    return createToolBarButton(icon, null, tooltip);
  }

  public static JButton createToolBarButton(Icon icon, Icon disabledIcon, String tooltip) {
    JButton button = new JButton(icon);
    if (disabledIcon != null) {
      button.setDisabledIcon(disabledIcon);
    }
    button.setFocusable(false);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setToolTipText(tooltip);
    button.setBorder(null);
    button.setUI(new BasicButtonUI());

    return button;
  }
  public static void addCopyPaste(ActionListener copyListener, ActionListener pasteListener, JComponent panel) {
    // TODO ideally support paste and copy with control or command
    KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false);
    KeyStroke copy2 = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_MASK, false);
    KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK, false);
    KeyStroke paste2 = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_MASK, false);
    panel.registerKeyboardAction(copyListener, "Copy", copy, JComponent.WHEN_FOCUSED);
    panel.registerKeyboardAction(copyListener, "Copy", copy2, JComponent.WHEN_FOCUSED);
    panel.registerKeyboardAction(pasteListener, "Paste", paste, JComponent.WHEN_FOCUSED);
    panel.registerKeyboardAction(pasteListener, "Paste", paste2, JComponent.WHEN_FOCUSED);
  }
  public static Font getToolBarButtonSmallFont() {
    return new JButton().getFont().deriveFont(12f);
  }
  public static final Color getBorderColor() {
    return makeColor("Borders.color" , 0x10101*192, 0x10101*50);
  }
  public static Border getPanelBottomBorder() {
    return new LineBorder(MEUI.getBorderColor(),1,false);
  }

  public static Insets dialogTitleInsets() { return MEUI.insets(8, 12, 0, 12); }
  public static Insets dialogSeparatorInsets() { return MEUI.insets(8, 0, 0, 0); }
  public static Insets dialogLabelInsets() { return MEUI.insets(8, 12, 0, 12); }
  public static Insets dialogControlInsets() { return MEUI.insets(4, 14, 0, 12); }
  public static Insets dialogBottomButtonInsets() { return MEUI.insets(12, 12, 12, 12); }

  public static BufferedImage createImage(int w, int h, int typeIntArgb) {
    return new BufferedImage(w, h, typeIntArgb);
  }

  public static Icon generateImageIcon(BufferedImage image) {
    return new ImageIcon(image);
  }

  public interface Popup {
    void dismiss();

    void hide();

    void show();
  }

  public static class Overview {
    public static final Color ourCS = makeColor("UIDesigner.motion.ConstraintSet.background", 0xFFFFFF, 0x515658);
    public static final Color ourCSText = makeColor("UIDesigner.motion.ConstraintSetText.foreground", 0x000000, 0xC7C7C7);
    public static final Color ourCS_Background = makeColor("UIDesigner.motion.ourCS.background", 0xFFFFFF, 0x515658);
    public static final Color ourCS_Hover = makeColor("UIDesigner.motion.HoverColor.disabledBackground", 0XEAF2FE, 0X6E869B);
    public static final Color ourCS_SelectedBackground =
        makeColor("UIDesigner.motion.ourCS_SelectedBackground.selectionInactiveBackground", 0xD3D3D3, 0x797B7C);
    public static final Color ourCS_SelectedFocusBackground =
        makeColor("UIDesigner.motion.ourCS_SelectedFocusBackground.selectionForeground", 0xD1E7FD, 0x7691AB);


    public static final Color ourCS_Border = makeColor("UIDesigner.motion.ourCS_Border.borderColor", 0xBEBEBE, 0x6D6D6E);
    public static final Color ourCS_HoverBorder = makeColor("UIDesigner.motion.hoverBorderColor", 0x7A7A7A, 0xA1A1A1);
    public static final Color ourCS_SelectedBorder =
        makeColor("UIDesigner.motion.ourCS_SelectedBorder.pressedBorderColor", 0x7A7A7A, 0xA1A1A1);
    public static final Color ourCS_SelectedFocusBorder =
        makeColor("UIDesigner.motion.ourCS_SelectedFocusBorder.focusedBorderColor", 0x1886F7, 0x9CCDFF);

    public static final Color ourCS_TextColor = makeColor("UIDesigner.motion.ourCS_TextColor.foreground", 0x686868, 0xc7c7c7);
    public static final Color ourCS_FocusTextColor = makeColor("UIDesigner.motion.cs_FocusText.infoForeground", 0x888888 , 0xC7C7C7);
    public static final Color ourML_BarColor = makeColor("UIDesigner.motion.ourML_BarColor.separatorColor", 0xd8d8d8, 0x808385);
    public static final Color ourPositionColor = makeColor("UIDesigner.motion.PositionMarkColor", 0XF0A732, 0XF0A732);
  }
  public static class Graph {
    public static final Color ourG_Background = makeColor("UIDesigner.motion.motionGraph.background", 0xfcfcfc, 0x313334);
    public static final Color ourG_line = makeColor("UIDesigner.motion.graphLine.lineSeparatorColor", 0xE66F9A, 0xA04E6C);
    public static final Color ourCursorTextColor = makeColor("UIDesigner.motion.CursorTextColor.foreground", 0xFFFFFF, 0x000000);
  }
  public static class CSPanel {
    public static final Color our_SelectedFocusBackground =
        makeColor("UIDesigner.motion.CSPanel.SelectedFocusBackground", 0x3973d6, 0x2E65CA);
    public static final Color our_SelectedBackground =
        makeColor("UIDesigner.motion.CSPanel.SelectedBackground", 0xD3D3D3, 0x0C283E);
  }
  public static JPopupMenu createPopupMenu() {
    JPopupMenu ret = new JPopupMenu();
    return ret;
  }

  public static Popup createPopup(JComponent component, JComponent local) {
    return new Popup() {
      private final JComponent myComponent = component;
      private final JComponent myLocal = local;

      @Override
      public void dismiss() {
        hide();
      }

      @Override
      public void hide() {

      }

      @Override
      public void show() {

      }

    };
  }

}
