package container.mosaic;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/**
 * A builder capable of properly creating a {@link Surface}. This builder
 * uses "fluid" semantics so that configuration calls may be chained.
 * 
 * @author David Ray
 *
 * @param <T> The object type being added to the {@link MosaicEngine}/{@link Surface} to be layed out, which known type
 * 			will be handed back within the {@link SurfaceListener#changed(Object, java.awt.geom.Rectangle2D, java.awt.geom.Rectangle2D)}
 * 			method. The most efficient use is to declare the type which will be mutated by a change in location or size.
 */
public interface SurfaceBuilder<T> {
	/**
	 * Distance from starting from any corner extending out from that corner and representing a circle within which
	 * a corner click (and drag) is recognized.
	 * 	
	 * @param radius		the radius from a corner, describing the click recognition distance.
	 * @return				this builder.
	 */
	public SurfaceBuilder<T> cornerClickRadius(double radius);
	/**
	 * Sets the size of all dividers in their static (unchanging) dimension.
	 * @param size	the size of all dividers.
	 * @return		this builder.
	 */
	public SurfaceBuilder<T> dividerSize(double size);
	/**
	 * Sets the distance from a snap point, within which a snap to a valid location
	 * described by another divider or screen edge, will occur. If the distance is 0,
	 * it essentially turns off snapping. This distance may not be negative.
	 * 
	 * @param distance		the distance from another divider within which a snap will occur		
	 * @return				this builder.
	 */
	public SurfaceBuilder<T> snapDistance(double distance);
	/**
	 * Sets a flag indicating that sizes and locations should be within integer precision
	 * and not to double precision for compatibility with frameworks using integer precision.
	 * 
	 * @param b		true if using integer precision, false if not. (Default).
	 * @return		this builder.
	 */
	public SurfaceBuilder<T> useIntegerPrecision(boolean b);
	/**
	 * Sets a flag indicating that locations should use the specified 
	 * @param b
	 * @return	this builder.
	 */
	public SurfaceBuilder<T> useSurfaceOffset(boolean b);
	/**
	 * Sets the offset used during calculations and returned by the {@link SurfaceListener#changed(Object, Rectangle2D, Rectangle2D)}
	 * method's returned {@link Rectangle2D}'s. There may be times when the user has a need to specify an
	 * offset to the x and/or y location for special layout reasons and this method allows that capability.
	 * If a offset is set and {@link MosaicEngine#usesSurfaceOffset()} returns true, all internal calculations will
	 * be offset by the amount specified in either the x or y direction. 
	 * 
	 * WARNING: Not yet fully implemented in version 0.0.1-SNAPSHOT
	 * @param offset		the amount by which to offset the x and/or y location of the surface
	 * @return				true if so, false if not.
	 */
	public SurfaceBuilder<T> surfaceOffset(Point2D.Double offset);
	
	/**
	 * Returns the properly built {@link Surface} ready for use once objects have been
	 * added, {@link SurfaceListener}(s) have been added and {@link MosaicEngine#addSurface(Surface)} 
	 * has been called. 
	 * <p>
	 * This builder </i><em>requires</em></i> that {@link #setCornerClickRadius(double)}, {@link
	 *  #setDividerSize(double)}, {@link #setSnapDistance(double)} all be called. {@link #useIntegerPrecision(boolean)}
	 *  and {@link #useSurfaceOffset(boolean)} are <em>optional</em>.
	 *  <p>
	 * 
	 * @return		a propertly setup {@link Surface}.
	 * @throws IllegalStateException if any of the builder options have not been set other 
	 * than {@link #setSurfaceOffset(double)} which is only valid if {@link #useSurfaceOffset(boolean)}
	 * has been called previously, and {@link #useIntegerPrecision(boolean)} which <em>defaults to false</em>.
	 */
	public Surface<T> build();
}