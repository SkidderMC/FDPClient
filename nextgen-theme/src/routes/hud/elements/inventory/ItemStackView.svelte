<script lang="ts">
    import type {ItemStack} from "../../../../integration/types";
    import {mapToColor} from "../../../../util/color_utils";
    import {itemTextureUrl} from "../../../../integration/rest";

    export let stack: ItemStack;

    let count = stack.count;
    let damage = stack.damage;
    let identifier = stack.identifier;
    let maxDamage = stack.maxDamage;
    let enchantments = stack.enchantments;
    let durability = 100;
    let countColor = "white";
    let valueColor = mapToColor(120);

    $: {
        count = stack.count;
        damage = stack.damage;
        identifier = stack.identifier;
        maxDamage = stack.maxDamage;
        enchantments = stack.enchantments;
        durability = maxDamage > 0
            ? Math.max(0, Math.min(100, 100 * (maxDamage - damage) / maxDamage))
            : 100;
        countColor = count <= 0 ? "red" : "white";
        valueColor = mapToColor(1.2 * durability);
    }
</script>

<div class="item-stack">
    {#if enchantments}
        <div class="mask" style="mask-image: url({itemTextureUrl(identifier)})"></div>
    {/if}
    <img class="item-icon" src={itemTextureUrl(identifier)} alt={identifier}/>

    <div class="durability-bar" class:hidden={damage === 0}>
        <div class="durability"
             style="width: {durability}%; background-color: {valueColor}">
        </div>
    </div>

    <div class="count" class:hidden={count === 1 || identifier === "minecraft:air"} style="color: {countColor}">
        {count}
    </div>
</div>

<style lang="scss">

  .hidden {
    display: none;
  }

  .item-stack {
    position: relative;
    width: var(--item-size, 32px);
    height: var(--item-size, 32px);
  }

  .mask {
    position: absolute;
    background: radial-gradient(circle, var(--item-enchant-glow-start-color), var(--item-enchant-glow-end-color) 100%);
    mix-blend-mode: screen;
    transform: scale(1.05);
    transform-origin: center;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    mask-size: cover;
  }

  .item-icon {
    width: 100%;
    height: 100%;
  }

  .durability-bar {
    position: absolute;
    bottom: 0;
    left: 10%;
    width: 80%;
    height: 2px;
    background-color: var(--item-damage-background-color);
  }

  .durability {
    height: 100%;
    transition: width 150ms;
  }

  .count {
    position: absolute;
    bottom: 0;
    right: 0;
    font-size: 14px;
    font-weight: bold;
    text-shadow: 1px 1px var(--item-count-shadow-color); // This is inconsistent with other UI elements but it looks better so I will let it pass ~Senk Ju
    font-family: monospace;
  }
</style>
