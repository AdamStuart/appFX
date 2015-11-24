package container.mosaic;

import container.mosaic.PathIterator.HorizontalPath;
import container.mosaic.PathIterator.VerticalPath;

/**
 * Implemented by those classes interested in iterating over
 * {@link PathIterator.Path} objects to accomplish some work
 * over a given path.
 * 
 * @param <T>	the type specified by {@link MosaicEngineImpl}'s 
 * 				generics resolution.
 */
@SuppressWarnings("rawtypes")
interface PathVisitor <T> {
	public void visit(HorizontalPath hp);
    public void visit(VerticalPath vp);
}
