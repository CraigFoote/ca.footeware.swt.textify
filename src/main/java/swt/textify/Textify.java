/**
 *
 */
package swt.textify;

import java.io.File;
import java.io.FileWriter;
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import swt.textify.dialogs.AboutDialog;
import swt.textify.dialogs.PreferencesDialog;
import swt.textify.exceptions.FontException;
import swt.textify.preferences.FontUtils;
import swt.textify.preferences.PreferenceProvider;

/**
 * A minimal text editor.
 */
public class Textify {

	private static final String SAVE_PROMPT = "The text has been modified. Would you like to save it?";
	private Shell shell;
	private Text text;
	private Image newImage;
	private Image openImage;
	private Image saveImage;
	private Image saveAsImage;
	private Image hamburgerImage;
	private File currentFile;
	private boolean textChanged = false;
	private PreferenceProvider prefs;
	private Composite statusbar;
	private Label filenameLabel;
	private Label numCharsLabel;

	/**
	 * Constructor.
	 */
	public Textify(String[] args) {
		final Display display = new Display();
		shell = new Shell(display);
		shell.setText("textify");

		// preferences
		prefs = new PreferenceProvider();

		setShellSize();

		shell.addDisposeListener(e -> {
			// save shell size to prefs
			final Point size = shell.getSize();
			prefs.setProperty("shell.width", String.valueOf(size.x));
			prefs.setProperty("shell.height", String.valueOf(size.y));
			prefs.save();

			// prompt to save modifications
			if (textChanged) {
				final MessageBox box = new MessageBox(shell, SWT.NO | SWT.YES | SWT.ICON_QUESTION);
				box.setText("Save");
				box.setMessage(SAVE_PROMPT);
				if (box.open() == SWT.YES) {
					save();
				}
			}

			// dispose of images
			dispose();
		});

		GridLayout gridLayout = new GridLayout(2, false);
		shell.setLayout(gridLayout);

		createLeftToolbar();
		createRightToolbar();
		createScrollingText();
		createStatusbar();

		handleCliArgs(args);

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
	 * Check if file can be opened.
	 *
	 * @param file {@link File}
	 * @throws IOException              if mimetype cannot be determined
	 * @throws IllegalArgumentException if file cannot be opened as text
	 */
	private void checkFile(File file) throws IOException, IllegalArgumentException {
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
		if (!charset.equals(StandardCharsets.UTF_8)) {
			throw new IllegalArgumentException("File is not UTF-8: " + charset);
		}
	}

	/**
	 * Clears text widget and sets appropriate labels.
	 */
	private void clear() {
		text.setText("");
		currentFile = null;
		textChanged = false;
		filenameLabel.setText("");
		numCharsLabel.setText("0 chars");
		shell.setText("textify");
	}

	/**
	 * Create a toolbar with buttons aligned left.
	 */
	private void createLeftToolbar() {
		// left toolbar
		final ToolBar toolBarLeft = new ToolBar(shell, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		toolBarLeft.setLayoutData(gridData);
		Rectangle clientArea = shell.getClientArea();
		toolBarLeft.setLocation(clientArea.x, clientArea.y);

		// New
		final ToolItem newItem = new ToolItem(toolBarLeft, SWT.NONE);
		InputStream in = Textify.class.getResourceAsStream("/images/new.png");
		newImage = new Image(shell.getDisplay(), in);
		newItem.setImage(newImage);
		newItem.setToolTipText("Start a new document");
		newItem.addListener(SWT.Selection, event -> newFile());

		// Open
		final ToolItem openItem = new ToolItem(toolBarLeft, SWT.NONE);
		in = Textify.class.getResourceAsStream("/images/open.png");
		openImage = new Image(shell.getDisplay(), in);
		openItem.setImage(openImage);
		openItem.setToolTipText("Open an existing document");
		openItem.addListener(SWT.Selection, event -> openFile());

		// Save
		final ToolItem saveItem = new ToolItem(toolBarLeft, SWT.NONE);
		in = Textify.class.getResourceAsStream("/images/save.png");
		saveImage = new Image(shell.getDisplay(), in);
		saveItem.setToolTipText("Save current document to file");
		saveItem.setImage(saveImage);
		saveItem.addListener(SWT.Selection, event -> save());

		// Save As
		final ToolItem saveAsItem = new ToolItem(toolBarLeft, SWT.NONE);
		in = Textify.class.getResourceAsStream("/images/save-as.png");
		saveAsImage = new Image(shell.getDisplay(), in);
		saveAsItem.setImage(saveAsImage);
		saveAsItem.setToolTipText("Save current document as new file");
		saveAsItem.addListener(SWT.Selection, event -> saveAs());
	}

	/**
	 * Create a toolbar with buttons aligned right.
	 */
	private void createRightToolbar() {
		// right toolbar
		final ToolBar toolBarRight = new ToolBar(shell, SWT.NONE);
		GridData gridData = new GridData(GridData.END, GridData.FILL, true, false);
		toolBarRight.setLayoutData(gridData);
		Rectangle clientArea = shell.getClientArea();
		toolBarRight.setLocation(clientArea.x, clientArea.y);

		// menu for hamburger button
		final Menu menu = new Menu(shell, SWT.POP_UP);

		// Preferences
		MenuItem prefsItem = new MenuItem(menu, SWT.PUSH);
		prefsItem.setText("Preferences");
		prefsItem.addListener(SWT.Selection, event -> {
			new PreferencesDialog(shell, prefs).open();
			try {
				final Font font = getFont(prefs, shell.getDisplay());
				text.setFont(font);
				font.dispose();
			} catch (FontException e1) {
				showError(e1.getMessage());
			}
		});

		// About menu
		MenuItem aboutItem = new MenuItem(menu, SWT.PUSH);
		aboutItem.setText("About");
		aboutItem.addListener(SWT.Selection, event -> new AboutDialog(shell).open());

		// hamburger
		final ToolItem item = new ToolItem(toolBarRight, SWT.NONE);
		InputStream in = Textify.class.getResourceAsStream("/images/hamburger.png");
		hamburgerImage = new Image(shell.getDisplay(), in);
		item.setImage(hamburgerImage);
		item.addListener(SWT.Selection, event -> {
			Rectangle rect = item.getBounds();
			Point pt = new Point(rect.x, rect.y + rect.height);
			pt = toolBarRight.toDisplay(pt);
			menu.setLocation(pt.x, pt.y);
			menu.setVisible(true);
		});
		toolBarRight.pack();
	}

	/**
	 * Create a text widget inside a scroller.
	 */
	private void createScrollingText() {
		// scroller
		ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
		scrolledComposite.setLayoutData(gridData);

		// text
		text = new Text(scrolledComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		text.setLayoutData(gridData);
		text.addModifyListener(e -> {
			textChanged = true;
			shell.setText("* " + shell.getText().replace("* ", ""));
			numCharsLabel.setText(text.getCharCount() + " chars");
			statusbar.layout(true);
		});
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if ((e.stateMask & SWT.CTRL) != 0) {
					if (e.keyCode == 115) { // ctrl+s
						save();
					} else if (e.keyCode == 119) { // ctrl+w
						shell.close();
					}
				}
				super.keyReleased(e);
			}
		});
		try {
			final Font font = getFont(prefs, shell.getDisplay());
			text.setFont(font);
			font.dispose();
		} catch (FontException e1) {
			showError(e1.getMessage());
		}

		// finish scrollbar init
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
	}

