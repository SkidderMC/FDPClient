<script lang="ts">
    import {createEventDispatcher} from "svelte";
    import type {ButtonSetting, ModuleSetting} from "../../../integration/types";
    import {runClientAction} from "../../../integration/rest";
    import {convertToSpacedString, spaceSeperatedNames} from "../../../theme/theme_config";
    import SettingButton from "./common/SettingButton.svelte";

    export let setting: ModuleSetting;

    const cSetting = setting as ButtonSetting;
    const dispatch = createEventDispatcher();

    async function handleClick() {
        await runClientAction(cSetting.action);
        dispatch("change");
    }
</script>

<div class="setting">
    <SettingButton
        value={$spaceSeperatedNames ? convertToSpacedString(cSetting.value) : cSetting.value}
        on:click={handleClick}
    />
</div>

<style lang="scss">
    .setting {
        padding: 7px 0;
    }
</style>
