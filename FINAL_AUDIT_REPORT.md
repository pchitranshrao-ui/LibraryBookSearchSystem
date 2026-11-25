# Final Audit Report

## Project overview

Atlas Library is a Java Swing library catalog with local CSV persistence and array-based linear/binary search and bubble/selection/insertion sort. Algorithm trace snapshots drive the interactive visualization; the normal sort path does not build a visualization trace.

## Files inspected

Inspected all project source, test, data, and documentation files:

- `src/library/Main.java`
- `src/library/model/Book.java`
- `src/library/algorithms/Algorithms.java`
- `src/library/storage/BookStore.java`
- `src/library/ui/LibraryFrame.java`
- `tests/AlgorithmChecks.java`
- `data/books.csv`
- `README.md`
- `.gitignore`

The project uses only Java standard library APIs. No secrets or external service integrations were found. Compiled output is excluded by `.gitignore` and removed after verification.

## Compilation and launch

- Clean compilation with the installed JDK 25 succeeded.
- The actual Swing application launched from the documented command.
- The dashboard was visually inspected after launch. It showed 5 total books, 4 available, 1 issued, 4 categories, and all five sample table records.
- App shutdown/restart was partially checked by stopping and relaunching during this audit. A complete multi-cycle user workflow was not exercised.

## Algorithm and persistence checks

The dependency-free `library.AlgorithmChecks` suite passed. It exercises all three sort algorithms across all supported sort keys; empty, single, duplicate-key, reverse and larger inputs; first/last/missing/duplicate search cases for linear and binary search; special punctuation; trace outputs; exact counter cases; and CSV save/reload with quoted commas and quotes. A malformed CSV record is rejected with a line-specific error instead of silently discarded. A missing CSV file loads as an empty catalog.

The larger sort test initially exposed an out-of-memory issue because normal sorting generated a full trace. Normal sorting and visualization tracing are now separate paths, and trace snapshots are capped at 5,000 events.

## GUI review

The overview screen was inspected at its default window size after launch. It displays the expected catalog and counters. Other navigation buttons and screens are present in source, and the sorting/search visualizer controls are wired to Swing timers and worker-generated traces. Concurrent desktop input prevented completing a reliable button-by-button GUI interaction audit. The requested 1280x720, 1366x768, and 1920x1080 checks were not completed.

## Bugs discovered and fixed

| Severity | Finding | Resolution |
|---|---|---|
| High | Only the last-created table model was refreshed; earlier screens could show empty tables. | Added per-page table/model tracking and refreshed all page models. Visually verified the dashboard table after relaunch. |
| High | Standard sorting built and retained a full visualization trace, exhausting memory on larger input. | Normal sorting now executes without trace snapshots; visualization traces have a 5,000-event bound. |
| Medium | CSV parsing discarded malformed records and writing destroyed commas in fields. | Added quoted CSV parsing/writing, surfaced malformed input as an error, and protected the source CSV from overwrite after a load error. |
| Medium | Search and sort views did not show algorithm steps. | Added actual loop-derived compare/swap/shift/search checkpoints with counters and playback controls. Compilation and trace output are covered by the algorithm check suite. |
| Low | Feedback labels could update the wrong page. | Status labels are now tracked per page. |

## Test results

| Test Name | Expected Result | Actual Result | Status |
|---|---|---|---|
| Clean Java compilation | All application and test sources compile | Completed with exit code 0 | PASS |
| Sorting by ID, title, author, and year | Correct ordering and record count preserved | All three sorts passed across all keys | PASS |
| Empty, single, duplicate, reverse, and 500-book sort inputs | No crash; correct result | Passed | PASS |
| Search first, last, missing, duplicate-key, and empty cases | Correct match/no-match | Linear and binary search passed | PASS |
| Exact bubble, selection, and insertion counters | Expected comparisons and swaps/shifts | Counter assertions passed | PASS |
| Search and sorting trace outputs | Nonempty trace and correct final result | Passed for all sort algorithms and both search variants | PASS |
| CSV quoted field save and reload | Commas and quotes preserved after reopening | Passed using a fresh BookStore instance | PASS |
| Malformed CSV handling | Report invalid row rather than silently skip | Load threw an IOException with line number | PASS |
| Missing CSV handling | Empty catalog | Passed | PASS |
| Swing app startup | Main window opens | Native Java window observed | PASS |
| Dashboard population | Counts and all book rows visible | Visually confirmed after fix | PASS |
| Add/edit/delete and duplicate-ID dialogs | Correct data updates and validation | Not manually exercised | NOT TESTED |
| Issue/return actions and invalid transitions | Status changes and invalid action rejected | Not manually exercised | NOT TESTED |
| Search/sort visualizer pause/resume/reset/speed controls | Controls behave correctly in live UI | Not manually exercised | NOT TESTED |
| Responsive layout at requested resolutions | No overflow or inaccessible controls | Not checked at requested sizes | NOT TESTED |
| CSV persistence through full GUI mutation and app restart | Mutations survive restart | Storage roundtrip tested directly; GUI mutation/restart workflow not tested | NOT TESTED |
| GitHub publication | Push to authorized destination | No destination or remote was provided; no upload performed | NOT TESTED |

## Security and performance observations

- Storage is local and uses a fixed relative path (`data/books.csv`); book fields are not used to construct filesystem paths.
- No credentials, network calls, or third-party dependencies were found.
- CSV values are quoted/escaped; embedded line breaks are normalized to spaces.
- Algorithm tracing is prepared in a `SwingWorker`, and frame updates/playback use Swing's event thread and timer. The search/sort result operations themselves are in-memory.
- Trace detail is limited to 5,000 events to bound memory. Large traces may therefore show a final note that intermediate checkpoints were capped; the actual algorithm still completes and returns its result.

## Remaining limitations

- Full manual testing of every visible control and the requested multi-resolution layouts remains incomplete.
- Add/edit/delete, issue/return, duplicate-ID validation, and complete GUI persistence workflows need live interaction testing.
- Java 25 was used for compilation and execution; Java 17 compatibility was not independently run on a Java 17 installation.
- No GitHub remote was configured or provided. The local project is prepared for Git and can be pushed after choosing a destination.

## Run commands

From the project directory:

```sh
javac -d out src/library/Main.java src/library/model/Book.java src/library/algorithms/Algorithms.java src/library/storage/BookStore.java src/library/ui/LibraryFrame.java
java -cp out library.Main
```

Run the checks:

```sh
javac -d out src/library/Main.java src/library/model/Book.java src/library/algorithms/Algorithms.java src/library/storage/BookStore.java src/library/ui/LibraryFrame.java tests/AlgorithmChecks.java
java -cp out library.AlgorithmChecks
```

## GitHub readiness

README and `.gitignore` are present, the sample CSV is included, no remote repository was configured, and compiled output was removed. No push was attempted. Once a destination repository is selected, use:

```sh
git remote add origin <repository-url>
git push -u origin main
```
