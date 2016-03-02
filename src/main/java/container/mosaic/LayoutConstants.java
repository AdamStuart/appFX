package container.mosaic;

/**
 * Designates official indexes and keys for internal
 * storage and serialization.
 * 
 * @author David Ray
 */
public interface LayoutConstants {
	/** Parameter indexes */
	public static final int ID = 0;
	public static final int X = 1;
	public static final int Y = 2;
	public static final int W = 3;
	public static final int H = 4;
	public static final int PRV_N = 5;
	public static final int MIN_W = 5;
	public static final int NXT_N = 6;
	public static final int MAX_W = 6;
	public static final int LED_J = 7;
	public static final int MIN_H = 7;
	public static final int TRL_J = 8;
	public static final int MAX_H = 8;
	public static final int H_WT = 9;
	public static final int V_WT = 10;
	
	public static final String KEY_CORNER_CLICK_RADIUS = "cornerClickRadius";
	public static final String KEY_SNAP_DISTANCE = "snapDistance";
	public static final String KEY_SURFACE_OFFSET = "offset";
	public static final String KEY_USE_INT_PRECISION = "useIntegerPrecision";
	public static final String KEY_LAYOUT = "layout";
	public static final String KEY_SURFACE_BOUNDS = "coords";
	public static final String KEY_IGNORE_BOUNDS = "bounds";
	public static final String KEY_DIVIDER_SIZE = "dividerSize";
	public static final String KEY_USE_SURFACE_OFFSET = "useSurfaceOffset";
	public static final String KEY_CELLS = "cells";
	public static final String KEY_DIVIDER_BOUNDS = "divBounds";
	
	public static final String CELL_PTRN = "[\\s,\\,]+";
	public static final String SUB_PTRN = "[\\:]+";
}
