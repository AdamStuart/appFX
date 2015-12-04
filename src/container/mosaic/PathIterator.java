package container.mosaic;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Position;

/**
 * Contains and creates {@link PathIterator.Path} objects. Accepts
 * {@link PathVisitor}s on behalf of the layout mechanism in order to 
 * dispatch the visitor along useful {@code PathIterator.Path}s it has previously 
 * assembled during the initial layout phase.
 */
class PathIterator<T> implements PathVisitable<T> {
    private List<HorizontalPath> horizontalPaths;
    private List<VerticalPath> verticalPaths;
    
    /**
     * Constructs a new {@code PathIterator}
     */
    public PathIterator() {
        horizontalPaths = new ArrayList<HorizontalPath>();
        verticalPaths = new ArrayList<VerticalPath>();
    }
    
    /**
     * Releases memory by clearing out all {@link Path} references.
     */
    void clearAll() {
    	horizontalPaths.clear();
    	verticalPaths.clear();
    }
    
    /**
     * Copy constructor. Uses the specified {@link LayoutImpl}'s nodelist
     * to re-construct an identical copy of this {@code PathIterator}
     * 
     * @param l			the {@link LayoutImpl} supply the required {@link Node}s
     * @param p			the {@code PathIterator} to copy.
     */
    public PathIterator(LayoutImpl<T> l, PathIterator<T> p) {
    	this();
    	
    	for(HorizontalPath path : p.horizontalPaths) {
    		horizontalPaths.add(path.getCopy(l));
    	}
    	
    	for(VerticalPath path : p.verticalPaths) {
    		verticalPaths.add(path.getCopy(l));
    	}
    }
    
    /**
     * Returns a List of {@link HorizontalPath} objects.
     * 
     * @return	 a List of {@link HorizontalPath} objects.
     */
    public List<HorizontalPath> getHorizontalPaths() {
        return horizontalPaths;
    }
    
    /**
     * Returns a List of {@link VerticalPath} objects.
     * 
     * @return	a List of {@link VerticalPath} objects.
     */
    public List<VerticalPath> getVerticalPaths() {
        return verticalPaths;
    }
    
    /**
     * Returns a count of vertical paths within this 
     * {@code PathIterator}
     * 
     * @return	a count of vertical paths within this 
     * {@code PathIterator}
     */
    public int getVerticalPathCount() {
    	return verticalPaths.size();
    }
    
    /**
     * Returns a count of horizontal paths within this 
     * {@code PathIterator}
     * 
     * @return	a count of horizontal paths within this 
     * {@code PathIterator}
     */
    public int getHorizontalPathCount() {
    	return horizontalPaths.size();
    }
    
    public int getTotalPathCount() {
    	return horizontalPaths.size() + verticalPaths.size();
    }
    
    /**
     * Called by {@link MosaicEngineImpl#requestLayout()} to do the
     * initial assembly of {@link Path} objects.
     * 
     * @param node	the "root" node.
     */
    void assemblePaths(Node<T> node) {
    	if(horizontalPaths == null) {
	        horizontalPaths = new ArrayList<HorizontalPath>();
	        verticalPaths = new ArrayList<VerticalPath>();
    	}else{
    		horizontalPaths.clear();
    		verticalPaths.clear();
    	}
        
        assembleHorizontalPaths(node, horizontalPaths);
        assembleVerticalPaths(node, verticalPaths);
    }
    
    /**
     * The guts of horizontal path assembly which recursively calls
     * itself to descend the links between nodes at the very left
     * of a layout, until it comes across a node at the very right.
     * 
     * When there is a branch from a Divider to many nodes, the path
     * up to that point is copied, the result of which creates a
     * distinct path to each from the starting object to a given
     * ending object.
     * 
     * @param curr		the current {@link Node}
     * @param hPaths	List of {@link HorizontalPath}s
     * @param currPath	the current {@code HorizontalPath}
     */
    private void doHorizontalScan(Node<T> curr, List<HorizontalPath> hPaths, HorizontalPath currPath) {
        currPath.add(curr);
        if(curr.nextVertical != null) {
            currPath.add(curr.nextVertical);
            
            int i = 0;
            for(Node<T> n : curr.nextVertical.nextNodes()) {
                if(i++ == 0) {
//                    doHorizontalScan(n, hPaths, currPath);
                }else{
                    currPath = currPath.copyBranch(n.prevVertical);
                    hPaths.add(currPath);
                    doHorizontalScan(n, hPaths, currPath);
                }
            }
        }
    }
    
