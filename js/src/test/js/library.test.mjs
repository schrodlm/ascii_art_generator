// Tests the built library the way JavaScript uses it: by importing the ES module.
// ASCII_ART_JS picks which build to test; without it, the development build.
import { describe, test } from "node:test";
import assert from "node:assert/strict";

const library = process.env.ASCII_ART_JS ?? new URL("../../../target/scala-2.13/js-fastopt/main.mjs", import.meta.url);
const { tables, render, plasma } = await import(library);

// A picture in the browser's layout; colour(x, y) gives [red, green, blue, alpha].
function picture(width, height, colour) {
  const data = new Uint8ClampedArray(width * height * 4);
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) data.set(colour(x, y), (y * width + x) * 4);
  }
  return { data, width, height };
}

const grey = (value) => [value, value, value, 255];

// Four greys from black to white. With the default table "@%#*+=-:. " they are "@*- ".
const ramp = picture(4, 1, (x) => grey([0, 85, 170, 255][x]));

// Square cells, so one pixel becomes one character and the ramp stays one row.
const square = { cellAspect: 1 };

describe("tables", () => {
  test("lists the built-in tables", () => {
    assert.deepEqual(tables, ["default", "nonlinear-default", "bourke"]);
  });

  test("cannot be changed", () => {
    assert.throws(() => tables.push("mine"), TypeError);
  });
});

describe("render", () => {
  test("turns a known picture into known characters", () => {
    const art = render(ramp, square);

    assert.equal(art.text, "@*- ");
    assert.deepEqual(art.lines, ["@*- "]);
    assert.equal(art.columns, 4);
    assert.equal(art.rows, 1);
  });

  test("returns a plain object whose text is its lines joined", () => {
    const art = render(picture(3, 3, () => grey(0)), square);

    assert.deepEqual(Object.keys(art).sort(), ["columns", "lines", "rows", "text"]);
    assert.equal(art.text, art.lines.join("\n"));
  });

  test("renders the seed 7 plasma exactly as core does on the JVM", () => {
    // The same text as core's GoldenRenderTest, so the library and core agree.
    assert.equal(render(plasma(7), { maxColumns: 40 }).text, [
      ":-=+****+=-:....:-+*##%%%#**+===++***###",
      "=+*****+=-::::-=+*#%@@%%#*+=====++*****+",
      "*###**+=-::--=*#%@@@%%#*+=----==++++++=-",
      "%%##*+=---=+*#%%@@%#*+=-::::--=++++==--:",
      "%#*+=----=+*#%%%%#*=-:....:--=++++==---:",
      "#+=-----=+*####*+=-:.  ..:-=+++++==-----",
      "=-::::-=+*##**+=:..  ..:-++*****+======+",
      "...::-=+****+=-..  .:-=+*####**++===++**",
      " .:-=+****+=-:....:-+*#%%%%##*+====++**#",
      ".:=+**#**+=-::::-=+*%%@@%%#*+======++***",
      "=+*####*+=-----=*#%%@@%%#*+=-----=++++++",
      "#%%%%#*+=---==+*#%@@%#*+=-:::::-==++++==",
      "@@%%#+=----=+*#%%%%#*=-:....::-=+++++=--",
      "@%#*=--:--=+*####*+=-:.  ..:-=++**++==--",
      "#+=-::::-=+*****+=:..  ..:-+**##**++====",
    ].join("\n"));
  });

  test("reads pixels whose fields are getters, as on a canvas's ImageData", () => {
    const { data } = ramp;
    const imageData = Object.create({ get data() { return data; }, get width() { return 4; }, get height() { return 1; } });

    assert.equal(render(imageData, square).text, "@*- ");
  });

  test("reads a Uint8Array that is a view into a larger buffer", () => {
    const buffer = new Uint8Array(ramp.data.length + 8);
    buffer.set(ramp.data, 8);

    assert.equal(render({ data: buffer.subarray(8), width: 4, height: 1 }, square).text, "@*- ");
  });

  test("fits the art inside maxColumns and maxRows, whichever is tighter", () => {
    const wide = render(plasma(7), { maxColumns: 40 });
    const short = render(plasma(7), { maxColumns: 40, maxRows: 10 });

    assert.equal(wide.columns, 40);
    assert.equal(short.rows, 10);
    assert.ok(short.columns < 40);
    for (const art of [wide, short]) {
      assert.equal(art.lines.length, art.rows);
      for (const line of art.lines) assert.equal(line.length, art.columns);
    }
  });

  test("makes the art shorter for narrower character cells", () => {
    const block = picture(4, 4, () => grey(0));

    assert.equal(render(block, { cellAspect: 1 }).rows, 4);
    assert.equal(render(block, { cellAspect: 0.5 }).rows, 2);
  });

  test("uses a built-in table chosen by name", () => {
    assert.equal(render(ramp, { ...square, table: "bourke" }).text, "$01.");
  });

  test("uses your own characters, darkest first", () => {
    assert.equal(render(ramp, { ...square, table: { chars: "ab" } }).text, "aabb");
  });

  test("uses your own grey limits", () => {
    assert.equal(render(ramp, { ...square, table: { limits: [[100, "x"], [255, "o"]] } }).text, "xxoo");
  });
});

