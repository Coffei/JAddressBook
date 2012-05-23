package cz.muni.fi.coffei.addressbook.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.muni.fi.coffei.addressbook.gui.PersonTableModel.ContactType;
import cz.muni.fi.pv168.Contact;
import cz.muni.fi.pv168.Person;

/**
 * For internal use ONLY!
 * @author Coffei
 *
 */
public class PersonContainer extends Person {
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
	
	/**
	 * Converts current instance into Person
	 * @return Person instance
	 */
	public Person getPerson() {
		Person p = new Person();
		p.setId(getId());
		p.setName(getName());
		p.setBorn(getBorn());
		
		return p;
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