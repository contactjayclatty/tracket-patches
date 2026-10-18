# Tracket Patches website

The public site lives in this `site/` folder. It is a fully static, multi-page dark-mode website with no backend, bundler, dependency installation, or third-party JavaScript.

## Pages

- `index.html` — Overview
- `patches/index.html` — Patch catalogue
- `docs/index.html` — Documentation index and browser-side search
- `docs/technical/index.html` — Technical reference
- `docs/compatibility/index.html` — Compatibility and troubleshooting
- `docs/maintaining/index.html` — Maintainer guide
- `releases/index.html` — Releases
- `about/index.html` — About and developer support

Assets live under `site/assets/`. All internal paths are rooted at `/tracket-patches/`, matching the default GitHub Pages project path.

## Publishing

The workflow `.github/workflows/pages.yml` uploads `site/` and deploys through GitHub Pages on updates to `main` that affect the website.

In repository **Settings → Pages**, set **Build and deployment → Source** to **GitHub Actions**. GitHub Pages must be enabled before the first successful deployment.

Default expected site URL: https://contactjayclatty.github.io/tracket-patches/

Do not move the existing `docs/*.md` files or change `patches-bundle.json` when editing the website. The patcher and the site deploy independently.

## Design

- Dark mode throughout
- Responsive navbar and mobile dropdown
- Custom SVG brand and orbital visual
- Semantic pages and keyboard navigation
- Reduced-motion support
- Client-only documentation search and copy controls

Tracket Patches is unofficial and not affiliated with Tracket or Google Play.
