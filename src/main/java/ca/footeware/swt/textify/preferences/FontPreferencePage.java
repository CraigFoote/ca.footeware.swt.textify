package ca.footeware.swt.textify.preferences;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;

import ca.footeware.swt.textify.exceptions.FontException;

/**
 * This class creates a preference page that allows the user to choose the
 * display font.
 */
public class FontPreferencePage extends PreferencePage {

	private static final String FONT_PROPERTY_NAME = "Font";
	private static final Logger LOGGER = LogManager.getLogger(FontPreferencePage.class);
	private Font font;
	private Label fontLabel;

	/**
	 * Constructor.
	 */
	public FontPreferencePage() {
		super(FONT_PROPERTY_NAME);
		setDescription("Select a display font.");
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(getClass(), "/images/fonts.png");
		setImageDescriptor(descriptor);
	}

	/**
	 * Creates the controls for this page
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).extendedMargins(25, 25, 25, 25).equalWidth(false)
				.applyTo(composite);

		IPreferenceStore preferenceStore = getPreferenceStore();

		// Create the label displaying the selected font
		fontLabel = new Label(composite, SWT.NONE);
		try {
			String fontProperty = preferenceStore.getString(FONT_PROPERTY_NAME);
			if (fontProperty != null && !fontProperty.isEmpty()) {
				font = new Font(parent.getDisplay(), FontUtils.getFontData(fontProperty));
				fontLabel.setText(FontUtils.getDisplayText(font.getFontData()[0]));
				fontLabel.setFont(font);
			}
		} catch (FontException e) {
			LOGGER.log(Level.ERROR, "An error occurred getting the selected font.", e);
		}
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, true).applyTo(fontLabel);

		// create the button to open the font dialog
		final Button chooseButton = new Button(composite, SWT.PUSH);
		chooseButton.setText("Choose...");
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).applyTo(chooseButton);
		chooseButton.addListener(SWT.Selection, event -> {
			FontDialog fontDialog = new FontDialog(getShell());
			// Pre-fill the dialog with any previous selection
			if (font != null) {
				fontDialog.setFontList(font.getFontData());
			}

			if (fontDialog.open() != null) {
				if (font != null) {
					font.dispose();
				}
				font = new Font(getShell().getDisplay(), fontDialog.getFontList());
				fontLabel.setFont(font);
				fontLabel.setText(FontUtils.getDisplayText(font.getFontData()[0]));
				fontLabel.requestLayout();
			}
		});

		return composite;
	}

	@Override
	public void dispose() {
		if (font != null) {
			font.dispose();
		}
		super.dispose();
	}

	@Override
	protected void performDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		try {
			String defaultProperty = preferenceStore.getDefaultString(FONT_PROPERTY_NAME);
			if (defaultProperty != null && !defaultProperty.isEmpty()) {
				FontData fontData = FontUtils.getFontData(defaultProperty);
				font = new Font(getShell().getDisplay(), fontData);
				fontLabel.setText(FontUtils.getDisplayText(fontData));
				fontLabel.setFont(font);
			} else {
				fontLabel.setText("");
			}
		} catch (FontException e) {
			LOGGER.log(Level.ERROR, "An error occurred setting the default font.", e);
		}
	}

	@Override
	public boolean performOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		if (font != null) {
			preferenceStore.setValue(FONT_PROPERTY_NAME, font.getFontData()[0].toString());
		}
		return true;
	}
}