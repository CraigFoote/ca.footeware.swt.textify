/**
 *
 */
package ca.footeware.swt.textify.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ca.footeware.swt.textify.providers.ImageProvider;

/**
 *
 */
public class KeyBindingsDialog extends Dialog {

	private ImageProvider imageProvider;

	public KeyBindingsDialog(Shell parentShell, ImageProvider imageProvider) {
		super(parentShell);
		this.imageProvider = imageProvider;
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		if (id == IDialogConstants.CANCEL_ID) {
			return null;
		}
		return super.createButton(parent, id, label, defaultButton);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(area, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(container);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).spacing(20, 20).margins(20, 10)
				.applyTo(container);

		// keyboard image
		final Label keyboardImage = new Label(container, SWT.NONE);
		keyboardImage.setImage(imageProvider.getKeyboardImage());
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).span(2, 1).applyTo(keyboardImage);

		// list of key bindings
		final String shortcuts = """
				Ctrl+A
				Ctrl+F
				Ctrl+N
				Ctrl+O
				Ctrl+P
				Ctrl+S
				Ctrl+W
				Esc""";
		Label label = new Label(container, SWT.NONE);
		label.setText(shortcuts);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.FILL).grab(true, true).applyTo(label);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(label.getFont()).setStyle(SWT.BOLD);
		final Font boldFont = boldDescriptor.createFont(label.getDisplay());
		label.setFont(boldFont);
		// dispose of font we just created
		getShell().addDisposeListener(e -> boldFont.dispose());

		// list of descriptions
		final String descriptions = """
				Select All
				Open Search Bar
				Create a New File
				Open a File
				Print
				Save
				Close Window
				Close Search Bar When in Focus""";
		label = new Label(container, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).grab(true, true).applyTo(label);
		label.setText(descriptions);

		return area;
	}

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.APPLICATION_MODAL);
	}
}
