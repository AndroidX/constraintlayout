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

package androidx.constraintlayout.compose

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.*
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*
import androidx.constraintlayout.core.motion.Motion
import androidx.constraintlayout.core.parser.CLParser
import androidx.constraintlayout.core.parser.CLParsingException
import androidx.constraintlayout.core.state.*
import androidx.constraintlayout.core.state.Dimension
import androidx.constraintlayout.core.state.Transition
import androidx.constraintlayout.core.widgets.Optimizer
import org.intellij.lang.annotations.Language
import java.lang.StringBuilder
import java.util.*


/**
 * Layout that interpolate its children layout given two sets of constraint and
 * a progress (from 0 to 1)
 */
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun MotionLayout(
        start: ConstraintSet,
        end: ConstraintSet,
        transition: androidx.constraintlayout.compose.Transition? = null,
        progress: Float,
        debug: EnumSet<MotionLayoutDebugFlags> = EnumSet.of(MotionLayoutDebugFlags.NONE),
        modifier: Modifier = Modifier,
        optimizationLevel: Int = Optimizer.OPTIMIZATION_STANDARD,
        crossinline content: @Composable MotionLayoutScope.() -> Unit
) {
    val measurer = remember { MotionMeasurer() }
    val scope = remember { MotionLayoutScope(measurer) }
    val progressState = remember { mutableStateOf(0f) }
    SideEffect { progressState.value = progress }
    val measurePolicy =
        rememberMotionLayoutMeasurePolicy(optimizationLevel, debug, start, end, transition, progressState, measurer)
    if (!debug.contains(MotionLayoutDebugFlags.NONE)) {
        Box {
            @Suppress("DEPRECATION")
            (MultiMeasureLayout(
                modifier = modifier.semantics { designInfoProvider = measurer },
                measurePolicy = measurePolicy,
                content = { scope.content() }
            ))
            with(measurer) {
                drawDebug()
            }
        }
    } else {
        @Suppress("DEPRECATION")
        (MultiMeasureLayout(
            modifier = modifier.semantics { designInfoProvider = measurer },
            measurePolicy = measurePolicy,
            content = { scope.content() }
        ))
    }
}

@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun MotionLayout(
    motionScene: MotionScene,
    progress: Float,
    debug: EnumSet<MotionLayoutDebugFlags> = EnumSet.of(MotionLayoutDebugFlags.NONE),
    modifier: Modifier = Modifier,
    optimizationLevel: Int = Optimizer.OPTIMIZATION_STANDARD,
    crossinline content: @Composable() (MotionLayoutScope.() -> Unit),
) {
    val needsUpdate = remember {
        mutableStateOf(0L)
    }
    motionScene.setUpdateFlag(needsUpdate)

    var usedDebugMode = debug
    if (motionScene.getForcedDrawDebug() != MotionLayoutDebugFlags.UNKNOWN) {
        usedDebugMode = EnumSet.of(motionScene.getForcedDrawDebug())
    }

    val startContent = remember(motionScene, needsUpdate.value) {
        motionScene.getConstraintSet("start")
    }
    val endContent = remember(motionScene, needsUpdate.value) {
        motionScene.getConstraintSet("end")
    }
    val transitionContent = remember(motionScene, needsUpdate.value) {
        motionScene.getTransition("default")
    }
    val forcedProgress = motionScene.getForcedProgress()
    val usedProgress = if (forcedProgress.isNaN()) progress else forcedProgress

    if (startContent == null || endContent == null) {
        return
    }
    val start = ConstraintSet(startContent)
    val end = ConstraintSet(endContent)
    val transition : androidx.constraintlayout.compose.Transition? = if (transitionContent != null) Transition(transitionContent) else null

    val measurer = remember { MotionMeasurer() }
    val scope = remember { MotionLayoutScope(measurer) }
    val progressState = remember { mutableStateOf(0f) }
    SideEffect { progressState.value = usedProgress }
    val measurePolicy =
        rememberMotionLayoutMeasurePolicy(optimizationLevel, usedDebugMode, start, end, transition, progressState, measurer)
    measurer.setLayoutInformationReceiver(motionScene as InternalMotionScene)

    if (!usedDebugMode.contains(MotionLayoutDebugFlags.NONE)) {
        Box {
            @Suppress("DEPRECATION")
            (MultiMeasureLayout(
                modifier = modifier.semantics { designInfoProvider = measurer },
                measurePolicy = measurePolicy,
                content = { scope.content() }
            ))
            with(measurer) {
                drawDebug()
            }
        }
    } else {
        @Suppress("DEPRECATION")
        (MultiMeasureLayout(
            modifier = modifier.semantics { designInfoProvider = measurer },
            measurePolicy = measurePolicy,
            content = { scope.content() }
        ))
    }

}

