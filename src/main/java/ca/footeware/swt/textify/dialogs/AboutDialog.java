/**
 *
 */
package ca.footeware.swt.textify.dialogs;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

import com.vaadin.open.Open;

import ca.footeware.swt.textify.providers.ImageProvider;

/**
 *
 */
public class AboutDialog extends Dialog {

	private static final Logger LOGGER = LogManager.getLogger(AboutDialog.class);
	private ImageProvider imageProvider;

	public AboutDialog(Shell parent, ImageProvider imageProvider) {
		super(parent, SWT.APPLICATION_MODAL);
		this.imageProvider = imageProvider;
	}

	private void createContents(final Shell shell) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 20;
		gridLayout.marginWidth = 10;
		shell.setLayout(gridLayout);

		final Label label = new Label(shell, SWT.NONE);
		label.setImage(imageProvider.getProgrammerImage());

		final Link link = new Link(shell, SWT.NONE);
		link.setText("Another fine mess by <a href=\"http://footeware.ca\">Footeware.ca</a>");
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		link.setLayoutData(gridData);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Open.open(e.text);
			}
		});

		final Link versionLink = new Link(shell, SWT.NONE);
		versionLink.setText(
				"<a href=\"https://github.com/CraigFoote/ca.footeware.swt.textify/releases\">" + getVersion() + "</a>");
		gridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		versionLink.setLayoutData(gridData);
		versionLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Open.open(e.text);
			}
		});

		final Button button = new Button(shell, SWT.PUSH);
		button.setText("Close");
		gridData = new GridData(SWT.END, SWT.CENTER, true, false);
		gridData.widthHint = 90;
		button.setLayoutData(gridData);
		button.setFocus();
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});

		shell.setDefaultButton(button);
	}

	private String getVersion() {
		InputStream in = AboutDialog.class.getClassLoader().getResourceAsStream("version.properties");
		try {
			Properties props = new Properties();
			props.load(in);
			return props.getProperty("version");
		} catch (IOException e) {
			LOGGER.log(Level.ERROR, "An error occurred getting app version.", e);
			return e.getMessage();
		}
	}

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
