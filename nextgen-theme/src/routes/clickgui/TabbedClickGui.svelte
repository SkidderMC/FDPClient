<script lang="ts">
    import ClickGui from "./ClickGui.svelte";
    import GlobalSettings from "./tabs/GlobalSettings.svelte";
    import Tabs from "./tabs/Tabs.svelte";
    import {darken, gridSize, os, scaleFactor, snappingEnabled} from "./clickgui_store";
    import type {ConfigurableSetting, ModuleSetting, TogglableSetting} from "../../integration/types";
    import {onMount} from "svelte";
    import {
        getClientInfo,
        getGameWindow,
        getModuleSettings,
        setHudEditorSelected,
        setTyping
    } from "../../integration/rest";
    import {listen} from "../../integration/ws";
    import type {ClickGuiValueChangeEvent, ScaleFactorChangeEvent} from "../../integration/events";
    import HudEditor from "./tabs/hud_editor/HudEditor.svelte";
    import {getHashParams} from "../../integration/util";

    const tabs = [
        {title: "ClickGUI", content: ClickGui},
        {title: "HUD Editor", content: HudEditor},
        {title: "Settings", content: GlobalSettings}
    ];

    let activeTab = $state(getHashParams().get("tab") === "hud-editor" ? 1 : 0);
    let minecraftScaleFactor = $state(2);
    let clickGuiScaleFactor = $state(1);

    $effect(() => {
        $scaleFactor = minecraftScaleFactor * clickGuiScaleFactor;
    });

    function applyValues(configurable: ConfigurableSetting) {
        const scaleValue = findSetting(configurable.value, "Scale");
        const snappingValue = findSetting(configurable.value, "Snapping") as TogglableSetting | undefined;

        if (scaleValue) {
            clickGuiScaleFactor = scaleValue.value as number;
        }

        if (snappingValue) {
            $snappingEnabled = snappingValue.value.find(v => v.name === "Enabled")?.value as boolean ?? true;
            $gridSize = snappingValue.value.find(v => v.name === "GridSize")?.value as number ?? 10;
        }
    }

    function findSetting(settings: ModuleSetting[], name: string): ModuleSetting | undefined {
        for (const setting of settings) {
            if (setting.name === name) return setting;
            if ((setting.valueType === "CONFIGURABLE" || setting.valueType === "TOGGLEABLE") &&
                Array.isArray(setting.value)) {
                const nested = findSetting(setting.value as ModuleSetting[], name);
                if (nested) return nested;
            }
        }
        return undefined;
    }

    onMount(async () => {
        await setHudEditorSelected(false);
        $os = (await getClientInfo()).os;

        const gameWindow = await getGameWindow();
        minecraftScaleFactor = gameWindow.scaleFactor;

        const clickGuiSettings = await getModuleSettings("ClickGUI");
        applyValues(clickGuiSettings);

        await setTyping(false);
    });

    listen("scaleFactorChange", (e: ScaleFactorChangeEvent) => {
        minecraftScaleFactor = e.scaleFactor;
    });

    listen("clickGuiValueChange", (e: ClickGuiValueChangeEvent) => {
        applyValues(e.configurable);
    });
</script>

<div
        class="tabbed-clickgui"
        class:darken={$darken}
>
    <Tabs {tabs} bind:activeTab/>
</div>

<style lang="scss">

  .tabbed-clickgui {
    overflow: hidden;
    position: absolute;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    transition: ease background-color .2s;

    &.darken {
      background-color: var(--clickgui-overlay-background-color);
    }
  }
</style>