@Immutable
interface MotionScene {
    fun setConstraintSetContent(name: String, content: String)
    fun setTransitionContent(name: String, content: String)
    fun getConstraintSet(name: String): String?
    fun getTransition(name: String) : String?
    fun setUpdateFlag(needsUpdate: MutableState<Long>)
    fun setDebugName(name: String?)
    fun getForcedProgress(): Float
    fun getDebugName() : String?
    fun getForcedDrawDebug(): MotionLayoutDebugFlags
}

class InternalMotionScene(@Language("json5") content : String) : MotionScene, LayoutInformationReceiver {

    private var forcedDrawDebug: MotionLayoutDebugFlags = MotionLayoutDebugFlags.UNKNOWN
    private var layoutInformationMode: LayoutInfoFlags = LayoutInfoFlags.NONE
    private var forcedProgress: Float = Float.NaN
    private var updateFlag: MutableState<Long>? = null
    private val constraintSetsContent = HashMap<String, String>()
    private val transitionsContent = HashMap<String, String>()
    private var debugName:String? = null
    private var currentContent = content
    private var currentFormattedContent = ""
    private var layoutInformation = ""

    init {
        parseMotionSceneJSON(this, currentContent)
        try {
            val json = CLParser.parse(currentContent)
            currentFormattedContent = json.toFormattedJSON()
        } catch (e : CLParsingException) {

        }

        if (debugName != null) {
            val mainHandler = Handler(Looper.getMainLooper())
            val scene = this
            val callback = object : RegistryCallback {
                override fun onNewMotionScene(content: String?) {
                    if (content == null) {
                        return
                    }
                    mainHandler.post {
                        try {
                            currentContent = content
                            parseMotionSceneJSON(scene, currentContent);
                            try {
                                val json = CLParser.parse(currentContent)
                                currentFormattedContent = json.toFormattedJSON()
                            } catch (e : CLParsingException) {

                            }
                            if (updateFlag != null) {
                                updateFlag!!.value = updateFlag!!.value + 1
                            }
                        } catch (e : Exception) {}
                    }
                }

                override fun onProgress(progress: Float) {
                    mainHandler.post {
                        try {
                            forcedProgress = progress
                            if (updateFlag != null) {
                                updateFlag!!.value = updateFlag!!.value + 1
                            }
                        } catch (e : Exception) {}
                    }
                }

                override fun currentMotionScene() : String {
                    return currentFormattedContent
                }

                override fun currentLayoutInformation() : String {
                    return layoutInformation
                }

                override fun setLayoutInformationMode(mode: Int) {
                    mainHandler.post {
                        try {
                            when (mode) {
                                LayoutInfoFlags.NONE.ordinal -> layoutInformationMode = LayoutInfoFlags.NONE
                                LayoutInfoFlags.BOUNDS.ordinal -> layoutInformationMode = LayoutInfoFlags.BOUNDS
                            }
                            if (updateFlag != null) {
                                updateFlag!!.value = updateFlag!!.value + 1
                            }
                        } catch (e : Exception) {}
                    }
                }

                override fun setDrawDebug(debugMode: Int) {
                    mainHandler.post {
                        try {
                            when (debugMode) {
                                -1 -> forcedDrawDebug = MotionLayoutDebugFlags.UNKNOWN
                                MotionLayoutDebugFlags.UNKNOWN.ordinal -> forcedDrawDebug = MotionLayoutDebugFlags.UNKNOWN
                                MotionLayoutDebugFlags.NONE.ordinal -> forcedDrawDebug = MotionLayoutDebugFlags.NONE
                                MotionLayoutDebugFlags.SHOW_ALL.ordinal -> forcedDrawDebug = MotionLayoutDebugFlags.SHOW_ALL
                            }
                            if (updateFlag != null) {
                                updateFlag!!.value = updateFlag!!.value + 1
                            }
                        } catch (e : Exception) {}
                    }
                }
            }
            val registry = Registry.getInstance()
            registry.register(debugName, callback)
        }
    }

