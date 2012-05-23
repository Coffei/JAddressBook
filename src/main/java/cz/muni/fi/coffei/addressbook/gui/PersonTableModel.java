package cz.muni.fi.coffei.addressbook.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;

import javax.swing.table.AbstractTableModel;

import org.springframework.context.ApplicationContext;

import cz.muni.fi.pv168.Contact;
import cz.muni.fi.pv168.ContactManager;
import cz.muni.fi.pv168.DBUtils;
import cz.muni.fi.pv168.Person;
import cz.muni.fi.pv168.PersonManager;
import cz.muni.fi.pv168.ServiceFailureException;

public class PersonTableModel extends AbstractTableModel {
	
	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("cz.muni.fi.coffei.addressbook.gui.Windows"); //$NON-NLS-1
	
	private List<PersonContainer> people = new ArrayList<>();
	private ApplicationContext appCtx;
	
	/**
	 * Ctor, may be long-lasting!
	 * @throws ServiceFailureException when backing store operation fails
	 */
	public PersonTableModel() throws ServiceFailureException {
		appCtx = DBUtils.getAppContext();
		
		loadAllPeople();
		
	}
	
	
	
	private void loadAllPeople() throws ServiceFailureException {
		PersonManager personMan = appCtx.getBean("personManager", PersonManager.class);
		ContactManager contactMan = appCtx.getBean("contactManager", ContactManager.class);
		
		
		List<Person> allpeople = personMan.findAllPersons();
		for(Person p : allpeople) {
			PersonContainer pc = new PersonContainer(p);
			List<Contact> contacts = new ArrayList();//contactMan.findContactsByPerson(p);
			for(Contact c : contacts) {
				pc.addContact(c);
			}
			people.add(pc);
		}
		
		fireTableRowsInserted(0, people.size()-1);
	}
	
	
	
	@Override
	public int getRowCount() {
		return people.size();
	}

	@Override
	public int getColumnCount() { //Name, mail, phone (mobile), address, born, nick
		return 7;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex >= people.size() && rowIndex >=0)
			throw new IndexOutOfBoundsException("rowIndex expected bewteen 0 and " + people.size());
		
		switch (columnIndex) {
		case 0: 
			return people.get(rowIndex).getName();
		case 1: //mail
			return people.get(rowIndex).getPropertyContact(ContactType.EMAIL);
		case 2: //phone (mobile)
			return people.get(rowIndex).getPropertyContact(ContactType.MOBILE);
		case 3: //address
			return people.get(rowIndex).getPropertyContact(ContactType.ADDRESS);
		case 4: //born
			return people.get(rowIndex).getBorn();
		case 5: //nick
			return people.get(rowIndex).getPropertyContact(ContactType.NICK);
		case 6:
			return people.get(rowIndex).getOtherContacts();
			default:
				throw new UnsupportedOperationException("unsupported columnIndex");
		}
		
		
	}
	
	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: return BUNDLE.getString("MainWindow.nameCol");
		case 1: return BUNDLE.getString("MainWindow.mailCol");
		case 2: return BUNDLE.getString("MainWindow.mobileCol");
		case 3: return BUNDLE.getString("MainWindow.addressCol");
		case 4: return BUNDLE.getString("MainWindow.bornCol");
		case 5: return BUNDLE.getString("MainWindow.nickCol");
		case 6: return BUNDLE.getString("MainWindow.otherCol");
			default:
				throw new UnsupportedOperationException("unsupported columnIndex");
		}
		
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0: return String.class;
		case 1:
		case 2:
		case 3:
		case 5: return Contact.class;
		case 4: return Calendar.class;
		case 6: return Collection.class;
			default:
				throw new UnsupportedOperationException("unsupported columnIndex");
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	/**
	 * For internal use ONLY!
	 * @author Coffei
	 *
	 */
	private class PersonContainer extends Person {
		private List<Contact> other;
		private Map<ContactType, Contact> properties;
		
		public PersonContainer() {
			other = new ArrayList<>(2);
			properties = new HashMap<>(4);
		}
		
		public PersonContainer(Person person) {
			this();
			setPerson(person);
		}
		
		public void addContact(Contact contact) {
			if(contact==null)
				throw new NullPointerException("contact");
			
			if(contact.getType().equals("email")) {
				properties.put(ContactType.EMAIL, contact);
			} else if (contact.getType().equals("mobile")) {
				properties.put(ContactType.MOBILE, contact);
			} else if (contact.getType().equals("address")) {
				properties.put(ContactType.ADDRESS, contact);
			} else if (contact.getType().equals("nick")) {
				properties.put(ContactType.NICK, contact);
			} else {
				other.add(contact);
			}
		}
		
		public void setPerson(Person person) {
			if(person==null)
				throw new NullPointerException("person");
			
			this.setId(person.getId());
			this.setName(person.getName());
			this.setBorn(person.getBorn());
		}
		
		public Contact getPropertyContact(ContactType type) {
			if(type==null)
				throw new NullPointerException("type");
			
			return properties.get(type);
		}
		
		public Collection<Contact> getOtherContacts() {
			return Collections.unmodifiableCollection(other);
		}
	
	}
	
	private enum ContactType {
		EMAIL, MOBILE, ADDRESS, NICK
	}

}
	
