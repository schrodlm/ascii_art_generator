// Times render on frames the size of a webcam, HD and full HD video, at three widths.
// Not part of sbt test, because timings depend on the machine. Run after sbt js/fullLinkJS:
//   node js/bench/render.bench.mjs [path to main.mjs]
const library = process.argv[2] ?? new URL("../target/scala-2.13/js-opt/main.mjs", import.meta.url);
const { render, plasma } = await import(library);

// A frame of busy, repeatable noise, so no shortcut can make it cheaper than a real one.
function noise(width, height) {
  const data = new Uint8ClampedArray(width * height * 4);
  for (let i = 0; i < data.length; i++) data[i] = (i * 2654435761) >>> 24;
  return { data, width, height };
}

const frames = { "640×480 (webcam)": plasma(1), "1280×720 (HD)": noise(1280, 720), "1920×1080 (full HD)": noise(1920, 1080) };
const median = (times) => times.sort((a, b) => a - b)[Math.floor(times.length / 2)];

for (const [name, frame] of Object.entries(frames)) {
  const results = [];
  for (const columns of [80, 150, 300]) {
    const settings = { maxColumns: columns, greyFilters: [{ type: "contrast", factor: 1.2 }, { type: "invert" }] };
    // Warm up first: the engine compiles code to machine code only after it has run a while.
    for (let i = 0; i < 20; i++) render(frame, settings);
    const times = [];
    for (let i = 0; i < 100; i++) {
      const start = performance.now();
      render(frame, settings);
      times.push(performance.now() - start);
    }
    results.push(`${String(columns).padStart(3)} columns: ${median(times).toFixed(1).padStart(5)} ms`);
  }
  console.log(name.padEnd(20), results.join("   "));
}
