/**
 * 
 */
package swt.textify;

import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */
public class Textify {

	private static Text text;
	private static Image newImage;
	private static Image openImage;
	private static Image saveImage;
	private static Image saveAsImage;
	private static Image hamburgerImage;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setText("textify");
		shell.setSize(800, 600);
		shell.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				dispose();
			}
		});
		GridLayout gridLayout = new GridLayout();
		shell.setLayout(gridLayout);

		Composite toolbar = new Composite(shell, SWT.NONE);
		GridData gridData = new GridData(GridData.FILL, GridData.CENTER, true, false);
		toolbar.setLayoutData(gridData);
		gridLayout = new GridLayout(5, false);
		toolbar.setLayout(gridLayout);

		Button button = new Button(toolbar, SWT.PUSH);
		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		button.setLayoutData(gridData);
		InputStream in = Textify.class.getResourceAsStream("/new.png");
		newImage = new Image(display, in);
		button.setImage(newImage);
		button.setToolTipText("Start a new document");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text.setText("");
			}
		});

		button = new Button(toolbar, SWT.PUSH);
		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		button.setLayoutData(gridData);
		in = Textify.class.getResourceAsStream("/open.png");
		openImage = new Image(display, in);
		button.setImage(openImage);
		button.setToolTipText("Open an existing document");

		button = new Button(toolbar, SWT.PUSH);
		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		button.setLayoutData(gridData);
		in = Textify.class.getResourceAsStream("/save.png");
		saveImage = new Image(display, in);
		button.setImage(saveImage);
		button.setToolTipText("Save document");

		button = new Button(toolbar, SWT.PUSH);
		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
		button.setLayoutData(gridData);
		in = Textify.class.getResourceAsStream("/save-as.png");
		saveAsImage = new Image(display, in);
		button.setImage(saveAsImage);
		button.setToolTipText("Save document as a new file");

		button = new Button(toolbar, SWT.PUSH);
		gridData = new GridData(GridData.END, GridData.FILL, true, false);
		button.setLayoutData(gridData);
		in = Textify.class.getResourceAsStream("/hamburger.png");
		hamburgerImage = new Image(display, in);
		button.setImage(hamburgerImage);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AboutDialog dialog = new AboutDialog(shell);
				dialog.open();
			}
		});

		ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.V_SCROLL | SWT.BORDER);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		scrolledComposite.setLayoutData(gridData);

		text = new Text(scrolledComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		text.setLayoutData(gridData);

		scrolledComposite.setContent(text);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				Rectangle r = scrolledComposite.getClientArea();
				scrolledComposite.setMinSize(text.computeSize(r.width, SWT.DEFAULT));
			}
		});

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		// disposes all associated windows and their components
		display.dispose();
	}

	protected static void dispose() {
		if (newImage != null) {
			newImage.dispose();
			newImage = null;
		}
		if (openImage != null) {
			openImage.dispose();
			openImage = null;
		}
		if (saveImage != null) {
			saveImage.dispose();
			saveImage = null;
		}
		if (saveAsImage != null) {
			saveAsImage.dispose();
			saveAsImage = null;
		}
		if (hamburgerImage != null) {
			hamburgerImage.dispose();
			hamburgerImage = null;
		}
	}

}
