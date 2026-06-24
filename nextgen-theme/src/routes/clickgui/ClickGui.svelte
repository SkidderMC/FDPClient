<script lang="ts">
    import type {GroupedModules, Module} from "../../integration/types";
    import Panel from "./Panel.svelte";
    import Search from "./Search.svelte";
    import Description from "./Description.svelte";
    import {fade} from "svelte/transition";
    import {onMount} from "svelte";
    import {getModules} from "../../integration/rest";
    import {groupByCategory} from "../../integration/util";
    import {listen} from "../../integration/ws";

    let categories = $state<GroupedModules>({});
    let modules = $state<Module[]>([]);

    async function refreshModules() {
        modules = await getModules();
        categories = groupByCategory(modules);
    }

    onMount(refreshModules);
    listen("configurationChanged", refreshModules);
    listen("socketReady", refreshModules);

    function cloneModules(value: Module[]): Module[] {
        return JSON.parse(JSON.stringify(value));
    }
</script>

<div class="clickgui" transition:fade|global={{ duration: 200 }}>
    <Description/>
    <Search modules={cloneModules($state.snapshot(modules))}/>

    {#each Object.entries(categories) as [category, modules], panelIndex (category)}
        <Panel {category} {modules} {panelIndex}/>
    {/each}
</div>
