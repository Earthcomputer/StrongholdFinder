package seedfinder.gui;

import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MainWindow extends JFrame {

	private static final long serialVersionUID = -4137074698662928841L;

	public MainWindow() {
		setTitle("Seed Finder");

		JMenuBar menuBar = new JMenuBar();
		JMenu menu;
		JMenuItem menuItem;

		menu = new JMenu("Find");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("Search for a seed");
		menuItem = new JMenuItem("Eyes in portal");
		menuItem.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Search for a seed with many eyes in the end portal");
		menuItem.addActionListener(e -> searchForStrongholdSeed());
		menu.add(menuItem);
		menuBar.add(menu);

		setJMenuBar(menuBar);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setVisible(true);
		setMaximizedBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
		setExtendedState(getExtendedState() | MAXIMIZED_BOTH);
	}

	private static void searchForStrongholdSeed() {
		System.out.println("Searching for stronghold seed");
	}

}
