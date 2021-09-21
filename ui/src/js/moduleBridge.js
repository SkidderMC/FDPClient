const LiquidBounce = java.importClass("net.ccbluex.liquidbounce.LiquidBounce")
const Module = java.importClass("net.ccbluex.liquidbounce.features.module.Module")
const BoolValue = java.importClass("net.ccbluex.liquidbounce.value.BoolValue")
const TextValue = java.importClass("net.ccbluex.liquidbounce.value.TextValue")
const ListValue = java.importClass("net.ccbluex.liquidbounce.value.ListValue")
const IntegerValue = java.importClass("net.ccbluex.liquidbounce.value.IntegerValue")
const FloatValue = java.importClass("net.ccbluex.liquidbounce.value.FloatValue")
const FontValue = java.importClass("net.ccbluex.liquidbounce.value.FontValue")
const ModuleCategory = java.importClass("net.ccbluex.liquidbounce.features.module.ModuleCategory")
const GameFontRenderer = java.importClass("net.ccbluex.liquidbounce.ui.font.GameFontRenderer")
const ClickGUI = java.importClass("net.ccbluex.liquidbounce.launch.data.ultralight.ClickGUIModule").INSTANCE
const Fonts = java.importClass("net.ccbluex.liquidbounce.ui.font.Fonts")
const Float = java.importClass("java.lang.Float")
const Integer = java.importClass("java.lang.Integer")

function getDataJson(module) {
    var json = {
        Name: translate(module.getLocalizedName()),
        Description: translate(module.getDescription()),
        Key: module.getName(),
        Enable: module.getState(),
        Setting: []
    }
    bridge.forEach(module.getValues(), function(value) {
        if (bridge.instanceOf(BoolValue, value)) {
            json.Setting.push({
                Type: 'Bool',
                Val: value.get()
            })
        } else if (bridge.instanceOf(TextValue, value)) {
            json.Setting.push({
                Type: 'Text',
                Val: value.get()
            })
        } else if (bridge.instanceOf(ListValue, value)) {
            var select = {
                Type: 'Mode',
                Val: value.get(),
                Vals: []
            };
            var listValues = value.getValues()
            for (var j = 0; j < listValues.length; j++) {
                select.Vals.push(listValues[j])
            }
            json.Setting.push(select)
        } else if (bridge.instanceOf(IntegerValue, value)) {
            json.Setting.push({
                Type: 'Num',
                Val: value.get(),
                Step: 1,
                MinVal: value.getMinimum(),
                MaxVal: value.getMaximum()
            })
        } else if (bridge.instanceOf(FloatValue, value)) {
            json.Setting.push({
                Type: 'Num',
                Val: value.get(),
                Step: .01,
                MinVal: value.getMinimum(),
                MaxVal: value.getMaximum()
            })
        } else if (bridge.instanceOf(FontValue, value)) {
            var select = {
                Type: 'Mode',
                Val: getFontName(value.get()),
                Vals: []
            };
            bridge.forEach(Fonts.getFonts(), function(font) {
                select.Vals.push(getFontName(font))
            })
            json.Setting.push(select)
        }
        var lastJson = json.Setting[json.Setting.length - 1]
        lastJson.Name = translate(value.getName())
        lastJson.Key = value.getName()
        lastJson.isVisable = value.getDisplayable
    })
    return json
}

function getFontName(font) {
    if (bridge.instanceOf(GameFontRenderer, font)) {
        return font.getDefaultFont().getFont().getName() + " - " + font.getDefaultFont().getFont().getSize();
    } else if (bridge.equal(font, Fonts.minecraftFont)) {
        return "Minecraft"
    }
    return "Unknown"
}

function getCategoryJson() {
    var json = []
    var values = ModuleCategory.values()
    for (var i = 0; i < values.length; i++) {
        var category = values[i]
        var categoryJson = {
            Type: translate(category.getDisplayName()),
            Icon: category.getHtmlIcon(),
            modules: []
        }
        bridge.forEach(LiquidBounce.moduleManager.getModules(), function(module) {
            if (bridge.equal(module.getCategory(), category)) {
                categoryJson.modules.push(getDataJson(module))
            }
        })
        json.push(categoryJson)
    }
    return json
}

function getModule(name) {
    return LiquidBounce.moduleManager.getModule(name)
}

function setModuleValue(name, valueName, value) {
    getModule(name).getValue(valueName).set(value)
}