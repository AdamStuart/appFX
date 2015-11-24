package container.mosaic;

import java.awt.geom.Point2D;


/**
 * 
 * @author David Ray
 *
 * @param <T>
 */
public class MosaicSurfaceBuilder<T> implements SurfaceBuilder<T> {
	private static final double UNSET = Double.MIN_VALUE;
	private double dividerSize = UNSET;
	private double cornerClickRadius = UNSET;
	private double snapDistance = UNSET;
	
	private Boolean useSurfaceOffset;
	private Boolean useIntegerPrecision;
	private Point2D.Double surfaceOffset;
	
	private SurfaceImpl<T> surface;
	
	/**
	 * Constructs a new {@link MosaicSurfaceBuilder}
	 */
	public MosaicSurfaceBuilder() {
		
	}
	
	/**
	 * Returns a new instance of a {@link Surface} implementation.
	 * 
	 * @return 	a new {@link Surface}
	 */
	public Surface<T> getSurface() {
		return surface;
	}
	
	/**
	 * Sets the radius which forms a circle around a given corner,
	 * within which a corner drag gesture will be recognized.
	 * 
	 * @param radius	the distance from the corner.
	 */
	@Override
	public SurfaceBuilder<T> cornerClickRadius(double radius) {
		cornerClickRadius = radius;
		return this;
	}

	/**
	 *	Sets the divider size along its static dimension.
	 *	@return this builder 
	 */
	@Override
	public SurfaceBuilder<T> dividerSize(double size) {
		dividerSize = size;
		return this;
	}
	
	/**
	 * Sets the snap distance or distance away from a relevant divider or 
	 * screen edge, before a snap to a position will occurr. Defaults to 
	 * 15, a good setting but some will prefer stronger.
	 * 
	 * @return this builder.
	 */
	@Override
	public SurfaceBuilder<T> snapDistance(double distance) {
		snapDistance = distance;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SurfaceBuilder<T> useIntegerPrecision(boolean b) {
		useIntegerPrecision = b;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SurfaceBuilder<T> useSurfaceOffset(boolean b) {
		useSurfaceOffset = b;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SurfaceBuilder<T> surfaceOffset(Point2D.Double offset) {
		surfaceOffset = offset;
		return this;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Surface<T> build() {
		surface = new SurfaceImpl<T>();
		
		if(cornerClickRadius == UNSET) {
			throw new IllegalStateException("Corner click radius unset. (reasonable value is 3)");
		}else{
			surface.setCornerClickRadius(cornerClickRadius);
		}
		if(dividerSize == UNSET) {
			throw new IllegalStateException("Divider size unset. (reasonable value is 10)");
		}else{
			surface.setDividerSize(dividerSize);
		}
		
		if(snapDistance != UNSET) {
			surface.setSnapDistance(snapDistance);
		}
		
		if(useIntegerPrecision != null) {
			surface.setUseIntegerPrecision(useIntegerPrecision.booleanValue());
		}
		if(useSurfaceOffset != null && useSurfaceOffset && surfaceOffset != null) {
			surface.setUseSurfaceOffset(useSurfaceOffset.booleanValue());
			surface.setSurfaceOffset(surfaceOffset);
		}else if(useSurfaceOffset != null && useSurfaceOffset && surfaceOffset == null) {
			throw new IllegalStateException("There must be a surface offset to use if useSurfaceOffset is set to \"true\"");
		}else if(surfaceOffset != null && useSurfaceOffset != null && useSurfaceOffset) {
			System.err.println("Warning surfaceOffset was set but the flag useSurfaceOffset was never set to true so it will remain unused");
		}
		
		return surface;
	}

	class NodeShell {
		
		public NodeShell connectTo(String dividerNum) {
			return this;
		}
	}
	
	class DividerShell {
		
	}
	
	public NodeShell newNode(String id) {
		return null;
	}
}
