/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.config.choices
import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts.font35
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawGradientRect
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.*
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object SnakeGame : Module("SnakeGame", Category.CLIENT, gameDetecting = false, hideModule = false) {

    private var snake = mutableListOf(Pos(0, 0))
    private var lastKey = 208
    private var food = Pos(0, 0)
    private var score = 0
    private var highScore = 0
    private val Mode by choices("Mode", arrayOf("Easy", "Normal", "Hard"), "Easy")
    private var obstacles = mutableListOf(Pos(0, 0))

    private const val BLOCK_SIZE = 10
    private const val FIELD_WIDTH = 200
    private const val FIELD_HEIGHT = 150

    override fun onDisable() {
        checkHighScore()
        setupGame()
    }

    override fun onEnable() {
        setupGame()
    }

    private val speed: Int
        get() = when (Mode) {
            "Easy" -> 3
            "Normal" -> 2
            "Hard" -> 2
            else -> 3
        }

    val onKey = handler<KeyEvent> { e ->
        val k = e.key
        if (k == 1) {
            toggle()
        }
        if ((k == 205 && lastKey != 203) || (k == 203 && lastKey != 205)
            || (k == 200 && lastKey != 208) || (k == 208 && lastKey != 200)
        ) {
            lastKey = k
        }
    }

    val onUpdate = handler<UpdateEvent> {
        if (mc.thePlayer.ticksExisted % speed == 0) {
            if (snake[0].x == food.x && snake[0].y == food.y) {
                score++
                when (Mode) {
                    "Easy" -> {
                        if (score % 3 == 0) generateOneObstacle()
                        if (score % 10 == 0 && obstacles.isNotEmpty()) obstacles.removeAt(obstacles.lastIndex)
                    }
                    "Normal" -> {
                        if (score % 2 == 0) generateOneObstacle()
                        if (score % 5 == 0 && obstacles.isNotEmpty()) obstacles.removeAt(obstacles.lastIndex)
                    }
                    "Hard" -> {
                        if (score % 5 == 0 && obstacles.isNotEmpty()) obstacles.removeAt(obstacles.lastIndex)
                    }
                }
                moveFood()
                snake.add(Pos(snake[0].x, snake[0].y))
            }
            for (i in snake.size - 1 downTo 1) {
                snake[i].x = snake[i - 1].x
                snake[i].y = snake[i - 1].y
            }
            when (lastKey) {
                205 -> snake[0].x++
                203 -> snake[0].x--
                200 -> snake[0].y--
                208 -> snake[0].y++
            }
            if (Mode == "Hard") {
                for (obs in obstacles) {
                    if (snake[0].x == obs.x && snake[0].y == obs.y) {
                        checkHighScore()
                        setupGame()
                        return@handler
                    }
                }
            }
            for (i in 1 until snake.size) {
                if (snake[i].x == snake[0].x && snake[i].y == snake[0].y) {
                    checkHighScore()
                    setupGame()
                    return@handler
                }
            }
        }
    }

    val onRender2D = handler<Render2DEvent> {
        val sr = ScaledResolution(mc)
        val w = sr.scaledWidth
        val h = sr.scaledHeight
        val sx = (w / 2 - FIELD_WIDTH / 2).toDouble()
        val sy = (h / 2 - FIELD_HEIGHT / 2).toDouble()
        for (i in 0 until 18) {
            drawGradientRect(
                sx.toInt() - i + 4,
                sy.toInt() - i - 3,
                sx.toInt() + FIELD_WIDTH + i - 4,
                sy.toInt() + FIELD_HEIGHT + i - 4,
                Color(0, 0, 0, 120).rgb,
                Color(0, 0, 0, 120).rgb,
                0f
            )
            drawBorder(
                sx - i + 15,
                sy - i + 15,
                sx + FIELD_WIDTH + i - 15,
                sy + FIELD_HEIGHT + i - 15,
                Color(6, 70, 255, 120).rgb
            )
        }
        drawRect(sx, sy, sx + FIELD_WIDTH, sy + FIELD_HEIGHT, Color(30, 0, 0, 0).rgb)
        val fx = food.x * BLOCK_SIZE + sx
        val fy = food.y * BLOCK_SIZE + sy
        val cFood = ColorUtils.fade(Color(255, 15, 15), 1, 3)
        drawRect(fx, fy, fx + BLOCK_SIZE, fy + BLOCK_SIZE, cFood.rgb)
        if (Mode == "Hard" || Mode == "Normal" || Mode == "Easy") {
            for (obs in obstacles) {
                val ox = obs.x * BLOCK_SIZE + sx
                val oy = obs.y * BLOCK_SIZE + sy
                val cObs = ColorUtils.fade(Color(255, 255, 0), 1, 3)
                drawRect(ox, oy, ox + BLOCK_SIZE, oy + BLOCK_SIZE, cObs.rgb)
            }
        }
        for (i in snake.indices) {
            val xx = snake[i].x * BLOCK_SIZE + sx
            val yy = snake[i].y * BLOCK_SIZE + sy
            val cc = ColorUtils.fade(Color(255, 253, 255), i, snake.size)
            drawRect(xx, yy, xx + BLOCK_SIZE, yy + BLOCK_SIZE, cc.rgb)
        }
        if (snake[0].x * BLOCK_SIZE + sx >= sx + FIELD_WIDTH
            || snake[0].x * BLOCK_SIZE + sx < sx
            || snake[0].y * BLOCK_SIZE + sy < sy
            || snake[0].y * BLOCK_SIZE + sy >= sy + FIELD_HEIGHT
        ) {
            checkHighScore()
            setupGame()
        }
        font35.drawStringWithShadow("Score: §a$score", sx.toFloat(), (sy - 14.0).toFloat(), Color(220, 220, 220).rgb)
        val hsTxt = "High Score: §a$highScore"
        val hsW = font35.getStringWidth(hsTxt)
        val hsH = font35.FONT_HEIGHT
        val hsX1 = sx.toInt()
        val hsY1 = (sy - 28.0).toInt()
        val hsX2 = hsX1 + hsW + 6
        val hsY2 = hsY1 + hsH + 4
        drawGradientRect(hsX1, hsY1, hsX2, hsY2, Color(0, 0, 0, 120).rgb, Color(0, 0, 0, 120).rgb, 0f)
        drawBorder(hsX1.toDouble(), hsY1.toDouble(), hsX2.toDouble(), hsY2.toDouble(), Color(6, 70, 255, 120).rgb)
        font35.drawStringWithShadow(hsTxt, (hsX1 + 3).toFloat(), (hsY1 + 2).toFloat(), Color(220, 220, 220).rgb)
        font35.drawStringWithShadow("Mode: $Mode", (sx + FIELD_WIDTH - 50).toFloat(), (sy - 14.0).toFloat(), Color(220, 220, 220).rgb)
    }

    private fun setupGame() {
        snake = mutableListOf(Pos(0, 0))
        moveFood()
        lastKey = 208
        score = 0
        when (Mode) {
            "Hard" -> {
                generateObstacles(7)
            }
            "Normal" -> {
                obstacles.clear()
            }
            else -> {
                obstacles.clear()
            }
        }
    }

    private fun moveFood() {
        var px: Int
        var py: Int
        do {
            px = nextInt(0, FIELD_WIDTH / BLOCK_SIZE)
            py = nextInt(0, FIELD_HEIGHT / BLOCK_SIZE)
        } while (
            snake.any { it.x == px && it.y == py } ||
            obstacles.any { it.x == px && it.y == py }
        )
        food = Pos(px, py)
    }

    private fun generateObstacles(count: Int) {
        obstacles.clear()
        repeat(count) {
            generateOneObstacle()
        }
    }

    private fun generateOneObstacle() {
        var ox: Int
        var oy: Int
        do {
            ox = nextInt(0, FIELD_WIDTH / BLOCK_SIZE)
            oy = nextInt(0, FIELD_HEIGHT / BLOCK_SIZE)
        } while (
            (ox == snake[0].x && oy == snake[0].y) ||
            obstacles.any { it.x == ox && it.y == oy } ||
            snake.any { it.x == ox && it.y == oy }
        )
        obstacles.add(Pos(ox, oy))
    }

    private fun checkHighScore() {
        if (score > highScore) {
            highScore = score
        }
    }

    private fun drawRect(xs: Double, ys: Double, xe: Double, ye: Double, c: Int) {
        val a = (c shr 24 and 0xFF) / 255.0F
        val r = (c shr 16 and 0xFF) / 255.0F
        val g = (c shr 8 and 0xFF) / 255.0F
        val b = (c and 0xFF) / 255.0F
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glPushMatrix()
        glColor4f(r, g, b, a)
        glBegin(GL_TRIANGLE_FAN)
        glVertex2d(xe, ys)
        glVertex2d(xs, ys)
        glVertex2d(xs, ye)
        glVertex2d(xe, ye)
        glEnd()
        glPopMatrix()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun drawBorder(xs: Double, ys: Double, xe: Double, ye: Double, c: Int) {
        val a = (c shr 24 and 0xFF) / 255.0f
        val r = (c shr 16 and 0xFF) / 255.0f
        val g = (c shr 8 and 0xFF) / 255.0f
        val b = (c and 0xFF) / 255.0f
        enableBlend()
        disableTexture2D()
        blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        pushMatrix()
        glColor4f(r, g, b, a)
        glLineWidth(1f)
        glBegin(GL_LINE_LOOP)
        glVertex2d(xe, ys)
        glVertex2d(xs, ys)
        glVertex2d(xs, ye)
        glVertex2d(xe, ye)
        glEnd()
        popMatrix()
        enableTexture2D()
        disableBlend()
        glDisable(GL_LINE_SMOOTH)
        color(1f, 1f, 1f, 1f)
    }

    data class Pos(var x: Int, var y: Int)
}
