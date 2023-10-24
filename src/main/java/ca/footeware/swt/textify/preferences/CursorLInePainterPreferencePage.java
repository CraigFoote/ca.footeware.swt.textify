/**
 *
 */
package ca.footeware.swt.textify.preferences;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.footeware.swt.textify.Constants;

/**
 * Allows a user to specify whether or not to highlight the current line.
 */
public class CursorLInePainterPreferencePage extends PreferencePage {

	private Button button;

	/**
	 * Constructor.
	 */
	public CursorLInePainterPreferencePage() {
		super(Constants.CURSOR_LINE_PAINTER_PROPERTY_NAME);
		setDescription("Choose whether or not to highlight the current line.");
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(getClass(), "/images/highlight.png");
		setImageDescriptor(descriptor);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).extendedMargins(25, 25, 25, 25).equalWidth(false)
				.applyTo(composite);

		IPreferenceStore preferenceStore = getPreferenceStore();

		button = new Button(composite, SWT.CHECK);
		button.setText("Highlight current line");

		boolean wrapProperty = preferenceStore.getBoolean(Constants.CURSOR_LINE_PAINTER_PROPERTY_NAME);
		button.setSelection(wrapProperty);

		return composite;
	}

	@Override
	protected void performDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		boolean defaultProperty = preferenceStore.getDefaultBoolean(Constants.CURSOR_LINE_PAINTER_PROPERTY_NAME);
		button.setSelection(defaultProperty);
	}

	@Override
	public boolean performOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(Constants.CURSOR_LINE_PAINTER_PROPERTY_NAME, button.getSelection());
		return true;
	}

}
