# Tracket Morphe patches

One Morphe patch for [Tracket](https://play.google.com/store/apps/details?id=com.tracket.app) `com.tracket.app` **0.9.38** (versionCode 237).

| Patch | Default | What it does |
| --- | --- | --- |
| Unlock lifetime | on | Seeds the on-device entitlement grant and skips the Play license check |

The patch is GPL-3.0-or-later, the same license family as Morphe patches. See `LICENSE`. Author: Mara Quill.

## What the patch changes

The grant method is found by fingerprint, not by its obfuscated class name: a `void` method with one `java.util.List` parameter whose body contains both `purchaseTime` and `SHA-256`. A prelude is inserted at the start of that method. It seeds the client ratchet, publishes a grant, starts the existing refresh coroutine, and returns. The original method body stays in the dex and is not reached.

`com.pairip.licensecheck.LicenseClient.checkLicense` gets `return-void` as its first instruction so a re-signed APK is not stopped by the Play licensing client. This build of Tracket has no `libpairip` native library.

## Limits

- This is a client-side grant. Tracket's servers can still refuse an account. The client proof does not encode monthly, yearly, or lifetime. Any active grant is treated as pro. Lifetime is the product name this patch is named for (`pro_lifetime`).
- Pinned to Tracket 0.9.38. The grant method can survive a rename of its own class. These R8 names are hardcoded and will not survive a new obfuscation pass: `Lql3`, `Lxb6`, `Le2`, `Lmr`, `Lk01`, `Lsq`, `Ltz7`, `Luy7`, `Lhy7`, `Lqp1`.
- Built against Morphe desktop CLI 1.18.1. Apply with `--bytecode-mode=FULL`. That is the mode this bundle was checked with.
- `src/tracket/morphe/CheckPrelude.java` is a maintainer verifier. It is not inside the `.mpp`.

## Import the file

`dist/patches-1.0.0.mpp` is the bundle. In Morphe Manager, import that file as a local patch source. A local `.mpp` does not auto-update.

Desktop CLI, from a stock `0.9.38` APK:

```text
morphe patch --bytecode-mode=FULL -p dist/patches-1.0.0.mpp -o tracket-0.9.38-morphe.apk tracket-0.9.38.apk
```

Options file, if you want one:

```json
[
  {
    "patches": {
      "Unlock lifetime": { "enabled": true }
    }
  }
]
```

Pass it with `--options-file`.

## Build

You need JDK 17 or newer and `morphe-desktop-1.18.1-all.jar`.

```text
$env:MORPHE_JAR = 'C:\path\to\morphe-desktop-1.18.1-all.jar'
pwsh -File build.ps1
```

On this machine the jar is already at `C:\Android\tools\morphe\morphe-desktop-1.18.1-all.jar`, so `MORPHE_JAR` can be left unset. The script writes `dist/patches-1.0.0.mpp` and refreshes `tracket-lifetime-1.0.0.mpp` next to this README. The second name is only for the local patch catalog.

## Morphe Manager

Add this source:

```text
github.com/contactjayclatty/tracket-patches
```

Manager reads `patches-bundle.json` on the default branch and downloads `patches-1.0.0.mpp` from the `v1.0.0` release. A local import of `dist/patches-1.0.0.mpp` still works and does not auto-update.

## Next release

1. Bump `version` in `patches-bundle.json` and in `build.ps1`.
2. Rebuild. Keep the asset name `patches-<version>.mpp`.
3. Set `download_url` to `https://github.com/contactjayclatty/tracket-patches/releases/download/v<version>/patches-<version>.mpp`.
4. Set `created_at` and `description` for that version.
5. Commit `patches-bundle.json` on `main`.
6. Create a GitHub release tagged `v<version>` and attach that `.mpp` under the same file name.
