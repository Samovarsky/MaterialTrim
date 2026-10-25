# Material Trim

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-24+-3DDC84?logo=android)](https://developer.android.com/)
[![FFmpegKit](https://img.shields.io/badge/FFmpegKit-GPL-red)](https://github.com/arthenica/ffmpeg-kit)

An offline audio & video editor for Android, built with Jetpack Compose and FFmpegKit. Trim, merge, convert, apply effects, edit metadata, export GIFs — all on device. No ads,no network.

> [Читать по-русски ↓](#material-trim-русский)

## Features

| Category | What it does |
|---|---|
| **Audio trim** | Lossy & lossless trim with fade in/out, 5-band parametric EQ, volume and speed control. Presets from current start position. Undo/Redo (30 snapshots). |
| **Video trim** | Lossless cut (keyframe-aligned, no re-encode) and re-encode mode with `libopenh264`. Preview with seek-closest on API 26+. |
| **Video tools** | GIF export (palettegen/paletteuse, lanczos scale), transform (scale, transpose, flip, speed), replace audio track, strip audio track. |
| **Merge** | Concat with optional `acrossfade` crossfade between tracks. Reorder inputs freely. |
| **Convert** | MP3, WAV, M4A, OGG, FLAC with selectable bitrate (128/192/256/320 kbps). Single-file and batch modes. |
| **Split** | Split one file into two at any point. |
| **Extract** | Pull audio track out of any video into MP3/WAV/M4A. |
| **Effects** | Reverse (`areverse`), loop N times, denoise (`afftdn`), loudness normalize (`loudnorm` EBU R128). |
| **Metadata** | Batch-edit ID3/M4A tags (title, artist, album, year) and embedded cover art. Smart prefill from current tags, dirty-tracking per field. |
| **History** | Last 20 operations with timestamps, persisted in cache. |
| **Theming** | 7 Material 3 palettes (Pink, Green, Olive, Yellow, Brown, Red, Cyan) + Material You wallpaper colors on Android 12+. Adaptive icon with system recolor. |
| **Localization** | Full ru/en interface, English default on first launch. Persisted per-app. |

## Tech stack

- **Kotlin 2.0+**, **Jetpack Compose**, **Material 3**
- **FFmpegKit** (GPL build — libx264, libopenh264, libmp3lame, libvorbis, libvpx, flac, aac, pcm_s16le)
- **AndroidX**: Lifecycle (ViewModel + StateFlow), DocumentFile (SAF), Activity Compose contracts
- **MediaMetadataRetriever** for tag reading, **MediaPlayer** / **VideoView** for preview
- `minSdk 24`, `targetSdk 35`

## Build

```bash
git clone https://github.com/Samovarsky/MaterialTrim.git
cd MaterialTrim
./gradlew assembleDebug
```

Requirements: **Android Studio Ladybug+**, **JDK 17**, **AGP 9.x**. First Gradle sync downloads FFmpegKit binaries (~60 MB per ABI).

## Dependencies

| Library | License |
|---|---|
| FFmpegKit (GPL build) | GPL-3.0 |
| libx264 | GPL-2.0+ |
| libopenh264 | BSD-2-Clause |
| Jetpack Compose, Material 3 | Apache-2.0 |
| AndroidX (Lifecycle, DocumentFile, Activity) | Apache-2.0 |

## Development / AI assistance

This project was developed with the assistance of a generative AI coding
assistant for code generation and diagnostics. Architecture, debugging,
design decisions, testing and final verification were performed by the
human maintainer. The AI was used as a tool, not as an author; all shipped
code was reviewed, corrected and validated by hand before each release.

## License

This project is licensed under the **GNU General Public License v3.0** — see [LICENSE](LICENSE) for the full text.

> The GPL choice is not optional: the project ships **libx264** (itself GPL) via FFmpegKit, which forces a copyleft license on the whole derived work. Any fork that distributes binaries must release its source under compatible GPL terms.


---

# Material Trim (русский)

Оффлайн-редактор аудио и видео для Android на Jetpack Compose и FFmpegKit: обрезка, склеивание, конвертация, эффекты, метаданные, экспорт GIF — всё прямо на устройстве. Без рекламы, без сети.
 
## Возможности

| Категория | Что умеет |
|---|---|
| **Обрезка аудио** | Lossy- и lossless-обрезка, fade in/out, 5-полосный эквалайзер, громкость, скорость. Пресеты от текущей позиции начала. Undo/Redo (30 снимков). |
| **Обрезка видео** | Lossless-резка по ключевым кадрам без перекодирования и режим перекодирования через `libopenh264`. Предпросмотр с точным seek на API 26+. |
| **Видео-инструменты** | Экспорт GIF (palettegen/paletteuse, lanczos), трансформация (масштаб, поворот, зеркало, скорость), замена и удаление аудиодорожки. |
| **Склеивание** | Конкатенация с опциональным кроссфейдом `acrossfade`, свободный порядок дорожек. |
| **Конвертация** | MP3, WAV, M4A, OGG, FLAC, битрейт 128/192/256/320 kbps, одиночный и пакетный режимы. |
| **Разделение и извлечение** | Разрез файла на две части; извлечение аудио из любого видео в MP3/WAV/M4A. |
| **Эффекты** | Реверс (`areverse`), зацикливание, шумоподавление (`afftdn`), нормализация громкости (`loudnorm`, EBU R128). |
| **Метаданные** | Пакетные теги ID3/M4A (название, исполнитель, альбом, год) и встроенная обложка; предзаполнение из текущих тегов. |
| **История** | Последние 20 операций с временными метками. |
| **Темы и локализация** | 7 палитр Material 3 + цвета обоев Material You (Android 12+), адаптивная иконка с системной перекраской; полный ru/en, английский по умолчанию. |

## Сборка

Требования: Android Studio Ladybug+, JDK 17, AGP 9.x. Команды клонирования и сборки — в английской секции выше (`./gradlew assembleDebug`).

## Разработка / помощь ИИ

Проект создавался с использованием генеративного ИИ-ассистента для
генерации кода и диагностики ошибок. Архитектура, отладка, проектные
решения, тестирование и финальная проверка выполнены человеком-мейнтейнером.
ИИ использовался как инструмент, а не как автор: весь публикуемый код
проходил ручную ревизию, исправления и валидацию перед каждым релизом.

## Лицензия

Проект распространяется под **GNU General Public License v3.0** — полный текст в файле [LICENSE](LICENSE). Выбор GPL не случаен: в сборке присутствует libx264 (GPL) через FFmpegKit, что требует copyleft для всего производного кода.


