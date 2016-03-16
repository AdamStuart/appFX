package container.mosaic;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Base class for parts of a layout. Layout parts consist of {@link Node}s
 * and {@link Divider}s.
 * @param <T>
 */
abstract class Element<T> implements ElementVisitable<T> {
	@JsonProperty
    protected int id;
	
    protected String stringID;
    
    protected T t;
    
    /** Used to generate a node id */
	private static int nodeCounter = 0;
	
	/** Flag indicating all locations are to be relative to an offset location */
	protected boolean useSurfaceOffset;
	
	/** (optional) offset distance from origin (0,0) */
    private Point2D.Double surfaceOffset = new Point2D.Double();
    
    /** The rectangle modified during layout */
    @JsonSerialize(using = DividerSerializer.class)
	@JsonDeserialize(using = DividerDeserializer.class)
	protected Rectangle2D.Double r;
	/** The rectangle used to compare to see if anythings changed */
	protected Rectangle2D.Double copy;
	/** The offsets of the object within it's container. */
	protected Rectangle2D.Double wo;
	
	/** The percentage of the overall width this object will be allocated */
	protected double horizontalWeight;
	/** The percentage of the overall height this object will be allocated */
    protected double verticalWeight;
	
    /** This object's element type. One of {@link ConnectType} types. */
	protected ElementType type;
	
	 
    
	Element() {
		r = new Rectangle2D.Double();
		copy = new Rectangle2D.Double();
		wo = new Rectangle2D.Double();
	}
	
	/**
	 * Base copy constructor duplicates the basics.
	 * 
	 * @param e
	 */
	Element(Element<T> e) {
		this();
		
		this.type = e.type;
		
		this.t = e.t;
		this.id = e.id;
		this.stringID = e.stringID;
		
		this.r.setFrame(e.r);
		this.wo.setFrame(e.wo);
		this.copy.setFrame(e.copy);
	}
	
	/**
	 * Creates a new Element
	 * 
	 * 
	 * @param t			the object being layed out.
	 * @param id		the id of the definition corresponding to T
	 */
	Element(T t, String id) {
		this.t = t;
		this.id = nodeCounter++; //REPLACE WITH MORE FORMAL GUUID MECHANISM OR SOMETHING
		this.stringID = id == null ? "" + this.id : id;
		r = new Rectangle2D.Double();
		copy = new Rectangle2D.Double();
		wo = new Rectangle2D.Double();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
        sb.append((this instanceof Node) ? "Node: \t" : "Split: ");
        sb.append(stringID == null ? id : stringID).append(" \t[").append("x: ").append(r.x).append(", ").
        append("y: ").append(r.y).append(", ").
        append("width: ").append(r.width).append(", ").
        append("height: ").append(r.height);
        if(Node.class.isAssignableFrom(this.getClass())) {
        	sb.append(", minW: ").append(((Node<T>)this).getMinWidth())
        	.append(", maxW: ").append(((Node<T>)this).getMaxWidth())
        	.append(", minH: ").append(((Node<T>)this).getMinHeight())
        	.append(", maxH: ").append(((Node<T>)this).getMaxHeight());
        }
        sb.append(", hWeight: ").append(horizontalWeight).append(", ").
        append("vWeight: ").append(verticalWeight);
        sb.append("]");
        
        if(useSurfaceOffset) {
            sb.append(" \t offset [").append("x: ").append(wo.x).append(", ").
            append("y: ").append(wo.y).append(", ").
            append("width: ").append(wo.width).append(", ").
            append("height: ").append(wo.height).append(", ").append("] ");
        }
        sb.append(" surface offset [x: ").append(surfaceOffset.x).
        append(", y: ").append(surfaceOffset.y).
        append("]");
        return sb.toString();
    }
	
	/**
	 * Used internally by the serialization infrastructure DO NOT CALL THIS! :)
	 * @param id
	 */
	void setId(int id) { 
		this.id = id;
		if(this.type == ElementType.DIVIDER) {
			this.stringID = "" + id;
		}
	}
	
	/**
	 * Returns the id of this {@code Element}
	 * 
	 * @return	the id of this {@code Element}
	 */
	int getId() { return id; }
	
	/**
	 * Returns T, the user object being wrapped by this
	 * layout-able element.
	 * @return
	 */
	T getTarget() {
        return t;
    }
	
	/**
	 * Sets the layout object.
	 * 
	 * @param t
	 */
	void setTarget(T t) {
		this.t = t;
	}
	
	/**
	 * Sets the offset which can be included when calculating locations.
	 */
	void setOffset(SurfacePriviledged<T> surface) {
		Rectangle2D s = surface.getArea();
		wo.setFrame(r.x + s.getX(), r.y + s.getY(), r.width, r.height);
	}
	
	/**
	 * Returns the computed horizontal weight of this
	 * {@code Node} which is the ratio of this Node's
	 * size to the overall size of its most recent container
	 * in a particular dimension (width).
	 * 
	 * @return	the computed horizontal weight (influences width).
	 */
	double getHorizontalWeight() {
		return horizontalWeight;
	}
	
	/**
	 * Returns the computed vertical weight of this
	 * {@code Node} which is the ratio of this Node's
	 * size to the overall size of its most recent container
	 * in a particular dimension (height).
	 * 
	 * @return	the computed vertical weight (influences height).
	 */
	double getVerticalWeight() {
		return verticalWeight;
	}
	
	/**
	 * Called after changing the bounds of this node. This method
	 * invokes the actual call to {@link SurfaceListener#changed(ChangeType, Object, String, Rectangle2D, Rectangle2D)}
	 * which subsequently notifies registered clients of layout changes.
	 * <p>
	 * Unlike {@link #force(SurfacePriviledged, ChangeType)}, this method checks
	 * its internal copy to see if there are any changes before propagating messages
	 * to listeners. (see {@link #force(SurfacePriviledged, ChangeType)})
	 * 
	 * @param s				the containing {@link SurfacePriviledged}
	 * @param changeType 	the {@link ChangeType}
	 * @see #force(SurfacePriviledged, ChangeType)
	 */
	abstract void set(SurfacePriviledged<T> s, ChangeType changeType);
	
	/**
	 * * Called after changing the bounds of this node. This method
	 * invokes the actual call to {@link SurfaceListener#changed(ChangeType, Object, String, Rectangle2D, Rectangle2D)}
	 * which subsequently notifies registered clients of layout changes.
	 * <p>
	 * Unlike {@link #set(SurfacePriviledged, ChangeType)}, this method <em>always</em> notifies the installed
	 * listeners regardless of whether there are any changes to this {@code Element} or not.
	 * (see {@link #set(SurfacePriviledged, ChangeType)})
	 * 
	 * @param s				the containing {@link SurfacePriviledged}
	 * @param changeType	the {@link ChangeType}
	 * @see #set(SurfacePriviledged, ChangeType)
	 */
	abstract void force(SurfacePriviledged<T> s, ChangeType changeType);
	
	void setUseSurfaceOffset(boolean b) {
		this.useSurfaceOffset = b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((stringID == null) ? 0 : stringID.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Element<?> other = (Element<?>) obj;
		if (stringID == null) {
			if (other.stringID != null)
				return false;
		} else if (!stringID.equals(other.stringID))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}
