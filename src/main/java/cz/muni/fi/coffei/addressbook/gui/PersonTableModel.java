package cz.muni.fi.coffei.addressbook.gui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.springframework.context.ApplicationContext;

import cz.muni.fi.coffei.addressbook.gui.PersonContainer.ContactType;
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

		reloadAllPeople();
	}

	


	public void reloadAllPeople() throws ServiceFailureException {
		PersonManager personMan = appCtx.getBean("personManager", PersonManager.class);
		ContactManager contactMan = appCtx.getBean("contactManager", ContactManager.class);

		people.clear();

		List<Person> allpeople = personMan.findAllPersons();
		for(Person p : allpeople) {
			PersonContainer pc = new PersonContainer(p);
			List<Contact> contacts = contactMan.findContactsByPerson(p);
			for(Contact c : contacts) {
				pc.addContact(c);
			}
			people.add(pc);
		}

		fireTableDataChanged();

	}

	public void removePerson(int index) throws ServiceFailureException {
		if(index < 0 || index >= people.size())
			throw new IndexOutOfBoundsException("index expected between 0 and " + people.size());

		Person p = people.get(index).getPerson();

		PersonManager man = appCtx.getBean("personManager", PersonManager.class);
		man.deletePerson(p);

		//simulate changes
		people.remove(index);
		fireTableRowsDeleted(index, index);
	}

	public void duplicatePerson(int index) throws ServiceFailureException {
		if(index < 0 || index >= people.size())
			throw new IndexOutOfBoundsException("index expected between 0 and " + people.size());

		Person p  = people.get(index).getPerson();

		PersonManager personMan = appCtx.getBean("personManager", PersonManager.class);
		ContactManager contactMan = appCtx.getBean("contactManager", ContactManager.class);

		//duplicate on datastore
		List<Contact> contacts = contactMan.findContactsByPerson(p);
		p.setId(null);
		p = personMan.createPerson(p);

		PersonContainer newPerson = new PersonContainer(p);
		for(Contact c : contacts) {
			c.setId(null);
			c = contactMan.createContact(c, p);
			newPerson.addContact(c);
		}

		//simulate changes
		people.add(newPerson);

		fireTableDataChanged();
	}
	
	public Person getPersonAt(int index) {
		if (index < 0 || index >= people.size())
			throw new IndexOutOfBoundsException("index expected between 0 and " + people.size());
		
		return people.get(index).getPerson();
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

	//Filter
	public static class ContainsFilter extends RowFilter<TableModel, Integer> {

		private String contains;
		DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
		
		public ContainsFilter(String contains) {
			this.contains = contains.toLowerCase();
		}

		@Override
		public boolean include(
				javax.swing.RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
			TableModel model = entry.getModel();
			int index = entry.getIdentifier().intValue();
			
			String name = (String) model.getValueAt(index, 0);
			if(name!=null && name.toLowerCase().contains(contains))
				return true;
			
			Contact c = (Contact) model.getValueAt(index, 1);
			if(c.getValue()!=null && c.getValue().toLowerCase().contains(contains))
				return true;
			
			c = (Contact) model.getValueAt(index, 2);
			if(c.getValue()!=null && c.getValue().toLowerCase().contains(contains))
				return true;
			
			c = (Contact) model.getValueAt(index, 3);
			if(c.getValue()!=null && c.getValue().toLowerCase().contains(contains))
				return true;
			
			c = (Contact) model.getValueAt(index, 5);
			if(c.getValue()!=null && c.getValue().toLowerCase().contains(contains))
				return true;
			
			Calendar cal = (Calendar) model.getValueAt(index, 4);
			if(cal!=null && format.format(cal.getTime()).contains(contains))
				return true;
			
			Collection<Contact> collection = (Collection<Contact>) model.getValueAt(index, 6);
			if(collection!=null) {
				for(Contact contact : collection) {
					if(contact.getValue().toLowerCase().contains(contains) ||
							contact.getType().toLowerCase().contains(contains))
						return true;
				}
			}
			
			return false;
			
		}
		
	}




}