	/**
	 * Create statusbar with labels.
	 */
	private void createStatusbar() {
		// statusbar
		statusbar = new Composite(shell, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		statusbar.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout(2, false);
		statusbar.setLayout(gridLayout);

		// filename label
		filenameLabel = new Label(statusbar, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		filenameLabel.setLayoutData(gridData);

		// # chars
		numCharsLabel = new Label(statusbar, SWT.NONE);
		numCharsLabel.setText("0 chars");
		gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		numCharsLabel.setLayoutData(gridData);
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

	/**
	 * Get the font from preferences. The caller is responsible for disposing of it.
	 *
	 * @param prefs   {@link PreferenceProvider}
	 * @param display {@link Display}
	 * @return {@link Font}
	 * @throws FontException if an error occurs getting FontData from prefs property
	 */
	private Font getFont(PreferenceProvider prefs, Display display) throws FontException {
		String fontProperty = prefs.getProperty("font", null);
		FontData fontData;
		if (fontProperty == null) {
			fontData = new FontData("sans", 14, SWT.NORMAL);
		} else {
			fontData = FontUtils.getFontData(fontProperty);
		}
		return new Font(display, fontData);
	}

	private void handleCliArgs(String[] args) {
		// handle cli arg
		if (args.length > 1) {
			String message = "Expected at most one argument, a file, but received: " + args.length;
			System.err.println(message);
			showError(message);
		}
		if (args.length == 1) {
			// file received
			System.out.println("File received: " + args[0]);
			File file = new File(args[0]);
			if (!file.exists()) {
				try {
					System.out.println("File does not exist - creating it...");
					boolean created = file.createNewFile();
					if (!created) {
						final String message = "Unknown error creating file.";
						System.err.println(message);
						showError(message);
					} else {
						System.out.println("File created - loading...");
						loadFile(file);
					}
				} catch (IOException e1) {
					final String message = "Error loading file:\n" + e1.getMessage();
					System.err.println(message);
					showError(message);
				}
			} else {
				loadFile(file);
			}
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
	 * Load the contents of the provided file into the text widget.
	 *
	 * @param file {@link File}
	 */
	private void loadFile(File file) {
		try {
			checkFile(file);
			// load contents of file
			List<String> allLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
			StringBuilder builder = new StringBuilder();
			for (String line : allLines) {
				builder.append(line);
				builder.append("\n");
			}
			// set in text widget
			text.setText(builder.toString());
			currentFile = file;
			textChanged = false;
			filenameLabel.setText(file.getAbsolutePath());
			shell.setText(file.getName());
		} catch (IOException | IllegalArgumentException e) {
			showError(e.getMessage());
		}
	}

	/**
	 * Respond to the user pressing the New button.
	 */
	private void newFile() {
		if (textChanged) {
			final MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
			box.setText("Save");
			box.setMessage(SAVE_PROMPT);
			int result = box.open();
			if (result == SWT.NO || (result == SWT.YES && save())) {
				clear();
			}
		} else {
			clear();
		}
	}

	/**
	 * Open a file.
	 */
	protected void openFile() {
		if (textChanged) {
			// prompt user to save
			final MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
			box.setText("Save");
			box.setMessage(SAVE_PROMPT);
			int result = box.open();
			if (result == SWT.CANCEL) {
				return;
			} else if (result == SWT.YES) {
				save();
			}
		}
		// proceed with opening a file
		final FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		final String path = dialog.open();
		if (path != null && !path.isEmpty()) {
			File file = new File(path);
			loadFile(file);
		}
	}

	/**
	 * Save text changes to file.
	 *
	 * @return boolean true if text was saved to file
	 */
	private boolean save() {
		// save text to file
		File file = null;
		// pessimistic view on writing
		boolean write = false;
		if (currentFile != null) {
			file = currentFile;
			write = true;
			// good to go
		} else {
			// prompt for filename and location
			final FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
			final String chosenPath = fileDialog.open();
			// filePath will be null if a file was not chosen or name entered
			if (chosenPath != null) {
				file = new File(chosenPath);
				if (file.exists()) {
					// prompt for overwrite
					final MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
					box.setText("Overwrite");
					box.setMessage("File exists. Would you like to overwrite it?");
					int result = box.open();
					if (result == SWT.CANCEL) {
						return false;
					} else if (result == SWT.YES) {
						write = true;
					}
				}
			}
		}
		// do we have everything we need to write file?
		if (write) {
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(text.getText());
				currentFile = file;
				textChanged = false;
				filenameLabel.setText(file.getAbsolutePath());
				shell.setText(file.getName());
				return true;
			} catch (IOException e) {
				showError(e.getMessage());
			}
		}
		return false;
	}

	/**
	 * Prompt for filename and location and save text to file.
	 */
	private void saveAs() {
		// save text to file
		File file = null;
		// pessimistic view on writing
		boolean write = false;
		// prompt for filename and location
		final FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
		final String chosenPath = fileDialog.open();
		// filePath will be null if a file was not chosen or name entered
		if (chosenPath != null) {
			file = new File(chosenPath);
			if (file.exists()) {
				// prompt for overwrite
				final MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
				box.setText("Overwrite");
				box.setMessage("File exists. Would you like to overwrite it?");
				int result = box.open();
				if (result == SWT.CANCEL) {
					return;
				} else if (result == SWT.YES) {
					write = true;
				}
			} else {
				write = true;
			}
		}
		// do we have everything we need to write file?
		if (write) {
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(text.getText());
				currentFile = file;
				textChanged = false;
				filenameLabel.setText(file.getAbsolutePath());
				shell.setText(file.getName());
			} catch (IOException e) {
				showError(e.getMessage());
			}
		}
	}

	/**
	 * Set size of shell from prefs.
	 */
	private void setShellSize() {
		final int savedWidth = Integer.parseInt(prefs.getProperty("shell.width", String.valueOf(800)));
		final int savedHeight = Integer.parseInt(prefs.getProperty("shell.height", String.valueOf(600)));
		shell.setSize(savedWidth, savedHeight);
	}

	/**
	 * Display an error message to the user.
	 *
	 * @param string {@link String}
	 */
	private void showError(String string) {
		MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
		messageBox.setText("Error");
		messageBox.setMessage(string);
		messageBox.open();
	}
}
