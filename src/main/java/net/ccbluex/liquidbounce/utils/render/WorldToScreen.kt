/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glGetFloat
import org.lwjgl.util.vector.Matrix4f
import org.lwjgl.util.vector.Vector2f
import org.lwjgl.util.vector.Vector3f
import org.lwjgl.util.vector.Vector4f
import kotlin.math.abs

object WorldToScreen : MinecraftInstance {
    private val matrixBuffer = BufferUtils.createFloatBuffer(16)
    private val capturedView = Matrix4f()
    private val capturedProjection = Matrix4f()
    private val captured = Projection(capturedView, capturedProjection)

    /** Captures both GL matrices once and returns a reusable, allocation-free projector. */
    fun capture(screenWidth: Int = mc.displayWidth, screenHeight: Int = mc.displayHeight): Projection {
        loadMatrix(GL11.GL_MODELVIEW_MATRIX, capturedView)
        loadMatrix(GL11.GL_PROJECTION_MATRIX, capturedProjection)
        captured.screenWidth = screenWidth
        captured.screenHeight = screenHeight
        return captured
    }

    fun getMatrix(matrix: Int): Matrix4f {
        val floatBuffer = BufferUtils.createFloatBuffer(16)
        
        glGetFloat(matrix, floatBuffer)
        
        return Matrix4f().load(floatBuffer) as Matrix4f
    }

    private fun loadMatrix(type: Int, destination: Matrix4f) {
        matrixBuffer.clear()
        glGetFloat(type, matrixBuffer)
        matrixBuffer.rewind()
        destination.load(matrixBuffer)
    }

    class Projection internal constructor(
        private val view: Matrix4f,
        private val projection: Matrix4f,
    ) {
        internal var screenWidth = 0
        internal var screenHeight = 0

        /** Writes screen x/y and normalized depth to [out] without creating vector objects. */
        fun project(x: Float, y: Float, z: Float, out: FloatArray): Boolean {
            require(out.size >= 3) { "Projection output requires three entries" }

            val vx = x * view.m00 + y * view.m10 + z * view.m20 + view.m30
            val vy = x * view.m01 + y * view.m11 + z * view.m21 + view.m31
            val vz = x * view.m02 + y * view.m12 + z * view.m22 + view.m32
            val vw = x * view.m03 + y * view.m13 + z * view.m23 + view.m33

            val cx = vx * projection.m00 + vy * projection.m10 + vz * projection.m20 + vw * projection.m30
            val cy = vx * projection.m01 + vy * projection.m11 + vz * projection.m21 + vw * projection.m31
            val cz = vx * projection.m02 + vy * projection.m12 + vz * projection.m22 + vw * projection.m32
            val cw = vx * projection.m03 + vy * projection.m13 + vz * projection.m23 + vw * projection.m33
            if (!cw.isFinite() || kotlin.math.abs(cw) < 1.0e-6f) return false

            val inverseW = 1f / cw
            val ndcX = cx * inverseW
            val ndcY = cy * inverseW
            val ndcZ = cz * inverseW
            if (!ndcZ.isFinite() || abs(ndcZ) > 1f) return false

            out[0] = (ndcX + 1f) * 0.5f * screenWidth
            out[1] = (1f - ndcY) * 0.5f * screenHeight
            out[2] = ndcZ
            return out[0].isFinite() && out[1].isFinite()
        }
    }

    fun worldToScreen(
        pointInWorld: Vector3f,
        viewMatrix: Matrix4f = getMatrix(GL11.GL_MODELVIEW_MATRIX),
        projectionMatrix: Matrix4f = getMatrix(GL11.GL_PROJECTION_MATRIX),
        screenWidth: Int = mc.displayWidth,
        screenHeight: Int = mc.displayHeight
    ): Vector2f? {
        val clipSpacePos = Vector4f(pointInWorld.x, pointInWorld.y, pointInWorld.z, 1f) * viewMatrix * projectionMatrix

        val ndcSpacePos = Vector3f(clipSpacePos.x, clipSpacePos.y, clipSpacePos.z).scale(1 / clipSpacePos.w) as Vector3f

        val screenX = (ndcSpacePos.x + 1f) / 2f * screenWidth
        val screenY = (1f - ndcSpacePos.y) / 2f * screenHeight

        return if (abs(ndcSpacePos.z) > 1) null
        else Vector2f(screenX, screenY)
    }
}

private operator fun Vector4f.times(mat: Matrix4f) = Vector4f(
    this.x * mat.m00 + this.y * mat.m10 + this.z * mat.m20 + this.w * mat.m30,
    this.x * mat.m01 + this.y * mat.m11 + this.z * mat.m21 + this.w * mat.m31,
    this.x * mat.m02 + this.y * mat.m12 + this.z * mat.m22 + this.w * mat.m32,
    this.x * mat.m03 + this.y * mat.m13 + this.z * mat.m23 + this.w * mat.m33
)
