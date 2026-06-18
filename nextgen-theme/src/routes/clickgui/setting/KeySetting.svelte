<script lang="ts">
    import type {KeySetting, ModuleSetting} from "../../../integration/types";
    import {convertToSpacedString, spaceSeperatedNames} from "../../../theme/theme_config";
    import {getPrintableKeyName} from "../../../integration/rest";
    import {createEventDispatcher} from "svelte";
    import {UNKNOWN_KEY} from "../../../util/utils";

    export let setting: ModuleSetting;

    const cSetting = setting as KeySetting;

    const dispatch = createEventDispatcher();

    const KEY_MAP: Record<string, string> = {
        "Esc": "key.keyboard.escape",
        "Escape": "key.keyboard.escape",
        "Enter": "key.keyboard.enter",
        "Return": "key.keyboard.enter",
        "Backspace": "key.keyboard.backspace",
        "Tab": "key.keyboard.tab",
        " ": "key.keyboard.space",
        "Space": "key.keyboard.space",
        "Spacebar": "key.keyboard.space",
        "Shift": "key.keyboard.left.shift",
        "ShiftLeft": "key.keyboard.left.shift",
        "ShiftRight": "key.keyboard.right.shift",
        "Control": "key.keyboard.left.control",
        "ControlLeft": "key.keyboard.left.control",
        "ControlRight": "key.keyboard.right.control",
        "Alt": "key.keyboard.left.alt",
        "AltLeft": "key.keyboard.left.alt",
        "AltRight": "key.keyboard.right.alt",
        "ArrowLeft": "key.keyboard.left",
        "Left": "key.keyboard.left",
        "ArrowRight": "key.keyboard.right",
        "Right": "key.keyboard.right",
        "ArrowUp": "key.keyboard.up",
        "Up": "key.keyboard.up",
        "ArrowDown": "key.keyboard.down",
        "Down": "key.keyboard.down",
        "Delete": "key.keyboard.delete",
        "Del": "key.keyboard.delete",
        "Insert": "key.keyboard.insert",
        "Home": "key.keyboard.home",
        "End": "key.keyboard.end",
        "PageUp": "key.keyboard.page.up",
        "PageDown": "key.keyboard.page.down",
        "CapsLock": "key.keyboard.caps.lock",
        "NumLock": "key.keyboard.num.lock"
    };

    let isHovered = false;
    let binding = false;
    let printableKeyName = "";

    $: {
        if (cSetting.value !== UNKNOWN_KEY) {
            getPrintableKeyName(cSetting.value)
                .then(printableKey => {
                    printableKeyName = printableKey.localized;
                });
        }
    }

    async function toggleBinding() {
        if (binding) {
            cSetting.value = UNKNOWN_KEY;
        }

        binding = !binding;

        setting = {...cSetting};

        dispatch("change");
    }

    function handleBrowserKeyDown(e: KeyboardEvent) {
        if (!binding) {
            return;
        }

        e.preventDefault();
        e.stopPropagation();

        binding = false;

        if (e.key === "Escape" || e.key === "Esc") {
            cSetting.value = UNKNOWN_KEY;
        } else {
            cSetting.value = browserKeyToMinecraftKey(e);
        }

        setting = {...cSetting};

        dispatch("change");
    }

    function handleBrowserMouseDown(e: MouseEvent) {
        if (!binding || (e.button === 0 && isHovered)) {
            return;
        }

        e.preventDefault();
        e.stopPropagation();
        binding = false;
        cSetting.value = UNKNOWN_KEY;

        setting = {...cSetting};
        dispatch("change");
    }

    function browserKeyToMinecraftKey(e: KeyboardEvent): string {
        const key = e.key || "";
        const code = e.code || "";
        const legacyIdentifier = (e as any).keyIdentifier || "";

        const direct = KEY_MAP[key] || KEY_MAP[code] || KEY_MAP[legacyIdentifier];
        if (direct) {
            return direct;
        }

        if (/^F\d{1,2}$/i.test(key)) {
            return `key.keyboard.${key.toLowerCase()}`;
        }

        if (/^F\d{1,2}$/i.test(code)) {
            return `key.keyboard.${code.toLowerCase()}`;
        }

        if (/^Key[A-Z]$/.test(code)) {
            return `key.keyboard.${code.slice(3).toLowerCase()}`;
        }

        if (/^Digit[0-9]$/.test(code)) {
            return `key.keyboard.${code.slice(5)}`;
        }

        if (/^Numpad[0-9]$/.test(code)) {
            return `key.keyboard.${code.slice(6)}`;
        }

        if (key.length === 1 && /^[a-z0-9]$/i.test(key)) {
            return `key.keyboard.${key.toLowerCase()}`;
        }

        if (/^U\+[0-9A-F]{4}$/i.test(legacyIdentifier)) {
            const char = String.fromCharCode(parseInt(legacyIdentifier.slice(2), 16));
            if (/^[a-z0-9]$/i.test(char)) {
                return `key.keyboard.${char.toLowerCase()}`;
            }
        }

        return UNKNOWN_KEY;
    }
</script>

<svelte:window on:keydown={handleBrowserKeyDown} on:mousedown={handleBrowserMouseDown}/>

<div class="setting">
    <button
            class="change-bind"
            on:click={toggleBinding}
            on:mouseenter={() => isHovered = true}
            on:mouseleave={() => isHovered = false}
    >
        {#if !binding}
            <div class="name">{$spaceSeperatedNames ? convertToSpacedString(cSetting.name) : cSetting.name}:</div>

            {#if cSetting.value === UNKNOWN_KEY}
                <span class="none">None</span>
            {:else}
                <span>{printableKeyName}</span>
            {/if}
        {:else}
            <span>Press any key</span>
        {/if}
    </button>
</div>

<style lang="scss">

  .setting {
    padding: 7px 0;
  }

  .change-bind {
    background-color: transparent;
    border: solid 2px var(--accent-color);
    border-radius: 3px;
    cursor: pointer;
    padding: 4px;
    font-weight: 500;
    color: var(--clickgui-text-color);
    font-size: 12px;
    font-family: "Inter", sans-serif;
    width: 100%;
    display: flex;
    justify-content: center;
    column-gap: 5px;

    .name {
      font-weight: 500;
    }

    .none {
      color: var(--clickgui-text-dimmed-color);
    }
  }
</style>
