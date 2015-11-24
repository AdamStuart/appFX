package container.mosaic;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The divider between objects being laid out. There are no metaphors 
 * being handled by client code for this. The concept of a Divider is
 * totally "virtual" in that no corresponding client object for this 
 * exists. Therefore this Divider exists as a space keeper and a means
 * of detecting mouse actions on a virtual divider.
 */
class Divider<T> extends Element<T> {
	boolean isVertical;
	
	final static String EMPTY = "empty";
	
	List<Node<T>> prevNodes = new ArrayList<Node<T>>();
	List<Node<T>> nextNodes = new ArrayList<Node<T>>();
	Set<Divider<T>> leadingJoins = new HashSet<Divider<T>>();
	Set<Divider<T>> trailingJoins = new HashSet<Divider<T>>();
	
	Comparator<Node<T>> verticalComparator;
	Comparator<Node<T>> horizontalComparator;
	
	Divider<T> trailingJoin;
	Divider<T> leadingJoin;
	
	double dividerSize;
	
	double length;
	
	Divider() { 
		super();
		this.type = ElementType.DIVIDER;
		
		createComparators();
	}
	
	Divider(Divider<T> other) {
		super(other);
		
		this.dividerSize = other.dividerSize;
		this.length = other.length;
		this.isVertical = other.isVertical;
		
		createComparators();
	}
	
	
	/**
	 * Constructs a new Divider
	 * 
	 * @param x				the x location of this divider
	 * @param y				the y location of this dividier
	 * @param length		the length of this divider along it's relevant axis
	 * @param isVertical	true if this is a vertical divider, false if not.
	 */
	Divider(double x, double y, double length, boolean isVertical) {
		super(null, null);
		this.type = ElementType.DIVIDER;
		
		this.length = length;
		
		r.x = x;
		r.y = y;
		r.width = isVertical ? dividerSize : length;
		r.height = isVertical ? length : dividerSize;
		this.isVertical = isVertical;
		
		createComparators();
	}
	
	private void createComparators() {
		verticalComparator = new Comparator<Node<T>>() {
			public int compare(Node<T> n0, Node<T> n1) {
				if(n0.r.y > n1.r.y) return 1;
				else if(n0.r.y < n1.r.y) return -1;
				return 0;
			}
		};
		
		horizontalComparator = new Comparator<Node<T>>() {
			public int compare(Node<T> n0, Node<T> n1) {
				if(n0.r.x > n1.r.x) return 1;
				else if(n0.r.x < n1.r.x) return -1;
				return 0;
			}
		};
	}
	
	/**
	 * Releases the nodes and dividers this Divider has references to.
	 */
	void clear() {
		nextNodes.clear();
		prevNodes.clear();
		trailingJoins.clear();
		leadingJoins.clear();
		if(leadingJoin != null) {
			leadingJoin.trailingJoins.remove(this);
			leadingJoin = null;
		}
		if(trailingJoin != null) {
			trailingJoin.leadingJoins.remove(this);
			trailingJoin = null;
		}
		
	}
	
	/**
	 * Removes the specified {@link Node} from any references this
	 * Divider maintains.
	 * 
	 * @param node	the {@link Node} to remove.
	 */
	void removeNode(Node<T> node) {
		nextNodes.remove(node);
		prevNodes.remove(node);
	}
	
	/**
	 * Addes a node as one of the previous nodes.
	 * @param n	the node to add
	 */
	void addPrevious(Node<T> n) {
		prevNodes.add(n);
		Collections.sort(prevNodes, isVertical ? verticalComparator : horizontalComparator);
	}
	
	/**
	 * Returns a list of this divider's previous nodes (a
	 * previous node is a node to the top or left).
	 * @return	a list of this divider's previous nodes.
	 */
	List<Node<T>> previousNodes() {
	    return prevNodes;
	}
	
	/**
	 * Returns a flag indicating whether this node
	 * has any previous nodes.
	 * @return	true if yes, false if no
	 */
	boolean hasPrevious() {
		return prevNodes.size() > 0;
	}
	
	/**
	 * Adds a "next" node (node to the right or bottom)
	 * to this divider's list of next nodes.
	 * @param n	the Node being added
	 */
	void addNext(Node<T> n) {
		nextNodes.add(n);
		Collections.sort(nextNodes, isVertical ? verticalComparator : horizontalComparator);
	}
	
