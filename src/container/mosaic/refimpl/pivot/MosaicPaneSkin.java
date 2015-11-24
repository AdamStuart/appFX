package container.mosaic.refimpl.pivot;

import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Implementation of Pivot "skin" which houses the calls to
 * the underlying layout engine.
 * 
 * @author David Ray
 */
public class MosaicPaneSkin extends ContainerSkin {
	/**
	 * Called by the pivot framework to create a new MosaicPaneSkin
	 */
	public MosaicPaneSkin() {
		
	}
	
	/**
	 * Overridden to receive and relay calls to the underlying layout engine.
	 */
	@Override
	public void layout() {
		
	}
	
	@Override
	public boolean isFocusable() {
		return true;
	}
	
	/**
	 * Called by the underlying framework to add the skin
	 * to the component.
	 */
	@Override
	public void install(Component component) {
		super.install(component);
	}
	
}
