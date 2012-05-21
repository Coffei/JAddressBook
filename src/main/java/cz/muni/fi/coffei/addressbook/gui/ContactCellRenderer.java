package cz.muni.fi.coffei.addressbook.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import cz.muni.fi.pv168.Contact;

public class ContactCellRenderer extends DefaultTableCellRenderer {

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		if(value!=null) {
			Contact c = (Contact)value;
			setText(c.getValue());

		} else {
			setText("");
		}

		return this;
	}



}
