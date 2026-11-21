// Checks index.d.ts by using the library as the site will. TypeScript compiles this file and never
// runs it: the correct usage must type-check, and each line under @ts-expect-error must be rejected.
import { plasma, render, tables, type Art, type Pixels, type Settings } from "@schrodlm/ascii-art";

declare const imageData: ImageData;

// Correct usage, which must be accepted.

const names: readonly string[] = tables;
const fromCanvas: Pixels = imageData;
const fromPlasma: Pixels = plasma(7);
const plasmaImage = new ImageData(plasma().data, 640, 480);
const noSettings: Settings = {};

const art: Art = render(imageData, {
  maxColumns: 120,
  maxRows: 40,
  cellAspect: 0.5,
  table: "bourke",
  rgbFilters: [{ type: "rotate", degrees: 90 }],
  greyFilters: [{ type: "brightness", amount: 20 }, { type: "contrast", factor: 1.5 }, { type: "invert" }],
  asciiFilters: [{ type: "flip", direction: "horizontal" }, { type: "scale", factor: 2 }],
});
const lines: string[] = art.lines;
const text: string = art.text;
const cells: number = art.columns * art.rows;

render(plasma());
render({ data: new Uint8Array(4), width: 1, height: 1 }, { table: { chars: " .:-=+*#%@" } });
render(imageData, { table: { limits: [[127, "#"], [255, "."]] } });

// Mistakes, which must be rejected.

// @ts-expect-error a misspelt setting
render(imageData, { maxColumn: 120 });
// @ts-expect-error a number given as text
render(imageData, { maxColumns: "120" });
// @ts-expect-error a flip among the grey filters
render(imageData, { greyFilters: [{ type: "flip", direction: "vertical" }] });
// @ts-expect-error brightness without its amount
render(imageData, { greyFilters: [{ type: "brightness" }] });
// @ts-expect-error a misspelt filter type
render(imageData, { greyFilters: [{ type: "brightnes", amount: 20 }] });
// @ts-expect-error an unknown flip direction
render(imageData, { asciiFilters: [{ type: "flip", direction: "up" }] });
// @ts-expect-error a limit that is not a [grey, character] pair
render(imageData, { table: { limits: [[255]] } });
// @ts-expect-error pixels without data
render({ width: 1, height: 1 });
// @ts-expect-error a plain array of numbers as data
render({ data: [0, 0, 0, 255], width: 1, height: 1 });
// @ts-expect-error the art's text cannot be replaced
art.text = "";
// @ts-expect-error the list of tables cannot be changed
tables.push("mine");
// @ts-expect-error plasma's seed is a number
plasma("7");
