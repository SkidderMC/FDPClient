import {hasEventSocket, isStatic, WS_BASE} from "./host";
import type {EventMap} from "./events";
import {onDestroy} from "svelte";

let ws: WebSocket | undefined;
let reconnectAttempts = 0;
let reconnectTimer: ReturnType<typeof setTimeout> | undefined;

function connect() {
    if (ws?.readyState === WebSocket.OPEN || ws?.readyState === WebSocket.CONNECTING) return;
    ws = new WebSocket(WS_BASE);

    ws.onopen = () => {
        console.log("[WS] Connected to server");
        reconnectAttempts = 0;
        // Mirror onmessage and notify both maps, so component-scoped socketReady handlers
        // (registered via listen) also refresh on every (re)connect, not just persistent ones.
        alwaysListeners.get("socketReady")?.forEach(callback => callback());
        listeners.get("socketReady")?.forEach(callback => callback());
    };

    ws.onclose = () => {
        reconnectAttempts++;
        const delay = Math.min(30_000, 1_000 * (2 ** Math.min(reconnectAttempts - 1, 5)));
        console.log(`[WS] Disconnected; reconnecting in ${delay}ms`);
        if (reconnectTimer) clearTimeout(reconnectTimer);
        reconnectTimer = setTimeout(connect, delay);
    };

    ws.onerror = (error) => {
        console.error("[WS] WebSocket error: ", error)
    };

    ws.onmessage = (event) => {
        try {
            const json = JSON.parse(event.data);
            const eventName = json.name as keyof EventMap;
            const eventData = json.event;

            alwaysListeners.get(eventName)?.forEach(callback => callback(eventData));
            listeners.get(eventName)?.forEach(callback => callback(eventData));
        } catch (error) {
            console.error("[WS] Invalid event payload", error);
        }
    }
}

const alwaysListeners = new Map<keyof EventMap, Function[]>();
const listeners = new Map<keyof EventMap, Function[]>();

export function listenAlways<NAME extends keyof EventMap>(eventName: NAME, callback: (event: EventMap[NAME]) => void) {
    if (!alwaysListeners.has(eventName)) {
        alwaysListeners.set(eventName, []);
    }

    alwaysListeners.get(eventName)!!.push(callback);
}

/**
 * Registers a event listener that will be called on every event of the given name.
 *
 * @param eventName
 * @param callback
 * @return A function that can be called to remove the listener.
 */
function listenNonComponent<NAME extends keyof EventMap>(eventName: NAME, callback: (event: EventMap[NAME]) => void) {
    if (!listeners.has(eventName)) {
        listeners.set(eventName, []);
    }

    listeners.get(eventName)!!.push(callback);

    return () => deleteListener(eventName, callback);
}

/**
 * Registers a event listener that will be called on every event of the given name.
 *
 * The listener will be automatically removed when the component is destroyed.
 *
 * Should only be used inside a Svelte component.
 *
 * @param eventName
 * @param callback
 */
export function listen<NAME extends keyof EventMap>(eventName: NAME, callback: (event: EventMap[NAME]) => void) {
    const onDestroyHook = listenNonComponent(eventName, callback);
    onDestroy(onDestroyHook);
}

/**
 * Wait next event which matches given {@link predicate}.
 */
export async function waitMatches<NAME extends keyof EventMap>(eventName: NAME, predicate: (event: EventMap[NAME]) => boolean): Promise<EventMap[NAME]> {
    return new Promise((resolve, reject) => {
        const deleteHandler = listenNonComponent(eventName, (e) => {
            try {
                if (predicate(e)) {
                    resolve(e);
                    deleteHandler();
                }
            } catch (e) {
                reject(e);
                deleteHandler();
            }
        })
    });
}

export function cleanupListeners() {
    listeners.clear();
    console.log("[WS] Cleaned up event listeners");
}

export function deleteListener<NAME extends keyof EventMap>(eventName: NAME, cb: (event: EventMap[NAME]) => void) {
    listeners.set(
        eventName,
        listeners.get(eventName)?.filter(handler => handler !== cb) ?? []
    );
}

const PING_PAYLOAD = JSON.stringify({name: "ping", event: {}});

// Send ping to server every 5 seconds
setInterval(() => {
    if (!ws) return;
    if (ws.readyState !== 1) return;

    ws.send(PING_PAYLOAD);
}, 5000);

if (!isStatic || hasEventSocket) {
    console.log("Connecting to server at: ", WS_BASE);
    connect();
}
