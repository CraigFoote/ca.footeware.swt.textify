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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import swt.textify.dialogs.AboutDialog;
import swt.textify.exceptions.FontException;
import swt.textify.preferences.FontPreferencePage;
import swt.textify.preferences.FontUtils;
import swt.textify.preferences.WrapPreferencePage;

/**
 *
 */
public class Textify extends ApplicationWindow {

	private static final String APP_NAME = "textify";
	private static final String IMAGE_PATH = File.separator + "images" + File.separator;
	private static final String SAVE_PROMPT = "The text has been modified. Would you like to save it?";
	private static final Logger LOGGER = LogManager.getLogger(Textify.class);
	private Image newImage;
	private Image openImage;
	private Image saveImage;
	private Image saveAsImage;
	private Image menuImage;
	private Composite statusbar;
	private Label filenameLabel;
	private Label numCharsLabel;
	private ITextViewer viewer;
	private boolean textChanged = false;
	private File currentFile;
	private PreferenceStore preferenceStore;
	private PreferenceManager preferenceManager;
	private Font font;
	private String[] args;
	private Clipboard clipboard;

	/**
	 * @constructor
	 * @param args {@link String}[]
	 */
	public Textify(String[] args) {
		super(null);
		this.args = args;
		setBlockOnOpen(true);
		open();
		dispose();
		Display.getCurrent().dispose();
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
		viewer.getDocument().set("");
		currentFile = null;
		textChanged = false;
		filenameLabel.setText("");
		numCharsLabel.setText("0 chars");
		getShell().setText(APP_NAME);
	}

	/**
	 * Configure the provided composite.
	 *
	 * @param parent {@link Composite}
	 */
	private void configureParent(Composite parent) {
		Control[] children = parent.getChildren();
		if (children.length > 0 && children[0] != null && !children[0].isDisposed()) {
			parent.getChildren()[0].dispose(); // why is there a label here?
		}
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(parent);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(parent);
	}

	/**
	 * Set up preferences for eventual use. Set text font and wrap from preferences.
	 */
	private void configurePreferences() {
		preferenceManager = new PreferenceManager();

		PreferenceNode fontNode = new PreferenceNode("Font", "Font", null, FontPreferencePage.class.getName());
		preferenceManager.addToRoot(fontNode);

		PreferenceNode wrapNode = new PreferenceNode("Wrap", "Wrap", null, WrapPreferencePage.class.getName());
		preferenceManager.addToRoot(wrapNode);

		// Set the preference store
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(System.getProperty("user.home"));
		stringBuilder.append(File.separator);
		stringBuilder.append(".local");
		stringBuilder.append(File.separator);
		stringBuilder.append("share");
		stringBuilder.append(File.separator);
		stringBuilder.append(APP_NAME);
		stringBuilder.append(File.separator);
		stringBuilder.append("textify.properties");
		String storePath = stringBuilder.toString();
		preferenceStore = new PreferenceStore(storePath);

		// defaults
		preferenceStore.setDefault("Font", Display.getDefault().getSystemFont().getFontData()[0].toString());
		preferenceStore.setDefault("Wrap", true);

		// load prefs from file
		try {
			preferenceStore.load();
			initWidgets();
		} catch (IOException e) {
			LOGGER.log(Level.INFO, e);
		}
	}

	/**
	 * Initialize appropriate widgets to their value in preferences.
	 */
	private void initWidgets() {
		// set text wrap
		final boolean wrapProperty = preferenceStore.getBoolean("Wrap");
		viewer.getTextWidget().setWordWrap(wrapProperty);

		// set text font
		final String fontProperty = preferenceStore.getString("Font");
		if (fontProperty != null && !fontProperty.isEmpty()) {
			try {
				FontData fontData = FontUtils.getFontData(fontProperty);
				if (font != null) {
					font.dispose();
				}
				font = new Font(getShell().getDisplay(), fontData);
				viewer.getTextWidget().setFont(font);
			} catch (FontException e) {
				showError("An error occurred getting font from preferences.", e);
			}
		}
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(APP_NAME);
	}

