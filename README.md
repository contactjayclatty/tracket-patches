# Tracket patches

[![release](https://img.shields.io/github/v/release/contactjayclatty/tracket-patches?style=flat-square&label=release)](https://github.com/contactjayclatty/tracket-patches/releases/latest)
[![Tracket 0.9.38](https://img.shields.io/badge/Tracket-0.9.38-2ea44f?style=flat-square)](https://github.com/contactjayclatty/tracket-patches/releases/latest)
[![license](https://img.shields.io/badge/license-GPL--3.0-blue?style=flat-square)](LICENSE)
[![Morphe](https://img.shields.io/badge/Morphe-desktop%201.18.1-6e40c9?style=flat-square)](https://github.com/MorpheApp/morphe-cli)

Patches for Tracket (`com.tracket.app`) **0.9.38**, versionCode 237. One patch is included. It is on by default.

| Patch | What you get |
| --- | --- |
| **Unlock lifetime** | The app treats a lifetime purchase as active, and a re-signed APK gets past the Play license check. |

Author: Mara Quill. License: [GPL-3.0-or-later](LICENSE).

<p align="center">
  <a href="https://morphe.software/add-source?github=contactjayclatty/tracket-patches&name=Tracket%20patches"><strong>Add this source in Morphe Manager</strong></a>
</p>

> [!IMPORTANT]
> Use a stock Tracket **0.9.38** APK. Any other version is refused by the patch.
> This changes the app on the phone. Tracket's servers can still refuse the account.

> [!WARNING]
> A patch source decides what is written into the app. Add this source only if you trust it.

## Use it in Morphe Manager

1. Install [Morphe Manager](https://github.com/MorpheApp/morphe-manager).
2. Tap the button above. Manager asks before it adds the source.
   Or do it by hand: **Sources** → **+** → **Remote**, then paste:

   ```text
   github.com/contactjayclatty/tracket-patches
   ```

3. Open Tracket in Manager. Leave **Unlock lifetime** enabled.
4. Patch the stock 0.9.38 APK and install the result.

Manager reads [`patches-bundle.json`](patches-bundle.json) on `main` and downloads [`patches-1.0.0.mpp`](https://github.com/contactjayclatty/tracket-patches/releases/download/v1.0.0/patches-1.0.0.mpp) from the [v1.0.0 release](https://github.com/contactjayclatty/tracket-patches/releases/tag/v1.0.0). A repository source updates on its own when a newer release is published.

## If you already have the file

Import [`dist/patches-1.0.0.mpp`](dist/patches-1.0.0.mpp) from **Sources** → **+** → **Local**.

A local file stays at the version you imported. Replace it yourself when a new release comes out.

## Desktop

From a stock `0.9.38` APK, with the [Morphe desktop CLI](https://github.com/MorpheApp/morphe-cli) 1.18.1:

```text
morphe patch --bytecode-mode=FULL -p patches-1.0.0.mpp -o tracket-0.9.38-morphe.apk tracket-0.9.38.apk
```

`--bytecode-mode=FULL` is the mode this bundle was checked with.

To turn the patch off, pass an options file with `--options-file`:

```json
[
  {
    "meta": {
      "source": "patches-1.0.0.mpp"
    },
    "patches": {
      "Unlock lifetime": {
        "enabled": false
      }
    }
  }
]
```

## Before you patch

| | |
| --- | --- |
| App | Tracket, package `com.tracket.app` |
| Version | 0.9.38 only (versionCode 237) |
| Patch | Unlock lifetime, enabled by default |
| Needs | Morphe Manager, or Morphe desktop CLI 1.18.1 |

The grant lives in the app. It does not encode monthly, yearly, or lifetime. Any active grant is treated as pro. Lifetime is the product this patch is named for (`pro_lifetime`).

A newer Tracket build uses new obfuscated names. This release will fail on that build instead of guessing. The names pinned to 0.9.38 are `Lql3`, `Lxb6`, `Le2`, `Lmr`, `Lk01`, `Lsq`, `Ltz7`, `Luy7`, `Lhy7`, and `Lqp1`.

## Build

You need JDK 17 or newer and `morphe-desktop-1.18.1-all.jar` from the Morphe desktop CLI.

```text
$env:MORPHE_JAR = 'C:\path\to\morphe-desktop-1.18.1-all.jar'
pwsh -File build.ps1
```

The script writes `dist/patches-1.0.0.mpp`. `src/tracket/morphe/CheckPrelude.java` is a maintainer check. It is not packed into the `.mpp`.

<details>
<summary>How the patch finds the code</summary>

The purchase method is found by fingerprint: a `void` method with one `java.util.List` parameter whose body contains both `purchaseTime` and `SHA-256`. A short prelude is inserted at the start. It seeds the on-device grant, starts the app's own refresh coroutine, and returns. The original method body stays in the dex and is not reached.

`com.pairip.licensecheck.LicenseClient.checkLicense` returns immediately, so a re-signed APK is not stopped by the Play licensing client. Tracket 0.9.38 has no `libpairip` native library.

</details>

<details>
<summary>Cut the next release</summary>

1. Bump `version` in `patches-bundle.json` and in `build.ps1`.
2. Rebuild. Keep the asset name `patches-<version>.mpp`.
3. Set `download_url` to `https://github.com/contactjayclatty/tracket-patches/releases/download/v<version>/patches-<version>.mpp`.
4. Set `created_at` and `description` for that version.
5. Commit `patches-bundle.json` on `main`.
6. Create a GitHub release tagged `v<version>` and attach that `.mpp` under the same file name.

</details>
