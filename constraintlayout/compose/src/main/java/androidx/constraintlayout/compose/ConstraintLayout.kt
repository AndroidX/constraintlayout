/*
 * Copyright 2019 The Android Open Source Project
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
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.FloatRange
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.constraintlayout.core.state.ConstraintReference
import androidx.constraintlayout.core.state.Dimension.*
import androidx.constraintlayout.core.state.Transition
import androidx.constraintlayout.core.state.WidgetFrame
import androidx.constraintlayout.core.widgets.ConstraintWidget
import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour.FIXED
import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour.MATCH_CONSTRAINT
import androidx.constraintlayout.core.widgets.ConstraintWidget.DimensionBehaviour.WRAP_CONTENT
import androidx.constraintlayout.core.widgets.ConstraintWidget.MATCH_CONSTRAINT_SPREAD
import androidx.constraintlayout.core.widgets.ConstraintWidget.MATCH_CONSTRAINT_WRAP
import androidx.constraintlayout.core.widgets.ConstraintWidgetContainer
import androidx.constraintlayout.core.widgets.HelperWidget
import androidx.constraintlayout.core.widgets.Optimizer
import androidx.constraintlayout.core.widgets.analyzer.BasicMeasure
import androidx.constraintlayout.core.widgets.analyzer.BasicMeasure.Measure.TRY_GIVEN_DIMENSIONS
import androidx.constraintlayout.core.widgets.analyzer.BasicMeasure.Measure.USE_GIVEN_DIMENSIONS
import org.intellij.lang.annotations.Language
import java.util.*
import kotlin.collections.ArrayList

/**
 * Layout that positions its children according to the constraints between them.
 *
 * Example usage:
 * @sample androidx.compose.foundation.layout.samples.DemoInlineDSL
 */
@Composable
inline fun ConstraintLayout(
    modifier: Modifier = Modifier,
    optimizationLevel: Int = Optimizer.OPTIMIZATION_STANDARD,
    crossinline content: @Composable ConstraintLayoutScope.() -> Unit
) {
    val measurer = remember { Measurer() }
    val scope = remember { ConstraintLayoutScope() }
    val remeasureRequesterState = remember { mutableStateOf(false) }
    val (measurePolicy, onHelpersChanged) = rememberConstraintLayoutMeasurePolicy(
        optimizationLevel,
        scope,
        remeasureRequesterState,
        measurer
    )
    @Suppress("Deprecation")
    MultiMeasureLayout(
        modifier = modifier.semantics { designInfoProvider = measurer },
        measurePolicy = measurePolicy,
        content = {
            val previousHelpersHashCode = scope.helpersHashCode
            scope.reset()
            scope.content()
            if (scope.helpersHashCode != previousHelpersHashCode) onHelpersChanged()
        }
    )
}

@Composable
@PublishedApi
internal fun rememberConstraintLayoutMeasurePolicy(
    optimizationLevel: Int,
    scope: ConstraintLayoutScope,
    remeasureRequesterState: MutableState<Boolean>,
    measurer: Measurer
): Pair<MeasurePolicy, () -> Unit> {
    val constraintSet = remember { ConstraintSetForInlineDsl(scope) }

    return remember(optimizationLevel) {
        val measurePolicy = MeasurePolicy { measurables, constraints ->
            val layoutSize = measurer.performMeasure(
                constraints,
                layoutDirection,
                constraintSet,
                measurables,
                optimizationLevel,
                this
            )
            // We read the remeasurement requester state, to request remeasure when the value
            // changes. This will happen when the scope helpers are changing at recomposition.
            remeasureRequesterState.value

            layout(layoutSize.width, layoutSize.height) {
                with(measurer) { performLayout(measurables) }
            }
        }
        val onHelpersChanged = {
            // If the helpers have changed, we need to request remeasurement. To achieve this,
            // we are changing this boolean state that is read during measurement.
            remeasureRequesterState.value = !remeasureRequesterState.value
            constraintSet.knownDirty = true
        }
        measurePolicy to onHelpersChanged
    }
}

private class ConstraintSetForInlineDsl(
    val scope: ConstraintLayoutScope
) : ConstraintSet, RememberObserver {
    private var handler: Handler? = null
    private val observer = SnapshotStateObserver {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            it()
        } else {
            val h = handler ?: Handler(Looper.getMainLooper()).also { h -> handler = h }
            h.post(it)
        }
    }

    override fun applyTo(state: State, measurables: List<Measurable>) {
        scope.applyTo(state)
        previousDatas.clear()
        observer.observeReads(Unit, onCommitAffectingConstrainLambdas) {
            measurables.fastForEach { measurable ->
                val parentData = measurable.parentData as? ConstraintLayoutParentData
                // Run the constrainAs block of the child, to obtain its constraints.
                if (parentData != null) {
                    val constrainScope = ConstrainScope(parentData.ref.id)
                    parentData.constrain(constrainScope)
                    constrainScope.applyTo(state)
                }
                previousDatas.add(parentData)
            }
        }
        knownDirty = false
    }

    var knownDirty = true

    private val onCommitAffectingConstrainLambdas = { _: Unit -> knownDirty = true }

    override fun isDirty(measurables: List<Measurable>): Boolean {
        if (knownDirty || measurables.size != previousDatas.size) return true

        measurables.fastForEachIndexed { index, measurable ->
            Log.d("MIHAI", "SE verifica si: ${measurable.parentData} vs ${previousDatas[index]}")
            if (measurable.parentData as? ConstraintLayoutParentData != previousDatas[index]) {
                return true
            }
        }

        return false
    }

    private val previousDatas = mutableListOf<ConstraintLayoutParentData?>()

    override fun onRemembered() {
        observer.start()
    }

    override fun onForgotten() {
        observer.stop()
        observer.clear()
    }

    override fun onAbandoned() {}
}

/**
 * Layout that positions its children according to the constraints between them.
 *
 * Example usage:
 * @sample androidx.compose.foundation.layout.samples.DemoConstraintSet
 */
@Suppress("NOTHING_TO_INLINE")
@Composable
inline fun ConstraintLayout(
    constraintSet: ConstraintSet,
    modifier: Modifier = Modifier,
    optimizationLevel: Int = Optimizer.OPTIMIZATION_STANDARD,
    noinline content: @Composable () -> Unit
) {
    val measurer = remember { Measurer() }
    val measurePolicy = rememberConstraintLayoutMeasurePolicy(optimizationLevel, constraintSet, measurer)
    @Suppress("DEPRECATION")
    MultiMeasureLayout(
        modifier = modifier.semantics { designInfoProvider = measurer },
        measurePolicy = measurePolicy,
        content = content
    )
}

@Composable
@PublishedApi
internal fun rememberConstraintLayoutMeasurePolicy(
    optimizationLevel: Int,
    constraintSet: ConstraintSet,
    measurer: Measurer
) = remember(optimizationLevel, constraintSet) {
    MeasurePolicy { measurables, constraints ->
        val layoutSize = measurer.performMeasure(
            constraints,
            layoutDirection,
            constraintSet,
            measurables,
            optimizationLevel,
            this
        )
        layout(layoutSize.width, layoutSize.height) {
            with(measurer) { performLayout(measurables) }
        }
    }
}

/**
 * Represents a layout within a [ConstraintLayout].
 */
@Stable
class ConstrainedLayoutReference(val id: Any) {
    /**
     * The start anchor of this layout. Represents left in LTR layout direction, or right in RTL.
     */
    @Stable
    val start = ConstraintLayoutBaseScope.VerticalAnchor(id, -2)

    /**
     * The left anchor of this layout.
     */
    @Stable
    val absoluteLeft = ConstraintLayoutBaseScope.VerticalAnchor(id, 0)

    /**
     * The top anchor of this layout.
     */
    @Stable
    val top = ConstraintLayoutBaseScope.HorizontalAnchor(id, 0)

