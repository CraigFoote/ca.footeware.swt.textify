/**
 *
 */
package ca.footeware.swt.textify.dialogs;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ca.footeware.swt.textify.providers.ImageProvider;

/**
 * Displays keybindings and their descriptions.
 */
public class KeyBindingsDialog extends Dialog {

	private ImageProvider imageProvider;

	/**
	 * Constructor.
	 * 
	 * @param parent        {@link Shell}
	 * @param imageProvider {@link ImageProvider}
	 */
	public KeyBindingsDialog(Shell parent, ImageProvider imageProvider) {
		super(parent, SWT.APPLICATION_MODAL);
		this.imageProvider = imageProvider;
	}

	/**
	 * Create and layout the widgets.
	 * 
	 * @param shell {@link Shell}
	 */
	private void createContents(final Shell shell) {
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).spacing(20, 20).margins(20, 10).applyTo(shell);

		// keyboard image
		final Label keyboardImage = new Label(shell, SWT.NONE);
		keyboardImage.setImage(imageProvider.getKeyboardImage());
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.FILL).span(2, 1).applyTo(keyboardImage);

		// list of key bindings
		final String shortcuts = """
				Ctrl+A
				Ctrl+F
				Ctrl+P
				Ctrl+S
				Ctrl+W
				Esc""";
		Label label = new Label(shell, SWT.NONE);
		label.setText(shortcuts);
		GridDataFactory.swtDefaults().align(SWT.RIGHT, SWT.FILL).grab(true, true).applyTo(label);
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(label.getFont()).setStyle(SWT.BOLD);
		final Font boldFont = boldDescriptor.createFont(label.getDisplay());
		label.setFont(boldFont);
		// dispose of font we just created
		shell.addDisposeListener(e -> boldFont.dispose());

		// list of descriptions
		final String descriptions = """
				Select All
				Open Search Bar
				Print
				Save
				Close Window
				Close Search Bar When in Focus""";
		label = new Label(shell, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.FILL).grab(true, true).applyTo(label);
		label.setText(descriptions);

		// close button
		final Button closeButton = new Button(shell, SWT.PUSH);
		closeButton.setText("Close");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.FILL).span(2, 1).applyTo(closeButton);
		closeButton.setFocus();
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});

		shell.setDefaultButton(closeButton);
	}

	/**
	 * Open the dialog.
	 */
	public void open() {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		createContents(shell);
		shell.pack();
		shell.open();
		final Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
