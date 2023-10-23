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
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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

	public AboutDialog(Shell parentShell, ImageProvider imageProvider) {
		super(parentShell);
		this.imageProvider = imageProvider;
	}

	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		if (id == IDialogConstants.CANCEL_ID) {
			return null;
		}
		Button button = super.createButton(parent, id, label, defaultButton);
		button.setFocus();
		return button;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);

		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		container.setLayout(layout);

		final Label label = new Label(container, SWT.NONE);
		label.setImage(imageProvider.getProgrammerImage());

		final Link link = new Link(container, SWT.NONE);
		link.setText("Another fine mess by <a href=\"http://footeware.ca\">Footeware.ca</a>");
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		link.setLayoutData(gridData);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Open.open(e.text);
			}
		});

		final Link versionLink = new Link(container, SWT.NONE);
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

		return area;
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

	@Override
	protected void setShellStyle(int newShellStyle) {
		super.setShellStyle(SWT.APPLICATION_MODAL);
	}
}
