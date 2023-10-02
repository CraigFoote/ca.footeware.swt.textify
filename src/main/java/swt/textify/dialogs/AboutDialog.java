/**
 *
 */
package swt.textify.dialogs;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

/**
 *
 */
public class AboutDialog extends Dialog {

	private Image programmerImage;

	public AboutDialog(Shell parent) {
		super(parent, SWT.APPLICATION_MODAL);
	}

	private void createContents(final Shell shell) {
		final GridLayout gridLayout = new GridLayout();
		gridLayout.verticalSpacing = 20;
		gridLayout.marginWidth = 10;
		shell.setLayout(gridLayout);

		final InputStream in = AboutDialog.class.getResourceAsStream("/images/programmer.jpg");
		programmerImage = new Image(Display.getDefault(), in);
		final Label label = new Label(shell, SWT.NONE);
		label.setImage(programmerImage);

		final Link link = new Link(shell, SWT.NONE);
		link.setText("Another fine mess by <a href=\"http://footeware.ca\">Footeware.ca</a>");
		GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		link.setLayoutData(gridData);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final String url = e.text;
				if (Desktop.isDesktopSupported()) {
					final Desktop desktop = Desktop.getDesktop();
					try {
						final URI uri = new URI(url);
						desktop.browse(uri);
					} catch (IOException | URISyntaxException ex) {
						// ignore
					}
				}
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
				final String url = e.text;
				if (Desktop.isDesktopSupported()) {
					final Desktop desktop = Desktop.getDesktop();
					try {
						final URI uri = new URI(url);
						desktop.browse(uri);
					} catch (IOException | URISyntaxException ex) {
						// ignore
					}
				}
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
		final InputStream in = AboutDialog.class
				.getResourceAsStream("/META-INF/maven/ca.footeware/swt.textify/pom.xml");
		try {
			final MavenXpp3Reader reader = new MavenXpp3Reader();
			final Model model = reader.read(in);
			return model.getVersion();
		} catch (IOException | XmlPullParserException e) {
			return e.getMessage();
		}
	}

	public void open() {
		final Shell shell = new Shell(getParent(), getStyle());
		shell.setText(getText());
		shell.addDisposeListener(e -> {
			if (programmerImage != null) {
				programmerImage.dispose();
				programmerImage = null;
			}
		});
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
