/**
 * 
 */
package ippoz.madness.detector.executable.ui;

import ippoz.madness.detector.commons.algorithm.AlgorithmType;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Tommy
 *
 */
@SuppressWarnings("serial")
public class AddAlgorithmFrame extends JFrame {
	
	private JComboBox<String> comboBox;
	
	public AddAlgorithmFrame(){
		super("Add Algorithm");
		buildFrame();
		buildUI();
	}
	
	private void buildUI() {
		JPanel panel = new JPanel();
		panel.setBounds((int) (getContentPane().getWidth()*0.01), 20, (int) (getContentPane().getWidth()*0.98), 100);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel("Choose Algorithm");
		lbl.setBounds(getContentPane().getWidth()/10, 0, getContentPane().getWidth()*2/5, 20);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		comboBox = new JComboBox<String>();
		comboBox.setBounds(getContentPane().getWidth()/2, 40, getContentPane().getWidth()*2/5, 25);
		for(AlgorithmType at : AlgorithmType.values()){
			comboBox.addItem(at.toString());
		}
		panel.add(comboBox);
		
		getContentPane().add(panel);

	}

	private void buildFrame(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setTitle("Add Algorithm");
		setIconImage(new ImageIcon(getClass().getResource("/RELOAD_Transparent.png")).getImage());
		if(screenSize.getWidth() > 1600)
			setBounds(0, 0, (int)(screenSize.getWidth()*0.2), (int)(screenSize.getHeight()*0.2));
		else setBounds(0, 0, 200, 120);
		getContentPane().setBackground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);
	}

	public String getAlgorithmTag() {
		return comboBox.getSelectedItem().toString();
	}

}
