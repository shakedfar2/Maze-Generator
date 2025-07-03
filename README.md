# Maze-Generator 🧩

A fully-featured maze generator and solver built with **JavaFX** and a **multi-threaded client-server architecture**.  
Designed using the **MVVM pattern**, this project allows users to generate, solve, and explore mazes with interactive GUI controls and smart search algorithms.

---

## 🚀 Features

- 🧱 Maze generation using efficient iterative algorithms
- 🔎 Maze solving using BFS, DFS, and Best-First Search
- 🧠 MVVM architecture for clean separation of logic and UI
- 🖱️ Full GUI with mouse & keyboard controls
- 📁 Save/load mazes from disk
- 🎵 Optional background music
- 🎮 Player character that can move inside the maze

---

## 🛠️ Technologies

- Java 24
- JavaFX (UI)
- Multi-threading (Client/Server)
- Maven 

---

## 📁 Project Structure
Maze-Generator/
├── libs/ # Contains compiled JAR dependencies
│ └── ATPProjectJAR.jar/
│ ├── algorithms/
│ ├── Client/
│ ├── IO/
│ ├── Server/
│ ├── test/
│ └── config.properties
│
├── out/ # Build artifacts (generated)
│ └── artifacts/
│ └── ATP_Project_PartC.jar
│
├── src/
│ └── main/
│ ├── java/
│ │ ├── Model/ # Maze logic, directions, model classes
│ │ ├── View/ # JavaFX components (Main, Controller, Displayer)
│ │ └── ViewModel/ # ViewModel logic (MVVM connection)
│ │
│ └── resources/
│ ├── images/ # Maze graphics: player, wall
│ ├── sounds/ # Background music & sound effects
│ ├── View/ # FXML layout files
│ └── config.properties
│
├── pom.xml # Maven configuration file
├── .gitignore # Git ignore rules
└── mvnw / mvnw.cmd / target # Maven wrapper & build output