describe("render's filters", () => {
  test("rotate turns the picture clockwise before it is shrunk", () => {
    const art = render(ramp, { ...square, rgbFilters: [{ type: "rotate", degrees: 90 }] });

    assert.deepEqual(art.lines, ["@", "*", "-", " "]);
  });

  test("brightness, contrast and invert change the greys", () => {
    const brighter = render(ramp, { ...square, greyFilters: [{ type: "brightness", amount: 85 }] });
    const flat = render(ramp, { ...square, greyFilters: [{ type: "contrast", factor: 0 }] });
    const inverted = render(ramp, { ...square, greyFilters: [{ type: "invert" }] });

    assert.equal(brighter.text, "*-  ");
    assert.equal(flat.text, "====");
    assert.equal(inverted.text, " -*@");
  });

  test("grey filters run in the order given", () => {
    const brightenFirst = [{ type: "brightness", amount: 100 }, { type: "invert" }];
    const invertFirst = [{ type: "invert" }, { type: "brightness", amount: 100 }];

    // Black: 0 + 100 = 100, inverted 155, is "-"; but 0 inverted is 255, plus 100 stays 255, " ".
    assert.equal(render(ramp, { ...square, greyFilters: brightenFirst }).text[0], "-");
    assert.equal(render(ramp, { ...square, greyFilters: invertFirst }).text[0], " ");
  });

  test("flip mirrors the picture and scale resizes the finished characters", () => {
    const mirrored = render(ramp, { ...square, rgbFilters: [{ type: "flip", direction: "horizontal" }] });
    const upsideDown = render(ramp, {
      ...square,
      rgbFilters: [{ type: "rotate", degrees: 90 }, { type: "flip", direction: "vertical" }],
    });
    const doubled = render(ramp, { ...square, asciiFilters: [{ type: "scale", factor: 2 }] });

    assert.equal(mirrored.text, " -*@");
    assert.deepEqual(upsideDown.lines, [" ", "-", "*", "@"]);
    assert.deepEqual(doubled.lines, ["@@**--  ", "@@**--  "]);
  });
});

describe("render's colours", () => {
  const red = [255, 0, 0, 255], green = [0, 255, 0, 255], blue = [0, 0, 255, 255], white = grey(255);
  // One pixel each, so each becomes one character.
  const colourful = picture(4, 1, (x) => [red, green, blue, white][x]);

  test("are left out unless asked for", () => {
    assert.equal("colors" in render(colourful, square), false);
    assert.equal("colors" in render(colourful, { ...square, colors: false }), false);
  });

  test("give each character the colour of its pixels, laid out like the pixels", () => {
    // deepEqual compares the kind of array too, so this also checks it is a Uint8ClampedArray.
    assert.deepEqual(render(colourful, { ...square, colors: true }).colors, colourful.data);
  });

  test("average the pixels a character covers", () => {
    const blackAndWhite = picture(2, 1, (x) => grey([0, 255][x]));

    // Cells twice as wide as tall, so both pixels fall into one character.
    assert.deepEqual([...render(blackAndWhite, { maxColumns: 1, cellAspect: 2, colors: true }).colors], [127, 127, 127, 255]);
  });

  test("stay through grey filters and move with flips", () => {
    const art = render(colourful, {
      ...square,
      colors: true,
      rgbFilters: [{ type: "flip", direction: "horizontal" }],
      greyFilters: [{ type: "invert" }],
    });

    assert.deepEqual([...art.colors], [...white, ...blue, ...green, ...red]);
  });
});

