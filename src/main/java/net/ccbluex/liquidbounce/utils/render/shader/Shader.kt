/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render.shader

import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.render.GlEnvironment
import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.*
import org.lwjgl.opengl.ARBShaderObjects.*
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glUseProgram
import java.io.File
import java.io.IOException
import java.nio.file.Files

abstract class Shader : MinecraftInstance {

    companion object {
        @JvmStatic
        val shadersSupported by lazy {
            runCatching {
                val caps = GLContext.getCapabilities()
                !GlEnvironment.isGlEsWrapper && caps.OpenGL20 && caps.GL_ARB_shader_objects &&
                    caps.GL_ARB_vertex_shader && caps.GL_ARB_fragment_shader
            }.getOrDefault(false)
        }
    }

    var programId = 0
        private set

    var broken = false
        private set

    private var active = false

    private val uniformsMap = mutableMapOf<String, Int>()

    val isUsable: Boolean
        get() = programId != 0 && !broken

    constructor(fragmentShader: String) {
        if (!shadersSupported) {
            broken = true
            return
        }

        val vertexShaderID: Int
        val fragmentShaderID: Int

        try {
            val vertexStream = javaClass.getResourceAsStream("/assets/minecraft/fdpclient/shader/vertex.vert")
            vertexShaderID = createShader(IOUtils.toString(vertexStream), ARBVertexShader.GL_VERTEX_SHADER_ARB)
            IOUtils.closeQuietly(vertexStream)

            val fragmentStream = javaClass.getResourceAsStream("/assets/minecraft/fdpclient/shader/fragment/$fragmentShader")
            fragmentShaderID = createShader(IOUtils.toString(fragmentStream), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
            IOUtils.closeQuietly(fragmentStream)
        } catch (e: Exception) {
            e.printStackTrace()
            broken = true
            return
        }

        if (vertexShaderID == 0 || fragmentShaderID == 0) {
            broken = true
            return
        }

        programId = glCreateProgramObjectARB()

        if (programId == 0) {
            broken = true
            return
        }

        glAttachObjectARB(programId, vertexShaderID)
        glAttachObjectARB(programId, fragmentShaderID)

        glLinkProgramARB(programId)
        glValidateProgramARB(programId)

        if (glGetObjectParameteriARB(programId, GL_OBJECT_LINK_STATUS_ARB) == GL_FALSE) {
            LOGGER.warn("[Shader] Failed to link $fragmentShader, disabling this shader")
            glDeleteObjectARB(programId)
            programId = 0
            broken = true
            return
        }

        LOGGER.info("[Shader] Successfully loaded: $fragmentShader")
    }

    @Throws(IOException::class)
    constructor(fragmentShader: File) {
        if (!shadersSupported) {
            broken = true
            return
        }

        val vertexShaderID: Int
        val fragmentShaderID: Int

        val vertexStream = javaClass.getResourceAsStream("/assets/minecraft/fdpclient/shader/vertex.vert")
        vertexShaderID = createShader(IOUtils.toString(vertexStream), ARBVertexShader.GL_VERTEX_SHADER_ARB)
        IOUtils.closeQuietly(vertexStream)

        val fragmentStream = Files.newInputStream(fragmentShader.toPath())
        fragmentShaderID = createShader(IOUtils.toString(fragmentStream), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB)
        IOUtils.closeQuietly(fragmentStream)

        if (vertexShaderID == 0 || fragmentShaderID == 0) {
            broken = true
            return
        }

        programId = glCreateProgramObjectARB()

        if (programId == 0) {
            broken = true
            return
        }

        glAttachObjectARB(programId, vertexShaderID)
        glAttachObjectARB(programId, fragmentShaderID)

        glLinkProgramARB(programId)
        glValidateProgramARB(programId)

        if (glGetObjectParameteriARB(programId, GL_OBJECT_LINK_STATUS_ARB) == GL_FALSE) {
            LOGGER.warn("[Shader] Failed to link ${fragmentShader.name}, disabling this shader")
            glDeleteObjectARB(programId)
            programId = 0
            broken = true
            return
        }

        LOGGER.info("[Shader] Successfully loaded: " + fragmentShader.name)
    }

    open fun startShader() {
        if (!isUsable)
            return

        glPushMatrix()
        glUseProgram(programId)

        // Only setup uniforms once (cached in uniformsMap)
        if (uniformsMap.isEmpty()) {
            setupUniforms()
        }

        updateUniforms()
        active = true
    }

    open fun stopShader() {
        if (!active)
            return

        active = false
        glUseProgram(0)
        glPopMatrix()
    }

    abstract fun setupUniforms()
    abstract fun updateUniforms()
    private fun createShader(shaderSource: String, shaderType: Int): Int {
        var shader = 0

        return try {
            shader = glCreateShaderObjectARB(shaderType)

            if (shader == 0)
                return 0

            glShaderSourceARB(shader, shaderSource)
            glCompileShaderARB(shader)

            if (glGetObjectParameteriARB(shader, GL_OBJECT_COMPILE_STATUS_ARB) == GL_FALSE)
                throw RuntimeException("Error creating shader: " + getLogInfo(shader))

            shader
        } catch (e: Exception) {
            glDeleteObjectARB(shader)
            throw e
        }
    }

    private fun getLogInfo(i: Int) = glGetInfoLogARB(i, glGetObjectParameteriARB(i, GL_OBJECT_INFO_LOG_LENGTH_ARB))

    fun setUniform(uniformName: String, location: Int) {
        uniformsMap[uniformName] = location
    }

    fun setupUniform(uniformName: String) = setUniform(uniformName, glGetUniformLocation(programId, uniformName))

    fun getUniform(uniformName: String) = uniformsMap.getValue(uniformName)
}