    /**
     * The end anchor of this layout. Represents right in LTR layout direction, or left in RTL.
     */
    @Stable
    val end = ConstraintLayoutBaseScope.VerticalAnchor(id, -1)

    /**
     * The right anchor of this layout.
     */
    @Stable
    val absoluteRight = ConstraintLayoutBaseScope.VerticalAnchor(id, 1)

    /**
     * The bottom anchor of this layout.
     */
    @Stable
    val bottom = ConstraintLayoutBaseScope.HorizontalAnchor(id, 1)

    /**
     * The baseline anchor of this layout.
     */
    @Stable
    val baseline = ConstraintLayoutBaseScope.BaselineAnchor(id)
}

/**
 * Common scope for [ConstraintLayoutScope] and [ConstraintSetScope], the content being shared
 * between the inline DSL API and the ConstraintSet-based API.
 */
abstract class ConstraintLayoutBaseScope {
    protected val tasks = mutableListOf<(State) -> Unit>()

    fun applyTo(state: State) = tasks.forEach { it(state) }

    open fun reset() {
        tasks.clear()
        helperId = HelpersStartId
        helpersHashCode = 0
    }

    @PublishedApi internal var helpersHashCode: Int = 0
    private fun updateHelpersHashCode(value: Int) {
        helpersHashCode = (helpersHashCode * 1009 + value) % 1000000007
    }

    private val HelpersStartId = 1000
    private var helperId = HelpersStartId
    private fun createHelperId() = helperId++

    /**
     * Represents a vertical anchor (e.g. start/end of a layout, guideline) that layouts
     * can link to in their `Modifier.constrainAs` or `constrain` blocks.
     */
    @Stable
    data class VerticalAnchor internal constructor(internal val id: Any, internal val index: Int)

    /**
     * Represents a horizontal anchor (e.g. top/bottom of a layout, guideline) that layouts
     * can link to in their `Modifier.constrainAs` or `constrain` blocks.
     */
    @Stable
    data class HorizontalAnchor internal constructor(internal val id: Any, internal val index: Int)

    /**
     * Represents a horizontal anchor corresponding to the [FirstBaseline] of a layout that other
     * layouts can link to in their `Modifier.constrainAs` or `constrain` blocks.
     */
    // TODO(popam): investigate if this can be just a HorizontalAnchor
    @Stable
    data class BaselineAnchor internal constructor(internal val id: Any)

    /**
     * Creates a guideline at a specific offset from the start of the [ConstraintLayout].
     */
    fun createGuidelineFromStart(offset: Dp): VerticalAnchor {
        val id = createHelperId()
        tasks.add { state ->
            state.verticalGuideline(id).apply {
                if (state.layoutDirection == LayoutDirection.Ltr) start(offset) else end(offset)
            }
        }
        updateHelpersHashCode(1)
        updateHelpersHashCode(offset.hashCode())
        return VerticalAnchor(id, 0)
    }

    /**
     * Creates a guideline at a specific offset from the left of the [ConstraintLayout].
     */
    fun createGuidelineFromAbsoluteLeft(offset: Dp): VerticalAnchor {
        val id = createHelperId()
        tasks.add { state -> state.verticalGuideline(id).apply { start(offset) } }
        updateHelpersHashCode(2)
        updateHelpersHashCode(offset.hashCode())
        return VerticalAnchor(id, 0)
    }

    /**
     * Creates a guideline at a specific offset from the start of the [ConstraintLayout].
     * A [fraction] of 0f will correspond to the start of the [ConstraintLayout], while 1f will
     * correspond to the end.
     */
    fun createGuidelineFromStart(fraction: Float): VerticalAnchor {
        val id = createHelperId()
        tasks.add { state ->
            state.verticalGuideline(id).apply {
                if (state.layoutDirection == LayoutDirection.Ltr) {
                    percent(fraction)
                } else {
                    percent(1f - fraction)
                }
            }
        }
        updateHelpersHashCode(3)
        updateHelpersHashCode(fraction.hashCode())
        return VerticalAnchor(id, 0)
    }

    /**
     * Creates a guideline at a width fraction from the left of the [ConstraintLayout].
     * A [fraction] of 0f will correspond to the left of the [ConstraintLayout], while 1f will
     * correspond to the right.
     */
    // TODO(popam, b/157781990): this is not really percenide
    fun createGuidelineFromAbsoluteLeft(fraction: Float): VerticalAnchor {
        val id = createHelperId()
        tasks.add { state -> state.verticalGuideline(id).apply { percent(fraction) } }
        updateHelpersHashCode(4)
        updateHelpersHashCode(fraction.hashCode())
        return VerticalAnchor(id, 0)
    }

    /**
     * Creates a guideline at a specific offset from the end of the [ConstraintLayout].
     */
    fun createGuidelineFromEnd(offset: Dp): VerticalAnchor {
        val id = createHelperId()
        tasks.add { state ->
            state.verticalGuideline(id).apply {
                if (state.layoutDirection == LayoutDirection.Ltr) end(offset) else start(offset)
            }
        }
        updateHelpersHashCode(5)
        updateHelpersHashCode(offset.hashCode())
        return VerticalAnchor(id, 0)
    }

    /**
     * Creates a guideline at a specific offset from the right of the [ConstraintLayout].
     */
    fun createGuidelineFromAbsoluteRight(offset: Dp): VerticalAnchor {
        val id = createHelperId()
        tasks.add { state -> state.verticalGuideline(id).apply { end(offset) } }
        updateHelpersHashCode(6)
        updateHelpersHashCode(offset.hashCode())
        return VerticalAnchor(id, 0)
    }

    /**
     * Creates a guideline at a width fraction from the end of the [ConstraintLayout].
     * A [fraction] of 0f will correspond to the end of the [ConstraintLayout], while 1f will
     * correspond to the start.
     */
    fun createGuidelineFromEnd(fraction: Float): VerticalAnchor {
        return createGuidelineFromStart(1f - fraction)
    }

    /**
     * Creates a guideline at a width fraction from the right of the [ConstraintLayout].
     * A [fraction] of 0f will correspond to the right of the [ConstraintLayout], while 1f will
     * correspond to the left.
     */
    fun createGuidelineFromAbsoluteRight(fraction: Float): VerticalAnchor {
        return createGuidelineFromAbsoluteLeft(1f - fraction)
    }

    /**
     * Creates a guideline at a specific offset from the top of the [ConstraintLayout].
     */
    fun createGuidelineFromTop(offset: Dp): HorizontalAnchor {
        val id = createHelperId()
        tasks.add { state -> state.horizontalGuideline(id).apply { start(offset) } }
        updateHelpersHashCode(7)
        updateHelpersHashCode(offset.hashCode())
        return HorizontalAnchor(id, 0)
    }

    /**
     * Creates a guideline at a height percenide from the top of the [ConstraintLayout].
     * A [fraction] of 0f will correspond to the top of the [ConstraintLayout], while 1f will
     * correspond to the bottom.
     */
    fun createGuidelineFromTop(fraction: Float): HorizontalAnchor {
        val id = createHelperId()
        tasks.add { state -> state.horizontalGuideline(id).apply { percent(fraction) } }
        updateHelpersHashCode(8)
        updateHelpersHashCode(fraction.hashCode())
        return HorizontalAnchor(id, 0)
    }

    /**
     * Creates a guideline at a specific offset from the bottom of the [ConstraintLayout].
     */
    fun createGuidelineFromBottom(offset: Dp): HorizontalAnchor {
        val id = createHelperId()
        tasks.add { state -> state.horizontalGuideline(id).apply { end(offset) } }
        updateHelpersHashCode(9)
        updateHelpersHashCode(offset.hashCode())
        return HorizontalAnchor(id, 0)
    }

