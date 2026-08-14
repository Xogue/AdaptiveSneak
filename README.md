# Adaptive Sneak

A client-side Fabric mod that makes the sneak key adaptive: tap it to toggle
crouching, or hold it for momentary crouching.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API
- Java 25

## Build

Run `./gradlew build` on macOS/Linux or `gradlew.bat build` on Windows. The
built mod JAR is written to `build/libs/`.

## Configuration

The hold threshold defaults to 150 milliseconds. After the first launch, edit
`config/adaptive_sneak.json` and change `holdThresholdMs` to customize it.

## License

Copyright (c) 2026 Xogue. Adaptive Sneak is free software licensed under the
GNU Lesser General Public License, version 3 or (at your option) any later
version. See [LICENSE](LICENSE) and [COPYING](COPYING).
