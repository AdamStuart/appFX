package diagrams.draw;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class Shape2 extends StackPane {
	SVGPath path = null;
	
	public Shape2()
	{
		StringBuilder heart = new StringBuilder();
//		heart.append("<svg version=\"1.1\" id=\"Layer_1\" ");
//		heart.append("xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" ");
//		heart.append("x=\"0px\" y=\"0px\" width=\"512px\" height=\"512px\" ");
//		heart.append("viewBox=\"0 0 512 512\" style=\"enable-background:new 0 0 512 512;\" ");
//		heart.append("xml:space=\"preserve\">");
//		heart.append("<path id=\"template\" d=\"");
		heart.append("M340.8,98.4c50.7,0,91.9,41.3,91.9,92.3c0,26.2-10.9,49.8-28.3,66.6L256,407.1L105,254.6c-15.8-16.6-25.6-39.1-25.6-63.9 ");
		heart.append("c0-51,41.1-92.3,91.9-92.3c38.2,0,70.9,23.4,84.8,56.8C269.8,121.9,302.6,98.4,340.8,98.4 M340.8,83C307,83,276,98.8,256,124.8 ");
		heart.append("c-20-26-51-41.8-84.8-41.8C112.1,83,64,131.3,64,190.7c0,27.9,10.6,54.4,29.9,74.6L245.1,418l10.9,11l10.9-11l148.3-149.8 ");
		heart.append("c21-20.3,32.8-47.9,32.8-77.5C448,131.3,399.9,83,340.8,83L340.8,83z");
//		heart.append("\"/></svg>");
		path = new SVGPath();
		path.setContent(heart.toString());
		Text text = new Text("Heart");
		getChildren().addAll(path, text);

		
	}
}
