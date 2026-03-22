package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.timing.TickedActions.nextTick
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.XYChart
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

abstract class ChartRecorderModule(name: String) : Module(name, Category.OTHER, Category.SubCategory.MISCELLANEOUS) {

    protected var chart: XYChart? = null
    protected var failed = false

    protected fun initializeChart(
        failureMessage: String,
        startMessage: String,
        chartBuilder: () -> XYChart,
    ): Boolean {
        return try {
            chart = chartBuilder()
            chat(startMessage)
            true
        } catch (exception: Exception) {
            exception.printStackTrace()
            chat(failureMessage)
            nextTick {
                failed = true
                state = false
            }
            false
        }
    }

    protected fun limitReached(currentSize: Int, maxRecords: Int, entryName: String): Boolean {
        if (currentSize < maxRecords) {
            return false
        }

        chat("§cWarning: Maximum $entryName limit reached ($maxRecords). Recording stopped.")
        state = false
        return true
    }

    protected fun saveChart(prefix: String) {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        val fileName = "${prefix}_${currentDateTime.format(formatter)}.png"
        val file = FileManager.dir.resolve(fileName)

        try {
            BitmapEncoder.saveBitmap(chart, file.absolutePath, BitmapEncoder.BitmapFormat.PNG)
        } catch (exception: IOException) {
            exception.printStackTrace()
        } finally {
            chat("Saved as $fileName in ${FileManager.dir}")
        }
    }
}
