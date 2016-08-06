package table.networkTable;

import java.util.ArrayList;
import java.util.HashMap;

import chart.usMap.ColorUtil;
import javafx.scene.paint.Color;
import model.Range;

public class SquareMap extends HashMap<String, HashMap<String, Double>> {

	ArrayList<String> names;
	Range range = new Range();

	Range getValueRange() {

		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (String name : names) {
			HashMap<String, Double> row = get(name);
			for (String str : names) {
				double d = row.get(str);
				if (d < min)
					min = d;
				if (d > max)
					max = d;
			}
		}
		return new Range(min, max);
	}

	double[][] buildArray() {
		int nRows = entrySet().size();
		double[][] matrix = new double[nRows][nRows];
		for (int i = 0; i < nRows; i++) {
			String name = names.get(i);
			HashMap<String, Double> row = get(name);
			for (int j = 0; j < nRows; j++)
				matrix[i][j] = row.get(names.get(j));
		}
		return matrix;
	}

	static boolean GRAYSCALE = true;

	Color valToColor(double v) {
		double x = (v = range.min()) / (range.width());
		return (GRAYSCALE) ? ColorUtil.gray(x) : ColorUtil.blueYellow(x);
	}
}
