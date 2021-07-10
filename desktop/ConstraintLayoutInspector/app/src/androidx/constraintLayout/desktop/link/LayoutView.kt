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
import androidx.constraintLayout.desktop.utils.ScenePicker
import androidx.constraintlayout.core.parser.CLKey
import androidx.constraintlayout.core.parser.CLObject
import androidx.constraintlayout.core.parser.CLParser
import androidx.constraintlayout.core.state.WidgetFrame
import androidx.constraintlayout.core.widgets.ConstraintWidget
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.geom.GeneralPath
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel
import java.io.IOException

import java.io.File

import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min


class LayoutView : JPanel(BorderLayout()) {
    var widgets = ArrayList<Widget>()
    var zoom = 0.9f
    var scenePicker = ScenePicker()
    var currentDragElement : GuidelineModel? = null

    var currentX : Int = 0
    var rangeX : Int = 0
    var offsetX : Int = 0

    var currentY : Int = 0
    var rangeY : Int = 0
    var offsetY : Int = 0

    var dragging : Boolean = false
    var designSurfaceModificationCallback: Main.DesignSurfaceModification? = null

    init {
        scenePicker.setSelectListener { over, dist ->
            currentDragElement = over as GuidelineModel
        }

        addMouseListener(object: MouseListener {
            override fun mouseClicked(e: MouseEvent) {
            }

            override fun mousePressed(e: MouseEvent) {
                scenePicker.find(e.x, e.y)
                if (currentDragElement != null) {
                    dragging = true
                }
            }

            override fun mouseReleased(e: MouseEvent?) {
                currentDragElement = null
                dragging = false
            }

            override fun mouseEntered(e: MouseEvent?) {
            }

            override fun mouseExited(e: MouseEvent?) {
            }

        })

        addMouseMotionListener(object: MouseMotionListener {
            override fun mouseDragged(e: MouseEvent) {
                if (currentDragElement is GuidelineModel) {
                    var value = 0f
                    if (currentDragElement is HorizontalGuideline) {
                        currentY = e.y
                        value = (currentY - offsetY) / rangeY.toFloat()
                    } else if (currentDragElement is VerticalGuideline) {
                        currentX = e.x
                        value = (currentX - offsetX) / rangeX.toFloat()
                    }
                    value = max(0f, min(1f, value))
                    value = (value * 100).toInt() / 100f
                    val target = currentDragElement?.name as String
                    println("Range $value for $target")
                    if (designSurfaceModificationCallback != null) {
                        var element = designSurfaceModificationCallback!!.getElement(target)
                        if (element is CLObject) {
                            element.putNumber("percent", value)
                            designSurfaceModificationCallback?.updateElement(target, element)
                        }
                    }
                }
            }

            override fun mouseMoved(e: MouseEvent?) {
            }

        })
    }

    data class Widget(val id: String, val key: CLKey) {
        var interpolated = WidgetFrame()
        var start = WidgetFrame()
        var end = WidgetFrame()
        var name = "unknown";
        var path = Path2D.Float()
        val drawFont = Font("Helvetica", Font.ITALIC, 32)
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
            if (widget.isGuideline) {
                continue
            }
            widget.draw(g2, widget == root)
        }

        scenePicker.reset()

        offsetX = offX.toInt()
        rangeX = (root.width() * scaleX).toInt()
        offsetY = offY.toInt()
        rangeY = (root.height() * scaleY).toInt()

        oG.translate(offX.toDouble(), offY.toDouble())
        for (guideline in horizontalGuidelines) {
            guideline.draw(oG, (rootWidth * scaleX).toInt(), (rootHeight * scaleY).toInt())
            guideline.addToPicker(scenePicker, offsetX, offsetY, rangeY)
        }

        for (guideline in verticalGuidelines) {
            guideline.draw(oG, (rootWidth * scaleX).toInt(), (rootHeight * scaleY).toInt())
            guideline.addToPicker(scenePicker, offsetX, offsetY, rangeX)
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
        for (guideline in horizontalGuidelines) {
            for (widget in widgets) {
                if (guideline.name.equals(widget.id)) {
                    widget.isGuideline = true
                }
            }
        }
        for (guideline in verticalGuidelines) {
            for (widget in widgets) {
                if (guideline.name.equals(widget.id)) {
                    widget.isGuideline = true
                }
            }
        }
    }

    open class GuidelineModel(val id: String, val p : Float = 0f) {
        var name = id
        var percent = p
    }

    class HorizontalGuideline(val key: String, element: CLObject) : GuidelineModel(key, element.getFloat("percent")) {

        lateinit var img : BufferedImage
        val gap = 2

        init {
            try {
                img = ImageIO.read(File("/Users/nicolasroard/Desktop/old2/guideline-horiz.png"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        fun draw(g: Graphics2D, w: Int, h: Int) {
            g.setColor(WidgetFrameUtils.theme.pathColor())
            val y = (h * percent).toInt()
            g.drawLine(0, y, w, y)
            drawImage(g,0, y)
        }
        fun drawImage(g: Graphics2D, x : Int, y: Int) {
            val h = img.height
            val w = img.width
            g.drawImage(img, - gap - w, y - h / 2, null)
        }

        fun addToPicker(scenePicker: ScenePicker, ox: Int, oy: Int, h: Int) {
            val ih = img.height
            val iw = img.width
            val y = (h * percent).toInt()
            val x1 = ox - gap - iw
            val x2 = x1 + iw
            val y1 = oy + y - ih / 2
            val y2 = y1 + ih
            scenePicker.addRect(this, 0, x1, y1, x2, y2)
        }
    }

    class VerticalGuideline(val key: String, element: CLObject) : GuidelineModel(key, element.getFloat("percent")) {
        lateinit var img : BufferedImage
        val gap = 2

        init {
            try {
                img = ImageIO.read(File("/Users/nicolasroard/Desktop/old2/guideline-vert.png"))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun draw(g: Graphics2D, w: Int, h: Int) {
            g.setColor(WidgetFrameUtils.theme.pathColor())
            val x = (w * percent).toInt()
            g.drawLine(x, 0, x, h)
            drawImage(g,x, 0)
        }

        fun drawImage(g: Graphics2D, x : Int, y: Int) {
            val h = img.height
            val w = img.width
            g.drawImage(img, x - w / 2, - gap - h, null)
        }

        fun addToPicker(scenePicker: ScenePicker, ox: Int, oy: Int, w: Int) {
            val ih = img.height
            val iw = img.width
            val x = (w * percent).toInt()
            val x1 = ox + x - iw / 2
            val x2 = x1 + iw
            val y1 = oy -gap -ih
            val y2 = y1 + ih
            scenePicker.addRect(this, 0, x1, y1, x2, y2)
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
            ConstraintWidget.HORIZONTAL -> {
                val guideline = HorizontalGuideline(name, element)
                //guideline.addToPicker(scenePicker)
                horizontalGuidelines.add(guideline)
            }
            ConstraintWidget.VERTICAL -> {
                val guideline = VerticalGuideline(name, element)
                //guideline.addToPicker(scenePicker)
                verticalGuidelines.add(guideline)
            }
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
            inspector.layoutView.designSurfaceModificationCallback = callback
            return inspector.layoutView
        }
    }
}