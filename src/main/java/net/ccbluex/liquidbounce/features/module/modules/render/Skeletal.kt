/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateModelEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.model.ModelPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector3f

@ModuleInfo(name = "Skeletal", category = ModuleCategory.RENDER)
class Skeletal : Module() {
    private val redValue = IntegerValue("Red", 255, 0, 255).displayable { !rainbowValue.get() }
    private val greenValue = IntegerValue("Green", 255, 0, 255).displayable { !rainbowValue.get() }
    private val blueValue = IntegerValue("Blue", 255, 0, 255).displayable { !rainbowValue.get() }
    private val rainbowValue = BoolValue("Rainbow", false)
    private val lineWidthValue = FloatValue("LineWidth", 2f, 1f, 5f)
    private val onlyEnemyValue = BoolValue("OnlyEnemy", false)

    private val playerModelMap = mutableMapOf<EntityPlayer, ClonedModelPlayer>()

    override fun onDisable() {
        playerModelMap.clear()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        playerModelMap.clear()
    }

    @EventTarget
    fun onModelUpdate(event: UpdateModelEvent) {
        playerModelMap[event.player] = ClonedModelPlayer(event.model)
    }

    @EventTarget
    fun onRender(event: Render3DEvent) {
        val map = playerModelMap
            .filter { (!onlyEnemyValue.get() || EntityUtils.isSelected(it.key, true)) && (mc.gameSettings.thirdPersonView != 0 || mc.thePlayer.entityId != it.key.entityId) }

        if (map.isEmpty()) {
            return
        }

        fun render(vec3f: Vector3f) {
            GL11.glRotatef(vec3f.x * 57.29578f, 1.0f, 0.0f, 0.0f)
            GL11.glRotatef(vec3f.y * 57.29578f, 0.0f, 1.0f, 0.0f)
            GL11.glRotatef(vec3f.z * 57.29578f, 0.0f, 0.0f, 1.0f)
        }

        GL11.glDisable(2848)
        GL11.glDisable(2929)
        GL11.glDisable(3553)
        GL11.glEnable(2903)
        GL11.glDepthMask(true)
        if (rainbowValue.get()) {
            RenderUtils.glColor(ColorUtils.rainbow())
        } else {
            GL11.glColor4f(redValue.get() / 255.0f, greenValue.get() / 255.0f, blueValue.get() / 255.0f, 1.0f)
        }
        map.forEach { (player, model) ->
            if (!mc.theWorld.playerEntities.contains(player)) {
                playerModelMap.remove(player)
            }
            GL11.glPushMatrix()
            GL11.glLineWidth(lineWidthValue.get())
            GL11.glTranslated(interpolate(player.posX, player.lastTickPosX, event.partialTicks.toDouble()) - mc.renderManager.renderPosX,
                interpolate(player.posY, player.lastTickPosY, event.partialTicks.toDouble()) - mc.renderManager.renderPosY,
                interpolate(player.posZ, player.lastTickPosZ, event.partialTicks.toDouble()) - mc.renderManager.renderPosZ)
            val bodyYawOffset = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * mc.timer.renderPartialTicks
            GL11.glRotatef(-bodyYawOffset, 0.0f, 1.0f, 0.0f)
            GL11.glTranslated(0.0, 0.0, if (player.isSneaking) -0.235 else 0.0)
            val legHeight = if (player.isSneaking) 0.6f else 0.75f
            GL11.glPushMatrix()
            GL11.glTranslated(-0.125, legHeight.toDouble(), 0.0)
            render(model.rightLeg)
            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, (-legHeight).toDouble(), 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPushMatrix()
            GL11.glTranslated(0.125, legHeight.toDouble(), 0.0)
            render(model.leftLeg)
            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, (-legHeight).toDouble(), 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glTranslated(0.0, 0.0, if (player.isSneaking) 0.25 else 0.0)
            GL11.glPushMatrix()
            GL11.glTranslated(0.0, if (player.isSneaking) -0.05 else 0.0, if (player.isSneaking) -0.01725 else 0.0)
            GL11.glPushMatrix()
            GL11.glTranslated(-0.375, legHeight + 0.55, 0.0)
            render(model.rightArm)
            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, -0.5, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPushMatrix()
            GL11.glTranslated(0.375, legHeight + 0.55, 0.0)
            render(model.leftArm)
            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, -0.5, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glRotatef(bodyYawOffset - player.rotationYawHead, 0.0f, 1.0f, 0.0f)
            GL11.glPushMatrix()
            GL11.glTranslated(0.0, legHeight + 0.55, 0.0)
            GL11.glRotatef(model.head.x * 57.29578f, 1.0f, 0.0f, 0.0f)
            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, 0.3, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPopMatrix()
            GL11.glRotatef(if (player.isSneaking) 25.0f else 0.0f, 1.0f, 0.0f, 0.0f)
            GL11.glTranslated(0.0, if (player.isSneaking) -0.16175 else 0.0, if (player.isSneaking) -0.48025 else 0.0)
            GL11.glPushMatrix()
            GL11.glTranslated(0.0, legHeight.toDouble(), 0.0)
            GL11.glBegin(3)
            GL11.glVertex3d(-0.125, 0.0, 0.0)
            GL11.glVertex3d(0.125, 0.0, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPushMatrix()
            GL11.glTranslated(0.0, legHeight.toDouble(), 0.0)
            GL11.glBegin(3)
            GL11.glVertex3d(0.0, 0.0, 0.0)
            GL11.glVertex3d(0.0, 0.55, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPushMatrix()
            GL11.glTranslated(0.0, legHeight + 0.55, 0.0)
            GL11.glBegin(3)
            GL11.glVertex3d(-0.375, 0.0, 0.0)
            GL11.glVertex3d(0.375, 0.0, 0.0)
            GL11.glEnd()
            GL11.glPopMatrix()
            GL11.glPopMatrix()
        }
        GL11.glEnable(2848)
        GL11.glEnable(2929)
        GL11.glEnable(3553)
        GL11.glDisable(2903)
        GlStateManager.resetColor()
        GL11.glDepthMask(false)
    }

    private fun interpolate(current: Double, old: Double, scale: Double): Double {
        return old + (current - old) * scale
    }

    class ClonedModelPlayer(model: ModelPlayer) {
        val head = Vector3f(model.bipedHead.rotateAngleX, model.bipedHead.rotateAngleY, model.bipedHead.rotateAngleZ)
        val rightArm = Vector3f(model.bipedRightArm.rotateAngleX, model.bipedRightArm.rotateAngleY, model.bipedRightArm.rotateAngleZ)
        val leftArm = Vector3f(model.bipedLeftArm.rotateAngleX, model.bipedLeftArm.rotateAngleY, model.bipedLeftArm.rotateAngleZ)
        val rightLeg = Vector3f(model.bipedRightLeg.rotateAngleX, model.bipedRightLeg.rotateAngleY, model.bipedRightLeg.rotateAngleZ)
        val leftLeg = Vector3f(model.bipedLeftLeg.rotateAngleX, model.bipedLeftLeg.rotateAngleY, model.bipedLeftLeg.rotateAngleZ)
    }
}