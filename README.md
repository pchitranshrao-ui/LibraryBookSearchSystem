# Atlas Library — Book Search System

A Java 17+ Swing desktop app demonstrating array-based book management, searching, sorting, and issue/return operations. Data is stored locally in `data/books.csv`.

## Features
- Dashboard counts, editable book catalog, duplicate ID and field validation.
- Linear search and binary search by ID, title, or author. Binary search sorts a copied array by the selected key first.
- Bubble, selection, and insertion sort by ID, title, author, or publication year.
- Step-by-step sorting and searching visualizations with comparison highlights, counters, pause/resume/reset, and adjustable playback speed. Traces are generated from algorithm execution; snapshots are bounded to keep memory use reasonable.
- Issue/return validation, availability filters, CSV autosave via temporary file and atomic replacement.
- Algorithm complexity notes and measured comparison/move counts.

## Algorithms
All catalog algorithm operations use manually implemented loops. Search/sort timing uses `System.nanoTime()` and is indicative; JVM warmup and system load affect results.

| Algorithm | Best | Average | Worst |
|---|---:|---:|---:|
| Linear search | O(1) | O(n) | O(n) |
| Binary search | O(1) | O(log n) | O(log n) |
| Bubble sort (early exit) | O(n) | O(n²) | O(n²) |
| Selection sort | O(n²) | O(n²) | O(n²) |
| Insertion sort | O(n) | O(n²) | O(n²) |

## Run
From this project directory:

```sh
javac -d out src/library/Main.java src/library/model/Book.java src/library/algorithms/Algorithms.java src/library/storage/BookStore.java src/library/ui/LibraryFrame.java
java -cp out library.Main
```

The process reads and writes `data/books.csv` relative to the working directory. Java 17 or newer is recommended. No external dependencies.

## Run checks

```sh
javac -d out src/library/Main.java src/library/model/Book.java src/library/algorithms/Algorithms.java src/library/storage/BookStore.java src/library/ui/LibraryFrame.java tests/AlgorithmChecks.java
java -cp out library.AlgorithmChecks
```

## Structure
- `src/library/model` — Book model
- `src/library/algorithms` — manual search and sorting algorithms
- `src/library/storage` — CSV persistence
- `src/library/ui` — Swing application
- `data/books.csv` — sample library dataset
- `tests` — dependency-free algorithm and CSV persistence checks

## Limitations
CSV supports quoted commas and quotes; embedded line breaks are normalized to spaces. Malformed rows are reported as a load error instead of silently discarded. Execution timings are not benchmarking-grade. The UI is a local single-user app.
