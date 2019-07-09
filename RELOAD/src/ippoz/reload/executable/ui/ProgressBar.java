/**
 * 
 */
package ippoz.reload.executable.ui;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

/**
 * @author Tommy
 *
 */
@SuppressWarnings("serial")
public class ProgressBar extends JProgressBar {
	
	private JDialog dlg;

	public ProgressBar(JFrame frame, String tag, int from, int to) {
		super(from, to);
		dlg = new JDialog(frame, tag, true);
		dlg.add(BorderLayout.CENTER, this);
	    dlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	    dlg.setSize(300, 120);
	    dlg.setResizable(false);
	    dlg.setLocationRelativeTo(frame);
	}
	
	public void deleteFrame() {
		dlg.setVisible(false);
	}

	public void moveNext(){
		setValue(getValue()+1);
	}
	
	public void moveN(int n){
		setValue(getValue()+n);
	}
	
	public void showBar(){
		dlg.setVisible(true);
		setVisible(true);
	}	
	
}