	/**
	 * Create a button.
	 *
	 * @param parent  {@link Composite}
	 * @param text    {@link String}
	 * @param image   {@link Image}
	 * @param toolTip {@link String}
	 * @return {@link Button}
	 */
	private Button createButton(Composite parent, String text, Image image, String toolTip) {
		final Button button = new Button(parent, SWT.PUSH | SWT.FLAT);
		button.setText(text);
		button.setImage(image);
		button.setToolTipText(toolTip);
		GridDataFactory.swtDefaults().applyTo(button);
		return button;
	}

	@Override
	protected Control createContents(Composite parent) {
		configureParent(parent);
		createLeftToolbar();
		createRightToolbar();
		createTextViewer();
		createStatusbar();
		configurePreferences();
		handleCliArgs();
		createContextMenu();
		createDisposeListener();
		return parent;
	}

	/**
	 * Handle window closing when text has been modified.
	 */
	private void createDisposeListener() {
		getShell().addDisposeListener(e -> {
			if (textChanged) {
				final MessageBox box = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
				box.setText("Save");
				box.setMessage("Text has been modified. Would you like to save it before closing?");
				if (box.open() == SWT.NO) {
					getShell().close();
				} else {
					if (save()) {
						getShell().close();
					}
				}
			}
		});
	}

	/**
	 * Creates the context menu
	 */
	protected void createContextMenu() {
		clipboard = new Clipboard(getShell().getDisplay());

		MenuManager contextMenu = new MenuManager("#ViewerMenu");
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(this::fillContextMenu);
		Menu menu = contextMenu.createContextMenu(viewer.getTextWidget());
		viewer.getTextWidget().setMenu(menu);
	}

	/**
	 * Create the left-most toolbar with its buttons.
	 */
	private void createLeftToolbar() {
		final Composite leftToolBar = new Composite(getShell(), SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).grab(false, false).applyTo(leftToolBar);
		GridLayoutFactory.swtDefaults().numColumns(4).equalWidth(true).applyTo(leftToolBar);

		newImage = new Image(getShell().getDisplay(), Textify.class.getResourceAsStream(IMAGE_PATH + "new.png"));
		Button button = createButton(leftToolBar, "New", newImage, "Start a new document");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).hint(100, 50).applyTo(button);
		button.addListener(SWT.Selection, event -> newFile());

		openImage = new Image(getShell().getDisplay(), Textify.class.getResourceAsStream(IMAGE_PATH + "open.png"));
		button = createButton(leftToolBar, "Open", openImage, "Open an existing document");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(button);
		button.addListener(SWT.Selection, event -> openFile());

		saveImage = new Image(getShell().getDisplay(), Textify.class.getResourceAsStream(IMAGE_PATH + "save.png"));
		button = createButton(leftToolBar, "Save", saveImage, "Save current document to file");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(button);
		button.addListener(SWT.Selection, event -> save());

