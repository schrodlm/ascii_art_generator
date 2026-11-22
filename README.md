# Clascii

**Live demo: https://schrodlm.github.io/clascii/** (even with live camera, hell yeah)

Clascii turns pictures into ASCII art. It draws them with classic character tables, with characters
of your own, or with braille, whose 2 by 4 dots per character show eight times the detail of a
letter, and three ways of dithering keep its greys smooth. Every character can carry the colour of
the part of the picture it stands for. Before drawing, you can rotate or flip the picture and change
its brightness or contrast, or invert it; afterwards you can scale the finished art. The art fits
whatever size you ask for, in columns, rows and the shape of your font's characters, and it is quick
enough to draw a live camera in the browser.

It comes three ways: a package for JavaScript and TypeScript, a Scala library for the JVM and
Scala.js, and a command-line tool that reads PNG, JPEG, GIF, BMP and TIFF files, or draws a picture
of its own, and prints the art to your terminal in 24-bit colour or writes it to a file.

![The Great Wave off Kanagawa drawn in coloured ASCII characters in a terminal](docs/terminal-ascii.png)

## JavaScript and TypeScript

```bash
npm install clascii
```

The package is the same Scala code as the library below, compiled to a JavaScript module with
Scala.js. It has no dependencies and runs in browsers and in Node. Because JavaScript can pass
anything, it checks every value and throws an `Error` that says what is wrong, and its TypeScript
types describe every setting, so your editor lists them as you type.

A picture from a canvas:

```js
import { render } from "clascii";

const canvas = document.querySelector("canvas");
const pixels = canvas.getContext("2d").getImageData(0, 0, canvas.width, canvas.height);

document.querySelector("pre").textContent = render(pixels, { maxColumns: 100 }).text;
```

A live camera, in braille. Drawing each frame at about 4 pixels per column first lets the browser
do the shrinking, which keeps every frame quick:

```js
const video = document.querySelector("video");
video.srcObject = await navigator.mediaDevices.getUserMedia({ video: true });
await video.play();

const frame = document.createElement("canvas").getContext("2d", { willReadFrequently: true });
const columns = 120;

function draw() {
  frame.canvas.width = columns * 4;
  frame.canvas.height = Math.round(frame.canvas.width * video.videoHeight / video.videoWidth);
  frame.drawImage(video, 0, 0, frame.canvas.width, frame.canvas.height);
  const pixels = frame.getImageData(0, 0, frame.canvas.width, frame.canvas.height);
  const art = render(pixels, { maxColumns: columns, characters: { braille: "ordered" }, greyFilters: [{ type: "invert" }] });
  document.querySelector("pre").textContent = art.text;
  requestAnimationFrame(draw);
}
requestAnimationFrame(draw);
```

Your own characters, in colour:

```ts
import { render, type ColoredArt } from "clascii";

const art: ColoredArt = render(pixels, { characters: { chars: " .:-=+*#%@" }, colors: true });
// Red, green, blue and alpha for every character: paint each one in its colour.
const colours = new ImageData(art.colors, art.columns, art.rows);
```

## Scala

```scala
libraryDependencies += "io.github.schrodlm" %%% "clascii" % "0.1.0"
```

The library is plain Scala with no dependencies, for the JVM and for Scala.js. You hand it an image
and settings, and it hands back the art:

```scala
import clascii.core.characters.Dither
import clascii.core.conversion.BrailleConverter
import clascii.core.image.RGBImage
import clascii.core.pipeline.{AsciiArtPipeline, RenderSettings}

// One Int per pixel, 0xAARRGGBB, row by row.
val image = new RGBImage(width, height, pixels)
val settings = RenderSettings(maxColumns = 100, characterConverter = new BrailleConverter(Dither.Diffusion))

println(AsciiArtPipeline.render(image, settings).toText)
```

Filters and character converters are open traits, so filters and ways of drawing of your own fit in
beside the built-in ones.

## Command line

The tool runs from a clone of this repository, with a JDK and [sbt](https://www.scala-sbt.org/):

```bash
sbt "cli/run --image photo.jpg --braille --invert --color --output-console"
sbt "cli/run --help"
```

The screenshots write `clascii` for `sbt "cli/run …"`.

![The Great Wave drawn in colour braille in a terminal](docs/terminal-braille.png)

## Demo page

**[Try it in your browser](https://schrodlm.github.io/clascii/)**: every setting, on a
generated picture, an image or a video of your own, or your camera. Nothing leaves your browser. A
stats panel shows the frame rate, how long each part of a frame takes, and how the picture is shrunk
into characters.

![The demo page showing the Great Wave in colour braille, with its stats panel](docs/demo.png)

To run it locally, build it next to the page and serve the folder:

```bash
sbt js/demo
python3 -m http.server 8137 --directory js/demo
# then open http://localhost:8137
```

## How it works

![The stages from a picture to text](docs/pipeline.png)

The picture is filtered, then shrunk to the size of the art, turned grey, filtered again, and
turned into characters; then the finished characters can be filtered too. The character converter
decides how many grey pixels one character needs, and the shrink delivers exactly that many: one
for a table, 2 by 4 for braille. That is where braille's extra detail comes from. With `colors`,
each character also keeps the average colour of the pixels it covers; grey filters change the
characters but never the colours.

## Architecture

![UML class diagram of the cli, core and js modules](docs/architecture.png)

The project has three sbt modules:

- **core** is the converter, plain Scala without any input or output, compiled both for the JVM and
  for JavaScript. It is published to Maven Central as `clascii`.
- **cli** reads image files, parses the options and prints the art. It runs on the JVM.
- **js** turns core into a JavaScript library with Scala.js. It checks every value JavaScript
  passes in, since JavaScript can pass anything, and hands back plain objects. Its types are
  written by hand in `js/package/index.d.ts`. It is published to npm as `clascii`.

## Development

You need a JDK and sbt, and Node (tested with 20) for the JavaScript side. Once, install the
package's build tools:

```bash
cd js/package && npm ci
```

Then:

```bash
sbt test                       # every test: core on the JVM and on JS, the CLI, the JS library
                               # against the development and the minified build, and the types
sbt js/npmPackage              # builds the npm package in js/package
node js/bench/render.bench.mjs # times render on webcam, HD and full HD frames (after sbt js/fullLinkJS)
```

The diagrams are drawn with Graphviz: `dot -Tpng -Gdpi=150 docs/pipeline.dot -o docs/pipeline.png`,
and the same for `architecture.dot`.

## Licence

[MIT](LICENSE). The picture in the screenshots is *The Great Wave off Kanagawa* by Katsushika
Hokusai (about 1831), which is in the public domain.
