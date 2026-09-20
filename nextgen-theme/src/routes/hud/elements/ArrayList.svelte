<script lang="ts">
    import {onMount, tick} from "svelte";
    import type {Module} from "../../../integration/types";
    import {getModules} from "../../../integration/rest";
    import {listen} from "../../../integration/ws";
    import {getTextWidth} from "../../../integration/text_measurement";
    import {flip} from "svelte/animate";
    import {fly} from "svelte/transition";
    import {convertToSpacedString} from "../../../theme/theme_config";
    import {intToRgba} from "../../../integration/util";

    export let settings: { [name: string]: any };

    let c: any = settings;
    let enabledModules: Module[] = [];
    let animationClock = Date.now();
    $: c = settings;
    $: settings, updateEnabledModules();

    function rgba(value: number | undefined, alphaMultiplier = 1): string {
        const [r, g, b, a] = intToRgba(value ?? 0xffffffff);
        return `rgba(${r}, ${g}, ${b}, ${Math.max(0, Math.min(1, (a / 255) * alphaMultiplier))})`;
    }

    function blend(left: number | undefined, right: number | undefined, amount: number, alphaMultiplier = 1): string {
        const a = intToRgba(left ?? 0xffffffff);
        const b = intToRgba(right ?? 0xffffffff);
        const t = Math.max(0, Math.min(1, amount));
        const v = a.map((n, i) => Math.round(n + (b[i] - n) * t));
        return `rgba(${v[0]}, ${v[1]}, ${v[2]}, ${Math.max(0, Math.min(1, v[3] / 255 * alphaMultiplier))})`;
    }

    function wave(value: number): number {
        const normalized = ((value % 2) + 2) % 2;
        return normalized <= 1 ? normalized : 2 - normalized;
    }

    function gradientColor(colors: number[], position: number, alphaMultiplier: number): string {
        if (colors.length <= 1) return rgba(colors[0], alphaMultiplier);
        const scaled = wave(position) * (colors.length - 1);
        const left = Math.floor(scaled);
        const right = Math.min(colors.length - 1, left + 1);
        return blend(colors[left], colors[right], scaled - left, alphaMultiplier);
    }

    function modeColor(
        mode: string,
        primary: number,
        secondary: number,
        gradient: number[],
        gradientSpeed: number,
        fadeDistance: number,
        index: number,
        total: number,
        background = false,
        alphaMultiplier = 1,
    ): string {
        const ratio = total <= 1 ? 0 : index / (total - 1);
        if (mode === "Custom") return rgba(primary, alphaMultiplier);
        if (mode === "Fade") return blend(primary, secondary, wave(index * fadeDistance / 100), alphaMultiplier);
        if (mode === "Gradient") {
            const axis = index * ((1 / Math.max(1, Math.abs(c.gradientX ?? -1000))) +
                (1 / Math.max(1, Math.abs(c.gradientY ?? -1000))));
            return gradientColor(gradient, animationClock / (5000 / Math.max(.5, gradientSpeed)) + ratio + axis, alphaMultiplier);
        }
        if (mode === "Random") {
            return `hsla(${(index * 67) % 360}, ${(c.randomSaturation ?? .9) * 100}%, ${(c.randomBrightness ?? 1) * 62}%, ${(background ? .62 : 1) * alphaMultiplier})`;
        }
        if (mode === "Rainbow") {
            const axis = index * (Math.abs(c.rainbowX ?? -1000) + Math.abs(c.rainbowY ?? -1000)) / 1000;
            return `hsla(${(animationClock / 35 + index * 24 + axis) % 360}, 90%, 65%, ${(background ? .55 : 1) * alphaMultiplier})`;
        }
        return background
            ? `rgba(8, 12, 20, ${.68 * alphaMultiplier})`
            : "var(--accent-color)";
    }

    function applyCase(value: string, mode: string): string {
        if (mode === "Uppercase") return value.toUpperCase();
        if (mode === "Lowercase") return value.toLowerCase();
        return value;
    }

    function displayName(module: Module): string {
        const name = c.spacedModules || (c.spaceSeparatedNames ?? 0) > 0
            ? convertToSpacedString(module.name)
            : module.name;
        return applyCase(name, c.moduleCase ?? "Normal");
    }

    function displayTag(tag: string): string {
        const value = applyCase(tag, c.tagsCase ?? "Normal");
        const style = c.tagsStyle ?? "Space";
        if (style === "[]") return `[${value}]`;
        if (style === "()") return `(${value})`;
        if (style === "<>") return `<${value}>`;
        if (style === "-" || style === "|") return `${style} ${value}`;
        return value;
    }

    function itemStyle(module: Module, index: number): string {
        const count = Math.max(1, enabledModules.length);
        const inactive = module.active === false && c.inactiveModulesStyle === "Color";
        const alphaFactor = 1 - Math.max(0, Math.min(1, c.alphaBlendRange ?? 0)) *
            (count <= 1 ? 0 : index / (count - 1));
        const textGradient = [c.textGradient1, c.textGradient2, c.textGradient3, c.textGradient4]
            .slice(0, Math.max(1, Math.min(4, c.maxTextGradientColors ?? 4)));
        const rectGradient = [c.rectGradient1, c.rectGradient2, c.rectGradient3, c.rectGradient4]
            .slice(0, Math.max(1, Math.min(4, c.maxRectGradientColors ?? 4)));
        const backgroundGradient = [c.backgroundGradient1, c.backgroundGradient2, c.backgroundGradient3, c.backgroundGradient4]
            .slice(0, Math.max(1, Math.min(4, c.maxBackgroundGradientColors ?? 4)));
        const text = inactive ? "rgba(170,170,170,.72)" : modeColor(
            c.textMode, c.textColor, c.textFade, textGradient, c.textGradientSpeed ?? 1,
            c.textFadeDistance ?? 50, index, count,
        );
        const rect = inactive ? "rgba(130,130,130,.55)" : modeColor(
            c.rectColorMode, c.rectColor, c.rectFade, rectGradient, c.rectGradientSpeed ?? 1,
            c.rectFadeDistance ?? 50, index, count,
        );
        const bg = inactive ? `rgba(20,20,20,${.5 * alphaFactor})` : modeColor(
            c.backgroundMode, c.backgroundColor, c.backgroundFade, backgroundGradient,
            c.backgroundGradientSpeed ?? 1, c.backgroundFadeDistance ?? 50,
            index, count, true, alphaFactor,
        );
        const icon = inactive ? "rgba(160,160,160,.7)" : c.iconColorMode === "Custom" ? rgba(c.iconColor) :
            c.iconColorMode === "Fade" ? blend(
                c.iconColor,
                c.iconFadeColor,
                wave(index * (c.iconFadeDistance ?? 50) / 100),
            ) : "var(--accent-color)";
        const iconShadow = c.iconShadows
            ? `${c.shadowXDistance ?? 0}px ${c.shadowYDistance ?? 0}px 5px ${rgba(c.shadowColor ?? 0x80000000)}`
            : "none";
        return `--item-text:${text};--item-rect:${rect};--item-bg:${bg};--item-icon:${icon};` +
            `--item-icon-shadow:${iconShadow};` +
            `--item-radius:${c.roundedBackGroundRadius ?? 3}px;--rect-radius:${c.roundedRectRadius ?? 2}px;` +
            `--item-gap:${c.space ?? 1}px;--item-font:${c.textHeight ?? 14}px;--item-pad-y:${c.textY ?? 3.25}px;` +
            `font-family:${c.font ?? "Inter"},sans-serif;`;
    }

    async function updateEnabledModules() {
        const modules = await getModules();
        const visible = modules.filter(m => m.enabled && !m.hidden &&
            (c.inactiveModulesStyle !== "Hide" || m.active !== false));
        const withWidths = visible.map(module => ({
            ...module,
            width: getTextWidth(displayName(module) + (module.tag ?? ""), `500 ${c.textHeight ?? 14}px ${c.font ?? "Inter"}`)
        }));
        withWidths.sort((a, b) => c.order === "Ascending" ? a.width - b.width : b.width - a.width);
        enabledModules = withWidths;
        await tick();
    }

    onMount(() => {
        void updateEnabledModules();
        const timer = window.setInterval(() => animationClock = Date.now(), 50);
        return () => window.clearInterval(timer);
    });
    listen("moduleToggle", updateEnabledModules);
    listen("refreshArrayList", updateEnabledModules);