    override fun setConstraintSetContent(name: String, content: String) {
        constraintSetsContent[name] = content
    }

    override fun setTransitionContent(name: String, content: String) {
        transitionsContent[name] = content
    }

    override fun getConstraintSet(name: String): String? {
        return constraintSetsContent[name]
    }

    override fun getTransition(name: String): String? {
        return transitionsContent[name]
    }

    override fun getForcedProgress() : Float {
        return forcedProgress;
    }

    override fun setUpdateFlag(needsUpdate: MutableState<Long>) {
        updateFlag = needsUpdate
    }

    override fun setDebugName(name: String?) {
        debugName = name
    }

    override fun getDebugName() : String?{
        return debugName
    }

    override fun getForcedDrawDebug(): MotionLayoutDebugFlags {
        return forcedDrawDebug
    }

    override fun getLayoutInformationMode(): LayoutInfoFlags {
        return layoutInformationMode
    }

    override fun setLayoutInformation(information: String) {
        layoutInformation = information
    }
}

@SuppressLint("ComposableNaming")
@Composable
fun MotionScene(@Language("json5") content: String): MotionScene {
    return remember(content) {
        InternalMotionScene(content)
    }
}

@LayoutScopeMarker
class MotionLayoutScope @PublishedApi internal constructor(measurer: MotionMeasurer) {
    private var myMeasurer = measurer

    class MotionProperties internal constructor(id: String, tag: String?, measurer: MotionMeasurer) {
        private var myId = id
        private var myTag = null
        private var myMeasurer = measurer

        fun id() : String {
            return myId
        }

        fun tag() : String? {
            return myTag
        }

        fun color(name: String) : Color {
            return myMeasurer.getCustomColor(myId, name)
        }

        fun float(name: String) : Float {
            return myMeasurer.getCustomFloat(myId, name)
        }

        fun int(name: String): Int {
            return myMeasurer.getCustomFloat(myId, name).toInt()
        }

        fun distance(name: String): Dp {
            return myMeasurer.getCustomFloat(myId, name).dp
        }

        fun fontSize(name: String) : TextUnit {
            return myMeasurer.getCustomFloat(myId, name).sp
        }
    }

    @Composable
    fun motionProperties(id: String): MutableState<MotionProperties> = remember {
        mutableStateOf(MotionProperties(id, null, myMeasurer))
    }

    fun motionProperties(id: String, tag: String): MotionProperties{
        return MotionProperties(id, tag, myMeasurer)
    }

    fun motionColor(id: String, name: String): Color {
        return myMeasurer.getCustomColor(id, name)
    }

    fun motionFloat(id: String, name: String): Float {
        return myMeasurer.getCustomFloat(id, name)
    }

    fun motionInt(id: String, name: String): Int {
        return myMeasurer.getCustomFloat(id, name).toInt()
    }

    fun motionDistance(id: String, name: String): Dp {
        return myMeasurer.getCustomFloat(id, name).dp
    }

    fun motionFontSize(id: String, name: String): TextUnit {
        return myMeasurer.getCustomFloat(id, name).sp
    }
}

@Immutable
interface Transition {
    fun applyTo(transition: Transition, type: Int)
}

@SuppressLint("ComposableNaming")
@Composable
fun Transition(@Language("json5") content : String) : androidx.constraintlayout.compose.Transition {
    val transition = remember(content) {
        mutableStateOf(object : androidx.constraintlayout.compose.Transition {
            override fun applyTo(transition: Transition, type: Int) {
                parseTransition(content, transition)
            }
        })
    }
    return transition.value
}

enum class MotionLayoutDebugFlags {
    NONE,
    SHOW_ALL,
    UNKNOWN
}

enum class LayoutInfoFlags {
    NONE,
    BOUNDS
}