    /**
     * Creates a guideline at a height percenide from the bottom of the [ConstraintLayout].
     * A [fraction] of 0f will correspond to the bottom of the [ConstraintLayout], while 1f will
     * correspond to the top.
     */
    fun createGuidelineFromBottom(fraction: Float): HorizontalAnchor {
        return createGuidelineFromTop(1f - fraction)
    }

    /**
     * Creates and returns a start barrier, containing the specified elements.
     */
    fun createStartBarrier(
        vararg elements: ConstrainedLayoutReference,
        margin: Dp = 0.dp
    ): VerticalAnchor {
        val id = createHelperId()
        tasks.add { state ->
            val direction = if (state.layoutDirection == LayoutDirection.Ltr) {
                SolverDirection.LEFT
            } else {
                SolverDirection.RIGHT
            }
            state.barrier(id, direction).apply {
                add(*(elements.map { it.id }.toTypedArray()))
            }.margin(state.convertDimension(margin))
        }
        updateHelpersHashCode(10)
        elements.forEach { updateHelpersHashCode(it.hashCode()) }
        updateHelpersHashCode(margin.hashCode())
        return VerticalAnchor(id, 0)
    }

    /**
     * Creates and returns a left barrier, containing the specified elements.
     */
    fun createAbsoluteLeftBarrier(
        vararg elements: ConstrainedLayoutReference,
        margin: Dp = 0.dp
    ): VerticalAnchor {
        val id = createHelperId()
        tasks.add { state ->
            state.barrier(id, SolverDirection.LEFT).apply {
                add(*(elements.map { it.id }.toTypedArray()))
            }.margin(state.convertDimension(margin))
        }
        updateHelpersHashCode(11)
        elements.forEach { updateHelpersHashCode(it.hashCode()) }
        updateHelpersHashCode(margin.hashCode())
        return VerticalAnchor(id, 0)
    }

    /**
     * Creates and returns a top barrier, containing the specified elements.
     */
    fun createTopBarrier(
        vararg elements: ConstrainedLayoutReference,
        margin: Dp = 0.dp
    ): HorizontalAnchor {
        val id = createHelperId()
        tasks.add { state ->
            state.barrier(id, SolverDirection.TOP).apply {
                add(*(elements.map { it.id }.toTypedArray()))
            }.margin(state.convertDimension(margin))
        }
        updateHelpersHashCode(12)
        elements.forEach { updateHelpersHashCode(it.hashCode()) }
        updateHelpersHashCode(margin.hashCode())
        return HorizontalAnchor(id, 0)
    }

    /**
     * Creates and returns an end barrier, containing the specified elements.
     */
    fun createEndBarrier(
        vararg elements: ConstrainedLayoutReference,
        margin: Dp = 0.dp
    ): VerticalAnchor {
        val id = createHelperId()
        tasks.add { state ->
            val direction = if (state.layoutDirection == LayoutDirection.Ltr) {
                SolverDirection.RIGHT
            } else {
                SolverDirection.LEFT
            }
            state.barrier(id, direction).apply {
                add(*(elements.map { it.id }.toTypedArray()))
            }.margin(state.convertDimension(margin))
        }
        updateHelpersHashCode(13)
        elements.forEach { updateHelpersHashCode(it.hashCode()) }
        updateHelpersHashCode(margin.hashCode())
        return VerticalAnchor(id, 0)
    }

    /**
     * Creates and returns a right barrier, containing the specified elements.
     */
    fun createAbsoluteRightBarrier(
        vararg elements: ConstrainedLayoutReference,
        margin: Dp = 0.dp
    ): VerticalAnchor {
        val id = createHelperId()
        tasks.add { state ->
            state.barrier(id, SolverDirection.RIGHT).apply {
                add(*(elements.map { it.id }.toTypedArray()))
            }.margin(state.convertDimension(margin))
        }
        updateHelpersHashCode(14)
        elements.forEach { updateHelpersHashCode(it.hashCode()) }
        updateHelpersHashCode(margin.hashCode())
        return VerticalAnchor(id, 0)
    }

    /**
     * Creates and returns a bottom barrier, containing the specified elements.
     */
    fun createBottomBarrier(
        vararg elements: ConstrainedLayoutReference,
        margin: Dp = 0.dp
    ): HorizontalAnchor {
        val id = createHelperId()
        tasks.add { state ->
            state.barrier(id, SolverDirection.BOTTOM).apply {
                add(*(elements.map { it.id }.toTypedArray()))
            }.margin(state.convertDimension(margin))
        }
        updateHelpersHashCode(15)
        elements.forEach { updateHelpersHashCode(it.hashCode()) }
        updateHelpersHashCode(margin.hashCode())
        return HorizontalAnchor(id, 0)
    }

    /**
     * Creates a horizontal chain including the referenced layouts.
     */
    // TODO(popam, b/157783937): this API should be improved
    fun createHorizontalChain(
        vararg elements: ConstrainedLayoutReference,
        chainStyle: ChainStyle = ChainStyle.Spread
    ) {
        tasks.add { state ->
            state.horizontalChain(*(elements.map { it.id }.toTypedArray()))
                .also { it.style(chainStyle.style) }
                .apply()
            if (chainStyle.bias != null) {
                state.constraints(elements[0].id).horizontalBias(chainStyle.bias)
            }
        }
        updateHelpersHashCode(16)
        elements.forEach { updateHelpersHashCode(it.hashCode()) }
        updateHelpersHashCode(chainStyle.hashCode())
    }

    /**
     * Creates a vertical chain including the referenced layouts.
     */
    // TODO(popam, b/157783937): this API should be improved
    fun createVerticalChain(
        vararg elements: ConstrainedLayoutReference,
        chainStyle: ChainStyle = ChainStyle.Spread
    ) {
        tasks.add { state ->
            state.verticalChain(*(elements.map { it.id }.toTypedArray()))
                .also { it.style(chainStyle.style) }
                .apply()
            if (chainStyle.bias != null) {
                state.constraints(elements[0].id).verticalBias(chainStyle.bias)
            }
        }
        updateHelpersHashCode(17)
        elements.forEach { updateHelpersHashCode(it.hashCode()) }
        updateHelpersHashCode(chainStyle.hashCode())
    }
}

/**
 * Scope used by the inline DSL of [ConstraintLayout].
 */
@LayoutScopeMarker
class ConstraintLayoutScope @PublishedApi internal constructor() : ConstraintLayoutBaseScope() {
    /**
     * Creates one [ConstrainedLayoutReference], which needs to be assigned to a layout within the
     * [ConstraintLayout] as part of [Modifier.constrainAs]. To create more references at the
     * same time, see [createRefs].
     */
    fun createRef() = childrenRefs.getOrNull(childId++) ?:
        ConstrainedLayoutReference(childId).also { childrenRefs.add(it) }

    /**
     * Convenient way to create multiple [ConstrainedLayoutReference]s, which need to be assigned
     * to layouts within the [ConstraintLayout] as part of [Modifier.constrainAs]. To create just
     * one reference, see [createRef].
     */
    @Stable
    fun createRefs() =
            referencesObject ?: ConstrainedLayoutReferences().also { referencesObject = it }
    private var referencesObject: ConstrainedLayoutReferences? = null

    private val ChildrenStartIndex = 0
    private var childId = ChildrenStartIndex
    private val childrenRefs = ArrayList<ConstrainedLayoutReference>()
    override fun reset() {
        super.reset()
        childId = ChildrenStartIndex
    }

    /**
     * Convenience API for creating multiple [ConstrainedLayoutReference] via [createRefs].
     */
    inner class ConstrainedLayoutReferences internal constructor() {
        operator fun component1() = createRef()
        operator fun component2() = createRef()
        operator fun component3() = createRef()
        operator fun component4() = createRef()
        operator fun component5() = createRef()
        operator fun component6() = createRef()
        operator fun component7() = createRef()
        operator fun component8() = createRef()
        operator fun component9() = createRef()
        operator fun component10() = createRef()
        operator fun component11() = createRef()
        operator fun component12() = createRef()
        operator fun component13() = createRef()
        operator fun component14() = createRef()
        operator fun component15() = createRef()
        operator fun component16() = createRef()
    }

