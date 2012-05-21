package cz.muni.fi.coffei.addressbook.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import cz.muni.fi.pv168.DBUtils;
import cz.muni.fi.pv168.Group;
import cz.muni.fi.pv168.GroupManager;
import cz.muni.fi.pv168.Person;
import cz.muni.fi.pv168.PersonManager;
import cz.muni.fi.pv168.ServiceFailureException;


/**
 * JDialog for creating and editing Groups
 * @author Coffei
 *
 */
public class GroupForm extends JDialog {
	
	/**
	 * generated serialID
	 */
	private static final long serialVersionUID = -236097638646306386L;

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("cz.muni.fi.coffei.addressbook.gui.Windows"); //$NON-NLS-1$

	private static final Logger log = LoggerFactory.getLogger(GroupForm.class);

	
	
	private final JPanel contentPanel = new JPanel();
	private JButton okButton;
	private JTextField nameText;
	private JList<BoolWrapper<Person>> list;
	private JLabel loadText;
	private JPanel buttonPanel;
	private JPanel loadPanel;

	private Group group;
	private List<Person> assignedPeople = Collections.emptyList();
	
	
	private ApplicationContext appCtx = null;


	/**
	 * Launch the application. Later to be removed
	 * @throws ServiceFailureException 
	 */
	@Deprecated
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
			GroupForm dialog = new GroupForm(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	

	/**
	 * Create a dialog.
	 * @param group group to be modified or null if creating new
	 * @param parent parent window of this dialog if set this window will be centered accordingly, can be null
	 * @see JDialog.setModalityType
	 */
	public GroupForm(Group group, Window parent) {
		super(parent);
		this.group = group;
		

		setTitle(BUNDLE.getString("GroupForm.title"));

		setBounds(100, 100, 500, 340);
		setMinimumSize(new Dimension(200, 200));
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, BUNDLE.getString("GroupForm.basics"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, BUNDLE.getString("GroupForm.members"), TitledBorder.LEADING, TitledBorder.TOP, null, null));
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

		list = new JList<BoolWrapper<Person>>();
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		//list.setBackground(UIManager.getColor("List.background"));
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(CHECKBOX_LIST_RENDERER);
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
				JLabel lblName = new JLabel(BUNDLE.getString("GroupForm.name"));
				panel_2.add(lblName, BorderLayout.WEST);
			}


