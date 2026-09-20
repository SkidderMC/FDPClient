<script lang="ts">
    import ArmorStatus from "./ArmorStatus.svelte";
    import {listen} from "../../../../integration/ws.js";
    import type {PlayerData} from "../../../../integration/types";
    import {REST_BASE} from "../../../../integration/host";
    import {fly} from "svelte/transition";
    import HealthProgress from "./HealthProgress.svelte";
    import type {TargetChangeEvent} from "../../../../integration/events";

    export let settings: { [name: string]: any } = {};

    let target: PlayerData | null = null;
    let visible = true;
    let skinFailed = false;

    let hideTimeout: number;

    function startHideTimeout() {
        hideTimeout = setTimeout(() => {
            visible = false;
        }, 1000);
    }

    listen("targetChange", (data: TargetChangeEvent) => {
        if (data.target === null) {
            startHideTimeout();
            return;
        }
        if (settings.onlyPlayer !== false && data.target.isPlayer === false) {
            return;
        }
        target = data.target;
        visible = true;
        skinFailed = false;
        clearTimeout(hideTimeout);
        startHideTimeout();
    });

    startHideTimeout();
</script>

{#if visible && target != null}
    <div
        class="targethud style-{String(settings.style ?? 'Modern').toLowerCase()}"
        transition:fly={{
            x: settings.animation === "Slide" ? 24 : 0,
            y: settings.animation === "Fade" ? 0 : -10,
            duration: Math.max(40, (settings.animationSpeed ?? .2) * 1000)
        }}
    >
        <div class="main-wrapper">
            {#if settings.showAvatar !== false}<div class="avatar">
                <span class="avatar-fallback">{target.username.slice(0, 1).toUpperCase()}</span>
                {#if !skinFailed}
                    <img
                        src="{REST_BASE}/api/v1/client/resource/skin?uuid={encodeURIComponent(target.uuid)}"
                        alt=""
                        on:error={() => skinFailed = true}
                    />
                {/if}
            </div>{/if}
    
            <div class="name">{target.username}</div>
            <div class="health-stats">
                <div class="stat">
                    <div class="value">{Math.floor(target.actualHealth)}</div>
                    <img
                            class="icon"
                            src="img/hud/targethud/icon-health.svg"
                            alt="health"
                    />
                </div>
                {#if target.absorption > 0}
                    <div class="stat">
                        <div class="value">{Math.floor(target.absorption)}</div>
                        <img
                                class="icon"
                                src="img/hud/targethud/icon-absorption.svg"
                                alt="absorption"
                        />
                    </div>
                {/if}
                <div class="stat">
                    <div class="value">{Math.floor(target.armor)}</div>
                    <img
                            class="icon"
                            src="img/hud/targethud/icon-armor.svg"
                            alt="armor"
                    />
                </div>
            </div>
            {#if settings.showArmor !== false}<div class="armor-stats">
                {#if target.armorItems[3].count > 0}
                    <ArmorStatus itemStack={target.armorItems[3]} />
                {/if}
                {#if target.armorItems[2].count > 0}
                    <ArmorStatus itemStack={target.armorItems[2]} />
                {/if}
                {#if target.armorItems[1].count > 0}
                    <ArmorStatus itemStack={target.armorItems[1]} />
                {/if}
                {#if target.armorItems[0].count > 0}
                    <ArmorStatus itemStack={target.armorItems[0]} />
                {/if}
            </div>{/if}
        </div>    
        
        <HealthProgress
            maxHealth={target.maxHealth + target.absorption}
            health={target.actualHealth + target.absorption}
            speed={settings.healthSpeed ?? .2}
        />
    </div>
{/if}

<style lang="scss">

    .targethud {
        background-color: var(--targethud-background-color);
        border-radius: 5px;
        overflow: hidden;
    }
    .style-compact .armor-stats { display: none; }
    .style-compact .main-wrapper { padding: 7px 10px; }
    .style-classic { border: 1px solid var(--accent-color); border-radius: 0; }

    .main-wrapper {
        display: grid;
        grid-template-areas:
            "a b d"
            "a c d";
        column-gap: 10px;
        padding: 10px 15px;
    }

    .name {
        grid-area: b;
        color: var(--targethud-text-color);
        font-weight: 500;
        align-self: flex-end;
    }

    .health-stats {
        grid-area: c;
        display: flex;
        column-gap: 10px;

        .stat {
            .value {
                color: var(--targethud-text-dimmed-color);
                font-size: 14px;
                min-width: 18px;
                display: inline-block;
            }
        }
    }

    .armor-stats {
        grid-area: d;
        display: flex;
        align-items: center;
        column-gap: 10px;
        padding-left: 5px;
    }

    .avatar {
        grid-area: a;
        height: 50px;
        width: 50px;
        position: relative;
        image-rendering: pixelated;
        background-image: url("/img/steve.png");
        background-repeat: no-repeat;
        background-size: cover;
        border-radius: 5px;
        overflow: hidden;

        .avatar-fallback {
            position: absolute;
            top: 0;
            right: 0;
            bottom: 0;
            left: 0;
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--targethud-text-color);
            font-size: 22px;
            font-weight: 700;
        }

        img {
            position: absolute;
            width: 400px;
            height: 400px;
            max-width: none;
            left: -50px;
            top: -50px;
            image-rendering: pixelated;
        }
    }
</style>
