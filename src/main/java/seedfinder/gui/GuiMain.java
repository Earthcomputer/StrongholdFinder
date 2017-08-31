package seedfinder.gui;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class GuiMain {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
		} catch (Exception e) {
			System.err.println("Unable to set look and feel");
		}
		JOptionPane.showMessageDialog(null, "Placeholder GUI");
	}

}
