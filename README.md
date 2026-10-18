<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.tracket.app">
    <img src="docs/banner.png" width="100%" alt="Please consider buying a subscription. If you like this app, support the dev behind this magic :) Plans: Monthly, Yearly, and Lifetime.">
  </a>
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.tracket.app"><img alt="Buy a subscription on Google Play" src="https://img.shields.io/badge/Google_Play-Buy_a_subscription-111111?style=for-the-badge&logo=googleplay&logoColor=white"></a>
  &nbsp;
  <a href="https://morphe.software/add-source?github=contactjayclatty/tracket-patches&name=Tracket%20patches"><img alt="Add this source in Morphe Manager" src="https://img.shields.io/badge/Morphe_Manager-Add_this_source-6e40c9?style=for-the-badge&logo=android&logoColor=white"></a>
</p>

<p align="center">
  <a href="https://github.com/contactjayclatty/tracket-patches/releases/latest"><img alt="release" src="https://img.shields.io/github/v/release/contactjayclatty/tracket-patches?style=flat-square&label=release&color=e4b15a"></a>
  <a href="https://github.com/contactjayclatty/tracket-patches/releases/latest"><img alt="Tracket 0.9.38" src="https://img.shields.io/badge/Tracket-0.9.38-2ea44f?style=flat-square"></a>
  <a href="LICENSE"><img alt="GPL-3.0" src="https://img.shields.io/badge/license-GPL--3.0-blue?style=flat-square"></a>
  <a href="https://github.com/MorpheApp/morphe-cli"><img alt="Morphe desktop 1.18.1" src="https://img.shields.io/badge/Morphe-desktop_1.18.1-6e40c9?style=flat-square"></a>
</p>

# Tracket patches

Patches for Tracket (`com.tracket.app`) **0.9.38**, versionCode 237, by [Mara Quill](#support-the-developer).

## Support the developer

Please consider buying a subscription if you like this app. Support the dev behind this magic :)

Tracket sells three plans. The names below are the ones in version 0.9.38. The price is the one Google Play shows in the app.

| Plan | How it bills | Product |
| --- | --- | --- |
| **Monthly** | Renews every month | Play subscription |
| **Yearly** | Renews every year | Play subscription |
| **Lifetime** | Pay once | `pro_lifetime` |

