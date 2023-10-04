/**
 *
 */
package swt.textify.dialogs;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import swt.textify.exceptions.FontException;
import swt.textify.preferences.FontUtils;
import swt.textify.preferences.PreferenceProvider;

/**
 *
 */
public class PreferencesDialog extends Dialog {

	private static final Logger LOGGER = LogManager.getLogger(PreferencesDialog.class);
	private PreferenceProvider prefs;

	public PreferencesDialog(Shell parent, PreferenceProvider prefs) {
		super(parent, SWT.APPLICATION_MODAL | SWT.RESIZE);
		this.prefs = prefs;
	}

	private void createContents(Shell shell) {
		// font-related stuff
		final Group fontGroup = new Group(shell, SWT.SHADOW_ETCHED_OUT);
		fontGroup.setText("Font");
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fontGroup.setLayoutData(gridData);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 20;
		gridLayout.marginWidth = 20;
		fontGroup.setLayout(gridLayout);

		// font label
		final Label fontLabel = new Label(fontGroup, SWT.NONE);
		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		fontLabel.setLayoutData(gridData);
		FontData fontData;
		try {
			final String fontProperty = this.prefs.getProperty("font", FontUtils.getDefaultFontData().toString());
			fontData = FontUtils.getFontData(fontProperty);
			final Font font = new Font(shell.getDisplay(), fontData);
			fontLabel.setFont(font);
			fontLabel.setText(FontUtils.getDisplayText(fontData));
			font.dispose();
		} catch (FontException e) {
			LOGGER.log(Level.ERROR, e);
		}

		// font button
		final Button fontButton = new Button(fontGroup, SWT.PUSH);
		fontButton.setText("Choose...");
		fontButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final FontDialog dialog = new FontDialog(shell);
				String fontProperty = prefs.getProperty("font", new FontData("sans", 14, SWT.NORMAL).toString());
				try {
					dialog.setFontList(new FontData[] { FontUtils.getFontData(fontProperty) });
				} catch (FontException e1) {
					LOGGER.log(Level.ERROR, e1);
				}
				FontData fontData = dialog.open();
				if (fontData != null) {
					final Font font = new Font(shell.getDisplay(), fontData);
					fontLabel.setFont(font);
					fontLabel.setText(FontUtils.getDisplayText(fontData));
					prefs.setProperty("font", fontData.toString());
					prefs.save();
					font.dispose();
				}
			}
		});
		gridData = new GridData(SWT.END, SWT.CENTER, false, false);
		fontButton.setLayoutData(gridData);

		// Close
		Button closeButton = new Button(shell, SWT.PUSH);
		closeButton.setText("Close");
		gridData = new GridData(SWT.END, SWT.BOTTOM, true, true);
		gridData.widthHint = 90;
		closeButton.setLayoutData(gridData);
		closeButton.setFocus();
		closeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});

		shell.setDefaultButton(closeButton);
	}

	public void open() {
		Shell shell = new Shell(getParent(), getStyle());
		shell.setText("Preferences");
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 25;
		gridLayout.marginHeight = 25;
		shell.setLayout(gridLayout);

		createContents(shell);
		shell.setSize(600, 230);
		shell.open();

		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
