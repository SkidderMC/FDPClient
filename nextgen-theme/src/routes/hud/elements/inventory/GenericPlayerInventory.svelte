<script lang="ts">
    import type {ItemStack} from "../../../../integration/types";
    import {listen} from "../../../../integration/ws";
    import type {ClientPlayerInventoryEvent, PlayerInventory} from "../../../../integration/events";
    import ItemStackView from "./ItemStackView.svelte";
    import {onMount} from "svelte";
    import {getPlayerInventory} from "../../../../integration/rest";

    export let rowLength: number;
    export let backgroundColor: string = "var(--inventory-background-color)";
    export let gap: string = "0.5rem";
    export let getRenderedStacks: (inventory: PlayerInventory) => ItemStack[];
    export let settings: { [name: string]: any } = {};

    let inventory: PlayerInventory | undefined;
    let stacks: ItemStack[] = [];

    listen("clientPlayerInventory", (data: ClientPlayerInventoryEvent) => {
        inventory = data.inventory;
    });

    onMount(async () => {
        inventory = await getPlayerInventory();
    });

    $: stacks = inventory ? getRenderedStacks(inventory) : [];
</script>

<div class="inventory-wrapper">
{#if settings.showTitle}<div class="inventory-title" style="color: var(--inventory-title-color)">{settings.title}</div>{/if}
<div class="inventory" class:with-border={settings.border} style="
    background-color: {backgroundColor};
    gap: {settings.slotGap ?? gap}px;
    --row-length: {rowLength};
    --item-size: {settings.slotSize ?? 32}px;
    --inventory-title-color: var(--accent-color);
    border-color: var(--accent-color);
">
    {#each stacks as stack (stack)}
        <ItemStackView {stack}/>
    {/each}
</div>
</div>

<style lang="scss">
  .inventory {
    padding: 4px;
    border-radius: 5px;
    display: grid;
    grid-template-columns: repeat(var(--row-length), 1fr);
  }
  .with-border { border: 1px solid; }
  .inventory-title { padding: 5px 7px; font-size: 13px; font-weight: 700; background: var(--inventory-background-color); border-radius: 5px 5px 0 0; }
</style>
