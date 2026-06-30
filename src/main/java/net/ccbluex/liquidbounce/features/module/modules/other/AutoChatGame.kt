/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import net.ccbluex.liquidbounce.config.Configurable
import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.async.launchSequence
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.io.HttpClient
import net.ccbluex.liquidbounce.utils.io.newCall
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.minecraft.network.play.server.S02PacketChat
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

object AutoChatGame : Module("AutoChatGame", Category.OTHER, Category.SubCategory.MISCELLANEOUS, gameDetecting = false) {

    private val baseUrl by text("BaseUrl", "https://api.openai.com/v1")
        .describe("Base URL of the chat completions API.")
    private val apiKey by text("ApiKey", "")
        .describe("API key used to authenticate requests.")
    private val model by text("Model", "gpt-4o-mini")
        .describe("Model name used to answer questions.")
    private val reactionTime by intRange("ReactionTime", 1000..5000, 0..10000)
        .describe("Random delay range before sending the answer.")
    private val cooldownMinutes by int("Cooldown", 2, 0..60)
        .describe("Minutes to wait between answering questions.")
    private val bufferTime by int("BufferTime", 200, 0..500)
        .describe("How long to gather chat lines into one question.")
    private val triggerSentence by text("TriggerSentence", "Chat Game")
        .describe("Phrase that starts collecting a new question.")
    private val includeTrigger by boolean("IncludeTrigger", true)
        .describe("Include the trigger line as part of the question.")
    private val serverName by text("ServerName", "Minecraft")
        .describe("Server name given to the model for context.")
    private val answerTemplate by text("AnswerTemplate", "%s")
        .describe("Format applied to the answer before sending it.")

    private val defaultPrompt =
        ("You participate in a chat game in which you have to answer questions or do tasks. " +
            "Your goal is to answer them as short and precise as possible and win the game. " +
            "The questions might be based on the game Minecraft or the server you are playing on. " +
            "The server name is {SERVER_NAME}. " +
            "On true or false questions, respond without any dots, in lower-case with 'true' or 'false'. " +
            "On math questions, respond with the result. " +
            "On first to type tasks, respond with the word. " +
            "On unscramble tasks, the word is scrambled and might be from the game Minecraft, " +
            "respond with the unscrambled word. " +
            "On other questions, respond with the answer. " +
            "DO NOT SAY ANYTHING ELSE THAN THE ANSWER! If you do, you will be disqualified.")

    private val prompt by text("Prompt", defaultPrompt)
        .describe("System prompt that instructs the model how to play.")

    private val apiGroup = Configurable("Api")
    private val triggerGroup = Configurable("Trigger")
    private val timingGroup = Configurable("Timing")
    private val behaviorGroup = Configurable("Behavior")

    init {
        moveValues(apiGroup, "BaseUrl", "ApiKey", "Model")
        moveValues(triggerGroup, "TriggerSentence", "IncludeTrigger", "BufferTime")
        moveValues(timingGroup, "ReactionTime", "Cooldown")
        moveValues(behaviorGroup, "ServerName", "AnswerTemplate", "Prompt")

        addValues(listOf(apiGroup, triggerGroup, timingGroup, behaviorGroup))
    }
    private val chatBuffer = mutableListOf<String>()
    private val triggerTimer = MSTimer().apply { zero() }
    private val cooldownTimer = MSTimer().apply { zero() }
    private var pending = false

    override fun onEnable() {
        if (apiKey.isBlank()) {
            chat("§cPlease enter your API key in the module settings.")
            state = false
            return
        }
        chatBuffer.clear()
        triggerTimer.zero()
        cooldownTimer.zero()
        pending = false
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet
        if (packet !is S02PacketChat) {
            return@handler
        }

        val message = packet.chatComponent?.unformattedText?.trim() ?: return@handler
        if (message.isEmpty()) {
            return@handler
        }

        if (message.contains("Show some love by typing", ignoreCase = true)) {
            answer("gg")
            return@handler
        }

        if (!cooldownTimer.hasTimePassed(cooldownMinutes * 60000L)) {
            chatBuffer.clear()
            return@handler
        }

        if (message.contains(triggerSentence)) {
            triggerTimer.reset()
            chatBuffer.clear()
            if (!includeTrigger) {
                return@handler
            }
        }

        if (!triggerTimer.hasTimePassed(bufferTime.toLong())) {
            chatBuffer.add(message)
            return@handler
        }

        if (chatBuffer.isNotEmpty() && !pending) {
            solve()
        }
    }

    val onMotion = handler<MotionEvent> { event ->
        if (event.eventState != EventState.PRE) {
            return@handler
        }

        if (!triggerTimer.hasTimePassed(bufferTime.toLong())) {
            return@handler
        }

        if (chatBuffer.isNotEmpty() && !pending) {
            solve()
        }
    }

    private fun solve() {
        val question = chatBuffer.joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()

        chatBuffer.clear()
        cooldownTimer.reset()

        if (question.isEmpty()) {
            return
        }

        pending = true
        chat("§aUnderstood question: $question")

        launchSequence(Dispatchers.IO) {
            val solution = runCatching { requestAnswer(question) }
                .onFailure { chat("§cFailed to answer question: ${it.message}") }
                .getOrNull()
                ?.trimEnd('.')

            if (solution.isNullOrBlank()) {
                pending = false
                return@launchSequence
            }

            chat("§aAnswer: $solution")
            delay(reactionTime.random().toLong())
            answer(answerTemplate.format(solution))
            pending = false
        }
    }

    private fun answer(text: String) {
        mc.thePlayer?.sendChatMessage(text)
    }

    private fun requestAnswer(question: String): String? {
        val systemPrompt = prompt.replace("{SERVER_NAME}", serverName)

        val messages = JsonArray().apply {
            add(JsonObject().apply {
                addProperty("role", "system")
                addProperty("content", systemPrompt)
            })
            add(JsonObject().apply {
                addProperty("role", "user")
                addProperty("content", question)
            })
        }

        val payload = JsonObject().apply {
            addProperty("model", model)
            add("messages", messages)
        }

        val body = payload.toString().toRequestBody("application/json".toMediaType())
        val url = baseUrl.trimEnd('/') + "/chat/completions"

        return HttpClient.newCall {
            url(url)
            addHeader("Authorization", "Bearer $apiKey")
            post(body)
        }.execute().use { response ->
            val raw = response.body?.string() ?: return null
            if (!response.isSuccessful) {
                chat("§cRequest failed (${response.code}).")
                return null
            }

            val root = JsonParser().parse(raw).asJsonObject
            val choices = root.getAsJsonArray("choices") ?: return null
            if (choices.size() == 0) {
                return null
            }

            choices.get(0).asJsonObject
                .getAsJsonObject("message")
                .get("content")
                .asString
                .trim()
        }
    }
}
