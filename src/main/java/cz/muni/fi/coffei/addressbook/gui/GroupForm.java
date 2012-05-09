package cz.muni.fi.coffei.addressbook.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import cz.muni.fi.pv168.DBUtils;
import cz.muni.fi.pv168.Group;
import cz.muni.fi.pv168.GroupManager;
import cz.muni.fi.pv168.Person;
import cz.muni.fi.pv168.PersonManager;
import cz.muni.fi.pv168.ServiceFailureException;

public class GroupForm extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private DefaultListModel<BoolWrapper<Person>> model;

	private final Group group;
	private List<Person> assignedPeople = Collections.emptyList();


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) { //temporary, for testing only
		for(LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if(info.getName().toLowerCase().contains("nimbus")) {
				try {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				} catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException
						| UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
			}
		}

		try {
			GroupForm dialog = new GroupForm(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a dialog.
	 * @param group group to be modified or null if creating new
	 */
	public GroupForm(Group group) {
		this.group = group;

		setTitle("Group form");

		setBounds(100, 100, 500, 340);
		setMinimumSize(new Dimension(200, 200));
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Basic", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "People", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
				gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addComponent(panel, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
				.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
				);
		gl_contentPanel.setVerticalGroup(
				gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
				);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		panel_1.add(scrollPane, BorderLayout.CENTER);

		JList<BoolWrapper<Person>> list = new JList<BoolWrapper<Person>>();
		list.setBackground(UIManager.getColor("List.background"));
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(CHECKBOX_LIST_RENDERER);
		list.setModel(loadPeople());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				listClicked(e);
			}
		});

		scrollPane.setViewportView(list);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		{
			JPanel panel_2 = new JPanel();
			panel_2.setAlignmentX(Component.LEFT_ALIGNMENT);
			panel.add(panel_2);
			panel_2.setLayout(new BorderLayout(7, 0));
			{
				JLabel lblName = new JLabel("Name");
				panel_2.add(lblName, BorderLayout.WEST);
			}


			JTextPane nameText = new JTextPane();
			nameText.setPreferredSize(new Dimension(12, 12));
			nameText.setInputVerifier(new InputVerifier() {
				public boolean verify(JComponent input) { //put all verification-dependent code here
					String text = ((JTextComponent)input).getText();
					return text!=null && !text.isEmpty() && text.length() < 255;
				}
			});
			panel_2.add(nameText);
		}
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okClicked(e);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelClicked(e);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	/**
	 * Loads people from data source and creates according ListModel.
	 * @param group group to edit, null if creating new
	 * @return ListModel ready to assign to JList
	 */
	private ListModel<BoolWrapper<Person>> loadPeople() {
		GroupManager groupman = DBUtils.getAppContext().getBean("groupManager", GroupManager.class);
		PersonManager personman = DBUtils.getAppContext().getBean("personManager", PersonManager.class);
		model = new DefaultListModel<>();
		if(group!=null) {
			try {
				for(Person p : (assignedPeople = groupman.findAllPersonsInGroup(group))) {
					model.addElement(new BoolWrapper<Person>(p, true));
				}
			} catch (ServiceFailureException e) {
				//TODO: exception handling
			}
		}

		List<Person> allPeople = personman.findAllPersons();
		allPeople.removeAll(assignedPeople);
		for(Person p : allPeople ) {
			model.addElement(new BoolWrapper<Person>(p));
		}

		return model;
	}

	protected void cancelClicked(ActionEvent e) {
		this.setVisible(false);
		this.dispose();

	}

	protected void okClicked(ActionEvent e) {// Find changes and save them
		GroupManager groupman = DBUtils.getAppContext().getBean("groupManager", GroupManager.class);

		//Name
		try {

			for(Person p : assignedPeople) {
				if(!model.contains(new BoolWrapper<Person>(p))) {
					groupman.removePersonFromGroup(p, group);
				}
			}

			for (int i = 0; i < model.getSize(); i++) {
				BoolWrapper<Person> w = model.get(i);
				if(w.value && !assignedPeople.contains(w.object)) {
					groupman.addPersonToGroup(w.object, group);
				}
			}

		} catch (ServiceFailureException sfex) {
			//TODO:Exception handling
		}



	}

	//JList click event, change bool value of particular BoolWraper and repaint to show change
	protected void listClicked(MouseEvent e) {
		JList<BoolWrapper<Person>> source = (JList<BoolWrapper<Person>>)e.getSource();
		int index = source.getSelectedIndex();
		if(index>=0) {
			BoolWrapper<Person> wrapper = ((DefaultListModel<BoolWrapper<Person>>)source.getModel()).get(index);
			wrapper.value ^= true; //negate wrapper value
			source.repaint();
		}
	}

	/**
	 * ListCellRenderer for JList, uses checkboxes
	 */
	private static ListCellRenderer<BoolWrapper<Person>> CHECKBOX_LIST_RENDERER = new ListCellRenderer<BoolWrapper<Person>>() {

		@Override
		public Component getListCellRendererComponent(
				JList<? extends BoolWrapper<Person>> list,
						BoolWrapper<Person> value, int index, boolean isSelected,
						boolean cellHasFocus) {
			JCheckBox check = new JCheckBox();
			if(value !=null) {
				check.setText(value.object.getName());
				check.setSelected(value.getValue());
			}

			return check;
		}
	};



	/**
	 * Class to manage a bond between E object and boolean
	 * @author Coffei
	 *
	 * @param <E> type of the object to be stored in this object
	 */
	private class BoolWrapper<E>{
		private E object;
		private boolean value;

		/**
		 * Constructor
		 * @param object object to be bonded, can not be null
		 * @param value value to bond
		 */
		public BoolWrapper(E object, boolean value) {
			if(object==null)
				throw new NullPointerException("object");
			this.object = object;
			this.value = value;
		}

		/**
		 * Constructor
		 * @param object object to be bonded with false
		 */
		public BoolWrapper(E object) {
			this(object, false);
		}

		public E getObject() {
			return object;
		}
		public boolean getValue() {
			return value;
		}
		public void setValue(boolean value) {
			this.value = value;
		}

		@Override
		public int hashCode() {
			return object.hashCode() * 31;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BoolWrapper<?> other = (BoolWrapper<?>) obj;
			if (object == null) {
				if (other.object != null)
					return false;
			} else if (!object.equals(other.object))
				return false;
			return true;
		}





	}
}
