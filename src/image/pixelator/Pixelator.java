/*
 * This code is completely free of any restrictions on usage.
 *
 * Feel free to study it, modify it, redistribute it and even claim it as your own if you like!
 *
 * Courtesy of Bembrick Software Labs in the interest of promoting JavaFX.
 */
package image.pixelator;

import java.util.LinkedList;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * <code>Pixelator</code> provides a method to pixelate an image with a specified block size.
 *
 * @author Originally by Gerrit Grunwald (@hansolo_) and adapted by Felix Bembrick (@Lucan1d)
 * @version 1.0 September 2013
 */
public class Pixelator {

	/**
	 * Pixelates the given source image and stores the result in the given writable image.
	 */
	public static void pixelate(final WritableImage wi, final Image source, final int blockSize) {
		final int size = blockSize < 1 ? 1 : blockSize;
		final PixelReader pixelReader = source.getPixelReader();
		final int width = (int) source.getWidth();
		final int height = (int) source.getHeight();

		final List<Color> colors = new LinkedList<>();

		final Color[][] blockColors = new Color[width / size + 1][height / size + 1];

		for (int y = 0; y < height; y += size) {
			for (int x = 0; x < width; x += size) {
				final Color col = pixelReader.getColor(x, y);
				int newRed = 0;
				int newGreen = 0;
				int newBlue = 0;
				colors.clear();

				for (int blockY = y; blockY < y + size; ++blockY) {
					for (int blockX = x; blockX < x + size; ++blockX) {
						if (blockX < 0 || blockX >= width) {
							colors.add(col);
							continue;
						}
						if (blockY < 0 || blockY >= height) {
							colors.add(col);
							continue;
						}
						colors.add(pixelReader.getColor(blockX, blockY));
					}
				}

				for (final Color color : colors) {
					newRed += (int) (color.getRed() * 255) & 0xFF;
					newGreen += (int) (color.getGreen() * 255) & 0xFF;
					newBlue += (int) (color.getBlue() * 255) & 0xFF;
				}

				final int noOfColors = colors.size();
				newRed /= noOfColors;
				newGreen /= noOfColors;
				newBlue /= noOfColors;

				blockColors[x / size][y / size] = Color.rgb(newRed, newGreen, newBlue);
			}
		}

		// Get the writable image's pixel writer and set the colour of every pixel according to
		// the array of colours we have built.
		final PixelWriter pixelWriter = wi.getPixelWriter();

		for (int y = 0; y < height; y += size) {
			for (int x = 0; x < width; x += size) {
				for (int blockY = y; blockY < y + size && blockY < height; ++blockY) {
					for (int blockX = x; blockX < x + size && blockX < width; ++blockX) {
						pixelWriter.setColor(blockX, blockY, blockColors[x / size][y / size]);
					}
				}
			}
		}
	}
}
