package cz.muni.fi.coffei.addressbook.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import cz.muni.fi.pv168.DBUtils;
import cz.muni.fi.pv168.Group;
import cz.muni.fi.pv168.GroupManager;
import cz.muni.fi.pv168.Person;
import cz.muni.fi.pv168.ServiceFailureException;


/**
 * JDialog for managing Groups.
 * @author Coffei
 *
 */
public class GroupsDialog extends JDialog {


	/**
	 * generated serial ID
	 */
	private static final long serialVersionUID = -889704213508067385L;

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("cz.muni.fi.coffei.addressbook.gui.Windows"); //$NON-NLS-1

	private static final Logger log = LoggerFactory.getLogger(GroupsDialog.class);

	private ApplicationContext appCtx = null; //lazily initialized when first needed

	// Actions
	
	private final Action addAction = new AbstractAction() {
		private static final long serialVersionUID = -6943814658690984236L;

		@Override
		public void actionPerformed(ActionEvent e) {
			GroupForm form = new GroupForm(null, GroupsDialog.this);
			form.setModalityType(ModalityType.APPLICATION_MODAL);
			form.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					dialogClosing(e);
				}
			});
			form.setVisible(true);
		}
	};

	private final Action removeAction = new AbstractAction() {
		private static final long serialVersionUID = -5054199333938603922L;

		@Override
		public void actionPerformed(ActionEvent e) {
			int index = list.getSelectedIndex(); 
			if(index >= 0) {
				ListModel<Group> model = list.getModel();
				Group toDelete = model.getElementAt(index);
				(new DeleteWorker(toDelete)).execute();
			}
		}
	};

	private final Action editAction = new AbstractAction() {
		private static final long serialVersionUID = -1002941693449736906L;

		@Override
		public void actionPerformed(ActionEvent e) {
			int index = list.getSelectedIndex();
			if(index >= 0) {
				Group group = list.getModel().getElementAt(index);
				GroupForm form = new GroupForm(group, GroupsDialog.this);
				form.setModalityType(ModalityType.APPLICATION_MODAL);
				form.addWindowListener(new WindowAdapter() {
					public void windowClosed(WindowEvent e) {
						dialogClosing(e);
					}
				});
				form.setVisible(true);
			}
		}
	};

	private final Action duplicateAction = new AbstractAction() {
		static final long serialVersionUID = -3002493134045025579L;

		@Override
		public void actionPerformed(ActionEvent e) {
			int index = list.getSelectedIndex();
			if(index >= 0) {
				Group group = list.getModel().getElementAt(index);
				(new DuplicateWorker(group)).execute();
			}
		}
	};

	//Components
	private final JPanel contentPanel = new JPanel();
	private final JPopupMenu rightClickMenu;
	private final JList<Group> list;
	private JPanel loadPanel;
	private JScrollPane listScrollPane;
	private JLabel loadText;
	private JPanel buttonPane;

	/**
	 * Launch the application. Later will be deleted
	 */
	@Deprecated
	public static void main(String[] args) {
		//Set Nimbus LnF
		for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			if (info.getName().toLowerCase().contains("nimbus")) {
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
			GroupsDialog dialog = new GroupsDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor, creates a dialog with no parent.
	 * @see JDialog.setModalityType
	 */
	public GroupsDialog() {
		this(null);

	}

	/**
	 * Constructor, creates a dialog with specified parent, can be null.
	 * @param parent parent of this dialog
	 * @see JDialog.setModalityType
	 */
	public GroupsDialog(Window parent) {
		super(parent);

		rightClickMenu = new JPopupMenu();
		JMenuItem addItem = new JMenuItem(addAction);
		addItem.setText(BUNDLE.getString("GroupsDialog.addGroup"));
		JMenuItem removeItem = new JMenuItem(removeAction);
		removeItem.setText(BUNDLE.getString("GroupsDialog.removeGroup"));
		JMenuItem editItem = new JMenuItem(editAction);
		editItem.setText(BUNDLE.getString("GroupsDialog.editGroup"));
		JMenuItem duplicateItem = new JMenuItem(duplicateAction);
		duplicateItem.setText(BUNDLE.getString("GroupsDialog.duplicateGroup"));

		rightClickMenu.add(addItem);
		rightClickMenu.add(editItem);
		rightClickMenu.add(removeItem);
		rightClickMenu.add(duplicateItem);



		setBounds(100, 100, 350, 300);
		setMinimumSize(new Dimension(350, 150));
		setTitle(BUNDLE.getString("GroupsDialog.title"));
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5,5,5,5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			listScrollPane = new JScrollPane();
			listScrollPane.setVisible(false);
			listScrollPane.setBorder(null);
			contentPanel.add(listScrollPane);
			{
				list = new JList<Group>();
				list.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						listCLicked(e);
					}
				});
				list.setCellRenderer(GROUP_CELL_RENDERER);

				listScrollPane.setViewportView(list);
			}
		}
		{
			loadPanel = new JPanel();
			contentPanel.add(loadPanel);
			loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.Y_AXIS));
			{
				loadText = new JLabel(BUNDLE.getString("GroupsDialog.loadText"));
				loadText.setBorder(new EmptyBorder(30, 0, 5, 0));
				loadText.setAlignmentX(Component.CENTER_ALIGNMENT);
				loadPanel.add(loadText);
			}
			{
				JProgressBar loadProgress = new JProgressBar();
				loadProgress.setIndeterminate(true);
				loadPanel.add(loadProgress);
			}
		}
		{
			buttonPane = new JPanel();
			buttonPane.setVisible(false);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton removeButton = new JButton(removeAction);
				removeButton.setText(BUNDLE.getString("GroupsDialog.removeGroup")); //$NON-NLS-1$
				buttonPane.add(removeButton);
			}
			{
				JButton addButton = new JButton(addAction);
				addButton.setText(BUNDLE.getString("GroupsDialog.addGroup")); //$NON-NLS-1$
				buttonPane.add(addButton);
			}
			{
				JButton cancelButton = new JButton(BUNDLE.getString("GroupsDialog.close")); //$NON-NLS-1$
				cancelButton.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						GroupsDialog.this.setVisible(false);
						GroupsDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

		(new LoadingWorker()).execute();
	}

	
	/// EVENTS 

	protected void listCLicked(MouseEvent e) {
		if(e.getClickCount()==2 && e.getButton() == MouseEvent.BUTTON1 && list.getSelectedIndex() >= 0) {
			editAction.actionPerformed(new ActionEvent(e.getSource(), e.getID()+1, ""));
		}
		if(e.getButton() == MouseEvent.BUTTON3 && list.getSelectedIndex() >= 0) {
			rightClickMenu.show(this, e.getX(), e.getY());
			e.consume();
		}

	}

	private void dialogClosing(WindowEvent e) {
		(new LoadingWorker()).execute();
	}

	
	/**
	 * Loads groups from DB and stores them into a ListModel
	 * @return ListModel ready to be assigned to a JList
	 * @throws ServiceFailureException when DB operation fails
	 */
	private ListModel<Group> loadGroups() throws ServiceFailureException {
		if(appCtx==null)
			appCtx = DBUtils.getAppContext();
		GroupManager man = appCtx.getBean("groupManager", GroupManager.class);
		DefaultListModel<Group> model = new DefaultListModel<>();

		List<Group> groups = man.findAllGroups();
		Collections.sort(groups, new Comparator<Group>() {
			@Override
			public int compare(Group o1, Group o2) {
				Collator col = Collator.getInstance();
				if(o1.getName() != null && o2.getName()!=null)
					return col.compare(o1.getName(), o2.getName());
				else 
					return o1.getName()==null? -1 : 1;
			}

		});

		for(Group g : groups) {
			model.addElement(g);
		}


		return model;
	}

	/**
	 * Simple ListCellRenderer for groups.
	 */
	private static ListCellRenderer<Object> GROUP_CELL_RENDERER = new DefaultListCellRenderer() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9196768592503619410L;

		@Override
		public Component getListCellRendererComponent(JList<?> list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);
			if(value instanceof Group) {
				setText(((Group)value).getName());
			}

			return this;
		}

	};

	/**
	 * SwingWorker for loading content.
	 * @author Coffei
	 */
	private final class LoadingWorker extends SwingWorker<ListModel<Group>, Void> {

		public LoadingWorker() {// on start
			loadText.setText(BUNDLE.getString("GroupsDialog.loadText"));
			loadPanel.setVisible(true);
			buttonPane.setVisible(false);
			listScrollPane.setVisible(false);
		}

		@Override
		protected void done() {
			try {
				ListModel<Group> model = get();
				list.setModel(model);
			} catch (ExecutionException e) {
				if(e.getCause() instanceof ServiceFailureException) {
					log.error("datastore error", e.getCause());
					ExceptionDialogs.notifyOfException((Exception)e.getCause(), true, GroupsDialog.this);
				} else {
					log.error("some exception during group loading", e.getCause());
					ExceptionDialogs.notifyOfException(e.getCause() instanceof Exception? (Exception)e.getCause() : e, true, GroupsDialog.this);
				}
			} catch (InterruptedException e) {
				log.error("interrupted error, should never happen!", e);
				ExceptionDialogs.notifyOfException(e, true, GroupsDialog.this);
			} 


			loadPanel.setVisible(false);
			buttonPane.setVisible(true);
			listScrollPane.setVisible(true);

		}

		@Override
		protected ListModel<Group> doInBackground() throws Exception {
			return loadGroups();
		}

	}

	
	/**
	 * SwingWorker for deleting content.
	 * @author Coffei
	 */
	private final class DeleteWorker extends SwingWorker<Void,Void> {
		private Group toDelete;

		public DeleteWorker(Group target) {
			this.toDelete = target;

			loadText.setText(BUNDLE.getString("GroupsDialog.removeText"));
			loadPanel.setVisible(true);
			buttonPane.setVisible(false);
			listScrollPane.setVisible(false);
		}

		@Override
		protected void done() {
			try {
				get();

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						int index = list.getSelectedIndex();
						if(index >= 0) {
							DefaultListModel<Group> model = ((DefaultListModel<Group>) list.getModel());
							model.remove(index); 
						}
					}
				});

			} catch (ExecutionException e) {
				if(e.getCause() instanceof ServiceFailureException) {
					log.error("datastore error", e.getCause());
					ExceptionDialogs.notifyOfException((Exception)e.getCause(), false, GroupsDialog.this);
				} else {
					log.error("some exception during group loading", e.getCause());
					ExceptionDialogs.notifyOfException(e.getCause() instanceof Exception? (Exception)e.getCause() : e, false, GroupsDialog.this);
				}
			} catch (InterruptedException e) {
				//shouldn't happen
				log.error("interrupted error, should never happen!", e);
				ExceptionDialogs.notifyOfException(e, false, GroupsDialog.this);
			} 


			loadPanel.setVisible(false);
			buttonPane.setVisible(true);
			listScrollPane.setVisible(true);
		}

		@Override
		protected Void doInBackground() throws Exception {
			if(appCtx == null)//should not occur
				appCtx = DBUtils.getAppContext();

			GroupManager man = appCtx.getBean("groupManager", GroupManager.class);
			if(toDelete!=null) {
				man.deleteGroup(toDelete);
			}

			return null;
		}

	}

	
	/**
	 * SwingWorker for duplicating content.
	 * @author Coffei
	 */
	private final class DuplicateWorker extends SwingWorker<Group, Void> {

		private Group toDuplicate;

		public DuplicateWorker(Group target) {
			this.toDuplicate = target;

			loadText.setText(BUNDLE.getString("GroupsDialog.duplicateText"));
			loadPanel.setVisible(true);
			buttonPane.setVisible(false);
			listScrollPane.setVisible(false);
		}

		@Override
		protected void done() {

			try {
				Group group = get();
				((DefaultListModel<Group>)list.getModel()).addElement(group);

			} catch (ExecutionException e) {
				if(e.getCause() instanceof ServiceFailureException) {
					log.error("datastore error", e.getCause());
					ExceptionDialogs.notifyOfException((Exception)e.getCause(), false, GroupsDialog.this);
				} else {
					log.error("some exception during group loading", e.getCause());
					ExceptionDialogs.notifyOfException(e.getCause() instanceof Exception? (Exception)e.getCause() : e, false, GroupsDialog.this);
				}
			} catch (InterruptedException e) {
				//shouldn't happen
				log.error("interrupted error, should never happen!", e);
				ExceptionDialogs.notifyOfException(e, false, GroupsDialog.this);
			} 


			loadPanel.setVisible(false);
			buttonPane.setVisible(true);
			listScrollPane.setVisible(true);

		}

		@Override
		protected Group doInBackground() throws Exception {
			if(appCtx == null)//should not occur
				appCtx = DBUtils.getAppContext();

			GroupManager man = appCtx.getBean("groupManager", GroupManager.class);

			Group newGroup = new Group(toDuplicate);
			man.createGroup(newGroup);

			for(Person p : man.findAllPersonsInGroup(toDuplicate)) {
				man.addPersonToGroup(p, newGroup);
			}

			return newGroup;
		}

	}

}