</script>

<div class="arraylist" style="gap: {c.space ?? 1}px">
    {#each enabledModules as module, index (module.name)}
        <div
            class="module rect-{String(c.rectMode ?? 'Right').toLowerCase()}"
            class:text-shadow={c.shadowText}
            class:tag-accent={c.tagsArrayColor}
            style="{itemStyle(module, index)} {c.itemAlignment === 'Left' ? 'margin-right:auto' : 'margin-left:auto'}"
            animate:flip={{duration: Math.max(40, (c.animationSpeed ?? .2) * 1000)}}
            transition:fly={{x: c.animation === "Slide" ? 50 : 15, duration: Math.max(40, (c.animationSpeed ?? .2) * 1000)}}
        >
            {#if c.displayIcons}<span class="module-icon"></span>{/if}
            <span>{displayName(module)}</span>
            {#if module.tag && c.showTags && c.tags}
                <span class="tag">{displayTag(module.tag)}</span>
            {/if}
        </div>
    {/each}
</div>

<style lang="scss">
  .arraylist { display: flex; flex-direction: column; width: max-content; min-width: 50px; }
  .module {
    position: relative;
    display: flex;
    align-items: center;
    gap: 6px;
    width: max-content;
    padding: var(--item-pad-y) 8px;
    overflow: hidden;
    border-radius: var(--item-radius);
    background: var(--item-bg);
    color: var(--item-text);
    font-size: var(--item-font);
    font-weight: 500;
    line-height: 1.15;
    white-space: nowrap;
  }
  .text-shadow { text-shadow: 0 1px 3px rgba(0,0,0,.8); }
  .module-icon { width: 6px; height: 6px; border-radius: 50%; background: var(--item-icon); box-shadow: var(--item-icon-shadow); }
  .tag { color: rgba(255,255,255,.58); }
  .tag-accent .tag { color: var(--item-text); }
  .rect-none { border: 0; }
  .rect-left { border-left: 3px solid var(--item-rect); border-radius: var(--rect-radius); }
  .rect-right { border-right: 3px solid var(--item-rect); border-radius: var(--rect-radius); }
  .rect-outline { border: 1px solid var(--item-rect); border-radius: var(--rect-radius); }
  .rect-top { border-top: 2px solid var(--item-rect); border-radius: var(--rect-radius); }
  .rect-special { border-right: 3px solid var(--item-rect); box-shadow: inset -7px 0 12px -8px var(--item-rect); }
</style>
