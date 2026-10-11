package com.example.maiplan.home.task.screens

internal fun taskStepTop(order: List<String>, id: String, heights: Map<String, Int>, spacing: Float): Float =
    order.takeWhile { it != id }.sumOf { heights[it] ?: 0 }.toFloat() +
        order.indexOf(id).coerceAtLeast(0) * spacing

/** Move when the leading edge crosses a neighbour's centre, allowing rows of any height to reach either end. */
internal fun taskStepDragOrder(order: List<String>, id: String, top: Float,
    heights: Map<String, Int>, spacing: Float): List<String> {
    if (id !in order || order.any { it !in heights }) return order
    val result = order.toMutableList()
    val bottom = top + heights.getValue(id)
    var index = result.indexOf(id)
    val originalTop = taskStepTop(result, id, heights, spacing)
    if (top > originalTop) {
        while (index < result.lastIndex) {
            val next = result[index + 1]
            val nextCentre = taskStepTop(result, next, heights, spacing) + heights.getValue(next) / 2f
            if (bottom <= nextCentre) break
            result.add(index + 1, result.removeAt(index))
            index++
        }
    } else {
        while (index > 0) {
            val previous = result[index - 1]
            val previousCentre = taskStepTop(result, previous, heights, spacing) + heights.getValue(previous) / 2f
            if (top >= previousCentre) break
            result.add(index - 1, result.removeAt(index))
            index--
        }
    }
    return result
}