    /**
     * [Modifier] that defines the constraints, as part of a [ConstraintLayout], of the layout
     * element.
     */
    @Stable
    fun Modifier.constrainAs(
        ref: ConstrainedLayoutReference,
        constrainBlock: ConstrainScope.() -> Unit
    ) = this.then(ConstrainAsModifier(ref, constrainBlock))

    @Stable
    private class ConstrainAsModifier(
        private val ref: ConstrainedLayoutReference,
        private val constrainBlock: ConstrainScope.() -> Unit
    ) : ParentDataModifier, InspectorValueInfo(
        debugInspectorInfo {
            name = "constrainAs"
            properties["ref"] = ref
            properties["constrainBlock"] = constrainBlock
        }
    ) {
        override fun Density.modifyParentData(parentData: Any?) =
            ConstraintLayoutParentData(ref, constrainBlock)

        override fun hashCode() = constrainBlock.hashCode()

        override fun equals(other: Any?) =
            constrainBlock == (other as? ConstrainAsModifier)?.constrainBlock
    }
}

/**
 * Scope used by the [ConstraintSet] DSL.
 */
@LayoutScopeMarker
class ConstraintSetScope internal constructor() : ConstraintLayoutBaseScope() {
    /**
     * Creates one [ConstrainedLayoutReference] corresponding to the [ConstraintLayout] element
     * with [id].
     */
    fun createRefFor(id: Any) = ConstrainedLayoutReference(id)

    /**
     * Specifies the constraints associated to the layout identified with [ref].
     */
    fun constrain(
        ref: ConstrainedLayoutReference,
        constrainBlock: ConstrainScope.() -> Unit
    ) = ConstrainScope(ref.id).apply {
        constrainBlock()
        this@ConstraintSetScope.tasks.addAll(this.tasks)
    }
}

/**
 * The style of a horizontal or vertical chain.
 */
@Immutable
class ChainStyle internal constructor(
    internal val style: SolverChain,
    internal val bias: Float? = null
) {
    companion object {
        /**
         * A chain style that evenly distributes the contained layouts.
         */
        @Stable
        val Spread = ChainStyle(SolverChain.SPREAD)

        /**
         * A chain style where the first and last layouts are affixed to the constraints
         * on each end of the chain and the rest are evenly distributed.
         */
        @Stable
        val SpreadInside = ChainStyle(SolverChain.SPREAD_INSIDE)

        /**
         * A chain style where the contained layouts are packed together and placed to the
         * center of the available space.
         */
        @Stable
        val Packed = Packed(0.5f)

        /**
         * A chain style where the contained layouts are packed together and placed in
         * the available space according to a given [bias].
         */
        @Stable
        fun Packed(bias: Float) = ChainStyle(SolverChain.PACKED, bias)
    }
}

/**
 * Parent data provided by `Modifier.constrainAs`.
 */
private class ConstraintLayoutParentData(
    val ref: ConstrainedLayoutReference,
    val constrain: ConstrainScope.() -> Unit
) : LayoutIdParentData {
    override val layoutId: Any = ref.id

    override fun equals(other: Any?) = other is ConstraintLayoutParentData &&
            ref.id == other.ref.id && constrain == other.constrain

    override fun hashCode() = ref.id.hashCode() * 31 + constrain.hashCode()
}

/**
 * Scope used by `Modifier.constrainAs`.
 */
@LayoutScopeMarker
@Stable
class ConstrainScope internal constructor(internal val id: Any) {
    internal val tasks = mutableListOf<(State) -> Unit>()
    internal fun applyTo(state: State) = tasks.forEach { it(state) }

    /**
     * Reference to the [ConstraintLayout] itself, which can be used to specify constraints
     * between itself and its children.
     */
    val parent = ConstrainedLayoutReference(SolverState.PARENT)

    /**
     * The start anchor of the layout - can be constrained using [VerticalAnchorable.linkTo].
     */
    val start = VerticalAnchorable(id, -2)

    /**
     * The left anchor of the layout - can be constrained using [VerticalAnchorable.linkTo].
     */
    val absoluteLeft = VerticalAnchorable(id, 0)

    /**
     * The top anchor of the layout - can be constrained using [HorizontalAnchorable.linkTo].
     */
    val top = HorizontalAnchorable(id, 0)

    /**
     * The end anchor of the layout - can be constrained using [VerticalAnchorable.linkTo].
     */
    val end = VerticalAnchorable(id, -1)

    /**
     * The right anchor of the layout - can be constrained using [VerticalAnchorable.linkTo].
     */
    val absoluteRight = VerticalAnchorable(id, 1)

    /**
     * The bottom anchor of the layout - can be constrained using [HorizontalAnchorable.linkTo].
     */
    val bottom = HorizontalAnchorable(id, 1)

    /**
     * The [FirstBaseline] of the layout - can be constrained using [BaselineAnchorable.linkTo].
     */
    val baseline = BaselineAnchorable(id)

    /**
     * The width of the [ConstraintLayout] child.
     */
    var width: Dimension = Dimension.wrapContent
        set(value) {
            field = value
            tasks.add { state ->
                state.constraints(id).width(
                    (value as DimensionDescription).toSolverDimension(state)
                )
            }
        }

    /**
     * The height of the [ConstraintLayout] child.
     */
    var height: Dimension = Dimension.wrapContent
        set(value) {
            field = value
            tasks.add { state ->
                state.constraints(id).height(
                    (value as DimensionDescription).toSolverDimension(state)
                )
            }
        }

    /**
     * Represents a vertical side of a layout (i.e start and end) that can be anchored using
     * [linkTo] in their `Modifier.constrainAs` blocks.
     */
    inner class VerticalAnchorable internal constructor(
        internal val id: Any,
        internal val index: Int
    ) {
        /**
         * Adds a link towards a [ConstraintLayoutBaseScope.VerticalAnchor].
         */
        // TODO(popam, b/158069248): add parameter for gone margin
        fun linkTo(anchor: ConstraintLayoutBaseScope.VerticalAnchor, margin: Dp = 0.dp) {
            tasks.add { state ->
                with(state.constraints(id)) {
                    val layoutDirection = state.layoutDirection
                    val index1 = verticalAnchorIndexToFunctionIndex(index, layoutDirection)
                    val index2 = verticalAnchorIndexToFunctionIndex(anchor.index, layoutDirection)
                    verticalAnchorFunctions[index1][index2]
                        .invoke(this, anchor.id, state.layoutDirection)
                        .margin(margin)
                }
            }
        }
    }

    /**
     * Represents a horizontal side of a layout (i.e top and bottom) that can be anchored using
     * [linkTo] in their `Modifier.constrainAs` blocks.
     */
    inner class HorizontalAnchorable internal constructor(
        internal val tag: Any,
        internal val index: Int
    ) {
        /**
         * Adds a link towards a [ConstraintLayoutBaseScope.HorizontalAnchor].
         */
        // TODO(popam, b/158069248): add parameter for gone margin
        fun linkTo(anchor: ConstraintLayoutBaseScope.HorizontalAnchor, margin: Dp = 0.dp) {
            tasks.add { state ->
                with(state.constraints(id)) {
                    horizontalAnchorFunctions[index][anchor.index]
                        .invoke(this, anchor.id)
                        .margin(margin)
                }
            }
        }
    }

