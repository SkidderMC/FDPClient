import {defineConfig} from 'vite'
import {svelte} from '@sveltejs/vite-plugin-svelte'
import legacy from '@vitejs/plugin-legacy'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        svelte(),
        legacy({
            targets: ["chrome >= 75"],
            modernTargets: ["chrome >= 75"],
            renderLegacyChunks: false,
            modernPolyfills: true
        })
    ],
    base: "",
    build: {
        target: "chrome75",
        rollupOptions: {
            output: {
                manualChunks(id) {
                    if (id.includes("chart.js") || id.includes("chartjs-plugin-dragdata")) {
                        return "charts";
                    }
                    if (id.includes("@simonwep/pickr") || id.includes("nouislider")) {
                        return "controls";
                    }
                    if (id.includes("node_modules/svelte")) {
                        return "svelte";
                    }
                }
            }
        }
    },
    css: {
        preprocessorOptions: {
            scss: {
                api: "modern"
            }
        }
    }
})
