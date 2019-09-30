/**
 * 
 */
package ippoz.reload.ui;

import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;
import ippoz.reload.loader.ARFFLoader;
import ippoz.reload.loader.CSVBaseLoader;
import ippoz.reload.loader.CSVCompleteLoader;
import ippoz.reload.loader.Loader;
import ippoz.reload.loader.LoaderType;
import ippoz.reload.loader.MySQLLoader;
import ippoz.reload.manager.InputManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
public class LoaderFrame {
	
	private JFrame lFrame;
	
	private Font bigFont;
	
	private Font labelFont;
	
	private int labelSpacing;
	
	private int bigLabelSpacing;
	
	private JPanel loaderPanel;
	
	private InputManager iManager;
	
	private PreferencesManager loaderPref;
	
	private Loader tLoader;
	
	private Loader vLoader;
	
	public LoaderFrame(InputManager iManager, PreferencesManager loaderPref) {
		this.iManager = iManager;
		this.loaderPref = loaderPref;
		tLoader = buildLoader("train");
		vLoader = buildLoader("validation");
		
		buildFrame();
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		
		bigFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelFont = new Font("Times", Font.PLAIN, (int)((14 + rate)/2));
		
		labelSpacing = (int)(lFrame.getHeight()/26);
		bigLabelSpacing = (int)(lFrame.getHeight()/20);
		
		loaderPanel = buildMainPanel();
	}
	
	private void reload() {
		lFrame.setVisible(false);
		lFrame.getContentPane().removeAll();
		tLoader = buildLoader("train");
		vLoader = buildLoader("validation");
		loaderPanel = buildMainPanel();
		setVisible(true);
	}

