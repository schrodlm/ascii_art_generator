// Tests the npm package as someone who installs it gets it: imported by its name, through the
// exports in package.json, from the minified bundle that sbt js/npmPackage puts next to it.
import { test } from "node:test";
import assert from "node:assert/strict";
import { execFileSync } from "node:child_process";
import { readFileSync } from "node:fs";

const library = await import("clascii");
const packageDir = new URL("..", import.meta.url);

test("exports exactly what index.d.ts declares", () => {
  const types = readFileSync(new URL("index.d.ts", packageDir), "utf8");
  const declared = new Set([...types.matchAll(/^export declare (?:const|function) (\w+)/gm)].map((match) => match[1]));

  assert.deepEqual(Object.keys(library).sort(), [...declared].sort());
});

test("draws art", () => {
  const art = library.render(library.plasma(1), { maxColumns: 40, characters: { braille: "ordered" } });

  assert.equal(art.columns, 40);
  assert.equal(art.lines.length, art.rows);
});

test("packs the licence, the types, the bundle and package.json, nothing else", () => {
  const [pack] = JSON.parse(execFileSync("npm", ["pack", "--dry-run", "--json"], { cwd: packageDir, encoding: "utf8" }));

  assert.deepEqual(pack.files.map((file) => file.path).sort(), ["LICENSE", "index.d.ts", "main.mjs", "package.json"]);
});
