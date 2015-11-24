package container.mosaic;

import java.awt.geom.Rectangle2D;




/**
 * Wraps an object to be laid out by this layout engine.
 * @author David Ray
 *
 */
class Node<T> extends Element<T> {
    protected double percentX;
    protected double percentY;
    protected double percentWidth;
    protected double percentHeight;
    
    private double minWidth = 0;
    private double maxWidth = Double.MAX_VALUE;
    private double minHeight = 0;
    private double maxHeight = Double.MAX_VALUE;
    
    protected Divider<T> prevVertical;
    protected Divider<T> prevHorizontal;
    protected Divider<T> nextVertical;
    protected Divider<T> nextHorizontal;
    
    /**
     * Constructs a new Node object
     * 
     * @param t					the object being laid out in most cases.
     * @param percentX			the percentage of the overall width, used to locate this node.
     * @param percentY			the percentage of the overall height used to locate this node.
     * @param percentWidth		the percentage of the overall width used to size this node horizontally.
     * @param percentHeight		the percentage of the overall height used to size this node vertically. 
     */
	Node(T t, double percentX, double percentY, double percentWidth, double percentHeight) {
		this(t, null, percentX, percentY, percentWidth, percentHeight);
	}
	
	/**
	 * Constructs a new Node object
	 * @param t					the object being laid out in most cases.
	 * @param id				user specified String id.
	 * @param percentX			the percentage of the overall width, used to locate this node.
     * @param percentY			the percentage of the overall height used to locate thithiss node.
     * @param percentWidth		the percentage of the overall width used to size this node horizontally.
     * @param percentHeight		the percentage of the overall height used to size this node vertically. 
	 */
	Node(T t, String id, double percentX, double percentY, double percentWidth, double percentHeight) {
		super(t, id);
		this.type = ElementType.NODE;
	    
		this.percentX = percentX;
		this.percentY = percentY;
		this.percentWidth = percentWidth;
		this.percentHeight = percentHeight;
	}
	
	/**
	 * Constructs a new Node object with constraints if specified.
	 * 
	 * @param surface
	 * @param t
	 * @param id
	 * @param percentX
	 * @param percentY
	 * @param percentWidth
	 * @param percentHeight
	 * @param minW
	 * @param maxW
	 * @param minH
	 * @param maxH
	 */
	Node(T t, String id, double percentX, double percentY, double percentWidth, double percentHeight, 
		double minW, double maxW, double minH, double maxH) {
		super(t, id);
		this.type = ElementType.NODE;
	    
		this.percentX = percentX;
		this.percentY = percentY;
		this.percentWidth = percentWidth;
		this.percentHeight = percentHeight;
		this.minWidth = minW;
		this.maxWidth = maxW;
		this.minHeight = minH;
		this.maxHeight = maxH;
	}
	
	/**
	 * Constructs a new Node with absolute parameters set, allowing for width and height
	 * constraints.
	 * 
	 * @param surface
	 * @param t
	 * @param id
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @param minW
	 * @param maxW
	 * @param minH
	 * @param maxH
	 * @param hWeight
	 * @param vWeight
	 */
	Node(T t, String id, double x, double y, double w, double h, 
		double minW, double maxW, double minH, double maxH, double hWeight, double vWeight) {
		super(t, id);
		this.type = ElementType.NODE;
	    
		r.x = x;
		r.y = y;
		r.width = w;
		r.height = h;
		
		this.minWidth = minW;
		this.maxWidth = maxW;
		this.minHeight = minH;
		this.maxHeight = maxH;
		this.horizontalWeight = hWeight;
		this.verticalWeight = vWeight;
	}
	
	Node(Node<T> other) {
		super(other);
		
		//Set Node specific parameters
		this.percentX = other.percentX;
		this.percentY = other.percentY;
		this.percentWidth = other.percentWidth;
		this.percentHeight = other.percentHeight;
		
		this.minWidth = other.minWidth;
		this.maxWidth = other.maxWidth;
		this.minHeight = other.minHeight;
		this.maxHeight = other.maxHeight;
		this.horizontalWeight = other.horizontalWeight;
		this.verticalWeight = other.verticalWeight;
	}
	
	/**
	 * Releases the references to it's resources.
	 */
	void clear() {
		nextHorizontal = null;
		prevHorizontal = null;
		nextVertical = null;
		prevVertical = null;
	}
	
	/**
	 * Separates this Node from its divider references.
	 */
	void disconnectFromDividers() {
		if(nextHorizontal != null) {
			nextHorizontal.removeNode(this);
			nextHorizontal = null;
		}
		if(nextVertical != null) {
			nextVertical.removeNode(this);
			nextVertical = null;
		}
		if(prevHorizontal != null) {
			prevHorizontal.removeNode(this);
			prevHorizontal = null;
		}
		if(prevVertical != null) {
			prevVertical.removeNode(this);
			prevVertical = null;
		}
	}
	
