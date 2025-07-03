package View;

import ViewModel.MyViewModel;
import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.ResourceBundle;

public class MyViewController implements IView, Observer, javafx.fxml.Initializable {

    private MyViewModel viewModel;
    private Stage primaryStage;
    private Main mainApp;

    @FXML
    private TextField rowsField;
    @FXML
    private TextField colsField;

    @FXML
    private MazeDisplayer mazeCanvas;

    private MediaPlayer winSoundPlayer;


    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }
    @FXML
    private Button solveButton;

    @FXML
    private Button unsolveButton;

    @FXML
    public void generateMazeClicked() {
        try {
            String rowsText = rowsField.getText();
            String colsText = colsField.getText();

            if (rowsText == null || rowsText.isEmpty() || colsText == null || colsText.isEmpty()) {
                displayError("Please enter values for both rows and columns.");
                return;
            }

            int rows = Integer.parseInt(rowsText);
            int cols = Integer.parseInt(colsText);

            if (rows <= 0 || cols <= 0) {
                displayError("Rows and columns must be positive numbers.");
                return;
            }

            viewModel.generateMaze(rows, cols);
            solveButton.setDisable(false);
            unsolveButton.setDisable(false);
            mazeCanvas.requestFocus();


            // When generating a new maze, stop any existing confetti animation in MazeDisplayer
            if (mazeCanvas != null) {
                mazeCanvas.stopConfetti();
            }
            if (mainApp != null) {
                mainApp.startGameMusic();
            }

        } catch (NumberFormatException e) {
            displayError("Please enter only numbers for rows and columns.");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bindKeys();

        try {
            mazeCanvas.setWallImage(getClass().getResource("/images/wall.jpg"));
        } catch (NullPointerException e) {
            System.err.println("Could not load wall image.");
            mazeCanvas.setWallImage(null);
        }
        try {
            mazeCanvas.setPlayerImage(getClass().getResource("/images/player.jpg"));
        } catch (NullPointerException e) {
            System.err.println("Could not load player image.");
            mazeCanvas.setPlayerImage(null);
        }

        mazeCanvas.setSolutionDrawer((gc, state) -> {
            if (state instanceof algorithms.search.MazeState mazeState) {
                if (viewModel.getMaze() != null) {
                    double cellHeight = mazeCanvas.getHeight() / viewModel.getMaze().getRows();
                    double cellWidth = mazeCanvas.getWidth() / viewModel.getMaze().getColumns();
                    gc.setFill(javafx.scene.paint.Color.YELLOW.deriveColor(1, 1, 1, 0.7));
                    gc.fillRect(mazeState.getPosition().getColumnIndex() * cellWidth, mazeState.getPosition().getRowIndex() * cellHeight, cellWidth, cellHeight);
                }
            }
        });

        mazeCanvas.setOnMouseClicked(event -> mazeCanvas.requestFocus());
    }

    @Override
    public void displayMaze(Maze maze) {
        if (mazeCanvas != null) {
            mazeCanvas.setMaze(maze);
            mazeCanvas.setPlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol());
            mazeCanvas.draw();
        }
    }

    @Override
    public void updatePlayerPosition(int row, int col) {
        if (mazeCanvas != null) {
            mazeCanvas.setPlayerPosition(row, col);
            mazeCanvas.draw();
        }
    }

    @Override
    public void displaySolution(Solution solution) {
        if (mazeCanvas != null) {
            mazeCanvas.setSolution(solution);
            mazeCanvas.draw();
        }
    }

    @Override
    public void bindKeys() {
        mazeCanvas.setOnKeyPressed(this::handleKeyPressed);
        mazeCanvas.setFocusTraversable(true);
    }

    private void handleKeyPressed(KeyEvent event) {
        if (viewModel != null) {
            viewModel.movePlayer(event);
            Platform.runLater(() -> mazeCanvas.requestFocus());
        }
    }

    public void displayError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Error");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void displayInformation(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    @FXML
    public void solveMazeClicked() {
        if (viewModel.getMaze() == null) {
            displayError("Please generate a maze first.");
            return;
        }
        viewModel.solveMaze();
        mazeCanvas.requestFocus();
    }

    @FXML
    public void unsolveMazeClicked() {
        if (viewModel.getMaze() == null) {
            displayError("No maze generated to unsolve.");
            return;
        }
        viewModel.clearSolution();
        mazeCanvas.requestFocus();
    }


    @Override
    public void update(Observable o, Object arg) {
        String change = (String) arg;
        System.out.println("DEBUG (ViewController): Received notification: " + change);

        Platform.runLater(() -> {
            if (change == null) {
                System.err.println("ViewController update received null argument.");
                displayError("An unexpected error occurred: Null notification.");
                return;
            }

            switch (change) {
                case "maze generated":
                    System.out.println("DEBUG (ViewController): Handling 'maze generated'.");
                    Maze maze = viewModel.getMaze();
                    if (mazeCanvas != null) {
                        displayMaze(maze);
                    } else {
                        System.err.println("ERROR: mazeCanvas is null when handling 'maze generated'.");
                    }
                    break;

                case "player moved":
                    System.out.println("DEBUG (ViewController): Handling 'player moved'.");
                    updatePlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol());
                    break;

                case "maze solved":
                    System.out.println("DEBUG (ViewController): Handling 'maze solved'.");
                    displaySolution(viewModel.getSolution());
                    displayInformation("Maze Solved!", "A solution path has been calculated.");
                    break;

                case "solution cleared":
                    System.out.println("DEBUG (ViewController): Handling 'solution cleared'.");
                    if (mazeCanvas != null) {
                        mazeCanvas.setSolution(null); // Clear the solution from MazeDisplayer
                        mazeCanvas.draw(); // Redraw the maze without the solution
                    }
                    displayInformation("Solution Cleared", "The displayed solution has been removed.");
                    break;

                case "maze completed":
                    System.out.println("DEBUG (ViewController): Handling 'maze completed'. Initiating celebration.");
                    if (mazeCanvas != null) {
                        mazeCanvas.setPlayerPosition(viewModel.getPlayerRow(), viewModel.getPlayerCol());
                        mazeCanvas.draw();
                        mazeCanvas.startConfetti();
                    }
                    displayInformation("Congratulations!", "You have completed the maze!");

                    if (mainApp != null) {
                        System.out.println("DEBUG (ViewController): Playing victory music via Main app.");
                        mainApp.playVictoryMusic(() -> {
                            mainApp.showWelcomeScene();
                        });
                    }
                    break;

                case "error":
                    System.err.println("DEBUG (ViewController): Handling 'error' notification.");
                    displayError("An error occurred during a model operation. Check console for details.");
                    break;

                default:
                    System.out.println("DEBUG (ViewController): Unknown notification: " + change);
                    break;
            }
        });
    }


    @FXML
    public void saveMazeClicked() {
        if (viewModel.getMaze() == null) {
            displayError("No maze to save. Please generate a maze first.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Maze");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Maze Files", "*.maze"));
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            viewModel.saveMaze(file.getAbsolutePath());
            displayInformation("Maze Saved", "Maze saved successfully to: " + file.getAbsolutePath());
        }
        mazeCanvas.requestFocus();
    }

    @FXML
    public void loadMazeClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Maze");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Maze Files", "*.maze"));
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            viewModel.loadMaze(file.getAbsolutePath());
            mazeCanvas.requestFocus();
        }
    }

    @FXML
    public void exitApp() {
        exitApplicationConfirmation();
    }

    private void exitApplicationConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Exit Application");
        alert.setContentText("Are you sure you want to exit?");
        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                if (viewModel != null) {
                    viewModel.stopServers();
                }
                if (mainApp != null) {
                    mainApp.startGameMusic();
                }
                if (mazeCanvas != null) {
                    mazeCanvas.stopConfetti();
                }
                Platform.exit();
                System.exit(0);
            }
        });
    }

    @FXML
    public void openProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            if (input == null) {
                displayError("Could not find config.properties in resources folder.");
                return;
            }
            properties.load(input);

            StringBuilder content = new StringBuilder("Current Configuration:\n\n");
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                content.append(key).append(": ").append(value).append("\n");
            }

            displayInformation("Application Properties", content.toString());

        } catch (IOException e) {
            displayError("Failed to load properties: " + e.getMessage());
        }
    }
    @FXML
    public void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("How to Play");

        String helpText = """
        Use arrow keys or numeric keypad (2, 4, 6, 8) to move your player.
        Click "Generate Maze" to create a new maze.
        Use "Solve Maze" to show the optimal path.
        Click "Unsolve Maze" to hide the solution path.
        Reach the green goal cell to complete the maze.
        You can save or load mazes via the File menu.
        Enjoy the game!
        """;

        javafx.scene.control.TextArea textArea = new javafx.scene.control.TextArea(helpText);
        textArea.setWrapText(true);
        textArea.setEditable(false);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    @FXML
    public void showAbout() {
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

        displayInformation("About Maze Game", content);
    }
}