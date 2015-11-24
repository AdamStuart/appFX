package container.mosaic;


/**
 * Implemented by those classes which accept a {@link PathVisitor}.
 * @author David Ray
 *
 * @param <T>	the type specified by {@link MosaicEngineImpl}'s 
 * 				generics resolution.
 */
interface PathVisitable<T> {
    public void accept(PathVisitor<T> lv);
}
