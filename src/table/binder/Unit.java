package table.binder;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public enum Unit 
{ 
	 PX("pixels", 100), 
	 PT("points", 72),
	 CM("centimeters", 2.54),
	 MM("millimeters", 25.4),
	 IN("inches", 1),
	 M("meters", 0.0254);
	 
	 private String name;
//	 private double perInch;
	 private double perMeter;
	 
	 Unit(String a, double perIn)
	 {
		 name = a;
//		 perInch = perIn;
		 perMeter = perIn / 0.0254;
	 }
	 
	 public static ObservableList<String> valuesSquared()
	 {
		 ObservableList<String> vals = FXCollections.observableArrayList();
		 for (Unit un : Unit.values())
			 vals.add("" + un.toString() + "^2");
		 return vals;
	 }
	 public String asString() { return toString().toLowerCase();	}
	 static Unit fromString(String s)
	 {
		 for (Unit u: Unit.values())
			 if (s.equals(u.getName()) || s.equals(u.asString()))
				 return u;
		 return IN;
	 }
	 public String getName() { return name;	}
//	 public double getPerInch() { return perInch;	}
	 public double getPerMeter() { return perMeter;	}

	public static ObservableList<Unit> getNames() {
		ObservableList<Unit> units = FXCollections.observableArrayList();
		for (Unit u: Unit.values())
			units.add(u);
		return units;
	}
};