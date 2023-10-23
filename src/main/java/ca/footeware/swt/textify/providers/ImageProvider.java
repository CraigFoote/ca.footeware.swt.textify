/**
 *
 */
package ca.footeware.swt.textify.providers;

import java.io.File;
import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ca.footeware.swt.textify.Textify;

/**
 * Provides...wait for it...images!
 */
public class ImageProvider {

	private static final String IMAGE_PATH = File.separator + "images" + File.separator;
	private Image keyboardImage;
	private Image menuImage;
	private Image newImage;
	private Image openImage;
	private Image programmerImage;
	private Image saveAsImage;
	private Image saveImage;
	private Image searchImage;

	/**
	 * Constructor.
	 *
	 * @param shell {@link Shell}
	 */
	public ImageProvider(Shell shell) {
		shell.addDisposeListener(e -> dispose());
	}

	/**
	 * Disposes the images and removes preferences listener.
	 */
	public void dispose() {
		if (newImage != null && !newImage.isDisposed()) {
			newImage.dispose();
		}
		if (openImage != null && !openImage.isDisposed()) {
			openImage.dispose();
		}
		if (saveImage != null && !saveImage.isDisposed()) {
			saveImage.dispose();
		}
		if (saveAsImage != null && !saveAsImage.isDisposed()) {
			saveAsImage.dispose();
		}
		if (menuImage != null && !menuImage.isDisposed()) {
			menuImage.dispose();
		}
		if (searchImage != null && !searchImage.isDisposed()) {
			searchImage.dispose();
		}
		if (programmerImage != null && !programmerImage.isDisposed()) {
			programmerImage.dispose();
		}
		if (keyboardImage != null && !keyboardImage.isDisposed()) {
			keyboardImage.dispose();
		}
	}

	/**
	 * @return the keyboardImage
	 */
	public Image getKeyboardImage() {
		if (keyboardImage == null) {
			InputStream in = Textify.class.getResourceAsStream(IMAGE_PATH + "keyboard.png");
			keyboardImage = new Image(Display.getDefault(), in);
		}
		return keyboardImage;
	}

	/**
	 * @return the menuImage
	 */
	public Image getMenuImage() {
		if (menuImage == null) {
			InputStream in = Textify.class.getResourceAsStream(IMAGE_PATH + "menu.png");
			menuImage = new Image(Display.getDefault(), in);
		}
		return menuImage;
	}

	/**
	 * @return the newImage
	 */
	public Image getNewImage() {
		if (newImage == null) {
			InputStream in = Textify.class.getResourceAsStream(IMAGE_PATH + "new.png");
			newImage = new Image(Display.getDefault(), in);
		}
		return newImage;
	}

	/**
	 * @return the openImage
	 */
	public Image getOpenImage() {
		if (openImage == null) {
			InputStream in = Textify.class.getResourceAsStream(IMAGE_PATH + "open.png");
			openImage = new Image(Display.getDefault(), in);
		}
		return openImage;
	}

	/**
	 * @return the programmerImage
	 */
	public Image getProgrammerImage() {
		if (programmerImage == null) {
			InputStream in = Textify.class.getResourceAsStream(IMAGE_PATH + "programmer.jpg");
			programmerImage = new Image(Display.getDefault(), in);
		}
		return programmerImage;
	}

	/**
	 * @return the saveAsImage
	 */
	public Image getSaveAsImage() {
		if (saveAsImage == null) {
			InputStream in = Textify.class.getResourceAsStream(IMAGE_PATH + "save-as.png");
			saveAsImage = new Image(Display.getDefault(), in);
		}
		return saveAsImage;
	}

	/**
	 * @return the saveImage
	 */
	public Image getSaveImage() {
		if (saveImage == null) {
			InputStream in = Textify.class.getResourceAsStream(IMAGE_PATH + "save.png");
			saveImage = new Image(Display.getDefault(), in);
		}
		return saveImage;
	}

	/**
	 * @return the searchImage
	 */
	public Image getSearchImage() {
		if (searchImage == null) {
			InputStream in = Textify.class.getResourceAsStream(IMAGE_PATH + "search.png");
			searchImage = new Image(Display.getDefault(), in);
		}
		return searchImage;
	}
}
