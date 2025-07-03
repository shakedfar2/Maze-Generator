package ViewModel;

import Model.IModel;
import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.Solution;
import javafx.scene.input.KeyEvent;
import Model.MovementDirection;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyViewModel extends Observable implements Observer {

    private IModel model;
    private ExecutorService executor;

    public MyViewModel(IModel model) {
        this.model = model;
        this.model.addObserver(this);
        this.executor = Executors.newFixedThreadPool(2);
    }

    public void generateMaze(int rows, int cols) {
        executor.execute(() -> {
            model.generateMaze(rows, cols);
        });
    }

    public Maze getMaze() {
        return model.getMaze();
    }

    public int getPlayerRow() {
        return model.getPlayerRow();
    }

    public int getPlayerCol() {
        return model.getPlayerCol();
    }

    public Position getCurrentPosition() {
        return new Position(model.getPlayerRow(), model.getPlayerCol());
    }

    public int[] getGoalPosition() {
        Position goal = model.getMaze().getGoalPosition();
        return new int[]{goal.getRowIndex(), goal.getColumnIndex()};
    }


    public void movePlayer(KeyEvent event) {
        final MovementDirection direction;

        switch (event.getCode()) {
            case NUMPAD8, UP -> direction = MovementDirection.UP;
            case NUMPAD2, DOWN -> direction = MovementDirection.DOWN;
            case NUMPAD4, LEFT -> direction = MovementDirection.LEFT;
            case NUMPAD6, RIGHT -> direction = MovementDirection.RIGHT;
            case CONTROL -> {
                return;
            }
            default -> direction = null; // Ensure 'direction' is always initialized
        }

        if (direction != null) { // Only call if a valid direction was found
            executor.execute(() -> { // Execute movement in the thread pool
                model.updatePlayerLocation(direction); // Call the new method in the model
            });
        }
    }


    public void solveMaze() {
        executor.execute(() -> {
            model.solveMaze();
        });
    }

    public Solution getSolution() {
        return model.getSolution();
    }

    public void clearSolution() {
        executor.execute(() -> {
            model.clearSolution();
        });
    }

    public void saveMaze(String filePath) {
        executor.execute(() -> {
            model.saveMaze(filePath);
        });
    }

    public void loadMaze(String filePath) {
        executor.execute(() -> {
            model.loadMaze(filePath);
        });
    }

    public void stopServers() {
        executor.execute(() -> {
            model.stopServers();
            executor.shutdown(); // Shutdown the executor when servers stop
        });
    }

    @Override
    public void update(Observable o, Object arg) {
        // This method is called when the Model changes.
        // Forward the notification to the View.
        setChanged();
        notifyObservers(arg);
    }
}
