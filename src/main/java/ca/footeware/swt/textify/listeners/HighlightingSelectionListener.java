package ca.footeware.swt.textify.listeners;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import ca.footeware.swt.textify.Textify;

/**
 * Highlights all occurrences of selected text.
 */
public class HighlightingSelectionListener extends SelectionAdapter {

	private Textify textify;

	/**
	 * Constructor.
	 *
	 * @param textify {@link Textify}
	 */
	public HighlightingSelectionListener(Textify textify) {
		this.textify = textify;
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		textify.getHighlighter().highlightText(textify);
	}
}