		saveAsImage = new Image(getShell().getDisplay(), Textify.class.getResourceAsStream(IMAGE_PATH + "save-as.png"));
		button = createButton(leftToolBar, "Save As", saveAsImage, "Save current document as a new file");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(button);
		button.addListener(SWT.Selection, event -> saveAs());
	}

	/**
	 * Create the right-most toolbar with its buttons.
	 */
	private void createRightToolbar() {
		// right toolbar
		final Composite rightToolBar = new Composite(getShell(), SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.FILL).grab(false, false).applyTo(rightToolBar);
		GridLayoutFactory.swtDefaults().applyTo(rightToolBar);

		// menu for hamburger button
		final Menu menu = new Menu(getShell(), SWT.POP_UP);

		// Preferences
		MenuItem preferencesItem = new MenuItem(menu, SWT.PUSH);
		preferencesItem.setText("Preferences");
		preferencesItem.addListener(SWT.Selection, event -> {
			PreferenceDialog preferenceDialog = new PreferenceDialog(getShell(), preferenceManager);
			preferenceDialog.setMinimumPageSize(SWT.DEFAULT, 50);
			preferenceDialog.setPreferenceStore(preferenceStore);
			preferenceDialog.open();
			try {
				preferenceStore.save();
			} catch (IOException e) {
				showError("An error occurred saving preferences.", e);
			}
			try {
				viewer.getTextWidget().setWordWrap(preferenceStore.getBoolean("Wrap"));
				viewer.getTextWidget().setFont(
						new Font(getShell().getDisplay(), FontUtils.getFontData(preferenceStore.getString("Font"))));
			} catch (FontException e) {
				showError("An error occurred setting the viewer font.", e);
			}
		});

		// About menu
		MenuItem aboutItem = new MenuItem(menu, SWT.PUSH);
		aboutItem.setText("About");
		aboutItem.addListener(SWT.Selection, event -> new AboutDialog(getShell()).open());

		// hamburger
		final Button menuButton = new Button(rightToolBar, SWT.NONE);
		InputStream in = Textify.class.getResourceAsStream(IMAGE_PATH + "menu.png");
		menuImage = new Image(getShell().getDisplay(), in);
		menuButton.setImage(menuImage);
		GridDataFactory.swtDefaults().hint(50, 50).align(SWT.FILL, SWT.FILL).applyTo(menuButton);
		menuButton.addListener(SWT.Selection, event -> {
			Rectangle rect = menuButton.getBounds();
			Point pt = new Point(rect.x, rect.y + rect.height);
			pt = rightToolBar.toDisplay(pt);
			menu.setLocation(pt.x, pt.y);
			menu.setVisible(true);
		});
		rightToolBar.pack();
	}

	/**
	 * Create the statusbar with its two labels.
	 */
	private void createStatusbar() {
		// statusbar
		statusbar = new Composite(getShell(), SWT.NONE);
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
	 * Create the {@link TextViewer}.
	 */
	private void createTextViewer() {
		viewer = new TextViewer(getShell(), SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).span(2, 1).hint(800, 600)
				.applyTo(viewer.getTextWidget());
		viewer.setDocument(new Document());
		viewer.addTextListener(event -> {
			textChanged = true;
			getShell().setText("* " + getShell().getText().replace("* ", ""));
			numCharsLabel.setText(viewer.getDocument().get().length() + " chars");
			statusbar.layout(true);
		});
		viewer.getTextWidget().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if ((e.stateMask & SWT.CTRL) != 0) {
					if (e.keyCode == 115) { // ctrl+s
						save();
					} else if (e.keyCode == 119) { // ctrl+w
						getShell().close();
					}
				}
				super.keyReleased(e);
			}
		});
		viewer.getTextWidget().setFocus();
	}

	/**
	 * Disposes the images
	 */
	private void dispose() {
		if (newImage != null)
			newImage.dispose();
		if (openImage != null)
			openImage.dispose();
		if (saveImage != null)
			saveImage.dispose();
		if (saveAsImage != null)
			saveAsImage.dispose();
		if (menuImage != null)
			menuImage.dispose();
		if (font != null)
			font.dispose();
	}

	/**
	 * Fill dynamic context menu
	 *
	 * @param contextMenu
	 */
	protected void fillContextMenu(IMenuManager contextMenu) {
		// Cut
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(getClass(), "/images/cut.png");
		contextMenu.add(new Action("Cut", descriptor) {
			@Override
			public void run() {
				final String selectionText = viewer.getTextWidget().getSelectionText();
				if (!selectionText.isEmpty()) {
					final TextTransfer textTransfer = TextTransfer.getInstance();
					final Transfer[] types = new Transfer[] { textTransfer };
					clipboard.setContents(new Object[] { selectionText }, types);

					Point selectedRange = viewer.getSelectedRange();
					try {
						viewer.getDocument().replace(selectedRange.x, selectedRange.y, "");
					} catch (BadLocationException e) {
						showError("An error occurred cutting text.", e);
					}
				}
			}
		});
		// Copy
		descriptor = ImageDescriptor.createFromFile(getClass(), "/images/copy.png");
		contextMenu.add(new Action("Copy", descriptor) {
			@Override
			public void run() {
				final String selectionText = viewer.getTextWidget().getSelectionText();
				if (!selectionText.isEmpty()) {
					final TextTransfer textTransfer = TextTransfer.getInstance();
					final Transfer[] types = new Transfer[] { textTransfer };
					clipboard.setContents(new Object[] { selectionText }, types);
				}
			}
		});
		// Paste
		descriptor = ImageDescriptor.createFromFile(getClass(), "/images/paste.png");
		contextMenu.add(new Action("Paste", descriptor) {
			@Override
			public void run() {
				final TextTransfer textTransfer = TextTransfer.getInstance();
				final String data = (String) clipboard.getContents(textTransfer);
				if (data != null) {
					viewer.getTextWidget().insert(data);
				}
			}
		});
	}

	/**
	 * Handle command line arguments.
	 */
	private void handleCliArgs() {
		// handle cli arg
		if (args.length > 1) {
			showError("Expected at most one argument, a file, but received: " + args.length, null);
		}
		if (args.length == 1) {
			// file received
			String message = "File received: " + args[0];
			LOGGER.log(Level.INFO, message);
			final File file = new File(args[0]);
			if (!file.exists()) {
				try {
					LOGGER.log(Level.INFO, "File does not exist - creating it.");
					final boolean created = file.createNewFile();
					if (!created) {
						showError("Unknown error creating file.", null);
					} else {
						LOGGER.log(Level.INFO, "File created - loading...");
						loadFile(file);
					}
				} catch (IOException e1) {
					showError("An error occurred loading the file", e1);
				}
			} else {
				LOGGER.log(Level.DEBUG, "Loading file.");
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
			final List<String> allLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
			final StringBuilder builder = new StringBuilder();
			for (String line : allLines) {
				builder.append(line);
				builder.append("\n");
			}
			// set in text widget
			viewer.getDocument().set(builder.toString());
			currentFile = file;
			textChanged = false;
			filenameLabel.setText(file.getAbsolutePath());
			getShell().setText(file.getName());
		} catch (IOException | IllegalArgumentException e) {
			showError("An error occurred loading the file.", e);
		}
	}

	/**
	 * Respond to the user pressing the New button.
	 */
	private void newFile() {
		if (textChanged) {
			final MessageBox box = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
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
	private void openFile() {
		if (textChanged) {
			// prompt user to save
			final MessageBox box = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
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
		final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		final String path = dialog.open();
		if (path != null && !path.isEmpty()) {
			File file = new File(path);
			loadFile(file);
		}
	}

	/**
	 * Prompt user to overwrite file
	 *
	 * @return int one of SWT.YES, SWT.NO or SWT.CANCEL
	 */
	private int promptForOverwrite() {
		final MessageBox box = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
		box.setText("Overwrite");
		box.setMessage("File exists. Would you like to overwrite it?");
		return box.open();
	}

	/**
	 * Save text changes to file.
	 *
	 * @return boolean true if text was saved to file
	 */
	protected boolean save() {
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
			final FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
			final String chosenPath = fileDialog.open();
			// filePath will be null if a file was not chosen or name entered
			if (chosenPath != null) {
				file = new File(chosenPath);
				if (file.exists()) {
					// prompt for overwrite
					final int overwrite = promptForOverwrite();
					if (overwrite == SWT.CANCEL) {
						return false;
					} else if (overwrite == SWT.YES) {
						write = true;
					}
				} else {
					write = true;
				}
			}
		}
		// do we have everything we need to write file?
		if (write) {
			return write(file);
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
		final FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
		final String chosenPath = fileDialog.open();
		// filePath will be null if a file was not chosen or name entered
		if (chosenPath != null) {
			file = new File(chosenPath);
			if (file.exists()) {
				// prompt for overwrite
				final int overwrite = promptForOverwrite();
				if (overwrite == SWT.CANCEL) {
					return;
				} else if (overwrite == SWT.YES) {
					write = true;
				}
			} else {
				write = true;
			}
		}
		// do we have everything we need to write file?
		if (write) {
			write(file);
		}
	}

	/**
	 * Display an error message to the user.
	 *
	 * @param string {@link String}
	 */
	private void showError(String string, Exception e) {
		LOGGER.log(Level.ERROR, string, e);
		final MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR);
		messageBox.setText("Error");
		messageBox.setMessage(string);
		messageBox.open();
	}

	/**
	 * Write file to disk and update UI.
	 *
	 * @param file {@link File}
	 * @return boolean true if file was written
	 */
	private boolean write(File file) {
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(viewer.getDocument().get());
			currentFile = file;
			textChanged = false;
			filenameLabel.setText(file.getAbsolutePath());
			getShell().setText(file.getName());
			return true;
		} catch (IOException e) {
			showError("An error occurred writing the file out to disk.", e);
		}
		return false;
	}
}