@Composable
@PublishedApi
internal fun rememberMotionLayoutMeasurePolicy(
    optimizationLevel: Int,
    debug: EnumSet<MotionLayoutDebugFlags>,
    constraintSetStart: ConstraintSet,
    constraintSetEnd: ConstraintSet,
    transition: androidx.constraintlayout.compose.Transition?,
    progress: MutableState<Float>,
    measurer: MotionMeasurer
) = remember(optimizationLevel, debug, constraintSetStart, constraintSetEnd, transition) {
    measurer.initWith(constraintSetStart, constraintSetEnd, transition, progress.value)
    MeasurePolicy { measurables, constraints ->
        val layoutSize = measurer.performInterpolationMeasure(
            constraints,
            layoutDirection,
            constraintSetStart,
            constraintSetEnd,
            transition,
            measurables,
            optimizationLevel,
            progress.value,
            this
        )
        layout(layoutSize.width, layoutSize.height) {
            with(measurer) {
                performLayout(measurables)
            }
        }
    }
}

interface LayoutInformationReceiver {
    fun setLayoutInformation(information: String)
    fun getLayoutInformationMode() : LayoutInfoFlags
}

@PublishedApi
internal class MotionMeasurer : Measurer() {
    private var layoutInformationReceiver: LayoutInformationReceiver? = null
    private var motionProgress = 0f
    val transition = Transition()

    fun getProgress() : Float { return motionProgress }

    private fun measureConstraintSet(optimizationLevel: Int, constraintSetStart: ConstraintSet,
                                     measurables: List<Measurable>, constraints: Constraints
    ) {
        state.reset()
        constraintSetStart.applyTo(state, measurables)
        state.apply(root)
        root.width = constraints.maxWidth
        root.height = constraints.maxHeight
        root.updateHierarchy()

        if (DEBUG) {
            root.debugName = "ConstraintLayout"
            root.children.forEach { child ->
                child.debugName =
                    (child.companionWidget as? Measurable)?.layoutId?.toString() ?: "NOTAG"
            }
        }

        root.children.forEach { child ->
            var measurable = (child.companionWidget as? Measurable)
            var id = measurable?.layoutId ?: measurable?.constraintLayoutId
            child.stringId = id?.toString()
        }

        root.optimizationLevel = optimizationLevel
        // No need to set sizes and size modes as we passed them to the state above.
        root.measure(Optimizer.OPTIMIZATION_NONE, 0, 0, 0, 0, 0, 0, 0, 0)
    }

    fun performInterpolationMeasure(
            constraints: Constraints,
            layoutDirection: LayoutDirection,
            constraintSetStart: ConstraintSet,
            constraintSetEnd: ConstraintSet,
            transition: androidx.constraintlayout.compose.Transition?,
            measurables: List<Measurable>,
            optimizationLevel: Int,
            progress: Float,
            measureScope: MeasureScope
    ): IntSize {
        this.density = measureScope
        this.measureScope = measureScope
        if (motionProgress != progress
            || this.transition.isEmpty()
            || frameCache.isEmpty()) {
            motionProgress = progress
            if (this.transition.isEmpty() || frameCache.isEmpty()) {
                this.transition.clear()
                reset()
                // Define the size of the ConstraintLayout.
                state.width(
                    if (constraints.hasFixedWidth) {
                        Dimension.Fixed(constraints.maxWidth)
                    } else {
                        Dimension.Wrap().min(constraints.minWidth)
                    }
                )
                state.height(
                    if (constraints.hasFixedHeight) {
                        Dimension.Fixed(constraints.maxHeight)
                    } else {
                        Dimension.Wrap().min(constraints.minHeight)
                    }
                )
                // Build constraint set and apply it to the state.
                state.rootIncomingConstraints = constraints
                state.layoutDirection = layoutDirection

                measureConstraintSet(optimizationLevel, constraintSetStart, measurables, constraints)
                this.transition.updateFrom(root, Transition.START)
                measureConstraintSet(optimizationLevel, constraintSetEnd, measurables, constraints)
                this.transition.updateFrom(root, Transition.END)
                if (transition != null) {
                    transition.applyTo(this.transition, 0)
                }
            }
            this.transition.interpolate(root.width, root.height, progress)
            var index = 0
            for (child in root.children) {
                val measurable = child.companionWidget
                if (measurable !is Measurable) continue
                var interpolatedFrame = this.transition.getInterpolated(child)
                if (interpolatedFrame == null) {
                    continue
                }
                val placeable = placeables[measurable]
                val currentWidth = placeable?.width
                val currentHeight = placeable?.height
                if (placeable == null
                    || currentWidth != interpolatedFrame.width()
                    || currentHeight != interpolatedFrame.height()
                ) {
                    measurable.measure(
                        Constraints.fixed(interpolatedFrame.width(), interpolatedFrame.height())
                    )
                        .also {
                            placeables[measurable] = it
                        }
                }
                frameCache[measurable] = interpolatedFrame
                index++
            }
            if (layoutInformationReceiver?.getLayoutInformationMode() == LayoutInfoFlags.BOUNDS) {
                computeLayoutResult()
            }
        }
        return IntSize(root.width, root.height)
    }

//    fun computeLayoutResult() {
//        val json = StringBuilder()
//        json.append("{ [")
//        json.append("{ root: [ 0, 0, ${root.width}, ${root.height} ] }")
//        for (child in root.children) {
//            val frame = transition.getInterpolated(child.stringId)
//            json.append(", ")
//            json.append("{ ${child.stringId}: [ ${frame.left}, ${frame.top}, ${frame.right}, ${frame.bottom} ] }")
//        }
//        json.append("] }")
//        layoutInformationReceiver?.setLayoutInformation(json.toString())
//    }

