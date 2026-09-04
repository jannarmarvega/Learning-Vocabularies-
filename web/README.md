# My Dictionary — Web

A Nuxt 4 port of the Android app, running the **same SQLite database** the app ships
(`app/src/main/assets/databases/dictionary.db` — 1762 words, 36 categories, 12 parts of speech).

## Run it

```bash
docker compose up -d --build
```

Open <http://localhost:3000>.

```bash
docker compose logs -f dictionary   # follow logs
docker compose down                 # stop (data survives)
docker compose down -v              # stop and wipe favourites/learnings
```

### Hot-reloading dev server

```bash
docker compose --profile dev up dictionary-dev
```

Serves on <http://localhost:3001> with the source bind-mounted.

> Run this through Docker rather than `npm run dev` on the host: esbuild's binary
> segfaults when executed from a `/mnt/c` WSL2 drive mount.

## How the database works

The word list ships read-only inside the image at `/app/seed/dictionary.db`. On first
boot it is copied to `/app/data/dictionary.db` on the `dictionary-data` volume, and
everything after that — favourites, learnings, the accent setting — is written there.
This mirrors Room's `createFromAsset`, so rebuilding the image never discards your data.

To ship an updated word list, replace `server/db/dictionary.seed.db` (rebuild it with
`tools/build_db.py`), then `docker compose down -v && docker compose up -d --build`.

## API

| Method | Route | Notes |
| --- | --- | --- |
| `GET` | `/api/words?q=&pos=&category=&group=&offset=&limit=` | Paged search; prefix matches rank first |
| `GET` | `/api/words/:word` | One word plus its `isFavorite` flag |
| `GET` | `/api/random-word` | Random pick |
| `GET` | `/api/word-of-the-day` | Stable for the calendar day |
| `GET` | `/api/categories` | Categories nested under their group |
| `GET` | `/api/parts-of-speech` | With word counts |
| `GET` | `/api/favorites` | Newest first |
| `POST` | `/api/favorites/toggle` | `{ word }` |
| `GET` `POST` | `/api/learnings` | List/search (`?q=`), create |
| `GET` `PUT` `DELETE` | `/api/learnings/:id` | Read, rewrite, remove |
| `GET` `PUT` | `/api/settings/accent` | `en-GB` or `en-US` |

## Notable differences from the Android app

- **Pronunciation** uses the browser's Web Speech API instead of Android TTS. Voice
  availability varies by browser and OS; Chrome and Edge carry both accents, Firefox
  on Linux may have none installed.
- **Navigation** is a bottom bar under 900px and a top bar above it, rather than a
  fixed bottom bar.
- **Dark mode** follows the OS by default, with a manual toggle stored per browser.

## Layout

```
server/
  utils/db.ts        opens the SQLite file, seeds it, shared settings helpers
  api/               one file per endpoint
  db/                the read-only seed copied from the Android assets
app/
  pages/             one file per screen, mirroring the app's navigation graph
  components/        WordCard, WordListItem, SpeakButton, AppNav, AppIcon
  composables/       useSpeech (Web Speech API), useTheme (light/dark)
  assets/css/        Material 3 tokens ported from ui/theme/Color.kt
```
