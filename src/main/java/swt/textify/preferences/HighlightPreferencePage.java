/**
 *
 */
package swt.textify.preferences;

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
public class HighlightPreferencePage extends PreferencePage {

	private static final String HIGHLIGHT_PROPERTY_NAME = "Highlight";
	private Button button;

	/**
	 * @constructor
	 */
	public HighlightPreferencePage() {
		super(HIGHLIGHT_PROPERTY_NAME);
		setDescription("Highlight all occurrences of selected text.");
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
		button.setText("Highlight Text");

		boolean highlightProperty = preferenceStore.getBoolean(HIGHLIGHT_PROPERTY_NAME);
		button.setSelection(highlightProperty);

		return composite;
	}

	@Override
	protected void performDefaults() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		boolean defaultProperty = preferenceStore.getDefaultBoolean(HIGHLIGHT_PROPERTY_NAME);
		button.setSelection(defaultProperty);
	}

	@Override
	public boolean performOk() {
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setValue(HIGHLIGHT_PROPERTY_NAME, button.getSelection());
		return true;
	}
}
