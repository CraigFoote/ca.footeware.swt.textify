/**
 * 
 */
package swt.textify;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A minimal text editor.
 */
public class Textify {

	private Text text;
	private Image newImage;
	private Image openImage;
	private Image saveImage;
	private Image saveAsImage;
	private Image hamburgerImage;

	/**
	 * Constructor.
	 */
	public Textify() {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("textify");
		shell.setSize(800, 600);
		shell.addDisposeListener(e -> dispose());
		GridLayout gridLayout = new GridLayout(2, false);
		shell.setLayout(gridLayout);

		// left toolbar
		final ToolBar toolBarLeft = new ToolBar(shell, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		toolBarLeft.setLayoutData(gridData);
		Rectangle clientArea = shell.getClientArea();
		toolBarLeft.setLocation(clientArea.x, clientArea.y);

		// New
		final ToolItem newItem = new ToolItem(toolBarLeft, SWT.NONE);
		gridData = new GridData(GridData.END, GridData.FILL, true, false);
		InputStream in = Textify.class.getResourceAsStream("/new.png");
		newImage = new Image(display, in);
		newItem.setImage(newImage);
		newItem.setToolTipText("Start a new document");
		newItem.addListener(SWT.Selection, event -> {
			// TODO check for open file or modified text
			text.setText("");
		});

		// Open
		final ToolItem openItem = new ToolItem(toolBarLeft, SWT.NONE);
		gridData = new GridData(GridData.END, GridData.FILL, true, false);
		in = Textify.class.getResourceAsStream("/open.png");
		openImage = new Image(display, in);
		openItem.setImage(openImage);
		openItem.setToolTipText("Open an existing document");
		openItem.addListener(SWT.Selection, event -> openFile(shell));

		// Save
		final ToolItem saveItem = new ToolItem(toolBarLeft, SWT.NONE);
		gridData = new GridData(GridData.END, GridData.FILL, true, false);
		in = Textify.class.getResourceAsStream("/save.png");
		saveImage = new Image(display, in);
		saveItem.setToolTipText("Save current document to file");
		saveItem.setImage(saveImage);
		saveItem.addListener(SWT.Selection, event -> {
			System.out.println("Save");
		});

		// Save As
		final ToolItem saveAsItem = new ToolItem(toolBarLeft, SWT.NONE);
		gridData = new GridData(GridData.END, GridData.FILL, true, false);
		in = Textify.class.getResourceAsStream("/save-as.png");
		saveAsImage = new Image(display, in);
		saveAsItem.setImage(saveAsImage);
		saveItem.setToolTipText("Save current document as new file");
		saveAsItem.addListener(SWT.Selection, event -> {
			System.out.println("Save As");
		});

		// right toolbar
		final ToolBar toolBarRight = new ToolBar(shell, SWT.NONE);
		gridData = new GridData(GridData.END, GridData.FILL, true, false);
		toolBarRight.setLayoutData(gridData);
		clientArea = shell.getClientArea();
		toolBarRight.setLocation(clientArea.x, clientArea.y);

		// menu for hamburger button
		final Menu menu = new Menu(shell, SWT.POP_UP);
		MenuItem aboutItem = new MenuItem(menu, SWT.PUSH);
		aboutItem.setText("About");
		aboutItem.addListener(SWT.Selection, event -> {
			new AboutDialog(shell).open();
		});

		// hamburger
		final ToolItem item = new ToolItem(toolBarRight, SWT.NONE);
		in = Textify.class.getResourceAsStream("/hamburger.png");
		hamburgerImage = new Image(display, in);
		item.setImage(hamburgerImage);
		item.addListener(SWT.Selection, event -> {
			Rectangle rect = item.getBounds();
			Point pt = new Point(rect.x, rect.y + rect.height);
			pt = toolBarRight.toDisplay(pt);
			menu.setLocation(pt.x, pt.y);
			menu.setVisible(true);
		});
		toolBarRight.pack();

		// scroller
		ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
		scrolledComposite.setLayoutData(gridData);

		// text
		text = new Text(scrolledComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		text.setLayoutData(gridData);

		scrolledComposite.setContent(text);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = scrolledComposite.getClientArea();
				scrolledComposite.setMinSize(text.computeSize(r.width, SWT.DEFAULT));
			}
		});

		text.setFocus();

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		// disposes all associated windows and their components
		display.dispose();
	}

	/**
	 * Open a file.
	 * 
	 * @param shell {@link Shell}
	 */
	protected void openFile(Shell shell) {
		// TODO check for open file or modified text
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		String filePath = dialog.open();
		if (filePath != null && !filePath.isEmpty()) {
			File file = new File(filePath);
			try {
				checkFile(file);
				// load contents of file
				List<String> allLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
				StringBuilder builder = new StringBuilder();
				for (String line : allLines) {
					builder.append(line);
				}
				// set in text widget
				text.setText(builder.toString());
			} catch (IOException | IllegalArgumentException e) {
				MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
				messageBox.setText("Error");
				messageBox.setMessage(e.getMessage());
				messageBox.open();
			}
		}
	}

	/**
	 * Check if file can be opened.
	 * 
	 * @param file {@link File}
	 * @throws IOException if file cannot be opened
	 */
	private void checkFile(File file) throws IOException {
		// check file exists
		if (!file.exists()) {
			throw new IllegalArgumentException("File does not exist:\n" + file.toPath());
		}
		// check mimetype
		String mimeType = Files.probeContentType(file.toPath());
		if (!isText(mimeType)) {
			throw new IllegalArgumentException("File is not text: " + mimeType);
		}
		// check charset
		Charset charset = Charset.defaultCharset();
		if (!charset.equals(Charset.forName("UTF-8"))) {
			throw new IllegalArgumentException("File is not UTF-8: " + charset);
		}
	}

	/**
	 * Determines if provided mimeType is text-based.
	 *
	 * @param mimeType {@link String}
	 * @return boolean true if mimeType indicates text-based
	 */
	private boolean isText(String mimeType) {
		return mimeType == null || mimeType.startsWith("text") || mimeType.contains("xml") || mimeType.contains("json")
				|| mimeType.equals("audio/mpegurl") || mimeType.contains("x-sh");
	}

	/**
	 * Explicitly dispose of created images.
	 */
	protected void dispose() {
		if (newImage != null) {
			newImage.dispose();
			newImage = null;
		}
		if (openImage != null) {
			openImage.dispose();
			openImage = null;
		}
		if (saveImage != null) {
			saveImage.dispose();
			saveImage = null;
		}
		if (saveAsImage != null) {
			saveAsImage.dispose();
			saveAsImage = null;
		}
		if (hamburgerImage != null) {
			hamburgerImage.dispose();
			hamburgerImage = null;
		}
	}
}
