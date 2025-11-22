# CW1-AI
This repository contains the full implementation for Coursework 1 of the Artificial Intelligence module.
It includes two main components:

### **1. Sudoku Solver (Java)**

A complete Sudoku solving system implemented using **Backtracking with Basic Constraint Pruning (BBCP)**.
The project includes:

* `SudokuSolver.java` — core backtracking + pruning algorithm
* `SudokuGUI.java` — Java Swing graphical interface
* `demo.java` — simplified demo version of the solver

The solver supports real-time visualization, CSV/TXT puzzle loading, and displays runtime metrics such as execution time, nodes visited, and backtracking count.

Note: When loading a puzzle from file, each cell must be separated by commas or whitespace.
Empty cells may be represented using 0 or a single space, but the cell must not be left blank.
For CSV files, commas are inserted automatically between columns, so only the values or spaces for each cell need to be provided.

---

### **2. Lunar Mission Planning (PDDL)**

A set of PDDL domain and problem files modelling an **ESA-style lunar rover mission**.
It includes:

* `domain.pddl` — base lunar rover domain used for Mission 1 and 2
* `domain-ext.pddl` — extended domain with astronauts operations used for Mission 3
* `mission1/2/3.pddl` — three mission problem files

The domains and problems can be executed using planners **LAMA-first**.