    /**
     * Represents the [FirstBaseline] of a layout that can be anchored
     * using [linkTo] in their `Modifier.constrainAs` blocks.
     */
    inner class BaselineAnchorable internal constructor(internal val id: Any) {
        /**
         * Adds a link towards a [ConstraintLayoutBaseScope.BaselineAnchor].
         */
        // TODO(popam, b/158069248): add parameter for gone margin
        fun linkTo(anchor: ConstraintLayoutBaseScope.BaselineAnchor, margin: Dp = 0.dp) {
            tasks.add { state ->
                (state as? State)?.let {
                    it.baselineNeededFor(id)
                    it.baselineNeededFor(anchor.id)
                }
                with(state.constraints(id)) {
                    baselineAnchorFunction.invoke(this, anchor.id).margin(margin)
                }
            }
        }
    }

    /**
     * Adds both start and end links towards other [ConstraintLayoutBaseScope.HorizontalAnchor]s.
     */
    // TODO(popam, b/158069248): add parameter for gone margin
    fun linkTo(
        start: ConstraintLayoutBaseScope.VerticalAnchor,
        end: ConstraintLayoutBaseScope.VerticalAnchor,
        startMargin: Dp = 0.dp,
        endMargin: Dp = 0.dp,
        @FloatRange(from = 0.0, to = 1.0) bias: Float = 0.5f
    ) {
        this@ConstrainScope.start.linkTo(start, startMargin)
        this@ConstrainScope.end.linkTo(end, endMargin)
        tasks.add { state ->
            state.constraints(id).horizontalBias(bias)
        }
    }

    /**
     * Adds both top and bottom links towards other [ConstraintLayoutBaseScope.HorizontalAnchor]s.
     */
    // TODO(popam, b/158069248): add parameter for gone margin
    fun linkTo(
        top: ConstraintLayoutBaseScope.HorizontalAnchor,
        bottom: ConstraintLayoutBaseScope.HorizontalAnchor,
        topMargin: Dp = 0.dp,
        bottomMargin: Dp = 0.dp,
        @FloatRange(from = 0.0, to = 1.0) bias: Float = 0.5f
    ) {
        this@ConstrainScope.top.linkTo(top, topMargin)
        this@ConstrainScope.bottom.linkTo(bottom, bottomMargin)
        tasks.add { state ->
            state.constraints(id).verticalBias(bias)
        }
    }

    /**
     * Adds all start, top, end, bottom links towards
     * other [ConstraintLayoutBaseScope.HorizontalAnchor]s.
     */
    // TODO(popam, b/158069248): add parameter for gone margin
    fun linkTo(
        start: ConstraintLayoutBaseScope.VerticalAnchor,
        top: ConstraintLayoutBaseScope.HorizontalAnchor,
        end: ConstraintLayoutBaseScope.VerticalAnchor,
        bottom: ConstraintLayoutBaseScope.HorizontalAnchor,
        startMargin: Dp = 0.dp,
        topMargin: Dp = 0.dp,
        endMargin: Dp = 0.dp,
        bottomMargin: Dp = 0.dp,
        @FloatRange(from = 0.0, to = 1.0) horizontalBias: Float = 0.5f,
        @FloatRange(from = 0.0, to = 1.0) verticalBias: Float = 0.5f
    ) {
        linkTo(start, end, startMargin, endMargin, horizontalBias)
        linkTo(top, bottom, topMargin, bottomMargin, verticalBias)
    }

    /**
     * Adds all start, top, end, bottom links towards the corresponding anchors of [other].
     * This will center the current layout inside or around (depending on size) [other].
     */
    fun centerTo(other: ConstrainedLayoutReference) {
        linkTo(other.start, other.top, other.end, other.bottom)
    }

    /**
     * Adds start and end links towards the corresponding anchors of [other].
     * This will center horizontally the current layout inside or around (depending on size)
     * [other].
     */
    fun centerHorizontallyTo(other: ConstrainedLayoutReference) {
        linkTo(other.start, other.end)
    }

    /**
     * Adds top and bottom links towards the corresponding anchors of [other].
     * This will center vertically the current layout inside or around (depending on size)
     * [other].
     */
    fun centerVerticallyTo(other: ConstrainedLayoutReference) {
        linkTo(other.top, other.bottom)
    }

    /**
     * Adds start and end links towards a vertical [anchor].
     * This will center the current layout around the vertical [anchor].
     */
    fun centerAround(anchor: ConstraintLayoutBaseScope.VerticalAnchor) {
        linkTo(anchor, anchor)
    }

    /**
     * Adds top and bottom links towards a horizontal [anchor].
     * This will center the current layout around the horizontal [anchor].
     */
    fun centerAround(anchor: ConstraintLayoutBaseScope.HorizontalAnchor) {
        linkTo(anchor, anchor)
    }

    internal companion object {
        val verticalAnchorFunctions:
            Array<Array<ConstraintReference.(Any, LayoutDirection) -> ConstraintReference>> =
            arrayOf(
                arrayOf(
                    { other, layoutDirection ->
                        clearLeft(layoutDirection); leftToLeft(other)
                    },
                    { other, layoutDirection ->
                        clearLeft(layoutDirection); leftToRight(other)
                    }
                ),
                arrayOf(
                    { other, layoutDirection ->
                        clearRight(layoutDirection); rightToLeft(other)
                    },
                    { other, layoutDirection ->
                        clearRight(layoutDirection); rightToRight(other)
                    }
                )
            )

        private fun ConstraintReference.clearLeft(layoutDirection: LayoutDirection) {
            leftToLeft(null)
            leftToRight(null)
            when (layoutDirection) {
                LayoutDirection.Ltr -> {
                    startToStart(null); startToEnd(null)
                }
                LayoutDirection.Rtl -> {
                    endToStart(null); endToEnd(null)
                }
            }
        }

        private fun ConstraintReference.clearRight(layoutDirection: LayoutDirection) {
            rightToLeft(null)
            rightToRight(null)
            when (layoutDirection) {
                LayoutDirection.Ltr -> {
                    endToStart(null); endToEnd(null)
                }
                LayoutDirection.Rtl -> {
                    startToStart(null); startToEnd(null)
                }
            }
        }

        /**
         * Converts the index (-2 -> start, -1 -> end, 0 -> left, 1 -> right) to an index in
         * the arrays above (0 -> left, 1 -> right).
         */
        // TODO(popam, b/157886946): this is temporary until we can use CL's own RTL handling
        fun verticalAnchorIndexToFunctionIndex(index: Int, layoutDirection: LayoutDirection) =
            when {
                index >= 0 -> index // already left or right
                layoutDirection == LayoutDirection.Ltr -> 2 + index // start -> left, end -> right
                else -> -index - 1 // start -> right, end -> left
            }

        val horizontalAnchorFunctions:
            Array<Array<ConstraintReference.(Any) -> ConstraintReference>> = arrayOf(
            arrayOf(
                { other -> topToBottom(null); baselineToBaseline(null); topToTop(other) },
                { other -> topToTop(null); baselineToBaseline(null); topToBottom(other) }
            ),
            arrayOf(
                { other -> bottomToBottom(null); baselineToBaseline(null); bottomToTop(other) },
                { other -> bottomToTop(null); baselineToBaseline(null); bottomToBottom(other) }
            )
        )
        val baselineAnchorFunction: ConstraintReference.(Any) -> ConstraintReference =
            { other ->
                topToTop(null)
                topToBottom(null)
                bottomToTop(null)
                bottomToBottom(null)
                baselineToBaseline(other)
            }
    }
}

/**
 * Convenience for creating ids corresponding to layout references that cannot be referred
 * to from the outside of the scope (e.g. barriers, layout references in the modifier-based API,
 * etc.).
 */
private fun createId() = object : Any() {}

/**
 * Represents a dimension that can be assigned to the width or height of a [ConstraintLayout]
 * [child][ConstrainedLayoutReference].
 */
