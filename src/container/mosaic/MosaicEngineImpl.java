package container.mosaic;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Cross-framework free-form, multi-split layout engine written to be used 
 * with any GUI toolkit and other than it being written in Java
 * - it is agnostic to any specific framework and can be used in
 * major frameworks such as AWT, SWING, JavaFX, Pivot, etc.
 * 
 * The main engine is generic and any object may be added to it,
 * so that once a {@link SurfaceListener} is added the same object
 * will be returned with two {@link Rectangle2D}, one describing
 * the old location and dimensions, and another describing the new.
 * 
 * Typical boilerplate code would look like:
 * <pre>
 * 		MosaicEngine<MyType> engine = new MosaicEngine<MyType>();
 * 		engine.setSurface(Rectangle2D);
 *      engine.addChangeListener(new ChangeListener<MyType>() {
 *      	public void changed(MyType type, Rectangle2D.Double oldR, Rectangle2D.Double newR) {
 *      		type.setX(newR.getX());
 *      		type.setY(newR.getY());
 *      		type.setWidth(newR.getWidth());
 *   			type.setHeight(newR.getHeight());
 *      	}
 *      }
 *      
 *      engine.add(MyObject, 0, 0, 0.5, 0.5);
 *      engine.add(MyObject, 0, 0.5, 0.5, 1);
 *      engine.add(MyObject, 0.5, 0, 0.5, 0.5);
 *      ...
 *      engine.requestLayout();
 *      
 * </pre>
 * @author David Ray
 *
 * @param <T> Any object of your choice, usually the object to be layed out, but it could be a String or 
 * some key indicating another object.
 */
class MosaicEngineImpl<T> implements MosaicEngine<T> {

	/** Used as a return value while connecting up the layout */
    private enum ConnectType { 
    	BOTH, TOP, BOTTOM, LEFT, RIGHT, NONE;
    }
    
    /** Classifies corner search results */
    private enum Corner { NW, SW, NE, SE };
    
    static final int FACTOR = 10;
    static final int PRECISION = 2;
    static final int SOURCE_IDX = 0;
    static final int TARGET_IDX = 1;
    
    /** ratio of tolerance (fudge) when locating dividers during initial construction */
    static final double DIVIDER_TOLERANCE_RATIO = 0.009375;
	
	
	/** List of Surfaces */
	private List<SurfacePriviledged<T>> surfaces = new ArrayList<SurfacePriviledged<T>>();
	
	/** Map of registered Surfaces to their respective visitation tools */
	private Map<SurfacePriviledged<T>, PathVisitor<T>[]> visitors = new HashMap<SurfacePriviledged<T>, PathVisitor<T>[]>();
	
	private static final int LAYOUT_INDEX = 0;
	private static final int ALIGNMENT_INDEX = 1;
	private static final int WEIGHT_INDEX = 2;
	
	/**
	 *  Private constructor
	 *  
	 *  @see EngineBuilder, MosaicEngineBuilder
	 */
	MosaicEngineImpl() {}
	
	/**
	 * Sets the area/bounds being managed.
	 * @param surface	Surface representing a container of objects to layout.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void addSurface(Surface<T> surface) {
		SurfacePriviledged<T> s = (SurfacePriviledged<T>)surface;
		if(visitors.get(s) != null) {
			throw new IllegalArgumentException("Surface already registered with the engine.");
		}
		
		visitors.put(s, new PathVisitor[] { 
			new LayoutVisitor<T>(s), 
			new AlignmentVisitor<T>(s), 
			new WeightAdjustmentVisitor<T>(s) 
		});
		
		s.setEngine(this);
		surfaces.add(s);
	}
	
	/**
	 * Removes the {@link Surface} from the {@code MosaicEngine}'s {@link Collection}
	 * of managed Surfaces.
	 * 
	 * @param surface	the {@code Surface} to remove.
	 */
	public void removeSurface(Surface<T> surface) {
		SurfacePriviledged<T> s = (SurfacePriviledged<T>)surface;
		s.setEngine(null);
		visitors.remove(surface);
		surfaces.remove(s);
	}
	
	/**
	 * Returns a {@link Rectangle2D.Double} which outlines the calculated
	 * maximum bounding area for any move by the specified {@link Divider}
	 * 
	 * @param area		the Surface area
	 * @param d			the Divider being moved.
	 * @return			the corresponding maximum move bounds.
	 */
	Rectangle2D.Double getMaxMoveBounds(Rectangle2D.Double area, Divider<T> d) {
		Rectangle2D.Double r = new Rectangle2D.Double();
		if(d.isVertical) {
			r.y = d.r.y;
			r.height = d.r.height;
			r.x = d.getMinX();
			r.width = d.getMaxX(area) - r.x;
		}else{
			r.x = d.r.x;
			r.width = d.r.width;
			r.y = d.getMinY();
			r.height = d.getMaxY(area) - r.y;
		}
		
		return r;
	}
	
	/**
	 * Returns a new {@link Rectangle2D.Double} which for a vertical origin, is
	 * the dimensions in the vertical direction taken from the origin, and the 
	 * horizontal bounds is taken from the alt. Conversely for a horizontal origin, 
	 * the horizontal bounds is taken from the orig bounds and the vertical bounds 
	 * is taken from the alt passed in bounds.
	 * 
	 * @param orig				the original bounds.
	 * @param alt				the alternative bounds.
	 * @param origIsVertical	the flag saying which (vertical or horizontal) is taken
	 * 							from the origin.
	 * @return	return a new combined Rectangle2D.Double
	 */
	private Rectangle2D.Double addPerpendicularBounds(Rectangle2D.Double orig, Rectangle2D.Double alt, boolean origIsVertical) {
		Rectangle2D.Double retVal = new Rectangle2D.Double();
		if(!origIsVertical) {
			retVal.setFrame(orig.x, alt.y, orig.width, alt.height);
		}else {
			retVal.setFrame(alt.x, orig.y, alt.width, orig.height);
		}
		return retVal;
	}
	
	/**
	 * Takes the point specified by p and moves it to the point specified by mouseX
	 * and mouseY using the x and y offset and taking the maximum move bounds in to
	 * account - by allowing the maxMoveBounds to constrain the movement in any particular
	 * direction.
	 * 
	 * @param p					the previous point considered to be the current origin of
	 * 							a {@link Divider} or {@link Node}. 
	 * @param maxMoveBounds		the previously calculated bounds outlining the allowed extreme
	 * 							of movement in any particular direction.
	 * @param mouseX			the x portion of the new point to create
	 * @param mouseY			the y portion of the new point to create
	 * @param xOffset			the distance from mouseX to the object's origin x.
	 * @param yOffset			the distance from mouseY to the object's origin y
	 
	 * @return	the point moved as close to mouseX/mouseY as the maxMoveBounds will allow.
	 * 
	 * @see #getMaxMoveBounds(Divider)
	 * @see #addPerpendicularBounds(java.awt.geom.Rectangle2D.Double, java.awt.geom.Rectangle2D.Double, boolean)
	 */
	private Point2D.Double moveDragPoint(Point2D.Double p, Rectangle2D.Double maxMoveBounds,
		double mouseX, double mouseY, double xOffset, double yOffset) {
		
		Point2D.Double nextPoint = new Point2D.Double(p.x, p.y);
		nextPoint.x = Math.max(maxMoveBounds.x, Math.min(maxMoveBounds.getMaxX(), mouseX - xOffset));
		nextPoint.y = Math.max(maxMoveBounds.y, Math.min(maxMoveBounds.getMaxY(), mouseY - yOffset));
		
		return nextPoint;
	}
	
	/**
	 * Adds the distance to the snap point to the {@link Point2D.Double} passed
	 * in, if the specified point is within "snapDistance". Searches the node
	 * structure for qualifying divider locations to determine if a snap-worthy 
	 * location exists and should be used.
	 * 
	 * @param surface			the {@link SurfacePriviledged} containing the elements.
	 * @param d					the {@link Divider} being used.
	 * @param p					the current {@link Point.Double}
	 * @param maxMoveBounds		the maximum bounded area permissable to move within.
	 * 
	 * @return	a Point altered to contain the required distance if a snap is indicated.
	 */
	private Point2D.Double snapDragPoint(SurfacePriviledged<T> surface, Divider<T> d, Point2D.Double p, Rectangle2D.Double maxMoveBounds) {
		double dividerSize = surface.getDividerSize();
		double snapDistance = surface.getSnapDistance();
		Rectangle2D.Double area = surface.getArea();
		
		Divider<T> snapTo = getClosestSnapRangeDivider(surface, p, d);
		if(snapTo != null) {
			//For vertical snap candidate: Though there is a divider in snap range, check that the divider is "more snap-worthy" 
			//than a container edge.
			if(snapTo.isVertical && ((Math.abs(p.x - area.x) < Math.abs(p.x - snapTo.r.x)) ||
				(Math.abs(p.x + dividerSize - area.getMaxX()) < Math.abs(p.x + dividerSize - snapTo.r.x)))) {
				p.x = Math.abs(p.x - area.x) < Math.abs(p.x - snapTo.r.x) ? area.x : area.getMaxX() - dividerSize;
			}else if(!snapTo.isVertical && ((Math.abs(p.y - area.y) < Math.abs(p.y - snapTo.r.y)) ||
				(Math.abs(p.y + dividerSize - area.getMaxY()) < Math.abs(p.y + dividerSize - snapTo.r.y)))) {
				//For horizontal snap candidate: Though there is a divider in snap range, check that the divider is "more snap-worthy" 
				//than a container edge.
				p.y = Math.abs(p.y - area.y) < Math.abs(p.y - snapTo.r.y) ? area.y : area.getMaxY() - dividerSize;
			}else if(d.overlaps(snapTo)) {//Snap flush to side of divider
				if(d.isVertical) {
					if(snapTo.r.x < d.r.x) { 
						p.x = snapTo.r.getMaxX();
					}else{
						p.x = snapTo.r.x - dividerSize;
					}
				}else{
					if(snapTo.r.y < d.r.y) {
						p.y = snapTo.r.getMaxY();
					}else{
						p.y = snapTo.r.y - dividerSize;
					}
				}
			}else{ //Snap to match divider's end
				if(d.isVertical) {
					p.x = snapTo.r.x;
				}else{
					p.y = snapTo.r.y;
				}
			}
		}else{ //Snap to edges of container if appropriate
			if(d.isVertical) {
				if(Math.abs(p.x - area.x) <= snapDistance) {
					p.x = area.x;
				}else if(Math.abs(p.x + dividerSize - area.getMaxX()) <= snapDistance) {
					p.x = area.getMaxX() - dividerSize;
				}
			}else{
				if(Math.abs(p.y - area.y) <= snapDistance) {
					p.y = area.y;
				}else if(Math.abs(p.y + dividerSize - area.getMaxY()) <= snapDistance) {
					p.y = area.getMaxY() - dividerSize;
				}
			}
		}		
		
		return p;
	}
	
	/**
	 * Returns the closest {@link Divider} within snap range.
	 * 
	 * @param surface		the {@link SurfacePriviledged} containing the {@link Divider}s. 
	 * @param p				the current mouse point (different than the location of the divider being
	 * 						dragged).
	 * @param d				the divider being dragged.
	 * @return	`			the divider within closest snap range or null.
	 */
	private Divider<T> getClosestSnapRangeDivider(SurfacePriviledged<T> surface, Point2D.Double p, Divider<T> d) {
		double dividerSize = surface.getDividerSize();
		double snapDistance = surface.getSnapDistance();
		
		Divider<T> retVal = null;
		if(d.isVertical) {
			List<Divider<T>> verticalDividers = getAllVerticalDividersInRange(
				surface.getVerticalDividers(), p.x - dividerSize - snapDistance, p.x + dividerSize + snapDistance, d);
			for(Divider<T> div : verticalDividers) {
				if(retVal == null || (Math.abs(p.x - div.r.x) < Math.abs(retVal.r.x - p.x))) {
					retVal = div;
				}
			}
		}else{
			List<Divider<T>> horizontalDividers = getAllHorizontalDividersInRange(
				surface.getHorizontalDividers(), p.y - dividerSize - snapDistance, p.y + dividerSize + snapDistance, d);
			for(Divider<T> div : horizontalDividers) {
				if(retVal == null || (Math.abs(p.y - div.r.y) < Math.abs(retVal.r.y - p.y))) {
					retVal = div;
				}
			}
		}
		
		return retVal;
	}
	
	/**
	 * Returns a list of those {@link Divider}s that lie between the two
	 * x locations specified.
	 *  
	 * @param dividers	list of the Surface's vertical Dividers
	 * @param y1		the smallest x boundary
	 * @param y2		the largest x boundary
	 * @param exclude	the {@link Divider} to exclude (don't want to include the start
	 * 					divider in the results)
	 * @return
	 */
	private List<Divider<T>> getAllVerticalDividersInRange(List<Divider<T>> dividers, double x1, double x2, Divider<T> exclude) {
		List<Divider<T>> searchResults = new ArrayList<Divider<T>>();
		for(Divider<T> d : dividers) {
			if(d == exclude) continue;
			if(d.r.x >= x1 && d.r.x <= x2) {
				searchResults.add(d);
			}
		}
		return searchResults;
	}
	
	/**
	 * Returns a list of those {@link Divider}s that lie between the two
	 * y locations specified.
	 * 
	 * @param dividers	list of the Surface's horizontal Dividers
	 * @param y1		the smallest y boundary
	 * @param y2		the largest y boundary
	 * @param exclude	the {@link Divider} to exclude (don't want to include the start
	 * 					divider in the results)
	 * @return
	 */
	private List<Divider<T>> getAllHorizontalDividersInRange(List<Divider<T>> dividers, double y1, double y2, Divider<T> exclude) {
		List<Divider<T>> searchResults = new ArrayList<Divider<T>>();
		for(Divider<T> d : dividers) {
			if(d == exclude) continue;
			if(d.r.y >= y1 && d.r.y <= y2) {
				searchResults.add(d);
			}
		}
		return searchResults;
	}
	
	/**
	 * Returns a list view of all {@link Divider}s which overlap the specified Divider.
	 *  
	 * @param surface		the {@link SurfacePriviledged} containing the specified {@link Divider}
	 * @param divider		the Divider for which to find overlapping Dividers.
	 * @return				a list view of all {@link Divider}s which overlap the specified Divider.
	 */
	List<Divider<T>> getAllOverlappingDividers(SurfacePriviledged<T> surface, Divider<T> divider) {
		List<Divider<T>> l = new ArrayList<Divider<T>>();
		List<Divider<T>> searchList = divider.isVertical ? 
			surface.getVerticalDividers() : surface.getHorizontalDividers();
		for(Divider<T> d : searchList) {
			if(divider.overlaps(d)) {
				l.add(d);
			}
		}
		return l;
	}
	
	/**
	 * Returns a list view of all {@link Divider}s which overlap the specified Divider,
	 * and lie between the lower and upper bounds specified.
	 * 
	 * @param surface		the {@link SurfacePriviledged} containing the specified {@link Divider}
	 * @param divider		the Divider for which to find overlapping Dividers. 
	 * @param lowerLoc		the lower bounding value (x for vertical dividers, y for horizontal)
	 * @param upperLoc		the upper bounding value (x for vertical dividers, y for horizontal)
	 * @param exclude		Dividers not included in search.
	 * @return		a list view of all {@link Divider}s which overlap the specified Divider,
	 * 				and lie between the lower and upper bounds specified.
	 */
	List<Divider<T>> getAllOverlappingDividersBetween(SurfacePriviledged<T> surface, Divider<T> divider, double lowerLoc, double upperLoc, List<Divider<T>> exclude) {
		List<Divider<T>> l = new ArrayList<Divider<T>>();
		List<Divider<T>> searchList = divider.isVertical ? 
			surface.getVerticalDividers() : surface.getHorizontalDividers();
		for(Divider<T> d : searchList) {
			if(divider.overlaps(d) && !exclude.contains(d) &&
				((d.isVertical && (d.r.x > lowerLoc && d.r.x < upperLoc )) || 
				(!d.isVertical && (d.r.y > lowerLoc && d.r.y < upperLoc )))) {
				l.add(d);
			}
		}
		return l;
	}
	
	private void transferHorizontalJoins(Divider<T> source, Divider<T> target, double delta) {
		//Resize joins and transfer those joins to the target divider
		for(Divider<T> d : source.leadingJoins) {
			d.r.width -= delta;
			target.addPerpendicularJoin(d, true);
		}
		for(Divider<T> d : source.trailingJoins) {
			d.r.x -= delta;
			d.r.width += delta;
			target.addPerpendicularJoin(d, false);
		}
	}
	
	private void transferVerticalJoins(Divider<T> source, Divider<T> target, double delta) {
		//Resize joins and transfer those joins to the target divider
		for(Divider<T> d : source.trailingJoins) {
			d.r.y -= delta;
			d.r.height += delta;
			target.addPerpendicularJoin(d, false);
		}
		for(Divider<T> d : source.leadingJoins) {
			d.r.height -= delta;
			target.addPerpendicularJoin(d, true);
		}
	}
	
	private List<Node<T>> transferNodesLeft(SurfacePriviledged<T> surface, Divider<T> source, Divider<T> target, double mergeLoc) {
		List<Node<T>> affectedNodes = new ArrayList<Node<T>>();
		double delta = source.r.x - mergeLoc;
		for(Node<T> n : source.nextNodes()) {
			n.r.x -= delta;
			n.r.width += delta;
			if(n.r.y == 0 && n.r.x == 0) {
				surface.setRoot(n);
			}
			if(target != null) {
				target.addNext(n);
			}
			n.prevVertical = target;
			affectedNodes.add(n);
		}
		
		if(target != null) {
			//Resize and add nodes bounded by the source but NOT the target 
			//(maybe laying outside the length of the target) but now need to be added.
			for(Node<T> n : source.previousNodes()) {
				if(!target.overlaps(n)) {
					n.r.width -= delta;
					target.addPrevious(n);
					n.nextVertical = target;
					affectedNodes.add(n);
				}
			}
		
			transferHorizontalJoins(source, target, delta);
		}
		
		return affectedNodes;
	}
	
	private List<Node<T>> transferNodesRight(SurfacePriviledged<T> surface, Divider<T> source, Divider<T> target, double mergeLoc) {
		List<Node<T>> affectedNodes = new ArrayList<Node<T>>();
		double delta = source.r.x - mergeLoc;
		
		for(Node<T> n : source.previousNodes()) {
			n.r.width -= delta;
			if(target != null) {
				target.addPrevious(n);
			}
			n.nextVertical = target;
			affectedNodes.add(n);
		}
		
		if(target != null) {
			//Resize and add nodes bounded by the source but NOT the target 
			//(maybe laying outside the length of the target) but now need to be added.
			for(Node<T> n : source.nextNodes()) {
				if(!target.overlaps(n)) {
					n.r.x -= delta;
					n.r.width += delta;
					target.addNext(n);
					n.prevVertical = target;
					affectedNodes.add(n);
				}
			}
		
			transferHorizontalJoins(source, target, delta);
		}
		
		return affectedNodes;
	}
	
	private List<Node<T>> transferNodesUp(SurfacePriviledged<T> surface, Divider<T> source, Divider<T> target, double mergeLoc) {
		List<Node<T>> affectedNodes = new ArrayList<Node<T>>();
		double delta = source.r.y - mergeLoc;
		
		for(Node<T> n : source.nextNodes()) {
			n.r.y -= delta;
			n.r.height += delta;
			if(n.r.y == 0 && n.r.x == 0) {
				surface.setRoot(n);
			}
			if(target != null) {
				target.addNext(n);
			}
			n.prevHorizontal = target;
			affectedNodes.add(n);
		}
		
		if(target != null) {
			//Resize and add nodes bounded by the source but NOT the target 
			//(maybe laying outside the length of the target) but now need to be added.
			for(Node<T> n : source.previousNodes()) {
				if(!target.overlaps(n)) {
					n.r.height -= delta;
					target.addPrevious(n);
					affectedNodes.add(n);
				}
			}
			
			transferVerticalJoins(source, target, delta);
		}
		
		return affectedNodes;
	}
	
	private List<Node<T>> transferNodesDown(SurfacePriviledged<T> surface, Divider<T> source, Divider<T> target, double mergeLoc) {
		List<Node<T>> affectedNodes = new ArrayList<Node<T>>();
		double delta = source.r.y - mergeLoc;
		
		for(Node<T> n : source.previousNodes()) {
			n.r.height -= delta;
			if(target != null) {
				target.addPrevious(n);
			}
			n.nextHorizontal = target;
			affectedNodes.add(n);
		}
		
		if(target != null) {
			//Resize and add nodes bounded by the source but NOT the target 
			//(maybe laying outside the length of the target) but now need to be added.
			for(Node<T> n : source.nextNodes) {
				if(!target.overlaps(n)) {
					n.r.y -= delta;
					n.r.height += delta;
					target.addNext(n);
					n.prevHorizontal = target;
					affectedNodes.add(n);
				}
			}
			
			transferVerticalJoins(source, target, delta);
		}
		
		return affectedNodes;
	}
	
	/**
	 * Merges two bordering horizontal dividers moving any other horizontal dividers 
	 * which may lie between the two dividers proportionally out of the way.
	 * 
	 * @param surfaceImpl	the {@link Surface} containing the moved dividers.
	 * @param node			the {@link Node} being removed whose bordering dividers
	 * 						are being merged.
	 * @return				a list of those {@link Node}s that are affected by
	 * 						the moves.
	 */
	private List<Node<T>> mergeDividersVerticallyWithPeripheralMove(SurfacePriviledged<T> surface, Node<T> node) {
		List<Node<T>> affectedNodes = new ArrayList<Node<T>>();
		
		double dividerAllowance = surface.getDividerSize() * 2;
		double mergeLoc = node.r.getMaxY() - Math.rint(node.r.height / 2);
		
		List<Divider<T>> prevSearchResults = surface.getSearchResults(node.prevHorizontal);
		boolean resultsAreLeading = node.prevVertical.leadingJoins.contains(prevSearchResults.get(0));
		Set<Divider<T>> moveList = resultsAreLeading ? 
			new HashSet<Divider<T>>(node.prevVertical.leadingJoins) : 
				new HashSet<Divider<T>>(node.nextVertical.trailingJoins);
		moveList.addAll(prevSearchResults);
		
		double reductionAmt = Math.rint((node.r.height / 2d) + dividerAllowance / (double)(moveList.size() + 1));
		for(Divider<T> d : moveList) {
			moveDivider(surface, affectedNodes, d, null, new Point2D.Double(0, d.r.y + reductionAmt), surface.getDividerSize());
		}
		
		prevSearchResults = surface.getSearchResults(node.nextHorizontal);
		resultsAreLeading = node.prevVertical.leadingJoins.contains(prevSearchResults.get(0));
		moveList = resultsAreLeading ? 
			new HashSet<Divider<T>>(node.prevVertical.leadingJoins) : 
				new HashSet<Divider<T>>(node.nextVertical.trailingJoins);
		moveList.addAll(prevSearchResults);
		
		reductionAmt = Math.rint((node.r.height / 2d) + dividerAllowance / (double)(moveList.size() + 1));
		for(Divider<T> d : moveList) {
			moveDivider(surface, affectedNodes, d, null, new Point2D.Double(0, d.r.y - reductionAmt), surface.getDividerSize());
		}
		
		affectedNodes.addAll(mergeDividers(surface, node.prevHorizontal, node.nextHorizontal, mergeLoc));
		
		return affectedNodes;
	}
	
	/**
	 * Merges two bordering vertical dividers moving any other vertical dividers 
	 * which may lie between the two dividers proportionally out of the way.
	 * 
	 * @param surfaceImpl	the {@link Surface} containing the moved dividers.
	 * @param node			the {@link Node} being removed whose bordering dividers
	 * 						are being merged.
	 * @return				a list of those {@link Node}s that are affected by
	 * 						the moves.
	 */
	private List<Node<T>> mergeDividersHorizontallyWithPeripheralMove(SurfacePriviledged<T> surface, Node<T> node) {
		List<Node<T>> affectedNodes = new ArrayList<Node<T>>();
		
		double dividerAllowance = surface.getDividerSize() * 2;
		double mergeLoc = node.r.getMaxX() - Math.rint(node.r.width / 2);
		
		List<Divider<T>> prevSearchResults = surface.getSearchResults(node.prevVertical);
		boolean resultsAreLeading = node.prevHorizontal.leadingJoins.contains(prevSearchResults.get(0));
		Set<Divider<T>> moveList = resultsAreLeading ? 
			new HashSet<Divider<T>>(node.prevHorizontal.leadingJoins) : 
				new HashSet<Divider<T>>(node.nextHorizontal.trailingJoins);
		moveList.addAll(prevSearchResults);
		
		double reductionAmt = Math.rint((node.r.width / 2d) + dividerAllowance / (double)(moveList.size() + 1));
		for(Divider<T> d : moveList) {
			moveDivider(surface, affectedNodes, d, null, new Point2D.Double(d.r.x + reductionAmt, 0), surface.getDividerSize());
		}
		
		
		prevSearchResults = surface.getSearchResults(node.nextVertical);
		resultsAreLeading = node.prevHorizontal.leadingJoins.contains(prevSearchResults.get(0));
		moveList = resultsAreLeading ? 
			new HashSet<Divider<T>>(node.prevHorizontal.leadingJoins) : 
				new HashSet<Divider<T>>(node.nextHorizontal.trailingJoins);
		moveList.addAll(prevSearchResults);
		
		reductionAmt = Math.rint((node.r.width / 2d) + dividerAllowance / (double)(moveList.size() + 1));
		for(Divider<T> d : moveList) {
			moveDivider(surface, affectedNodes, d, null, new Point2D.Double(d.r.x - reductionAmt, 0), surface.getDividerSize());
		}
		
		affectedNodes.addAll(mergeDividers(surface, node.prevVertical, node.nextVertical, mergeLoc));
		
		return affectedNodes;
	}
	
	/**
	 * Merges the specified {@link Divider}s together thus making one divider. The Divider
	 * specified as the "source" divider is merged into the Divider specified as the
	 * "target" divider. In addition, the location where the merging takes place can
	 * be specified thus the resulting divider's location will be the location specified
	 * as mergeLoc.
	 * 
	 * @param surface		the {@link SurfacePriviledged} containing the merged {@link Divider}s.
	 * @param source		the source {@link Divider}
	 * @param target		the target {@link Divider}
	 * @param mergeLoc		the location where the resulting Divider will be positioned.
	 * 						Usually specified as the target Dividers's location but may
	 * 						optionally lie anywhere between the source and target Divider.
	 * @return				a list of the {@link Node}s affected by the merging.
	 */
	private List<Node<T>>  mergeDividers(SurfacePriviledged<T> surface, Divider<T> source, Divider<T> target, double mergeLoc) {
		List<Node<T>> affectedNodes = new ArrayList<Node<T>>();
		
		if(target != null) {
			//Merging some place in middle, so move target before merging the source into it.
			if((!target.isVertical && mergeLoc != target.r.y && mergeLoc != source.r.y) || 
				(target.isVertical && mergeLoc != target.r.x && mergeLoc != source.r.x)) {
				
				moveDivider(surface, affectedNodes, target, null, 
					new Point2D.Double(mergeLoc, mergeLoc), surface.getDividerSize());
			}
		}
		
		if(source.isVertical) {
			if(mergeLoc < source.r.x) {
				affectedNodes.addAll(transferNodesLeft(surface, source, target, mergeLoc));
			}else{
				affectedNodes.addAll(transferNodesRight(surface, source, target, mergeLoc));
			}
			
			if(target != null) {
				//Retain the extremes of either side to get a union of the length
				target.r.y = Math.min(source.r.y, target.r.y);
				target.r.height = Math.max(source.r.getMaxY() - target.r.y, target.r.getMaxY() - target.r.y);
			}
			source.clear();
			surface.getVerticalDividers().remove(source);
		}else{ //Horizontal Dividers
			if(mergeLoc < source.r.y) {
				affectedNodes.addAll(transferNodesUp(surface, source, target, mergeLoc));
			}else{
				affectedNodes.addAll(transferNodesDown(surface, source, target, mergeLoc));
			}
			
			if(target != null) {
				//Retain the extremes of either side to get a union of the length
				target.r.x = Math.min(source.r.x, target.r.x);
				target.r.width = Math.max(source.r.getMaxX() - target.r.x, target.r.getMaxX() - target.r.x);
			}
			source.clear();
			surface.getHorizontalDividers().remove(source);
		}
		
		return affectedNodes;
	}
	
	/**
	 * Moves the specified {@link Divider} to the precise location
	 * specified by either the x OR the y value specified by the
	 * {@link Point2D} passed in. Horizontal Dividers can only be 
	 * moved along vertically and therefore only have deltaY
	 * applied; same is true for vertical Dividers and horizontal
	 * movement, however Dividers can be compressed or expanded along
	 * their length.
	 * <p>
	 * This method also recursively calls itself to act on the "alt"
	 * divider which is understood to be perpendicular (the two 
	 * representing a corner).
	 * 
	 * @param surface			the {@link SurfacePriviledged} containing the moved {@link Divider}
	 * @param d					the {@link Divider} to move.
	 * @param alt				the perpendicular {@link Divider} to move, if any (not null)
	 * @param movePoint			the exact point to move to (using only the x value for vertical 
	 * 								dividers, and the y value for horizontal dividers).
	 * @param dividerSize		the size of the static dimension of {@link Divider}s on its {@link Surface}
	 */
	private void moveDividerImmediately(SurfacePriviledged<T> s, Divider<T> d, Divider<T> alt, Point2D.Double movePoint, double dividerSize) {
		if(d.isVertical) {
			double deltaX = movePoint.x - d.r.x;
			d.r.x += deltaX;
			for(Node<T> n : d.prevNodes) {
				n.r.width += deltaX; 
				n.set(s, ChangeType.RESIZE_RELOCATE);
			}
			for(Node<T> n : d.nextNodes) {
				n.r.width -= deltaX;
				n.r.x += deltaX;
				n.set(s, ChangeType.RESIZE_RELOCATE);
			}
			
			//Stretch/Compress perpendicularly flush dividers along their length
			for(Divider<T> joined : d.leadingJoins) {
				joined.r.width += deltaX;
			}
			for(Divider<T> joined : d.trailingJoins) {
				joined.r.x += deltaX;
				joined.r.width -= deltaX;
			}
			if(alt != null) {
				moveDividerImmediately(s, alt, null, movePoint, dividerSize);
			}
		}else{
			double deltaY = movePoint.y - d.r.y;
			d.r.y += deltaY;
			for(Node<T> n : d.prevNodes) {
				n.r.height += deltaY;
				n.set(s, ChangeType.RESIZE_RELOCATE);
			}
			for(Node<T> n : d.nextNodes) {
				n.r.height -= deltaY;
				n.r.y += deltaY;
				n.set(s, ChangeType.RESIZE_RELOCATE);
			}
			
			//Stretch/Compress perpendicularly flush dividers along their length
			for(Divider<T> joined : d.leadingJoins) {
				joined.r.height += deltaY;
			}
			for(Divider<T> joined : d.trailingJoins) {
				joined.r.y += deltaY;
				joined.r.height -= deltaY;
			}
			if(alt != null) {
				moveDividerImmediately(s, alt, null, movePoint, dividerSize);
			}
		}
	}
	
	/**
	 * Moves the specified {@link Divider} to the precise location
	 * specified by either the x OR the y value specified by the
	 * {@link Point2D} passed in. Horizontal Dividers can only be 
	 * moved along vertically and therefore only have deltaY
	 * applied; same is true for vertical Dividers and horizontal
	 * movement, however Dividers can be compressed or expanded along
	 * their length.
	 * <p>
	 * This method also recursively calls itself to act on the "alt"
	 * divider which is understood to be perpendicular (the two 
	 * representing a corner).
	 * <p>
	 * In addition this method adds to the list of affected nodes for later notification
	 * rather than calling {@link Node#set(SurfacePriviledged, ChangeType)} immediately
	 * to notify listeners
	 * 
	 * @param surface			the {@link SurfacePriviledged} containing the moved {@link Divider}
	 * @param affectedNodes		the list of affected nodes which will possibly be added to by this method.
	 * @param d					the {@link Divider} to move.
	 * @param alt				the perpendicular {@link Divider} to move, if any (not null)
	 * @param movePoint			the exact point to move to (using only the x value for vertical 
	 * 								dividers, and the y value for horizontal dividers).
	 * @param dividerSize		the size of the static dimension of {@link Divider}s on its {@link Surface}
	 */
	private List<Node<T>> moveDivider(SurfacePriviledged<T> s, List<Node<T>> affectedNodes, Divider<T> d, Divider<T> alt, Point2D.Double movePoint, double dividerSize) {
		if(d.isVertical) {
			double deltaX = movePoint.x - d.r.x;
			d.r.x += deltaX;
			for(Node<T> n : d.prevNodes) {
				n.r.width += deltaX; 
				affectedNodes.add(n);
			}
			for(Node<T> n : d.nextNodes) {
				n.r.width -= deltaX;
				n.r.x += deltaX;
				affectedNodes.add(n);
			}
			
			//Stretch/Compress perpendicularly flush dividers along their length
			for(Divider<T> joined : d.leadingJoins) {
				joined.r.width += deltaX;
			}
			for(Divider<T> joined : d.trailingJoins) {
				joined.r.x += deltaX;
				joined.r.width -= deltaX;
			}
			if(alt != null) {
				moveDivider(s, affectedNodes, alt, null, movePoint, dividerSize);
			}
		}else{
			double deltaY = movePoint.y - d.r.y;
			d.r.y += deltaY;
			for(Node<T> n : d.prevNodes) {
				n.r.height += deltaY;
				affectedNodes.add(n);
			}
			for(Node<T> n : d.nextNodes) {
				n.r.height -= deltaY;
				n.r.y += deltaY;
				affectedNodes.add(n);
			}
			
			//Stretch/Compress perpendicularly flush dividers along their length
			for(Divider<T> joined : d.leadingJoins) {
				joined.r.height += deltaY;
			}
			for(Divider<T> joined : d.trailingJoins) {
				joined.r.y += deltaY;
				joined.r.height -= deltaY;
			}
			if(alt != null) {
				moveDivider(s, affectedNodes, alt, null, movePoint, dividerSize);
			}
		}
		
		return affectedNodes;
	}
	
	
	/**
	 * Alters the specified {@link Point2D.Double} to adhere to the specified maximum move
	 * bounds, then delegates to {@link #moveDivider(SurfacePriviledged, Divider, Divider, java.awt.geom.Point2D.Double, double)}
	 * 
	 * @param surface			the {@link SurfacePriviledged} containing the moved {@link Divider}
	 * @param d					the {@link Divider} to move.
	 * @param alt				the perpendicular {@link Divider} to move, if any (not null)
	 * @param maxMoveBounds		the maximum bounds any Divider movement can have.
	 * @param movePoint			the exact point to move to (using only the x value for vertical 
	 * 								dividers, and the y value for horizontal dividers).
	 * @param dividerSize		the size of the static dimension of {@link Divider}s on its {@link Surface}
	 * @see #moveDivider(SurfacePriviledged, Divider, Divider, java.awt.geom.Point2D.Double, double)
	 */
	private void moveDividerWithinMinMax(SurfacePriviledged<T> s, Divider<T> d, Divider<T> alt, Rectangle2D.Double maxMoveBounds, Point2D.Double movePoint, double dividerSize) {
		movePoint.x = Math.min(maxMoveBounds.getMaxX(), Math.max(movePoint.x, maxMoveBounds.x));
		movePoint.y = Math.min(maxMoveBounds.getMaxY(), Math.max(movePoint.y, maxMoveBounds.y));
		moveDividerImmediately(s, d, alt, movePoint, dividerSize);
	}
	
	/** Used for testing */
	private void doStats(SurfacePriviledged<T> surface, long start) {
		long finish = System.nanoTime() - start;
		
		System.out.println("Divider Size: " + surface.getDividerSize());
		System.out.println("Integer Precision: " + surface.getUseIntegerPrecision());
		System.out.println("Area: " + surface.getArea());
		
		for(PathIterator<T>.Path p : surface.getPathIterator().getHorizontalPaths()) {
		    for(Element<T> e : p.elems){
		        System.out.println("Element: " + e);
		    }
		}
  
		for(PathIterator<T>.Path p : surface.getPathIterator().getVerticalPaths()) {
		    for(Element<T> e : p.elems){
		        System.out.println("Element: " + e);
		    }
		}
        System.out.println("execution time: " + ((double)finish / 1000000000.0d) + "  secs.");
	}
	
	/**
	 * Signals the engine to begin a layout cycle. 
	 * <p>
	 * The method is called by client code, which ideally should be calling this method 
	 * from within a framework specific method such as "doLayout()" in SWING parlance, 
	 * or some method like "setBounds()" which is called when the given GUI framework has 
	 * found a need to re-layout its content.
	 * <p>
	 * Reference implementations have been provided called MosaicPaneRefImpl.java for a
	 * couple of frameworks, these classes assemble a reference implementation which uses
	 * framework specific "MosaicPane"(s) which demonstrate the use of the MosaicEngine to 
	 * layout content in a framework specific way.
	 */
	void requestLayout(SurfacePriviledged<T> surface) {
		if(surface.getIsDragging() || surface.isLocked()) return;
		
		if(surface.getNodeList().size() < 1) {
			throw new IllegalStateException("Cannot layout an empty surface.");
		}
		
		long start = System.nanoTime();
		
		PathIterator<T> pathIterator = surface.getPathIterator();
		
		if(surface.getIsInit()) {
			connectNodes(surface);
		    pathIterator.assemblePaths(surface.getRoot());
		}
		
		pathIterator.accept(visitors.get(surface)[LAYOUT_INDEX]);
		pathIterator.accept(visitors.get(surface)[ALIGNMENT_INDEX]);
		pathIterator.accept(visitors.get(surface)[WEIGHT_INDEX]);
		
		surface.setIsInit(false);
		
		////////////////////////////Layout Statistics Below  ////////////////////////////
		doStats(surface, start);
	}
	
	/**
	 * Sends a {@link PathVisitor} over the surface's {@link PathIterator.Path}s which
	 * is specialized to adjust and set the {@link Node} weights.
	 * 
	 * @param surface	the surface for which {@link Node} weights need adjustment.
	 */
	void adjustWeights(SurfacePriviledged<T> surface) {
		surface.getPathIterator().accept(visitors.get(surface)[WEIGHT_INDEX]);
	}
	
	/**
	 * Connects the {@link Node} objects to {@link Divider}s and prepares 
	 * for the assembly of {@link PathIterator.Path}s which are the next
	 * step in the layout process. This method is only called once, when 
	 * the objects have not yet been assembled. This method searches for
	 * relevant {@link Divider}s which already exist or creates new ones
	 * when existing ones at relevant locations (the bounds of a given
	 * Node) aren't found.
	 */
	private void connectNodes(SurfacePriviledged<T> surface) {
		double boundaryY = surface.getBoundaryDividerCondition().y;
        double boundaryX = surface.getBoundaryDividerCondition().x;
        
        List<Divider<T>> horizontalDividers = surface.getHorizontalDividers();
        List<Divider<T>> verticalDividers = surface.getVerticalDividers();
        
        Rectangle2D.Double surfaceArea = surface.getArea();
        
		for(Node<T> n : surface.getNodeList()) {
			double nx = n.percentToAproxX(surfaceArea);
            double ny = n.percentToAproxY(surfaceArea);
            double nw = n.percentToAproxWidth(surfaceArea);
            double nh = n.percentToAproxHeight(surfaceArea);
            
            ConnectType type = connectAdjacentHorizontalDividers(surface, n);
			if(type != ConnectType.BOTH) {
				Divider<T> div = null;
				
				if(ny > boundaryY && type != ConnectType.TOP) {
					div = new Divider<T>(nx, ny, nw, false);
					div.dividerSize = surface.getDividerSize();
					div.addNext(n);
					n.prevHorizontal = div;
					horizontalDividers.add(div);
				}
				if(ny + nh < surfaceArea.getHeight() - boundaryY &&
					type != ConnectType.BOTTOM) {
					div = new Divider<T>(nx, ny + nh, nw, false);
					div.dividerSize = surface.getDividerSize();
					div.addPrevious(n);
					n.nextHorizontal = div;
					horizontalDividers.add(div);
				}
			}
			
			type = connectAdjacentVerticalDividers(surface, n);
			if(type != ConnectType.BOTH) {
				Divider<T> div = null;
				if(nx > boundaryX && type != ConnectType.LEFT) {
					div = new Divider<T>(nx, ny, nh, true);
					div.dividerSize = surface.getDividerSize();
					div.addNext(n);
					n.prevVertical = div;
					verticalDividers.add(div);
				}
				if(nx + nw < surfaceArea.getWidth() - boundaryX &&
					type != ConnectType.RIGHT) {
					div = new Divider<T>(nx + nw, ny, nh, true);
					div.dividerSize = surface.getDividerSize();
					div.addPrevious(n);
					n.nextVertical = div;
					verticalDividers.add(div);
				}
			}
		}
	}
	
	/**
	 * Called by {@link #connectNodes()} to search for the horizontal dividers to
	 * connect the specified {@link Node} to if it exists. If a divider is found
	 * at any of the bounds specified by the bounds of the specified {@link Node},
	 * it is connected to that/those {@link Divider}(s) and the type of connection
	 * is returned so that we know what {@link Divider}s to create.  
	 * @param n		the {@link Node} to connect.
	 * @return	a {@link ConnectType} indicating what was connected.
	 */
	private ConnectType connectAdjacentHorizontalDividers(SurfacePriviledged<T> surface, Node<T> n) {
		Rectangle2D.Double surfaceArea = surface.getArea();
		double tolerance = DIVIDER_TOLERANCE_RATIO * (Math.max(surfaceArea.width, surface.getArea().height));
        boolean connectedTop = false;
        boolean connectedBottom = false;
        for(Divider<T> div : surface.getHorizontalDividers()) {
        	if(n.percentToAproxY(surface.getArea()) >= div.r.y - (tolerance) && 
        		n.percentToAproxY(surfaceArea) <= div.r.y + (tolerance) && 
        		div.overlapsRelative(n, surfaceArea)) {
        		
                div.addNext(n);
                n.prevHorizontal = div;
                div.length = Math.max(div.length, n.percentToAproxWidth(surfaceArea));
                div.addPoint(n.percentToAproxX(surfaceArea), 0);								//y is a don't care
                div.addPoint(n.percentToAproxX(surfaceArea) + n.percentToAproxWidth(surfaceArea), 0);	//y is a don't care
                connectedTop = true;
            }else if((n.percentToAproxY(surfaceArea) + n.percentToAproxHeight(surfaceArea)) >= div.r.y - (tolerance) && 
            	(n.percentToAproxY(surfaceArea) + n.percentToAproxHeight(surfaceArea)) <= div.r.y + (tolerance) && 
            	div.overlapsRelative(n, surfaceArea)) {
            	
                div.addPrevious(n);
                n.nextHorizontal = div;
                div.length = Math.max(div.length, n.percentToAproxWidth(surfaceArea));
                div.addPoint(n.percentToAproxX(surfaceArea), 0);
                div.addPoint(n.percentToAproxX(surfaceArea) + n.percentToAproxWidth(surfaceArea), 0);
                connectedBottom = true;
            }
        }
        
        return connectedTop && connectedBottom ? ConnectType.BOTH : 
        	connectedTop ? ConnectType.TOP : connectedBottom ? 
        		ConnectType.BOTTOM : ConnectType.NONE;
    }
    
	/**
	 * Called by {@link #connectNodes()} to search for the vertical dividers to
	 * connect the specified {@link Node} to if it exists. If a divider is found
	 * at any of the bounds specified by the bounds of the specified {@link Node},
	 * it is connected to that/those {@link Divider}(s) and the type of connection
	 * is returned so that we know what {@link Divider}s to create.  
	 * @param n		the {@link Node} to connect.
	 * @return	a {@link ConnectType} indicating what was connected.
	 */
    private ConnectType connectAdjacentVerticalDividers(SurfacePriviledged<T> surface, Node<T> n) {
    	Rectangle2D.Double surfaceArea = surface.getArea();
    	double tolerance = DIVIDER_TOLERANCE_RATIO * (Math.max(surfaceArea.width, surfaceArea.height));
        boolean connectedLeft = false;
        boolean connectedRight = false;
        for(Divider<T> div : surface.getVerticalDividers()) {
            if(n.percentToAproxX(surfaceArea) >= div.r.x - (tolerance) && n.percentToAproxX(surfaceArea) <= div.r.x + (tolerance) && div.overlapsRelative(n, surfaceArea)) {
                div.addNext(n);
                n.prevVertical = div;
                div.length = Math.max(div.length, n.percentToAproxHeight(surfaceArea));
                div.addPoint(0, n.percentToAproxY(surfaceArea)); 							//x is a don't care
                div.addPoint(0, n.percentToAproxY(surfaceArea) + n.percentToAproxHeight(surfaceArea));  //x is a don't care
                connectedLeft = true;
            }else if((n.percentToAproxX(surfaceArea) + n.percentToAproxWidth(surfaceArea)) >= div.r.x - (tolerance) &&
            	(n.percentToAproxX(surfaceArea) + n.percentToAproxWidth(surfaceArea)) <= div.r.x + (tolerance) && div.overlapsRelative(n, surfaceArea)) {
                div.addPrevious(n);
                n.nextVertical = div;
                div.length = Math.max(div.length, n.percentToAproxHeight(surfaceArea));
                div.addPoint(0, n.percentToAproxY(surfaceArea)); 							//x is a don't care
                div.addPoint(0, n.percentToAproxY(surfaceArea) + n.percentToAproxHeight(surfaceArea)); 	//x is a don't care
                connectedRight = true;
            }
        }
        
        return connectedLeft && connectedRight ? ConnectType.BOTH : 
        	connectedLeft ? ConnectType.LEFT : connectedRight ? 
            	ConnectType.RIGHT : ConnectType.NONE;
    }
    
    private void connectDividersForAdd(SurfacePriviledged<T> surface, Node<T> source, Node<T> target, Position p) {
    	source.disconnectFromDividers();
    	switch(p) {
			case NORTH : {
				if(target.prevHorizontal != null) {
					target.prevHorizontal.removeNode(target);
					source.prevHorizontal = target.prevHorizontal;
					source.prevHorizontal.addNext(source);
				}
				
				Divider<T> newDiv = new Divider<T>(source.r.x, source.r.getMaxY(), source.r.width, false);
				newDiv.dividerSize = surface.getDividerSize();
				newDiv.r.height = newDiv.dividerSize;
				newDiv.addPrevious(source);
				
				source.nextHorizontal = newDiv;
				newDiv.addNext(target);
				target.prevHorizontal = newDiv;
				
				if(target.prevVertical != null) {
					source.prevVertical = target.prevVertical;
					source.prevVertical.addNext(source);
					source.prevVertical.addPerpendicularJoin(newDiv, false);
				}
				if(target.nextVertical != null) {
					source.nextVertical = target.nextVertical;
					source.nextVertical.addPrevious(source);
					source.nextVertical.addPerpendicularJoin(newDiv, true);
				}
				
				surface.getHorizontalDividers().add(newDiv);
				break;
			}
			case SOUTH: {
				if(target.nextHorizontal != null) {
					target.nextHorizontal.removeNode(target);
					source.nextHorizontal = target.nextHorizontal;
					source.nextHorizontal.addPrevious(source);
				}
				
				Divider<T> newDiv = new Divider<T>(source.r.x, target.r.getMaxY(), source.r.width, false);
				newDiv.dividerSize = surface.getDividerSize();
				newDiv.r.height = newDiv.dividerSize;
				newDiv.addNext(source);
				
				source.prevHorizontal = newDiv;
				newDiv.addPrevious(target);
				target.nextHorizontal = newDiv;
				
				if(target.prevVertical != null) {
					source.prevVertical = target.prevVertical;
					source.prevVertical.addNext(source);
					source.prevVertical.addPerpendicularJoin(newDiv, false);
				}
				if(target.nextVertical != null) {
					source.nextVertical = target.nextVertical;
					source.nextVertical.addPrevious(source);
					source.nextVertical.addPerpendicularJoin(newDiv, true);
				}
				
				surface.getHorizontalDividers().add(newDiv);
				break;
	    	}
	    	case EAST: {
	    		if(target.nextVertical != null) {
	    			target.nextVertical.removeNode(target);
	    			source.nextVertical = target.nextVertical;
	    			source.nextVertical.addPrevious(source);
	    		}
	    		
	    		Divider<T> newDiv = new Divider<T>(target.r.getMaxX(), target.r.y, source.r.height, true);
	    		newDiv.dividerSize = surface.getDividerSize();
	    		newDiv.r.width = newDiv.dividerSize;
	    		newDiv.addNext(source);
	    		
	    		source.prevVertical = newDiv;
	    		newDiv.addPrevious(target);
	    		target.nextVertical = newDiv;
	    		
	    		if(target.prevHorizontal != null) {
	    			source.prevHorizontal = target.prevHorizontal;
	    			source.prevHorizontal.addNext(source);
	    			source.prevHorizontal.addPerpendicularJoin(newDiv, false);
	    		}
	    		if(target.nextHorizontal != null) {
	    			source.nextHorizontal = target.nextHorizontal;
	    			source.nextHorizontal.addPrevious(source);
	    			source.nextHorizontal.addPerpendicularJoin(newDiv, true);
	    		}
	    		
	    		surface.getVerticalDividers().add(newDiv);
	    		break;
	    	}
	    	case WEST: {
	    		if(target.prevVertical != null) {
	    			target.prevVertical.removeNode(target);
	    			source.prevVertical = target.prevVertical;
	    			source.prevVertical.addNext(source);
	    		}
	    		
	    		Divider<T> newDiv = new Divider<T>(source.r.getMaxX(), target.r.y, target.r.height, true);
	    		newDiv.dividerSize = surface.getDividerSize();
	    		newDiv.r.width = newDiv.dividerSize;
	    		newDiv.addPrevious(source);
	    		
	    		source.nextVertical = newDiv;
	    		newDiv.addNext(target);
	    		target.prevVertical = newDiv;
	    		
	    		if(target.prevHorizontal != null) {
	    			source.prevHorizontal = target.prevHorizontal;
	    			source.prevHorizontal.addNext(source);
	    			source.prevHorizontal.addPerpendicularJoin(newDiv, false);
	    		}
	    		if(target.nextHorizontal != null) {
	    			source.nextHorizontal = target.nextHorizontal;
	    			source.nextHorizontal.addPrevious(source);
	    			source.nextHorizontal.addPerpendicularJoin(newDiv, true);
	    		}
	    		
	    		surface.getVerticalDividers().add(newDiv);
	    		break;
	    	}
		}
    }
    
    Rectangle2D.Double[] getAddBounds(Node<T> source, Node<T> target, Position p, double dividerSize) {
    	Rectangle2D.Double retVal[] = new Rectangle2D.Double[2];
    	
    	switch(p) {
	    	case NORTH: //Allow fall-through
	    	case SOUTH: {
	    		double targetHeight = 0;
	    		double sourceHeight = 0;
	    		double halfHeight = Math.rint((target.r.height - dividerSize) / 2d);
	    		if(source.r.height > 0 && source.r.height <= halfHeight) {
	    			targetHeight = target.r.height - (source.r.height + dividerSize);
	    			sourceHeight = target.r.height - (targetHeight + dividerSize);
	    		}else{
	    			targetHeight = sourceHeight = halfHeight;
	    		}
	    		//Not enough v room for source
//	    		if(targetHeight == target.r.height || target.r.height - targetHeight - dividerSize < source.getMinHeight() ||
//	    			source.getMinWidth() > target.r.width) {
//	    			return retVal;
//	    		}
	    		
	    		retVal[TARGET_IDX] = new Rectangle2D.Double(target.r.x, p == Position.NORTH ? target.r.y + sourceHeight + dividerSize : target.r.y,
	    	    	target.r.width, targetHeight);
	    		retVal[SOURCE_IDX] = new Rectangle2D.Double(target.r.x, p == Position.NORTH ? target.r.y : target.r.y + targetHeight + dividerSize,
	    			target.r.width, sourceHeight);
	    		
	    		break;
	    	}
	    	case WEST: //Allow fall-through
	    	case EAST: {
	    		double targetWidth = 0;
	    		double sourceWidth = 0;
	    		double halfWidth = Math.rint((target.r.width - dividerSize) / 2d);
	    		if(source.r.width > 0 && source.r.width <= halfWidth) {
	    			targetWidth = target.r.width - (source.r.width + dividerSize);
	    			sourceWidth = target.r.width - (targetWidth + dividerSize);
	    		}else{
	    			targetWidth = sourceWidth = halfWidth;
	    		}	
	    		
	    		//Not enough h room for source
//	    		if(targetWidth == target.r.width || target.r.width - targetWidth - dividerSize < source.getMinWidth() ||
//	    			source.getMinHeight() > target.r.height) {
//	    			return retVal;
//	    		}
	    		retVal[TARGET_IDX] = new Rectangle2D.Double(p == Position.WEST ? target.r.x + sourceWidth + dividerSize : target.r.x, target.r.y,
	    	    	targetWidth, target.r.height);
	    		retVal[SOURCE_IDX] = new Rectangle2D.Double(p == Position.WEST ? target.r.x : target.r.x + targetWidth + dividerSize, target.r.y,
	    			sourceWidth, target.r.height);
	    		break;
	    	}
    	}
    	
    	return retVal;
    }
    
    void beginDropElement(SurfacePriviledged<T> surface, Node<T> source) {
    	surface.snapshotLayout();
		
		source.force(surface, ChangeType.MOVE_BEGIN);
		
		List<Node<T>> affectedNodes = requestRemoveElement(surface, source);
		
		if(!affectedNodes.isEmpty()) {
			adjustWeights(surface);
			
			for(Node<T> n : affectedNodes) {
				if(n == source) continue;
				n.set(surface, ChangeType.ANIMATE_RESIZE_RELOCATE);
			}
			
			surface.removeNodeReferences(source);
			
			surface.getPathIterator().assemblePaths(surface.getRoot());
			
			surface.snapshotInterimLayout();
		}else{
			//////////  VETO REMOVE /////////
		}
    }
    
    void testDropElement(SurfacePriviledged<T> surface, LayoutImpl<T> interimLayout, Node<T> source, Node<T> target, Position p) {
    	Rectangle2D.Double[] bounds = getAddBounds(surface.getSnapshot().getNode(source.stringID), interimLayout.getNode(target.stringID), p, surface.getDividerSize());
    	if(bounds[TARGET_IDX] != null) {
    		source.r.setFrame(bounds[SOURCE_IDX]);
    		target.r.setFrame(bounds[TARGET_IDX]);
    		
    		source.force(surface, ChangeType.RELOCATE_DRAG_TARGET);
    		source.force(surface, ChangeType.RESIZE_DRAG_TARGET);
    		target.force(surface, ChangeType.ANIMATE_RESIZE_RELOCATE);
    		
    		surface.setHasValidDrop(true);
    	}
    }
    
    void rejectDropElement(SurfacePriviledged<T> surface, LayoutImpl<T> interimLayout, Node<T> source, Node<T> target) {
    	Node<T> startNode = surface.getSnapshot().getNode(source.stringID);
    	source.r.setFrame(source.r.x, source.r.y, startNode.r.width, startNode.r.height);
    	source.force(surface, ChangeType.RELOCATE_DRAG_TARGET);
		source.force(surface, ChangeType.RESIZE_DRAG_TARGET);
		
    	target.r.setFrame(interimLayout.getNode(target.stringID).r);
    	target.force(surface, ChangeType.ANIMATE_RESIZE_RELOCATE);
    	
    	surface.setHasValidDrop(false);
    	
    	for(Node<T> n : surface.getNodeList()) {
    		n.r.setFrame(interimLayout.getNode(n.stringID).r);
	    	n.force(surface, ChangeType.RESIZE_RELOCATE);
    	}
    }
    
    @SuppressWarnings("unchecked")
    void commitDropElement(SurfacePriviledged<T> surface, Node<T> source, Node<T> target, Position p) {
    	surface.getNodeList().add(source);
	    connectDividersForAdd(surface, source, target, p);
	    
		//Swap root if new node is added in root position (0,0)
		if(source.r.x == 0 && source.r.y == 0 && (p == Position.NORTH || p == Position.WEST)) {
			surface.setRoot(source);
		}
		
		surface.getPathIterator().assemblePaths(surface.getRoot());
		
		adjustWeights(surface);
		 
		source.force(surface, ChangeType.ANIMATE_RESIZE_RELOCATE);
		target.force(surface, ChangeType.ANIMATE_RESIZE_RELOCATE);
		
		((LayoutImpl<T>)surface.getLayout()).setRelative(false);
		((LayoutImpl<T>)surface.getLayout()).put(source.stringID, source.getTarget());
		((LayoutImpl<T>)surface.getLayout()).addCell(source.stringID, source.r.x, source.r.y, source.r.width, 
			source.r.height, source.getMinWidth(), source.getMaxWidth(), source.getMinHeight(), source.getMaxHeight(),
				source.getHorizontalWeight(), source.getVerticalWeight());
		
		surface.updateLayoutSerializables(false);
		
		source.force(surface, ChangeType.MOVE_END);
		surface.setHasValidDrop(false);
   }
    
    @SuppressWarnings("unchecked")
    void cancelDropElement(SurfacePriviledged<T> surface, Node<T> source) {
	    //Temporarily enforce "snap back"
	    surface.revertLayout();
		
	    //requestLayout(surface);
	    for(Node<T> n : surface.getNodeList()) {
		    ((LayoutImpl<T>)surface.getLayout()).getNode(n.stringID).force(surface, ChangeType.RESIZE_RELOCATE);
	    }
	    
	    source.force(surface, ChangeType.MOVE_END);
		surface.setHasValidDrop(false);
    }
    
    @SuppressWarnings("unchecked")
	void requestAddElement(SurfacePriviledged<T> surface, Node<T> source, Node<T> target, Position p) {
    	Rectangle2D.Double[] bounds = getAddBounds(source, target, p, surface.getDividerSize());
    	if(bounds[TARGET_IDX] != null) {
    		source.r.setFrame(bounds[SOURCE_IDX]);
    		target.r.setFrame(bounds[TARGET_IDX]);
    		surface.getNodeList().add(source);
    		connectDividersForAdd(surface, source, target, p);
    		
    		//Swap root if new node is added in root position (0,0)
    		if(source.r.x == 0 && source.r.y == 0 && (p == Position.NORTH || p == Position.WEST)) {
    			surface.setRoot(source);
    		}
    		
    		surface.getPathIterator().assemblePaths(surface.getRoot());
    		
    		adjustWeights(surface);
    		
    		source.set(surface, ChangeType.ADD_COMMIT);
    		target.set(surface, ChangeType.RESIZE_RELOCATE);
    		
    		((LayoutImpl<T>)surface.getLayout()).setRelative(false);
    		((LayoutImpl<T>)surface.getLayout()).put(source.stringID, source.getTarget());
    		((LayoutImpl<T>)surface.getLayout()).addCell(source.stringID, source.r.x, source.r.y, source.r.width, 
    			source.r.height, source.getMinWidth(), source.getMaxWidth(), source.getMinHeight(), source.getMaxHeight(),
    				source.getHorizontalWeight(), source.getVerticalWeight());
    		
    		surface.updateLayoutSerializables(false);
    	}
    }
    
    
    
    /**
     * Removes the specified {@link Node} from all of its dependencies
     * and adjusts the virtual "view" elements to reflect the change.
     * 
     * @param surface	the Surface containing the specified Node
     * @param node		the Node to remove.
     */
    List<Node<T>> requestRemoveElement(SurfacePriviledged<T> surface, Node<T> node) {
    	List<Node<T>> affectedNodes = new ArrayList<Node<T>>();
    	
    	ConnectType vType = getVerticalMergeOptions(node);
		ConnectType hType = getHorizontalMergeOptions(node);
		
		if(vType != ConnectType.NONE || hType != ConnectType.NONE) {
			if(vType != ConnectType.NONE) {
				switch(vType) {
					case TOP: {
						affectedNodes = mergeDividers(surface, node.prevHorizontal, node.nextHorizontal, 
							node.nextHorizontal == null ? surface.getArea().height : node.nextHorizontal.r.y);
						break;
					}
					case BOTTOM: {
						affectedNodes = mergeDividers(surface, node.nextHorizontal, node.prevHorizontal, 
							node.prevHorizontal == null ? -surface.getDividerSize() : node.prevHorizontal.r.y);
						break;
					}
					case BOTH: {
						affectedNodes = mergeDividers(surface, node.prevHorizontal, node.nextHorizontal, 
							node.r.getMaxY() - Math.rint(node.r.height / 2));
						break;
					}
                    default:
                        break;
				}
			}else{
				switch(hType) {
					case LEFT: {
						affectedNodes = mergeDividers(surface, node.prevVertical, node.nextVertical, 
							node.nextVertical == null ? surface.getArea().width : node.nextVertical.r.x);
						break;
					}
					case RIGHT: {
						affectedNodes = mergeDividers(surface, node.nextVertical, node.prevVertical, 
							node.prevVertical == null ? -surface.getDividerSize() : node.prevVertical.r.x);
						break;
					}
					case BOTH: {
						affectedNodes = mergeDividers(surface, node.prevVertical, node.nextVertical, 
							node.r.getMaxX() - Math.rint(node.r.width / 2));
						break;
					}
                    default:
                        break;
				}
			}
		}else{
			vType = getVerticalMergeOptionsLevel2(surface, node);
			hType = getHorizontalMergeOptionsLevel2(surface, node);
			if(vType != ConnectType.NONE || hType != ConnectType.NONE) {
				if(vType != ConnectType.NONE) {
					switch(vType) {
						case TOP: {
							affectedNodes = mergeDividers(surface, node.prevHorizontal, node.nextHorizontal, 
								node.nextHorizontal == null ? surface.getArea().height : node.nextHorizontal.r.y);
							break;
						}
						case BOTTOM: {
							affectedNodes = mergeDividers(surface, node.nextHorizontal, node.prevHorizontal, 
								node.prevHorizontal == null ? -surface.getDividerSize() : node.prevHorizontal.r.y);
							break;
						}
						case BOTH: {
							affectedNodes = mergeDividers(surface, node.prevHorizontal, node.nextHorizontal, 
								node.r.getMaxY() - Math.rint(node.r.height / 2));
							break;
						}
                        default:
                            break;
					}
				}else{
					switch(hType) {
						case LEFT: {
							affectedNodes = mergeDividers(surface, node.prevVertical, node.nextVertical, 
								node.nextVertical == null ? surface.getArea().width : node.nextVertical.r.x);
							break;
						}
						case RIGHT: {
							affectedNodes = mergeDividers(surface, node.nextVertical, node.prevVertical, 
								node.prevVertical == null ? -surface.getDividerSize() : node.prevVertical.r.x);
							break;
						}
						case BOTH: {
							affectedNodes = mergeDividers(surface, node.prevVertical, node.nextVertical, 
								node.r.getMaxX() - Math.rint(node.r.width / 2));
							break;
						}
                        default:
                            break;
					}
				}
			}else{
				vType = getVerticalMergeOptionsLevel3(surface, node);
				hType = getHorizontalMergeOptionsLevel3(surface, node);
				
				if(vType != ConnectType.NONE || hType != ConnectType.NONE) {
					if(vType == ConnectType.BOTH) {
						affectedNodes = mergeDividersVerticallyWithPeripheralMove(surface, node);
					}else{
						affectedNodes = mergeDividersHorizontallyWithPeripheralMove(surface, node);
					}
				}
			}
		}
		//Cache created from getVertical/Horizontal...OptionsLevel2()
		surface.clearSearchResults();
	
		return affectedNodes;
    }
    
    /**
     * Returns a ConnectType enum internally overloaded to return a reference to the side
     * or side(s) which can can be moved. This method checks for the option of vertically
     * merging two Horizontal Dividers and requires that the Divider length be the same
     * length as the specified Node, and have as it's only next or previous node, the 
     * specified Node.
     * 
     * @param node	the Node being moved or added
     * @return		a ConnectType of {@link ConnectType#TOP}, {@link ConnectType#BOTTOM},
     * 				{@link ConnectType#BOTH}, or {@link ConnectType#NONE} 
     */
    private ConnectType getVerticalMergeOptions(Node<T> node) {
    	ConnectType type = ConnectType.NONE;
    	if(node.prevHorizontal != null && node.prevHorizontal.r.width == node.r.width) {
    		type = ConnectType.TOP;
    	}
    	
    	if(node.nextHorizontal != null && node.nextHorizontal.r.width == node.r.width){
    		if(type == ConnectType.TOP) {
    			type = ConnectType.BOTH;
    		}else{
    			type = ConnectType.BOTTOM;
    		}
    	}
    	
    	return type;
    }
    
    /**
     * Returns a ConnectType enum internally overloaded to return a reference to the side
     * or side(s) which can can be moved. This method checks for the option of vertically
     * merging two Horizontal Dividers - the same as {@link #getVerticalMergeOptions(Node)}
     * except that it does additional checking for overlapping dividers that lie between
     * the Dividers bordering the specified Node. 
     * <p>
     * If there exists an overlapping divider preventing only ONE side from moving, the
     * other side is returned - otherwise BOTH or NONE are returned.
     * 
     * @param surface	the {@link SurfacePriviledged} containing the {@link Element}s to be moved.
     * @param node		the Node being moved or added
     * @return			a ConnectType of {@link ConnectType#TOP}, {@link ConnectType#BOTTOM},
     * 					{@link ConnectType#BOTH}, or {@link ConnectType#NONE}
     */
    private ConnectType getVerticalMergeOptionsLevel2(SurfacePriviledged<T> surface, Node<T> node) {
    	ConnectType type = ConnectType.NONE;
    	List<Divider<T>> exclude = new ArrayList<Divider<T>>();
    	exclude.add(node.prevHorizontal);
    	exclude.add(node.nextHorizontal);
    	
    	if(node.prevHorizontal != null && node.nextHorizontal != null) {
	    	List<Divider<T>> prevOl = getAllOverlappingDividersBetween(surface, node.prevHorizontal, node.prevHorizontal.r.y, node.nextHorizontal.r.y, exclude);
	    	if(prevOl.isEmpty()) {
	    		type = ConnectType.TOP;
	    	}
	    	
	    	List<Divider<T>> nextOl = getAllOverlappingDividersBetween(surface, node.nextHorizontal, node.prevHorizontal.r.y, node.nextHorizontal.r.y, exclude);
	    	if(type == ConnectType.TOP && nextOl.isEmpty()) {
				type = ConnectType.BOTH;
			}else if(nextOl.isEmpty()){
				type = ConnectType.BOTTOM;
			}
	    	
	    	if(!prevOl.isEmpty() && !nextOl.isEmpty()) {
	    		//Temporarily store results in surface
	    		surface.storeSearchResults(node.prevHorizontal, prevOl);
	    		surface.storeSearchResults(node.nextHorizontal, nextOl);
	    	}
    	}
    	
    	return type;
    }
    
    /**
     * Returns a ConnectType enum internally overloaded to return a reference to the side
     * or side(s) which can can be moved. In addition, this method will check to see if
     * peripheral dividers can be moved on BOTH sides by doing other checks such as obstructing
     * Dividers for one side not being the same obstructing Dividers for the other side.
     * 
     * @param surface	the {@link SurfacePriviledged} containing the {@link Element}s to be moved.
     * @param node		the Node being moved or added
     * @return			a ConnectType of {@link ConnectType#TOP}, {@link ConnectType#BOTTOM},
     * 					{@link ConnectType#BOTH}, or {@link ConnectType#NONE}
     */
    private ConnectType getVerticalMergeOptionsLevel3(SurfacePriviledged<T> surface, Node<T> node) {
    	//vMergeTop refers to the leading vertical joins of the top horizontal divider
    	List<Divider<T>> vMergeTop = surface.getSearchResults(node.prevHorizontal);
    	//vMergeBottom refers to the trailing vertical joins of the bottom horizontal divider
		List<Divider<T>> vMergeBottom = surface.getSearchResults(node.nextHorizontal);
    	return (vMergeTop != null && vMergeBottom != null && Collections.disjoint(vMergeTop, vMergeBottom)) ?
    		ConnectType.BOTH : ConnectType.NONE;
    }
    
    /**
     * Returns a ConnectType enum internally overloaded to return a reference to the side
     * or side(s) which can can be moved. This method checks for the option of horizontally
     * merging two Vertical Dividers and requires that the Divider length be the same
     * length as the specified Node, and have as it's only next or previous node, the 
     * specified Node.
     * 
     * @param node	the Node being moved or added
     * @return		a ConnectType of {@link ConnectType#LEFT}, {@link ConnectType#RIGHT},
     * 				{@link ConnectType#BOTH}, or {@link ConnectType#NONE} 
     */
    private ConnectType getHorizontalMergeOptions(Node<T> node) {
    	ConnectType type = ConnectType.NONE;
    	if(node.prevVertical != null && node.prevVertical.r.height == node.r.height) {
    		type = ConnectType.LEFT;
    	}
    	
    	if(node.nextVertical != null && node.nextVertical.r.height == node.r.height){
    		if(type == ConnectType.LEFT) {
    			type = ConnectType.BOTH;
    		}else{
    			type = ConnectType.RIGHT;
    		}
    	}
    	
    	return type;
    }
    
    /**
     * Returns a ConnectType enum internally overloaded to return a reference to the side
     * or side(s) which can can be moved. This method checks for the option of horizontally
     * merging two Vertical Dividers - the same as {@link #getHorizontalMergeOptions(Node)}
     * except that it does additional checking for overlapping dividers that lie between
     * the Dividers bordering the specified Node. 
     * <p>
     * If there exists an overlapping divider preventing only ONE side from moving, the
     * other side is returned - otherwise BOTH or NONE are returned.
     * 
     * @param surface	the {@link SurfacePriviledged} containing the {@link Element}s to be moved.
     * @param node		the Node being moved or added
     * @return			a ConnectType of {@link ConnectType#LEFT}, {@link ConnectType#RIGHT},
     * 					{@link ConnectType#BOTH}, or {@link ConnectType#NONE}
     */
    private ConnectType getHorizontalMergeOptionsLevel2(SurfacePriviledged<T> surface, Node<T> node) {
    	ConnectType type = ConnectType.NONE;
    	List<Divider<T>> exclude = new ArrayList<Divider<T>>();
    	exclude.add(node.prevVertical);
    	exclude.add(node.nextVertical);
    	
    	if(node.prevVertical != null && node.nextVertical != null) {
	    	List<Divider<T>> prevOl = getAllOverlappingDividersBetween(surface, node.prevVertical, node.prevVertical.r.x, node.nextVertical.r.x, exclude);
	    	if(prevOl.isEmpty()) {
	    		type = ConnectType.LEFT;
	    	}
	    	
	    	List<Divider<T>> nextOl = getAllOverlappingDividersBetween(surface, node.nextVertical, node.prevVertical.r.x, node.nextVertical.r.x, exclude);
	    	if(type == ConnectType.LEFT && nextOl.isEmpty()) {
				type = ConnectType.BOTH;
			}else if(nextOl.isEmpty()){
				type = ConnectType.RIGHT;
			}
	    	
	    	if(!prevOl.isEmpty() && !nextOl.isEmpty()) {
	    		//Temporarily store results in surface
	    		surface.storeSearchResults(node.prevVertical, prevOl);
	    		surface.storeSearchResults(node.nextVertical, nextOl);
	    	}
    	}
    	
    	return type;
    }
    
    /**
     * Returns a ConnectType enum internally overloaded to return a reference to the side
     * or side(s) which can can be moved. In addition, this method will check to see if
     * peripheral dividers can be moved on BOTH sides by doing other checks such as obstructing
     * Dividers for one side not being the same obstructing Dividers for the other side.
     * 
     * @param surface	the {@link Surface} containing the {@link Element}s to be moved.
     * @param node		the Node being moved or added
     * @return			a ConnectType of {@link ConnectType#LEFT}, {@link ConnectType#RIGHT},
     * 					{@link ConnectType#BOTH}, or {@link ConnectType#NONE}
     */
    private ConnectType getHorizontalMergeOptionsLevel3(SurfacePriviledged<T> surface, Node<T> node) {
    	//hMergeLeft refers to the leading horizontal joins of the left vertical divider
    	List<Divider<T>> hMergeLeft = surface.getSearchResults(node.prevVertical);
    	//hMergeRight refers to the trailing horizontal joins of the right vertical divider
		List<Divider<T>> hMergeRight = surface.getSearchResults(node.nextVertical);
    	return (hMergeLeft != null && hMergeRight != null && Collections.disjoint(hMergeLeft, hMergeRight)) ?
    		ConnectType.BOTH : ConnectType.NONE;
    }
    
    /**
	 * User mouse events are delegated to this class via the {@link MosaicEngineImpl}
	 * which then invokes certain actions on the engines internal model.
	 * 
	 * Instances of this class are created by {@link SurfacePriviledged}
	 * 
	 * @author David Ray
	 *
	 */
	class InputManager {
		Element<T> selectedElement;
		Element<T> altElement;
		double dragXOffset;
		double dragYOffset;
		double dividerSize;
		
		boolean isDraggingNode;
		
		SurfacePriviledged<T> surface;
		
		List<Node<T>> affectedNodes;
		
		List<Element<T>> searchResults = new ArrayList<Element<T>>();
		
		InputManager(SurfacePriviledged<T> surface) {
			this.surface = surface;
			this.dividerSize = surface.getDividerSize();
		}
		
		double distance(double x1, double y1, double x2, double y2) {
			x1 -= x2;
	        y1 -= y2;
	        return Math.sqrt(x1 * x1 + y1 * y1);
		}
		
		Corner withinCornerRadius(Node<T> n, double x, double y) {
			double nx = n.r.x;
			double ny = n.r.y;
			double nMaxX = n.r.getMaxX();
			double nMaxY = n.r.getMaxY();
			
			double cornerClickRadius = surface.getCornerClickRadius();
			
			Corner retVal = distance(nx, ny, x, y) <= cornerClickRadius ? Corner.NW :
				distance(nx, nMaxY, x, y) <= cornerClickRadius ? Corner.SW :
					distance(nMaxX, ny, x, y) <= cornerClickRadius ? Corner.NE :
						distance(nMaxX, nMaxY, x, y) <= cornerClickRadius ? Corner.SE : null;
			
			return retVal;
		}
		
		List<Element<T>> findElements(double x, double y) {
			Corner corner = null;
			searchResults.clear();
			List<Node<T>> searchList = surface.getNodeList();
			for(Node<T> n : searchList) { 
				if(n.r.contains(x, y)) {
					searchResults.add(n);
					break;
				}else if((corner = withinCornerRadius(n, x, y)) != null) {
					switch(corner) {
						case NW: {
							searchResults.add(n.prevVertical);
							searchResults.add(n.prevHorizontal);
							break;
						}
						case SW: {
							searchResults.add(n.prevVertical);
							searchResults.add(n.nextHorizontal);
							break;
						}
						case NE: {
							searchResults.add(n.nextVertical);
							searchResults.add(n.prevHorizontal);
							break;
						}
						case SE: {
							searchResults.add(n.nextVertical);
							searchResults.add(n.nextHorizontal);
							break;
						}
					}
				}
			}
			
			if(searchResults.size() == 0) {
				for(Divider<T> d : surface.getVerticalDividers()) {
					if(d.r.contains(x, y)) {
						searchResults.add(d);
						break;
					}
				}
			}
			
			if(searchResults.size() == 0) {
				for(Divider<T> d : surface.getHorizontalDividers()) {
					if(d.r.contains(x, y)) {
						searchResults.add(d);
						break;
					}
				}
			}
			
			return searchResults;
		}
		
		void selectElement(List<Element<T>> elems, double x, double y) {
			if(elems.size() < 1) return;
			
			Element<T> e = this.selectedElement = elems.get(0);
			Element<T> alt = this.altElement = elems.size() > 1 ? elems.get(1) : null;
			
			System.out.println("\tselected e " + e + "\n\t alt " + alt);
			
			if(e.type == ElementType.DIVIDER) {
				System.out.println("Divider " + e.stringID + "'s leading: ");
				for(Divider<T> d : ((Divider<T>)e).leadingJoins) {
					System.out.println("\t " + d);
				}
				System.out.println("Divider " + e.stringID + "'s trailing: ");
				for(Divider<T> d : ((Divider<T>)e).trailingJoins) {
					System.out.println("\t " + d);
				}
				//must use the perpendicular's x/y when alt isn't null and calculating alternate axis
				dragXOffset = e.type == ElementType.DIVIDER && ((Divider<T>)e).isVertical || alt == null ? x - e.r.x : x - alt.r.x;
				dragYOffset = e.type == ElementType.DIVIDER && ((Divider<T>)e).isVertical && alt != null ? y - alt.r.y : y - e.r.y;
				
			}else{
				dragXOffset = x - selectedElement.r.x;
				dragYOffset = y - selectedElement.r.y;
			}
			
			
		}
		
		private Position getDragQuadrant(Rectangle2D.Double r, double x, double y) {
			x -= r.x;
			y -= r.y;
			if(x < r.width / 4 && y > r.height / 3 && y < r.height - (r.height / 3)) {
				return Position.WEST;
			}else if(x > r.width - (r.width / 4) && y > r.height / 3 && y < r.height - (r.height / 3)) {
				return Position.EAST;
			}else if(y < r.height / 4 && x > r.width / 3 && x < r.width - (r.width / 3)) {
				return Position.NORTH;
			}else if(y > r.height - (r.height / 4) && x > r.width / 3 && x < r.width - (r.width / 3)) {
				return Position.SOUTH;
			}
			
			return null;
		}
		
		Node<T> getDragOverNode(SurfacePriviledged<T> surface, LayoutImpl<T> layout, double x, double y) {
			Node<T> retVal = null;
			if(retVal == null) {
				for(Node<T> n : layout.getNodeList()) {
					if(n.r.contains(x, y)) {
						retVal = surface.getNode(n.stringID);
						break;
					}
				}
			}
			return retVal;
		}
		
		Node<T> lastDragOver;
		Position lastQuadrant;
		
		void dragElement(SurfacePriviledged<T> surface, double x, double y) {
			if(selectedElement == null) return;

			switch(selectedElement.type) {
				case NODE: {
					Point2D.Double dragPoint = moveDragPoint(new Point2D.Double(selectedElement.r.x, selectedElement.r.y), surface.getArea(), x, y, dragXOffset, dragYOffset);
					selectedElement.r.x = dragPoint.x;
					selectedElement.r.y = dragPoint.y;
					LayoutImpl<T> removalSnapshot = surface.getInterimSnapshot();
					if(isDraggingNode) {
						Node<T> currentDragOver =  getDragOverNode(surface, removalSnapshot, x, y);
						if(currentDragOver != null) {
							lastDragOver = currentDragOver;
							
							Position p = getDragQuadrant(removalSnapshot.getNode(currentDragOver.stringID).r, x, y);
							lastQuadrant = p;
							if(p != null) {
								testDropElement(surface, removalSnapshot, (Node<T>)selectedElement, currentDragOver, p);
							}else{
								rejectDropElement(surface, removalSnapshot, (Node<T>)selectedElement, currentDragOver);
							}
						}else if(lastDragOver != null){
							rejectDropElement(surface, removalSnapshot, (Node<T>)selectedElement, lastDragOver);
						}
						
						selectedElement.set(surface, ChangeType.RELOCATE_DRAG_TARGET);
					}else{
						surface.setLocked(true);
						beginDropElement(surface, (Node<T>)selectedElement);
					}
					
					isDraggingNode = true;
					break;
				}
				case DIVIDER: {
					Rectangle2D.Double maxMoveBounds = getMaxMoveBounds(surface.getArea(), (Divider<T>)selectedElement);
					Rectangle2D.Double altMaxMoveBounds = null;
					if(altElement != null) {
						altMaxMoveBounds = getMaxMoveBounds(surface.getArea(), (Divider<T>)altElement);
						maxMoveBounds = addPerpendicularBounds(maxMoveBounds, altMaxMoveBounds, ! ((Divider<T>)selectedElement).isVertical);
					}
					Point2D.Double dividerLocation = new Point2D.Double(selectedElement.r.x, selectedElement.r.y);
					Point2D.Double dragPoint = moveDragPoint(dividerLocation, maxMoveBounds, x, y, dragXOffset, dragYOffset);
					dragPoint = snapDragPoint(surface, (Divider<T>)selectedElement, dragPoint, maxMoveBounds);
					if(altElement != null) {
						dragPoint = snapDragPoint(surface, (Divider<T>)altElement, dragPoint, maxMoveBounds);
					}
					
					moveDividerWithinMinMax(surface, (Divider<T>)selectedElement, (Divider<T>)altElement, maxMoveBounds, dragPoint, dividerSize);
					adjustWeights(surface);
					break;
				}
			}
		}
		
		void releaseElement(SurfacePriviledged<T> surface) {
			if(selectedElement == null) return;
			
			switch(selectedElement.type) {
				case NODE: {
					if(isDraggingNode) {
						isDraggingNode = false;
						
						if(surface.hasValidDrop()) { 
							commitDropElement(surface, (Node<T>)selectedElement, lastDragOver, lastQuadrant);
						}else{
							cancelDropElement(surface, (Node<T>)selectedElement);
						}
					}
					surface.setLocked(false);
					break;
				}
				case DIVIDER: {
					System.out.println("releaseElement: Divider");
					break;
				}
			}
		}
	}
}
