<script lang="ts">
    import {onMount} from "svelte";
    import {listen} from "../../../integration/ws";
    import {getPlayerData} from "../../../integration/rest";
    import type {ClientPlayerDataEvent} from "../../../integration/events";
    import type {PlayerData} from "../../../integration/types";
    import {intToRgba} from "../../../integration/util";

    export let settings: { [name: string]: any };

    let player: PlayerData | null = null;
    $: c = settings as any;

    function color(value: number | undefined, fallback: string): string {
        if (value === undefined) return fallback;
        const [r, g, b, a] = intToRgba(value);
        return `rgba(${r}, ${g}, ${b}, ${a / 255})`;
    }

    function fixed(value: number | undefined, digits = 1): string {
        return Number.isFinite(value) ? Number(value).toFixed(digits) : "0";
    }

    function position(p: PlayerData): string {
        return `${Math.floor(p.position.x)} ${Math.floor(p.position.y)} ${Math.floor(p.position.z)}`;
    }

    listen("clientPlayerData", (event: ClientPlayerDataEvent) => player = event.playerData);
    onMount(async () => player = await getPlayerData());
</script>

{#if player}
    <div
        class="watermark style-{String(c.style ?? 'Chips').toLowerCase()}"
        class:with-shadow={c.shadow}
        class:with-blur={c.blur}
        style="--wm-accent: {color(c.accentColor, 'var(--accent-color)')}; --wm-bg: {color(c.backgroundColor, 'rgba(8, 12, 20, .72)')}; --wm-text: {color(c.textColor, '#fff')}; --wm-radius: {c.radius ?? 7}px; --wm-font-size: {c.fontSize ?? 13}px;"
    >
        {#if c.showLogo}<div class="chip brand"><span class="mark">F</span><strong>FDP</strong></div>{/if}
        {#if c.showPlayerName}<div class="chip"><span class="label">USER</span>{player.username}</div>{/if}
        {#if c.showFPS}<div class="chip"><span class="label">FPS</span>{player.fps ?? 0}</div>{/if}
        {#if c.showPosition}<div class="chip"><span class="label">XYZ</span>{position(player)}</div>{/if}
        {#if c.showPing}<div class="chip"><span class="label">PING</span>{player.ping ?? 0} ms</div>{/if}
        {#if c.showTPS}<div class="chip"><span class="label">TPS</span>{fixed(player.tps, 1)}</div>{/if}
        {#if c.showBPS}<div class="chip"><span class="label">BPS</span>{fixed(player.bps, 2)}</div>{/if}
        {#if c.showAnticheat}<div class="chip"><span class="label">AC</span>{player.anticheat ?? "Unknown"}</div>{/if}
        {#if c.showOnline}<div class="chip"><span class="label">ONLINE</span>{player.onlinePlayers ?? 1}</div>{/if}
        {#if c.showBiomeLight}<div class="chip"><span class="label">BIOME</span>{player.biome ?? "Unknown"} · {player.light ?? 0}</div>{/if}
        {#if c.showDimension}<div class="chip"><span class="label">WORLD</span>{player.dimension ?? "Overworld"}</div>{/if}
    </div>
{/if}

<style lang="scss">
  .watermark {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 5px;
    max-width: 720px;
    color: var(--wm-text);
    font-size: var(--wm-font-size);
    line-height: 1;
  }

  .chip {
    display: flex;
    align-items: center;
    gap: 6px;
    min-height: 22px;
    padding: 4px 8px;
    border: 1px solid rgba(255, 255, 255, .09);
    border-radius: var(--wm-radius);
    background: var(--wm-bg);
    white-space: nowrap;
  }

  .label { color: var(--wm-accent); font-size: .72em; font-weight: 800; letter-spacing: .06em; }
  .brand { padding-left: 5px; }
  .mark { display: flex; align-items: center; justify-content: center; width: 19px; height: 19px; border-radius: calc(var(--wm-radius) - 2px); background: var(--wm-accent); color: #071018; font-weight: 900; }
  .with-shadow .chip { box-shadow: 0 5px 18px rgba(0, 0, 0, .32); }
  .with-blur .chip { background-color: var(--wm-bg); }

  .style-compact { gap: 0; overflow: hidden; border-radius: var(--wm-radius); background: var(--wm-bg); }
  .style-compact .chip { border: 0; border-right: 1px solid rgba(255,255,255,.08); border-radius: 0; background: transparent; box-shadow: none; }
  .style-panel { display: grid; grid-template-columns: repeat(3, max-content); gap: 4px; padding: 6px; border-radius: var(--wm-radius); background: var(--wm-bg); }
  .style-panel .chip { border: 0; background: rgba(255,255,255,.035); box-shadow: none; }
</style>
