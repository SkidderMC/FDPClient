/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.event.KeyEvent
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts.fontSemibold35
import net.ccbluex.liquidbounce.utils.kotlin.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawBorder
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawGradientRect
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawRect
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import javax.vecmath.Point2i

object SnakeGame : Module("SnakeGame", Category.CLIENT, gameDetecting = false) {

    private val mode by choices("Mode", arrayOf("Easy", "Normal", "Hard"), "Easy")

    private var obstacles = mutableListOf(Point2i(0, 0))
    private var snake = mutableListOf(Point2i(0, 0))
    private var lastKey = 208
    private var food = Point2i(0, 0)
    private var score = 0
    private var highScore = 0

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

    private val speed
        get() = when (mode) {
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
        if ((k == 205 && lastKey != 203) ||
            (k == 203 && lastKey != 205) ||
            (k == 200 && lastKey != 208) ||
            (k == 208 && lastKey != 200)
        ) {
            lastKey = k
        }
    }

    val onUpdate = handler<UpdateEvent> {
        val player = mc.thePlayer ?: return@handler

        if (player.ticksExisted % speed == 0) {
            if (snake[0].x == food.x && snake[0].y == food.y) {
                score++
                when (mode) {
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
                snake.add(Point2i(snake[0].x, snake[0].y))
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

            if (mode == "Hard") {
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
        val sx = (w / 2 - FIELD_WIDTH / 2).toFloat()
        val sy = (h / 2 - FIELD_HEIGHT / 2).toFloat()

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
                1f,
                Color(6, 70, 255, 120).rgb
            )
        }

        drawRect(sx, sy, sx + FIELD_WIDTH, sy + FIELD_HEIGHT, Color(30, 0, 0, 0).rgb)

        val fx = food.x * BLOCK_SIZE + sx
        val fy = food.y * BLOCK_SIZE + sy
        val cFood = ColorUtils.fade(Color(255, 15, 15), 1, 3)
        drawRect(fx, fy, fx + BLOCK_SIZE, fy + BLOCK_SIZE, cFood.rgb)

        if (mode in listOf("Hard", "Normal", "Easy")) {
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

        if (snake[0].x * BLOCK_SIZE + sx >= sx + FIELD_WIDTH ||
            snake[0].x * BLOCK_SIZE + sx < sx ||
            snake[0].y * BLOCK_SIZE + sy < sy ||
            snake[0].y * BLOCK_SIZE + sy >= sy + FIELD_HEIGHT
        ) {
            checkHighScore()
            setupGame()
        }

        fontSemibold35.drawStringWithShadow("Score: §a$score", sx.toFloat(), (sy - 14.0).toFloat(), Color(220, 220, 220).rgb)

        val hsTxt = "High Score: §a$highScore"
        val hsW = fontSemibold35.getStringWidth(hsTxt)
        val hsH = fontSemibold35.FONT_HEIGHT
        val hsX1 = sx.toInt()
        val hsY1 = (sy - 28.0).toInt()
        val hsX2 = hsX1 + hsW + 6
        val hsY2 = hsY1 + hsH + 4
        drawGradientRect(hsX1, hsY1, hsX2, hsY2, Color(0, 0, 0, 120).rgb, Color(0, 0, 0, 120).rgb, 0f)
        drawBorder(hsX1, hsY1, hsX2, hsY2, 1f, Color(6, 70, 255, 120).rgb)
        fontSemibold35.drawStringWithShadow(hsTxt, (hsX1 + 3).toFloat(), (hsY1 + 2).toFloat(), Color(220, 220, 220).rgb)

        fontSemibold35.drawStringWithShadow(
            "mode: $mode",
            (sx + FIELD_WIDTH - 50).toFloat(),
            (sy - 14.0).toFloat(),
            Color(220, 220, 220).rgb
        )
    }

    private fun setupGame() {
        snake = mutableListOf(Point2i(0, 0))
        moveFood()
        lastKey = 208
        score = 0
        when (mode) {
            "Hard" -> {
                generateObstacles(7)
            }

            "Normal", "Easy" -> {
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
        food = Point2i(px, py)
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
        obstacles.add(Point2i(ox, oy))
    }

    private fun checkHighScore() {
        if (score > highScore) {
            highScore = score
        }
    }
}