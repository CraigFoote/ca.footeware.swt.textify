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

/**
 * 
 */
public class LineNumberPreferencePage extends PreferencePage {

	private static final String LINE_NUMBER_PROPERTY_NAME = "LineNumbers";
	private Button button;

	/**
	 * Constructor.
	 */
	public LineNumberPreferencePage() {
		super(LINE_NUMBER_PROPERTY_NAME);
		setDescription("Choose whether or not to display line numbers.");
		ImageDescriptor descriptor = ImageDescriptor.createFromFile(getClass(), "/images/numbered-list.png");
		setImageDescriptor(descriptor);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).extendedMargins(25, 25, 25, 25).equalWidth(false)
				.applyTo(composite);

		IPreferenceStore preferenceStore = getPreferenceStore();

		button = new Button(composite, SWT.CHECK);
		button.setText("Show line numbers");

		boolean wrapProperty = preferenceStore.getBoolean(LINE_NUMBER_PROPERTY_NAME);
		button.setSelection(wrapProperty);

		return composite;
	}

	@Override
	protected void performDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		boolean defaultProperty = preferenceStore.getDefaultBoolean(LINE_NUMBER_PROPERTY_NAME);
		button.setSelection(defaultProperty);
	}

	@Override
	public boolean performOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(LINE_NUMBER_PROPERTY_NAME, button.getSelection());
		return true;
	}
}
