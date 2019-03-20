/**
 * 
 */
package ippoz.madness.detector.executable.ui;

import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.commons.support.PreferencesManager;
import ippoz.madness.detector.loader.CSVPreLoader;
import ippoz.madness.detector.loader.Loader;
import ippoz.madness.detector.manager.InputManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Tommy
 *
 */
public class LoaderFrame {
	
	private JFrame lFrame;
	
	private Font bigFont;
	
	private Font labelFont;
	
	private Font smallLabelFont;
	
	private int labelSpacing;
	
	private int bigLabelSpacing;
	
	private JPanel loaderPanel;
	
	private InputManager iManager;
	
	private PreferencesManager loaderPref;
	
	public LoaderFrame(InputManager iManager, PreferencesManager loaderPref) {
		this.iManager = iManager;
		this.loaderPref = loaderPref;
		buildFrame();
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		
		bigFont = new Font("Times", Font.PLAIN, (int)((18 + rate)/2));
		labelFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		smallLabelFont = new Font("Times", Font.PLAIN, (int)((14 + rate)/2));
		
		labelSpacing = (int)(lFrame.getHeight()/25);
		bigLabelSpacing = (int)(lFrame.getHeight()/18);
		
		loaderPanel = buildMainPanel();
	}

	public void setVisible(boolean b) {
		if(lFrame != null){
			lFrame.add(loaderPanel);
			lFrame.setLocationRelativeTo(null);
			lFrame.setVisible(b);
		}
	}

