package View;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.application.Platform;

public class WelcomeController {

    private Main mainApp; // Reference to the main application to switch scenes

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void startGame() {
        System.out.println("Start Game button clicked!");
        if (mainApp != null) {
            mainApp.showGameScene();
        } else {
            System.err.println("Main app reference is null. Cannot start game.");
        }
    }

    @FXML
    private void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("How to Play");
        alert.setContentText("""
            Use the arrow keys or numeric keypad (2, 4, 6, 8) to move your player.
            Click "Generate Maze" to create a new maze.
            Use "Solve Maze" to show the optimal path.
            Reach the green cell to win!
            """);
        alert.showAndWait();
    }

    @FXML
    private void showAbout() {
        String content = """
            Maze Game - ATP Project

            Developed by: Noa Lieber and Shaked Farjun

            Maze Generation Algorithm:
            - Iterative Backtracking (Depth-First Search with Stack)

            Maze Solving Algorithms:
            - Breadth First Search (BFS)
            - Depth First Search (DFS)
            - Best First Search (A* variant)

            Technologies:
            - JavaFX
            - MVVM Pattern
            - Socket-based client-server communication
            - Multithreading with ThreadPool
            - MediaPlayer for audio
            """;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("About Maze Game");
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void exitGame() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit");
        alert.setHeaderText("Exit Application");
        alert.setContentText("Are you sure you want to exit?");
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                if (mainApp != null) {
                    mainApp.stopAllMusic();
                }
                Platform.exit();
                System.exit(0);
            }
        });
    }
}