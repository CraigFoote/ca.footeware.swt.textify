/**
 *
 */
package ca.footeware.swt.textify.processors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

import ca.footeware.swt.textify.Constants;
import ca.footeware.swt.textify.Textify;

/**
 *
 */
public class Highlighter {

	private static final Logger LOGGER = LogManager.getLogger(Highlighter.class);
	private Textify textify;

	/**
	 * Perform the actual highlighting.
	 */
	private void doHighlight() {
		final ITextSelection selection = (ITextSelection) textify.getViewer().getSelectionProvider().getSelection();
		// clear styles
		textify.getPresentation().clear();
		if (selection != null && !selection.isEmpty()) {
			final FindReplaceDocumentAdapter finder = new FindReplaceDocumentAdapter(textify.getViewer().getDocument());
			try {
				IRegion region = null;
				int startIndex = 0;
				final int docLength = textify.getViewer().getDocument().getLength();
				do {
					// find selected text
					region = finder.find(startIndex, selection.getText(), true, false, false, false);
					if (region == null) {
						break;
					}
					startIndex = region.getOffset() + region.getLength();
					// create a new style
					final Color fgColor = Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE);
					final Color bgColor = Display.getDefault().getSystemColor(SWT.COLOR_YELLOW);
					final TextAttribute attr = new TextAttribute(fgColor, bgColor, 0);
					final StyleRange styleRange = new StyleRange(region.getOffset(), region.getLength(),
							attr.getForeground(), attr.getBackground());
					textify.getPresentation().addStyleRange(styleRange);
				} while (region != null && startIndex < docLength);
			} catch (BadLocationException e1) {
				LOGGER.log(Level.ERROR, "An error occurred finding a region.", e1);
			}
		}
		TextPresentation.applyTextPresentation(textify.getPresentation(), textify.getViewer().getTextWidget());
	}

	/**
	 * Highlight all occurrences of selected text based on preference.
	 *
	 * @param textify {@link Textify}
	 */
	public void highlightText(Textify textify) {
		this.textify = textify;
		if (!textify.getPreferenceStore().getBoolean(Constants.HIGHLIGHT_PROPERTY_NAME)) {
			textify.getPresentation().clear();
			TextPresentation.applyTextPresentation(textify.getPresentation(), textify.getViewer().getTextWidget());
		} else {
			textify.getHighlighter().doHighlight();
		}
	}
}
