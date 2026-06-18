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
        target: "chrome75"
    },
    css: {
        preprocessorOptions: {
            scss: {
                api: "modern"
            }
        }
    }
})
