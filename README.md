# OCR-Reader (Clash of Clans Bot)

A Java desktop automation bot for Clash of Clans that combines screen-region OCR (Tesseract) and template matching to farm resources automatically.

## Disclaimer

This is a personal/educational automation project. Using bots in Clash of Clans violates the game's Terms of Service and may result in an account ban. Use at your own risk.

## Features

- **Farming loop** — searches for bases with enough loot, attacks, and collects resources
- **Template matching** — finds UI buttons and troops anywhere on screen with OpenCV and clicks where it found them (no hard-coded click positions)
- **OCR** — reads gold/elixir amounts from the screen via Tesseract (Tess4j)
- **Two modes** — Main (farming) bot and Builder Base bot, controlled independently
- **Two input modes** — `Robot` (game on your PC screen) or `ADB` (game in an Android emulator such as LDPlayer, so you can keep using the PC in parallel)
- **GUI control panel** — dark Swing UI with live log, status indicator, ADB connection check, and settings that are remembered between runs
- **Global hotkey** — press `F9` anywhere (even with the game window focused) to stop the running bot instantly
- **Dev tools** (`tools/` folder) — helper scripts used to find screen coordinates and capture new UI templates

## Tech stack

- Java 21
- Maven
- [Tess4j](http://tess4j.sourceforge.net/) (Tesseract OCR wrapper)
- [JNativeHook](https://github.com/kwhat/jnativehook) (global keyboard hook)
- [OpenCV](https://opencv.org/) via `org.openpnp:opencv` (template matching)
- Java Swing + [FlatLaf](https://www.formdev.com/flatlaf/) (GUI)
- `java.awt.Robot` or ADB (`adb exec-out screencap`, `adb shell input`) for screen capture and input

## Architecture

- `Regions` / `UiElements` — screen coordinates and template definitions
- `ScreenSource` / `InputDevice` — abstractions over where screenshots come from and how input is sent, with `Robot` (`ScreenReader`, `RobotInputDevice`) and `ADB` (`AdbScreenSource`, `AdbInputDevice`) implementations; `GameContext` wires them together
- `ImageLoader` / `TemplateMatcher` — template loading and OpenCV image matching
- `Bot` — checks if a UI element is present and clicks it
- `OcrService` / `TextReader` / `ImagePreprocessor` — reads numbers (gold/elixir) from the screen
- `SearchBot` — clears popups, zooms out, and searches for a good base to attack
- `AttackBot` — deploys troops and heroes during battle
- `Builder` — separate loop for the Builder Base
- `FarmingBot` / `Main` — ties `SearchBot` and `AttackBot` together into the main farming loop
- `BotGui` — Swing control panel (mode, targets, start/stop, log, global hotkey)
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

## Running a prebuilt copy (sharing with a friend)

The bot reads `images/` (templates) and `tessdata/` (OCR data) from the **working directory**, so they must sit next to the jar.

1. Build: `mvn clean package`
2. Create a folder with:
   ```
   BotOCR/
     BotOCR-1.0-SNAPSHOT.jar   (from target/)
     images/
     tessdata/
     start.bat
   ```
3. Zip it. The other person needs **Java 21** installed and then just double-clicks `start.bat` (it switches to its own folder before starting the jar)
4. In LDPlayer: *Settings -> Other settings -> ADB debugging -> Open local connection*, game language English, resolution 1920x1080
5. In the GUI choose **ADB (LDPlayer)**. The bot tries to find `adb.exe` (LDPlayer / Android SDK folders) and the device serial by itself; if not, use **Browse** and **Test connection**

## Usage

1. Launch the bot GUI
2. Choose the input mode:
   - **Robot (PC screen)** — Clash of Clans is visible on your main screen
   - **ADB (LDPlayer)** — the `adb.exe` path (bundled with LDPlayer) and device serial (default `emulator-5554`) are detected automatically when possible; otherwise set them manually, then click **Test connection**. The emulator must run at 1920x1080
3. Set target gold/elixir amounts (the bot stops once reached)
4. Click **Start Main Bot** or **Start Builder Bot**
5. Press **F9** at any time to stop the running bot immediately

## Known limitations

- Templates and screen regions are calibrated for a 1920x1080 frame; other resolutions need new templates (see `tools/`)
- Troop deployment points are fixed coordinates and may need adjusting after game UI updates
- OCR is not 100% accurate on the in-game font; occasional misreads are possible
