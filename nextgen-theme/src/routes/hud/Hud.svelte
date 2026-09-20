<script lang="ts">
    import ArrayList from "./elements/ArrayList.svelte";
    import TargetHud from "./elements/targethud/TargetHud.svelte";
    import Watermark from "./elements/Watermark.svelte";
    import Notifications from "./elements/notifications/Notifications.svelte";
    import TabGui from "./elements/tabgui/TabGui.svelte";
    import HotBar from "./elements/hotbar/HotBar.svelte";
    import Scoreboard from "./elements/Scoreboard.svelte";
    import {onMount, setContext} from "svelte";
    import {
        getClientInfo,
        getComponents,
        getGameWindow,
        getMetadata,
        getNativeComponents
    } from "../../integration/rest";
    import {listen} from "../../integration/ws";
    import type {HudComponent, Metadata} from "../../integration/types";
    import Taco from "./elements/taco/Taco.svelte";
    import type {ComponentsUpdateEvent, ScaleFactorChangeEvent} from "../../integration/events";
    import Keystrokes from "./elements/keystrokes/Keystrokes.svelte";
    import Effects from "./elements/Effects.svelte";
    import BlockCounter from "./elements/BlockCounter.svelte";
    import Text from "./elements/Text.svelte";
    import DraggableComponent from "./elements/DraggableComponent.svelte";
    import KeyBinds from "./elements/KeyBinds.svelte";
    import GenericPlayerInventory from "./elements/inventory/GenericPlayerInventory.svelte";
    import {os} from "../clickgui/clickgui_store";
    import InventoryStatistics from "./elements/inventory/InventoryStatistics.svelte";
    import {intToRgba} from "../../integration/util";
    import {
        HUD_EDITOR_ELEMENTS_CONTEXT,
        type HudEditorDragState
    } from "../clickgui/tabs/hud_editor/constants";

    export let inEditor = false;
    export let onDragStateChange: ((state: HudEditorDragState) => void) | undefined = undefined;
    export let magneticTargetIds: string[] = [];

    let zoom = 100;
    let metadata: Metadata;
    let nativeComponents: HudComponent[] = [];
    let themeComponents: HudComponent[] = [];
    let selectedComponentId: string | undefined;

    $: renderedComponents = inEditor ? [...nativeComponents, ...themeComponents] : themeComponents;

    setContext(HUD_EDITOR_ELEMENTS_CONTEXT, new Map<string, HTMLElement>());

    function color(value: number | undefined, fallback: string, alphaMultiplier = 1): string {
        if (value === undefined) return fallback;
        const [r, g, b, a] = intToRgba(value);
        return `rgba(${r},${g},${b},${(a / 255) * alphaMultiplier})`;
    }

    function componentVisualStyle(settings: { [name: string]: any }): string {
        const accent = color(settings.accentColor, "var(--accent-color)");
        const background = color(settings.backgroundColor, "transparent");
        const text = color(settings.textColor, "#fff");
        const shadow = settings.shadow ? "0 6px 22px rgba(0,0,0,.32)" : "none";
        return `opacity:${settings.opacity ?? 1};transform:scale(${settings.elementScale ?? 1});` +
            `transform-origin:top left;padding:${settings.padding ?? 0}px;border-radius:${settings.radius ?? 5}px;` +
            `color:${text};box-shadow:${shadow};` +
            `--accent-color:${accent};--effects-background-color:${background};--effects-name-color:${text};` +
            `--effects-amplifier-color:${accent};--blockcounter-background-color:${background};` +
            `--scoreboard-body-background-color:${background};--scoreboard-header-background-color:${accent};` +
            `--targethud-background-color:${background};--targethud-text-color:${text};` +
            `--targethud-text-dimmed-color:${color(settings.textColor, "#bbb", .72)};` +
            `--inventory-background-color:${background};--hotbar-slot-background-color:${background};` +
            `--hotbar-slot-border-color:${accent};--hotbar-text-color:${text};`;
    }

    onMount(async () => {
        $os = (await getClientInfo()).os;

        const gameWindow = await getGameWindow();
        zoom = gameWindow.scaleFactor * 50;

        metadata = await getMetadata();
        [nativeComponents, themeComponents] = await Promise.all([
            inEditor ? getNativeComponents() : Promise.resolve([]),
            getComponents(metadata.id)
        ]);
    });

    listen("scaleFactorChange", (data: ScaleFactorChangeEvent) => {
        zoom = data.scaleFactor * 50;
    });

    listen("componentsUpdate", (event: ComponentsUpdateEvent) => {
        if (inEditor && event.source === "native") {
            nativeComponents = event.components;
        }

        if (event.source === "theme" && event.themeId === metadata?.id) {
            themeComponents = event.components;
        }

        if (selectedComponentId && ![...nativeComponents, ...themeComponents]
            .some(component => component.id === selectedComponentId && component.settings.enabled)) {
            selectedComponentId = undefined;
        }
    });

    function deselectOnCanvas(event: MouseEvent): void {
        if (inEditor && event.target === event.currentTarget) {
            selectedComponentId = undefined;
        }
    }
