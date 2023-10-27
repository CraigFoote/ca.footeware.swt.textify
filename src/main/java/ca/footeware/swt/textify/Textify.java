/**
 *
 */
package ca.footeware.swt.textify;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.layout.FillLayoutFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import ca.footeware.swt.textify.dialogs.AboutDialog;
import ca.footeware.swt.textify.dialogs.KeyBindingsDialog;
import ca.footeware.swt.textify.exceptions.FontException;
import ca.footeware.swt.textify.listeners.KeyListener;
import ca.footeware.swt.textify.listeners.PreferenceChangeListener;
import ca.footeware.swt.textify.preferences.ColorUtils;
import ca.footeware.swt.textify.preferences.FontUtils;
import ca.footeware.swt.textify.providers.ImageProvider;
import ca.footeware.swt.textify.providers.PreferenceProvider;
import ca.footeware.swt.textify.search.SearchBar;

/**
 * A minimal text editor.
 */
public class Textify extends ApplicationWindow {

	private static final Logger LOGGER = LogManager.getLogger(Textify.class);
	private static final String SAVE_PROMPT = "The text has been modified. Would you like to save it?";
	private String[] args;
	private File currentFile;
	private CursorLinePainter cursorLinePainter;
	private Color cursorLinePainterColor;
	private Font font;
	private ImageProvider imageProvider;
	private PreferenceManager preferenceManager;
	private PreferenceStore preferenceStore;
	private TextPresentation presentation;
	private IPropertyChangeListener propertyChangeListener;
	private CompositeRuler ruler;
	private SearchBar search;
	private boolean textChanged = false;
	private ISourceViewer viewer;

	/**
	 * Constructor.
	 *
	 * @param args {@link String}[]
	 */
	public Textify(String[] args) {
		super(null);
		addStatusLine();
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
		getStatusLineManager().setMessage("");
		getShell().setText(Constants.APP_NAME);
	}

