package ca.footeware.swt.textify.listeners;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

import ca.footeware.swt.textify.Textify;

/**
 * Listens for and handles user key strokes.
 */
public class KeyListener extends KeyAdapter {

	private Textify textify;

	/**
	 * Constructor.
	 *
	 * @param textify {@link Textify}
	 */
	public KeyListener(Textify textify) {
		this.textify = textify;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if ((e.stateMask & SWT.CTRL) != 0) {
			final String pressed = Action.findKeyString(e.keyCode);
			if (pressed != null) {
				switch (pressed) {
				case "s":
					textify.save();
					break;
				case "w":
					textify.getViewer().getTextWidget().getShell().close();
					break;
				case "a":
					((ITextOperationTarget) textify.getViewer()).doOperation(ITextOperationTarget.SELECT_ALL);
					break;
				case "p":
					((ITextOperationTarget) textify.getViewer()).doOperation(ITextOperationTarget.PRINT);
					break;
				case "f":
					textify.getSearch().setVisible(true);
					break;
				default:
					// do nothing
				}
			}
		}
		super.keyReleased(e);
	}
}