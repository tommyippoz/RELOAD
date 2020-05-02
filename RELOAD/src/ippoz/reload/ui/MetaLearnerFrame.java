/**
 * 
 */
package ippoz.reload.ui;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.meta.BaggingMetaLearner;
import ippoz.reload.algorithm.meta.VotingMetaLearner;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.manager.InputManager;
import ippoz.reload.meta.MetaLearnerType;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Tommy
 *
 */
public class MetaLearnerFrame {
	
	private JFrame mlFrame;
	
	private JPanel mlearnerPanel;
	
	private Font labelFont;
	
	private Font labelBoldFont;
	
	private InputManager iManager;
	
	private LearnerType oldLearner;
	
	private LearnerType lType;
	
	private BuildUI item;
	
	public MetaLearnerFrame(InputManager iManager, LearnerType lType, BuildUI item) {
		this.iManager = iManager;
		this.oldLearner = lType.clone();
		this.lType = lType;
		this.item = item;
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		
		labelFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelBoldFont = new Font("Times", Font.BOLD, (int)((15 + rate)/2));
		
		buildFrame();
		
		mlearnerPanel = buildMainPanel();
	}
	
	private void reload() {
		mlFrame.setVisible(false);
		mlFrame.getContentPane().removeAll();
		mlearnerPanel = buildMainPanel();
		setVisible(true);
	}

	public void setVisible(boolean b) {
		if(mlFrame != null){
			mlFrame.add(mlearnerPanel);
			mlFrame.setLocationRelativeTo(null);
			mlFrame.setVisible(b);
		}
	}

