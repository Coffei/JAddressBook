package cz.muni.fi.coffei.addressbook.gui;

import java.awt.Window;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import cz.muni.fi.pv168.ServiceFailureException;

public class ExceptionDialogs {
	
	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("cz.muni.fi.coffei.addressbook.gui.Windows"); //$NON-NLS-1$

	static public void notifyOfException(Exception e, final boolean closeOnFinish, final Window parent) {
		if(e==null)
			throw new NullPointerException("exception");
		if(parent==null)
			throw new NullPointerException("parent");
		
		final String message, title;

		if(e instanceof ServiceFailureException) {
			message = BUNDLE.getString("Exceptions.serviceFailure.message") 
					+ (closeOnFinish? "\n" + BUNDLE.getString("Exceptions.windowWillClose") : "");

			title = BUNDLE.getString("Exceptions.serviceFailure.title");

		} else {

			message = BUNDLE.getString("Exceptions.general.message") 
					+ (closeOnFinish? "\n" + BUNDLE.getString("Exceptions.windowWillClose") : "");

			title = BUNDLE.getString("Exceptions.general.title");

		}


		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
				if (closeOnFinish) {
					parent.setVisible(false);
					parent.dispose();
				}

			}
		});
	}

}
