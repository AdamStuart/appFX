package table.codeOrganizer;

import java.util.StringTokenizer;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import util.TreeUtil;

public class TreeTableModel
{

	static public TreeItem<String> getVehicleDataRoot()
	{

		TreeItem<String> root = new TreeItem<String>("Vehicles");
		root.setExpanded(true);
		TreeItem<String> cars = new TreeItem<String>("Cars");
		TreeItem<String> planes = new TreeItem<String>("Planes");
		TreeItem<String> boats = new TreeItem<String>("Boats");
		boats.setExpanded(true);

		TreeItem<String> ford = new TreeItem<String>("Ford");
		TreeItem<String> chrysler = new TreeItem<String>("Chrysler");
		TreeItem<String> volvo = new TreeItem<String>("Volvo");
		TreeItem<String> mazda = new TreeItem<String>("Mazda");
		TreeItem<String> z = new TreeItem<String>("Z");

		TreeItem<String> dinghy = new TreeItem<String>("Dinghy");
		TreeItem<String> raft = new TreeItem<String>("Raft");
		TreeItem<String> marlin = new TreeItem<String>("Marlin");
		TreeItem<String> catamaran = new TreeItem<String>("Catamaran");
		TreeItem<String> laser = new TreeItem<String>("Laser");

		TreeItem<String> cessna = new TreeItem<String>("Cessna");
		TreeItem<String> piper = new TreeItem<String>("Piper");
		TreeItem<String> gulf = new TreeItem<String>("Gulfstream");
		TreeItem<String> boeing = new TreeItem<String>("Boeing");
		TreeItem<String> lockheed = new TreeItem<String>("Lockheed");

		cars.getChildren().addAll(ford, chrysler, volvo, mazda, z);
		planes.getChildren().addAll(cessna, piper, gulf, boeing, lockheed);
		boats.getChildren().addAll(dinghy, raft, marlin, catamaran, laser);
		root.getChildren().addAll(cars, planes, boats);
		return root;

	}
	static public TreeItem<String> getCellPopulationTree()
	{

		TreeItem<String> raw = makeTree("Events");
		TreeItem<String> live = makeTree("Live");
		makeTrees(raw, "Debris", "Doublets", "Dead Cells");

		TreeItem<String> lymph = makeTree("Lymphocytes");
		TreeItem<String> t = makeTree("T Cells");
		TreeItem<String> b = makeTree("B Cells");
		live.getChildren().add(lymph);
		makeTrees(live, "Monocytes", "Granulocytes", "Neutrophils", "Eosinophils", "Basophils");
		makeTrees(lymph, "NK Cells", t, b);
		makeTrees(t, "T Cell Receptors", "CD4", "CD8");
		makeTrees(b, "Plasma B Cells", "Memory B Cells", "B-1", "B-reg");
		return raw;

	}
	private static TreeItem<String> makeTree(String s)
	{
		return TreeUtil.makeTree(s);
	}
	private static void makeTrees(TreeItem<String> root, Object ... obj)
	{
		for (Object o : obj)
		{
			TreeItem<String> t = null;
			if (o instanceof TreeItem<?>)
				t = (TreeItem<String>) o;
			else if (o instanceof String)
				t = makeTree((String) o);
			if (t != null)
				root.getChildren().add(t);
		}
	}


	//--------------------------------------------------------------------------------
	static String expr = "(	Root   (	Application (  Dashboard    )  )	  )";
	
// NOTE:  spaces must surround ( and )	
	static String expr2 = "(	Root   (	Application (  Dashboard    )  " +
	"Model ( Factories Converters ObjectLists DOM Database Documents ) "+
	"Views ( Buttons Enums Metaviews Tables Charts ( Pie Scatter Histograms Interactive ) Dialogs ) "+
	"Reports ( Lists Tables Trees PivotTables Printing PowerPrint SVG PDF HTML Layouts XML/JSON ) "+
	"Library ( String File Streams Color CSS RichText DragNDrop ) "+ " ) )";

