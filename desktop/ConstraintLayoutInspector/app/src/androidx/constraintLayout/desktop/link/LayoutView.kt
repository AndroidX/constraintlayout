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

import androidx.constraintLayout.desktop.scan.WidgetFrameUtils
import androidx.constraintLayout.desktop.utils.Desk
import androidx.constraintlayout.core.parser.CLKey
import androidx.constraintlayout.core.parser.CLObject
import androidx.constraintlayout.core.parser.CLParser
import androidx.constraintlayout.core.state.WidgetFrame
import androidx.constraintlayout.core.widgets.ConstraintWidget
import java.awt.*
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D
import javax.swing.JFrame
import javax.swing.JPanel

class LayoutView : JPanel(BorderLayout()) {
    var widgets = ArrayList<Widget>()
    var zoom = 0.9f

    data class Widget(val id: String, val key: CLKey) {
        var interpolated = WidgetFrame()
        var start = WidgetFrame()
        var end = WidgetFrame()
        var name = "unknown";
        var path = Path2D.Float()
        val drawFont = Font("Helvetica", Font.ITALIC, 32)

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
                    "path" -> WidgetFrameUtils.getPath(sec, path);
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

    override fun paint(g: Graphics?) {
        super.paint(g)
        if (widgets.size == 0) {
            return
        }
        val root = widgets[0]
        var rootWidth = root.width().toFloat()
        var rootHeight = root.height().toFloat()
        var scaleX = width / rootWidth
        var scaleY = height / rootHeight
        var offX = 0.0f
        var offY = 0.0f
        if (scaleX < scaleY) {
            scaleY = scaleX
        } else {
            scaleX = scaleY
        }

        scaleX *= zoom
        scaleY *= zoom
        offX = (width - root.width().toFloat() * scaleX) / 2
        offY = (height - root.height().toFloat() * scaleY) / 2

        g!!.color = WidgetFrameUtils.theme.backgroundColor()
        g!!.fillRect(0, 0, width, height)

        val oG = g.create() as Graphics2D
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(offX.toDouble(), offY.toDouble())
        g2.scale(scaleX.toDouble(), scaleY.toDouble())

        for (widget in widgets) {
            widget.draw(g2, widget == root)
        }

        oG.translate(offX.toDouble(), offY.toDouble())
        for (guideline in horizontalGuidelines) {
            guideline.draw(oG, (rootWidth * scaleX).toInt(), (rootHeight * scaleY).toInt())
        }

        for (guideline in verticalGuidelines) {
            guideline.draw(oG, (rootWidth * scaleX).toInt(), (rootHeight * scaleY).toInt())
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
        } catch (e : Exception) {}
    }

    fun setModel(model: CLObject) {
        if (model.has("ConstraintSets")) {
            // For now don't operate on MotionScenes
            return
        }
        clearGuidelines()
        var count = model.size()
        for (i in 0 until count) {
            var element = model[i]
            if (element is CLKey) {
                val value = element.value
                if (value is CLObject && value.has("type")) {
                    var type = value.getString("type")
                    when (type) {
                        "hGuideline" -> addGuideline(element.content(), value, ConstraintWidget.HORIZONTAL)
                        "vGuideline" -> addGuideline(element.content(), value, ConstraintWidget.VERTICAL)
                    }
                }
            }
        }
    }

    open class GuidelineModel(val id: String, val p : Float = 0f) {
        var name = id
        var percent = p
    }

    class HorizontalGuideline(val key: String, element: CLObject) : GuidelineModel(key, element.getFloat("percent")) {

        init {
            val x1Points = intArrayOf(0, 100, 0, 100)
            val y1Points = intArrayOf(0, 50, 50, 0)
            val polygon = GeneralPath(
                GeneralPath.WIND_EVEN_ODD,
                x1Points.size
            )
            polygon.moveTo(x1Points[0], y1Points[0])
        }
        fun draw(g: Graphics2D, w: Int, h: Int) {
            g.setColor(Color.RED)
            val y = (h * percent).toInt()
            g.drawLine(0, y, w, y)

            var path = Path2D()
            Path2D path =
            g.drawRect()
        }
    }

    class VerticalGuideline(val key: String, element: CLObject) : GuidelineModel(key, element.getFloat("percent")) {
        fun draw(g: Graphics2D, w: Int, h: Int) {
            g.setColor(Color.RED)
            val x = (w * percent).toInt()
            g.drawLine(x, 0, x, h)
        }
    }

    var horizontalGuidelines = ArrayList<HorizontalGuideline>()
    var verticalGuidelines = ArrayList<VerticalGuideline>()

    private fun clearGuidelines() {
        horizontalGuidelines.clear()
        verticalGuidelines.clear()
    }

    private fun addGuideline(name: String, element: CLObject, orientation: Int) {
        when (orientation) {
            ConstraintWidget.HORIZONTAL -> horizontalGuidelines.add(HorizontalGuideline(name, element))
            ConstraintWidget.VERTICAL -> verticalGuidelines.add(VerticalGuideline(name, element))
        }
    }

    companion object {

        fun showLayoutView(link: MotionLink, callback: Main.DesignSurfaceModification): LayoutView? {
            val frame = JFrame("Layout Inspector")
            val inspector = LayoutInspector(link)
            frame.contentPane = inspector
            Desk.rememberPosition(frame, null)
            frame.isVisible = true
            inspector.designSurfaceModificationCallback = callback
            return inspector.layoutView
        }
    }
}