    /**
     * Traverses (recursively) to the very bottommost object before calling {@link #doHorizontalScan(Position, List, HorizontalPath)}
     * to traverse the nodes from left to right, creating the {@link HorizontalPath} objects.
     * 
     * @param curr		the current {@link Node}
     * @param hPaths	List of {@link HorizontalPath}s
     */
    private void assembleHorizontalPaths(Node<T> curr, List<HorizontalPath> hPaths) {
    	Divider<T> nextHorizontal = null;
        if((nextHorizontal = curr.nextHorizontal) != null && nextHorizontal.hasNext()) {
        	assembleHorizontalPaths(nextHorizontal.nextNodes().get(0), hPaths);
        }
        
        HorizontalPath currPath = new HorizontalPath();
        hPaths.add(currPath);
        doHorizontalScan(curr, hPaths, currPath);
    }
    
    /**
     * The guts of {@link VerticalPath} assembly which recursively calls
     * itself to descend the links between nodes at the very top
     * of a layout, until it comes across a node at the very bottom.
     * 
     * When there is a branch from a {@link Divider} to many nodes, the path
     * up to that point is copied, the result of which creates a
     * distinct path to each from the starting object to a given
     * ending object.
     * 
     * @param curr		the current {@link Node}
     * @param vPaths	List of {@link VerticalPath}s
     * @param currPath	the current {@code VerticalPath}
     */
    private void doVerticalScan(Node<T> curr, List<VerticalPath> vPaths, VerticalPath currPath) {
        currPath.add(curr);
        if(curr.nextHorizontal != null) {
            currPath.add(curr.nextHorizontal);
            
            int i = 0;
            for(Node<T> n : curr.nextHorizontal.nextNodes()) {
                if(i++ == 0) {
                    doVerticalScan(n, vPaths, currPath);
                }else{
                    currPath = currPath.copyBranch(n.prevHorizontal);
                    vPaths.add(currPath);
                    doVerticalScan(n, vPaths, currPath);
                }
            }
        }
    }
    
    /**
     * Traverses (recursively) to the very rightmost object before calling {@link #doVerticalScan(Position, List, VerticalPath)}
     * to traverse the nodes from left to right, creating the {@link VerticalPath} objects.
     * 
     * @param curr		the current {@link Position}
     * @param vPaths	List of {@link HorizontalPath}s
     */
    private void assembleVerticalPaths(Node<T> curr, List<VerticalPath> vPaths) {
        Divider<T> nextVertical = null;
        if((nextVertical = curr.nextVertical) != null && nextVertical.hasNext()) {
        	assembleVerticalPaths(nextVertical.nextNodes().get(0), vPaths);
        }
        
        VerticalPath currPath = new VerticalPath();
        vPaths.add(currPath);
        doVerticalScan(curr, vPaths, currPath);
    }
    
    /**
     * Implementation of the {@link PathVisitable} interface which 
     * accepts a {@link PathVisitor}.
     * 
     * @param	lv 	the PathVisitor to accept
     */
    @Override
    public void accept(PathVisitor<T> lv) {
    	for(HorizontalPath p : horizontalPaths) {
    		p.accept(lv);
        }
        for(VerticalPath p : verticalPaths) {
        	p.accept(lv);
        }
    }
    
    /**
     * Temporary method used by tests to "lookup" a {@link Path}
     * by a containing {@link Node}'s String "id". Returns a list of
     * allPaths containing the Node specified by id.
     * 
     * @param id	the String id of the {@link Node} 
     * @return		Returns a list of allPaths containing the 
     * 				Node specified by id.
     */
    public List<Path> lookup(String id) {
    	List<Path> results = new ArrayList<Path>();
    	for(HorizontalPath p : horizontalPaths) {
    		if(p.contains(id)) results.add(p);
    	}
    	for(VerticalPath p : verticalPaths) {
    		if(p.contains(id)) results.add(p);
    	}
    	
    	return results;
    }
    
    /**
     * Base class for Paths containing nodes and dividers along either a horizontal
     * path or a vertical path beginning at one end of the layout surface
     * and ending at the other. Each path is unique in it's entirety while
     * it may be redundant along several points (an optimization waiting 
     * for an opportune time). Paths are assembled by recursively descending
     * down a Node to divider pathway until a divider containing more than 
     * one node is encountered. At this point, a copy of the current path is 
     * made and the path splits into as many separate and unique paths as there
     * are branches from the divider. In this way, all possible routes through
     * a variable arrangement are covered.
     */
    abstract class Path {
    	/** List of all elements (i.e. Nodes, Dividers) in this path */
        protected List<Element<T>> elems = new ArrayList<Element<T>>();
        
        /** The count of dividers (every other node) along this path */
        //protected int dividerCount;
        
        /**
         * Returns a deep copy of this {@code Path} as it's known subtype
         * and with copies of all this Path's inner elements - which copies
         * are all residents of the specified {@link LayoutImpl} instead
         * of the Layout which contains this specific {@code Path}. Therefore,
         * the Layout passed in must already have built its own {@link Element}s
         * and those Elements must have the same id's as the elements in this
         * Path.
         * 
         * @param l
         * @return
         */
        abstract <P extends Path> Path getCopy(LayoutImpl<T> l);
        
