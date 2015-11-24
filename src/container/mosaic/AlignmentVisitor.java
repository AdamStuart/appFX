package container.mosaic;

import java.awt.geom.Rectangle2D;
import java.util.List;

import container.mosaic.PathIterator.HorizontalPath;
import container.mosaic.PathIterator.VerticalPath;

/**
 * Implementation of the PathVisitor and ElementVisitor interfaces which 
 * precise "left-over" spacing on the {@link Node}s, and then work on each
 * {@link Divider} to accurately compute its bounds, and to establish
 * perpendicular relationships (joins) to other {@code Divider}s.
 */
class AlignmentVisitor<T> implements PathVisitor<T>, ElementVisitor<T> {
	private SurfacePriviledged<T> surface;
	
	/**
	 * Constructs a new AlignmentVisitor
	 */
	public AlignmentVisitor(SurfacePriviledged<T> surface) {
		this.surface = surface;
	}
	
	public void setSurface(SurfacePriviledged<T> surface) {
    	this.surface = surface;
    }

	/**
	 * Makes sure each node is flush with its bounding divider(s) on
	 * both the left and right sides.
	 */
	@Override
	public void visitHorizontal(Divider<T> s) {
		for(Node<T> n : s.previousNodes()) {
			n.r.width += (s.r.getX() - n.r.getMaxX());
			n.r.width = Math.ceil(n.r.width);
		}
		for(Node<T> n : s.nextNodes()) {
			double delta = s.r.getMaxX() - n.r.getX();
			n.r.x += delta;
			n.r.width = n.r.width - delta;
		}
		
		Rectangle2D.Double surfaceArea = surface.getArea();
		
		for(Divider<T> d : surface.getHorizontalDividers()) {
			//If d's x's fall within the detected extremes (end juts into divider s) and haven't been corrected
			if(d.r.y > s.r.y && d.r.y < s.r.getMaxY() && (surface.getIsInit() || (d.r.x != s.r.getMaxX()) && (d.r.getMaxX() != s.r.x)) && 
				((d.r.x >= s.r.x - 2 && d.r.x <= s.r.getMaxX() + 2) || (d.r.getMaxX() >= s.r.x - 2 && d.r.getMaxX() < s.r.getMaxX() + 2))){
				
				if(d.r.x >= s.r.x - 2 && d.r.x <= s.r.getMaxX() + 2) {
					d.r.x = s.r.getMaxX();
					Node<T> distal = d.getMostDistalNode();
					d.r.width = Math.min(surfaceArea.width, distal.r.getMaxX() - d.r.x);
					s.addPerpendicularJoin(d, false);
				}else if(d.r.getMaxX() >= s.r.x - 2 && d.r.getMaxX() < s.r.getMaxX() + 2) {
					d.r.width = Math.min(surfaceArea.width, s.r.x - d.r.x);
					s.addPerpendicularJoin(d, true);
				}				
			}
		}
		
		for(Divider<T> d : surface.getHorizontalDividers()) {
			d.r.x = d.getMostProximalNode().r.x;
			d.r.width = Math.min(surfaceArea.width, d.getMostDistalNode().r.getMaxX() - d.r.x);
		}
	}

