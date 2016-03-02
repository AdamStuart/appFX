package container.mosaic;

import container.mosaic.PathIterator.HorizontalPath;
import container.mosaic.PathIterator.VerticalPath;


/**
 * Implementation of the PathVisitor and ElementVisitor interfaces
 * which is used once to execute a medium fine layout short of shoring up the space left
 * over from double precision error and assigning weights which must
 * be done after the complete layout.
 * 
 * WARNING: NOT THREAD-SAFE AT THIS POINT
 */
class LayoutVisitor<T> implements PathVisitor<T>, ElementVisitor<T> {
    private SurfacePriviledged<T> surface;
    private double dividerSize;
    private double netWidth;
    private double netHeight;
    private double lastX;
    double lastY;
    
    /**
     * Constructs a new InitialLayoutVisitor
     */
    LayoutVisitor(SurfacePriviledged<T> surface) {
    	this.dividerSize = surface.getDividerSize();
    	this.surface = surface;
    }
    
    public void setSurface(SurfacePriviledged<T> surface) {
    	this.dividerSize = surface.getDividerSize();
    	this.surface = surface;
    }
    
    void setDividerSize(double size) {
    	this.dividerSize = size;
    }
    
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void visit(HorizontalPath hp) {
    	double totalWidth = surface.getArea().getWidth();
        netWidth = totalWidth;
        lastX = 0;
        for(Object o : hp.getElements()) {
        	Element<T> e = (Element<T>)o;
            e.acceptHorizontal(this);
            e.set(surface, ChangeType.RESIZE_RELOCATE);
        }
    }
    
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void visit(VerticalPath vp) {
    	double totalHeight = surface.getArea().getHeight();
        netHeight = totalHeight;
        lastY = 0;
        for(Object o : vp.getElements()) {
        	Element<T> e = (Element<T>)o; 
        	e.acceptVertical(this);
        	e.set(surface, ChangeType.RESIZE_RELOCATE);
        }
    }
    
    @Override
    public void visitHorizontal(Node<T> n) {
    	boolean useIntegerPrecision = surface.getUseIntegerPrecision();
    	n.r.x = n.prevVertical == null ? (surface.getUseIntegerPrecision() ? Math.rint(lastX) : lastX) : 
    		(useIntegerPrecision ? Math.rint(n.prevVertical.r.x + dividerSize) : n.prevVertical.r.x + dividerSize);
        n.r.width = surface.getIsInit() ? (useIntegerPrecision ? Math.rint(n.percentWidth * netWidth) : n.percentWidth * netWidth) : 
        	(useIntegerPrecision ? Math.rint(n.horizontalWeight * netWidth) : n.horizontalWeight * netWidth);

        //Constrain to surface area
        n.r.x = Math.max(0, n.r.x);
        n.r.width = Math.min(surface.getArea().width, n.r.width);
        
        if(n.prevHorizontal != null) {
        	n.prevHorizontal.addPoint(n.r.x, 0);
            n.prevHorizontal.addPoint(n.r.getMaxX(), 0);
         }
        lastX = n.r.x + n.r.width;
    }

    @Override
    public void visitHorizontal(Divider<T> s) {
        s.r.x = lastX;
        s.r.width = dividerSize;
        lastX = s.r.x + s.r.width;
    }
    
    @Override
    public void visitVertical(Node<T> n) {
    	boolean useIntegerPrecision = surface.getUseIntegerPrecision();
    	n.r.y = n.prevHorizontal == null ? (useIntegerPrecision ? Math.rint(lastY) : lastY) : 
    		(useIntegerPrecision ? Math.rint(n.prevHorizontal.r.y + dividerSize) : n.prevHorizontal.r.y + dividerSize);
        n.r.height = surface.getIsInit() ? (useIntegerPrecision ? Math.rint(n.percentHeight * netHeight) :  n.percentHeight * netHeight) :
        	(useIntegerPrecision ? Math.rint(n.verticalWeight * netHeight) : n.verticalWeight * netHeight);
        
        //Constrain to surface area
        n.r.y = Math.max(0, n.r.y);
        n.r.height = Math.min(surface.getArea().height, n.r.height);
        
        if(n.prevVertical != null) {
        	n.prevVertical.addPoint(0, n.r.y);
            n.prevVertical.addPoint(0, n.r.getMaxY());
        }
        lastY = n.r.y + n.r.height;
    }
    
    @Override
    public void visitVertical(Divider<T> s) {
    	s.r.y = lastY;
        s.r.height = dividerSize;
        lastY = s.r.y + s.r.height;
    }
}