        /**
         * Overridden by subclasses to duplicate the current {@code Path}
         * @param s		the {@link Divider} which is the last point to be
         * 				copied along this Path starting at the beginning of
         * 				this Path.
         * @return		a new Path object which is a copy of this Path from it's 
         * 				beginning and ending with the specified Divider.
         */
        abstract <P extends Path> Path copyBranch(Divider<T> s);
        
        /**
         * Overridden by Paths to add a new Node to this Path.
         * 
         * @param node	the node to be added.
         */
        void add(Node<T> node) {
        	elems.add(node);
        }
        
        /**
         * Returns the count of dividers along this particular
         * path.
         * 
         * @return	the current count of dividers.
         */
        int getDividerCount() {
        	int count = 0;
        	for(Element<T> e : elems) {
        		if(e.type == ElementType.DIVIDER) {
        			++ count;
        		}
        	}
            return count;
        }
        
        /**
         * Called locally only to manipulate Elements in the list
         * of Elements.
         * @return
         */
        List<Element<T>> getElements() {
            return elems;
        }
        
        /**
         * Public interface used for testing purposes. Returns
         * a copy of the current element list.
         * 
         * @return	a copy of the current element list.
         */
        public List<Element<T>> getAllElements() {
        	return new ArrayList<Element<T>>(elems);
        }
        
        /**
         * Adds a divider to this {@code Path}
         * 
         * @param divider		the current divider.
         */
        void add(Divider<T> divider) {
            //++ dividerCount;
            elems.add(divider);
        }
        
        /**
         * returns the number of elements in this {@code Path}
         * @return	the number of elements in this {@code Path}
         */
        int size() {
        	return elems.size();
        }
        
        /**
         * Returns a flag indicating whether a Node with the 
         * specified id exists in this path.
         * 
         * @param id
         * @return
         */
        
        boolean contains(String id) {
        	for(Element<T> e : elems) {
        		if(e.stringID.equals(id)) return true;
        	}
        	return false;
        }
        
        /**
         * Used for debugging to get the path start node.
         * @param nodeStringID
         * @return flag indicating whether this {@code Path} starts with
         * a Node with the specified string id.
         */
        boolean startsWith(String nodeStringID) {
        	return elems.get(0).stringID.equals(nodeStringID);
        }
    }
    
    /**
     * A specific instance of a Path object which only contains a horizontal
     * route from a left most Node to a right most Node.
     */
    class HorizontalPath extends Path implements PathVisitable<T> {
    	@Override
    	public <P extends Path> HorizontalPath getCopy(LayoutImpl<T> l) {
    		HorizontalPath newPath = new HorizontalPath();
    		//newPath.dividerCount = this.dividerCount;
    		for(Element<T> e : elems) {
    			switch(e.type) {
	    			case NODE: { newPath.elems.add(l.getNode(e.stringID)); break; }
	    			case DIVIDER: { newPath.elems.add(l.getDivider(e.stringID, ((Divider<T>)e).isVertical)); break; }
	    		}
    		}
    		return newPath;
    	}
    	@Override
        public <P extends Path> HorizontalPath copyBranch(Divider<T> s) {
            HorizontalPath p = new HorizontalPath();
            p.elems.addAll(this.elems.subList(0, this.elems.indexOf(s) + 1)); //Copy only up to divider
            //p.dividerCount = dividerCount;
            
            return p;
        }
        /**
         * Implementation of the {@link PathVisitable} interface
         * 
         * @param lv	the visitor
         */
    	@Override
        public void accept(PathVisitor<T> lv) {
             lv.visit(this);
        }
    }
    
    /**
     * A specific instance of a Path object which only contains a vertical
     * route from a top most Node to a bottom most Node.
     */
    class VerticalPath extends Path implements PathVisitable<T> {
    	@Override
    	public <P extends Path> VerticalPath getCopy(LayoutImpl<T> l) {
    		VerticalPath newPath = new VerticalPath();
    		//newPath.dividerCount = this.dividerCount;
    		for(Element<T> e : elems) {
    			if (e != null)
    				switch(e.type) {
	    			case NODE: { newPath.elems.add(l.getNode(e.stringID)); break; }
	    			case DIVIDER: { newPath.elems.add(l.getDivider(e.stringID, ((Divider<T>)e).isVertical)); break; }
	    		}
    		}
    		return newPath;
    	}
    	@Override
        public <P extends Path> VerticalPath copyBranch(Divider<T> s) {
            VerticalPath p = new VerticalPath();
            p.elems.addAll(this.elems.subList(0, this.elems.indexOf(s) + 1)); //Copy only up to divider
            //p.dividerCount = dividerCount;
            
            return p;
        }
    	/**
         * Implementation of the {@link PathVisitable} interface
         * 
         * @param lv	the visitor
         */
        @Override
        public void accept(PathVisitor<T> lv) {
            lv.visit(this);
        }
    }
}