    fun computeLayoutResult() {
        val json = StringBuilder()
        json.append("{ ")
        json.append("  root: ")
        json.append("{ left:  0,")
        json.append("  top:  0,")
        json.append("  right:   ${root.width} ,")
        json.append("  bottom:  ${root.height} ,")
        json.append(" },")

        for (child in root.children) {
            val frame = transition.getInterpolated(child.stringId)
            json.append(" ${child.stringId}: ")
            frame.serialize(json);
            json.append(", ")
        }
        json.append(" }")
        layoutInformationReceiver?.setLayoutInformation(json.toString())
    }
    @Composable
    fun BoxScope.drawDebug() {
        Canvas(modifier = Modifier.matchParentSize()) {
            var index = 0
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            for (child in root.children) {
                val startFrame = transition.getStart(child)
                val endFrame = transition.getEnd(child)
                translate(2f, 2f) {
                    drawFrameDebug(size.width, size.height, startFrame, endFrame, pathEffect, Color.White)
                }
                drawFrameDebug(size.width, size.height, startFrame, endFrame, pathEffect, Color.Blue)
                index++
            }
        }
    }

    private fun DrawScope.drawFrameDebug(
        parentWidth: Float,
        parentHeight: Float,
        startFrame: WidgetFrame,
        endFrame: WidgetFrame,
        pathEffect: PathEffect,
        color: Color
    ) {
        drawFrame(startFrame, pathEffect, color)
        drawFrame(endFrame, pathEffect, color)
        var numKeyPositions = transition.getNumberKeyPositions(startFrame)
        var debugRender = MotionRenderDebug(23f);

        debugRender.draw(drawContext.canvas.nativeCanvas,transition.getMotion(startFrame.widget.stringId),
            1000,   Motion.DRAW_PATH_BASIC,
              parentWidth.toInt(), parentHeight.toInt()  )
        if (numKeyPositions == 0) {
//            drawLine(
//                start = Offset(startFrame.centerX(), startFrame.centerY()),
//                end = Offset(endFrame.centerX(), endFrame.centerY()),
//                color = color,
//                strokeWidth = 3f,
//                pathEffect = pathEffect
//            )
        } else {
            var x = FloatArray(numKeyPositions)
            var y = FloatArray(numKeyPositions)
            var pos = FloatArray(numKeyPositions)
            transition.fillKeyPositions(startFrame, x, y, pos)
            var prex = startFrame.centerX()
            var prey = startFrame.centerY()

            for (i in 0 .. numKeyPositions-1) {
                var keyFrameProgress = pos[i] / 100f
                var frameWidth = ((1 - keyFrameProgress) * startFrame.width()) + (keyFrameProgress * endFrame.width())
                var frameHeight = ((1 - keyFrameProgress) * startFrame.height()) + (keyFrameProgress * endFrame.height())
                var curX = x[i] * parentWidth + frameWidth / 2f
                var curY = y[i] * parentHeight + frameHeight / 2f
//                drawLine(
//                    start = Offset(prex, prey),
//                    end = Offset(curX, curY),
//                    color = color,
//                    strokeWidth = 3f,
//                    pathEffect = pathEffect
//                )
                var path = Path()
                var pathSize = 20f
                path.moveTo(curX - pathSize, curY)
                path.lineTo(curX, curY + pathSize)
                path.lineTo(curX + pathSize, curY)
                path.lineTo(curX, curY - pathSize)
                path.close()

                var stroke = Stroke(width = 3f)
                drawPath(path, color, 1f, stroke)
                prex = curX
                prey = curY
            }
//            drawLine(
//                start = Offset(prex, prey),
//                end = Offset(endFrame.centerX(), endFrame.centerY()),
//                color = color,
//                strokeWidth = 3f,
//                pathEffect = pathEffect
//            )
        }
    }

