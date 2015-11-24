package container.mosaic;

public interface EngineBuilder<T> {
	/**
	 * Returns an implementation of {@link MosaicEngine}
	 * 
	 * @return	an implementation of {@link MosaicEngine}
	 */
	public MosaicEngine<T> build();
}
