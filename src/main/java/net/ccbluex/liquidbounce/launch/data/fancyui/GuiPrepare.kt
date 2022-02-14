package net.ccbluex.liquidbounce.launch.data.fancyui

import me.friwi.jcefmaven.EnumProgress
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler
import net.ccbluex.liquidbounce.utils.extensions.drawCenteredString
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.opengl.GL11
import java.awt.Color

class GuiPrepare(private val progressHandler: DynamicProgressHandler, private val doneCallback: () -> Unit) : GuiScreen() {

    private var timeOffset = System.currentTimeMillis()
    private var lastIsNoEstimation = false

    override fun drawScreen(p_drawScreen_1_: Int, p_drawScreen_2_: Int, p_drawScreen_3_: Float) {
        drawDefaultBackground()

        // alert text
        fontRendererObj.drawCenteredString("Please wait while we are preparing the renderer...", width / 2f, height / 2f - 50 - fontRendererObj.FONT_HEIGHT, 0xffffff)
        fontRendererObj.drawCenteredString("Current stage: ${progressHandler.currentProgressText}", width / 2f, height / 2f - 50, 0xffffff)

        val progress = progressHandler.currentProgressPercent
        if(progress == EnumProgress.NO_ESTIMATION && !lastIsNoEstimation) {
            timeOffset = System.currentTimeMillis()
            lastIsNoEstimation = false
        }
        lastIsNoEstimation = progress == EnumProgress.NO_ESTIMATION

        // progress bar
        val barStart = if(progress == EnumProgress.NO_ESTIMATION) {
            ((System.currentTimeMillis() - timeOffset) % 1500).toInt() / 1300f
        } else { 0f }
        val barEnd = if (progress == EnumProgress.NO_ESTIMATION) {
            barStart - (200f / 1300f)
        } else { progress / 100f }

        val barWidth = width * 0.7f
        val barHeight = height * 0.05f
        GL11.glPushMatrix()
        GL11.glTranslatef(width * 0.15f, height * 0.475f, 0f)
        RenderUtils.drawRect(0f, 0f, barWidth, barHeight, Color(100, 100, 100, 100))
        RenderUtils.drawRect((barStart).coerceAtLeast(0f).coerceAtMost(1f) * barWidth, 0f,
            (barEnd).coerceAtLeast(0f).coerceAtMost(1f) * barWidth, barHeight, Color.WHITE)
        GL11.glPopMatrix()

        if(progressHandler.currentProgress == EnumProgress.INITIALIZED) {
            doneCallback()
        }
    }

    /**
     * vanilla minecraft will display the main menu when keyTyped is called and keyCode is equal to escape, but we don't want that
     */
    override fun keyTyped(p_keyTyped_1_: Char, p_keyTyped_2_: Int) { }

    class DynamicProgressHandler : ConsoleProgressHandler() {

        var currentProgress = EnumProgress.LOCATING // first progress
            private set
        var currentProgressPercent = EnumProgress.NO_ESTIMATION
            private set
        var currentProgressText = "null"
            private set

        override fun handleProgress(state: EnumProgress, percent: Float) {
            currentProgress = state
            currentProgressPercent = percent
            updateProgressText()
            super.handleProgress(state, percent) // print to console
        }

        private fun updateProgressText() {
            currentProgressText = currentProgress.name.substring(0, 1).uppercase() + currentProgress.name.substring(1).lowercase()
        }
    }
}