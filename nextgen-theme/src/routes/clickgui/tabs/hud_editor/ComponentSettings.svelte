<script lang="ts">
    import {onMount, tick} from "svelte";
    import {getComponentSettings, setComponentSettings} from "../../../../integration/rest";
    import type {Alignment, ConfigurableSetting} from "../../../../integration/types";
    import TogglableSetting from "../../setting/TogglableSetting.svelte";

    export let name: string;
    export let id: string;
    export let alignment: Alignment;
    export let overlayOffset = 0;

    let element: HTMLElement | undefined;
    let configurable: ConfigurableSetting | undefined;

    let below = false;
    let marginLeft = 0;
    let componentHeight = 0;
    let maxHeight = 350;

    const SCREEN_EDGE_MARGIN = 10;
    const COMPONENT_GAP = 15;
    const MAX_SETTINGS_HEIGHT = 420;
    const MIN_SETTINGS_HEIGHT = 80;

    $: alignment.horizontalAlignment,
        alignment.horizontalOffset,
        alignment.verticalAlignment,
        alignment.verticalOffset,
        updatePosition();

    async function updatePosition() {
        await tick();

        if (!element) {
            return;
        }

        const componentElement = element.parentElement;
        if (componentElement) {
            const componentBounds = componentElement.getBoundingClientRect();
            componentHeight = componentElement.offsetHeight;
            const spaceAbove = componentBounds.top - COMPONENT_GAP - SCREEN_EDGE_MARGIN;
            const spaceBelow = window.innerHeight - componentBounds.bottom - COMPONENT_GAP - SCREEN_EDGE_MARGIN;
            below = spaceBelow >= spaceAbove;
            maxHeight = Math.max(
                MIN_SETTINGS_HEIGHT,
                Math.min(MAX_SETTINGS_HEIGHT, Math.max(spaceAbove, spaceBelow))
            );
        }

        await tick();

        const bounding = element.getBoundingClientRect();

        if (bounding.right > window.innerWidth - SCREEN_EDGE_MARGIN) {
            marginLeft = window.innerWidth - SCREEN_EDGE_MARGIN - bounding.right;
        } else if (bounding.left < SCREEN_EDGE_MARGIN) {
            marginLeft = SCREEN_EDGE_MARGIN - bounding.left;
        } else {
            marginLeft = 0;
        }
    }

    async function handleSettingChange() {
        if (!configurable) {
            return;
        }

        await setComponentSettings(id, configurable);
    }

    async function loadSettings() {
        const settings = await getComponentSettings(id);
        settings.value = settings.value.filter(setting => setting.name !== "Alignment");
        configurable = settings;
        await updatePosition();
    }

    onMount(() => {
        const resizeObserver = typeof ResizeObserver !== "undefined"
            ? new ResizeObserver(updatePosition)
            : undefined;

        if (element) resizeObserver?.observe(element);
        if (element?.parentElement) {
            resizeObserver?.observe(element.parentElement);
        }

        window.addEventListener("resize", updatePosition);
        loadSettings();

        return () => {
            resizeObserver?.disconnect();
            window.removeEventListener("resize", updatePosition);
        };
    });
</script>

<div
        class="settings-wrapper"
        class:below
        style="--component-height: {componentHeight}px; --overlay-offset: {overlayOffset}px; --settings-max-height: {maxHeight}px"
        bind:this={element}
>
    <!-- svelte-ignore a11y-no-static-element-interactions -->
    <div
        class="settings"
        style="transform: translateX({marginLeft}px)"
        on:wheel|stopPropagation
        on:mousedown|stopPropagation
    >
        {#if configurable !== undefined}
            <TogglableSetting path={name} bind:setting={configurable} on:change={handleSettingChange}>
                <svelte:fragment slot="control" let:disable let:label>
                    <div class="remove-component">
                        <button
                                title="Remove component"
                                on:click={disable}
                        >
                            <img src="img/clickgui/icon-cross.svg" alt="">
                        </button>
                        <span>{label}</span>
                    </div>
                </svelte:fragment>
            </TogglableSetting>
        {/if}
    </div>
</div>

<style lang="scss">
  .settings-wrapper {
    position: absolute;
    top: 0;
    left: 50%;
    transition: ease transform .2s;
    transform: translateY(calc(-100% - 15px - var(--overlay-offset))) translateX(-50%);

    .settings {
      background-color: var(--clickgui-hud-editor-component-settings-background-color);
      padding: 5px 10px;
      border-radius: 5px;
      width: 240px;
      box-shadow: 0 0 10px var(--clickgui-hud-editor-component-settings-shadow-color);
      max-height: var(--settings-max-height);
      overflow-x: hidden;
      overflow-y: auto;
      overscroll-behavior: contain;

      &::-webkit-scrollbar {
        width: 6px;
      }

      &::-webkit-scrollbar-thumb {
        border-radius: 6px;
        background-color: var(--accent-color);
      }

      .remove-component {
        display: flex;

        button {
          all: unset;
          align-items: center;
          min-width: 0;
          cursor: pointer;

          img {
            display: block;
            width: 10px;
            height: 10px;
            flex: 0 0 10px;
          }

        }

        span {
          margin-left: 7px;
          overflow: hidden;
          color: var(--clickgui-text-color);
          font-size: 12px;
          font-weight: 500;
          line-height: 12px;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    &::before,
    &::after {
      content: "";
      display: block;
      position: absolute;
      width: 0;
      height: 0;
      border-top: 8px solid transparent;
      border-bottom: 8px solid transparent;
      border-right: 8px solid var(--clickgui-hud-editor-component-settings-background-color);
      left: 50%;
      opacity: 0;
      transition: opacity .1s ease, transform .2s ease;
      z-index: -1;
    }

    &::before {
      top: -12px;
      transform: translateX(-50%) rotate(90deg) scale(.8);
    }

    &::after {
      bottom: -12px;
      opacity: 1;
      transform: translateX(-50%) rotate(-90deg);
    }

    &.below {
      transform: translateY(calc(var(--component-height) + 15px + var(--overlay-offset))) translateX(-50%);

      &::before {
        opacity: 1;
        transform: translateX(-50%) rotate(90deg);
      }

      &::after {
        opacity: 0;
        transform: translateX(-50%) rotate(-90deg) scale(.8);
      }
    }

  }
</style>