> [!TIP]
> Open Tracket, choose **See all plans**, and pick Monthly, Yearly, or Lifetime. [Get Tracket on Google Play](https://play.google.com/store/apps/details?id=com.tracket.app).

## On this page

- [The patch](#the-patch)
- [Add it in Morphe Manager](#add-it-in-morphe-manager)
- [You already have the file](#you-already-have-the-file)
- [Desktop](#desktop)
- [Before you patch](#before-you-patch)
- [Build](#build)
- [How the patch finds the code](#how-the-patch-finds-the-code)
- [Cut the next release](#cut-the-next-release)

## The patch

| | |
| --- | --- |
| **Name** | Unlock lifetime |
| **Default** | On |
| **You get** | The app treats a lifetime purchase as active, and a re-signed APK gets past the Play license check |
| **Works on** | Stock Tracket 0.9.38 only |

```mermaid
flowchart LR
  A["Stock Tracket 0.9.38"] --> B["Morphe Manager"]
  B --> C["Unlock lifetime"]
  C --> D["Install"]
```

## Add it in Morphe Manager

1. Install [Morphe Manager](https://github.com/MorpheApp/morphe-manager).
2. Tap **Add this source** at the top of this page. Manager asks before it adds anything.
   By hand: <kbd>Sources</kbd> → <kbd>+</kbd> → <kbd>Remote</kbd>, then paste:

   ```text
   github.com/contactjayclatty/tracket-patches
   ```

3. Open Tracket. Leave **Unlock lifetime** enabled.
4. Patch the stock 0.9.38 APK and install the result.

Manager reads [`patches-bundle.json`](patches-bundle.json) on `main` and downloads [`patches-1.0.0.mpp`](https://github.com/contactjayclatty/tracket-patches/releases/download/v1.0.0/patches-1.0.0.mpp) from the [v1.0.0 release](https://github.com/contactjayclatty/tracket-patches/releases/tag/v1.0.0). A repository source updates when a newer release is published.[^1]

> [!IMPORTANT]
> Use a stock Tracket **0.9.38** APK. Any other version is refused.
> This changes the copy on the phone. Tracket's servers can still refuse the account.

> [!WARNING]
> A patch source decides what is written into the app. Add this source only if you trust it.

## You already have the file

Import [`dist/patches-1.0.0.mpp`](dist/patches-1.0.0.mpp) from <kbd>Sources</kbd> → <kbd>+</kbd> → <kbd>Local</kbd>.

A local file stays on the version you imported. When a new release comes out, replace the file yourself.

## Desktop

From a stock `0.9.38` APK, with [Morphe desktop CLI](https://github.com/MorpheApp/morphe-cli) 1.18.1:

```text
morphe patch --bytecode-mode=FULL -p patches-1.0.0.mpp -o tracket-0.9.38-morphe.apk tracket-0.9.38.apk
```

`--bytecode-mode=FULL` is the mode this bundle was checked with.

To turn the patch off, pass `--options-file` with:

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
| Version | 0.9.38 only, versionCode 237 |
| Patch | Unlock lifetime, on by default |
| Needs | Morphe Manager, or Morphe desktop CLI 1.18.1 |
| License | [GPL-3.0-or-later](LICENSE) |

The on-device grant does not record monthly, yearly, or lifetime. Any active grant is treated as pro. Lifetime is the product this patch is named for (`pro_lifetime`).

A newer Tracket build ships new obfuscated names. This release fails on that build instead of guessing. The names pinned to 0.9.38 are `Lql3`, `Lxb6`, `Le2`, `Lmr`, `Lk01`, `Lsq`, `Ltz7`, `Luy7`, `Lhy7`, and `Lqp1`.

## Build

JDK 17 or newer, and `morphe-desktop-1.18.1-all.jar` from the Morphe desktop CLI.

```text
$env:MORPHE_JAR = 'C:\path\to\morphe-desktop-1.18.1-all.jar'
pwsh -File build.ps1
```

The script writes `dist/patches-1.0.0.mpp`. The banner at the top of this page is rendered from [`docs/banner.html`](docs/banner.html).

## How the patch finds the code

<details>
<summary>Fingerprint, grant, and the Play license check</summary>

The purchase method is found by fingerprint: a `void` method with one `java.util.List` parameter whose body contains both `purchaseTime` and `SHA-256`. A short prelude is inserted at the start. It seeds the on-device grant, starts the app's own refresh coroutine, and returns. The original method body stays in the dex and is not reached.

`com.pairip.licensecheck.LicenseClient.checkLicense` returns immediately, so a re-signed APK is not stopped by the Play licensing client. Tracket 0.9.38 has no `libpairip` native library.

`src/tracket/morphe/CheckPrelude.java` is a maintainer check. It is not packed into the `.mpp`.

</details>

## Cut the next release

<details>
<summary>Version bump checklist</summary>

1. Bump `version` in `patches-bundle.json` and in `build.ps1`.
2. Rebuild. Keep the asset name `patches-<version>.mpp`.
3. Set `download_url` to `https://github.com/contactjayclatty/tracket-patches/releases/download/v<version>/patches-<version>.mpp`.
4. Set `created_at` and `description` for that version. Keep the subscription ask in the description.
5. Commit `patches-bundle.json` on `main`.
6. Create a GitHub release tagged `v<version>` and attach that `.mpp` under the same file name.

</details>

---

<p align="center">
  Monthly · Yearly · Lifetime<br>
  <a href="https://play.google.com/store/apps/details?id=com.tracket.app"><strong>Support the dev behind this magic :)</strong></a>
</p>

[^1]: Local `.mpp` imports do not auto-update. Remote GitHub sources do.
