package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    private MyModel model;
    private MyViewModel viewModel;
    private Stage primaryStage;

    private MediaPlayer bgPlayer; // For game background music
    private MediaPlayer victoryPlayer;
    private MediaPlayer menuPlayer; // For main menu music

    private MyViewController gameViewController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("Main Application starting...");
        this.primaryStage = primaryStage; // Store primary stage
        primaryStage.setTitle("Maze Project"); // Set general title

        // Initialize model and ViewModel once
        model = new MyModel();
        viewModel = new MyViewModel(model); // Now viewModel is a field in Main

        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Consume the event to handle it manually
            // Use the gameViewController if it's initialized, otherwise handle directly
            if (gameViewController != null) {
                gameViewController.exitApp(); // This will show the confirmation dialog
            } else {
                // If gameViewController is not initialized
                // perform shutdown directly.
                if (model != null) {
                    model.stopServers();
                }
                stopAllMusic(); // Use the stopAllMusic method
                System.exit(0);
            }
        });

        // Load and show the Welcome Scene first
        showWelcomeScene();

        // Start menu music immediately
        playMenuMusic();

        primaryStage.show();
    }

    /**
     * Loads and displays the Welcome screen.
     */
    public void showWelcomeScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/WelcomeView.fxml"));
            Parent welcomeRoot = loader.load();

            WelcomeController welcomeController = loader.getController();
            welcomeController.setMainApp(this); // Pass reference to Main for scene switching

            Scene welcomeScene = new Scene(welcomeRoot, 600, 600);

            primaryStage.setScene(welcomeScene);
            primaryStage.setResizable(false);
            primaryStage.setTitle("Maze Project - Welcome");

            playMenuMusic();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading WelcomeView.fxml: " + e.getMessage());
        }
    }

    /**
     * Loads and displays the main Game screen.
     */
    public void showGameScene() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/View/MyView.fxml"));
            Parent gameRoot = fxmlLoader.load();

            // Store the gameViewController reference
            gameViewController = fxmlLoader.getController();
            gameViewController.setViewModel(viewModel); // Use the ViewModel initialized in Main
            gameViewController.setPrimaryStage(primaryStage);
            gameViewController.setMainApp(this); // Pass Main app reference

            // Ensure ViewModel observes ViewController
            viewModel.addObserver(gameViewController);

            Scene gameScene = new Scene(gameRoot, primaryStage.getWidth(), primaryStage.getHeight());

            primaryStage.setScene(gameScene);
            primaryStage.setResizable(true); // Game scene might be resizable
            primaryStage.setTitle("Maze Project - Game");

            // Set up key handling and focus for the game canvas
            // gameViewController.bindKeys();
            gameRoot.requestFocus();

            playStartMazeSound(); // Play the sound when starting a new game scene

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading MyView.fxml: " + e.getMessage());
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (model != null) {
            model.stopServers();
        }
        stopAllMusic(); // Ensure all music stops on application exit
        System.out.println("Application stopped and servers are shut down.");
    }

    // --- Music Control Methods ---

    // Helper to get Media URL from resource path
    private String getResourceMediaUrl(String path) {
        URL resourceUrl = getClass().getResource(path);
        if (resourceUrl == null) {
            System.err.println("Resource not found: " + path);
            return null;
        }
        return resourceUrl.toExternalForm();
    }

    public void playMenuMusic() {
        stopAllMusic(); // Stop any other music playing
        try {
            String url = getResourceMediaUrl("/sounds/mainMenu.mp3");
            if (url != null) {
                Media menuMusic = new Media(url);
                menuPlayer = new MediaPlayer(menuMusic);
                menuPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                menuPlayer.setVolume(0.5); // Set initial volume
                menuPlayer.play();
                System.out.println("Playing main menu music.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load main menu music: " + e.getMessage());
        }
    }

    public void stopMenuMusic() {
        if (menuPlayer != null) {
            menuPlayer.stop();
            menuPlayer.dispose(); // Release resources
            menuPlayer = null;
        }
    }

    public void startGameMusic() {
        stopAllMusic(); // Stop any other music playing
        try {
            String url = getResourceMediaUrl("/sounds/background.mp3");
            if (url != null) {
                Media bgMusic = new Media(url);
                bgPlayer = new MediaPlayer(bgMusic);
                bgPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                bgPlayer.setVolume(0.5); // Set initial volume
                bgPlayer.play();
                System.out.println("Playing game background music.");
            }
        } catch (Exception e) {
            System.err.println("Failed to load background music: " + e.getMessage());
        }
    }

    public void stopGameMusic() {
        if (bgPlayer != null) {
            bgPlayer.stop();
            bgPlayer.dispose(); // Release resources
            bgPlayer = null;
        }
    }
    public void playVictoryMusic(Runnable onFinish) {
        stopAllMusic();
        try {
            String url = getResourceMediaUrl("/sounds/victory.mp3");
            if (url != null) {
                Media victorySound = new Media(url);
                victoryPlayer = new MediaPlayer(victorySound);
                victoryPlayer.play();

                victoryPlayer.setOnEndOfMedia(() -> {
                    System.out.println("Victory music finished.");
                    if (onFinish != null) {
                        onFinish.run();
                    } else {
                        playMenuMusic();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Failed to play victory music: " + e.getMessage());
        }

    }


    public void playStartMazeSound() {
        try {
            String url = getResourceMediaUrl("/sounds/startMaze.mp3");
            if (url != null) {
                Media startSound = new Media(url);
                MediaPlayer startPlayer = new MediaPlayer(startSound); // Local player for one shot sound
                startPlayer.play();
                startPlayer.setOnEndOfMedia(startPlayer::dispose);
                System.out.println("Playing start maze sound.");
            }
        } catch (Exception e) {
            System.err.println("Failed to play start maze sound: " + e.getMessage());
        }
    }

    public void stopAllMusic() {
        if (bgPlayer != null) {
            bgPlayer.stop();
            bgPlayer.dispose();
            bgPlayer = null;
        }
        if (victoryPlayer != null) {
            victoryPlayer.stop();
            victoryPlayer.dispose();
            victoryPlayer = null;
        }
        if (menuPlayer != null) {
            menuPlayer.stop();
            menuPlayer.dispose();
            menuPlayer = null;
        }
        System.out.println("All music stopped.");
    }

    public void resumeBackgroundMusic() {
        if (bgPlayer != null && bgPlayer.getStatus() == MediaPlayer.Status.PAUSED) {
            bgPlayer.play();
        } else {
            startGameMusic(); // If not playing or paused, start it
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}