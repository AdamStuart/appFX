package container.mosaic;

import java.util.List;

import container.mosaic.PathIterator.HorizontalPath;
import container.mosaic.PathIterator.VerticalPath;


/**
 * Computes and sets the horizontal and vertical weights on each {@link Node}.
 * 
 * @author David Ray
 *
 * @param <T>
 */
class WeightAdjustmentVisitor<T> implements PathVisitor<T> {
	private SurfacePriviledged<T> surface;
	
	
	WeightAdjustmentVisitor(SurfacePriviledged<T> surface) {
		this.surface = surface;
	}

	public void setSurface(SurfacePriviledged<T> s) {
		this.surface = s;
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void visit(HorizontalPath hp) {
		double netWidth = surface.getArea().width;// - (hp.getDividerCount() * surface.getDividerSize());
		List<?> l = hp.getElements();
        int len = l.size();
        Element e = null;
        for(int i = 0;i < len;i++) {
        	e = (Element<T>)l.get(i);
        	if(e.type == ElementType.DIVIDER) continue;
        	e.horizontalWeight = e.r.width / netWidth;
        }
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void visit(VerticalPath vp) {
		double netHeight = surface.getArea().height;// - (vp.getDividerCount() * surface.getDividerSize());
		List<?> l = vp.getElements();
        int len = l.size();
        Element<T> e = null;
        for(int i = 0;i < len;i++) {
        	e = (Element<T>)l.get(i);
        	if(e.type == ElementType.DIVIDER) continue;
        	e.verticalWeight = e.r.height / netHeight;
        }
	}
}
