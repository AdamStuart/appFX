package life;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.scene.shape.Rectangle;

//Game of Life
//http://www.codeproject.com/Articles/1043443/The-Game-Of-Life-Advanced-Style-of-Programmation
/**
   * ComputeThread launches Master that returns the list of all live cells
   * on the grid. Next, update the grid on the view (GameOfLifeView)
   * 
   * @author Martella Massimiliano
   * @author JavaFX port by Adam Treister
   *
   */
  public class ComputeThread extends Thread {
 
  	private GameOfLifeView view;
  	private BooleanProperty stopFlag;
  	private IntegerProperty sleepTime;
  	private List<Rectangle> cells;

/**
* Main thread to check the status of the algorithm 
* 
* @param inView - the GameOfLifeView that displays the cells
* @param stopFlag -- a flag that must remain false for execution to continue
* @param sleeper-- number of milliseconds to wait as the thread is run
*/
  	public ComputeThread(GameOfLifeView inView, BooleanProperty flag, IntegerProperty sleeper) {
		view = inView;
		cells = inView.getLiveCells();
		stopFlag = flag;
		sleepTime = sleeper;
	}
//---------------------------------------------------------------------------------
	
	@Override public void run() {
		try {
			if (cells.isEmpty()) 			stopFlag.set(true);
			
			while (!stopFlag.getValue()) {
				Thread.sleep(sleepTime.get());
	            //The numbers of core in your pc
				int poolSize = Runtime.getRuntime().availableProcessors() + 1;
	
				/*
				 * master calculates the state of the cells. the
				 * result of all cells counted is saved in result
				 * (ArrayList < Rectangle >) that it is passed the  
				 * View to update the grid display 
				 */
				Master master = new Master(view.getLiveCells(), poolSize);
				ArrayList< Rectangle > result = master.compute();
				String state =  (stopFlag.getValue() ?" Interrupted: " : "") + result.size();
				view.setNextGen(state, result); 
				if (result.size() == 0) stopFlag.set(true);
				// TODO -- test whether the number of cells has changed, 
				// then look for steady state or small freq. oscillations
			}
		} catch (Exception ex) {	ex.printStackTrace();	}
	}
  /**---------------------------------------------------------------------------------
   * The problem is divided into Tasks, as many as the points on the grid. Each
   * point is calculated, according to his neighbors, whether to live or die
   * 
   */
   class Master extends Thread {
  
  	private ExecutorService executor;
  	private boolean[][] gameBoard;
  	private boolean[][] adjoining9;
  	private List<Rectangle> cells;
  	/**
  	 * 
  	 * @param set  : previous state represented by a list of Rectangles
  	 * @param poolSize : number of Thread
  	 * @param stopFlag  : monitor start and stop
  	 */
  	public Master(List<Rectangle> set, int poolSize)
  	{
  		this.executor = Executors.newFixedThreadPool(poolSize);
  		cells = set;
  	}
	/**
	 * The function returns the list of the living cells
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	 public ArrayList<Rectangle> compute() throws InterruptedException {
	  
		 ArrayList<Future<Rectangle>> nextGen = new ArrayList<Future<Rectangle>>();
	  		
	  		//	build the gameBoard: an ephemeral matrix of booleans the size of the grid
		int width = GameOfLifeView.getMatrixSide() + 1;
		int height = width;
		int minX= GameOfLifeView.matrixLen;
		int minY= GameOfLifeView.matrixLen;
		int maxX = 0;
		int maxY = 0;
		gameBoard = new boolean[width][height];
		for (Rectangle current : view.getLiveCells()) 
		{
			int x = (int) (current.getX() / GameOfLifeView.blockSize);
			int y = (int) (current.getY() / GameOfLifeView.blockSize);
			minX = Math.min(x,  minX);
			maxX = Math.max(x,  maxX);
			minY = Math.min(y,  minY);
			maxY = Math.max(y,  maxY);
			gameBoard[x][y] = true;
		}
		view.getLiveCells().clear();
		// TODO -- crop down to the used space		
		//		System.out.println(minX + " - " + maxX + ", " + minY + " - " + maxY);
		for (int i = 1; i < width - 1; i++) 
			for (int j = 1; j < height - 1; j++) 
				try {
					adjoining9 = new boolean[3][3];
					adjoining9[0][0] = gameBoard[i - 1][j - 1];
					adjoining9[0][1] = gameBoard[i - 1][j];
					adjoining9[0][2] = gameBoard[i - 1][j + 1];
					adjoining9[1][0] = gameBoard[i][j - 1];
					adjoining9[1][1] = gameBoard[i][j];  		//  ME
  					adjoining9[1][2] = gameBoard[i][j + 1];
  					adjoining9[2][0] = gameBoard[i + 1][j - 1];
  					adjoining9[2][1] = gameBoard[i + 1][j];
  					adjoining9[2][2] = gameBoard[i + 1][j + 1];
  
	  					/*
				 * If the point is evaluated "false" (white grid) then it is
				 * useless to create the res object type Future <Point> as
				 * it will always be "false" and the grid does not change.
				 */
  					int count = 0;
  					for (int a=0; a< 3; a++)
	  					for (int b=0; b< 3; b++)
  							if (!(a==1 && b==1) && adjoining9[a][b])	
  								count++;
  											
  
  					if (count > 0) {
	  						/*
					 * create the object of res futures; that is a
					 * "promise" of Point ("live" or. "Dead")
					 */
					ComputeTask tsk = new ComputeTask(adjoining9, i, j);
					Future<Rectangle> res = executor.submit(tsk);
					nextGen.add(res); 
	//						System.out.println("addking nextGen at " + res);
		
	  				}
	  			} catch (Exception e) {  	e.printStackTrace();  	}
	  
		// collect the finished tasks
	  	ArrayList<Rectangle> outputRects = new ArrayList<Rectangle>();
	  	for (Future<Rectangle> future : nextGen) {
	  		try {
	  			if (future.get() != null)
	  				outputRects.add(future.get());
	  		} catch (Exception ex) {	ex.printStackTrace();	}
	  	}
	  
	  	/*
		 * Once you have all the points,  shutdown the executor and
		 * return the list of rectangles for the next generation
		 */
		executor.shutdown();
		return outputRects;
	}
	  	
	  	/**---------------------------------------------------------------------------------
	  	  * The class implements Callable<Rectangle>: it calculates:
	  	   * whether to return a Rectangle for this spot at time T1
	  	   * @author Martella Massimiliano
	  	   */
	  	   class ComputeTask implements Callable<Rectangle> {
	  	  
	  	  	boolean[][] neighbors;
	  	  	int i, j;
	  	  
	  	  	/**
	  	  	 * Constructor
	  	  	 * @param grid9 : neighboring points at T0
	  	  	 * @param i : x coordinate of the new point in cell coords (not pixels)
	  	 	 * @param j : y coordinate of the new point
	  	  	 * @param stopFlag : a monitored property
	  	  	 */
	  	  	 ComputeTask(boolean[][] grid9, int inX, int inY) {
	  	  		neighbors = grid9;
	  	  		i = inX;
	  	  		j = inY;
	  	  	}
	  	  	/*
	  	  	 * Return state of the next time point
	  	  	 * */
	  	  	@Override public Rectangle call() {
	  	  		Rectangle rectAtTi = null;
	  	  		try {
	  	  			rectAtTi = computePoint(i, j);
	  	  		} catch (Exception ex) { 			ex.printStackTrace();  		}
	  	  		return rectAtTi;
	  	  	}
			/**  RULES:
			 * 1) a cell m [i, j] that in state T0 is Live and has zero or one
			 * live neighboring cells, in state T1 becomes Dead ("die of loneliness")
			 *
			 * 2) a cell m [i, j] that in state T0 is Live and has four or more
			 * neighboring cells becomes Dead ("dies from over-population ")
			 * 
			 * 3) an empty cell in T0 with 3 live neighbors will be born in T1
			 */

		   	public Rectangle computePoint(int i,int j) 
		  	{
		  		int surrounding = 0;
		  		if (neighbors[0][0])  			surrounding++;
		  		if (neighbors[0][1])  			surrounding++;
		  		if (neighbors[0][2])  			surrounding++;
		  		if (neighbors[1][0])  			surrounding++;
		  		// neighbors[1][1]		Don't count self here
		  		if (neighbors[1][2])  			surrounding++;
		  		if (neighbors[2][0])  			surrounding++;
		  		if (neighbors[2][1])  			surrounding++;
		  		if (neighbors[2][2])  			surrounding++;
		  
		  		boolean wasAlive = (neighbors[1][1]);
		  		boolean live =  (surrounding == 3)  || ( (wasAlive) && (surrounding == 2));
		  		return (live) ? view.makeCell(i, j) : null;
		  	}
	  	  }
	  }
}