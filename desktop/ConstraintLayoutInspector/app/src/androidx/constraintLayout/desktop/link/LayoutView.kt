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

import androidx.constraintLayout.desktop.scan.KeyFrameNodes
import androidx.constraintLayout.desktop.scan.WidgetFrameUtils
import androidx.constraintlayout.core.parser.CLKey
import androidx.constraintlayout.core.parser.CLObject
import androidx.constraintlayout.core.parser.CLParser
import androidx.constraintlayout.core.state.WidgetFrame
import java.awt.*
import java.awt.geom.Path2D
import javax.swing.JPanel

open class LayoutView : JPanel(BorderLayout()) {
    protected var widgets = ArrayList<Widget>()
    var zoom = 0.85f

    private var rootWidth: Float = 0f
    private var rootHeight: Float = 0f

    protected var lastRootWidth: Float = 0f
    protected var lastRootHeight: Float = 0f

    protected var scaleX = 0f
    protected var scaleY = 0f
    protected var offX = 0.0f
    protected var offY = 0.0f


    data class Widget(val id: String, val key: CLKey) {
        var interpolated = WidgetFrame()
        var start = WidgetFrame()
        var end = WidgetFrame()
        var name = "unknown";
        var path = Path2D.Float()
        val drawFont = Font("Helvetica", Font.ITALIC, 32)
        val keyFrames = KeyFrameNodes()
        var isGuideline = false

        init {
            name = key.content()
            val sections = key.value as CLObject
            val count = sections.size()
            for (i in 0 until count) {
                val sec = sections[i] as CLKey
                when (sec.content()) {
                    "start" -> WidgetFrameUtils.deserialize(sec, end)
                    "end" -> WidgetFrameUtils.deserialize(sec, start)
                    "interpolated" -> WidgetFrameUtils.deserialize(sec, interpolated)
                    "path" -> WidgetFrameUtils.getPath(sec, path)
                    "keyPos" -> keyFrames.setKeyFramesPos(sec)
                    "keyTypes" -> keyFrames.setKeyFramesTypes(sec)
                    "keyFrames" -> keyFrames.setKeyFramesProgress(sec)
                }
            }
        }

        fun width(): Int {
            return interpolated.width()
        }

        fun height(): Int {
            return interpolated.height()
        }

        fun draw(g: Graphics2D, drawRoot: Boolean) {

            val END_LOOK = WidgetFrameUtils.OUTLINE or WidgetFrameUtils.DASH_OUTLINE;
            g.color = WidgetFrameUtils.theme.startColor()
            WidgetFrameUtils.render(start, g, END_LOOK);
            keyFrames.render(g)
            g.color = WidgetFrameUtils.theme.endColor()
            WidgetFrameUtils.render(end, g, END_LOOK);

            g.color = WidgetFrameUtils.theme.pathColor()
            WidgetFrameUtils.renderPath(path, g);

            g.color = WidgetFrameUtils.theme.interpolatedColor()
            var style = WidgetFrameUtils.FILL
            if (drawRoot) {
                g.color = WidgetFrameUtils.theme.rootBackgroundColor()
            }
            g.font = drawFont
            style += WidgetFrameUtils.TEXT
            interpolated.name = name
            WidgetFrameUtils.render(interpolated, g, style);

        }
    }

    open fun computeScale(rootWidth : Float, rootHeight: Float) {
        lastRootWidth = rootWidth
        lastRootHeight = rootHeight
        scaleX = width / rootWidth
        scaleY = height / rootHeight

        scaleX *= zoom
        scaleY *= zoom

        if (scaleX < scaleY) {
            scaleY = scaleX
        } else {
            scaleX = scaleY
        }

        offX = (width - rootWidth * scaleX) / 2
        offY = (height - rootHeight * scaleY) / 2

    }

    override fun paint(g: Graphics?) {
        super.paint(g)
        if (widgets.size == 0) {
            return
        }
        val root = widgets[0]
        rootWidth = root.width().toFloat()
        rootHeight = root.height().toFloat()
        computeScale(rootWidth, rootHeight)

        val g2 = g!!.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        g2.color = WidgetFrameUtils.theme.backgroundColor()
        g2.fillRect(0, 0, width, height)

        g2.translate(offX.toDouble(), offY.toDouble())
        g2.scale(scaleX.toDouble(), scaleY.toDouble())

        for (widget in widgets) {
            if (widget.isGuideline) {
                continue
            }
            widget.draw(g2, widget == root)
        }
    }

    fun setLayoutInformation(information: String) {
        if (information.trim().isEmpty()) {
            return
        }

        try {
            val list = CLParser.parse(information)
            widgets.clear()

            for (i in 0 until list.size()) {
                val widget = list[i]

                if (widget is CLKey) {
                    val widgetId = widget.content()

                    widgets.add(Widget(widgetId, widget))
                }
            }

            repaint()
        } catch (e : Exception) { e.printStackTrace() }
    }
}
