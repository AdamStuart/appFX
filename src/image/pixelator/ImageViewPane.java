package image.pixelator;

/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 */
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

/**
 *
 * @author akouznet
 */
public class ImageViewPane extends Region {

	public ImageViewPane() {	this(new ImageView());	}

	public ImageViewPane(final ImageView imageView) 
	{
		imageViewProperty.addListener(new ChangeListener<ImageView>() 
		{
			@Override	public void changed(final ObservableValue<? extends ImageView> arg0, final ImageView oldIV,
			 final ImageView newIV) {
				if (oldIV != null) {					getChildren().remove(oldIV);				}
				if (newIV != null) {					getChildren().add(newIV);				}
			}
		});
		imageViewProperty.set(imageView);
	}

	private final ObjectProperty<ImageView> imageViewProperty = new SimpleObjectProperty<ImageView>();
	
	public ImageView getImageView() 						{		return imageViewProperty.get();	}
	public void setImageView(final ImageView imageView) 	{		imageViewProperty.set(imageView);	}
	public ObjectProperty<ImageView> imageViewProperty() 	{		return imageViewProperty;	}
	//--------------------------------------------------------

	@Override	protected void layoutChildren() {
		final ImageView imageView = imageViewProperty.get();
		if (imageView != null) {
			imageView.setFitWidth(getWidth());
			imageView.setFitHeight(getHeight());
			layoutInArea(imageView, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
		}
		super.layoutChildren();
	}
}
