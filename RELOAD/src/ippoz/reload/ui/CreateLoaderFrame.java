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
public class CreateLoaderFrame {
	
	private JFrame lFrame;
	
	private Font bigFont;
	
	private Font labelFont;
	
	private int labelSpacing;
	
	private JPanel createLoaderPanel;
	
	private InputManager iManager;
	
	private PreferencesManager loaderPref;
	
	private Loader loader;
	
	private String tvSplit;
	
	public CreateLoaderFrame(InputManager iManager) {
		this.iManager = iManager;
		
		buildFrame();
		tvSplit = null;
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		
		bigFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelFont = new Font("Times", Font.PLAIN, (int)((14 + rate)/2));
		labelSpacing = (int)(lFrame.getHeight()/26);
		
		createLoaderPanel = buildMainPanel();
	}
	
	private void reload() {
		lFrame.setVisible(false);
		lFrame.getContentPane().removeAll();
		loader = buildLoader();
		createLoaderPanel = buildMainPanel();
		setVisible(true);
	}

	private Loader buildLoader() {
		String loaderType = loaderPref.getPreference(Loader.LOADER_TYPE);
		String runIds = "0 - 999";
		if(loaderType != null && loaderType.toUpperCase().contains("CSV")){
			return new CSVLoader(loaderPref, "train", iManager.getAnomalyWindow(), iManager.getDatasetsFolder(), runIds);
		} else if(loaderType != null && loaderType.toUpperCase().contains("ARFF"))
			return new ARFFLoader(loaderPref, "train", iManager.getAnomalyWindow(), iManager.getDatasetsFolder(), runIds);
		else {
			AppLogger.logError(getClass(), "LoaderError", "Unable to parse loader '" + loaderType + "'");
			return null;
		} 
	}

	public void setVisible(boolean b) {
		if(lFrame != null){
			lFrame.add(createLoaderPanel);
			lFrame.setLocationRelativeTo(null);
			lFrame.setVisible(b);
		}
	}

