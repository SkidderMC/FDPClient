import { existsSync, readFileSync, readdirSync } from "node:fs";
import { dirname, join, relative, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const publicRoot = join(root, "public");
const componentsRoot = join(publicRoot, "components");
const hudSource = readFileSync(join(root, "src/routes/hud/Hud.svelte"), "utf8");
const readJson = path => JSON.parse(readFileSync(path, "utf8").replace(/^\uFEFF/, ""));
const metadata = readJson(join(publicRoot, "metadata.json"));
const errors = [];

const allowedTypes = new Set([
    "BOOLEAN", "COLOR", "TEXT", "CHOOSE", "INT", "FLOAT", "DOUBLE", "LONG",
    "MULTI_CHOOSE", "REGISTRY_LIST", "CONFIGURABLE", "TOGGLEABLE"
]);
const normalize = value => value.replace(/[^a-z0-9]/gi, "").toLowerCase();
const fail = message => errors.push(message);

if (!Array.isArray(metadata.components) || metadata.components.length !== 19) {
    fail(`metadata must declare exactly 19 HUD components (found ${metadata.components?.length ?? 0})`);
}

const declared = new Set(metadata.components);
if (declared.size !== metadata.components.length) fail("metadata contains duplicate component names");

const files = readdirSync(componentsRoot).filter(name => name.endsWith(".json")).sort();
const expectedFiles = [...declared].map(name => `${name.toLowerCase()}.json`).sort();
if (JSON.stringify(files) !== JSON.stringify(expectedFiles)) {
    fail("component JSON files do not exactly match metadata");
}

const rendered = new Set([...hudSource.matchAll(/c\.name\s*===\s*"([^"]+)"/g)].map(match => match[1]));
for (const name of declared) if (!rendered.has(name)) fail(`${name}: missing renderer branch in Hud.svelte`);
for (const name of rendered) if (!declared.has(name)) fail(`${name}: renderer is not declared in metadata`);

for (const file of files) {
    const definition = readJson(join(componentsRoot, file));
    const label = definition.name || file;
    if (!declared.has(definition.name)) fail(`${file}: invalid or undeclared component name`);
    if (typeof definition.description !== "string" || !definition.description.trim()) {
        fail(`${label}: missing description`);
    }
    const alignment = definition.alignment;
    if (!alignment || !["Left", "Center", "CenterTranslated", "Right"].includes(alignment.horizontalAlignment)) {
        fail(`${label}: invalid horizontal alignment`);
    }
    if (!alignment || !["Top", "Center", "CenterTranslated", "Bottom"].includes(alignment.verticalAlignment)) {
        fail(`${label}: invalid vertical alignment`);
    }
    const seen = new Set();
    for (const setting of definition.values ?? []) {
        const settingName = normalize(setting.name ?? "");
        if (!settingName || seen.has(settingName)) fail(`${label}: duplicate/invalid setting '${setting.name}'`);
        seen.add(settingName);
        if (!allowedTypes.has(setting.type)) fail(`${label}/${setting.name}: unsupported type ${setting.type}`);
        if (setting.type === "CHOOSE" && (!setting.choices?.includes(setting.value))) {
            fail(`${label}/${setting.name}: selected choice is not available`);
        }
        if (["INT", "FLOAT", "DOUBLE", "LONG"].includes(setting.type)) {
            if (!Number.isFinite(setting.value)) fail(`${label}/${setting.name}: value is not finite`);
            if (setting.range && (setting.value < setting.range.min || setting.value > setting.range.max)) {
                fail(`${label}/${setting.name}: value is outside its range`);
            }
        }
        if (setting.type === "COLOR" &&
            (!Number.isInteger(setting.value) || setting.value < 0 || setting.value > 0xffffffff)) {
            fail(`${label}/${setting.name}: color is not an unsigned ARGB integer`);
        }
    }
}

const sourceRoot = join(root, "src/routes/hud");
const sourceFiles = [];
const visit = directory => {
    for (const name of readdirSync(directory, { withFileTypes: true })) {
        const path = join(directory, name.name);
        if (name.isDirectory()) visit(path);
        else if (name.name.endsWith(".svelte")) sourceFiles.push(path);
    }
};
visit(sourceRoot);

for (const path of sourceFiles) {
    const source = readFileSync(path, "utf8");
    if (/\bbackdrop-filter\s*:/.test(source)) fail(`${relative(root, path)}: backdrop-filter breaks transparent MCEF overlays`);
    if (/(?:^|[;{\s])scale\s*:/.test(source)) fail(`${relative(root, path)}: individual CSS scale is unsupported by Chromium 75`);
    for (const match of source.matchAll(/["'(]\/??(img\/[A-Za-z0-9_./-]+\.(?:svg|png|jpg))/g)) {
        if (!existsSync(join(publicRoot, match[1]))) fail(`${relative(root, path)}: missing asset ${match[1]}`);
    }
}

const appSource = readFileSync(join(root, "src/App.svelte"), "utf8");
if (!appSource.includes("dedicatedHudOverlay")) fail("App.svelte does not lock the dedicated HUD route");
const indexSource = readFileSync(join(root, "index.html"), "utf8");
if (!/background:\s*transparent/.test(indexSource)) fail("index.html does not force a transparent browser surface");

if (errors.length) {
    console.error(`HUD validation failed with ${errors.length} problem(s):`);
    for (const error of errors) console.error(` - ${error}`);
    process.exit(1);
}

console.log(`HUD validation passed: ${declared.size} components, ${files.length} definitions, ${sourceFiles.length} render files.`);
