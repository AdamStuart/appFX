package table.binder.tablecellHelpers;

import java.text.NumberFormat;

import javafx.util.converter.DoubleStringConverter;
import util.StringUtil;

// ------------------------------------------------------------------------------

public class NumberColConverter extends DoubleStringConverter
{
	public NumberColConverter()
	{
		super();
	}
	private final NumberFormat nf = NumberFormat.getNumberInstance();

	{
		nf.setMaximumFractionDigits(3);
//		nf.setMinimumFractionDigits(3);
	}

	@Override public String toString(final Double value)
	{
		if (value instanceof Number)
			return nf.format(value);
		return "NaN";
	}

	@Override public Double fromString(final String s)
	{
		// Don't need this, unless table is editable, see
		// DoubleStringConverter if needed
		return StringUtil.toDouble(s);
	}
}