    private fun DrawScope.drawFrame(
        frame: WidgetFrame,
        pathEffect: PathEffect,
        color: Color
    ) {
        if (frame.isDefaultTransform) {
            var drawStyle = Stroke(width = 3f, pathEffect = pathEffect)
            drawRect(color, Offset(frame.left.toFloat(), frame.top.toFloat()),
                Size(frame.width().toFloat(), frame.height().toFloat()), style = drawStyle)
        } else {
            var matrix = Matrix()
            if (!frame.rotationZ.isNaN()) {
                matrix.preRotate(frame.rotationZ, frame.centerX(), frame.centerY())
            }
            var scaleX = if (frame.scaleX.isNaN()) 1f else frame.scaleX
            var scaleY = if (frame.scaleY.isNaN()) 1f else frame.scaleY
            matrix.preScale(
                scaleX,
                scaleY,
                frame.centerX(),
                frame.centerY()
            )
            var points = floatArrayOf(
                frame.left.toFloat(), frame.top.toFloat(),
                frame.right.toFloat(), frame.top.toFloat(),
                frame.right.toFloat(), frame.bottom.toFloat(),
                frame.left.toFloat(), frame.bottom.toFloat()
            )
            matrix.mapPoints(points)
            drawLine(
                start = Offset(points[0], points[1]),
                end = Offset(points[2], points[3]),
                color = color,
                strokeWidth = 3f,
                pathEffect = pathEffect
            )
            drawLine(
                start = Offset(points[2], points[3]),
                end = Offset(points[4], points[5]),
                color = color,
                strokeWidth = 3f,
                pathEffect = pathEffect
            )
            drawLine(
                start = Offset(points[4], points[5]),
                end = Offset(points[6], points[7]),
                color = color,
                strokeWidth = 3f,
                pathEffect = pathEffect
            )
            drawLine(
                start = Offset(points[6], points[7]),
                end = Offset(points[0], points[1]),
                color = color,
                strokeWidth = 3f,
                pathEffect = pathEffect
            )
        }
    }

    fun getCustomColor(id: String, name: String): Color {
        if (!transition.contains(id)) {
            return Color.Black
        }
        val startFrame = transition.getStart(id)
        val endFrame = transition.getEnd(id)

        transition.interpolate(root.width, root.height, motionProgress)

        val interpolatedFrame = transition.getInterpolated(id)
        val color = interpolatedFrame.getCustomColor(name)
        return Color(color);
    }

    fun getCustomFloat(id: String, name: String): Float {
        if (!transition.contains(id)) {
            return 0f;
        }
        val startFrame = transition.getStart(id)
        val endFrame = transition.getEnd(id)
        val startFloat = startFrame.getCustomFloat(name)
        val endFloat = endFrame.getCustomFloat(name)
        return (1f - motionProgress) * startFloat + motionProgress * endFloat
    }

    fun clearConstraintSets() {
        transition.clear()
        frameCache.clear()
    }

    fun initWith(
            start: ConstraintSet,
            end: ConstraintSet,
            transition: androidx.constraintlayout.compose.Transition?,
            progress: Float
    ) {
        clearConstraintSets()
        start.applyTo(this.transition, Transition.START)
        end.applyTo(this.transition, Transition.END)
        this.transition.interpolate(0, 0, progress)
        transition?.applyTo(this.transition, 0)
    }

    fun setLayoutInformationReceiver(layoutReceiver: LayoutInformationReceiver) {
        layoutInformationReceiver = layoutReceiver
    }
}

private val DEBUG = false