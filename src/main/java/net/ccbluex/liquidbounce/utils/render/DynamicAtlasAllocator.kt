/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

data class AtlasRegion(
    val page: Int,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)

data class AtlasUsage(val pageCount: Int, val allocationCount: Int, val usedArea: Long)

/** Bounded multipage best-fit allocator for incremental render caches. */
class DynamicAtlasAllocator(
    val pageWidth: Int,
    val pageHeight: Int,
    val maxPages: Int,
    val padding: Int = 1,
) {
    private data class Rectangle(val x: Int, val y: Int, val width: Int, val height: Int) {
        val area: Int get() = width * height
    }

    private class Page(width: Int, height: Int) {
        val free = arrayListOf(Rectangle(0, 0, width, height))
    }

    private data class Allocation(val page: Int, val packed: Rectangle)

    private val pages = ArrayList<Page>()
    private val allocations = HashMap<AtlasRegion, Allocation>()

    val pageCount: Int
        @Synchronized get() = pages.size

    init {
        require(pageWidth > 0 && pageHeight > 0) { "Atlas dimensions must be positive" }
        require(maxPages > 0) { "Atlas must allow at least one page" }
        require(padding >= 0) { "Atlas padding cannot be negative" }
    }

    @Synchronized
    fun allocate(width: Int, height: Int): AtlasRegion? {
        require(width > 0 && height > 0) { "Region dimensions must be positive" }
        val packedWidth = width + padding * 2
        val packedHeight = height + padding * 2
        if (packedWidth > pageWidth || packedHeight > pageHeight) return null

        pages.indices.forEach { pageIndex ->
            allocateOnPage(pageIndex, width, height, packedWidth, packedHeight)?.let { return it }
        }

        if (pages.size >= maxPages) return null
        pages += Page(pageWidth, pageHeight)
        return allocateOnPage(pages.lastIndex, width, height, packedWidth, packedHeight)
    }

    @Synchronized
    fun free(region: AtlasRegion) {
        val allocation = allocations.remove(region)
            ?: throw IllegalArgumentException("Atlas region was not allocated or was already freed")
        val page = pages[allocation.page]
        page.free += allocation.packed
        mergeFreeRectangles(page.free)
    }

    @Synchronized
    fun clear() {
        allocations.clear()
        pages.clear()
    }

    @Synchronized
    fun usage(): AtlasUsage = AtlasUsage(
        pageCount = pages.size,
        allocationCount = allocations.size,
        usedArea = allocations.values.sumOf { it.packed.area.toLong() },
    )

    private fun allocateOnPage(
        pageIndex: Int,
        contentWidth: Int,
        contentHeight: Int,
        packedWidth: Int,
        packedHeight: Int,
    ): AtlasRegion? {
        val page = pages[pageIndex]
        val candidateIndex = page.free.indices
            .filter { page.free[it].width >= packedWidth && page.free[it].height >= packedHeight }
            .minWithOrNull(compareBy<Int> { page.free[it].area - packedWidth * packedHeight }
                .thenBy { minOf(page.free[it].width - packedWidth, page.free[it].height - packedHeight) }
                .thenBy { page.free[it].y }
                .thenBy { page.free[it].x }) ?: return null

        val free = page.free.removeAt(candidateIndex)
        val packed = Rectangle(free.x, free.y, packedWidth, packedHeight)

        val rightWidth = free.width - packedWidth
        if (rightWidth > 0) {
            page.free += Rectangle(free.x + packedWidth, free.y, rightWidth, packedHeight)
        }
        val bottomHeight = free.height - packedHeight
        if (bottomHeight > 0) {
            page.free += Rectangle(free.x, free.y + packedHeight, free.width, bottomHeight)
        }

        val region = AtlasRegion(
            pageIndex,
            packed.x + padding,
            packed.y + padding,
            contentWidth,
            contentHeight,
        )
        allocations[region] = Allocation(pageIndex, packed)
        return region
    }

    private fun mergeFreeRectangles(rectangles: MutableList<Rectangle>) {
        var changed: Boolean
        do {
            changed = false
            outer@ for (firstIndex in 0 until rectangles.size) {
                for (secondIndex in firstIndex + 1 until rectangles.size) {
                    val merged = merge(rectangles[firstIndex], rectangles[secondIndex]) ?: continue
                    rectangles[firstIndex] = merged
                    rectangles.removeAt(secondIndex)
                    changed = true
                    break@outer
                }
            }
        } while (changed)
    }

    private fun merge(first: Rectangle, second: Rectangle): Rectangle? = when {
        first.y == second.y && first.height == second.height && first.x + first.width == second.x ->
            Rectangle(first.x, first.y, first.width + second.width, first.height)
        first.y == second.y && first.height == second.height && second.x + second.width == first.x ->
            Rectangle(second.x, second.y, first.width + second.width, first.height)
        first.x == second.x && first.width == second.width && first.y + first.height == second.y ->
            Rectangle(first.x, first.y, first.width, first.height + second.height)
        first.x == second.x && first.width == second.width && second.y + second.height == first.y ->
            Rectangle(second.x, second.y, first.width, first.height + second.height)
        else -> null
    }
}