	private void buildFrame(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		lFrame = new JFrame();
		lFrame.setTitle("Creating new Loader");
		if(screenSize.getWidth() > 1600)
			lFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.4), (int)(screenSize.getHeight()*0.6));
		else lFrame.setBounds(0, 0, 600, 800);
		lFrame.setBackground(Color.WHITE);
	}
	
	private JPanel buildMainPanel() {	
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setLayout(new GridLayout(6, 1));
		containerPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
		
		JPanel namePanel = new JPanel();
		namePanel.setBackground(Color.WHITE);
		namePanel.setLayout(new GridLayout(1, 2, 20, 0));
		namePanel.setBorder(new EmptyBorder(10, 40, 10, 40));
		
		JLabel lbl = new JLabel("Loader Name:");
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Specify name for the new loader. Changing the name will reset setup.");
		namePanel.add(lbl);
		
		JTextField textField = new JTextField();
		textField.setText(loaderPref != null ? loaderPref.getFilename().replace(".loader", "") : "newLoader");
		textField.setFont(labelFont);
		textField.setColumns(10);
		textField.getDocument().addDocumentListener(new DocumentListener() {  
			public void changedUpdate(DocumentEvent e) { workOnUpdate(); }	  
			public void removeUpdate(DocumentEvent e) { workOnUpdate(); }
			public void insertUpdate(DocumentEvent e) { workOnUpdate(); }
			public void workOnUpdate() { loaderPref = null; }  });
		namePanel.add(textField);
		
		containerPanel.add(namePanel);
		
		JPanel filePanel = new JPanel();
		filePanel.setBackground(Color.WHITE);
		filePanel.setEnabled(loaderPref != null);
		filePanel.setLayout(new GridLayout(2, 1, 20, 0));
		
		JPanel innerFilePanel = new JPanel();
		innerFilePanel.setBackground(Color.WHITE);
		innerFilePanel.setLayout(new GridLayout(1, 2, 20, 0));
		
		JButton button = new JButton("Select Main File");
		button.setVisible(true);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				JFileChooser jfc = new JFileChooser(new File(iManager.getDatasetsFolder()).getAbsolutePath());
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
			        Path pathBase = null, pathAbsolute = null;
					try {
						pathAbsolute = Paths.get(selectedFile.getCanonicalPath());
						pathBase = Paths.get(new File(iManager.getDatasetsFolder()).getCanonicalPath());
						if(!selectedFile.isDirectory()){
							if(selectedFile.getName().toUpperCase().endsWith("CSV")){
								loaderPref = iManager.generateDefaultLoaderPreferences(textField.getText(), LoaderType.CSV, pathBase.relativize(pathAbsolute).toString());
							} else if(selectedFile.getName().toUpperCase().endsWith("ARFF")){
								loaderPref = iManager.generateDefaultLoaderPreferences(textField.getText(), LoaderType.ARFF, pathBase.relativize(pathAbsolute).toString());
							} else JOptionPane.showMessageDialog(lFrame, "Type of the file is not supported");
							loader = buildLoader();
							tvSplit = "50";
							updateSplit("50");
							reload();
						} else JOptionPane.showMessageDialog(lFrame, "'" + pathBase.relativize(pathAbsolute).toString() + "' is not a file");
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			} } );
		innerFilePanel.add(button);
		
		lbl = new JLabel(loaderPref != null ? loaderPref.getPreference(Loader.TRAIN_FILE) : "");
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		innerFilePanel.add(lbl);
		
		filePanel.add(innerFilePanel);
		
		lbl = new JLabel("Loader Type: " + (loaderPref != null ? loaderPref.getPreference(Loader.LOADER_TYPE) : ""));
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		filePanel.add(lbl);
		
		containerPanel.add(filePanel); 
		
		JPanel tvPanel = new JPanel();
		tvPanel.setBackground(Color.WHITE);
		tvPanel.setLayout(new GridLayout(1, 2, 20, 0));
		tvPanel.setBorder(new EmptyBorder(10, 40, 10, 40));
		
		lbl = new JLabel("Train-Validation Split:");
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Specify name for the new loader. Changing the name will reset setup.");
		tvPanel.add(lbl);
		
		JTextField tvField = new JTextField();
		tvField.setText(tvSplit != null ? tvSplit : "");
		tvField.setFont(labelFont);
		tvField.setColumns(10);
		tvField.getDocument().addDocumentListener(new DocumentListener() {  
			public void changedUpdate(DocumentEvent e) { workOnUpdate(); }	  
			public void removeUpdate(DocumentEvent e) { workOnUpdate(); }
			public void insertUpdate(DocumentEvent e) { workOnUpdate(); }
			public void workOnUpdate() {
				if(AppUtility.isNumber(tvField.getText()) && loaderPref != null && loader != null){
					updateSplit(tvField.getText());
					tvSplit = tvField.getText();
				}
			}  });
		tvPanel.add(tvField);
		
		containerPanel.add(tvPanel);
		
		JPanel labelPanel = new JPanel();
		labelPanel.setBackground(Color.WHITE);
		labelPanel.setLayout(new GridLayout(1, 2));
		labelPanel.setEnabled(loader != null);
				
		lbl = new JLabel("Label Feature:");
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Set feature  to be used as label.");
		
		labelPanel.add(lbl);
		
		JComboBox<String> cb = new JComboBox<>(loader != null ? loader.getAllFeatureNames() : new String[]{});
		if(loaderPref != null && loaderPref.getPreference(FileLoader.LABEL_COLUMN) != null)
			cb.setSelectedItem(loaderPref.getPreference(FileLoader.LABEL_COLUMN));
		cb.setFont(labelFont);
		cb.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
	        	String newValue = cb.getSelectedItem().toString();
	        	loaderPref.updatePreference(FileLoader.LABEL_COLUMN, newValue, true);
	        	reload();
		    }
		});
		
		labelPanel.add(cb);
		
		containerPanel.add(labelPanel);		
		
		JPanel tagsPanel = new JPanel();
		tagsPanel.setBackground(Color.WHITE);
		tagsPanel.setEnabled(loader != null);
		tagsPanel.setLayout(new GridLayout(1, 2, 20, 0));
		tagsPanel.setBorder(new EmptyBorder(10, 40, 10, 40));
		
		lbl = new JLabel("Tags of Anomalies in '" + (loaderPref != null ? loaderPref.getPreference(FileLoader.LABEL_COLUMN) : "label") + "' Feature:");
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Specify name for the new loader. Changing the name will reset setup.");
		tagsPanel.add(lbl);
		
		JTextField tagsField = new JTextField();
		tagsField.setText(loaderPref != null ? loaderPref.getPreference(Loader.FAULTY_TAGS) : "");
		tagsField.setFont(labelFont);
		tagsField.setColumns(10);
		tagsField.getDocument().addDocumentListener(new DocumentListener() {  
			public void changedUpdate(DocumentEvent e) { workOnUpdate(); }	  
			public void removeUpdate(DocumentEvent e) { workOnUpdate(); }
			public void insertUpdate(DocumentEvent e) { workOnUpdate(); }
			public void workOnUpdate() {
				loaderPref.updatePreference(Loader.FAULTY_TAGS, tagsField.getText(), true);
			}  });
		tagsPanel.add(tagsField);
		
		containerPanel.add(tagsPanel);
		
		JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		fPanel.setLayout(new GridLayout(1, 3, 50, 0));
		fPanel.setBorder(new EmptyBorder(10, 40, 10, 40));
		
		button = new JButton("Save Changes");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				if(checkParameters()){
					loaderPref.updateToFile();
				} else JOptionPane.showMessageDialog(lFrame, "Error while setting parameters",
					    "Error while setting parameters", JOptionPane.ERROR_MESSAGE);
				lFrame.setVisible(false);
			} } );	
		fPanel.add(button);
		
		ImageIcon ii = new ImageIcon(getClass().getResource("/reload.png"));
		button = new JButton("", new ImageIcon(ii.getImage().getScaledInstance(labelSpacing+10, labelSpacing+10, Image.SCALE_DEFAULT)));
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				reload();
			}
		} );
		fPanel.add(button);
		
		button = new JButton("Discard Changes");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				lFrame.setVisible(false);
			} } );	
		fPanel.add(button);
		
		containerPanel.add(fPanel);
		
		return containerPanel;
	}
	
	private void updateSplit(String splitString){
		int n = loader.getRowNumber();
		double split = Double.parseDouble(splitString);
		int splitN = (int) (n * split / 100);
		loaderPref.updatePreference(Loader.TRAIN_PARTITION, "0 - " + (splitN-1), true);
		loaderPref.updatePreference(Loader.VALIDATION_PARTITION, splitN + " - " + (n-1), true);
	}
	
	protected boolean checkParameters() {
		return Loader.isValid(buildLoader());
	}
	
}