	/**
	 * Makes sure each node is flush with its bounding divider(s) on
	 * both the top and bottom sides.
	 */
	@Override
	public void visitVertical(Divider<T> s) {
		for(Node<T> n : s.previousNodes()) {
			n.r.height += (s.r.getY() - n.r.getMaxY());
			n.r.height = Math.ceil(n.r.height);
		}
		for(Node<T> n : s.nextNodes()) {
			double delta = s.r.getMaxY() - n.r.getY();
			n.r.y += delta;
			n.r.height -= delta;
		}
		
		Rectangle2D.Double surfaceArea = surface.getArea();
		
		for(Divider<T> d : surface.getVerticalDividers()) {
			//If d's y's fall within the detected extremes (end juts into divider s) and haven't been corrected
			if(d.r.x > s.r.x && d.r.x < s.r.getMaxX() && (surface.getIsInit() || (d.r.y != s.r.getMaxY()) && (d.r.getMaxY() != s.r.y)) &&
				((d.r.y >= s.r.y - 2 && d.r.y <= s.r.getMaxY() + 2) || (d.r.getMaxY() >= s.r.y - 2 && d.r.getMaxY() < s.r.getMaxY() + 2))) {
				
				if(d.r.y >= s.r.y - 2 && d.r.y <= s.r.getMaxY() + 2) {
					d.r.y = s.r.getMaxY();
					Node<T> distal = d.getMostDistalNode();
					d.r.height = Math.min(surfaceArea.height, distal.r.getMaxY() - d.r.y);
					s.addPerpendicularJoin(d, false);
				}else if(d.r.getMaxY() >= s.r.y - 2 && d.r.getMaxY() < s.r.getMaxY() + 2) {
					d.r.height = Math.min(surfaceArea.height, s.r.y - d.r.y);
					s.addPerpendicularJoin(d, true);
				}
			}
		}
		
		for(Divider<T> d : surface.getVerticalDividers()) {
			d.r.y = d.getMostProximalNode().r.y;
			d.r.height = Math.min(surfaceArea.height, d.getMostDistalNode().r.getMaxY() - d.r.y);
		}
	}

	/**
	 * Tracks "left-over" pixels along the horizontal, and assigns
	 * them to the last object along the horizontal path. Then calls
	 * {@link #setHorizontalWeights(List)} to set the final weights
	 * used to govern each element's size from this point on.
	 * 
	 * @param hp		The {@link HorizontalPath} to have each element
	 * 					"visit" during the second dispatch.
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void visit(HorizontalPath hp) {
		double leftOverWidth = surface.getArea().getWidth();
        List<?> l = hp.getElements();
        int len = l.size();
        Element<T> e = null;
        for(int i = 0;i < len;i++) {
        	e = (Element<T>)l.get(i);
            e.acceptHorizontal(this);
            e.set(surface, ChangeType.RESIZE_RELOCATE);
            
            leftOverWidth -= e.r.width;
        }
        //Add back in any fractions left over due to 
        //non-integer resolution - can't set the weights
        //until after this adjustment for perfect layout.
        if(surface.getUseIntegerPrecision()) {
        	e.r.width += Math.round(leftOverWidth);	
        	
        }else{
        	e.r.width += leftOverWidth;
        }
        e.set(surface, ChangeType.RESIZE_RELOCATE);
	}

	/**
	 * Tracks "left-over" pixels along the vertical, and assigns
	 * them to the last object along the vertical path. Then calls
	 * {@link #setVerticalWeights(List)} to set the final weights
	 * used to govern each element's size from this point on.
	 * 
	 * @param hp		The {@link VerticalPath} to have each element
	 * 					"visit" during the second dispatch.
	 */
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void visit(VerticalPath vp) {
		double leftOverHeight = surface.getArea().getHeight();
        List<?> l = vp.getElements();
        int len = l.size();
        Element<T> e = null;
        for(int i = 0;i < len;i++) {
        	e = (Element<T>)l.get(i);
            e.acceptVertical(this);
            e.set(surface, ChangeType.RESIZE_RELOCATE);
            
            leftOverHeight -= e.r.height;
        }
		//Add back in any fractions left over due to 
        //non-integer resolution - can't set the weights
        //until after this adjustment for perfect layout.
        if(surface.getUseIntegerPrecision()) {
        	e.r.height += Math.round(leftOverHeight);	
        	
        }else{
        	e.r.height += leftOverHeight;
        }
		e.set(surface, ChangeType.RESIZE_RELOCATE);
	}
	
	/** unimplemented */
	@Override public void visitHorizontal(Node<T> n) {}
	/** unimplemented */
	@Override public void visitVertical(Node<T> n) {}
}
