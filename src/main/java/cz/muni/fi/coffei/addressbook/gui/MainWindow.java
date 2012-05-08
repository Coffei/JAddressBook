package cz.muni.fi.coffei.addressbook.gui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class MainWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6422025473656342443L;

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("cz.muni.fi.coffei.addressbook.gui.MainWindow"); //$NON-NLS-1$

	private JPanel contentPane;
	private JTable tablePeople;
	private JTable tableGroups;
	private JPopupMenu rightClickMenu;

	private JTabbedPane tabbedPane;
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
			// TODO implement
			tablePeople.getSelectionModel().removeSelectionInterval(0,
					tablePeople.getRowCount() - 1);
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
			// TODO Implement
		}
	};
	private final Action removeAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Implement

		}
	};
	private final ButtonGroup buttonGroup = new ButtonGroup();

	private JButton removeButton;

	private JButton editBtn;

	private JButton addBtn;

	/**
	 * Launch the application.
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
	 */
	public MainWindow() {
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
		rightClickMenu.add(duplicateItem);
		rightClickMenu.add(removeItem);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu(
				BUNDLE.getString("MainWindow.mnZkladn.text")); //$NON-NLS-1$
		mnNewMenu.setMnemonic('b');
		menuBar.add(mnNewMenu);

		JMenuItem quitMenuItem = new JMenuItem(quitAction); //$NON-NLS-1$
		quitMenuItem.setText(BUNDLE.getString("MainWindow.mntmQuit.text"));
		quitMenuItem.setMnemonic('q');
		mnNewMenu.add(quitMenuItem);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				tabSelectionChanged(e);
			}
		});
		tabbedPane.setAlignmentX(0.0f);
		tabbedPane.setAlignmentY(0.0f);
		tabbedPane.setBorder(null);

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
										.addComponent(tabbedPane,
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
						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE,
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

		JPanel panel = new JPanel();
		panel.setAlignmentY(0.0f);
		panel.setAutoscrolls(true);
		panel.setAlignmentX(0.0f);
		tabbedPane.addTab(BUNDLE.getString("MainWindow.tabPeople.text"), null,
				panel, null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(null);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(gl_panel.createParallelGroup(
				Alignment.TRAILING).addGroup(
				gl_panel.createSequentialGroup()
						.addGap(0)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
								706, Short.MAX_VALUE).addGap(0)));
		gl_panel.setVerticalGroup(gl_panel.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel.createSequentialGroup()
						.addGap(0)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE,
								370, Short.MAX_VALUE)));

		tablePeople = new JTable();
		tablePeople.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				tablePeopleMouseClicked(e);
			}
		});
		tablePeople.setModel(new DefaultTableModel(new Object[][] {
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null },
				{ null, null, null, null, null, null }, }, new String[] {
				"Name", "Phone", "Address", "Email", "Other...", "Birthday" }));
		panel.setLayout(gl_panel);
		tablePeople.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						tablePeopleSelectionChanged(e);
					}
				});
		scrollPane.setViewportView(tablePeople);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab(BUNDLE.getString("MainWindow.tabGroups.text"), null,
				panel_1, null);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBorder(null);
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(gl_panel_1.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_1
						.createSequentialGroup()
						.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE,
								getBounds().width, Short.MAX_VALUE)));
		gl_panel_1.setVerticalGroup(gl_panel_1.createParallelGroup(
				Alignment.LEADING).addGroup(
				gl_panel_1
						.createSequentialGroup()
						.addComponent(scrollPane_1, GroupLayout.DEFAULT_SIZE,
								430, Short.MAX_VALUE)));

		tableGroups = new JTable();
		tableGroups.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				tableGroupMouseClicked(e);
			}
		});
		tableGroups.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						tableGroupsSelectionChanged(e);
					}
				});
		tableGroups.setModel(new DefaultTableModel(new Object[][] {
				{ null, null }, { null, null }, { null, null }, { null, null },
				{ null, null }, { null, null }, { null, null }, { null, null },
				{ null, null }, { null, null }, { null, null }, { null, null },
				{ null, null }, }, new String[] { "Name", "People" }));
		tableGroups.getColumnModel().getColumn(1).setPreferredWidth(551);
		scrollPane_1.setViewportView(tableGroups);
		panel_1.setLayout(gl_panel_1);
		contentPane.setLayout(gl_contentPane);

	}

	protected void tabSelectionChanged(ChangeEvent e) {
		// TODO:change labels
		if (((JTabbedPane) e.getSource()).getSelectedIndex() == 0) {
			// people tab selected

		} else {
			// group tab selected
		}
	}

	protected void tableGroupsSelectionChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && tabbedPane.getSelectedIndex() == 1) {
			if (((ListSelectionModel) e.getSource()).isSelectionEmpty()) {
				removeButton.setEnabled(false);
				editBtn.setEnabled(false);
			} else {
				removeButton.setEnabled(true);
				editBtn.setEnabled(true);
			}
		}
	}

	protected void tablePeopleSelectionChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && tabbedPane.getSelectedIndex() == 0) {
			if (((ListSelectionModel) e.getSource()).isSelectionEmpty()) {
				removeButton.setEnabled(false);
				editBtn.setEnabled(false);
			}

			else {
				removeButton.setEnabled(true);
				editBtn.setEnabled(true);
			}
		}
	}

	protected void tableGroupMouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3
				&& tableGroups.getSelectedRow() >= 0) {
			rightClickMenu.show(tableGroups, e.getX(), e.getY());
		}
	}

	protected void tablePeopleMouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3
				&& tablePeople.getSelectedRow() >= 0) {
			rightClickMenu.show(tablePeople, e.getX(), e.getY());
		}

	}
}
