package cz.muni.fi.coffei.addressbook.gui;

import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CalendarCellRenderer extends DefaultTableCellRenderer {
	
	/*public static void main(String...args) throws ServiceFailureException {
		ApplicationContext appCtx = DBUtils.getAppContext();
		PersonManager personMan = appCtx.getBean("personManager", PersonManager.class);
		ContactManager contactMan = appCtx.getBean("contactManager", ContactManager.class);
		
		Person p = new Person();
		p.setName("Marek Harm");
		p.setBorn(Calendar.getInstance());
		personMan.createPerson(p);
		contactMan.createContact(createCont("email", "ondrcha@email.sk"), p);
		contactMan.createContact(createCont("address", "fakeAddress3"), p);
		contactMan.createContact(createCont("nick", "Ondatra"), p);
		contactMan.createContact(createCont("mobile", "98523364"), p);
		contactMan.createContact(createCont("otherOne", "NiceOneTwo"), p);
		
	}
	
	static Contact createCont(String type, String value) {
		Contact c= new Contact();
		c.setType(type);
		c.setValue(value);
		
		return c;
	}*/
	

	/* (non-Javadoc)
	 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		if(value!=null) {
			Calendar c = (Calendar)value;
			DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
			setText(format.format(c.getTime()));
			
		} else {
			setText("");
		}
		

		return this;
	}



}
