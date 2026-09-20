<script lang="ts">
    import {listen} from "../../../integration/ws";
    import {fly} from "svelte/transition";
    import {mapToColor} from "../../../util/color_utils";
    import {itemTextureUrl} from "../../../integration/rest";

    export let settings: { [name: string]: any };

    let cSettings: HudBlockCounterSettings;

    $: cSettings = settings as HudBlockCounterSettings;

    let nextBlock: string | undefined = undefined;
    let count: number | undefined = undefined;

    listen("blockCountChange", (data) => {
        nextBlock = data.nextBlock;
        count = data.count;
    });

    const FLEX_DIRECTION = {
        None: "row",
        Left: "row",
        Right: "row-reverse",
        Top: "column",
        Bottom: "column-reverse",
    };
</script>

{#if count !== undefined}
    <div class="counter" class:text-shadow={cSettings.shadowText} style="color: {cSettings.textColor !== undefined ? `rgba(${(cSettings.textColor >> 16) & 255},${(cSettings.textColor >> 8) & 255},${cSettings.textColor & 255},${((cSettings.textColor >>> 24) & 255) / 255})` : mapToColor(count)}; flex-direction: {FLEX_DIRECTION[cSettings.iconPosition]}; font-family: {cSettings.font ?? 'Inter'}, sans-serif; border-radius: {cSettings.radius ?? 5}px" in:fly={{ y: -5, duration: 200 }}
         out:fly={{ y: -5, duration: 200 }}>
        {#if nextBlock && cSettings.iconPosition !== "None"}
            <img class="icon" src={itemTextureUrl(nextBlock)} alt={nextBlock}/>
        {/if}
        {count}
    </div>
{/if}

<style lang="scss">

  .counter {
    background-color: var(--blockcounter-background-color);
    border-radius: 5px;
    white-space: nowrap;
    padding: 5px 8px;
    font-weight: 500;
    text-align: center;
    width: fit-content;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 5px;
    transform: translate(-100%);
  }

  .icon {
    width: 24px;
    height: 24px;
  }
  .text-shadow { text-shadow: 0 1px 3px #000; }
</style>
