package seedfinder.gui;

import javax.swing.UIManager;

public class GuiMain {

	private static MainWindow window;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel");
		} catch (Exception e) {
			System.err.println("Unable to set look and feel");
		}

		window = new MainWindow();
	}

}
