<script lang="ts">
    import {onMount} from "svelte";
    import type {ConfigurableSetting as ConfigurableSettingData} from "../../../integration/types";
    import {getGlobalSettings, getMetadata, getTheme, setGlobalSettings} from "../../../integration/rest";
    import {intToRgba, rgbaToHex} from "../../../integration/util";
    import ConfigurableSetting from "../setting/ConfigurableSetting.svelte";
    import WindowPanel from "./WindowPanel.svelte";

    let globalSettings = $state<ConfigurableSettingData | null>(null);
    let metadataId = "";
    let defaultSurfaceColor = "#000000";

    async function fetchGlobalSettings() {
        globalSettings = await getGlobalSettings();
    }

    function setThemeColor(name: string, value: string) {
        document.documentElement.style.setProperty(`--${name}`, value);
    }

    function hexToRgb(value: string) {
        const raw = value.replace("#", "").slice(0, 6);
        const normalized = raw.length === 3
            ? raw.split("").map(v => v + v).join("")
            : raw.padEnd(6, "0");

        return [
            parseInt(normalized.slice(0, 2), 16),
            parseInt(normalized.slice(2, 4), 16),
            parseInt(normalized.slice(4, 6), 16)
        ];
    }

    function mixColors(leftColor: string, rightColor: string, strength: number) {
        const left = hexToRgb(leftColor);
        const right = hexToRgb(rightColor);
        const mix = Math.max(0, Math.min(100, strength)) / 100;

        return `#${left.map((value, index) =>
            Math.round(value * (1 - mix) + right[index] * mix)
                .toString(16)
                .padStart(2, "0")
        ).join("")}`;
    }

    async function refreshThemeColors() {
        if (!metadataId) {
            const metadata = await getMetadata();
            metadataId = metadata.id;
            defaultSurfaceColor = metadata.colors.Tint;
        }

        const theme = await getTheme(metadataId);
        setThemeColor("accent-color", rgbaToHex(intToRgba(theme.colors.accent)));
        setThemeColor("surface-color", mixColors(defaultSurfaceColor, rgbaToHex(intToRgba(theme.colors.tint)), 18));
    }

    async function updateGlobalSettings() {
        if (!globalSettings) return;

        await setGlobalSettings($state.snapshot(globalSettings));
        await fetchGlobalSettings();
        await refreshThemeColors();
    }

    onMount(async () => {
        await refreshThemeColors();
        await fetchGlobalSettings();
    });
</script>

<WindowPanel title="Global Settings" icon="client">
    <div class="settings-grid">
        {#if globalSettings}
            {#each globalSettings.value as _, i (globalSettings.value[i].name)}
                {#if globalSettings.value[i].valueType === "CONFIGURABLE" ||
                globalSettings.value[i].valueType === "TOGGLEABLE"}
                    <div class="setting-item">
                        <ConfigurableSetting
                                path="clickgui.global"
                                bind:setting={globalSettings.value[i]}
                                hideExpandControl={true}
                                on:change={updateGlobalSettings}
                        />
                    </div>
                {/if}
            {/each}
        {/if}
    </div>
</WindowPanel>

<style lang="scss">

  .settings-grid {
    column-count: 2;
    column-gap: 25px;
    column-rule: 1px solid var(--clickgui-global-settings-divider-color);
    column-fill: balance;
    overflow: visible;
  }

  @media (max-width: 900px) {
    .settings-grid {
      column-count: 1;
    }
  }

  .setting-item {
    break-inside: avoid;
    display: inline-block;
    width: 100%;
    margin-bottom: 15px;
  }
</style>
