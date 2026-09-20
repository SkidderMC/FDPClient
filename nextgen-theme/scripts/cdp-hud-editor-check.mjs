const socketUrl = process.argv[2];
if (!socketUrl) throw new Error("Missing Chrome DevTools websocket URL");

const socket = new WebSocket(socketUrl);
const pending = new Map();
let nextId = 1;

socket.addEventListener("message", event => {
    const response = JSON.parse(event.data);
    const callback = pending.get(response.id);
    if (callback) {
        pending.delete(response.id);
        callback(response);
    }
});

await new Promise((resolve, reject) => {
    socket.addEventListener("open", resolve, {once: true});
    socket.addEventListener("error", reject, {once: true});
});

function command(method, params = {}) {
    const id = nextId++;
    return new Promise(resolve => {
        pending.set(id, resolve);
        socket.send(JSON.stringify({id, method, params}));
    });
}

await command("Runtime.enable");
await command("Runtime.evaluate", {
    expression: `(() => {
        const component = document.querySelector('[data-component-name="ArrayList"]');
        const target = component?.querySelector('.contained-element');
        if (!target) return false;
        const bounds = target.getBoundingClientRect();
        const options = {bubbles: true, cancelable: true, button: 0,
            clientX: bounds.left + Math.min(10, bounds.width / 2),
            clientY: bounds.top + Math.min(10, bounds.height / 2)};
        target.dispatchEvent(new MouseEvent('mousedown', options));
        window.dispatchEvent(new MouseEvent('mouseup', options));
        return true;
    })()`,
    returnByValue: true,
});

await new Promise(resolve => setTimeout(resolve, 750));
const response = await command("Runtime.evaluate", {
    expression: `(() => {
        const component = document.querySelector('[data-component-name="ArrayList"]');
        const panel = component?.querySelector('.settings');
        const bounds = panel?.getBoundingClientRect();
        return {
            selected: component?.querySelector('.contained-element.selected') !== null,
            visibleSettingsPanels: document.querySelectorAll('.settings').length,
            clientHeight: panel?.clientHeight ?? 0,
            scrollHeight: panel?.scrollHeight ?? 0,
            hasVerticalScroll: !!panel && panel.scrollHeight > panel.clientHeight,
            insideViewport: !!bounds && bounds.top >= 0 && bounds.bottom <= window.innerHeight &&
                bounds.left >= 0 && bounds.right <= window.innerWidth,
            viewport: {width: window.innerWidth, height: window.innerHeight},
        };
    })()`,
    returnByValue: true,
});

const result = response.result?.result?.value;
socket.close();
if (!result?.selected || result.visibleSettingsPanels !== 1 || !result.hasVerticalScroll || !result.insideViewport) {
    console.error(JSON.stringify(result, null, 2));
    process.exit(1);
}
console.log(JSON.stringify(result));
