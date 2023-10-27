/**
 *
 */
package ca.footeware.swt.textify.providers;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Provides...wait for it...images!
 */
public class ImageProvider {

	private static final String IMAGE_PATH = "/images/";
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
			InputStream in = getClass().getResourceAsStream(IMAGE_PATH + "keyboard.png");
			if (in != null) {
				try {
					keyboardImage = new Image(Display.getDefault(), in);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return keyboardImage;
	}

	/**
	 * @return the menuImage
	 */
	public Image getMenuImage() {
		if (menuImage == null) {
			InputStream in = getClass().getResourceAsStream(IMAGE_PATH + "menu.png");
			if (in != null) {
				try {
					menuImage = new Image(Display.getDefault(), in);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return menuImage;
	}

	/**
	 * @return the newImage
	 */
	public Image getNewImage() {
		if (newImage == null) {
			InputStream in = getClass().getResourceAsStream(IMAGE_PATH + "new.png");
			if (in != null) {
				try {
					newImage = new Image(Display.getDefault(), in);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return newImage;
	}

	/**
	 * @return the openImage
	 */
	public Image getOpenImage() {
		if (openImage == null) {
			InputStream in = getClass().getResourceAsStream(IMAGE_PATH + "open.png");
			if (in != null) {
				try {
					openImage = new Image(Display.getDefault(), in);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return openImage;
	}

	/**
	 * @return the programmerImage
	 */
	public Image getProgrammerImage() {
		if (programmerImage == null) {
			InputStream in = getClass().getResourceAsStream(IMAGE_PATH + "programmer.jpg");
			if (in != null) {
				try {
					programmerImage = new Image(Display.getDefault(), in);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return programmerImage;
	}

	/**
	 * @return the saveAsImage
	 */
	public Image getSaveAsImage() {
		if (saveAsImage == null) {
			InputStream in = getClass().getResourceAsStream(IMAGE_PATH + "save-as.png");
			if (in != null) {
				try {
					saveAsImage = new Image(Display.getDefault(), in);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return saveAsImage;
	}

	/**
	 * @return the saveImage
	 */
	public Image getSaveImage() {
		if (saveImage == null) {
			InputStream in = getClass().getResourceAsStream(IMAGE_PATH + "save.png");
			if (in != null) {
				try {
					saveImage = new Image(Display.getDefault(), in);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return saveImage;
	}

	/**
	 * @return the searchImage
	 */
	public Image getSearchImage() {
		if (searchImage == null) {
			InputStream in = getClass().getResourceAsStream(IMAGE_PATH + "search.png");
			if (in != null) {
				try {
					searchImage = new Image(Display.getDefault(), in);
				} finally {
					try {
						in.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return searchImage;
	}
}
