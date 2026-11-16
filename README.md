# Charger

An Android app that walks you through a **legal adult name change in Nebraska**, one step at a time, and fills in the official Nebraska court forms for you.

## Install

1. On your phone, open the [latest release](../../releases/latest).
2. Download **Charger.apk** and open it. Allow installs from your browser if asked.
3. New builds install over the old one and keep your progress (fixed signing key in `signing/`).

## What it does

- **One next step at a time.** The home screen shows only what to do now.
- **Fills the real court forms** (bundled from the Nebraska Judicial Branch):
  Petition (DC 6:9.1), Confidential Party Information (DC 6:9.4), Legal Notice (DC 6:9.2),
  Decree (DC 6:9.3), Waiver of Publication request (DC 6:9.5), Affidavit of Mailing (DC 6:6.8).
- **Works out your dates**: earliest hearing (about 6 weeks after filing), when the newspaper notice
  must run (once a week, 2 weeks in a row, before the hearing), and the 5-day / 10-day mailing deadlines.
- **Hearing script** in big text, word for word, with the screen kept on.
- **After the decree**: certified copies and a checklist for Social Security, DMV (60 days), passport,
  birth certificate, voter registration, work, bank and more.
- Lancaster County (Lincoln) clerk address and phone built in; other counties link to the court directory.

## Privacy

No internet permission. Your answers stay on the phone; a form leaves only when you tap **Send**.

## Not legal advice

Built from the Nebraska Judicial Branch adult name change guides (rev. 07/2026), Neb. Rev. Stat. § 25-21,271,
and the court fee schedule (effective July 1, 2026). Rules, fees and local practice can change. Check with the
clerk of the district court in your county.

## Build

GitHub Actions builds on every push to `main` (`.github/workflows/build.yml`), runs the unit tests
(including filling every bundled form and checking every field still exists), and publishes `Charger.apk`
to the `latest` release.
