/**
 * 
 */
package ippoz.reload.ui;

import ippoz.reload.commons.loader.ARFFLoader;
import ippoz.reload.commons.loader.CSVLoader;
import ippoz.reload.commons.loader.FileLoader;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.loader.LoaderType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;
import ippoz.reload.manager.InputManager;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
	
	public LoaderFrame(InputManager iManager, PreferencesManager loaderPref, LoaderType loaderType) {
		this.iManager = iManager;
		this.loaderPref = loaderPref;
		if(loaderPref.hasPreference(Loader.LOADER_TYPE) && loaderPref.getPreference(Loader.LOADER_TYPE).equals("CSVALL"))
			loaderPref.updatePreference(Loader.LOADER_TYPE, "CSV", true);
		
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
		String runIds = null;
		if(loaderTag.equals("train"))
			runIds = loaderPref.getPreference(Loader.TRAIN_PARTITION);
		else runIds = loaderPref.getPreference(Loader.VALIDATION_PARTITION);
		if(loaderType != null && loaderType.toUpperCase().contains("CSV")){
			return new CSVLoader(loaderPref, loaderTag, iManager.getAnomalyWindow(), iManager.getDatasetsFolder(), runIds);
		} else if(loaderType != null && loaderType.toUpperCase().contains("ARFF"))
			return new ARFFLoader(loaderPref, loaderTag, iManager.getAnomalyWindow(), iManager.getDatasetsFolder(), runIds);
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
		containerPanel.setLayout(new GridLayout(4, 1));
		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
		
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

		JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		//fPanel.setBounds(10, generalPanel.getHeight() + sourcePanel.getHeight() + trainPanel.getHeight() + validationPanel.getHeight() + dataPanel.getHeight() + 10, containerPanel.getWidth()-20, 3*labelSpacing);
		fPanel.setLayout(new GridLayout(1, 3, 50, 0));
		fPanel.setBorder(new EmptyBorder(10, 40, 10, 40));
		
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
		
		generalPanel.add(fPanel);
		
		containerPanel.add(generalPanel);
		
		JPanel sourcePanel = new JPanel();
		sourcePanel.setBackground(Color.WHITE);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Sources Setup ", 
				TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		sourcePanel.setBorder(tb);
		sourcePanel.setLayout(new GridLayout(4, 1, 20, 0));
		
		JPanel trainDatasetInfoPanel = buildDatasetInfoPanel(tLoader);
		
		showPreferenceButton(sourcePanel, 1*bigLabelSpacing, FileLoader.TRAIN_FILE, 
				loaderPref.hasPreference(FileLoader.TRAIN_FILE) ? loaderPref.getPreference(FileLoader.TRAIN_FILE) : loaderPref.getPreference("TRAIN_" + loaderPref.getPreference(Loader.LOADER_TYPE) + "_FILE"), 
				"Specify train file path, starting from '" + iManager.getLoaderFolder() + "'", trainDatasetInfoPanel);
		
		JPanel validationDatasetInfoPanel = buildDatasetInfoPanel(vLoader);
		
		showPreferenceButton(sourcePanel, 2*bigLabelSpacing, FileLoader.VALIDATION_FILE, 
				loaderPref.hasPreference(FileLoader.VALIDATION_FILE) ? loaderPref.getPreference(FileLoader.VALIDATION_FILE) : loaderPref.getPreference("VALIDATION_" + loaderPref.getPreference(Loader.LOADER_TYPE) + "_FILE"), 
				"Specify validation file path, starting from '" + iManager.getLoaderFolder() + "'", validationDatasetInfoPanel);

		String[] features = null;
		if(tLoader != null && tLoader.canFetch())
			features = tLoader.getAllFeatureNames();
		else if(vLoader != null && vLoader.canFetch())
			features = vLoader.getAllFeatureNames();
		
		JLabel labelColumnLabel = initLabel("Not Defined");
		if(features != null && features.length > 0)
			labelColumnLabel.setText(features.length + " features");
		
		showCBLabels(sourcePanel, 3*bigLabelSpacing, FileLoader.LABEL_COLUMN, 
				features, loaderPref.getPreference(FileLoader.LABEL_COLUMN), 
				"Specify the name of the column that contains the label", labelColumnLabel);
		
		showPreferenceLabels(sourcePanel, 4*bigLabelSpacing, FileLoader.SKIP_COLUMNS, 
				loaderPref.getPreference(FileLoader.SKIP_COLUMNS), 
				"Define columns (starting from 0) to be skipped by algorithms i.e., non numeric ones, columns containing not-so-useful data.", initLabel(features.length + " features"));
		
		containerPanel.add(sourcePanel);
		
		JPanel trainPanel = new JPanel();
		trainPanel.setBackground(Color.WHITE);
		//trainPanel.setBounds(5, generalPanel.getHeight() + sourcePanel.getHeight() + dataPanel.getHeight() + 15, containerPanel.getWidth()-10, 4*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Train Setup ", 
				TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		trainPanel.setBorder(tb);
		trainPanel.setLayout(new GridLayout(3, 1, 20, 0));
		
		JLabel trainDataPointsLabel = initLabel("Not Defined");
		if(tLoader != null && tLoader.canFetch()){
			if(AppUtility.isNumber(FileLoader.getBatchPreference(loaderPref)))
				trainDataPointsLabel.setText(tLoader.getDataPoints() + " data points");
			else trainDataPointsLabel.setText(tLoader.getDataPoints() + " valid runs");
		}
		
		showPreferenceLabels(trainPanel, 1*bigLabelSpacing, Loader.TRAIN_PARTITION, 
				loaderPref.getPreference(Loader.TRAIN_PARTITION), 
				"Specify runs to be used as training set, either numbers (e.g., 8) or intervals (e.g., 10-15) separated by commas", trainDataPointsLabel);
		
		JLabel trainAnomalyRateLabel = initLabel("Not Defined");
		if(tLoader != null && tLoader.canFetch())
			trainAnomalyRateLabel.setText("Anomaly Rate: " + AppUtility.formatDouble(tLoader.getAnomalyRate()) + "%");
		
		showPreferenceLabels(trainPanel, 2*bigLabelSpacing, FileLoader.TRAIN_FAULTY_TAGS, 
				loaderPref.hasPreference(FileLoader.TRAIN_FAULTY_TAGS) ? loaderPref.getPreference(FileLoader.TRAIN_FAULTY_TAGS) : loaderPref.getPreference("FAULTY_TAGS"), 
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to faulty/attack data for training", trainAnomalyRateLabel);
		
		JLabel trainSkipRateLabel = initLabel("Not Defined");
		if(tLoader != null && tLoader.canFetch())
			trainSkipRateLabel.setText("Skip Rate: " + AppUtility.formatDouble(tLoader.getSkipRate()) + "%");
		
		showPreferenceLabels(trainPanel, 3*bigLabelSpacing, FileLoader.TRAIN_SKIP_ROWS, 
				loaderPref.hasPreference(FileLoader.TRAIN_SKIP_ROWS) ? loaderPref.getPreference(FileLoader.TRAIN_SKIP_ROWS) : loaderPref.getPreference("SKIP_ROWS"), 
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to be skipped i.e., not relevant for the analysis.", trainSkipRateLabel);
			
		containerPanel.add(trainPanel);
		
		JPanel validationPanel = new JPanel();
		validationPanel.setBackground(Color.WHITE);
		//validationPanel.setBounds(5, generalPanel.getHeight() + 20 + trainPanel.getHeight() + sourcePanel.getHeight() + dataPanel.getHeight(), containerPanel.getWidth()-10, 4*bigLabelSpacing + 10);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Validation Setup ", 
				TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 18), Color.DARK_GRAY);
		validationPanel.setBorder(tb);
		validationPanel.setLayout(new GridLayout(3, 1, 20, 0));
		
		JLabel validationDataPointsLabel = initLabel("Not Defined");
		if(vLoader != null && vLoader.canFetch()){
			if(AppUtility.isNumber(FileLoader.getBatchPreference(loaderPref)))
				validationDataPointsLabel.setText(vLoader.getDataPoints() + " data points");
			else validationDataPointsLabel.setText(vLoader.getDataPoints() + " valid runs");
		}
		
		showPreferenceLabels(validationPanel, 1*bigLabelSpacing, Loader.VALIDATION_PARTITION, 
				loaderPref.getPreference(Loader.VALIDATION_PARTITION), 
				"Specify runs to be used as validation set, either numbers (e.g., 8) or intervals (e.g., 10-15) separated by commas", validationDataPointsLabel);
		
		JLabel validationAnomalyRateLabel = initLabel("Not Defined");
		if(vLoader != null && vLoader.canFetch())
			validationAnomalyRateLabel.setText("Anomaly Rate: " + AppUtility.formatDouble(vLoader.getAnomalyRate()) + "%");
		
		showPreferenceLabels(validationPanel, 2*bigLabelSpacing, FileLoader.VALIDATION_FAULTY_TAGS, 
				loaderPref.hasPreference(FileLoader.VALIDATION_FAULTY_TAGS) ? loaderPref.getPreference(FileLoader.VALIDATION_FAULTY_TAGS) : loaderPref.getPreference("FAULTY_TAGS"),  
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to faulty/attack data for validation", validationAnomalyRateLabel);
		
		JLabel validationSkipRateLabel = initLabel("Not Defined");
		if(vLoader != null && vLoader.canFetch())
			validationSkipRateLabel.setText("Skip Rate: " + AppUtility.formatDouble(vLoader.getSkipRate()) + "%");
		
		showPreferenceLabels(validationPanel, 3*bigLabelSpacing, FileLoader.VALIDATION_SKIP_ROWS, 
				loaderPref.hasPreference(FileLoader.VALIDATION_SKIP_ROWS) ? loaderPref.getPreference(FileLoader.VALIDATION_SKIP_ROWS) : loaderPref.getPreference("SKIP_ROWS"), 
				"Specify the label(s) of 'LABEL_COLUMN' that identify rows related to be skipped i.e., not relevant for the analysis.", validationSkipRateLabel);
		
		containerPanel.add(validationPanel);
		
		/*if(lFrame.getHeight() < generalPanel.getHeight() + sourcePanel.getHeight() +  trainPanel.getHeight() + validationPanel.getHeight() + dataPanel.getHeight() + fPanel.getHeight() + 70)
			lFrame.setBounds(lFrame.getX(), lFrame.getY(), lFrame.getWidth(), generalPanel.getHeight() + sourcePanel.getHeight() +  trainPanel.getHeight() + validationPanel.getHeight() + dataPanel.getHeight() + fPanel.getHeight() + 70);
        */
		return containerPanel;
	}
	
	protected String checkParameters() {
		String output = "";
		String prefString;
		prefString = loaderPref.hasPreference(FileLoader.TRAIN_FILE) ? FileLoader.TRAIN_FILE : "TRAIN_" + loaderPref.getPreference(Loader.LOADER_TYPE) + "_FILE";
		if(!loaderPref.hasPreference(prefString) || 
				loaderPref.getPreference(prefString).trim().length() == 0){
			output = output + "Wrong TRAIN_FILE value: remember to specify file for training.\n";
		} else if(!new File(iManager.getDatasetsFolder() + loaderPref.getPreference(prefString)).exists()){
			output = output + "TRAIN_FILE (" + (iManager.getDatasetsFolder() + loaderPref.getPreference(prefString)) +  ") does not exist.\n";
		}
		prefString = loaderPref.hasPreference(FileLoader.VALIDATION_FILE) ? FileLoader.VALIDATION_FILE : "VALIDATION_" + loaderPref.getPreference(Loader.LOADER_TYPE) + "_FILE";
		if(!loaderPref.hasPreference(prefString) || 
				loaderPref.getPreference(prefString).trim().length() == 0){
			output = output + "Wrong VALIDATION_FILE value: remember to specify file for validation.\n";
		} else if(!new File(iManager.getDatasetsFolder() + loaderPref.getPreference(prefString)).exists()){
			output = output + "VALIDATION_FILE (" + (iManager.getDatasetsFolder() + loaderPref.getPreference(prefString)) +  ") does not exist.\n";
		}
		if(!loaderPref.hasPreference(FileLoader.TRAIN_PARTITION) || 
				loaderPref.getPreference(FileLoader.TRAIN_PARTITION).trim().length() == 0){
			output = output + "Wrong TRAIN_RUN_PREFERENCE value: remember to specify runs for training.\n";
		} else {
			tLoader = buildLoader("train");
			if(!Loader.isValid(tLoader))
				output = output + "Portion specified for training is either empty or does not specify a mixture of normal/anomaly data that is needed to tune parameters of algorithms.\n";
		}
		if(!loaderPref.hasPreference(FileLoader.VALIDATION_PARTITION) || 
				loaderPref.getPreference(FileLoader.VALIDATION_PARTITION).trim().length() == 0){
			output = output + "Wrong VALIDATION_RUN_PREFERENCE value: remember to specify runs for validation.\n";
		} else {
			vLoader = buildLoader("validation");
			if(!Loader.isValid(vLoader))
				output = output + "Portion specified for training is either empty or does not specify a mixture of normal/anomaly data that is needed to correctly calculate confusion matrix.\n";
		}
		return output.trim().length() > 0 ? output : null;
	}
	
	private void showPreferenceLabels(JPanel root, int panelY, String prefName, String textFieldText, String description, JComponent additionalInfo){
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
	        	loaderPref.updatePreference(prefName, textField.getText(), true, false);
			}
		});
		
		panel.add(textField);
		
		if(additionalInfo != null){
			//additionalInfo.setBounds(smallSize + bigSize + space*3, panelY, smallSize, bigLabelSpacing);
			panel.add(additionalInfo);
		}
		
		root.add(panel);
		
	}
	
	private void showCBLabels(JPanel root, int panelY, String prefName, String[] items, String selectedItem, String description, JComponent additionalInfo){
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
		
		JComboBox<String> cb = new JComboBox<>(items);
		cb.setSelectedItem(selectedItem);
		//textField.setBounds(smallSize + space*2, panelY, bigSize, bigLabelSpacing);
		cb.setFont(labelFont);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		cb.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
	        	String newValue = cb.getSelectedItem().toString();
	        	loaderPref.updatePreference(prefName, newValue, true, false);
		    }
		});
		
		panel.add(cb);
		
		if(additionalInfo != null){
			//additionalInfo.setBounds(smallSize + bigSize + space*3, panelY, smallSize, bigLabelSpacing);
			panel.add(additionalInfo);
		}
		
		root.add(panel);
		
	}
	
	private void showPreferenceButton(JPanel root, int panelY, String prefName, String textFieldText, String description, JComponent additionalInfo){
		int items = additionalInfo != null ? 3 : 2;
		int space = 20/(items+1);
		int basicSize = (root.getWidth()-20) / (1 + items*2);
		int bigSize = 3*basicSize;
		int smallSize = 2*basicSize;
		
		JPanel newPanel = new JPanel();
		newPanel.setBackground(Color.WHITE);
		newPanel.setLayout(new GridLayout(1, additionalInfo != null ? 3 : 2));
		
		JLabel lbl = new JLabel(prefName);
		lbl.setBounds(space, panelY, smallSize, labelSpacing);
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(description != null && description.trim().length() > 0)
			lbl.setToolTipText(description);
		
		newPanel.add(lbl);
		
		JButton button = new JButton(textFieldText);
		button.setVisible(true);
		button.setBounds(smallSize + 2*space, panelY, bigSize, bigLabelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				JFileChooser jfc = new JFileChooser(new File(iManager.getDatasetsFolder()).getAbsolutePath());
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					Path pathAbsolute = Paths.get(selectedFile.getAbsolutePath());
			        Path pathBase = Paths.get(new File(iManager.getDatasetsFolder()).getAbsolutePath());
					if(!selectedFile.isDirectory()){
						button.setText(pathBase.relativize(pathAbsolute).toString());
						loaderPref.updatePreference(prefName, pathBase.relativize(pathAbsolute).toString(), true, false);
					} else JOptionPane.showMessageDialog(lFrame, "'" + pathBase.relativize(pathAbsolute).toString() + "' is not a file");
				}
			} } );
		newPanel.add(button);		
		
		if(additionalInfo != null){
			additionalInfo.setBounds(smallSize + bigSize + space*3, panelY, smallSize, bigLabelSpacing);
			newPanel.add(additionalInfo);
		}
		
		root.add(newPanel);
	}
	
	private void showPreference2Labels(JPanel root, int panelY, String prefName, String textFieldText, String description, JComponent additionalInfo){
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridLayout(1, additionalInfo != null ? 3 : 2, 20, 20));
		panel.setBorder(new EmptyBorder(10, 40, 10, 40));
		
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
	
	private JPanel buildDatasetInfoPanel(Loader loader){
		JPanel datasetInfoPanel = new JPanel();
		datasetInfoPanel.setBackground(Color.WHITE);
		datasetInfoPanel.setLayout(new GridBagLayout());
		
		JLabel trainDatasetLabel = initLabel("Not Defined");
		if(loader != null && loader.canFetch())
			trainDatasetLabel.setText("Size: " + tLoader.getMBSize() + " MB, " + loader.getRowNumber() + " rows");
		datasetInfoPanel.add(trainDatasetLabel);
		
		ImageIcon it = new ImageIcon(getClass().getResource("/exploreDataset.png"));
		JButton button = new JButton("", new ImageIcon(it.getImage().getScaledInstance(labelSpacing + 5, labelSpacing + 5, Image.SCALE_DEFAULT)));
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setContentAreaFilled(false);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				DatasetInfoFrame dif = new DatasetInfoFrame(loader);
				dif.setVisible(true);
			}
		} );
		datasetInfoPanel.add(button);
		
		GridBagConstraints c = new GridBagConstraints();
	    c.insets = new Insets(2, 2, 2, 2);
	    c.weighty = 1.0;
	    c.weightx = 1.0;
	    c.gridx = 0;
	    c.gridy = 0;
	    c.gridheight = 2;
	    c.fill = GridBagConstraints.BOTH; // Use both horizontal & vertical
	    datasetInfoPanel.add(trainDatasetLabel, c);
	    c.gridx = 1;
	    c.gridheight = 1;
	    c.gridwidth = 2;
	    c.fill = GridBagConstraints.HORIZONTAL; // Horizontal only
	    datasetInfoPanel.add(button, c);
	    c.gridy = 1;
	    c.gridwidth = 1;
	    c.fill = GridBagConstraints.NONE; // Remember to reset to none
	    
	    return datasetInfoPanel;
	}

}
