# Sudoku Solver

### 📌 Overview

This project is a Java-based Sudoku solver that implements multiple constraint satisfaction problem (CSP) solving strategies.
It reads Sudoku puzzles from files, applies different algorithms, and writes results (solved boards + performance stats) to output files.

### 🏗️ Features

✅ Sudoku board representation with Cells, Domains, and Constraints

✅ Multiple solving algorithms:

* Backtracking

* Forward Checking (FC)

* AC-3 + Backtracking

* MRV (Minimum Remaining Values) + LCV (Least Constraining Value) Heuristic

* Simulated Annealing (experimental)

✅ Constraint propagation across rows, columns, and 3×3 subgrids

✅ Support for multiple difficulty levels (easy, medium, hard)

✅ Automatic performance tracking (execution time per algorithm)

✅ Writes solved puzzles to ./Results/ directory

### ⚙️ Technologies Used

1. Java (JDK 8+)
2. Object-Oriented Design (encapsulation of Cell, Sudoku, Algorithms)
3. Constraint Satisfaction Problem techniques (MRV, LCV, AC-3)
4. File I/O for reading boards and writing results

### 📂 Project Structure

    src/
    │── Cell.java               # Represents a Sudoku cell with value, domain, constraints
    │── CellPosition.java       # Row/column wrapper for cell position
    │── Sudoku.java             # Board representation + helper methods
    │── SudokuAlgorithms.java   # Algorithms (Backtracking, FC, AC-3, Heuristics, SA)
    │── boardfiles/             # Input Sudoku puzzles (e.g., easy1, medium2, hard3)
    │── Results/                # Output solved boards + runtime statistics

### 🗄️ Input Format

Each Sudoku puzzle is stored in a text file under boardfiles/ with the following rules:

9 lines, each containing 9 digits (0 = empty cell, 1–9 = pre-filled value).

Example (easy1):

    530070000
    600195000
    098000060
    800060003
    400803001
    700020006
    060000280
    000419005
    000080079

### ▶️ Running the Application

Compile the project:

    javac *.java

Run the program:

    java SudokuAlgorithms

The solver will:

* Load puzzles from boardfiles/ (easy, medium, hard, each with 5 puzzles).
* Solve each using 5 algorithms.
* Print results and execution times to console.
* Save final board states in ./Results/<level><number>.txt.

### 🌐 Algorithms Implemented

* Backtracking → Basic depth-first search with constraint checking.
* Forward Checking → Eliminates conflicting values from neighbors before recursion.
* AC-3 (Arc Consistency) → Preprocessing step that prunes domains.
* Heuristic (MRV + LCV) → Selects the most constrained variable and least constraining value.
* Simulated Annealing (SA) → Local search method (experimental).

### 📊 Example Output

Console output (snippet):

    easy1    true --> 15   true --> 5   true --> 3   true --> 4   false --> 20
    easy2    true --> 17   true --> 7   true --> 4   true --> 6   false --> 19


Where:

* true/false = solved or not
* Number = execution time (ms)

Solved board is also saved in ./Results/.