	private void buildFrame(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		mlFrame = new JFrame();
		mlFrame.setTitle("Meta-Learning Setup of '" + lType.toString() + "'");
		if(screenSize.getWidth() > 1600)
			mlFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.4), (int)(screenSize.getHeight()*0.6));
		else mlFrame.setBounds(0, 0, 400, 600);
		mlFrame.setBackground(Color.WHITE);
	}
	
	private JPanel buildMainPanel() {	
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setLayout(new BorderLayout());
		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		
		// HEADER
		
		JPanel headerPanel = new JPanel();
		headerPanel.setBackground(Color.WHITE);
		headerPanel.setBorder(new EmptyBorder(20, mlFrame.getWidth()/5, 20, mlFrame.getWidth()/5));
		headerPanel.setLayout(new GridLayout(2, 2, 10, 10));
		
		JLabel lbl = new JLabel("Learner:");
		lbl.setFont(labelBoldFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		headerPanel.add(lbl);
		
		lbl = new JLabel(oldLearner.toString());
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		headerPanel.add(lbl);
		
		lbl = new JLabel("Current Learner:");
		lbl.setFont(labelBoldFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		headerPanel.add(lbl);
		
		lbl = new JLabel(lType.toString());
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		headerPanel.add(lbl);
		
		containerPanel.add(headerPanel, BorderLayout.NORTH);
		
		// MAIN
		
		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setLayout(new GridLayout(MetaLearnerType.values().length, 2, 5, 5));
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Meta-Learning Options", TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		mainPanel.setBorder(tb);

		ButtonGroup bGroup = new ButtonGroup(); 
		for(MetaLearnerType mlt : MetaLearnerType.values()){
			JRadioButton jrb = new JRadioButton(mlt.toString());
			jrb.setFont(labelBoldFont);
			if(lType instanceof MetaLearner && ((MetaLearner)lType).getMetaType().equals(mlt))
				jrb.setSelected(true);
			jrb.setToolTipText(MetaLearner.describe(mlt));
			jrb.addItemListener(new ItemListener() { 
			    @Override
			    public void itemStateChanged(ItemEvent event) {
			        int state = event.getStateChange();
			        if (state == ItemEvent.SELECTED){
			        	if(lType instanceof BaseLearner)
			        		lType = ((BaseLearner)lType).toMeta(mlt);
			        	else ((MetaLearner)lType).changeMetaLearner(mlt);
			        } else {
			        	if(lType instanceof MetaLearner)
			        		lType = ((MetaLearner)lType).toBase();
			        } 
			        reload();
			    }
			});
			bGroup.add(jrb);
			mainPanel.add(jrb);
			mainPanel.add(buildMetaOptions(mlt, jrb.isSelected()));
		}
		
		containerPanel.add(mainPanel, BorderLayout.CENTER);
		
		// FOOTER
        
        JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		fPanel.setLayout(new GridLayout(1, 3, 50, 0));
		fPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
		
		JButton button = new JButton("Save Changes");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				iManager.removeAlgorithm(oldLearner);
				iManager.addAlgorithm(lType.toString());
				mlFrame.setVisible(false);
				item.reload();
			} } );	
		fPanel.add(button);
		
		button = new JButton("Clear Meta");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				bGroup.clearSelection();
			} } );	
		fPanel.add(button);
		
		button = new JButton("Discard Changes");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				mlFrame.setVisible(false);
				item.reload();
			} } );	
		fPanel.add(button);
		
		containerPanel.add(fPanel, BorderLayout.SOUTH);
		
		return containerPanel;
	}
	
	private JPanel buildMetaOptions(MetaLearnerType mlt, boolean enabled) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridLayout(1, 1));
		switch(mlt){
			case BAGGING:
				panel.add(showPreferenceLabels(BaggingMetaLearner.N_SAMPLES, lType.getPreference(BaggingMetaLearner.N_SAMPLES), "number of samples of Bagging meta-learner", enabled));
				break;
			case VOTING:
				panel.add(showPreferenceAlgorithms(VotingMetaLearner.BASE_LEARNERS, lType.getPreference(VotingMetaLearner.BASE_LEARNERS), "algorithms to vote", enabled));
				break;
			default:
				panel.add(new JLabel("-"));
		}
		return panel;
	}

	private Component showPreferenceAlgorithms(String prefName, String textFieldText, String description, boolean enabled) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridLayout(1, 2));
				
		JLabel lbl = new JLabel(prefName);
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		panel.add(lbl);
		
		JLabel textField = new JLabel(textFieldText);
		textField.setFont(labelFont);
		textField.setEnabled(enabled);
		textField.addMouseListener(new MouseAdapter()  
		{  
		    public void mouseClicked(MouseEvent e) { 
		    	String[] algList = new String[DetectionAlgorithm.availableAlgorithms().size()];
				int i = 0;
				for(AlgorithmType at : DetectionAlgorithm.availableAlgorithms()){
					algList[i++] = at.toString();
				}
				
				JPanel gui = new JPanel(new BorderLayout());
				JList<String> possibilities = new JList<String>(algList);
				gui.add(new JScrollPane(possibilities));
                JOptionPane.showMessageDialog(
                        null, 
                        gui,
                        "Choose Algorithm(s)",
                        JOptionPane.QUESTION_MESSAGE);
                List<String> items = possibilities.getSelectedValuesList();
                
                String returnValue = "";
                for (Object item : items) {
                    returnValue = returnValue + item + ",";
                }
                returnValue = returnValue.length() > 0 ? returnValue.substring(0, returnValue.length()-1) : returnValue;
				if (returnValue != null && returnValue.length() > 0) {
					lType.addPreference(prefName, returnValue);
				    reload();
				}
		    }  
		}); 
		
		panel.add(textField);
		
		return panel;
	}

	private JPanel showPreferenceLabels(String prefName, String textFieldText, String description, boolean enabled){
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridLayout(1, 2));
				
		JLabel lbl = new JLabel(prefName);
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		panel.add(lbl);
		
		JTextField textField = new JTextField();
		textField.setFont(labelFont);
		textField.setText(textFieldText);
		textField.setColumns(10);
		textField.setEnabled(enabled);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		textField.getDocument().addDocumentListener(new DocumentListener() {
			  
			public void changedUpdate(DocumentEvent e) {
				workOnUpdate();
			}
			  
			public void removeUpdate(DocumentEvent e) {
				workOnUpdate();
			}
			  
			public void insertUpdate(DocumentEvent e) {
				workOnUpdate();
			}

			public void workOnUpdate() {
	        	lType.addPreference(prefName, textField.getText());
			}
		});
		
		panel.add(textField);
		
		return panel;
	}

}
