package cz.muni.fi.coffei.addressbook.gui;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.text.Collator;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.DefaultRowSorter;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.muni.fi.pv168.Contact;
import cz.muni.fi.pv168.ServiceFailureException;

public class MainWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6422025473656342443L;

	private static final Logger log = LoggerFactory.getLogger(MainWindow.class);
	
	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("cz.muni.fi.coffei.addressbook.gui.Windows"); //$NON-NLS-1


	// Actions
	private final Action quitAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			MainWindow.this.processWindowEvent(new WindowEvent(MainWindow.this,
					WindowEvent.WINDOW_CLOSING));
		}
	};
	private final Action newAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			//TODO: implement
		}
	};

	private final Action editAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Implement

		}

	};

	private final Action duplicateAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			int index = tablePeople.convertRowIndexToModel(tablePeople.getSelectedRow());
			if(index >=0) {
				(new DuplicateWorker(index)).execute();
			}
		}
	};
	private final Action removeAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			int index = tablePeople.convertRowIndexToModel(tablePeople.getSelectedRow());
			if(index>=0) {
				//(new DeleteWorker(index)).execute();
			}
			
		}
	};
	
	
	// Components

	private JButton removeButton;
	private JButton editBtn;
	private JButton addBtn;
	private JMenu groupsMenu;
	private JMenuItem manageGroupsItem;
	private JMenuItem addGroupItem;
	private JPanel contentPane;
	private JPanel loadPanel;
	private JLabel loadText;
	private JTable tablePeople;
	private JPopupMenu rightClickMenu;
	private PersonTableModel tableModel;

	private JMenuBar menuBar;
	

	/**
	 * Launch the application.
	 * To run this program with splash, use java -splash:{path to image} MainWindow
	 */
	public static void main(String[] args) {

		// Select Nimbus LnF
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

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow frame = new MainWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws ServiceFailureException 
	 */
	public MainWindow() throws ServiceFailureException {
		setTitle(BUNDLE.getString("MainWindow.title")); //$NON-NLS-1$
		setMinimumSize(new Dimension(405, 220));
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 720, 500);

		rightClickMenu = new JPopupMenu();
		JMenuItem duplicateItem = new JMenuItem(duplicateAction);
		JMenuItem removeItem = new JMenuItem(removeAction);
		JMenuItem addNewItem = new JMenuItem(newAction);
		JMenuItem editItem = new JMenuItem(editAction);
		editItem.setText(BUNDLE.getString("MainWindow.update"));
		addNewItem.setText(BUNDLE.getString("MainWindow.new"));
		duplicateItem.setText(BUNDLE.getString("MainWindow.duplicate"));
		removeItem.setText(BUNDLE.getString("MainWindow.remove"));
		rightClickMenu.add(addNewItem);
		rightClickMenu.add(editItem);
		rightClickMenu.add(removeItem);
		rightClickMenu.add(duplicateItem);

		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu(
				BUNDLE.getString("MainWindow.mnZkladn.text")); //$NON-NLS-1$
		mnNewMenu.setMnemonic('b');
		menuBar.add(mnNewMenu);

		JMenuItem quitMenuItem = new JMenuItem(quitAction); //$NON-NLS-1$
		quitMenuItem.setText(BUNDLE.getString("MainWindow.mntmQuit.text"));
		quitMenuItem.setMnemonic('q');
		mnNewMenu.add(quitMenuItem);
		
		groupsMenu = new JMenu(BUNDLE.getString("MainWindow.groupsMenuItem")); //$NON-NLS-1$
		menuBar.add(groupsMenu);
		
		manageGroupsItem = new JMenuItem(BUNDLE.getString("MainWindow.manageGroups")); //$NON-NLS-1$
		manageGroupsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				manageGroupsAction(e);
			}
		});
		groupsMenu.add(manageGroupsItem);
		
		addGroupItem = new JMenuItem(BUNDLE.getString("MainWindow.addNewGroup")); //$NON-NLS-1$
		addGroupItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addGroupPerformed(e);
			}
		});
		groupsMenu.add(addGroupItem);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		

		JScrollPane scrollPane = new JScrollPane();
		
		removeButton = new JButton(removeAction);
		removeButton.setEnabled(false);
		removeButton.setText(BUNDLE.getString("MainWindow.removePeople"));

		addBtn = new JButton(newAction);
		addBtn.setText(BUNDLE.getString("MainWindow.addperson"));

		editBtn = new JButton(editAction);
		editBtn.setEnabled(false);
		editBtn.setText(BUNDLE.getString("MainWindow.editperson"));
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_contentPane
						.createParallelGroup(Alignment.TRAILING)
						.addGroup(
								gl_contentPane
										.createSequentialGroup()
										.addComponent(scrollPane,
												GroupLayout.DEFAULT_SIZE, 706,
												Short.MAX_VALUE).addGap(0))
						.addGroup(
								gl_contentPane.createSequentialGroup()
										.addContainerGap()
										.addComponent(editBtn).addGap(10)
										.addComponent(removeButton).addGap(10)
										.addComponent(addBtn).addGap(16))));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_contentPane
						.createSequentialGroup()
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
								400, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(
								gl_contentPane
										.createParallelGroup(Alignment.LEADING)
										.addComponent(editBtn,
												GroupLayout.PREFERRED_SIZE, 29,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(removeButton,
												GroupLayout.PREFERRED_SIZE, 29,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(addBtn,
												GroupLayout.PREFERRED_SIZE, 29,
												GroupLayout.PREFERRED_SIZE))
						.addGap(5)));
		
		scrollPane.setBorder(null);
		tablePeople = new JTable();
		tablePeople.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tablePeople.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				tablePeopleMouseClicked(e);
			}
		});
		
		
		tablePeople.setDefaultRenderer(Contact.class, new ContactCellRenderer());
		tablePeople.setDefaultRenderer(Collection.class, new CollectionContactCellRenderer());
		tablePeople.setDefaultRenderer(Calendar.class, new CalendarCellRenderer());
		tablePeople.setAutoCreateRowSorter(true);
		tablePeople.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						tablePeopleSelectionChanged(e);
					}
				});
		scrollPane.setViewportView(tablePeople);
		contentPane.setLayout(gl_contentPane);
		
		loadPanel = new JPanel();
		loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.Y_AXIS));
		{
			loadText = new JLabel(BUNDLE.getString("MainWindow.mainLoadText"));
			loadText.setBorder(new EmptyBorder(100, 0, 10, 0));
			loadText.setAlignmentX(Component.CENTER_ALIGNMENT);
			loadPanel.add(loadText);
		}
		{
			JProgressBar loadProgress = new JProgressBar();
			loadProgress.setIndeterminate(true);
			loadPanel.add(loadProgress);
		}
		
		
		initialize(true);
		
		

	}
	
	/**
	 * Loads content into the frame
	 * To run this program with splash, use java -splash:{path to image} MainWindow
	 * @param blockThread whether it can block the calling thread- use with splash only
	 */
	
	private void initialize(boolean blockThread) {
		if(blockThread) {
			try {
				tableModel = new PersonTableModel();
				tablePeople.setModel(tableModel);
				setContentPane(contentPane);
			} catch (ServiceFailureException e) {
				log.error("data store exception", e);
				ExceptionDialogs.notifyOfException(e, true, this);
			}
		} else {
			(new LoadingWorker()).execute();
		}
		
	}

	

	protected void addGroupPerformed(ActionEvent e) {
		GroupForm form = new GroupForm(null, this);
		form.setModalityType(ModalityType.APPLICATION_MODAL);
		form.setVisible(true);
	}

	protected void manageGroupsAction(ActionEvent e) {
		GroupsDialog groupsDialog = new GroupsDialog(this);
		groupsDialog.setModalityType(ModalityType.APPLICATION_MODAL);
		groupsDialog.setVisible(true);
		
	}

	protected void tablePeopleSelectionChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			if (((ListSelectionModel) e.getSource()).isSelectionEmpty()) {
				removeButton.setEnabled(false);
				editBtn.setEnabled(false);
			} else {
				removeButton.setEnabled(true);
				editBtn.setEnabled(true);
			}
		}
	}

	
	protected void tablePeopleMouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3
				&& tablePeople.getSelectedRow() >= 0) {
			rightClickMenu.show(tablePeople, e.getX(), e.getY());
		}

	}
	
	private Comparator<Contact> contactComparator = new Comparator<Contact>() {
		
		@Override
		public int compare(Contact o1, Contact o2) {
			return Collator.getInstance().compare(o1.getValue(), o2.getValue());
		}
	};
	
	
	private class LoadingWorker extends SwingWorker<PersonTableModel, Void> {
		
		
		public LoadingWorker() {
			MainWindow.this.setContentPane(loadPanel);
			loadText.setText(BUNDLE.getString("MainWindow.mainLoadText"));
			menuBar.setVisible(false);
			//MainWindow.this.pack();
		}
		
		@Override
		protected void done() {
			try {
				tableModel = get();
				tablePeople.setModel(tableModel);
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
				sorter.setComparator(0, Collator.getInstance());
				sorter.setComparator(1, contactComparator);
				sorter.setComparator(2, contactComparator);
				sorter.setComparator(3, contactComparator);
				sorter.setComparator(5, contactComparator);
				tablePeople.setRowSorter(sorter);
				
			} catch (ExecutionException e) {
				if(e.getCause() instanceof ServiceFailureException) {
					log.error("datastore error", e.getCause());
					ExceptionDialogs.notifyOfException((Exception)e.getCause(), true, MainWindow.this);
				} else {
					log.error("some exception during group loading", e.getCause());
					ExceptionDialogs.notifyOfException(e.getCause() instanceof Exception? (Exception)e.getCause() : e, true, MainWindow.this);
				}
			} catch (InterruptedException e) {
				log.error("LoadingWorker interrupted",e);
				ExceptionDialogs.notifyOfException(e, true, MainWindow.this);
			} 
			
			menuBar.setVisible(true);
			MainWindow.this.setContentPane(contentPane);
			//MainWindow.this.pack();
		}

		@Override
		protected PersonTableModel doInBackground() throws Exception {
			if(tableModel==null)
				return new PersonTableModel();
			else {
				tableModel.reloadAllPeople();
				return tableModel;
			}
		}
		
	}
	
	private class DeleteWorker extends SwingWorker<Void, Void> {

		private int index;
		
		public DeleteWorker(int index) {
			if(index<0)
				throw new IndexOutOfBoundsException("expected positive index");
			this.index = index;
			
			MainWindow.this.setContentPane(loadPanel);
			loadText.setText(BUNDLE.getString("MainWindow.deleteText"));
			menuBar.setVisible(false);
			//MainWindow.this.pack();
			
		}
		
	
		@Override
		protected void done() {
			try {
				get();
			} catch (ExecutionException e) {
				if(e.getCause() instanceof ServiceFailureException) {
					log.error("datastore error", e.getCause());
					ExceptionDialogs.notifyOfException((Exception)e.getCause(), false, MainWindow.this);
				} else {
					log.error("some exception during person deletion", e.getCause());
					ExceptionDialogs.notifyOfException(e.getCause() instanceof Exception? (Exception)e.getCause() : e, false, MainWindow.this);
				}
			} catch (InterruptedException e) {
				//shouldn't happen
				log.error("interrupted error, should never happen!", e);
				ExceptionDialogs.notifyOfException(e, false, MainWindow.this);
			} 
			
			
			MainWindow.this.setContentPane(contentPane);
			menuBar.setVisible(true);
			//MainWindow.this.pack();
			
		}




		@Override
		protected Void doInBackground() throws Exception {
			tableModel.removePerson(index);
			return null;
		}
		
		
	}
	
	private class DuplicateWorker extends SwingWorker<Void, Void> {
		private int index;
		
		public DuplicateWorker(int index) {
			if(index<0)
				throw new IndexOutOfBoundsException("index expected positive");
			
			this.index = index;
			
			MainWindow.this.setContentPane(loadPanel);
			loadText.setText(BUNDLE.getString("MainWindow.duplicateText"));
			menuBar.setVisible(false);
			//MainWindow.this.pack();
		}
		
		@Override
		protected void done() {
			try {
				get();
			} catch (ExecutionException e) {
				if(e.getCause() instanceof ServiceFailureException) {
					log.error("datastore error", e.getCause());
					ExceptionDialogs.notifyOfException((Exception)e.getCause(), false, MainWindow.this);
				} else {
					log.error("some exception during person duplication", e.getCause());
					ExceptionDialogs.notifyOfException(e.getCause() instanceof Exception? (Exception)e.getCause() : e, false, MainWindow.this);
				}
			} catch (InterruptedException e) {
				//shouldn't happen
				log.error("interrupted error, should never happen!", e);
				ExceptionDialogs.notifyOfException(e, false, MainWindow.this);
			} 
			
			MainWindow.this.setContentPane(contentPane);
			menuBar.setVisible(true);
			//MainWindow.this.pack();
		}



		@Override
		protected Void doInBackground() throws Exception {
			tableModel.duplicatePerson(index);
			return null;
		}
		
	}
}
