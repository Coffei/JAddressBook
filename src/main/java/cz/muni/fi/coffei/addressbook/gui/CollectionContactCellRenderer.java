package cz.muni.fi.coffei.addressbook.gui;

import java.awt.Component;
import java.util.Collection;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import cz.muni.fi.pv168.Contact;

public class CollectionContactCellRenderer extends DefaultTableCellRenderer {

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		if(value!=null) {
			Collection col = (Collection)value;
					
			StringBuilder text = null;
			if (!col.isEmpty()) {
				text = new StringBuilder("{ ");
				for (Object c : (Collection) value) {
					Contact contact = (Contact) c;
					text.append(contact.getType());
					text.append("=");
					text.append(contact.getValue());
					text.append(" ");
				}
				text.append("}");
				setText(text.toString());
			} else {
				setText("");
			}
		}

		return this;
	}

}
