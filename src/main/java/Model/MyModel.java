package Model;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import Server.Server;
import Server.ServerStrategyGenerateMaze;
import Server.ServerStrategySolveSearchProblem;
import Client.Client;
import Client.IClientStrategy;
import ViewModel.MyViewModel;
import IO.MyDecompressorInputStream;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Observer;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyModel extends Observable implements IModel {

    private Maze maze;
    private int playerRow;
    private int playerCol;
    private Solution solution;
    private Server mazeGeneratingServer;
    private Server solveSearchProblemServer;
    private ExecutorService modelThreadPool; // Thread pool for model operations

    // Hardcoded configuration values
    private static final int MAZE_GENERATING_SERVER_PORT = 5400;
    private static final int SOLVE_SEARCH_PROBLEM_SERVER_PORT = 5401;
    private static final int THREAD_POOL_SIZE = 3;
    private static final int DEFAULT_LISTENING_INTERVAL = 1000;


    public MyModel() {
        // Initialize servers with hardcoded ports and strategies
        mazeGeneratingServer = new Server(MAZE_GENERATING_SERVER_PORT, DEFAULT_LISTENING_INTERVAL, new ServerStrategyGenerateMaze());
        solveSearchProblemServer = new Server(SOLVE_SEARCH_PROBLEM_SERVER_PORT, DEFAULT_LISTENING_INTERVAL, new ServerStrategySolveSearchProblem());

        // Start servers in separate threads
        mazeGeneratingServer.start();
        solveSearchProblemServer.start();

        // Initialize a thread pool for model operations with hardcoded size
        modelThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    @Override
    public void assignObserver(Observer o) {
        this.addObserver(o);
    }

    @Override
    public void addObserver(MyViewModel o) {
        super.addObserver(o);
    }


    @Override
    public void generateMaze(int rows, int cols) {
        modelThreadPool.execute(() -> {
            try {
                Client mazeClient = new Client(InetAddress.getLocalHost(), MAZE_GENERATING_SERVER_PORT, new IClientStrategy() {
                    @Override
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            ObjectOutputStream objectOut = new ObjectOutputStream(outToServer);
                            objectOut.writeObject(new int[]{rows, cols}); // Send maze dimensions to server
                            objectOut.flush();

                            ObjectInputStream objectIn = new ObjectInputStream(inFromServer);
                            byte[] compressedMaze = (byte[]) objectIn.readObject(); // Receive compressed maze bytes

                            // The size calculation needs to be precise for decompression
                            int expectedSize = rows * cols + 12; // Maze data + 4 ints (rows, cols, startR, startC, goalR, goalC)
                            byte[] decompressedMaze = new byte[expectedSize];
                            MyDecompressorInputStream decompressionStream =
                                    new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                            decompressionStream.read(decompressedMaze);
                            decompressionStream.close(); // Close the stream

                            maze = new Maze(decompressedMaze); // Create Maze object from decompressed data
                            playerRow = maze.getStartPosition().getRowIndex();
                            playerCol = maze.getStartPosition().getColumnIndex();
                            solution = null; // Clear any old solution

                            setChanged();
                            notifyObservers("maze generated");

                        } catch (Exception e) {
                            System.err.println("Client strategy for maze generation failed: " + e.getMessage());
                            e.printStackTrace(); // Print full stack trace for debugging
                            setChanged();
                            notifyObservers("error");
                        }
                    }
                });
                mazeClient.communicateWithServer();
            } catch (UnknownHostException e) {
                System.err.println("Unknown host for maze generation server: " + e.getMessage());
                e.printStackTrace(); // Print full stack trace for debugging
                setChanged();
                notifyObservers("error");
            }
        });
    }

    @Override
    public Maze getMaze() {
        return maze;
    }

    @Override
    public void updatePlayerLocation(MovementDirection direction) {
        if (maze == null) {
            System.out.println("Cannot move: Maze not generated.");
            return;
        }

        int newRow = playerRow;
        int newCol = playerCol;

        switch (direction) {
            case UP -> newRow--;
            case DOWN -> newRow++;
            case LEFT -> newCol--;
            case RIGHT -> newCol++;
        }

        // Check if the new position is valid
        if (isValidMove(newRow, newCol)) {
            playerRow = newRow;
            playerCol = newCol;
            setChanged();
            notifyObservers("player moved");

            // Check for maze completion
            if (playerRow == maze.getGoalPosition().getRowIndex() && playerCol == maze.getGoalPosition().getColumnIndex()) {
                setChanged();
                notifyObservers("maze completed");
            }
        } else {
            System.out.println("Invalid move attempt to R:" + newRow + ", C:" + newCol);
        }
    }

    private boolean isValidMove(int row, int col) {
        return row >= 0 && row < maze.getRows() &&
                col >= 0 && col < maze.getColumns() &&
                maze.getGrid()[row][col] == 0; // 0 means path, 1 means wall
    }

    @Override
    public int getPlayerRow() {
        return playerRow;
    }

    @Override
    public int getPlayerCol() {
        return playerCol;
    }

    @Override
    public void solveMaze() {
        if (maze == null) {
            System.out.println("Cannot solve: Maze not generated.");
            return;
        }
        modelThreadPool.execute(() -> {
            try {
                Client solveClient = new Client(InetAddress.getLocalHost(), SOLVE_SEARCH_PROBLEM_SERVER_PORT, new IClientStrategy() {
                    @Override
                    public void clientStrategy(InputStream inFromServer, OutputStream outToServer) {
                        try {
                            ObjectOutputStream objectOut = new ObjectOutputStream(outToServer);
                            objectOut.writeObject(maze); // Send the maze to the server
                            objectOut.flush();

                            ObjectInputStream objectIn = new ObjectInputStream(inFromServer);
                            solution = (Solution) objectIn.readObject(); // Read the solution from the server
                            setChanged();
                            notifyObservers("maze solved");

                        } catch (Exception e) {
                            System.err.println("Client strategy for maze solving failed: " + e.getMessage());
                            e.printStackTrace(); // Print full stack trace for debugging
                            setChanged();
                            notifyObservers("error");
                        }
                    }
                });
                solveClient.communicateWithServer();
            }
            catch (UnknownHostException e) {
                System.err.println("Unknown host for solve search problem server: " + e.getMessage());
                e.printStackTrace(); // Print full stack trace for debugging
                setChanged();
                notifyObservers("error");
            }
        });
    }

    @Override
    public Solution getSolution() {
        return solution;
    }

    @Override
    public void clearSolution() {
        this.solution = null;
        setChanged();
        notifyObservers("solution cleared");
    }

    @Override
    public void saveMaze(String filePath) {
        if (maze == null) {
            System.out.println("No maze to save.");
            return;
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(maze);
            System.out.println("Maze saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to save maze: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            setChanged();
            notifyObservers("error");
        }
    }

    @Override
    public void loadMaze(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            maze = (Maze) ois.readObject();
            playerRow = maze.getStartPosition().getRowIndex();
            playerCol = maze.getStartPosition().getColumnIndex();
            solution = null; // Clear any old solution
            setChanged();
            notifyObservers("maze generated"); // Notify as if a new maze was generated
            System.out.println("Maze loaded from " + filePath);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load maze: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace for debugging
            setChanged();
            notifyObservers("error");
        }
    }

    @Override
    public void stopServers() {
        System.out.println("Stopping servers...");
        if (mazeGeneratingServer != null) {
            mazeGeneratingServer.stop();
        }
        if (solveSearchProblemServer != null) {
            solveSearchProblemServer.stop();
        }
        if (modelThreadPool != null) {
            modelThreadPool.shutdown();
            try {
                if (!modelThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    modelThreadPool.shutdownNow();
                    System.err.println("Model thread pool did not terminate in time.");
                }
            } catch (InterruptedException e) {
                modelThreadPool.shutdownNow();
                Thread.currentThread().interrupt();
                System.err.println("Model thread pool termination interrupted.");
                e.printStackTrace(); // Print full stack trace for debugging
            }
        }
        System.out.println("Servers and thread pool stopped.");
    }

    @Override
    public void update(Observable o, Object arg) {
        // This method is called when the Model changes.
        // Forward the notification to the View.
        setChanged();
        notifyObservers(arg);
    }
}