	//--------------------------------------------------------------------------------


static TreeItem<String> readTree(String s)
	{
		StringTokenizer tknize = new StringTokenizer(s);

		int indent  = 0;
		TreeItem<String> root = new TreeItem<String>("Root");
		String token;
		TreeItem<String> lastChild = null;
		TreeItem<String> parent  = root;
		System.out.println(indent);
		
		token = tknize.nextToken();		assert(token.equals("("));		indent++;
		System.out.println(indent);
		token = tknize.nextToken();		assert(token.equals("Root"));
		while (tknize.hasMoreTokens())
		{
			token = tknize.nextToken();
			if (token.equals("("))
			{
				indent++;
				if (lastChild != null) 
					parent = lastChild;
				System.out.println(indent);
			}
			else if (token.equals(")"))
			{
				indent--;
				if (parent != null)
					parent = parent.getParent();
				System.out.println(indent);
			}
			else
			{
				lastChild = new TreeItem<String>(token);
				parent.getChildren().add(lastChild);
				System.out.println(indent + " " + lastChild.getValue() );

			}
		}
		assert(indent == 0);
		
		return root;
	}
	
static public void dumpTree(TreeItem<String> t, int indent, StringBuffer buff)
{
	String pad = "          ".substring(0,indent);
//	String pad = "   ";
	buff.append(pad + t.getValue());
	ObservableList<TreeItem<String> >children = t.getChildren();
	buff.append(pad + "(\n" );
	if (children.size() > 0)
		for (TreeItem<String> child : children)
			dumpTree(child, indent + 1, buff);
	buff.append(pad + ")\n" );
}

static public void xmlTree(TreeItem<String> t, int indent, StringBuffer buff)
{
	String pad = "                ".substring(0,2 * indent);
	ObservableList<TreeItem<String> >children = t.getChildren();
	if (children.size() == 0)
		buff.append(pad + "<Element name=\"" + t.getValue() + "\" />" );
	else
	{
		buff.append("\n" + pad + "<Element name=\"" + t.getValue() + "\">" );
		for (TreeItem<String> child : children)
			xmlTree(child, indent + 1, buff);
		buff.append(pad + " </Element>\n" );
	}
}

	
	
	
	public static TreeItem<String> getCodeModuleApplication()
	{
		
		TreeItem<String> test = readTree(expr2);

		StringBuffer buff = new StringBuffer("(\n");
		dumpTree(test, 0, buff);
		buff.append(")\n");
		System.out.println(buff.toString());
		
		buff = new StringBuffer("<?xml> .... <Element> \n");
		xmlTree(test, 0, buff);
		buff.append(" </Element> \n");
		System.out.println(buff.toString());

		TreeItem<String> apps = new TreeItem<String>("Application");
		TreeItem<String> factories = new TreeItem<String>("Factories");
		TreeItem<String> converts = new TreeItem<String>("Converters");
		TreeItem<String> objs = new TreeItem<String>("ObjectLists");
		TreeItem<String> dom = new TreeItem<String>("DOM");
		TreeItem<String> db = new TreeItem<String>("Database");
		TreeItem<String> docs = new TreeItem<String>("Documents");
		TreeItem<String> text = new TreeItem<String>("RichText");
		TreeItem<String> print = new TreeItem<String>("Printing");
		TreeItem<String> dnd = new TreeItem<String>("DragNDrop");
		apps.getChildren().addAll(factories, converts, objs, dom, db, docs, text, print, dnd );

		return apps;
	}


	public static TreeItem<String> getCodeModuleView()
	{
		TreeItem<String> views = new TreeItem<String>("Views");
		TreeItem<String> buttons = new TreeItem<String>("Buttons");
		TreeItem<String> enums = new TreeItem<String>("Enums");
		TreeItem<String> meta = new TreeItem<String>("Metaviews");
		TreeItem<String> tables = new TreeItem<String>("Tables");
		TreeItem<String> charts = new TreeItem<String>("Charts");
		TreeItem<String> plates = new TreeItem<String>("Plates");
		TreeItem<String> dlogs = new TreeItem<String>("Dialogs");

		TreeItem<String> pie = new TreeItem<String>("Pie");
		TreeItem<String> scatter = new TreeItem<String>("Scatter");
		TreeItem<String> histo = new TreeItem<String>("Histograms");
		charts.getChildren().addAll(pie, scatter, histo);
		views.getChildren().addAll(buttons, enums, meta, tables, charts, plates, dlogs);
		return views;
	}

	public static TreeItem<String> getCodeModuleLibrary()
	{
		TreeItem<String> lib = new TreeItem<String>("Library");
		TreeItem<String> str = new TreeItem<String>("String");
		TreeItem<String> fil = new TreeItem<String>("File");
		TreeItem<String> strm = new TreeItem<String>("Stream");
		TreeItem<String> color = new TreeItem<String>("Color");
		TreeItem<String> css = new TreeItem<String>("CSS");
		lib.getChildren().addAll(str, fil, strm, color, css);
		return lib;

	}

	
	public static TreeItem<String> getCodeModuleReport()
	{
		TreeItem<String> root = new TreeItem<String>("Reports");
		TreeItem<String> lists = new TreeItem<String>("Lists");
		TreeItem<String> tabls = new TreeItem<String>("Tables");
		TreeItem<String> trees = new TreeItem<String>("Trees");
		TreeItem<String> pivot = new TreeItem<String>("Pivot Tables");
		root.getChildren().addAll(lists, tabls, pivot, trees);
		return root;
		
	}
	
	public static TreeItem<String> getCodeModuleRoot()
	{
		TreeItem<String> all = new TreeItem<String>("All");
		all.getChildren().addAll(getCodeModuleApplication(), getCodeModuleView(), getCodeModuleLibrary(), getCodeModuleReport());
		return all;
	}

	

	
}
