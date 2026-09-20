<script lang="ts">
    import {onMount} from "svelte";
    import {getModules} from "../../../integration/rest";
    import {listen} from "../../../integration/ws";
    import {convertToSpacedString, spaceSeperatedNames} from "../../../theme/theme_config";
    import type {Module} from "../../../integration/types";
    import {UNKNOWN_KEY} from "../../../util/utils";
    import BindDisplay from "../../clickgui/setting/bind/BindDisplay.svelte";
    import {intToRgba} from "../../../integration/util";

    let {settings = {}} = $props<{settings?: { [name: string]: any }}>();

    let modules: Module[] = $state([]);

    function color(value: number | undefined, fallback: string): string {
        if (value === undefined) return fallback;
        const [r, g, b, a] = intToRgba(value);
        return `rgba(${r}, ${g}, ${b}, ${a / 255})`;
    }

    async function updateModulesWithBinds() {
        modules = (await getModules()).filter(m => m.keyBind.boundKey !== UNKNOWN_KEY);
    }

    listen("moduleToggle", updateModulesWithBinds);
    listen("valueChanged", async (e) => {
        if (e.value.name === "Bind") {
            await updateModulesWithBinds();
        }
    })

    onMount(async () => {
        await updateModulesWithBinds();
    });
</script>

<div
    class="keybinds"
    style="min-width: {settings.minWidth ?? 150}px; --keybinds-accent-color: {color(settings.keyColor, 'var(--accent-color)')}; --keybinds-enabled-color: {color(settings.keyColor, 'var(--accent-color)')};"
>
    {#if settings.showTitle !== false}
    <div class="header">
        <span class="title">{settings.title ?? "Binds"}</span>
        {#if settings.icon !== false}<img class="icon" src="img/hud/keybinds/icon-keybinds.svg" alt="keybinds">{/if}
    </div>
    {/if}
    <div class="entries">
        {#each modules as m (m.name)}
            <div class="row" class:enabled={m.enabled}>
                <span class="module-name">{$spaceSeperatedNames ? convertToSpacedString(m.name) : m.name}</span>
                <span class="key-bind" class:muted={!m.enabled}>
                    [<BindDisplay boundKey={m.keyBind.boundKey} modifiers={m.keyBind.modifiers}/>]
                </span>
            </div>
        {:else}
            <div class="no-binds">No key bindings</div>
        {/each}
    </div>
</div>

<style lang="scss">

  .keybinds {
    width: max-content;
    border-radius: 5px;
    overflow: hidden;
    font-size: 14px;
    min-width: 150px;
    max-width: 200px;
    box-shadow: 0 5px 18px rgba(0, 0, 0, .24);
  }

  .header {
    background-color: var(--keybinds-background-color);
    padding: 7px 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .title {
      color: var(--keybinds-text-color);
      font-weight: 600;
    }

    .icon {
      width: 16px;
      height: 16px;
      min-width: 16px;
      max-width: 16px;
      object-fit: contain;
      flex: 0 0 16px;
    }
  }

  .entries {
    background-color: var(--keybinds-header-background-color);
    padding: 6px 10px;
    color: var(--keybinds-text-color);

    .no-binds {
      font-style: italic;
      margin-bottom: 5px;
    }
  }

  .row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 5px;
    gap: 12px;
    min-width: 0;

    &:last-child {
      margin-bottom: 0;
    }

    &.enabled {
      .module-name {
        color: var(--keybinds-enabled-color);
        font-weight: 500;
      }
    }

    .module-name {
      color: var(--keybinds-text-color);
      font-size: 14px;
      flex: 1;
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .key-bind {
      display: inline-flex;
      align-items: center;
      font-family: monospace;
      font-size: 11px;
      color: var(--keybinds-accent-color);
      font-weight: 600;
      flex-shrink: 0;
      min-width: max-content;
      white-space: nowrap;
      flex-wrap: nowrap;

      &.muted {
        color: var(--keybinds-text-muted-color);
        font-weight: 500;
      }
    }

    .key-bind :global(.wrapper),
    .key-bind :global(.boundKey),
    .key-bind :global(.modifier) {
      white-space: nowrap;
      flex-wrap: nowrap;
    }
  }
</style>
