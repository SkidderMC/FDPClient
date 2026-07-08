import "./app.scss";
import App from "./App.svelte";
import {mount} from "svelte";

window.addEventListener("contextmenu", event => {
    event.preventDefault();
}, {capture: true});

const target = document.getElementById("app");
if (!target) {
    throw new Error("Could not find app mount target");
}

const app = mount(App, {
    target
});

export default app;