// TODO(popam, b/157781841): It is unfortunate that this interface is top level in
// `foundation-layout`. This will be ok if we move constraint layout to its own module or at
// least subpackage.
interface Dimension {
    /**
     * A [Dimension] that can be assigned both min and max bounds.
     */
    interface Coercible : Dimension

    /**
     * A [Dimension] that can be assigned a min bound.
     */
    interface MinCoercible : Dimension

    /**
     * A [Dimension] that can be assigned a max bound.
     */
    interface MaxCoercible : Dimension

    companion object {
        /**
         * Creates a [Dimension] representing a suggested dp size. The requested size will
         * be respected unless the constraints in the [ConstraintSet] do not allow it. The min
         * and max bounds will be respected regardless of the constraints in the [ConstraintSet].
         * To make the value fixed (respected regardless the [ConstraintSet]), [value] should
         * be used instead.
         */
        fun preferredValue(dp: Dp): Dimension.Coercible =
            DimensionDescription { state -> SolverDimension.Suggested(state.convertDimension(dp)) }

        /**
         * Creates a [Dimension] representing a fixed dp size. The size will not change
         * according to the constraints in the [ConstraintSet].
         */
        fun value(dp: Dp): Dimension =
            DimensionDescription { state -> SolverDimension.Fixed(state.convertDimension(dp)) }

        /**
         * A [Dimension] with suggested wrap content behavior. The wrap content size
         * will be respected unless the constraints in the [ConstraintSet] do not allow it.
         * To make the value fixed (respected regardless the [ConstraintSet]), [wrapContent]
         * should be used instead.
         */
        val preferredWrapContent: Dimension.Coercible
            get() = DimensionDescription { SolverDimension.Suggested(WRAP_DIMENSION) }

        /**
         * A [Dimension] with fixed wrap content behavior. The size will not change
         * according to the constraints in the [ConstraintSet].
         */
        val wrapContent: Dimension
            get() = DimensionDescription { SolverDimension.Fixed(WRAP_DIMENSION) }

        /**
         * A [Dimension] that spreads to match constraints. Links should be specified from both
         * sides corresponding to this dimension, in order for this to work.
         */
        val fillToConstraints: Dimension
            get() = DimensionDescription { SolverDimension.Suggested(SPREAD_DIMENSION) }

        /**
         * A [Dimension] that is a percent of the parent in the corresponding direction.
         */
        fun percent(percent: Float): Dimension =
            // TODO(popam, b/157880732): make this nicer when possible in future solver releases
            DimensionDescription { SolverDimension.Percent(0, percent).suggested(0) }
    }
}

/**
 * Sets the lower bound of the current [Dimension] to be the wrap content size of the child.
 */
val Dimension.Coercible.atLeastWrapContent: Dimension.MaxCoercible
    get() = (this as DimensionDescription).also { it.minSymbol = WRAP_DIMENSION }

/**
 * Sets the lower bound of the current [Dimension] to a fixed [dp] value.
 */
fun Dimension.Coercible.atLeast(dp: Dp): Dimension.MaxCoercible =
    (this as DimensionDescription).also { it.min = dp }

/**
 * Sets the upper bound of the current [Dimension] to a fixed [dp] value.
 */
fun Dimension.Coercible.atMost(dp: Dp): Dimension.MinCoercible =
    (this as DimensionDescription).also { it.max = dp }

/**
 * Sets the upper bound of the current [Dimension] to be the wrap content size of the child.
 */
val Dimension.Coercible.atMostWrapContent: Dimension.MinCoercible
    get() = (this as DimensionDescription).also { it.maxSymbol = WRAP_DIMENSION }

/**
 * Sets the lower bound of the current [Dimension] to a fixed [dp] value.
 */
fun Dimension.MinCoercible.atLeastWrapContent(dp: Dp): Dimension =
    (this as DimensionDescription).also { it.min = dp }

/**
 * Sets the lower bound of the current [Dimension] to be the wrap content size of the child.
 */
val Dimension.MinCoercible.atLeastWrapContent: Dimension
    get() = (this as DimensionDescription).also { it.minSymbol = WRAP_DIMENSION }

/**
 * Sets the upper bound of the current [Dimension] to a fixed [dp] value.
 */
fun Dimension.MaxCoercible.atMost(dp: Dp): Dimension =
    (this as DimensionDescription).also { it.max = dp }

/**
 * Sets the upper bound of the current [Dimension] to be the [Wrap] size of the child.
 */
val Dimension.MaxCoercible.atMostWrapContent: Dimension
    get() = (this as DimensionDescription).also { it.maxSymbol = WRAP_DIMENSION }

/**
 * Describes a sizing behavior that can be applied to the width or height of a
 * [ConstraintLayout] child. The content of this class should not be instantiated
 * directly; helpers available in the [Dimension]'s companion object should be used.
 */
internal class DimensionDescription internal constructor(
    private val baseDimension: (State) -> SolverDimension
) : Dimension.Coercible, Dimension.MinCoercible, Dimension.MaxCoercible, Dimension {
    var min: Dp? = null
    var minSymbol: Any? = null
    var max: Dp? = null
    var maxSymbol: Any? = null
    internal fun toSolverDimension(state: State) = baseDimension(state).also {
        if (minSymbol != null) {
            it.min(minSymbol)
        } else if (min != null) {
            it.min(state.convertDimension(min!!))
        }
        if (maxSymbol != null) {
            it.max(maxSymbol)
        } else if (max != null) {
            it.max(state.convertDimension(max!!))
        }
    }
}

/**
 * Immutable description of the constraints used to layout the children of a [ConstraintLayout].
 */
@Immutable
interface ConstraintSet {
    /**
     * Applies the [ConstraintSet] to a state.
     */
    fun applyTo(state: State, measurables: List<Measurable>)

    fun override(name: String, value: Float) = this
    fun applyTo(transition: Transition, type: Int) {
        // nothing here, used in MotionLayout
    }

    fun isDirty(measurables: List<Measurable>): Boolean = true
}

@SuppressLint("ComposableNaming")
@Composable
fun ConstraintSet(@Language("json5") content : String) : ConstraintSet {
    val constraintset = remember {
        mutableStateOf(object : ConstraintSet {
            private val overridedVariables = HashMap<String, Float>()

            override fun applyTo(transition: Transition, type: Int) {
                val layoutVariables = LayoutVariables()
                for (name in overridedVariables.keys) {
                    layoutVariables.putOverride(name, overridedVariables[name]!!)
                }
                parseJSON(content, transition, type, layoutVariables)
            }

            override fun applyTo(state: State, measurables: List<Measurable>) {
                measurables.forEach { measurable ->
                    val layoutId =
                        measurable.layoutId ?: measurable.constraintLayoutId ?: createId()
                    state.map(layoutId, measurable)
                    val tag = measurable.constraintLayoutTag
                    if (tag != null && tag is String && layoutId is String) {
                        state.setTag(layoutId, tag)
                    }
                }
                val layoutVariables = LayoutVariables()
                for (name in overridedVariables.keys) {
                    layoutVariables.putOverride(name, overridedVariables[name]!!)
                }
                parseJSON(content, state, layoutVariables)
            }

            override fun override(name: String, value: Float): ConstraintSet {
                overridedVariables[name] = value
                return this
            }
        })
    }

    return constraintset.value
}

/**
 * Creates a [ConstraintSet].
 */
fun ConstraintSet(description: ConstraintSetScope.() -> Unit) = object : ConstraintSet {
    override fun applyTo(state: State, measurables: List<Measurable>) {
        measurables.forEach { measurable ->
            state.map((measurable.layoutId ?: createId()), measurable)
        }
        val scope = ConstraintSetScope()
        scope.description()
        scope.applyTo(state)
    }

    override fun override(name: String, value: Float) : ConstraintSet {
        // nothing yet
        return this
    }
}

/**
 * The state of the [ConstraintLayout] solver.
 */
