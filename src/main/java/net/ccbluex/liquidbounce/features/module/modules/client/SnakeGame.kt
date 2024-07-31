/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts.font35
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawGradientRect
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.*
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object SnakeGame : Module("SnakeGame", Category.CLIENT, gameDetecting = false, hideModule = false) {
    private var snake = mutableListOf(Position(0, 0))
    private var lastKey = 208
    private var food = Position(0, 0)
    private var score = 0

    override fun onDisable() {
        setupGame()
    }

    override fun onEnable() {
        setupGame()
    }

    @EventTarget
    fun onKey(event: KeyEvent) {
        val key = event.key
        if ((key == 205 && lastKey != 203) || (key == 203 && lastKey != 205)
            || (key == 200 && lastKey != 208) || (key == 208 && lastKey != 200)
        ) {
            lastKey = key
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.ticksExisted % 2 == 0) {
            if (snake[0].x == food.x && snake[0].y == food.y) {
                score += 1
                moveFood()
                snake.add(Position(snake[0].x, snake[0].y))
            }

            for (i in snake.size - 1 downTo 1) {
                snake[i].x = snake[i - 1].x
                snake[i].y = snake[i - 1].y
            }

            when (lastKey) {
                205 -> snake[0].x += 1
                203 -> snake[0].x -= 1
                200 -> snake[0].y -= 1
                208 -> snake[0].y += 1
            }

            for (i in 1 until snake.size) {
                if (snake[i].x == snake[0].x && snake[i].y == snake[0].y) {
                    setupGame()
                }
            }
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val resolution = ScaledResolution(mc)

        val width = resolution.scaledWidth
        val height = resolution.scaledHeight

        val startX = (width / 2 - fieldWidth / 2).toDouble()
        val startY = (height / 2 - fieldHeight / 2).toDouble()

        for (i in 0 until 18) {
            drawGradientRect(
                startX.toInt() - i + 4,
                startY.toInt() - i - 3,
                startX.toInt() + fieldWidth + i - 4,
                startY.toInt() + fieldHeight + i - 4,
                Color(0, 0, 0, 120).rgb,
                Color(0, 0, 0, 120).rgb
            )
            drawBorder(
                startX - i + 15,
                startY - i + 15,
                startX + fieldWidth + i - 15,
                startY + fieldHeight + i - 15,
                Color(6, 70, 255, 120).rgb
            )
        }

        drawRect(startX, startY, startX + fieldWidth, startY + fieldHeight, Color(30, 0, 0, 0).rgb)

        val foodX = food.x * blockSize + startX
        val foodY = food.y * blockSize + startY

        drawRect(foodX, foodY, foodX + blockSize, foodY + blockSize, Color(255, 15, 15).rgb)

        for (index in snake.indices) {
            val snakeStartX = snake[index].x * blockSize + startX
            val snakeStartY = snake[index].y * blockSize + startY

            drawRect(
                snakeStartX,
                snakeStartY,
                snakeStartX + blockSize,
                snakeStartY + blockSize,
                Color(255, 253, 255).rgb
            )
        }

        if (snake[0].x * blockSize + startX >= startX + fieldWidth || snake[0].x * blockSize + startX < startX || snake[0].y * blockSize + startY < startY || snake[0].y * blockSize + startY >= startY + fieldHeight) {
            setupGame()
        }

        font35.drawStringWithShadow(
            "Score: §a$score",
            startX.toFloat(),
            (startY - 14.0).toFloat(),
            Color(220, 220, 220).rgb
        )
    }

    private fun setupGame() {
        snake = mutableListOf(Position(0, 0))
        moveFood()
        lastKey = 208
        score = 0
    }

    private fun moveFood() {
        val foodX = nextInt(0, fieldWidth / blockSize)
        val foodY = nextInt(0, fieldHeight / blockSize)
        food = Position(foodX, foodY)
    }

    private fun drawRect(paramXStart: Double, paramYStart: Double, paramXEnd: Double, paramYEnd: Double, color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255.0F
        val red = (color shr 16 and 0xFF) / 255.0F
        val green = (color shr 8 and 0xFF) / 255.0F
        val blue = (color and 0xFF) / 255.0F

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)

        glPushMatrix()
        glColor4f(red, green, blue, alpha)
        glBegin(GL_TRIANGLE_FAN)
        glVertex2d(paramXEnd, paramYStart)
        glVertex2d(paramXStart, paramYStart)
        glVertex2d(paramXStart, paramYEnd)
        glVertex2d(paramXEnd, paramYEnd)

        glEnd()
        glPopMatrix()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)

        glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
    }

    private fun drawBorder(paramXStart: Double, paramYStart: Double, paramXEnd: Double, paramYEnd: Double, color: Int) {
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        enableBlend()
        disableTexture2D()
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)

        pushMatrix()
        glColor4f(red, green, blue, alpha)
        glLineWidth(1f)
        glBegin(GL_LINE_LOOP)
        glVertex2d(paramXEnd, paramYStart)
        glVertex2d(paramXStart, paramYStart)
        glVertex2d(paramXStart, paramYEnd)
        glVertex2d(paramXEnd, paramYEnd)

        glEnd()
        popMatrix()

        enableTexture2D()
        disableBlend()
        glDisable(GL_LINE_SMOOTH)

        color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    data class Position(var x: Int, var y: Int)

    private const val blockSize = 10
    private const val fieldWidth = 200
    private const val fieldHeight = 150
}