			nameText = new JTextField();
			nameText.setBorder(new EmptyBorder(0, 5, 0, 0));
			nameText.setPreferredSize(new Dimension(12, 12));
			nameText.setInputVerifier(new InputVerifier() {
				public boolean verify(JComponent input) { //put all verification-dependent code here
					String text = ((JTextComponent)input).getText();
					if(text==null || text.isEmpty() || text.length() > 255) {
						input.setBorder(BorderFactory.createLineBorder(Color.red, 1));
						okButton.setEnabled(false);
						return false;
					} else {
						input.setBorder(null);
						okButton.setEnabled(true);
						return true;
					}
					
				}
			});
			if(group!=null)
				nameText.setText(group.getName());
			panel_2.add(nameText);
		}
		contentPanel.setLayout(gl_contentPanel);
		{
			buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			{
				okButton = new JButton(BUNDLE.getString("GroupForm.save"));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okClicked(e);
					}
				});
				okButton.setActionCommand("OK");
				buttonPanel.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton(BUNDLE.getString("GroupForm.cancel"));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelClicked(e);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPanel.add(cancelButton);
			}
		}
		
		loadPanel = new JPanel();
		getContentPane().add(loadPanel, BorderLayout.NORTH);
		loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.Y_AXIS));
		
		loadText = new JLabel();
		loadText.setBorder(new EmptyBorder(30, 0, 5, 0));
		loadText.setAlignmentX(Component.CENTER_ALIGNMENT);
		loadPanel.add(loadText);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		loadPanel.add(progressBar);
		
		(new LoadingWorker()).execute();
	}
	
	
	/**
	 * Loads people from data source and creates according ListModel.
	 * @param group group to edit, null if creating new
	 * @return ListModel ready to assign to JList
	 * @throws ServiceFailureException 
	 */
	private ListModel<BoolWrapper<Person>> loadPeople() throws ServiceFailureException {
		if(appCtx==null)
			appCtx = DBUtils.getAppContext();

		GroupManager groupman = appCtx.getBean("groupManager", GroupManager.class);
		PersonManager personman = DBUtils.getAppContext().getBean("personManager", PersonManager.class);
		DefaultListModel<BoolWrapper<Person>> model = new DefaultListModel<>();
		if(group!=null) {

			for(Person p : (assignedPeople = groupman.findAllPersonsInGroup(group))) {
				model.addElement(new BoolWrapper<Person>(p, true));
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
		(new SavingWorker()).execute();
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
	 * SwingWorker for saving content
	 * @author Coffei
	 */
	private class SavingWorker extends SwingWorker<Void, Void> {
		
		public SavingWorker() {
			loadText.setText(BUNDLE.getString("GroupForm.saveText"));
			loadPanel.setVisible(true);
			contentPanel.setVisible(false);
			buttonPanel.setVisible(false);
		}

		@Override
		protected void done() {
			try {
				get();
			}  catch (ExecutionException e) {
				if(e.getCause() instanceof ServiceFailureException) {
					log.error("datastore error", e.getCause());
					 ExceptionDialogs.notifyOfException((Exception)e.getCause(), false, GroupForm.this);
				} else {
					log.error("some exception during group loading", e.getCause());
					ExceptionDialogs.notifyOfException(e.getCause() instanceof Exception? (Exception)e.getCause() : e, false, GroupForm.this);
				}
			} catch (InterruptedException e) {
				//shouldn't happen
				log.error("interrupted error, should never happen!", e);
				ExceptionDialogs.notifyOfException(e, false, GroupForm.this);
			} 
			
			GroupForm.this.setVisible(false);
			GroupForm.this.dispose();
		}

		@Override
		protected Void doInBackground() throws Exception {
			if(appCtx==null) 
				appCtx = DBUtils.getAppContext();
			
			
			GroupManager man = appCtx.getBean("groupManager", GroupManager.class);
			if(group==null) {
				group = new Group();
				group.setName(nameText.getText());
				group = man.createGroup(group);
			} else if (!nameText.getText().equals(group.getName())) {
				group.setName(nameText.getText());
				man.updateGroup(group);
			}
			
			ListModel<BoolWrapper<Person>> model = list.getModel();
			for (int i = 0; i < model.getSize(); i++) { //iterate over all BoolWrapers in model
				BoolWrapper<Person> w = model.getElementAt(i);
				if(w.value && !assignedPeople.contains(w.object)) {//add people
					man.addPersonToGroup(w.object, group);
				}
				else if(!w.value && assignedPeople.contains(w.object)) {//remove people
					man.removePersonFromGroup(w.object, group);
				}
			}
			
			return null;
		}
		
		
	}
	
	
	
	/**
	 * SwingWorker for loading content
	 * @author Coffei
	 */
	private class LoadingWorker extends SwingWorker<ListModel<BoolWrapper<Person>>, Void> {
		
		public LoadingWorker() {
			loadText.setText(BUNDLE.getString("GroupForm.loadText"));
			loadPanel.setVisible(true);
			contentPanel.setVisible(false);
			buttonPanel.setVisible(false);
			
		}
		
		@Override
		protected void done() {
			
			try {
				ListModel<BoolWrapper<Person>> model = get();
				list.setModel(model);
			} catch (ExecutionException e) {
				if(e.getCause() instanceof ServiceFailureException) {
					log.error("datastore error", e.getCause());
					ExceptionDialogs.notifyOfException((Exception)e.getCause(), true, GroupForm.this);
				} else {
					log.error("some exception during group loading", e.getCause());
					ExceptionDialogs.notifyOfException(e.getCause() instanceof Exception? (Exception)e.getCause() : e, true, GroupForm.this);
				}
			} catch (InterruptedException e) {
				//shouldn't happen
				log.error("interrupted error, should not happen!", e);
				ExceptionDialogs.notifyOfException(e, true, GroupForm.this);
			} 
			
			
			
			loadPanel.setVisible(false);
			contentPanel.setVisible(true);
			buttonPanel.setVisible(true);
			nameText.grabFocus();
		}

		@Override
		protected ListModel<BoolWrapper<Person>> doInBackground()
				throws Exception {
			
			
			return loadPeople();
			
		}
		
	}
	
	


	
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
