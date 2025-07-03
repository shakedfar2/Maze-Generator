package View;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import javafx.animation.AnimationTimer;

import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;


import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class MazeDisplayer extends Canvas implements Initializable {

    private Maze maze;
    private int playerRow;
    private int playerCol;
    private Solution solution;

    private Image wallImage;
    private Image playerImage;

    // Confetti related fields
    private List<ConfettiParticle> confettiParticles;
    private AnimationTimer confettiAnimationTimer;
    private Random confettiRandom;
    private long lastConfettiUpdate = 0;
    private boolean confettiActive = false;
    private final int MAX_CONFETTI_PARTICLES = 150;
    private final long CONFETTI_DURATION_NANOS = 5_000_000_000L; // 5 seconds
    private long confettiStartTime = 0;

    public MazeDisplayer() {
        // Initialize confetti related fields here
        confettiParticles = new ArrayList<>();
        confettiRandom = new Random();
        confettiAnimationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!confettiActive) {
                    stop(); // Stop the timer if confetti is no longer active
                    return;
                }

                if (confettiStartTime == 0) {
                    confettiStartTime = now;
                }

                if (lastConfettiUpdate == 0) {
                    lastConfettiUpdate = now;
                    return;
                }

                long elapsedNanos = now - confettiStartTime;
                if (elapsedNanos > CONFETTI_DURATION_NANOS) {
                    stopConfetti(); // Stop after duration
                    return;
                }

                double deltaTime = (now - lastConfettiUpdate) / 1_000_000_000.0; // seconds
                updateConfetti(deltaTime);
                draw(); // Redraw the entire canvas including confetti
                lastConfettiUpdate = now;

                // Add new particles periodically, but stop adding after some time or max particles reached
                if (confettiParticles.size() < MAX_CONFETTI_PARTICLES && confettiRandom.nextDouble() < 0.1) {
                    addConfettiParticle();
                }
            }
        };

        // Redraw everything when width or height changes
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
    }


    public Maze getMaze() {
        return maze;
    }

    public void setMaze(Maze maze) {
        this.maze = maze;
        this.solution = null;
        draw();
    }

    public int getPlayerRow() {
        return playerRow;
    }

    public int getPlayerCol() {
        return playerCol;
    }

    public void setPlayerPosition(int playerRow, int playerCol) {
        this.playerRow = playerRow;
        this.playerCol = playerCol;
        draw();
    }

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
        draw();
    }

    public void setWallImage(URL imageUrl) {
        try {
            this.wallImage = new Image(imageUrl.openStream());
        } catch (Exception e) {
            System.err.println("Failed to load wall image from URL: " + imageUrl);
            this.wallImage = null;
        }
    }

    public void setPlayerImage(URL imageUrl) {
        try {
            this.playerImage = new Image(imageUrl.openStream());
        } catch (Exception e) {
            System.err.println("Failed to load player image from URL: " + imageUrl);
            this.playerImage = null;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void draw() {
        if (maze == null) {
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight()); // Clear the canvas

        double cellHeight = getHeight() / maze.getRows();
        double cellWidth = getWidth() / maze.getColumns();

        // Draw maze walls
        for (int i = 0; i < maze.getRows(); i++) {
            for (int j = 0; j < maze.getColumns(); j++) {
                if (maze.getGrid()[i][j] == 1) { // It's a wall
                    if (wallImage != null) {
                        gc.drawImage(wallImage, j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                    } else {
                        gc.setFill(Color.GRAY);
                        gc.fillRect(j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                    }
                }
            }
        }

        // Draw solution (if exists)
        if (solution != null && solutionDrawer != null) {
            gc.save(); // Save current graphics context state
            // No cast needed here because MazeStateDrawer now accepts AState
            solution.getSolutionPath().forEach(aState -> solutionDrawer.draw(gc, aState));
            gc.restore(); // Restore to previous state
        }


        // Draw player
        if (playerImage != null) {
            gc.drawImage(playerImage, playerCol * cellWidth, playerRow * cellHeight, cellWidth, cellHeight);
        } else {
            gc.setFill(Color.BLUE);
            gc.fillOval(playerCol * cellWidth, playerRow * cellHeight, cellWidth, cellHeight);
        }

        // Draw goal position
        int goalRow = maze.getGoalPosition().getRowIndex();
        int goalCol = maze.getGoalPosition().getColumnIndex();
        gc.setFill(Color.GREEN.deriveColor(1, 1, 1, 0.5)); // Semi-transparent green
        gc.fillRect(goalCol * cellWidth, goalRow * cellHeight, cellWidth, cellHeight);


        // If confetti is active, draw it on top
        if (confettiActive) {
            // Draw confetti directly here, no separate drawConfetti() method needed
            for (ConfettiParticle particle : confettiParticles) {
                gc.save(); // Save context before applying transformations
                gc.translate(particle.getX() + particle.getSize() / 2, particle.getY() + particle.getSize() / 2); // Translate to center for rotation
                gc.rotate(particle.getRotation());
                gc.translate(-(particle.getSize() / 2), -(particle.getSize() / 2)); // Translate back
                gc.setFill(particle.getColor());
                gc.fillRect(0, 0, particle.getSize(), particle.getSize()); // Draw at 0,0 relative to translated context
                gc.restore(); // Restore context
            }
        }
    }


    private MazeStateDrawer solutionDrawer;

    public void setSolutionDrawer(MazeStateDrawer solutionDrawer) {
        this.solutionDrawer = solutionDrawer;
    }

    // Interface for drawing solution
    public interface MazeStateDrawer {
        void draw(GraphicsContext gc, algorithms.search.AState state);
    }


    public void startConfetti() {
        if (confettiActive) return;
        confettiActive = true;
        confettiParticles.clear();
        confettiStartTime = 0; // Reset timer start
        lastConfettiUpdate = 0; // Reset last update for delta time calculation

        for (int i = 0; i < 50; i++) { // Initial burst
            addConfettiParticle();
        }
        confettiAnimationTimer.start();
        draw(); // Redraw maze to ensure confetti appears
    }

    public void stopConfetti() {
        confettiActive = false;
        confettiAnimationTimer.stop();
        confettiParticles.clear();
        draw(); // Redraw maze to clear confetti
    }

    private void addConfettiParticle() {
        double startX = confettiRandom.nextDouble() * getWidth();
        double startY = -10; // Start slightly above the top
        double size = 5 + confettiRandom.nextDouble() * 10;
        Color color = generateRandomColor();
        double velocityX = (confettiRandom.nextDouble() * 200) - 100;
        double velocityY = 100 + confettiRandom.nextDouble() * 150;
        double rotationSpeed = (confettiRandom.nextDouble() * 360) - 180;

        confettiParticles.add(new ConfettiParticle(startX, startY, size, color, velocityX, velocityY, rotationSpeed));
    }

    private void updateConfetti(double deltaTime) {
        List<ConfettiParticle> particlesToRemove = new ArrayList<>();
        for (ConfettiParticle particle : confettiParticles) {
            particle.update(deltaTime, getHeight());
            if (particle.isOffScreen(getWidth(), getHeight(), 20)) {
                particlesToRemove.add(particle);
            }
        }
        confettiParticles.removeAll(particlesToRemove);
    }


    private Color generateRandomColor() {
        return Color.rgb(confettiRandom.nextInt(256), confettiRandom.nextInt(256), confettiRandom.nextInt(256));
    }

    private static class ConfettiParticle {
        private double x, y;
        private double size;
        private Color color;
        private double velocityX, velocityY;
        private double rotationSpeed;
        private double currentRotation; // in degrees

        public ConfettiParticle(double x, double y, double size, Color color,
                                double velocityX, double velocityY, double rotationSpeed) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.rotationSpeed = rotationSpeed;
            this.currentRotation = new Random().nextDouble() * 360; // Initial random rotation
        }

        public double getX() { return x; }
        public double getY() { return y; }
        public double getSize() { return size; }
        public Color getColor() { return color; }
        public double getRotation() { return currentRotation; }

        public void update(double deltaTime, double canvasHeight) {
            velocityY += 9.8 * 50 * deltaTime; // Adjust gravity strength

            x += velocityX * deltaTime;
            y += velocityY * deltaTime;

            currentRotation = (currentRotation + rotationSpeed * deltaTime) % 360;
        }

        public boolean isOffScreen(double canvasWidth, double canvasHeight, double buffer) {
            return y > canvasHeight + buffer || x < -buffer || x > canvasWidth + buffer;
        }

    }
}