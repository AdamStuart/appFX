package threeD;

import java.util.List;

import diagrams.plate.Well;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class PlateXForm extends Xform
{
	public PlateXForm(List<DoubleProperty> vals)
	{
		model = vals;
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(Color.ORANGE);
		redMaterial.setSpecularColor(Color.RED);

		final PhongMaterial whiteMaterial = new PhongMaterial();
		whiteMaterial.setDiffuseColor(Color.WHITE);
		whiteMaterial.setSpecularColor(Color.LIGHTBLUE);

		final PhongMaterial greyMaterial = new PhongMaterial();
		greyMaterial.setDiffuseColor(Color.DARKGREY);
		greyMaterial.setSpecularColor(Color.GREY);

		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(Color.DARKGREEN);
		greenMaterial.setSpecularColor(Color.GREEN);

		Xform plate = new Xform();

		final int k = 60;
		final int HEIGHT = 8;
		final int WIDTH = 12;
		final Box plateBox = new Box((WIDTH + 1) * k, (HEIGHT + 1) * k, k / 6);
		plateBox.setMaterial(redMaterial);
		plate.getChildren().add(plateBox);
		int halfK = k / 2;
		int halfWidth = 6 * k;
		int halfHeight = 4 * k;
		for (int i = 0; i < WIDTH; i++)
		{
			Xform[] wellColumn = new Xform[HEIGHT];
			for (int j = 0; j < HEIGHT; j++)
			{
				DoubleProperty heightProperty = wellHeightProperty(j, i);
				Cylinder c = new Cylinder(k / 3, 4);
				c.heightProperty().bind(heightProperty);
				c.setMaterial(whiteMaterial);
				if (2 * i == 3 * j)
					c.setMaterial(greyMaterial); // dummy coloring

				c.setRotationAxis(new Point3D(1, 0, 0));
				c.setRotate(90.);
				c.translateZProperty().bind(heightProperty.divide(2).add(k / 8)); 

				Text wellDescription = new Text();
				wellDescription.setText(Well.wellNotation96(i, j));
				wellDescription.setRotationAxis(new Point3D(0, 1, 0));
				wellDescription.setFont(Font.font(null, FontWeight.BOLD, 14));
				wellDescription.setTranslateX(0 - (k / 4 + (i == 8 ? -4 : 0)));    // a hack to make up for I being narrower than the other letters

				wellDescription.setTranslateZ(0);
				wellDescription.setRotate(180.);
				wellDescription.translateZProperty().bind(heightProperty.add(10));
				wellDescription.setFill(Color.BLACK);
				wellDescription.setTextOrigin(VPos.CENTER);

				wellColumn[j] = new Xform();
				wellColumn[j].getChildren().add(c);
				Group g = new Group();
				g.getChildren().add(c);
				g.getChildren().add(wellDescription);
				g.setTranslateX(0 - (k * i - halfWidth + halfK));
				g.setTranslateY(k * j - halfHeight + halfK);
				plate.getChildren().add(g);
			}
		}
		getChildren().add(plate);
	}
//-----------------------------------------------------------------------------------
	private List<DoubleProperty> model;

	private DoubleProperty wellHeightProperty(int i, int j)	{		return wellHeightProperty(i * 12 + j);	} // 96
	private DoubleProperty wellHeightProperty(int i)		{		return model.get(i);	} 
}
