# OCR-Reader (Clash of Clans Bot)

A Java desktop automation bot for Clash of Clans that combines screen-region OCR (Tesseract) and template matching to farm resources automatically.

## Disclaimer

This is a personal/educational automation project. Using bots in Clash of Clans violates the game's Terms of Service and may result in an account ban. Use at your own risk.

## Features

- **Farming loop** — searches for bases with enough loot, attacks, and collects resources
- **Template matching** — locates UI buttons and troops on screen using image comparison
- **OCR** — reads gold/elixir amounts from the screen via Tesseract (Tess4j)
- **Two modes** — Main (farming) bot and Builder Base bot, controlled independently
- **GUI control panel** — start/stop each bot and configure target resource amounts
- **Global hotkey** — press `S` anywhere (even with the game window focused) to stop both bots instantly
- **Dev tools** (`tools/` folder) — helper scripts used to find screen coordinates and capture new UI templates

## Tech stack

- Java 21
- Maven
- [Tess4j](http://tess4j.sourceforge.net/) (Tesseract OCR wrapper)
- [JNativeHook](https://github.com/kwhat/jnativehook) (global keyboard hook)
- Java Swing (GUI)
- `java.awt.Robot` (screen capture, mouse/keyboard control)

## Architecture

- `Regions` / `UiElements` — screen coordinates and template definitions
- `ScreenReader` / `ImageLoader` / `TemplateMatcher` — screenshot capture and image matching
- `Bot` — checks if a UI element is present and clicks it
- `OcrService` / `TextReader` / `ImagePreprocessor` — reads numbers (gold/elixir) from the screen
- `SearchBot` — clears popups, zooms out, and searches for a good base to attack
- `AttackBot` — deploys troops and heroes during battle
- `Builder` — separate loop for the Builder Base
- `FarmingBot` / `Main` — ties `SearchBot` and `AttackBot` together into the main farming loop
- `BotGui` — Swing control panel (start/stop, config, global hotkey)
- `tools/` — standalone helper scripts (find coordinates, capture template screenshots)

## Setup

1. Requires Java 21 and Maven
2. Clone the repo
3. Build with Maven:
   ```
   mvn clean package
   ```
4. Run the shaded jar (starts the GUI):
   ```
   java -jar target/BotOCR-1.0-SNAPSHOT.jar
   ```

## Usage

1. Open Clash of Clans in a window
2. Launch the bot GUI
3. Set target gold/elixir amounts (the bot stops once reached)
4. Click **Start Main Bot** or **Start Builder Bot**
5. Press **S** at any time to stop both bots immediately

## Known limitations

- Screen regions are calibrated for a specific screen resolution/window size — coordinates will need to be re-calibrated if your setup differs
- OCR is not 100% accurate on the in-game font; occasional misreads are possible
