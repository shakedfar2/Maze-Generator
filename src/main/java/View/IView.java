package View;

import algorithms.mazeGenerators.Maze;
import algorithms.search.Solution;
import ViewModel.MyViewModel;

public interface IView {
    void displayMaze(Maze maze);
    void setViewModel(MyViewModel viewModel);
    void displayError(String message);
    void bindKeys();
    void updatePlayerPosition(int row, int col);
    void displaySolution(Solution solution);
}
