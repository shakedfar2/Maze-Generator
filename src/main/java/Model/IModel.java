package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;

import java.util.Observable;
import java.util.Observer; // For assignObserver(Observer o)
import ViewModel.MyViewModel; // For addObserver(MyViewModel o)

public interface IModel {
    /**
     * Assigns a general observer to the model.
     * @param o The observer to register.
     */
    void assignObserver(Observer o);

    /**
     * Assigns a specific MyViewModel observer to the model.
     * This is needed if the IModel interface explicitly requires it.
     * @param o The MyViewModel observer to register.
     */
    void addObserver(MyViewModel o); // Specific for MyViewModel

    /**
     * Generates a new maze with the specified dimensions.
     * @param rows The number of rows for the maze.
     * @param cols The number of columns for the maze.
     */
    void generateMaze(int rows, int cols);

    /**
     * Returns the currently generated maze.
     * @return The Maze object.
     */
    Maze getMaze();

    /**
     * Updates the player's location based on the given direction.
     * @param direction The direction of movement.
     */
    void updatePlayerLocation(MovementDirection direction);

    /**
     * Returns the current row index of the player.
     * @return The player's row.
     */
    int getPlayerRow();

    /**
     * Returns the current column index of the player.
     * @return The player's column.
     */
    int getPlayerCol();

    /**
     * Solves the current maze and provides the solution.
     */
    void solveMaze();

    /**
     * Returns the solution to the current maze.
     * @return The Solution object.
     */
    Solution getSolution();

    /**
     * Clears the displayed solution.
     */
    void clearSolution();

    /**
     * Saves the current maze to the specified file path.
     * @param filePath The path to the file where the maze will be saved.
     */
    void saveMaze(String filePath);

    /**
     * Loads a maze from the specified file path.
     * @param filePath The path to the file from which the maze will be loaded.
     */
    void loadMaze(String filePath);

    /**
     * Stops any running servers and releases resources.
     */
    void stopServers();

    void update(Observable o, Object arg);
}
