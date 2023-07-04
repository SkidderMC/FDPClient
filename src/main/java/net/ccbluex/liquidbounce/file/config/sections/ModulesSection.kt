package net.ccbluex.liquidbounce.file.config.sections

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.module.EnumAutoDisableType
import net.ccbluex.liquidbounce.features.module.EnumTriggerType
import net.ccbluex.liquidbounce.file.config.ConfigSection

class ModulesSection : ConfigSection("modules") {
    override fun load(json: JsonObject) {
        // set them to default setting
        FDPClient.moduleManager.modules.forEach {
            val moduleInfo = it.moduleInfo
            it.state = moduleInfo.defaultOn
            it.keyBind = moduleInfo.keyBind
            it.array = moduleInfo.array
            it.autoDisable = moduleInfo.autoDisable
            it.values.forEach { value ->
                value.setDefault()
            }
        }
        // load config
        for (entrySet in json.entrySet()) {
            val module = FDPClient.moduleManager.getModule(entrySet.key) ?: continue
            val data = entrySet.value.asJsonObject

            if (data.has("state")) {
                module.state = data.get("state").asBoolean
            }

            if (data.has("keybind")) {
                module.keyBind = data.get("keybind").asInt
            }

            if (data.has("array")) {
                module.array = data.get("array").asBoolean
            }

            if (data.has("trigger")) {
                module.triggerType = EnumTriggerType.valueOf(data.get("trigger").asString)
            }

            if (data.has("autodisable")) {
                module.autoDisable = EnumAutoDisableType.valueOf(data.get("autodisable").asString)
            }

            val values = data.getAsJsonObject("values")
            module.values.forEach {
                if (values.has(it.name)) {
                    it.fromJson(values.get(it.name))
                }
            }
        }
    }

    override fun save(): JsonObject {
        val json = JsonObject()

        FDPClient.moduleManager.modules.forEach {
            val moduleJson = JsonObject()

            if (it.canEnable || it.triggerType != EnumTriggerType.PRESS) {
                moduleJson.addProperty("state", it.state)
            }

            moduleJson.addProperty("keybind", it.keyBind)

            if (it.canEnable) {
                moduleJson.addProperty("array", it.array)
            }

            if (it.canEnable) {
                moduleJson.addProperty("autodisable", it.autoDisable.toString())
            }

            moduleJson.addProperty("trigger", it.triggerType.toString())

            val valuesJson = JsonObject()
            it.values.forEach { value ->
                valuesJson.add(value.name, value.toJson())
            }
            moduleJson.add("values", valuesJson)

            json.add(it.name, moduleJson)
        }

        return json
    }
}