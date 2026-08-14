# CityHub README Showcase Refresh Report

## Status

`README_SHOWCASE_REFRESH: PASS`

The named execution prompt file was not present in the repository. This run followed the complete constraint set supplied in the user request.

## Audit Findings

- README previously used a four-image table and referenced the old `phase7-*` screenshots.
- `tb_activity` already contains 12 real activities, so no Activity records were added.
- The seed contained 4 Blog records. Four lightweight records were added, reusing existing Activity images and the existing two demo users; the live Docker database now contains 8 Blog records.
- No new business fields, routes, reservation logic, AI tools, or authentication model were introduced.

## Showcase Changes

README now presents a compact three-image sequence: home, activity detail/reservation, and AI assistant. Each image has one concise product description and a consistent horizontal width.

Removed the four obsolete `phase7-*` screenshots. New screenshots were captured from the real Docker/Nginx site at `http://127.0.0.1:8088`:

- `showcase-home.png`
- `showcase-activity-detail.png`
- `showcase-ai-assistant.png`

The AI image was captured after a real Qwen `qwen3.7-flash` streaming query, “当代摄影艺术展在哪里？”, and shows the ActivityTool-backed answer linking to `/activities/3`.

## Verification

| Check | Result |
|---|---|
| Docker/Nginx screenshot capture | PASS |
| Activity count | PASS — 12 |
| Blog count | PASS — 8 |
| README image references | PASS — 3 images |
| Maven compile | PASS |
| npm build | PASS |
| Docker compose config | PASS |
| Secret check | PASS — `.env` ignored; no real key/password in README/report |
| README/seed scope | PASS — showcase and lightweight demo data only |

## Git

The old screenshot files and the new showcase assets are included in the documentation commit. User prompt files, `.env`, and ignored logs are excluded.