	/**
	 * Called by various layout mechanisms to compute the x
	 * location based on the configured x percentage.
	 * @return	an approximate horizontal location of this node 
	 */
	double percentToAproxX(Rectangle2D.Double area) {
		return percentX * area.getWidth();
	}
	
	/**
	 * Called by various layout mechanisms to compute the width
	 * based on the configure width percentage.
	 * @return	an approximate horizontal width 
	 */
	double percentToAproxWidth(Rectangle2D.Double area) {
		return percentWidth * area.getWidth();
	}
	
	/** 
	 * Called by various layout mechanisms to compute the y
	 * location based on the configured y percentage.
	 * @return an approximate vertical location of this node
	 */
	double percentToAproxY(Rectangle2D.Double area) {
		return percentY * area.getHeight();
	}
	
	/**
	 * Called by various layout mechanisms to compute the height
	 * based on the configured height percentage.
	 * @return	an approximate vertical height
	 */
	double percentToAproxHeight(Rectangle2D.Double area) {
		return percentHeight * area.getHeight();
	}
	
	/**
	 * Returns the trailing horizontal divider. The trailing divider 
	 * is the divider to the right or bottom.
	 * @return	the next/trailing divider.
	 */
	Divider<T> nextVertical() {
	    return nextVertical;
	}
	
	/**
	 * Returns the next horizontal divider. The next divider is 
	 * either the right or bottom divider.
	 * @return	the next horizontal divider.
	 */
	Divider<T> nextHorizontal() {
	    return nextHorizontal;
	}
	
	/**
	 * Implementation of the {@link ElementVisitable} interface,
	 * called by a given visitor during first dispatch.
	 */
	@Override
	public void acceptHorizontal(ElementVisitor<T> ev) {
	    ev.visitHorizontal(this);
	}
	
	/**
	 * Implementation of the {@link ElementVisitable} interface.
	 */
	@Override
    public void acceptVertical(ElementVisitor<T> ev) {
	    ev.visitVertical(this);
    }
	
	/**
	 * Returns the user specified minimum width.
	 * @return	the user specified minimum width.
	 */
	public double getMinWidth() {
		return minWidth;
	}
	
	/**
	 * Sets the minimum width constraint
	 * @param d
	 */
	public void setMinWidth(double d) {
		this.minWidth = d;
	}
	
	/**
	 * Returns the user specified maximum width.
	 * @return	the user specified maximum width.
	 */
	public double getMaxWidth() {
		return maxWidth;
	}
	
	/**
	 * Sets the maximum width constraint
	 * @param d
	 */
	public void setMaxWidth(double d) {
		this.maxWidth = d;
	}
	
	/**
	 * Returns the user specified minimum height;
	 * @return	 the user specified minimum height;
	 */
	public double getMinHeight() {
		return minHeight;
	}
	
	/**
	 * Sets the minimum height constraint
	 * @param d
	 */
	public void setMinHeight(double d) {
		this.minHeight = d;
	}
	
	/**
	 * Returns the user specified maximum height;
	 * @return	the user specified maximum height;
	 */
	public double getMaxHeight() {
		return maxHeight;
	}
	
	/**
	 * Sets the maximum height constraint
	 * @param d
	 */
	public void setMaxHeight(double d) {
		this.maxHeight = d;
	}
	
	/**
	 * Called by layout code to invoke notification of this Node's 
	 * bounds change if appropriate.
	 * 
	 * @param surface 		{@link SurfacePriviledged} containing this Node
	 * @param changeType	{@link ChangeType} adds additional information
	 * 						for client.
	 */
	void set(SurfacePriviledged<T> surface, ChangeType changeType) {
		r.setFrame(r.x, r.y, Math.max(0, r.width), Math.max(0, r.height));
	    if(!r.equals(copy)) {
	    	setOffset(surface);
	    	surface.notifyChange(changeType, t, stringID, 
	    		new Rectangle2D.Double(copy.x, copy.y, copy.width, copy.height), 
	    		new Rectangle2D.Double(r.x, r.y, r.width, r.height));
            copy.setFrame(r);
        }
	}
	
	/**
	 * Forces a set and notify to the client.
	 * @param surface 		{@link SurfacePriviledged} containing this Node
	 * @param changeType	{@link ChangeType} adds additional information
	 * 						for client.
	 */
	void force(SurfacePriviledged<T> surface, ChangeType changeType) {
		setOffset(surface);
    	surface.notifyChange(changeType, t, stringID, 
    		new Rectangle2D.Double(copy.x, copy.y, copy.width, copy.height), 
    		new Rectangle2D.Double(r.x, r.y, r.width, r.height));
        copy.setFrame(r);
	}
}
