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

package androidx.constraintLayout.desktop.link

import androidx.constraintLayout.desktop.link.Main.DesignSurfaceModification
import androidx.constraintlayout.core.parser.CLObject
import java.awt.BorderLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.lang.StringBuilder
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JSlider

class LayoutInspector(link: MotionLink) : JPanel(BorderLayout()) {

    val layoutView = LayoutView()
    val motionLink = link
    var forceDimension = false
    var designSurfaceModificationCallback: DesignSurfaceModification? = null

    init {
        val northPanel = JPanel()
        val toggle = JButton("link resize")
        val liveConnection = JCheckBox("Live connection")
        val slider = JSlider(0, 100)
        liveConnection.isSelected = true

        northPanel.add(toggle)
        northPanel.add(liveConnection)
        northPanel.add(slider)

        add(northPanel, BorderLayout.NORTH)
        add(layoutView, BorderLayout.CENTER)

        slider.addChangeListener {
            var value = slider.value / 100f
            if (designSurfaceModificationCallback != null) {
                var element = designSurfaceModificationCallback!!.getElement("g1")
                if (element is CLObject) {
                    element.putNumber("percent", value)
                    designSurfaceModificationCallback?.updateElement("g1", element)
                }
            }
        }

        liveConnection.addChangeListener {
            motionLink.setUpdateLayoutPolling(liveConnection.isSelected)
        }

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                if (forceDimension) {
                    motionLink.sendLayoutDimensions(width * 3, height * 3)
                }
            }
        })

        toggle.addActionListener {
            forceDimension = !forceDimension
            if (forceDimension) {
                motionLink.sendLayoutDimensions(width * 3, height * 3)
                toggle.text = "Driving root dimension..."
            } else {
                motionLink.sendLayoutDimensions(Int.MIN_VALUE, Int.MIN_VALUE)
                toggle.text = "link resize"
            }
        }
    }

}