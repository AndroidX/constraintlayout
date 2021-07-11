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

import androidx.constraintLayout.desktop.ui.adapters.vd.ListIcons;
import androidx.constraintLayout.desktop.ui.adapters.vg.VDIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;

public class MEIcons {

  public static final Icon SLOW_MOTION = new VDIcon(ListIcons.getStream("slow-motion.xml"));
  public static final Icon PLAY = new VDIcon(ListIcons.getStream("play.xml"));
  public static final Icon FORWARD = new VDIcon(ListIcons.getStream("go-to-end.xml"));
  public static final Icon BACKWARD = new VDIcon(ListIcons.getStream("go-to-start.xml"));
  public static final Icon LOOP = new VDIcon(ListIcons.getStream("loop.xml"));
  public static final Icon PAUSE = new VDIcon(ListIcons.getStream("pause.xml"));
  public static final Icon CREATE_TRANSITION = new VDIcon(ListIcons.getStream("create-transition.xml"));
  public static final Icon CREATE_CONSTRAINTSET = new VDIcon(ListIcons.getStream("create-constraintset.xml"));
  public static final Icon CREATE_ON_CLICK = new VDIcon(ListIcons.getStream("create-on-click.xml"));
  public static final Icon CREATE_ON_SWIPE = new VDIcon(ListIcons.getStream("create-on-swipe.xml"));
  public static final Icon CREATE_ON_STAR = new VDIcon(ListIcons.getStream("create-on-star.xml"));
  public static final Icon LIST_LAYOUT = new VDIcon(ListIcons.getStream("list_layout.xml"));
  public static final Icon LIST_STATE = new VDIcon(ListIcons.getStream("list_state.xml"));
  public static final Icon LIST_TRANSITION = new VDIcon(ListIcons.getStream("list_transition.xml"));
  public static final Icon VIEW_OVERVIEW_ONLY = new VDIcon(ListIcons.getStream("create-on-swipe.xml"));
  public static final Icon VIEW_LIST_ONLY = new VDIcon(ListIcons.getStream("create-on-swipe.xml"));
  public static final Icon VIEW_OVERVIEW_AND_LIST = new VDIcon(ListIcons.getStream("create-on-swipe.xml"));
  public static final Icon CYCLE_LAYOUT = new VDIcon(ListIcons.getStream("switch-panel.xml"));
  public static final Icon CREATE_MENU = new VDIcon(ListIcons.getStream("create-menu.xml"));
  public static final Icon EDIT_MENU = new VDIcon(ListIcons.getStream("edit-menu.xml"));
  public static final Icon EDIT_MENU_DISABLED = new VDIcon(ListIcons.getStream("edit-menu-disabled.xml"));
  public static final Icon LIST_GRAY_STATE = new VDIcon(ListIcons.getStream("list_gray_state.xml"));
  public static final Icon CREATE_KEYFRAME = new VDIcon(ListIcons.getStream("create-keyframe.xml"));

  // TODO fix icons...
  public static final Icon CONSTRAINT_SET = new VDIcon(ListIcons.getStream("create-keyframe.xml"));
  public static final Icon SMALL_DOWN_ARROW = new VDIcon(ListIcons.getStream("create-keyframe.xml"));
  public static final Icon LOOP_FORWARD =  new VDIcon(ListIcons.getStream("create-keyframe.xml"));
  public static final Icon LOOP_BACKWARD =  new VDIcon(ListIcons.getStream("create-keyframe.xml"));
  public static final Icon LOOP_YOYO = new VDIcon(ListIcons.getStream("create-keyframe.xml"));
  public static final Object LIST_STATE_DERIVED =   new VDIcon(ListIcons.getStream("create-keyframe.xml"));
  public static final Icon LIST_STATE_DERIVED_SELECTED =   new VDIcon(ListIcons.getStream("create-keyframe.xml"));
  public static final Icon LIST_STATE_SELECTED =   new VDIcon(ListIcons.getStream("create-keyframe.xml"));
  public static final Icon GESTURE =    new VDIcon(ListIcons.getStream("create-keyframe.xml"));;

  public static void main(String[] args) throws Exception {
    JFrame frame = new JFrame("DisplayDynamic");
    frame.setBounds(10, 10, 240, 240);
    JButton button = new JButton(SLOW_MOTION);
    frame.setContentPane(button);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);

  }

  public static Image getUnscaledIconImage(Icon icon) {
    int h = icon.getIconHeight();
    int w = icon.getIconWidth();
    BufferedImage image = new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);
    icon.paintIcon(null, image.createGraphics(),w,h);
    return   image;
  }
}