	/**
	 * Returns a list of this divider's next nodes.
	 * @return	 a list of this divider's next nodes
	 */
	List<Node<T>> nextNodes() {
	    return nextNodes;
	}
	
	/**
	 * Returns a flag indicating whether this divider has "next" nodes.
	 * @return	true if yes, false if no
	 */
	boolean hasNext() {
		return nextNodes.size() > 0;
	}
	
	/**
	 * Returns the "next nodes" as a string suitable for serializing.
	 * @return a string suitable for serializing.
	 */
	String nextNodesSerial() {
		if(nextNodes.isEmpty()) return EMPTY;
		StringBuilder sb = new StringBuilder();
		for(Node<T> n : nextNodes) {
			sb.append(n.stringID).append(":");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	/**
	 * Returns the "prev nodes" as a string suitable for serializing.
	 * @return a string suitable for serializing.
	 */
	String prevNodesSerial() {
		if(prevNodes.isEmpty()) return EMPTY;
		StringBuilder sb = new StringBuilder();
		for(Node<T> n : prevNodes) {
			sb.append(n.stringID).append(":");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	/**
	 * Returns the "leading joins" as a string suitable for serializing.
	 * @return a string suitable for serializing.
	 */
	String leadingJoinsSerial() {
		if(leadingJoins.isEmpty()) return EMPTY;
		StringBuilder sb = new StringBuilder();
		for(Divider<T> n : leadingJoins) {
			sb.append(n.stringID).append(":");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	/**
	 * Returns the "trailing joins" as a string suitable for serializing.
	 * @return a string suitable for serializing.
	 */
	String trailingJoinsSerial() {
		if(trailingJoins.isEmpty()) return EMPTY;
		StringBuilder sb = new StringBuilder();
		for(Divider<T> n : trailingJoins) {
			sb.append(n.stringID).append(":");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}
	
	/**
	 * Returns a flag indicating whether or not the {@link Node}
	 * passed in is has a side which overlaps this divider. The Divider-
	 * to-side end points are given a tolerance of half the current
	 * divider size. 
	 * <p>
	 * Note: the tolerance should be set later to be half the "snap distance"
	 * when divider snapping is implemented.
	 * 
	 * @param n	the {@link Node} to check against this {@code Divider}
	 * @return	a flag indicating whether or not the {@link Node}
	 * passed in is has a side which overlaps this divider
	 */
	boolean overlapsRelative(Node<T> n, Rectangle2D.Double surfaceArea) {
		double tolerance = MosaicEngineImpl.DIVIDER_TOLERANCE_RATIO * (Math.max(surfaceArea.width, surfaceArea.height));
		if(isVertical) {
			double maxY = n.percentToAproxY(surfaceArea) + n.percentToAproxHeight(surfaceArea);
			return r.y - (dividerSize/2) <= maxY && n.percentToAproxY(surfaceArea) <= r.y + r.height + (tolerance);
		}else{
			double maxX = n.percentToAproxX(surfaceArea) + n.percentToAproxWidth(surfaceArea);
			return r.x - (dividerSize/2) <= maxX && n.percentToAproxX(surfaceArea) <= r.x + r.width + (tolerance);
		}
	}
	
	/**
	 * Returns a flag indicating whether the specified {@link Node}
	 * overlaps this {@code Divider}
	 * @param n		the Node to compare
	 * @return		true if so, false if not.
	 */
	boolean overlaps(Node<T> n) {
		if(isVertical) {
			return n.r.y <= r.getMaxY() && r.y <= n.r.getMaxY();
		}else{
			return n.r.x <= r.getMaxX() && r.x <= n.r.getMaxX();
		}
	}
	
	/**
	 * Returns a flag indicating whether the specified {@link Divider} 
	 * has the same orientation as this Divider and overlaps this 
	 * Divider along the axis of its orientation.
	 * 
	 * @param d		the divider to test against
	 * @return		true if so, false if not.
	 */
	boolean overlaps(Divider<T> d) {
		if(isVertical ^ d.isVertical) return false;
		
		if(isVertical) {
			return r.y <= d.r.getMaxY() && d.r.y <= r.getMaxY();
		}else{
			return r.x <= d.r.getMaxX() && d.r.x <= r.getMaxX();
		}
	}
	
	/**
	 * Adds <em>either</em> a x value OR y value depending on whether
	 * this divider is vertical or horizontal. If vertical, this 
	 * method adds the y value extending the box describing the bounds
	 * enclosing this divider to also include the specified y value
	 * and also attempts to expand the height if the y value is added
	 * in that direction. Like wise for horizontal dividers and adding
	 * the x value - the divider will attempt to expand both sides.
	 * @param x		the x value to expand the enclosing bounds around
	 * 				for the case of a horizontal divider.
	 * @param y		the y value to expand the enclosing bounds around for
	 * 				the case of a vertical divider.
	 */
	void addPoint(double x, double y) {
	    if(isVertical) {
	    	double maxY = r.getMaxY();
	        r.y = Math.min(r.getY(), y);
	        r.height = Math.max(r.getHeight(), y - r.getY());
	        //Can only be used for expansion
	        if(r.getMaxY() < maxY) r.height = maxY - r.y;
	    }else{
	    	double maxX = r.getMaxX();
	        r.x = Math.min(r.getX(), x);
	        r.width = Math.max(r.getWidth(), x - r.getX());
	        //Can only be used for expansion
	        if(r.getMaxX() < maxX) r.width = maxX - r.x;
	    }
	}
	
	/**
	 * Implementation of the {@link ElementVisitable} interface
	 */
	@Override
	public void acceptHorizontal(ElementVisitor<T> ev) {
	    ev.visitHorizontal(this);
	}
	
	/**
	 * Implementation of the {@link ElementVisitable} interface
	 */
	@Override
    public void acceptVertical(ElementVisitor<T> ev) {
	    ev.visitVertical(this);
    }
	
	/**
	 * Returns the minimum amount of this Divider's x value
	 * given the minimum sizes of the adjacent previous nodes.
	 * 
	 * @return  the minimum x value;
	 */
	@JsonIgnore
	public double getMinX() {
		if(!isVertical) return r.x;
		
		double currMin = 0;//dividerSize;
		for(Node<T> n : prevNodes) {
			if(n.r.x + n.getMinWidth() > currMin) {
				currMin = n.r.x + n.getMinWidth();
			}
		}
		
		for(Node<T> n : nextNodes) {
			double mlx = n.r.getMaxX() - n.getMaxWidth() + dividerSize;
			if(mlx > currMin) {
				currMin = mlx;
			}
		}
		return currMin;
	}
	
	/**
	 * Returns the maximum point this horizontal divider (vertical dividers
	 * are moot for this function) can move towards the top given minimum
	 * sizes of previous nodes and maximum sizes of next nodes.
	 * 
	 * @return	the minimum Y value
	 */
	@JsonIgnore
	public double getMinY() {
		if(isVertical) return r.y;
		
		double currMin = 0;
		for(Node<T> n : prevNodes) {
			if(n.r.y + n.getMinHeight() > currMin) {
				currMin = n.r.y + n.getMinHeight();
			}
		}
		for(Node<T> n : nextNodes) {
			double mby = n.r.getMaxY() - n.getMaxHeight() + dividerSize;
			if(mby > currMin) {
				currMin = mby;
			}
		}
		return currMin;
	}
	
	/**
	 * Returns the maximum amount of this Divider's x value
	 * given the minimum sizes of the adjacent next nodes and
	 * maximum sizes of adjacent previous nodes. Called by 
	 * {@link MosaicEngineImpl#getMaxMoveBounds(java.awt.geom.Rectangle2D.Double, Divider)}
	 * 
	 * @param	surfaceArea		Rectangle surface bounds 
	 * @return  the maximum x value;
	 */
	@JsonIgnore
	double getMaxX(Rectangle2D.Double surfaceArea) {
		if(!isVertical) return r.x;
		
		double currMax = surfaceArea.getMaxX() - dividerSize;
		for(Node<T> n : nextNodes) {
			double maxX = n.r.getMaxX() - n.getMinWidth() - dividerSize;
			if(maxX < currMax) {
				currMax = maxX;
			}
		}
		for(Node<T> n : prevNodes) {
			double maxRight = n.r.x + n.getMaxWidth();
			if(maxRight < currMax) {
				currMax = maxRight;
			}
		}
		return currMax;
	}
	
	/**
	 * Returns the maximum Y value this horizontal divider can
	 * move given the maxSizes of previous nodes and the min sizes
	 * of next nodes. Called by 
	 * {@link MosaicEngineImpl#getMaxMoveBounds(java.awt.geom.Rectangle2D.Double, Divider)}
	 * 
	 * @param	surfaceArea		Rectangle surface bounds
	 * @return
	 */
	@JsonIgnore
	double getMaxY(Rectangle2D.Double surfaceArea) {
		if(isVertical) return r.y;
		
		double currMax = surfaceArea.getMaxY() - dividerSize;
		for(Node<T> n : nextNodes) {
			double maxY = n.r.getMaxY() - n.getMinHeight() - dividerSize;
			if(maxY < currMax) {
				currMax = maxY;
			}
		}
		for(Node<T> n : prevNodes) {
			double maxExpandBottom = n.r.y + n.getMaxHeight();
			if(maxExpandBottom < currMax) {
				currMax = maxExpandBottom;
			}
		}
		return currMax;
	}
	
	/**
	 * Adds the specified {@link Divider} to the list of dividers
	 * which are perpendicularly adjacent to this {@code Divider},
	 * and whose location is between the endpoints of this Divider's
	 * length. 
	 * <p>
	 * It then adds a reference to the specified Divider, back to 
	 * this divider so that it can remove itself from the list of 
	 * this divider's perpendicular joins; if it is deleted
	 * by another operation.
	 * 
	 * @param d				the {@link Divider} to add.
	 * @param isLeading		flag indicating whether the {@code Divider} being
	 * 						added is adjacent to this Divider's leading 
	 * 						or trailing edge.
	 */
	void addPerpendicularJoin(Divider<T> d, boolean isLeading) {
		if(d == null) {
			throw new IllegalArgumentException("Attempt to add perpendicular join with null divider");
		}
		if(isLeading) {
			leadingJoins.add(d);
			d.trailingJoin = this;
		}else{
			trailingJoins.add(d);
			d.leadingJoin = this;
		}
	}
	
	/**
	 * Returns the top-most/left-most {@link Node} in this {@code Divider}'s
	 * {@link List} of previous nodes. Used during layout to determine
	 * the length of this Divider and it's endpoint locations.
	 * 
	 * The most "proximal" Node is the Node closest to the origin (0,0), that 
	 * exists in this Divider's list of Nodes along its length.
	 * 
	 * @return	the most "proximal" Node
	 */
	Node<T> getMostProximalNode() {
		Node<T> nodeWithMinValue = prevNodes.get(0);
		if(isVertical) {
			for(Node<T> n : prevNodes) {
				if(nodeWithMinValue.r.y > n.r.y) {
					nodeWithMinValue = n;
				}
			}
		}else{
			for(Node<T> n : prevNodes) {
				if(nodeWithMinValue.r.x > n.r.x) {
					nodeWithMinValue = n;
				}
			}
		}
		return nodeWithMinValue;
	}
	
	/**
	 * Returns the bottom-most/right-most {@link Node} in this {@code Divider}'s
	 * {@link List} of previous nodes. Used during layout to determine
	 * the breadth of this Divider and it's endpoint locations.
	 * 
	 * The most "distal" Node is the Node farthest from the origin (0,0), that 
	 * exists in this Divider's list of Nodes along its length.
	 * 
	 * @return	the most "distal" Node
	 */
	Node<T> getMostDistalNode() {
		Node<T> nodeWithMaxValue = prevNodes.get(0);
		if(isVertical) {
			for(Node<T> n : prevNodes) {
				if(nodeWithMaxValue.r.y + nodeWithMaxValue.r.height < n.r.y + n.r.height) {
					nodeWithMaxValue = n;
				}
			}
		}else{
			for(Node<T> n : prevNodes) {
				if(nodeWithMaxValue.r.x + nodeWithMaxValue.r.width < n.r.x + n.r.width) {
					nodeWithMaxValue = n;
				}
			}
		}
		return nodeWithMaxValue;
	}
	
	/** NOP */
	void set(SurfacePriviledged<T> s, ChangeType c) {}
	
	/** NOP */
	void force(SurfacePriviledged<T> s, ChangeType c) {}
}
