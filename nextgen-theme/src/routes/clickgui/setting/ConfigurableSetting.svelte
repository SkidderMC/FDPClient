<script lang="ts">
    import {createEventDispatcher} from "svelte";
    import type {ConfigurableSetting, ModuleSetting,} from "../../../integration/types";
    import GenericSetting from "./common/GenericSetting.svelte";
    import ExpandArrow from "./common/ExpandArrow.svelte";
    import {setItem} from "../../../integration/persistent_storage";
    import {convertToSpacedString, spaceSeperatedNames} from "../../../theme/theme_config";

    export let setting: ModuleSetting;
    export let path: string;
    export let hideExpandControl: boolean = false;

    const cSetting = setting as ConfigurableSetting;
    const thisPath = `${path}.${cSetting.name}`;

    const dispatch = createEventDispatcher();

    function handleChange() {
        setting = {...cSetting};
        dispatch("change");
    }

    let expanded = hideExpandControl ? true : localStorage.getItem(thisPath) === "true";

    $: setItem(thisPath, expanded.toString());

    function toggleExpanded() {
        if (hideExpandControl) {
            return;
        }
        expanded = !expanded;
    }
</script>

<div class="setting">
    <!-- svelte-ignore a11y-no-static-element-interactions -->
    <div class="head" class:expanded on:contextmenu|preventDefault={toggleExpanded}>
        <div class="title">{$spaceSeperatedNames ? convertToSpacedString(setting.name) : setting.name}</div>
        {#if !hideExpandControl}
            <div class="arrow"><ExpandArrow bind:expanded /></div>
        {/if}
    </div>

    {#if expanded}
        <div class="nested-settings">
            {#each cSetting.value as setting (setting.name)}
                <GenericSetting path={thisPath} bind:setting on:change={handleChange}/>
            {/each}
        </div>
    {/if}
</div>

<style lang="scss">

  .setting {
    padding: 7px 0;
  }

  .title {
    color: var(--clickgui-text-color);
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.2px;
    text-align: center;
  }

  .head {
    display: flex;
    justify-content: center;
    align-items: center;
    position: relative;
    padding: 6px 9px;
    background-color: var(--clickgui-panel-header-background-color);
    border-left: solid 2px var(--accent-color);
    border-radius: 3px;
    cursor: pointer;
    transition: ease margin-bottom .2s;

    &.expanded {
      margin-bottom: 8px;
    }

    .arrow {
      position: absolute;
      right: 9px;
      top: 50%;
      transform: translateY(-50%);
    }
  }

  .nested-settings {
    border-left: solid 2px var(--clickgui-setting-group-border-color);
    padding-left: 7px;
  }
</style>
