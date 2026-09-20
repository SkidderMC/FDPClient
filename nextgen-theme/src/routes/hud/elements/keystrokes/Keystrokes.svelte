<script lang="ts">
    import Key from "./Key.svelte";
    import {onMount} from "svelte";
    import {getMinecraftKeybinds} from "../../../../integration/rest";
    import type {MinecraftKeybind} from "../../../../integration/types";
    import {listen} from "../../../../integration/ws";

    export let settings: { [name: string]: any } = {};

    let keyForward: MinecraftKeybind | undefined;
    let keyBack: MinecraftKeybind | undefined;
    let keyLeft: MinecraftKeybind | undefined;
    let keyRight: MinecraftKeybind | undefined;
    let keyJump: MinecraftKeybind | undefined;

    async function updateKeybinds() {
        const keybinds = await getMinecraftKeybinds();

        keyForward = keybinds.find(k => k.bindName === "key.forward");
        keyBack = keybinds.find(k => k.bindName === "key.back");
        keyLeft = keybinds.find(k => k.bindName === "key.left");
        keyRight = keybinds.find(k => k.bindName === "key.right");
        keyJump = keybinds.find(k => k.bindName === "key.jump");
    }

    onMount(updateKeybinds);

    listen("keybindChange", updateKeybinds)
</script>

<div class="keystrokes" class:with-border={settings.renderBorder} class:text-shadow={settings.textShadow} style="--key-size: {settings.keySize ?? 50}px; --key-border: {settings.borderWidth ?? 1}px; font-family: {settings.font ?? 'Inter'}, sans-serif;">
    <Key key={keyForward} gridArea="a" />
    <Key key={keyLeft} gridArea="b" />
    <Key key={keyBack} gridArea="c" />
    <Key key={keyRight} gridArea="d" />
    <Key key={keyJump} gridArea="e" />
</div>

<style lang="scss">
    .keystrokes {
      display: grid;
      grid-template-areas:
        ". a ."
        "b c d"
        "e e e";
      grid-template-columns: repeat(3, var(--key-size));
      gap: 5px;
    }
    .keystrokes :global(.key) { height: var(--key-size); font-family: inherit; }
    .with-border :global(.key) { border: var(--key-border) solid var(--accent-color); }
    .text-shadow :global(.key) { text-shadow: 0 1px 3px #000; }
</style>