describe("render's mistakes", () => {
  const mistakes = [
    ["no pixels", () => render(), "pixels must be an object with data, width and height"],
    ["a fractional width", () => render({ ...ramp, width: 2.5 }), "pixels.width must be a whole number of at least 1"],
    ["a width given as text", () => render({ ...ramp, width: "4" }), "pixels.width must be a whole number of at least 1"],
    ["data that is a plain array", () => render({ ...ramp, data: [...ramp.data] }),
      "pixels.data must be a Uint8ClampedArray or a Uint8Array"],
    ["data of the wrong length", () => render({ ...ramp, height: 2 }),
      "pixels.data must hold width × height × 4 = 32 values, not 16"],
    ["a picture too large for core", () => render({ data: new Uint8ClampedArray(4), width: 50000, height: 50000 }),
      "pixels is too large: at most 2147483647 values"],
    ["settings that are not an object", () => render(ramp, 40), "settings must be an object"],
    ["a misspelt setting", () => render(ramp, { maxColumn: 40 }),
      "Unknown key 'maxColumn' in settings: use asciiFilters, cellAspect, colors, greyFilters, maxColumns, maxRows, rgbFilters, table"],
    ["zero columns", () => render(ramp, { maxColumns: 0 }), "maxColumns must be a whole number of at least 1"],
    ["colors given as text", () => render(ramp, { colors: "yes" }), "colors must be true or false"],
    ["an unknown table", () => render(ramp, { table: "nope" }), "Unknown table 'nope': use default, nonlinear-default, bourke"],
    ["both chars and limits", () => render(ramp, { table: { chars: "ab", limits: [] } }),
      "table must be a built-in table's name, { chars } or { limits }"],
    ["an empty table", () => render(ramp, { table: { chars: "" } }), "A character table needs at least one character"],
    ["emoji in a table", () => render(ramp, { table: { chars: "🌿 ." } }),
      "table.chars cannot hold emoji or other characters beyond U+FFFF"],
    ["a newline in a table", () => render(ramp, { table: { chars: "@\n." } }),
      "table.chars cannot hold control characters such as a newline"],
    ["limits that stop short of white", () => render(ramp, { table: { limits: [[100, "@"], [200, "+"]] } }),
      "The last grey limit must be 255, so every grey has a character"],
    ["a limit that is not a pair", () => render(ramp, { table: { limits: [[255]] } }), "table.limits[0] must be [limit, character]"],
    ["filters that are not a list", () => render(ramp, { greyFilters: { type: "invert" } }), "greyFilters must be an array"],
    ["a filter in the wrong list", () => render(ramp, { greyFilters: [{ type: "flip", direction: "vertical" }] }),
      "greyFilters[0].type must be one of brightness, contrast, invert"],
    ["a filter without its value", () => render(ramp, { greyFilters: [{ type: "brightness" }] }),
      "greyFilters[0].amount must be a whole number"],
    ["a misspelt filter key", () => render(ramp, { asciiFilters: [{ type: "scale", factor: 2, factr: 3 }] }),
      "Unknown key 'factr' in asciiFilters[0]: use factor, type"],
    ["an unknown flip direction", () => render(ramp, { rgbFilters: [{ type: "flip", direction: "up" }] }),
      'rgbFilters[0].direction must be "vertical" or "horizontal"'],
    ["a rotation that is not a quarter turn", () => render(ramp, { rgbFilters: [{ type: "rotate", degrees: 45 }] }),
      "Rotation must be a multiple of 90 degrees"],
  ];

  for (const [mistake, call, message] of mistakes) {
    test(`reports ${mistake} as an Error`, () => {
      assert.throws(call, (error) => error instanceof Error && error.message === message);
    });
  }
});

describe("plasma", () => {
  test("is a 640 by 480 picture in the browser's layout", () => {
    const { data, width, height } = plasma(1);

    assert.equal(width, 640);
    assert.equal(height, 480);
    assert.ok(data instanceof Uint8ClampedArray);
    assert.equal(data.length, 640 * 480 * 4);
    assert.ok(data.every((value, i) => i % 4 !== 3 || value === 255), "every pixel is opaque");
  });

  test("draws the same picture for the same seed, and another for another seed", () => {
    const same = (a, b) => Buffer.from(a.data.buffer).equals(Buffer.from(b.data.buffer));

    assert.ok(same(plasma(7), plasma(7)));
    assert.ok(!same(plasma(7), plasma(8)));
  });

  test("picks a seed itself when given none", () => {
    assert.equal(plasma().width, 640);
  });

  test("reports a seed that is not a whole number", () => {
    assert.throws(() => plasma(1.5), { message: "seed must be a whole number between -(2^53 - 1) and 2^53 - 1" });
  });
});