	private void buildFrame(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		lFrame = new JFrame();
		lFrame.setTitle("Setup of '" + loaderPref.getFilename() + "'");
		if(screenSize.getWidth() > 1600)
			lFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.4), (int)(screenSize.getHeight()*0.7));
		else lFrame.setBounds(0, 0, 800, 480);
		lFrame.setBackground(Color.WHITE);
		lFrame.setResizable(false);
	}
	
	private JPanel buildMainPanel() {	
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBounds(5, 5, lFrame.getWidth() - 10, lFrame.getHeight() - 10);
		containerPanel.setLayout(null);
		
		JPanel generalPanel = new JPanel();
		generalPanel.setBackground(Color.WHITE);
		generalPanel.setBounds(5, 5, containerPanel.getWidth()-10, 4*bigLabelSpacing + 10);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " General Characteristics ", 
				TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		generalPanel.setBorder(tb);
		generalPanel.setLayout(null);
		
		showPreference2Labels(generalPanel, bigLabelSpacing, "Loader Path", 
				loaderPref.getFilename(), 
				"");
		
		showPreferenceLabels(generalPanel, 2*bigLabelSpacing, Loader.LOADER_TYPE, 
				loaderPref.getPreference(Loader.LOADER_TYPE), 
				"Specify loader type, either CSVALL or MYSQL");
		
		showPreferenceLabels(generalPanel, 3*bigLabelSpacing, Loader.CONSIDERED_LAYERS, 
				loaderPref.getPreference(Loader.CONSIDERED_LAYERS), 
				"Specify considered layers, if indicators of the dataset are grouped through layers. Otherwise, type the 'NO_LAYER' option");
		
		containerPanel.add(generalPanel);
		
		JPanel runsPanel = new JPanel();
		runsPanel.setBackground(Color.WHITE);
		runsPanel.setBounds(5, generalPanel.getHeight() + 10, containerPanel.getWidth()-10, 7*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Runs Setup ", 
				TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		runsPanel.setBorder(tb);
		runsPanel.setLayout(null);
		
		showPreferenceLabels(runsPanel, bigLabelSpacing, CSVPreLoader.FILTERING_CSV_FILE, 
				loaderPref.getPreference(CSVPreLoader.FILTERING_CSV_FILE), 
				"Specify filtering file path, starting from '" + iManager.getLoaderFolder() + "'");
		
		showPreferenceLabels(runsPanel, 2*bigLabelSpacing, Loader.FILTERING_RUN_PREFERENCE, 
				loaderPref.getPreference(Loader.FILTERING_RUN_PREFERENCE), 
				"Specify runs to be used for filtering, either numbers (e.g., 8) or intervals (e.g., 10-15) separated by commas");
		
		showPreferenceLabels(runsPanel, 3*bigLabelSpacing, CSVPreLoader.TRAIN_CSV_FILE, 
				loaderPref.getPreference(CSVPreLoader.TRAIN_CSV_FILE), 
				"Specify train file path, starting from '" + iManager.getLoaderFolder() + "'");
		
		showPreferenceLabels(runsPanel, 4*bigLabelSpacing, Loader.TRAIN_RUN_PREFERENCE, 
				loaderPref.getPreference(Loader.TRAIN_RUN_PREFERENCE), 
				"Specify runs to be used as training set, either numbers (e.g., 8) or intervals (e.g., 10-15) separated by commas");
		
		showPreferenceLabels(runsPanel, 5*bigLabelSpacing, CSVPreLoader.VALIDATION_CSV_FILE, 
				loaderPref.getPreference(CSVPreLoader.VALIDATION_CSV_FILE), 
				"Specify validation file path, starting from '" + iManager.getLoaderFolder() + "'");
		
		showPreferenceLabels(runsPanel, 6*bigLabelSpacing, Loader.VALIDATION_RUN_PREFERENCE, 
				loaderPref.getPreference(Loader.VALIDATION_RUN_PREFERENCE), 
				"Specify runs to be used as validation set, either numbers (e.g., 8) or intervals (e.g., 10-15) separated by commas");
		
		containerPanel.add(runsPanel);
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBackground(Color.WHITE);
		dataPanel.setBounds(5, generalPanel.getHeight() + runsPanel.getHeight() + 20, containerPanel.getWidth()-10, 5*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Data Setup ", 
				TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		dataPanel.setBorder(tb);
		dataPanel.setLayout(null);
		
		showPreferenceLabels(dataPanel, bigLabelSpacing, CSVPreLoader.EXPERIMENT_ROWS, 
				loaderPref.getPreference(CSVPreLoader.EXPERIMENT_ROWS), 
				"Specify an integer that defines the amount of dataset rows to be considered as single experiment.");
		
		showPreferenceLabels(dataPanel, 2*bigLabelSpacing, CSVPreLoader.LABEL_COLUMN, 
				loaderPref.getPreference(CSVPreLoader.LABEL_COLUMN), 
				"Specify the index (starting from 0) of the column that contains the label, if any.");
		
		showPreferenceLabels(dataPanel, 3*bigLabelSpacing, CSVPreLoader.FAULTY_TAGS, 
				loaderPref.getPreference(CSVPreLoader.FAULTY_TAGS), 
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to faulty/attack data");
		
		showPreferenceLabels(dataPanel, 4*bigLabelSpacing, CSVPreLoader.SKIP_COLUMNS, 
				loaderPref.getPreference(CSVPreLoader.SKIP_COLUMNS), 
				"Define columns (starting from 0) to be skipped by algorithms i.e., non numeric ones, columns containing not-so-useful data.");
		
		containerPanel.add(dataPanel);
		
		// FOOTER
        
        JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		fPanel.setBounds(containerPanel.getWidth()/8, generalPanel.getHeight() + runsPanel.getHeight() + dataPanel.getHeight() + 20, containerPanel.getWidth()/4*3, labelSpacing + 40);
		fPanel.setLayout(null);
		
		JButton button = new JButton("Save Changes");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.setBounds(20, 25, fPanel.getWidth()/2 - 40, labelSpacing+10);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				String checkParameters = checkParameters();
				if(checkParameters == null){
					loaderPref.updateToFile();
				} else JOptionPane.showMessageDialog(lFrame, checkParameters,
					    "Error while setting parameters", JOptionPane.ERROR_MESSAGE);
			} } );	
		fPanel.add(button);
		
		button = new JButton("Discard Changes");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.setBounds(fPanel.getWidth()/2 + 20, 25, fPanel.getWidth()/2 - 40, labelSpacing + 10);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				lFrame.setVisible(false);
			} } );	
		fPanel.add(button);
		
		containerPanel.add(fPanel);
		
		if(lFrame.getHeight() < generalPanel.getHeight() + runsPanel.getHeight() + dataPanel.getHeight() + fPanel.getHeight() + 70)
			lFrame.setBounds(lFrame.getX(), lFrame.getY(), lFrame.getWidth(), generalPanel.getHeight() + runsPanel.getHeight() + dataPanel.getHeight() + fPanel.getHeight() + 70);
        
		return containerPanel;
	}
	
	protected String checkParameters() {
		String output = "";
		if(loaderPref.hasPreference(Loader.LOADER_TYPE) && 
				!loaderPref.getPreference(Loader.LOADER_TYPE).equals("CSVALL") && !loaderPref.getPreference(Loader.LOADER_TYPE).equals("MYSQL")){
			output = output + "Wrong LOADER_TYPE value: insert either CSVALL or MYSQL.\n";
		}
		if(loaderPref.hasPreference(Loader.CONSIDERED_LAYERS) && 
				!loaderPref.getPreference(Loader.CONSIDERED_LAYERS).equals("NO_LAYER")){
			output = output + "Wrong CONSIDERED_LAYERS value: consider trying with 'NO_LAYER'.\n";
		}
		if(!loaderPref.hasPreference(CSVPreLoader.FILTERING_CSV_FILE) || 
				loaderPref.getPreference(CSVPreLoader.FILTERING_CSV_FILE).trim().length() == 0){
			output = output + "Wrong FILTERING_CSV_FILE value: remember to specify file for filtering.\n";
		} else if(!new File(iManager.getDatasetsFolder() + loaderPref.getPreference(CSVPreLoader.FILTERING_CSV_FILE)).exists()){
			output = output + "FILTERING_CSV_FILE does not exist.\n";
		}
		if(!loaderPref.hasPreference(CSVPreLoader.TRAIN_CSV_FILE) || 
				loaderPref.getPreference(CSVPreLoader.TRAIN_CSV_FILE).trim().length() == 0){
			output = output + "Wrong TRAIN_CSV_FILE value: remember to specify file for training.\n";
		} else if(!new File(iManager.getDatasetsFolder() + loaderPref.getPreference(CSVPreLoader.TRAIN_CSV_FILE)).exists()){
			output = output + "TRAIN_CSV_FILE does not exist.\n";
		}
		if(!loaderPref.hasPreference(CSVPreLoader.VALIDATION_CSV_FILE) || 
				loaderPref.getPreference(CSVPreLoader.VALIDATION_CSV_FILE).trim().length() == 0){
			output = output + "Wrong VALIDATION_CSV_FILE value: remember to specify file for validation.\n";
		} else if(!new File(iManager.getDatasetsFolder() + loaderPref.getPreference(CSVPreLoader.VALIDATION_CSV_FILE)).exists()){
			output = output + "VALIDATION_CSV_FILE does not exist.\n";
		}
		if(!loaderPref.hasPreference(CSVPreLoader.FILTERING_RUN_PREFERENCE) || 
				loaderPref.getPreference(CSVPreLoader.FILTERING_RUN_PREFERENCE).trim().length() == 0){
			output = output + "Wrong FILTERING_RUN_PREFERENCE value: remember to specify runs for filtering.\n";
		}
		if(!loaderPref.hasPreference(CSVPreLoader.TRAIN_RUN_PREFERENCE) || 
				loaderPref.getPreference(CSVPreLoader.TRAIN_RUN_PREFERENCE).trim().length() == 0){
			output = output + "Wrong TRAIN_RUN_PREFERENCE value: remember to specify runs for training.\n";
		}
		if(!loaderPref.hasPreference(CSVPreLoader.VALIDATION_RUN_PREFERENCE) || 
				loaderPref.getPreference(CSVPreLoader.VALIDATION_RUN_PREFERENCE).trim().length() == 0){
			output = output + "Wrong VALIDATION_RUN_PREFERENCE value: remember to specify runs for validation.\n";
		}
		if(!loaderPref.hasPreference(CSVPreLoader.EXPERIMENT_ROWS) ||
				!AppUtility.isInteger(loaderPref.getPreference(CSVPreLoader.EXPERIMENT_ROWS))){
			output = output + "Wrong EXPERIMENT_ROWS value: insert a positive integer number.\n";
		}
		if(!loaderPref.hasPreference(CSVPreLoader.LABEL_COLUMN) ||
				!AppUtility.isInteger(loaderPref.getPreference(CSVPreLoader.LABEL_COLUMN))){
			output = output + "Wrong LABEL_COLUMN value: insert a positive integer number.\n";
		}
		return output.trim().length() > 0 ? output : null;
	}

	private void showPreferenceLabels(JPanel root, int panelY, String prefName, String textFieldText, String description){
		
		JLabel lbl = new JLabel(prefName);
		lbl.setBounds(10, panelY, (root.getWidth()-20)/2, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		root.add(lbl);
		
		JTextField textField = new JTextField();
		textField.setText(textFieldText);
		textField.setBounds(root.getWidth()/2, panelY, (root.getWidth()-20)/2, bigLabelSpacing);
		textField.setFont(labelFont);
		textField.setColumns(10);
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
				if (textField.getText() != null && textField.getText().length() > 0){
	        		loaderPref.updatePreference(prefName, textField.getText(), false);
	        	}
			}
		});
		
		root.add(textField);
		
	}
	
private void showPreference2Labels(JPanel root, int panelY, String prefName, String textFieldText, String description){
		
		JLabel lbl = new JLabel(prefName);
		lbl.setBounds(10, panelY, (root.getWidth()-20)/2, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		root.add(lbl);
		
		lbl = new JLabel(textFieldText);
		lbl.setBounds(root.getWidth()/2, panelY, (root.getWidth()-20)/2, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		root.add(lbl);
		
	}

}