	@Override
	public boolean close() {
		if (textChanged) {
			final MessageBox box = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_QUESTION);
			box.setText("Save");
			box.setMessage("Text has been modified. Would you like to save it before closing?");
			int response = box.open();
			if (response == SWT.CANCEL) {
				return false;
			} else if (response == SWT.NO) {
				return super.close();
			} else {
				if (!save()) {
					return false;
				}
			}
		}
		return super.close();
	}

	/**
	 * Configure preferences using the {@link PreferenceProvider}.
	 */
	private void configurePreferences() {
		PreferenceProvider preferenceProvider = new PreferenceProvider();
		preferenceManager = preferenceProvider.getPreferenceManager();
		preferenceStore = preferenceProvider.getPreferenceStore();
		preferenceStore.addPropertyChangeListener(propertyChangeListener);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Constants.APP_NAME);
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
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(container);
		GridLayoutFactory.swtDefaults().applyTo(container);
		imageProvider = new ImageProvider(getShell());
		createToolbars(container, imageProvider);
		createViewer(container);

		final Textify finalTextify = this;
		Display.getDefault().asyncExec(() -> {
			createContextMenu();
			configurePreferences();
			initWidgets();
			search = new SearchBar(container, imageProvider, finalTextify);
			search.setVisible(false);
			GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).exclude(true)
					.applyTo(search.getControl());
			handleCliArgs();
			viewer.getTextWidget().setFocus();
		});
		return parent;
	}

	/**
	 * Creates the context menu
	 */
	protected void createContextMenu() {
		MenuManager contextMenu = new MenuManager("#ViewerMenu");
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(this::fillContextMenu);
		Menu menu = contextMenu.createContextMenu(viewer.getTextWidget());
		viewer.getTextWidget().setMenu(menu);
	}

	/**
	 * Create the left-most toolbar with its buttons.
	 *
	 * @param parent        {@link Composite}
	 * @param imageProvider {@link ImageProvider}
	 */
	private void createLeftToolbar(Composite parent, ImageProvider imageProvider) {
		final Composite leftToolBar = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).grab(false, false).applyTo(leftToolBar);
		GridLayoutFactory.swtDefaults().numColumns(4).equalWidth(true).applyTo(leftToolBar);

		Button button = createButton(leftToolBar, "New", imageProvider.getNewImage(), "Start a new document");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).hint(80, 30).applyTo(button);
		button.addListener(SWT.Selection, event -> newFile());

		button = createButton(leftToolBar, "Open", imageProvider.getOpenImage(), "Open an existing document");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(button);
		button.addListener(SWT.Selection, event -> openFile());

		button = createButton(leftToolBar, "Save", imageProvider.getSaveImage(), "Save current document to file");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(button);
		button.addListener(SWT.Selection, event -> save());

		button = createButton(leftToolBar, "Save As", imageProvider.getSaveAsImage(),
				"Save current document as a new file");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(button);
		button.addListener(SWT.Selection, event -> saveAs());
	}

	/**
	 * Create the right-most toolbar with its buttons.
	 *
	 * @param parent        {@link Composite}
	 * @param imageProvider {@link ImageProvider}
	 */
	private void createRightToolbar(Composite parent, ImageProvider imageProvider) {
		// right toolbar
		final Composite rightToolBar = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.FILL).grab(true, false).applyTo(rightToolBar);
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
		});

		// Key bindings
		MenuItem keyBindingsItem = new MenuItem(menu, SWT.PUSH);
		keyBindingsItem.setText("Key Bindings");
		keyBindingsItem.addListener(SWT.Selection, event -> new KeyBindingsDialog(getShell(), imageProvider).open());

		// About menu
		MenuItem aboutItem = new MenuItem(menu, SWT.PUSH);
		aboutItem.setText("About");
		aboutItem.addListener(SWT.Selection, event -> new AboutDialog(getShell(), imageProvider).open());

		// hamburger
		final Button menuButton = new Button(rightToolBar, SWT.NONE);

		menuButton.setImage(imageProvider.getMenuImage());
		GridDataFactory.swtDefaults().hint(40, 30).align(SWT.FILL, SWT.FILL).applyTo(menuButton);
		menuButton.addListener(SWT.Selection, event -> {
			Rectangle rect = menuButton.getBounds();
			Point pt = new Point(rect.x, rect.y + rect.height);
			pt = rightToolBar.toDisplay(pt);
			menu.setLocation(pt.x, pt.y);
			menu.setVisible(true);
		});
		rightToolBar.pack();
	}

	@Override
	protected void createStatusLine(Shell shell) {
		getStatusLineManager().createControl(shell);
	}

	@Override
	protected StatusLineManager createStatusLineManager() {
		return new StatusLineManager();
	}

	/**
	 * Create the left and right toolbars.
	 *
	 * @param imageProvider {@link ImageProvider}
	 */
	private void createToolbars(Composite parent, ImageProvider imageProvider) {
		Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.FILL).applyTo(container);
		GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0).equalWidth(false).applyTo(container);
		createLeftToolbar(container, imageProvider);
		createRightToolbar(container, imageProvider);
	}

	/**
	 * Create the {@link TextViewer}.
	 *
	 * @param parent {@link Composite}
	 */
	private void createViewer(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).hint(640, 480).applyTo(container);
		FillLayoutFactory.fillDefaults().applyTo(container);

		// viewer with ruler based on prefs
		ruler = new CompositeRuler();
		viewer = new SourceViewer(container, ruler, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		viewer.setDocument(new Document());
		presentation = new TextPresentation();

		// property change listener
		propertyChangeListener = new PreferenceChangeListener(this);

		// key listener
		viewer.getTextWidget().addKeyListener(new KeyListener(this));

		// text listener
		viewer.addTextListener(event -> {
			textChanged = true;
			// set shell title
			getShell().setText("* " + getShell().getText().replaceFirst("\\* ", ""));
		});
	}

	/**
	 * Disposes the images and font and removes preferences listener.
	 */
	private void dispose() {
		preferenceStore.removePropertyChangeListener(propertyChangeListener);
		if (cursorLinePainterColor != null && !cursorLinePainterColor.isDisposed()) {
			cursorLinePainterColor.dispose();
		}
		if (cursorLinePainter != null) {
			cursorLinePainter.dispose();
		}
		if (font != null && !font.isDisposed()) {
			font.dispose();
		}
		if (imageProvider != null) {
			imageProvider.dispose();
		}
	}

	/**
	 * Fill dynamic context menu.
	 *
	 * @param contextMenu {@link IMenuManager}
	 */
	protected void fillContextMenu(IMenuManager contextMenu) {
		// Cut
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(getClass(), "/images/cut.png");
		contextMenu.add(new Action("Cut", descriptor) {
			@Override
			public void run() {
				((ITextOperationTarget) viewer).doOperation(ITextOperationTarget.CUT);
			}
		});
		// Copy
		descriptor = ImageDescriptor.createFromFile(getClass(), "/images/copy.png");
		contextMenu.add(new Action("Copy", descriptor) {
			@Override
			public void run() {
				((ITextOperationTarget) viewer).doOperation(ITextOperationTarget.COPY);
			}
		});
		// Paste
		descriptor = ImageDescriptor.createFromFile(getClass(), "/images/paste.png");
		contextMenu.add(new Action("Paste", descriptor) {
			@Override
			public void run() {
				((ITextOperationTarget) viewer).doOperation(ITextOperationTarget.PASTE);
			}
		});
	}

	/**
	 * Return the line painter that highlights the current line.
	 *
	 * @return {@link IPainter}
	 */
	public IPainter getCursorLinePainter() {
		return cursorLinePainter;
	}

	/**
	 * @return the preferenceManager
	 */
	public PreferenceManager getPreferenceManager() {
		return preferenceManager;
	}

	/**
	 * @return the preferenceStore
	 */
	public PreferenceStore getPreferenceStore() {
		return preferenceStore;
	}

	public TextPresentation getPresentation() {
		return presentation;
	}

	/**
	 * @return the ruler
	 */
	public CompositeRuler getRuler() {
		return ruler;
	}

	/**
	 * @return the search
	 */
	public SearchBar getSearch() {
		return search;
	}

	/**
	 * @return the viewer
	 */
	public ISourceViewer getViewer() {
		return viewer;
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
						loadFile(file);
					}
					LOGGER.log(Level.INFO, "File created - loading...");
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
	 * Hides the cursor line.
	 */
	private void hideCursorLine() {
		if (cursorLinePainter != null && viewer instanceof ITextViewerExtension2 extension) {
			extension.removePainter(cursorLinePainter);
			cursorLinePainter.deactivate(true);
			cursorLinePainter.dispose();
			cursorLinePainter = null;
		}
	}

	/**
	 * Initialize appropriate widgets to their value in preferences.
	 */
	private void initWidgets() {
		// highlight current (caret) line
		final boolean cursorLineBackgroundProperty = preferenceStore
				.getBoolean(Constants.CURSOR_LINE_PAINTER_PROPERTY_NAME);
		if (cursorLineBackgroundProperty) {
			cursorLinePainter = new CursorLinePainter(viewer);
			final String hexCode = preferenceStore.getString(Constants.CURSOR_LINE_PAINTER_COLOR_PROPERTY_NAME);
			RGB rgb = ColorUtils.convertToRGB(hexCode);
			if (cursorLinePainterColor != null && !cursorLinePainterColor.isDisposed()) {
				cursorLinePainterColor.dispose();
			}
			cursorLinePainterColor = new Color(rgb);
			cursorLinePainter.deactivate(true);
			cursorLinePainter.setHighlightColor(cursorLinePainterColor);
			ITextViewerExtension2 extension = (ITextViewerExtension2) viewer;
			extension.addPainter(cursorLinePainter);
			cursorLinePainter.paint(IPainter.CONFIGURATION);
		}

		// set text wrap
		final boolean wrapProperty = preferenceStore.getBoolean(Constants.WRAP_PROPERTY_NAME);
		viewer.getTextWidget().setWordWrap(wrapProperty);

		// set text font
		final String fontProperty = preferenceStore.getString(Constants.FONT_PROPERTY_NAME);
		if (fontProperty != null && !fontProperty.isEmpty()) {
			try {
				FontData fontData = FontUtils.getFontData(fontProperty);
				font = new Font(getShell().getDisplay(), fontData);
				viewer.getTextWidget().setFont(font);
				ruler.setFont(font);
				ruler.relayout();
			} catch (FontException e) {
				showError("An error occurred getting font from preferences.", e);
			}
		}

		// set line numbers
		if (preferenceStore.getBoolean(Constants.LINE_NUMBER_PROPERTY_NAME)) {
			final LineNumberRulerColumn numbers = new LineNumberRulerColumn();
			numbers.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
			numbers.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
			ruler.addDecorator(0, numbers);
		} else {
			// remove all decorators including highlighter
			final Iterator<IVerticalRulerColumn> iterator = ruler.getDecoratorIterator();
			int index = 0;
			while (iterator.hasNext()) {
				ruler.removeDecorator(index++);
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
				builder.append(System.lineSeparator());
			}
			// set in text widget
			viewer.getDocument().set(builder.toString());
			currentFile = file;
			textChanged = false;
			getStatusLineManager().setMessage(file.getAbsolutePath());
			getShell().setText(file.getName());
			viewer.getTextWidget().setFocus();
		} catch (IOException | IllegalArgumentException e) {
			showError("An error occurred loading the file.", e);
		}
	}

	/**
	 * Respond to the user pressing the New button.
	 */
	public void newFile() {
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
	public void openFile() {
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
	public boolean save() {
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
	 * Set the background color of the line with the cursor.
	 *
	 * @param rgb {@link RGB}
	 */
	public void setCursorLineBackgroundColor(RGB rgb) {
		if (cursorLinePainterColor != null && !cursorLinePainterColor.isDisposed()) {
			hideCursorLine();
		}
		cursorLinePainterColor = new Color(rgb);
		showCursorLine();
	}

	/**
	 * Set the viewer and its ruler to the provided font.
	 *
	 * @param fontData {@link FontData}
	 */
	public void setFont(FontData fontData) {
		final Font newFont = new Font(getShell().getDisplay(), fontData);
		viewer.getTextWidget().setFont(newFont);
		if (this.font != null && !this.font.isDisposed()) {
			this.font.dispose();
		}
		this.font = newFont;
		ruler.setFont(newFont);
		ruler.relayout();
	}

	/**
	 * Shows the cursor line.
	 */
	private void showCursorLine() {
		if (cursorLinePainter == null && viewer instanceof ITextViewerExtension2 extension) {
			cursorLinePainter = new CursorLinePainter(viewer);
			cursorLinePainter.setHighlightColor(cursorLinePainterColor);
			extension.addPainter(cursorLinePainter);
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
			getStatusLineManager().setMessage(file.getAbsolutePath());
			getShell().setText(file.getName());
			return true;
		} catch (IOException e) {
			showError("An error occurred writing the file out to disk.", e);
		}
		return false;
	}
}
