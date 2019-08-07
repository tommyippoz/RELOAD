/**
 * 
 */
package ippoz.reload.executable.ui;

import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;
import ippoz.reload.loader.CSVCompleteLoader;
import ippoz.reload.loader.Loader;
import ippoz.reload.loader.LoaderType;
import ippoz.reload.manager.InputManager;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
		
		bigFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelFont = new Font("Times", Font.PLAIN, (int)((14 + rate)/2));
		
		labelSpacing = (int)(lFrame.getHeight()/26);
		bigLabelSpacing = (int)(lFrame.getHeight()/20);
		
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
				loaderPref.getFilename(), "");
		
		showPreferenceCB(generalPanel, 2*bigLabelSpacing, Loader.LOADER_TYPE, 
				loaderPref.getPreference(Loader.LOADER_TYPE), LoaderType.values(), 
				"Specify loader type, either CSV, MYSQL or ARFF");
		
		showPreferenceLabels(generalPanel, 3*bigLabelSpacing, Loader.CONSIDERED_LAYERS, 
				loaderPref.getPreference(Loader.CONSIDERED_LAYERS), 
				"Specify considered layers, if indicators of the dataset are grouped through layers. Otherwise, type the 'NO_LAYER' option");
		
		containerPanel.add(generalPanel);
		
		JPanel trainPanel = new JPanel();
		trainPanel.setBackground(Color.WHITE);
		trainPanel.setBounds(5, generalPanel.getHeight() + 10, containerPanel.getWidth()-10, 7*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Train Setup ", 
				TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		trainPanel.setBorder(tb);
		trainPanel.setLayout(null);
		
		showPreferenceButton(trainPanel, 1*bigLabelSpacing, CSVCompleteLoader.TRAIN_CSV_FILE, 
				loaderPref.getPreference(CSVCompleteLoader.TRAIN_CSV_FILE), 
				"Specify train file path, starting from '" + iManager.getLoaderFolder() + "'");
		
		showPreferenceLabels(trainPanel, 2*bigLabelSpacing, Loader.TRAIN_RUN_PREFERENCE, 
				loaderPref.getPreference(Loader.TRAIN_RUN_PREFERENCE), 
				"Specify runs to be used as training set, either numbers (e.g., 8) or intervals (e.g., 10-15) separated by commas");
		
		showPreferenceLabels(trainPanel, 3*bigLabelSpacing, CSVCompleteLoader.TRAIN_FAULTY_TAGS, 
				loaderPref.hasPreference(CSVCompleteLoader.TRAIN_FAULTY_TAGS) ? loaderPref.getPreference(CSVCompleteLoader.TRAIN_FAULTY_TAGS) : loaderPref.getPreference("FAULTY_TAGS"), 
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to faulty/attack data for training");
		
		showPreferenceLabels(trainPanel, 4*bigLabelSpacing, CSVCompleteLoader.TRAIN_SKIP_ROWS, 
				loaderPref.hasPreference(CSVCompleteLoader.TRAIN_SKIP_ROWS) ? loaderPref.getPreference(CSVCompleteLoader.TRAIN_SKIP_ROWS) : loaderPref.getPreference("SKIP_ROWS"), 
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to be skipped i.e., not relevant for the analysis.");
		
		showCheckPreferenceLabels(trainPanel, 5*bigLabelSpacing, CSVCompleteLoader.TRAIN_EXPERIMENT_ROWS, 
				loaderPref.getPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_ROWS), loaderPref.hasPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_ROWS), 
				"Specify an integer that defines the amount of dataset rows to be considered as single experiment.");
		
		showCheckPreferenceLabels(trainPanel, 6*bigLabelSpacing, CSVCompleteLoader.TRAIN_EXPERIMENT_SPLIT_ROWS, 
				loaderPref.getPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_SPLIT_ROWS), loaderPref.hasPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_SPLIT_ROWS), 
				"Specify the index (starting from 0) of the column that changes when experiments change");
		
		containerPanel.add(trainPanel);
		
		JPanel validationPanel = new JPanel();
		validationPanel.setBackground(Color.WHITE);
		validationPanel.setBounds(5, generalPanel.getHeight() + 10 + trainPanel.getHeight(), containerPanel.getWidth()-10, 7*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Validation Setup ", 
				TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		validationPanel.setBorder(tb);
		validationPanel.setLayout(null);
		
		showPreferenceButton(validationPanel, bigLabelSpacing, CSVCompleteLoader.VALIDATION_CSV_FILE, 
				loaderPref.getPreference(CSVCompleteLoader.VALIDATION_CSV_FILE), 
				"Specify validation file path, starting from '" + iManager.getLoaderFolder() + "'");
		
		showPreferenceLabels(validationPanel, 2*bigLabelSpacing, Loader.VALIDATION_RUN_PREFERENCE, 
				loaderPref.getPreference(Loader.VALIDATION_RUN_PREFERENCE), 
				"Specify runs to be used as validation set, either numbers (e.g., 8) or intervals (e.g., 10-15) separated by commas");
		
		showPreferenceLabels(validationPanel, 3*bigLabelSpacing, CSVCompleteLoader.VALIDATION_FAULTY_TAGS, 
				loaderPref.hasPreference(CSVCompleteLoader.VALIDATION_FAULTY_TAGS) ? loaderPref.getPreference(CSVCompleteLoader.VALIDATION_FAULTY_TAGS) : loaderPref.getPreference("FAULTY_TAGS"),  
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to faulty/attack data for validation");
		
		showPreferenceLabels(validationPanel, 4*bigLabelSpacing, CSVCompleteLoader.VALIDATION_SKIP_ROWS, 
				loaderPref.hasPreference(CSVCompleteLoader.VALIDATION_SKIP_ROWS) ? loaderPref.getPreference(CSVCompleteLoader.VALIDATION_SKIP_ROWS) : loaderPref.getPreference("SKIP_ROWS"), 
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to be skipped i.e., not relevant for the analysis.");
		
		showCheckPreferenceLabels(validationPanel, 5*bigLabelSpacing, CSVCompleteLoader.VALIDATION_EXPERIMENT_ROWS, 
				loaderPref.getPreference(CSVCompleteLoader.VALIDATION_EXPERIMENT_ROWS), loaderPref.hasPreference(CSVCompleteLoader.VALIDATION_EXPERIMENT_ROWS), 
				"Specify an integer that defines the amount of dataset rows to be considered as single experiment.");
		
		showCheckPreferenceLabels(validationPanel, 6*bigLabelSpacing, CSVCompleteLoader.VALIDATION_EXPERIMENT_SPLIT_ROWS, 
				loaderPref.getPreference(CSVCompleteLoader.VALIDATION_EXPERIMENT_SPLIT_ROWS), loaderPref.hasPreference(CSVCompleteLoader.VALIDATION_EXPERIMENT_SPLIT_ROWS), 
				"Specify the index (starting from 0) of the column that changes when experiments change");
		
		containerPanel.add(validationPanel);
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBackground(Color.WHITE);
		dataPanel.setBounds(5, generalPanel.getHeight() + trainPanel.getHeight() + validationPanel.getHeight() + 20, containerPanel.getWidth()-10, 3*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Data Setup ", 
				TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		dataPanel.setBorder(tb);
		dataPanel.setLayout(null);
		
		showPreferenceLabels(dataPanel, bigLabelSpacing, CSVCompleteLoader.LABEL_COLUMN, 
				loaderPref.getPreference(CSVCompleteLoader.LABEL_COLUMN), 
				"Specify the index (starting from 0) of the column that contains the label, if any.");
		
		showPreferenceLabels(dataPanel, 2*bigLabelSpacing, CSVCompleteLoader.SKIP_COLUMNS, 
				loaderPref.getPreference(CSVCompleteLoader.SKIP_COLUMNS), 
				"Define columns (starting from 0) to be skipped by algorithms i.e., non numeric ones, columns containing not-so-useful data.");
		
		containerPanel.add(dataPanel);
		
		// FOOTER
        
        JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		fPanel.setBounds(10, generalPanel.getHeight() + trainPanel.getHeight() + validationPanel.getHeight() + dataPanel.getHeight() + 20, containerPanel.getWidth()-20, labelSpacing + 40);
		fPanel.setLayout(null);
		
		JButton button = new JButton("Save Changes");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.setBounds(20, 25, fPanel.getWidth()/3 - 40, labelSpacing+10);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				String checkParameters = checkParameters();
				if(checkParameters == null){
					loaderPref.updateToFile();
				} else JOptionPane.showMessageDialog(lFrame, checkParameters,
					    "Error while setting parameters", JOptionPane.ERROR_MESSAGE);
				lFrame.setVisible(false);
			} } );	
		fPanel.add(button);
		
		button = new JButton("Open File");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.setBounds(fPanel.getWidth()/3 + 20, 25, fPanel.getWidth()/3 - 40, labelSpacing + 10);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				try {
					Desktop.getDesktop().open(loaderPref.getFile());
					loaderPref.refresh();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} } );	
		fPanel.add(button);
		
		button = new JButton("Discard Changes");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.setBounds(fPanel.getWidth()/3*2 + 20, 25, fPanel.getWidth()/3 - 40, labelSpacing + 10);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				lFrame.setVisible(false);
			} } );	
		fPanel.add(button);
		
		containerPanel.add(fPanel);
		
		if(lFrame.getHeight() < generalPanel.getHeight() + trainPanel.getHeight() + validationPanel.getHeight() + dataPanel.getHeight() + fPanel.getHeight() + 70)
			lFrame.setBounds(lFrame.getX(), lFrame.getY(), lFrame.getWidth(), generalPanel.getHeight() + trainPanel.getHeight() + validationPanel.getHeight() + dataPanel.getHeight() + fPanel.getHeight() + 70);
        
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
		if(!loaderPref.hasPreference(CSVCompleteLoader.TRAIN_CSV_FILE) || 
				loaderPref.getPreference(CSVCompleteLoader.TRAIN_CSV_FILE).trim().length() == 0){
			output = output + "Wrong TRAIN_CSV_FILE value: remember to specify file for training.\n";
		} else if(!new File(iManager.getDatasetsFolder() + loaderPref.getPreference(CSVCompleteLoader.TRAIN_CSV_FILE)).exists()){
			output = output + "TRAIN_CSV_FILE (" + (iManager.getDatasetsFolder() + loaderPref.getPreference(CSVCompleteLoader.TRAIN_CSV_FILE)) +  ") does not exist.\n";
		}
		if(!loaderPref.hasPreference(CSVCompleteLoader.VALIDATION_CSV_FILE) || 
				loaderPref.getPreference(CSVCompleteLoader.VALIDATION_CSV_FILE).trim().length() == 0){
			output = output + "Wrong VALIDATION_CSV_FILE value: remember to specify file for validation.\n";
		} else if(!new File(iManager.getDatasetsFolder() + loaderPref.getPreference(CSVCompleteLoader.VALIDATION_CSV_FILE)).exists()){
			output = output + "VALIDATION_CSV_FILE (" + (iManager.getDatasetsFolder() + loaderPref.getPreference(CSVCompleteLoader.VALIDATION_CSV_FILE)) +  ") does not exist.\n";
		}
		if(!loaderPref.hasPreference(CSVCompleteLoader.TRAIN_RUN_PREFERENCE) || 
				loaderPref.getPreference(CSVCompleteLoader.TRAIN_RUN_PREFERENCE).trim().length() == 0){
			output = output + "Wrong TRAIN_RUN_PREFERENCE value: remember to specify runs for training.\n";
		}
		if(!loaderPref.hasPreference(CSVCompleteLoader.VALIDATION_RUN_PREFERENCE) || 
				loaderPref.getPreference(CSVCompleteLoader.VALIDATION_RUN_PREFERENCE).trim().length() == 0){
			output = output + "Wrong VALIDATION_RUN_PREFERENCE value: remember to specify runs for validation.\n";
		}
		if(loaderPref.hasPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_ROWS) &&
				loaderPref.getPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_ROWS).length() > 0 && 
					!AppUtility.isInteger(loaderPref.getPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_ROWS))){
			output = output + "Wrong EXPERIMENT_ROWS value: insert a positive integer number.\n";
		}
		if(loaderPref.hasPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_SPLIT_ROWS) &&
				loaderPref.getPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_SPLIT_ROWS).length() > 0 && 
					!AppUtility.isInteger(loaderPref.getPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_SPLIT_ROWS))){
			output = output + "Wrong EXPERIMENT_SPLIT_COLUMN value: insert a positive integer number.\n";
		}
		if(!loaderPref.hasPreference(CSVCompleteLoader.LABEL_COLUMN) ||
				!AppUtility.isInteger(loaderPref.getPreference(CSVCompleteLoader.LABEL_COLUMN))){
			output = output + "Wrong LABEL_COLUMN value: insert a positive integer number.\n";
		}
		return output.trim().length() > 0 ? output : null;
	}
	
	private void showPreferenceCB(JPanel root, int panelY, String prefName, String textFieldText, Object[] itemList, String description){
		
		JLabel lbl = new JLabel(prefName);
		lbl.setBounds(10, panelY, (root.getWidth()-20)/2, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		root.add(lbl);
		
		JComboBox<Object> comboBox = new JComboBox<Object>();
		comboBox.setFont(labelFont);
		comboBox.setBounds(root.getWidth()/2, panelY, (root.getWidth()-20)/2, bigLabelSpacing);
		
		if(itemList != null){
			for(Object ob : itemList){
				comboBox.addItem(ob);
			}
			comboBox.addActionListener (new ActionListener () {
			    public void actionPerformed(ActionEvent e) {
			        String newValue = comboBox.getSelectedItem().toString();
			        loaderPref.updatePreference(Loader.LOADER_TYPE, newValue, false);	
			    }
			});
		}
		
		if(textFieldText != null)
			comboBox.setSelectedItem(textFieldText);
		
		root.add(comboBox);
		
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
	        		loaderPref.updatePreference(prefName, textField.getText(), true, false);
	        	}
			}
		});
		
		root.add(textField);
		
	}
	
	private void showCheckPreferenceLabels(JPanel root, int panelY, String prefName, String textFieldText, boolean activated, String description){
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		panel.setBackground(Color.WHITE);
		panel.setBounds(10, panelY, (root.getWidth()-20)/2, labelSpacing);
		
		JCheckBox cb = new JCheckBox();
		cb.setSelected(activated);
		
		panel.add(cb);
		
		JLabel lbl = new JLabel(prefName);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		panel.add(lbl);
		
		root.add(panel);
		
		JTextField textField = new JTextField();
		textField.setText(textFieldText);
		textField.setBounds(root.getWidth()/2, panelY, (root.getWidth()-20)/2, bigLabelSpacing);
		textField.setFont(labelFont);
		textField.setEnabled(activated);
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
	        		loaderPref.updatePreference(prefName, textField.getText(), true, false);
	        	}
			}
		});
		
		cb.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent event) {
		        JCheckBox cb = (JCheckBox) event.getSource();
		        textField.setEnabled(cb.isSelected());		        	
		    }
		});
		
		root.add(textField);
		
	}
	
	private void showPreferenceButton(JPanel root, int panelY, String prefName, String textFieldText, String description){
		
		JLabel lbl = new JLabel(prefName);
		lbl.setBounds(10, panelY, (root.getWidth()-20)/2, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		root.add(lbl);
		
		JButton button = new JButton(textFieldText);
		button.setVisible(true);
		button.setBounds(root.getWidth()/2, panelY, (root.getWidth()-20)/2, bigLabelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				JFileChooser jfc = new JFileChooser(new File("").getAbsolutePath());
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					Path pathAbsolute = Paths.get(selectedFile.getAbsolutePath());
			        Path pathBase = Paths.get(new File(iManager.getDatasetsFolder()).getAbsolutePath());
					if(!selectedFile.isDirectory()){
						button.setText(pathBase.relativize(pathAbsolute).toString());
						loaderPref.updatePreference(prefName, pathBase.relativize(pathAbsolute).toString(), true, false);
					} else JOptionPane.showMessageDialog(lFrame, "'" + pathBase.relativize(pathAbsolute).toString() + "' is not a folder");
				}
			} } );
		root.add(button);		
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
