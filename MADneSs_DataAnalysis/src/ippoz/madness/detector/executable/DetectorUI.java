package ippoz.madness.detector.executable;

import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.PreferencesManager;
import ippoz.madness.detector.executable.ui.BuildUI;
import ippoz.madness.detector.manager.InputManager;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class DetectorUI {
	
	private static final String DEFAULT_PREF_FILE = "madness.preferences";

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		final InputManager iManager;
		try {
			try {
			    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
			        if ("Nimbus".equals(info.getName())) {
			            UIManager.setLookAndFeel(info.getClassName());
			            break;
			        }
			    }
			} catch (Exception e) {
			    // If Nimbus is not available, you can set the GUI to another look and feel.
			}
			if(new File(DEFAULT_PREF_FILE).exists()){
				iManager = new InputManager(new PreferencesManager(DEFAULT_PREF_FILE));
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							DetectorUI window = new DetectorUI(iManager);
							window.getFrame().setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			}
		} catch(Exception ex) {
			AppLogger.logException(DetectorMain.class, ex, "");
		}
	}

	/**
	 * Create the application.
	 */
	public DetectorUI(InputManager iManager) {
		BuildUI bui = new BuildUI(iManager);
		frame = bui.buildJFrame();
	}
	
	public JFrame getFrame() {
		return frame;
	}
}
