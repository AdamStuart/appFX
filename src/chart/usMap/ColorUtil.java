package chart.usMap;

import java.util.HashMap;

import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;


public class ColorUtil
{
	 
  /**
   * Get the color at the give {@code position} in the ladder of color stops
   */
  private static Color ladder(final double position, final Stop[] stops) {
      Stop prevStop = null;
      for (int i=0; i<stops.length; i++) {
          Stop stop = stops[i];
          if(position <= stop.getOffset()){
              if (prevStop == null) 
                  return stop.getColor();
              
              return interpolateLinear((position-prevStop.getOffset())/(stop.getOffset()-prevStop.getOffset()), prevStop.getColor(), stop.getColor());
          }
          prevStop = stop;
      }
      // position is greater than biggest stop, so will we biggest stop's color
      return prevStop.getColor();
  }
  
  /**
   * interpolate at a set {@code position} between two colors {@code color1} and {@code color2}.
   * The interpolation is done is linear RGB color space not the default sRGB color space.
   */
  private static Color interpolateLinear(double position, Color color1, Color color2) {
      Color c1Linear = convertSRGBtoLinearRGB(color1);
      Color c2Linear = convertSRGBtoLinearRGB(color2);
      return convertLinearRGBtoSRGB(Color.color(
          c1Linear.getRed()     + (c2Linear.getRed()     - c1Linear.getRed())     * position,
          c1Linear.getGreen()   + (c2Linear.getGreen()   - c1Linear.getGreen())   * position,
          c1Linear.getBlue()    + (c2Linear.getBlue()    - c1Linear.getBlue())    * position,
          c1Linear.getOpacity() + (c2Linear.getOpacity() - c1Linear.getOpacity()) * position
      ));
  }
  
  /**
   * Helper function to convert a color in sRGB space to linear RGB space.
   */
  public static Color convertSRGBtoLinearRGB(Color color) {
      double[] colors = new double[] { color.getRed(), color.getGreen(), color.getBlue() };
      for (int i=0; i<colors.length; i++) 
      {
          if (colors[i] <= 0.04045)   	colors[i] = colors[i] / 12.92;
           else               			colors[i] = Math.pow((colors[i] + 0.055) / 1.055, 2.4);
          
      }
      return Color.color(colors[0], colors[1], colors[2], color.getOpacity());
  }
  
  /**
   * Helper function to convert a color in linear RGB space to SRGB space.
   */
  public static Color convertLinearRGBtoSRGB(Color color) {
      double[] colors = new double[] { color.getRed(), color.getGreen(), color.getBlue() };
      for (int i=0; i<colors.length; i++) {
          if (colors[i] <= 0.0031308)   colors[i] = colors[i] * 12.92;
           else 			            colors[i] = (1.055 * Math.pow(colors[i], (1.0 / 2.4))) - 0.055;
          
      }
      return Color.color(colors[0], colors[1], colors[2], color.getOpacity());
  }

  
	public static HashMap<String, Color> makeColorMap(HashMap<String, Integer> statePop)
	{
		HashMap<String, Color> map = new HashMap<String, Color>();
		IntRange range = calcRange(statePop);
		int width = range.width();
		Color[] colors = getHeatMapColors();
		for (String s : statePop.keySet())
		{
			double val = statePop.get(s);
			int idx = (int) (((val - range.min) / width) * colors.length);
			if (idx < 32)
				map.put(s, colors[idx]);
		}
		return map;
	}

	static Color[] getHeatMapColors() // TODO -- integrate heatmap coloring from chart.heatmap
	{
		Color[] map = new Color[32];
		for (double i = 0; i < 32; i++)
			map[(int) i] = new Color(i / 32, i / 32, i / 32, 1);
		return map;
	}

	private static IntRange calcRange(HashMap<String, Integer> statePop)
	{
		int min = Integer.MAX_VALUE;
		int max = 0;
		for (String s : statePop.keySet())
		{
			min = Math.min(statePop.get(s), min);
			max = Math.max(statePop.get(s), max);
		}
		IntRange r = new IntRange(min, max);
		return r;

	}

 }
