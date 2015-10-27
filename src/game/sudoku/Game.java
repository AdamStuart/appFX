package game.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

/**
 * This class represents a Sudoku game. It contains the solution, the user
 * input, the selected number and methods to check the validation of the user
 * input.
 *
 * @author Eric Beijer
 */
public class Game extends Observable {
    private int[][] solution;       // Generated solution.
    private int[][] values;           // Generated game with user input.
    private boolean[][] check;      // Holder for checking validity of game.
    private int selectedNumber;     // Selected number by user.
    private boolean help;           // Help turned on or off.

    /**
     * Constructor
     */
    public Game() {
        newGame();
        check = new boolean[9][9];
        help = true;
    }

    /**
     * Generates a new Sudoku game.<br />
     * All observers will be notified, update action: new game.
     */
    public void newGame() {
        solution = generateSolution(new int[9][9], 0);
        dump(solution);
        values = generateGame(copy(solution));
        dump(values);
        setChanged();
//        notifyObservers(UpdateAction.NEW_GAME);
    }

    /**
     * Checks user input agains the solution and puts it into a check matrix.<br />
     * All observers will be notified, update action: check.
     */
    public void checkGame() {
        selectedNumber = 0;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++)
                check[y][x] = values[y][x] == solution[y][x];
        }
        setChanged();