class State(val density: Density) : SolverState() {
    var rootIncomingConstraints: Constraints = Constraints()
    lateinit var layoutDirection: LayoutDirection
    internal val baselineNeeded = mutableListOf<Any>()
    private var dirtyBaselineNeededWidgets = true
    private val baselineNeededWidgets = mutableSetOf<ConstraintWidget>()

    override fun convertDimension(value: Any?): Int {
        return if (value is Dp) {
            with(density) { value.roundToPx() }
        } else {
            super.convertDimension(value)
        }
    }

    override fun reset() {
        // TODO(b/158197001): this should likely be done by the solver
        mReferences.forEach { ref ->
            ref.value?.constraintWidget?.reset()
        }
        mReferences.clear()
        mReferences[PARENT] = mParent
        baselineNeeded.clear()
        dirtyBaselineNeededWidgets = true
        super.reset()
    }

    internal fun baselineNeededFor(id: Any) {
        baselineNeeded.add(id)
        dirtyBaselineNeededWidgets = true
    }

    internal fun isBaselineNeeded(constraintWidget: ConstraintWidget): Boolean {
        if (dirtyBaselineNeededWidgets) {
            baselineNeededWidgets.clear()
            baselineNeeded.forEach { id ->
                val widget = mReferences[id]?.constraintWidget
                if (widget != null) baselineNeededWidgets.add(widget)
            }
            dirtyBaselineNeededWidgets = false
        }
        return constraintWidget in baselineNeededWidgets
    }

    internal fun getKeyId(helperWidget: HelperWidget): Any? {
        return mHelperReferences.entries.firstOrNull { it.value.helperWidget == helperWidget }?.key
    }
}

@PublishedApi
internal open class Measurer : BasicMeasure.Measurer, DesignInfoProvider {
    protected val root = ConstraintWidgetContainer(0, 0).also { it.measurer = this }
    protected val placeables = mutableMapOf<Measurable, Placeable>()
    private val lastMeasures = mutableMapOf<Measurable, Array<Int>>()
    protected val frameCache = mutableMapOf<Measurable, WidgetFrame>()

    protected lateinit var density: Density
    protected lateinit var measureScope: MeasureScope
    protected val state by lazy(LazyThreadSafetyMode.NONE) { State(density) }

    private val widthConstraintsHolder = IntArray(2)
    private val heightConstraintsHolder = IntArray(2)

    /**
     * Method called by Compose tooling. Returns a JSON string that represents the Constraints
     * defined for this ConstraintLayout Composable.
     */
    override fun getDesignInfo(startX: Int, startY: Int, args: String) =
        parseConstraintsToJson(root, state, startX, startY)

    override fun measure(constraintWidget: ConstraintWidget, measure: BasicMeasure.Measure) {
        val measurable = constraintWidget.companionWidget
        if (measurable !is Measurable) return

        if (DEBUG) {
            Log.d(
                "CCL",
                "Measuring ${measurable.layoutId} with: " +
                    constraintWidget.toDebugString() + "\n" + measure.toDebugString()
            )
        }

        var constraints: Constraints
        run {
            val measurableLastMeasures = lastMeasures[measurable]
            obtainConstraints(
                constraintWidget.horizontalDimensionBehaviour,
                constraintWidget.width,
                constraintWidget.mMatchConstraintDefaultWidth,
                measure.measureStrategy,
                (measurableLastMeasures?.get(1) ?: 0) == constraintWidget.height,
                constraintWidget.isResolvedHorizontally,
                state.rootIncomingConstraints.maxWidth,
                widthConstraintsHolder
            )
            obtainConstraints(
                constraintWidget.verticalDimensionBehaviour,
                constraintWidget.height,
                constraintWidget.mMatchConstraintDefaultHeight,
                measure.measureStrategy,
                (measurableLastMeasures?.get(0) ?: 0) == constraintWidget.width,
                constraintWidget.isResolvedVertically,
                state.rootIncomingConstraints.maxHeight,
                heightConstraintsHolder
            )

            constraints = Constraints(
                widthConstraintsHolder[0],
                widthConstraintsHolder[1],
                heightConstraintsHolder[0],
                heightConstraintsHolder[1]
            )
        }

        if ((measure.measureStrategy == TRY_GIVEN_DIMENSIONS ||
                measure.measureStrategy == USE_GIVEN_DIMENSIONS) ||
            constraintWidget.horizontalDimensionBehaviour != MATCH_CONSTRAINT ||
            constraintWidget.mMatchConstraintDefaultWidth != MATCH_CONSTRAINT_SPREAD ||
            constraintWidget.verticalDimensionBehaviour != MATCH_CONSTRAINT ||
            constraintWidget.mMatchConstraintDefaultHeight != MATCH_CONSTRAINT_SPREAD
        ) {
            if (DEBUG) {
                Log.d("CCL", "Measuring ${measurable.layoutId} with $constraints")
            }
            val placeable = measurable.measure(constraints).also { placeables[measurable] = it }
            if (DEBUG) {
                Log.d(
                    "CCL",
                    "${measurable.layoutId} is size ${placeable.width} ${placeable.height}"
                )
            }

            val coercedWidth = placeable.width.coerceIn(
                constraintWidget.minWidth.takeIf { it > 0 },
                constraintWidget.maxWidth.takeIf { it > 0 }
            )
            val coercedHeight = placeable.height.coerceIn(
                constraintWidget.minHeight.takeIf { it > 0 },
                constraintWidget.maxHeight.takeIf { it > 0 }
            )

            var remeasure = false
            if (coercedWidth != placeable.width) {
                constraints = Constraints(
                    minWidth = coercedWidth,
                    minHeight = constraints.minHeight,
                    maxWidth = coercedWidth,
                    maxHeight = constraints.maxHeight
                )
                remeasure = true
            }
            if (coercedHeight != placeable.height) {
                constraints = Constraints(
                    minWidth = constraints.minWidth,
                    minHeight = coercedHeight,
                    maxWidth = constraints.maxWidth,
                    maxHeight = coercedHeight
                )
                remeasure = true
            }
            if (remeasure) {
                if (DEBUG) {
                    Log.d("CCL", "Remeasuring coerced ${measurable.layoutId} with $constraints")
                }
                measurable.measure(constraints).also { placeables[measurable] = it }
            }
        }

        val currentPlaceable = placeables[measurable]
        measure.measuredWidth = currentPlaceable?.width ?: constraintWidget.width
        measure.measuredHeight = currentPlaceable?.height ?: constraintWidget.height
        val baseline =
            if (currentPlaceable != null && state.isBaselineNeeded(constraintWidget)) {
                currentPlaceable[FirstBaseline]
            } else {
                AlignmentLine.Unspecified
            }
        measure.measuredHasBaseline = baseline != AlignmentLine.Unspecified
        measure.measuredBaseline = baseline
        lastMeasures.getOrPut(measurable, { arrayOf(0, 0, AlignmentLine.Unspecified) })
            .copyFrom(measure)

        measure.measuredNeedsSolverPass = measure.measuredWidth != measure.horizontalDimension ||
            measure.measuredHeight != measure.verticalDimension
    }

