<script lang="ts">
    export let width = "261.263px";
    export let height = "98px";
    export let badgeFill = "var(--accent-color)";
    export let badgeTextFill = "var(--text-color)";
    export let badgeGroupClass = "";
</script>

<svg
    class="fdpclient-logo"
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 261.263 98"
    aria-label="FDPClient"
    style={`width: ${width}; height: ${height};`}
>
    <defs>
        <slot name="defs" />
    </defs>

    <rect x="0" y="8" width="82" height="82" rx="22" fill="#ffffff"/>
    <path d="M20 28h46v12H34v10h27v12H34v18H20z" fill="#11131a"/>
    <text x="94" y="58" fill="#ffffff" font-family="Inter, sans-serif" font-size="37" font-weight="800">FDP</text>

    <g class={badgeGroupClass}>
        <rect x="92" y="67" width="158" height="25" rx="8" fill={badgeFill}/>
        <text x="171" y="85" text-anchor="middle" fill={badgeTextFill}
              font-family="Inter, sans-serif" font-size="14" font-weight="800" letter-spacing="4">CLIENT</text>
    </g>
</svg>

<style>
    .fdpclient-logo {
        display: block;
        flex: 0 0 auto;
        overflow: visible;
    }
</style>