//        notifyObservers(UpdateAction.CHECK);
    }

    /**
     * Sets help turned on or off.<br />
     * All observers will be notified, update action: help.
     * 
     * @param help True for help on, false for help off.
     */
    public void setHelp(boolean help) {
        this.help = help;
        setChanged();
//        notifyObservers(UpdateAction.HELP);
    }

    /**
     * Sets selected number to user input.<br />
     * All observers will be notified, update action: selected number.
     *
     * @param selectedNumber    Number selected by user.
     */
    public void setSelectedNumber(int selectedNumber) {
        this.selectedNumber = selectedNumber;
        setChanged();
//        notifyObservers(UpdateAction.SELECTED_NUMBER);
    }

    /**
     * Returns number selected user.
     *
     * @return  Number selected by user.
     */
    public int getSelectedNumber() {
        return selectedNumber;
    }

    /**
     * Returns whether help is turned on or off.
     *
     * @return True if help is turned on, false if help is turned off.
     */
    public boolean isHelp() {
        return help;
    }

    /**
     * Returns whether selected number is candidate at given position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      True if selected number on given position is candidate,
     *              false otherwise.
     */
    public boolean isSelectedNumberCandidate(int x, int y) {
        return values[y][x] == 0 && isPossibleX(values, y, selectedNumber)
                && isPossibleY(values, x, selectedNumber) && isPossibleBlock(values, x, y, selectedNumber);
    }

    /**
     * Sets given number on given position in the game.
     *
     * @param x         The x position in the game.
     * @param y         The y position in the game.
     * @param number    The number to be set.
     */
    public void setNumber(int x, int y, int number) {
        values[y][x] = number;
    }

    /**
     * Returns number of given position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      Number of given position.
     */
    public int getNumber(int x, int y) {
        return values[y][x];
    }

    /**
     * Returns the correct number for this position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      Solution of given position at this spot.
     */
    public int getSolution(int x, int y) {
        return solution[y][x];
    }

    /**
     * Solves the puzzle by copying the solution into the values.
     *
     */
  
    public void solve()
    {
        for (int y = 0; y < 9; y++) 
            for (int x = 0; x < 9; x++)
            	values[y][x] = solution[y][x];
    }
    /**
     * Returns whether user input is valid of given position.
     *
     * @param x     X position in game.
     * @param y     Y position in game.
     * @return      True if user input of given position is valid, false
     *              otherwise.
     */
    public boolean isCheckValid(int x, int y) {
        return check[y][x];
    }

    /**
     * Returns whether given number is candidate on x axis for given game.
     *
     * @param game      Game to check.
     * @param y         Position of x axis to check.
     * @param number    Number to check.
     * @return          True if number is candidate on x axis, false otherwise.
     */
    private boolean isPossibleX(int[][] game, int y, int number) {
        for (int x = 0; x < 9; x++) {
            if (game[y][x] == number)
                return false;
        }
        return true;
    }

    /**
     * Returns whether given number is candidate on y axis for given game.
     *
     * @param game      Game to check.
     * @param x         Position of y axis to check.
     * @param number    Number to check.
     * @return          True if number is candidate on y axis, false otherwise.
     */
    private boolean isPossibleY(int[][] game, int x, int number) {
        for (int y = 0; y < 9; y++) {
            if (game[y][x] == number)
                return false;
        }
        return true;
    }

    /**
     * Returns whether given number is candidate in block for given game.
     *
     * @param game      Game to check.
     * @param x         Position of number on x axis in game to check.
     * @param y         Position of number on y axis in game to check.
     * @param number    Number to check.
     * @return          True if number is candidate in block, false otherwise.
     */
    private boolean isPossibleBlock(int[][] game, int x, int y, int number) {
        int x1 = x < 3 ? 0 : x < 6 ? 3 : 6;
        int y1 = y < 3 ? 0 : y < 6 ? 3 : 6;
        for (int yy = y1; yy < y1 + 3; yy++) {
            for (int xx = x1; xx < x1 + 3; xx++) {
                if (game[yy][xx] == number)
                    return false;
            }
        }
        return true;
    }

    /**
     * Returns next possible number from list for given position or -1 when list
     * is empty.
     *
     * @param game      Game to check.
     * @param x         X position in game.
     * @param y         Y position in game.
     * @param numbers   List of remaining numbers.
     * @return          Next possible number for position in game or -1 when
     *                  list is empty.
     */
    private int getNextPossibleNumber(int[][] game, int x, int y, List<Integer> numbers) {
        while (numbers.size() > 0) {
            int number = numbers.remove(0);
            if (isPossibleX(game, y, number) && isPossibleY(game, x, number) && isPossibleBlock(game, x, y, number))
                return number;
        }
        return -1;
    }

    /**
     * Generates Sudoku game solution.
     *
     * @param vals      Game to fill, user should pass 'new int[9][9]'.
     * @param index     Current index, user should pass 0.
     * @return          Sudoku game solution.
     */
    private int[][] generateSolution(int[][] vals, int index) {
        if (index > 80)
            return vals;

        int x = index % 9;
        int y = index / 9;

        List<Integer> numbers = new ArrayList<Integer>();
        for (int i = 1; i <= 9; i++) numbers.add(i);
        Collections.shuffle(numbers);

        while (numbers.size() > 0) {
            int number = getNextPossibleNumber(vals, x, y, numbers);
            if (number == -1)
                return null;

            vals[y][x] = number;
            int[][] tmpGame = generateSolution(vals, index + 1);
            if (tmpGame != null)
                return tmpGame;
            vals[y][x] = 0;
        }

        return null;
    }

    /**
     * Generates Sudoku game from solution.
     *
     * @param game      Game to be generated, user should pass a solution.
     * @return          Generated Sudoku game.
     */
    private int[][] generateGame(int[][] game) {
        List<Integer> positions = new ArrayList<Integer>();
        for (int i = 0; i < 81; i++)
            positions.add(i);
        Collections.shuffle(positions);
        return generateGame(game, positions);
    }

    /**
     * Generates Sudoku game from solution, user should use the other
     * generateGame method. This method simply removes a number at a position.
     * If the game isn't anymore valid after this action, the game will be
     * brought back to previous state.
     *
     * @param vals          Game to be generated.
     * @param positions     List of remaining positions to clear.
     * @return              Generated Sudoku game.
     */
    private int[][] generateGame(int[][] vals, List<Integer> positions) {
        while (positions.size() > 0) {
            int position = positions.remove(0);
            int x = position % 9;
            int y = position / 9;
            int temp = vals[y][x];
            vals[y][x] = 0;

            if (!isValid(vals))
                vals[y][x] = temp;
        }

        return vals;
    }

    /**
     * Checks whether given game is valid.
     *
     * @param game      Game to check.
     * @return          True if game is valid, false otherwise.
     */
    private boolean isValid(int[][] game) {
        return isValid(game, 0, new int[] { 0 });
    }

    /**
     * Checks whether given game is valid, user should use the other isValid
     * method. There may only be one solution.
     *
     * @param game                  Game to check.
     * @param index                 Current index to check.
     * @param numberOfSolutions     Number of found solutions. Int[] instead of
     *                              int because of pass by reference.
     * @return                      True if game is valid, false otherwise.
     */
    private boolean isValid(int[][] game, int index, int[] numberOfSolutions) {
        if (index > 80)
            return ++numberOfSolutions[0] == 1;

        int x = index % 9;
        int y = index / 9;

        if (game[y][x] == 0) {
            List<Integer> numbers = new ArrayList<Integer>();
            for (int i = 1; i <= 9; i++)
                numbers.add(i);

            while (numbers.size() > 0) {
                int number = getNextPossibleNumber(game, x, y, numbers);
                if (number == -1)
                    break;
                game[y][x] = number;

                if (!isValid(game, index + 1, numberOfSolutions)) {
                    game[y][x] = 0;
                    return false;
                }
                game[y][x] = 0;
            }
        } else if (!isValid(game, index + 1, numberOfSolutions))
            return false;

        return true;
    }

    /**
     * Copies a game.
     *
     * @param game      Game to be copied.
     * @return          Copy of given game.
     */
    private int[][] copy(int[][] game) {
        int[][] copy = new int[9][9];
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++)
                copy[y][x] = game[y][x];
        }
        return copy;
    }
    /**
     * Prints given game to console. Used for debug.
     *
     */
    
    public void dump( int ints[][]) {
        System.out.println();
        if (ints == null) ints = values;
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++)
                System.out.print(" " + ints[y][x]);
            System.out.println();
        }
    }

	public boolean allFilled(int targ)
	{
		int ct = 0;
		for (int y = 0; y < 9; y++) 
			for (int x = 0; x < 9; x++)
		    		if (targ == values[y][x])
		    			ct++;
		
		System.out.println("filled: " + targ + " = " + ct);
		return ct == 9;
	}
}