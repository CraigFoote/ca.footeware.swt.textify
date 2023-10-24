package ca.footeware.swt.textify.listeners;

import java.util.Iterator;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import ca.footeware.swt.textify.Constants;
import ca.footeware.swt.textify.Textify;
import ca.footeware.swt.textify.exceptions.FontException;
import ca.footeware.swt.textify.preferences.FontUtils;

/**
 * Listen for preference changes and apply new settings to widgets.
 */
public final class PreferenceChangeListener implements IPropertyChangeListener {

	private static final Logger LOGGER = LogManager.getLogger(PreferenceChangeListener.class);
	private Textify textify;

	/**
	 * Constructor.
	 *
	 * @param textify {@link Textify}
	 */
	public PreferenceChangeListener(final Textify textify) {
		this.textify = textify;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		final String propertyName = event.getProperty();
		switch (propertyName) {
		case Constants.CURSOR_LINE_PAINTER_PROPERTY_NAME:
			ITextViewerExtension2 extension = (ITextViewerExtension2) textify.getViewer();
			if ((boolean) event.getNewValue()) {
				extension.addPainter(textify.getCursorLinePainter());
			} else {
				extension.removePainter(textify.getCursorLinePainter());
			}
			break;
		case Constants.FONT_PROPERTY_NAME:
			try {
				final FontData fontData = FontUtils.getFontData((String) event.getNewValue());
				textify.setFont(fontData);
				textify.getViewer().getTextWidget().setFocus();
			} catch (FontException e1) {
				LOGGER.log(Level.ERROR, "An error occurred getting font from preferences.", e1);
			}
			break;
		case Constants.LINE_NUMBER_PROPERTY_NAME:
			if ((boolean) event.getNewValue()) {
				final LineNumberRulerColumn numbers = new LineNumberRulerColumn();
				numbers.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION));
				numbers.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
				textify.getRuler().addDecorator(0, numbers);
			} else {
				Iterator<IVerticalRulerColumn> iterator = textify.getRuler().getDecoratorIterator();
				int index = 0;
				while (iterator.hasNext()) {
					textify.getRuler().removeDecorator(index++);
				}
			}
			break;
		case Constants.WRAP_PROPERTY_NAME:
			textify.getViewer().getTextWidget().setWordWrap((boolean) event.getNewValue());
			break;
		default:
			throw new IllegalArgumentException("Unknown property: " + propertyName);
		}
	}
}
