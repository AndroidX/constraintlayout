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

import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.MultiMeasureLayout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.*
import androidx.constraintlayout.core.state.Dimension
import androidx.constraintlayout.core.state.Transition
import androidx.constraintlayout.core.state.WidgetFrame
import androidx.constraintlayout.core.widgets.Optimizer
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
    progress: Float,
    debug: EnumSet<MotionLayoutDebugFlags> = EnumSet.of(MotionLayoutDebugFlags.NONE),
    modifier: Modifier = Modifier,
    optimizationLevel: Int = Optimizer.OPTIMIZATION_STANDARD,
    crossinline content: @Composable MotionLayoutScope.() -> Unit
) {
    val measurer = remember { MotionMeasurer(start, end, progress) }
    val scope = remember { MotionLayoutScope(measurer) }
    val progressState = remember { mutableStateOf(0f) }
    SideEffect { progressState.value = progress }
    val measurePolicy =
        rememberMotionLayoutMeasurePolicy(optimizationLevel, start, end, progressState, measurer)
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

enum class MotionLayoutDebugFlags {
    NONE,
    SHOW_ALL
}

@Composable
@PublishedApi
internal fun rememberMotionLayoutMeasurePolicy(
    optimizationLevel: Int,
    constraintSetStart: ConstraintSet,
    constraintSetEnd: ConstraintSet,
    progress: MutableState<Float>,
    measurer: MotionMeasurer
) = remember(optimizationLevel, constraintSetStart, constraintSetEnd) {
    MeasurePolicy { measurables, constraints ->
        val layoutSize = measurer.performInterpolationMeasure(
            constraints,
            layoutDirection,
            constraintSetStart,
            constraintSetEnd,
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

@PublishedApi
internal class MotionMeasurer(
    start: ConstraintSet,
    end: ConstraintSet,
    progress: Float
) : Measurer() {
    private var motionProgress = 0f
    val transition = Transition()

    init {
        start.applyTo(transition, Transition.START)
        end.applyTo(transition, Transition.END)
        transition.interpolate(progress)
    }

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
        measurables: List<Measurable>,
        optimizationLevel: Int,
        progress: Float,
        measureScope: MeasureScope
    ): IntSize {
        this.density = measureScope
        this.measureScope = measureScope
        if (motionProgress != progress
            || transition.isEmpty()
            || frameCache.isEmpty()) {
            motionProgress = progress
            if (transition.isEmpty() || frameCache.isEmpty()) {
                transition.clear()
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
                transition.updateFrom(root, Transition.START)
                measureConstraintSet(optimizationLevel, constraintSetEnd, measurables, constraints)
                transition.updateFrom(root, Transition.END)
            }
            transition.interpolate(progress)
            var index = 0
            for (child in root.children) {
                val measurable = child.companionWidget
                if (measurable !is Measurable) continue
                var interpolatedFrame = transition.getInterpolated(child)
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
        }
        return IntSize(root.width, root.height)
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
                    drawFrame(startFrame, pathEffect, Color.White)
                    drawFrame(endFrame, pathEffect, Color.White)
                    drawLine(
                        start = Offset(startFrame.centerX(), startFrame.centerY()),
                        end = Offset(endFrame.centerX(), endFrame.centerY()),
                        color = Color.White,
                        strokeWidth = 3f,
                        pathEffect = pathEffect
                    )
                }
                drawFrame(startFrame, pathEffect, Color.Blue)
                drawFrame(endFrame, pathEffect, Color.Blue)
                drawLine(
                    start = Offset(startFrame.centerX(), startFrame.centerY()),
                    end = Offset(endFrame.centerX(), endFrame.centerY()),
                    color = Color.Blue,
                    strokeWidth = 3f,
                    pathEffect = pathEffect
                )
                index++
            }
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
            matrix.preRotate(frame.rotationZ, frame.centerX(), frame.centerY())
            matrix.preScale(
                frame.scaleX,
                frame.scaleY,
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
        val startColor = startFrame.getCustomColor(name)
        val endColor = endFrame.getCustomColor(name)
        if (startColor != null && endColor != null) {
            var result = WidgetFrame.Color(0f, 0f, 0f, 0f)
            WidgetFrame.interpolateColor(result, startColor, endColor, motionProgress)
            return Color(result.r, result.g, result.b, result.a)
        }
        return Color.Black
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
}

private val DEBUG = false