</script>

<!-- svelte-ignore a11y-no-static-element-interactions -->
<div class="hud" style="zoom: {zoom}%" on:mousedown={deselectOnCanvas}>
    {#each renderedComponents as c (c.id)}
        {#if c.settings.enabled}
            <DraggableComponent
                    {inEditor}
                    {onDragStateChange}
                    componentId={c.id}
                    componentName={c.name}
                    alignment={c.settings.alignment}
                    selected={selectedComponentId === c.id}
                    onSelect={() => selectedComponentId = c.id}
                    magneticallyReferenced={magneticTargetIds.includes(c.id)}
                    width={c.width}
                    height={c.height}
            >
                <div class="component-visual" style={componentVisualStyle(c.settings)}>
                {#if c.name === "Watermark"}
                    <Watermark settings={c.settings}/>
                {:else if c.name === "ArrayList"}
                    <ArrayList settings={c.settings}/>
                {:else if c.name === "TabGui"}
                    <TabGui settings={c.settings}/>
                {:else if c.name === "Notifications"}
                    <Notifications settings={c.settings}/>
                {:else if c.name === "TargetHud"}
                    <TargetHud settings={c.settings}/>
                {:else if c.name === "BlockCounter"}
                    <BlockCounter settings={c.settings}/>
                {:else if c.name === "Hotbar"}
                    <HotBar settings={c.settings}/>
                {:else if c.name === "Scoreboard"}
                    <Scoreboard settings={c.settings}/>
                {:else if c.name === "ArmorItems"}
                    <GenericPlayerInventory
                            settings={c.settings}
                            rowLength={1}
                            backgroundColor="transparent"
                            gap="2px"
                            getRenderedStacks={it => Array.from(it.armor).reverse()}
                    />
                {:else if c.name === "InventoryStatistics"}
                    <InventoryStatistics settings={c.settings}/>
                {:else if c.name === "Inventory"}
                    <GenericPlayerInventory settings={c.settings} rowLength={9} getRenderedStacks={it => it.main.slice(9)}/>
                {:else if c.name === "CraftingInventory"}
                    <GenericPlayerInventory settings={c.settings} rowLength={2} getRenderedStacks={it => it.crafting}/>
                {:else if c.name === "EnderChestInventory"}
                    <GenericPlayerInventory settings={c.settings} rowLength={9} getRenderedStacks={it => it.enderChest}/>
                {:else if c.name === "Taco"}
                    <Taco/>
                {:else if c.name === "Keystrokes"}
                    <Keystrokes settings={c.settings}/>
                {:else if c.name === "Effects"}
                    <Effects settings={c.settings}/>
                {:else if c.name === "Text"}
                    <Text settings={c.settings}/>
                {:else if c.name === "Image"}
                    <img alt="" src="{c.settings.uRL}" style="transform: scale({c.settings.scale}); transform-origin: top left; color: {color(c.settings.color, '#fff')}; filter: {c.settings.imageShadow ? `drop-shadow(${c.settings.shadowXDistance ?? 0}px ${c.settings.shadowYDistance ?? 0}px 5px ${color(c.settings.shadowColor, '#000')})` : 'none'};">
                {:else if c.name === "KeyBinds"}
                    <KeyBinds settings={c.settings}/>
                {:else if c.width !== undefined && c.height !== undefined}
                    <div></div>
                {/if}
                </div>
            </DraggableComponent>
        {/if}
    {/each}
</div>

<style lang="scss">
  .hud {
    height: 100vh;
    width: 100vw;
  }

  .component-visual { width: max-content; height: max-content; }
</style>
