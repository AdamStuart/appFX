package chart.treemap;

/**
 * @author Tadas Subonis <tadas.subonis@gmail.com>
 */
class TreemapDtoElement {

    private double left;
    private double top;
    private double width;
    private double height;
    private double area;
    private final String label;
    private final Item item;

    public TreemapDtoElement(Item i) {
        area = i.getAmount();
        label = i.getLabel();
        item = i;
    }

    public Item getItem() 			{        return item;    }

    public double getArea() 		{        return area;    }
    void setArea(double a	) 		{        area = a;    }

    public double getLeft() 		{        return left;    }
    public void setLeft(double l) 	{        left = l;    }

    public double getTop() 			{        return top;    }
    public void setTop(double t) 	{       	top = t;    }

    public double getWidth() 		{        return width;    }
    public void setWidth(double w) 	{        width = w;    }

    public double getHeight() 		{        return height;    }
    public void setHeight(double h) {        height = h;    }

    public String getLabel() {        return label;    }

    @Override    public String toString() {
        return "TreemapDtoElement{label='" + label + '\'' + ", area=" + area + ", top=" + top +  ", left=" + left + '}';
    }

    boolean isContainer() {        return item.isContainer();    }

    @Override   public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreemapDtoElement that = (TreemapDtoElement) o;
        if (item != null ? !item.equals(that.item) : that.item != null) return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;

        return true;
    }

    @Override   public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        return 31 * result + (item != null ? item.hashCode() : 0);
    }
}
