<script lang="ts">
    export let maxHealth: number;
    export let health: number;
    export let speed = 0.2;

    let width = 0;
    $: width = maxHealth > 0 && Number.isFinite(health)
        ? Math.max(0, Math.min(100, Math.ceil(health / maxHealth * 100)))
        : 0;
</script>

<div class="health-progress">
    <div class="thumb" style="width: {width}%; transition-duration: {Math.max(0.01, speed)}s;"></div>
</div>

<style lang="scss">

    .health-progress {
        position: relative;
    }

    .thumb {
        height: 8px;
        background-color: var(--targethud-health-progress-color);
        transition: ease width 0.5s;
    }
</style>
