package container.mosaic;

import java.util.Collection;

/**
 * Interface through which users can interact with the {@link MosaicEngine}
 * 
 * @author David Ray
 *
 * @param <T>
 */
public interface MosaicEngine<T> {
	/**
	 * Sets the area/bounds being managed.
	 * @param surface	Surface representing a container of objects to layout.
	 */
	public void addSurface(Surface<T> surface);
	
	/**
	 * Removes the {@link Surface} from the {@code MosaicEngine}'s {@link Collection}
	 * of managed Surfaces.
	 * 
	 * @param surface	the {@code Surface} to remove.
	 */
	public void removeSurface(Surface<T> surface);
	
}