	private Loader buildLoader(String loaderTag) {
		String loaderType = loaderPref.getPreference(Loader.LOADER_TYPE);
		if(loaderType != null && loaderType.toUpperCase().contains("MYSQL"))
			return new MySQLLoader(null, loaderPref, loaderTag, "NO_LAYER", null);
		else if(loaderType != null && loaderType.toUpperCase().contains("CSV")){
			if(loaderTag.equals("train"))
				return new CSVCompleteLoader(iManager.readRunIds(loaderPref.getPreference(CSVBaseLoader.TRAIN_RUN_PREFERENCE)), loaderPref, loaderTag, iManager.getAnomalyWindow(), iManager.getDatasetsFolder());
			else return new CSVCompleteLoader(iManager.readRunIds(loaderPref.getPreference(CSVBaseLoader.VALIDATION_RUN_PREFERENCE)), loaderPref, loaderTag, iManager.getAnomalyWindow(), iManager.getDatasetsFolder());
		} else if(loaderType != null && loaderType.toUpperCase().contains("ARFF"))
			return new ARFFLoader(null, loaderPref, loaderTag, iManager.getAnomalyWindow(), iManager.getDatasetsFolder());
		else {
			AppLogger.logError(getClass(), "LoaderError", "Unable to parse loader '" + loaderType + "'");
			return null;
		} 
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
			lFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.5), (int)(screenSize.getHeight()*0.8));
		else lFrame.setBounds(0, 0, 600, 800);
		lFrame.setBackground(Color.WHITE);
		//lFrame.setResizable(false);
	}
	
	private JLabel initLabel(String text){
		JLabel lbl = new JLabel(text);
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		return lbl;
	}
	
	private JPanel buildMainPanel() {	
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		//containerPanel.setBounds(5, 5, lFrame.getWidth() - 10, lFrame.getHeight() - 10);
		containerPanel.setLayout(new BorderLayout());
		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		
		JPanel twoRowPanel = new JPanel();
		twoRowPanel.setBackground(Color.WHITE);
		twoRowPanel.setLayout(new GridLayout(2, 1, 20, 0));
		
		JPanel generalPanel = new JPanel();
		generalPanel.setBackground(Color.WHITE);
		//generalPanel.setBounds(5, 5, containerPanel.getWidth()-10, 3*bigLabelSpacing + 10);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " General Characteristics ", 
				TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		generalPanel.setBorder(tb);
		generalPanel.setLayout(new GridLayout(2, 1, 20, 0));
		
		JButton button = new JButton("Open File");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
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
		
		showPreference2Labels(generalPanel, bigLabelSpacing, "Loader Path", loaderPref.getFilename(), "", button);
		
		showPreferenceCB(generalPanel, 2*bigLabelSpacing, Loader.LOADER_TYPE, 
				loaderPref.getPreference(Loader.LOADER_TYPE), LoaderType.values(), 
				"Specify loader type, either CSV, MYSQL or ARFF", null);
		twoRowPanel.add(generalPanel);
		
		JPanel sourcePanel = new JPanel();
		sourcePanel.setBackground(Color.WHITE);
		//sourcePanel.setBounds(5, generalPanel.getHeight() + 5, containerPanel.getWidth()-10, 3*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Sources Setup ", 
				TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		sourcePanel.setBorder(tb);
		sourcePanel.setLayout(new GridLayout(2, 1, 20, 0));
		
		JLabel trainDatasetLabel = initLabel("Not Defined");
		if(tLoader != null && tLoader.canFetch())
			trainDatasetLabel.setText("Size: " + tLoader.getMBSize() + " MB, " + tLoader.getRowNumber() + " rows");
		
		showPreferenceButton(sourcePanel, 1*bigLabelSpacing, CSVCompleteLoader.TRAIN_CSV_FILE, 
				loaderPref.getPreference(CSVCompleteLoader.TRAIN_CSV_FILE), 
				"Specify train file path, starting from '" + iManager.getLoaderFolder() + "'", trainDatasetLabel);
		
		JLabel validationDatasetLabel = initLabel("Not Defined");
		if(vLoader != null && vLoader.canFetch())
			validationDatasetLabel.setText("Size: " + vLoader.getMBSize() + " MB, " + vLoader.getRowNumber() + " rows");
		
		showPreferenceButton(sourcePanel, 2*bigLabelSpacing, CSVCompleteLoader.VALIDATION_CSV_FILE, 
				loaderPref.getPreference(CSVCompleteLoader.VALIDATION_CSV_FILE), 
				"Specify validation file path, starting from '" + iManager.getLoaderFolder() + "'", validationDatasetLabel);
		
		twoRowPanel.add(sourcePanel);
		
		containerPanel.add(twoRowPanel, BorderLayout.NORTH);
		
		JPanel dataPanel = new JPanel();
		dataPanel.setBackground(Color.WHITE);
		//dataPanel.setBounds(5, generalPanel.getHeight() + sourcePanel.getHeight() + 10, containerPanel.getWidth()-10, 5*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Common Data Setup ", 
				TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		dataPanel.setBorder(tb);
		dataPanel.setLayout(new GridLayout(4, 1, 20, 0));
		
		showCheckPreferenceLabels(dataPanel, bigLabelSpacing, CSVCompleteLoader.TRAIN_EXPERIMENT_ROWS, 
				loaderPref.getPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_ROWS), loaderPref.hasPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_ROWS), 
				"Specify an integer that defines the amount of dataset rows to be considered as single experiment.", null);
		
		showCheckPreferenceLabels(dataPanel, 2*bigLabelSpacing, CSVCompleteLoader.TRAIN_EXPERIMENT_SPLIT_ROWS, 
				loaderPref.getPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_SPLIT_ROWS), loaderPref.hasPreference(CSVCompleteLoader.TRAIN_EXPERIMENT_SPLIT_ROWS), 
				"Specify the index (starting from 0) of the column that changes when experiments change", null);
		
		int featureNumber = 0;
		if(tLoader != null && tLoader.canFetch())
			featureNumber = tLoader.getFeatureNames().length;
		else if(vLoader != null && vLoader.canFetch())
			featureNumber = vLoader.getFeatureNames().length;
		
		JLabel labelColumnLabel = initLabel("Not Defined");
		if(featureNumber > 0)
			labelColumnLabel.setText("Available indexes: 0 - " + (featureNumber - 1));
		
		showPreferenceLabels(dataPanel, 3*bigLabelSpacing, CSVCompleteLoader.LABEL_COLUMN, 
				loaderPref.getPreference(CSVCompleteLoader.LABEL_COLUMN), 
				"Specify the index (starting from 0) of the column that contains the label, if any.", labelColumnLabel);
		
		JLabel labelColumnLabel2 = initLabel("Not Defined");
		if(featureNumber > 0)
			labelColumnLabel2.setText("Available indexes: 0 - " + (featureNumber - 1));
		
		showPreferenceLabels(dataPanel, 4*bigLabelSpacing, CSVCompleteLoader.SKIP_COLUMNS, 
				loaderPref.getPreference(CSVCompleteLoader.SKIP_COLUMNS), 
				"Define columns (starting from 0) to be skipped by algorithms i.e., non numeric ones, columns containing not-so-useful data.", labelColumnLabel2);
		
		containerPanel.add(dataPanel, BorderLayout.CENTER);
		
		JPanel threeRowPanel = new JPanel();
		threeRowPanel.setBackground(Color.WHITE);
		threeRowPanel.setLayout(new GridLayout(3, 1, 20, 0));
		
		JPanel trainPanel = new JPanel();
		trainPanel.setBackground(Color.WHITE);
		//trainPanel.setBounds(5, generalPanel.getHeight() + sourcePanel.getHeight() + dataPanel.getHeight() + 15, containerPanel.getWidth()-10, 4*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Train Setup ", 
				TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		trainPanel.setBorder(tb);
		trainPanel.setLayout(new GridLayout(3, 1, 20, 0));
		
		JLabel trainDataPointsLabel = initLabel("Not Defined");
		if(tLoader != null && tLoader.canFetch())
			trainDataPointsLabel.setText(tLoader.getDataPoints() + " data points");
		
		showPreferenceLabels(trainPanel, 1*bigLabelSpacing, Loader.TRAIN_RUN_PREFERENCE, 
				loaderPref.getPreference(Loader.TRAIN_RUN_PREFERENCE), 
				"Specify runs to be used as training set, either numbers (e.g., 8) or intervals (e.g., 10-15) separated by commas", trainDataPointsLabel);
		
		JLabel trainAnomalyRateLabel = initLabel("Not Defined");
		if(tLoader != null && tLoader.canFetch())
			trainAnomalyRateLabel.setText("Anomaly Rate: " + AppUtility.formatDouble(tLoader.getAnomalyRate()) + "%");
		
		showPreferenceLabels(trainPanel, 2*bigLabelSpacing, CSVCompleteLoader.TRAIN_FAULTY_TAGS, 
				loaderPref.hasPreference(CSVCompleteLoader.TRAIN_FAULTY_TAGS) ? loaderPref.getPreference(CSVCompleteLoader.TRAIN_FAULTY_TAGS) : loaderPref.getPreference("FAULTY_TAGS"), 
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to faulty/attack data for training", trainAnomalyRateLabel);
		
		JLabel trainSkipRateLabel = initLabel("Not Defined");
		if(tLoader != null && tLoader.canFetch())
			trainSkipRateLabel.setText("Skip Rate: " + AppUtility.formatDouble(tLoader.getSkipRate()) + "%");
		
		showPreferenceLabels(trainPanel, 3*bigLabelSpacing, CSVCompleteLoader.TRAIN_SKIP_ROWS, 
				loaderPref.hasPreference(CSVCompleteLoader.TRAIN_SKIP_ROWS) ? loaderPref.getPreference(CSVCompleteLoader.TRAIN_SKIP_ROWS) : loaderPref.getPreference("SKIP_ROWS"), 
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to be skipped i.e., not relevant for the analysis.", trainSkipRateLabel);
			
		threeRowPanel.add(trainPanel);
		
		JPanel validationPanel = new JPanel();
		validationPanel.setBackground(Color.WHITE);
		//validationPanel.setBounds(5, generalPanel.getHeight() + 20 + trainPanel.getHeight() + sourcePanel.getHeight() + dataPanel.getHeight(), containerPanel.getWidth()-10, 4*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Validation Setup ", 
				TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		validationPanel.setBorder(tb);
		validationPanel.setLayout(new GridLayout(3, 1, 20, 0));
		
		JLabel validationDataPointsLabel = initLabel("Not Defined");
		if(vLoader != null && vLoader.canFetch())
			validationDataPointsLabel.setText(vLoader.getDataPoints() + " data points");
		
		showPreferenceLabels(validationPanel, 1*bigLabelSpacing, Loader.VALIDATION_RUN_PREFERENCE, 
				loaderPref.getPreference(Loader.VALIDATION_RUN_PREFERENCE), 
				"Specify runs to be used as validation set, either numbers (e.g., 8) or intervals (e.g., 10-15) separated by commas", validationDataPointsLabel);
		
		JLabel validationAnomalyRateLabel = initLabel("Not Defined");
		if(vLoader != null && vLoader.canFetch())
			validationAnomalyRateLabel.setText("Anomaly Rate: " + AppUtility.formatDouble(vLoader.getAnomalyRate()) + "%");
		
		showPreferenceLabels(validationPanel, 2*bigLabelSpacing, CSVCompleteLoader.VALIDATION_FAULTY_TAGS, 
				loaderPref.hasPreference(CSVCompleteLoader.VALIDATION_FAULTY_TAGS) ? loaderPref.getPreference(CSVCompleteLoader.VALIDATION_FAULTY_TAGS) : loaderPref.getPreference("FAULTY_TAGS"),  
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to faulty/attack data for validation", validationAnomalyRateLabel);
		
		JLabel validationSkipRateLabel = initLabel("Not Defined");
		if(vLoader != null && vLoader.canFetch())
			validationSkipRateLabel.setText("Skip Rate: " + AppUtility.formatDouble(vLoader.getSkipRate()) + "%");
		
		showPreferenceLabels(validationPanel, 3*bigLabelSpacing, CSVCompleteLoader.VALIDATION_SKIP_ROWS, 
				loaderPref.hasPreference(CSVCompleteLoader.VALIDATION_SKIP_ROWS) ? loaderPref.getPreference(CSVCompleteLoader.VALIDATION_SKIP_ROWS) : loaderPref.getPreference("SKIP_ROWS"), 
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to be skipped i.e., not relevant for the analysis.", validationSkipRateLabel);
		
		threeRowPanel.add(validationPanel);
		
		// FOOTER
        
        JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		//fPanel.setBounds(10, generalPanel.getHeight() + sourcePanel.getHeight() + trainPanel.getHeight() + validationPanel.getHeight() + dataPanel.getHeight() + 10, containerPanel.getWidth()-20, 3*labelSpacing);
		fPanel.setLayout(new GridLayout(1, 3, 50, 0));
		fPanel.setBorder(new EmptyBorder(20, 50, 20, 50));
		
		button = new JButton("Save Changes");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		//button.setBounds(20, 25, fPanel.getWidth()/3 - 40, labelSpacing+10);
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
		
		ImageIcon ii = new ImageIcon(getClass().getResource("/reload.png"));
		button = new JButton("", new ImageIcon(ii.getImage().getScaledInstance(labelSpacing+10, labelSpacing+10, Image.SCALE_DEFAULT)));
		//button.setBounds(fPanel.getWidth()/2 - labelSpacing, labelSpacing/2, 2*labelSpacing, 2*labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				String checkParameters = checkParameters();
				if(checkParameters == null){
					loaderPref.updateToFile();
				} else JOptionPane.showMessageDialog(lFrame, checkParameters,
					    "Error while setting parameters", JOptionPane.ERROR_MESSAGE);
				reload();
			}
		} );
		fPanel.add(button);
		
		button = new JButton("Discard Changes");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		//button.setBounds(fPanel.getWidth()/3*2 + 20, 25, fPanel.getWidth()/3 - 40, labelSpacing + 10);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				lFrame.setVisible(false);
			} } );	
		fPanel.add(button);
		
		threeRowPanel.add(fPanel);
		
		containerPanel.add(threeRowPanel, BorderLayout.SOUTH);
		
		/*if(lFrame.getHeight() < generalPanel.getHeight() + sourcePanel.getHeight() +  trainPanel.getHeight() + validationPanel.getHeight() + dataPanel.getHeight() + fPanel.getHeight() + 70)
			lFrame.setBounds(lFrame.getX(), lFrame.getY(), lFrame.getWidth(), generalPanel.getHeight() + sourcePanel.getHeight() +  trainPanel.getHeight() + validationPanel.getHeight() + dataPanel.getHeight() + fPanel.getHeight() + 70);
        */
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
	
	private void showPreferenceCB(JPanel root, int panelY, String prefName, String textFieldText, Object[] itemList, String description, JComponent additionalInfo){
		int items = additionalInfo != null ? 3 : 2;
		int space = 20/(items+1);
		int basicSize = (root.getWidth()-20) / (1 + items*2);
		int bigSize = 3*basicSize;
		int smallSize = 2*basicSize;
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridLayout(1, additionalInfo != null ? 3 : 2));
		
		JLabel lbl = new JLabel(prefName);
		//lbl.setBounds(space, panelY, smallSize, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		panel.add(lbl);
		
		JComboBox<Object> comboBox = new JComboBox<Object>();
		comboBox.setFont(labelFont);
		//comboBox.setBounds(smallSize + 2*space, panelY, bigSize, bigLabelSpacing);
		
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
		
		panel.add(comboBox);
		
		if(additionalInfo != null){
			//additionalInfo.setBounds(smallSize + bigSize + space*3, panelY, smallSize, bigLabelSpacing);
			panel.add(additionalInfo);
		}
		
		root.add(panel);
		
	}
	
	private void showPreferenceLabels(JPanel root, int panelY, String prefName, String textFieldText, String description){
		showPreferenceLabels(root, panelY, prefName, textFieldText, description, null);
	}

	private void showPreferenceLabels(JPanel root, int panelY, String prefName, String textFieldText, String description, JComponent additionalInfo){
		int items = additionalInfo != null ? 3 : 2;
		int space = 20/(items+1);
		int basicSize = (root.getWidth()-20) / (1 + items*2);
		int bigSize = 3*basicSize;
		int smallSize = 2*basicSize;
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridLayout(1, additionalInfo != null ? 3 : 2));
				
		JLabel lbl = new JLabel(prefName);
		//lbl.setBounds(space, panelY, smallSize, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		panel.add(lbl);
		
		JTextField textField = new JTextField();
		textField.setText(textFieldText);
		//textField.setBounds(smallSize + space*2, panelY, bigSize, bigLabelSpacing);
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
		
		panel.add(textField);
		
		if(additionalInfo != null){
			//additionalInfo.setBounds(smallSize + bigSize + space*3, panelY, smallSize, bigLabelSpacing);
			panel.add(additionalInfo);
		}
		
		root.add(panel);
		
	}
	
	private void showCheckPreferenceLabels(JPanel root, int panelY, String prefName, String textFieldText, boolean activated, String description, JComponent additionalInfo){
		int space = 5;
		int basicSize = (root.getWidth()-20) / 7;
		int bigSize = 3*basicSize;
		int smallSize = 2*basicSize;
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridLayout(1, additionalInfo != null ? 4 : 3));
		
		JCheckBox cb = new JCheckBox("Enable");
		cb.setHorizontalAlignment(SwingConstants.CENTER);
		//cb.setBounds(smallSize + bigSize + space*3, panelY, smallSize, bigLabelSpacing);
		cb.setSelected(activated);
		
		JLabel lbl = new JLabel(prefName);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		//lbl.setBounds(space, panelY, smallSize, labelSpacing);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		panel.add(lbl);
		
		JTextField textField = new JTextField();
		textField.setText(textFieldText);
		//textField.setBounds(smallSize + 2*space, panelY, bigSize, bigLabelSpacing);
		textField.setFont(labelFont);
		textField.setEnabled(activated);
		textField.setColumns(10);
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
		
		panel.add(textField);
		
		if(additionalInfo != null){
			//additionalInfo.setBounds(smallSize + bigSize + space*3, panelY, smallSize, bigLabelSpacing);
			panel.add(additionalInfo);
		}
		
		panel.add(cb);
		
		root.add(panel);
		
	}
	
	private void showPreferenceButton(JPanel root, int panelY, String prefName, String textFieldText, String description, JComponent additionalInfo){
		int items = additionalInfo != null ? 3 : 2;
		int space = 20/(items+1);
		int basicSize = (root.getWidth()-20) / (1 + items*2);
		int bigSize = 3*basicSize;
		int smallSize = 2*basicSize;
		
		JLabel lbl = new JLabel(prefName);
		lbl.setBounds(space, panelY, smallSize, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		root.add(lbl);
		
		JButton button = new JButton(textFieldText);
		button.setVisible(true);
		button.setBounds(smallSize + 2*space, panelY, bigSize, bigLabelSpacing);
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
		
		if(additionalInfo != null){
			additionalInfo.setBounds(smallSize + bigSize + space*3, panelY, smallSize, bigLabelSpacing);
			root.add(additionalInfo);
		}
	}
	
	private void showPreference2Labels(JPanel root, int panelY, String prefName, String textFieldText, String description, JComponent additionalInfo){
		int items = additionalInfo != null ? 3 : 2;
		int space = 20/(items+1);
		int basicSize = (root.getWidth()-20) / (1 + items*2);
		int bigSize = 3*basicSize;
		int smallSize = 2*basicSize;
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridLayout(1, additionalInfo != null ? 3 : 2));
		
		JLabel lbl = new JLabel(prefName);
		//lbl.setBounds(space, panelY, smallSize, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		panel.add(lbl);
		
		lbl = new JLabel(textFieldText);
		//lbl.setBounds(smallSize + 2*space, panelY, bigSize, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		panel.add(lbl);
		
		if(additionalInfo != null){
			//additionalInfo.setBounds(smallSize + bigSize + space*3, panelY, smallSize, bigLabelSpacing);
			panel.add(additionalInfo);
		}
		
		root.add(panel);
		
	}

}