    /**
     * Calculates the [Constraints] in one direction that should be used to measure a child,
     * based on the solver measure request. Returns `true` if the constraints correspond to a
     * wrap content measurement.
     */
    private fun obtainConstraints(
        dimensionBehaviour: ConstraintWidget.DimensionBehaviour,
        dimension: Int,
        matchConstraintDefaultDimension: Int,
        measureStrategy: Int,
        otherDimensionResolved: Boolean,
        currentDimensionResolved: Boolean,
        rootMaxConstraint: Int,
        outConstraints: IntArray
    ): Boolean = when (dimensionBehaviour) {
        FIXED -> {
            outConstraints[0] = dimension
            outConstraints[1] = dimension
            false
        }
        WRAP_CONTENT -> {
            outConstraints[0] = 0
            outConstraints[1] = rootMaxConstraint
            true
        }
        MATCH_CONSTRAINT -> {
            if (DEBUG) {
                Log.d("CCL", "Measure strategy ${measureStrategy}")
                Log.d("CCL", "DW ${matchConstraintDefaultDimension}")
                Log.d("CCL", "ODR ${otherDimensionResolved}")
                Log.d("CCL", "IRH ${currentDimensionResolved}")
            }
            val useDimension = currentDimensionResolved ||
                (measureStrategy == TRY_GIVEN_DIMENSIONS ||
                    measureStrategy == USE_GIVEN_DIMENSIONS) &&
                (measureStrategy == USE_GIVEN_DIMENSIONS ||
                    matchConstraintDefaultDimension != MATCH_CONSTRAINT_WRAP ||
                    otherDimensionResolved)
            if (DEBUG) {
                Log.d("CCL", "UD $useDimension")
            }
            outConstraints[0] = if (useDimension) dimension else 0
            outConstraints[1] = if (useDimension) dimension else rootMaxConstraint
            !useDimension
        }
        else -> {
            error("MATCH_PARENT is not supported")
        }
    }

    private fun Array<Int>.copyFrom(measure: BasicMeasure.Measure) {
        this[0] = measure.measuredWidth
        this[1] = measure.measuredHeight
        this[2] = measure.measuredBaseline
    }

    fun performMeasure(
        constraints: Constraints,
        layoutDirection: LayoutDirection,
        constraintSet: ConstraintSet,
        measurables: List<Measurable>,
        optimizationLevel: Int,
        measureScope: MeasureScope
    ): IntSize {
        this.density = measureScope
        this.measureScope = measureScope
        // Define the size of the ConstraintLayout.
        state.width(
            if (constraints.hasFixedWidth) {
                SolverDimension.Fixed(constraints.maxWidth)
            } else {
                SolverDimension.Wrap().min(constraints.minWidth)
            }
        )
        state.height(
            if (constraints.hasFixedHeight) {
                SolverDimension.Fixed(constraints.maxHeight)
            } else {
                SolverDimension.Wrap().min(constraints.minHeight)
            }
        )
        // Build constraint set and apply it to the state.
        state.rootIncomingConstraints = constraints
        state.layoutDirection = layoutDirection
        resetMeasureState()
        if (constraintSet.isDirty(measurables)) {
            state.reset()
            constraintSet.applyTo(state, measurables)
            buildMapping(measurables)
            state.apply(root)
            root.updateHierarchy()
        } else {
            buildMapping(measurables)
        }
        root.width = constraints.maxWidth
        root.height = constraints.maxHeight

        if (DEBUG) {
            root.debugName = "ConstraintLayout"
            root.children.forEach { child ->
                child.debugName =
                    (child.companionWidget as? Measurable)?.layoutId?.toString() ?: "NOTAG"
            }
            Log.d("CCL", "ConstraintLayout is asked to measure with $constraints")
            Log.d("CCL", root.toDebugString())
            for (child in root.children) {
                Log.d("CCL", child.toDebugString())
            }
        }

        // No need to set sizes and size modes as we passed them to the state above.
        root.optimizationLevel = optimizationLevel
        root.measure(root.optimizationLevel, 0, 0, 0, 0, 0, 0, 0, 0)

        for (child in root.children) {
            val measurable = child.companionWidget
            if (measurable !is Measurable) continue
            val placeable = placeables[measurable]
            val currentWidth = placeable?.width
            val currentHeight = placeable?.height
            if (child.width != currentWidth || child.height != currentHeight) {
                if (DEBUG) {
                    Log.d(
                        "CCL",
                        "Final measurement for ${measurable.layoutId} " +
                            "to confirm size ${child.width} ${child.height}"
                    )
                }
                measurable.measure(Constraints.fixed(child.width, child.height))
                    .also { placeables[measurable] = it }
            }
        }
        if (DEBUG) {
            Log.d("CCL", "ConstraintLayout is at the end ${root.width} ${root.height}")
        }

        return IntSize(root.width, root.height)
    }

    internal fun resetMeasureState() {
        placeables.clear()
        lastMeasures.clear()
        frameCache.clear()
    }

    internal fun buildMapping(measurables: List<Measurable>) {
        measurables.fastForEach { measurable ->
            val parentData = measurable.parentData as? ConstraintLayoutParentData
            // Map the id and the measurable, to be retrieved later during measurement.
            val givenTag = parentData?.ref?.id
            state.map(givenTag ?: createId(), measurable)
        }
    }

    fun Placeable.PlacementScope.performLayout(measurables: List<Measurable>) {
        if (frameCache.isEmpty()) {
            for (child in root.children) {
                val measurable = child.companionWidget
                if (measurable !is Measurable) continue
                val frame = WidgetFrame(child.frame.update())
                frameCache[measurable] = frame
            }
        }
        measurables.fastForEach { measurable ->
            val frame = frameCache[measurable]!!
            if (frame.isDefaultTransform) {
                val x = frameCache[measurable]!!.left
                val y = frameCache[measurable]!!.top
                placeables[measurable]?.place(IntOffset(x, y))
            } else {
                val layerBlock: GraphicsLayerScope.() -> Unit = {
                    if (!frame.pivotX.isNaN() || !frame.pivotY.isNaN()) {
                        val pivotX = if (frame.pivotX.isNaN()) 0.5f else frame.pivotX
                        val pivotY = if (frame.pivotY.isNaN()) 0.5f else frame.pivotY
                        transformOrigin = TransformOrigin(pivotX, pivotY)
                    }
                    if (!frame.rotationX.isNaN()) {
                        rotationX = frame.rotationX
                    }
                    if (!frame.rotationY.isNaN()) {
                        rotationY = frame.rotationY
                    }
                    if (!frame.rotationZ.isNaN()) {
                        rotationZ = frame.rotationZ
                    }
                    if (!frame.translationX.isNaN()) {
                        translationX = frame.translationX
                    }
                    if (!frame.translationY.isNaN()) {
                        translationY = frame.translationY
                    }
                    if (!frame.translationZ.isNaN()) {
                        shadowElevation = frame.translationZ
                    }
                    if (!frame.scaleX.isNaN() || !frame.scaleY.isNaN()) {
                        scaleX = if (frame.scaleX.isNaN()) 1f else frame.scaleX
                        scaleY = if (frame.scaleY.isNaN()) 1f else frame.scaleY
                    }
                    if (!frame.alpha.isNaN()) {
                        alpha = frame.alpha
                    }
                }
                val x = frameCache[measurable]!!.left
                val y = frameCache[measurable]!!.top
                val zIndex = if (frame.translationZ.isNaN()) 0f else frame.translationZ
                placeables[measurable]?.placeWithLayer(x, y, layerBlock = layerBlock, zIndex = zIndex)
            }
        }
    }

    override fun didMeasures() {}
}

private typealias SolverDimension = androidx.constraintlayout.core.state.Dimension
internal typealias SolverState = androidx.constraintlayout.core.state.State
private typealias SolverDirection = androidx.constraintlayout.core.state.State.Direction
private typealias SolverChain = androidx.constraintlayout.core.state.State.Chain

private val DEBUG = false
private fun ConstraintWidget.toDebugString() =
    "$debugName " +
        "width $width minWidth $minWidth maxWidth $maxWidth " +
        "height $height minHeight $minHeight maxHeight $maxHeight " +
        "HDB $horizontalDimensionBehaviour VDB $verticalDimensionBehaviour " +
        "MCW $mMatchConstraintDefaultWidth MCH $mMatchConstraintDefaultHeight " +
        "percentW $mMatchConstraintPercentWidth percentH $mMatchConstraintPercentHeight"

private fun BasicMeasure.Measure.toDebugString() =
    "measure strategy is "
