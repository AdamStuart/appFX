package container.mosaic;

/**
 * Implemented by Visitors wishing to modify {@link Nodes} within a 
 * given {@link PathIterator.Path}.
 *
 * @param <T>	the type specified by {@link MosaicEngineImpl}'s 
 * 				generics resolution.
 */
interface ElementVisitor <T> {
    public void visitHorizontal(Node<T> n);
    public void visitHorizontal(Divider<T> d);
    public void visitVertical(Node<T> n);
    public void visitVertical(Divider<T> d);
}
