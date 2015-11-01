package image.turingPattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class TuringPatternList 
{
	private static final long serialVersionUID = 1L;
	private ObservableList<TuringPattern> theModel;
	PixelGrid theGrid;
	public int getSize()	{ return theModel.size();	}
	public int getNActive()	
	{
		int ct = 0; 
		for (TuringPattern pat : theModel)
			if (pat.isActive()) ct++;
		return ct;	
	}
	
	public TuringPatternList(PixelGrid inGrid)
	{
		 theModel = FXCollections.observableArrayList();
		 theGrid = inGrid;
	}
	
	public void add(TuringPattern turingPattern) 		{		theModel.add(turingPattern);	}
	public ObservableList<TuringPattern> getList() 		{		return theModel;	}

	
	public TuringPattern getBestPatternAt(int i, int j)
	 {   
	      TuringPattern best = null;
	      double minVariation = Double.MAX_VALUE;         
	      for (int level = 0; level < theModel.size(); level++) 
	      {
	         TuringPattern p = theModel.get(level);
	         if (!p.isActive()) continue;
	         double var = p.getVariations().get(i, j);
	         if (var < minVariation)
	         {
	        	 best=p; 
	        	 minVariation = var;
	         }
	      }
	      return best;
	  }
	  
	  void step()
	  {  
	     for (TuringPattern p : theModel) 
	        if (p.isActive()) 
	        	p.step(theGrid);
		theGrid.update(this);
	  }
	  //--------------------------------------------------------------
	  int BENCHMARK_SIZE = 100;
	  
	  public long benchmark()
	  {
		  long start = System.currentTimeMillis();
		  for (int x = 0; x < BENCHMARK_SIZE; x++)
		  {
			  for (TuringPattern p : theModel) 
				   if (p.isActive()) p.step(theGrid);
		  }
		  return System.currentTimeMillis() - start;
	  }
	  //--------------------------------------------------------------
	  String dump()
	  {
		  StringBuffer s = new StringBuffer();
		  for (TuringPattern p : theModel) 
			  s.append(p.toString() + "\n");
		  return s.toString();
	  }
}
