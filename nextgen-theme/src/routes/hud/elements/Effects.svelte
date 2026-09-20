<script lang="ts">
    import {listen} from "../../../integration/ws";
    import type {ClientPlayerDataEvent, ClientPlayerEffectEvent} from "../../../integration/events";
    import type {StatusEffect} from "../../../integration/types";
    import {effectTextureUrl} from "../../../integration/rest";

    export let settings: { [name: string]: any } = {};

    let effects: StatusEffect[] = [];

    listen("clientPlayerData", (event: ClientPlayerDataEvent) => {
        effects = event.playerData.effects;
    });

    listen("clientPlayerEffect", (event: ClientPlayerEffectEvent) => {
        effects = event.effects;
    });

    function formatTime(duration: number): string {
        if (duration === -1) {
            return "*:*";
        }

        const totalSeconds = Math.floor(duration / 20);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;

        return `${minutes}:${seconds.toString().padStart(2, "0")}`;
    }

    function formatAmplifier(n: number): string {
        return (n + 1).toString();
    }
</script>

{#if effects.length > 0}
    <div class="effects mode-{String(settings.mode ?? 'FDP').toLowerCase()}" style="font-family: {settings.font ?? 'Inter'}, sans-serif; text-align: {String(settings.titleAlign ?? 'Left').toLowerCase()};">
        {#each effects as e}
            <div class="effect">
                {#if settings.icon !== false}
                <img class="effect-icon" src={effectTextureUrl(e.effect)} alt={e.localizedName}/>
                {/if}
                {#if settings.name !== false}
                <span class="name">{e.localizedName}  <span
                        class="amplifier">{formatAmplifier(e.amplifier)}</span></span>
                {/if}
                <span class="duration">{formatTime(e.duration)}</span>
                {#if settings.durationBar !== false && e.duration > 0}
                    <span class="duration-bar" style="--duration-progress: {Math.min(100, e.duration / 12)}%"></span>
                {/if}
            </div>
        {/each}
    </div>
{/if}

<style lang="scss">

  .effects {
    display: flex;
    flex-direction: column;
    gap: 4px;
    background-color: var(--effects-background-color);
    border-radius: 5px;
    padding: 4px 6px;
  }

  .effect {
    position: relative;
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
    font-size: 14px;

    .effect-icon {
      width: 16px;
      height: 16px;
      image-rendering: pixelated;
      image-rendering: -moz-crisp-edges;
      image-rendering: crisp-edges;
    }

    .name {
      color: var(--effects-name-color);
    }

    .amplifier {
      color: var(--effects-amplifier-color);
    }

    .duration {
      margin-left: auto;
      font-family: monospace;
      color: var(--effects-duration-color);
      font-size: 12px;
    }
  }
  .duration-bar { position: absolute; left: 0; bottom: -2px; width: var(--duration-progress); height: 1px; background: var(--accent-color); }
  .mode-compact .effect-icon { display: none; }
  .mode-classic { border-radius: 0; }
</style>
