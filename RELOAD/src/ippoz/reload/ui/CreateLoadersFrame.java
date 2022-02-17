/**
 * 
 */
package ippoz.reload.ui;

import ippoz.reload.commons.loader.FileLoader;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.loader.LoaderType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;
import ippoz.reload.manager.InputManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Tommy
 *
 */
public class CreateLoadersFrame {
	
	private JFrame lFrame;
	
	private Font bigFont;
	
	private Font labelFont;
	
	private int labelSpacing;
	
	private JPanel createLoaderPanel;
	
	private InputManager iManager;
	
	private Loader loader;
	
	private double tvSplit;
	
	private File refFile;
	
	private String labelName;
	
	private int trainRows;
	
	private String normalTag;
	
	public CreateLoadersFrame(InputManager iManager) {
		this.iManager = iManager;
		refFile = null;
		labelName = "multilabel";
		trainRows = 10000;
		tvSplit = -1;
		normalTag = "normal";
		
		buildFrame();
		
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		
		bigFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelFont = new Font("Times", Font.PLAIN, (int)((14 + rate)/2));
		labelSpacing = (int)(lFrame.getHeight()/26);
		
		createLoaderPanel = buildMainPanel();
	}
	
	private void reload() {
		lFrame.setVisible(false);
		lFrame.getContentPane().removeAll();
		createLoaderPanel = buildMainPanel();
		setVisible(true);
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
		lFrame.setTitle("Creating new Loaders Automatically");
		if(screenSize.getWidth() > 1600)
			lFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.4), (int)(screenSize.getHeight()*0.5));
		else lFrame.setBounds(0, 0, 600, 800);
		lFrame.setBackground(Color.WHITE);
	}
	
	private JPanel buildMainPanel() {	
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setLayout(new GridLayout(6, 1));
		containerPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
		
		JPanel filePanel = new JPanel();
		filePanel.setBackground(Color.WHITE);
		filePanel.setLayout(new GridLayout(1, 2, 20, 0));
		filePanel.setBorder(new EmptyBorder(10, 40, 10, 40));
		
		JPanel innerFilePanel = new JPanel();
		innerFilePanel.setBackground(Color.WHITE);
		innerFilePanel.setLayout(new GridLayout(1, 2, 0, 0));
		
		JButton button = new JButton("Select Folder or File");
		button.setVisible(true);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				JFileChooser jfc = new JFileChooser(new File(iManager.getDatasetsFolder()).getAbsolutePath());
				jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
			        Path pathBase = null, pathAbsolute = null;
					try {
						pathAbsolute = Paths.get(selectedFile.getCanonicalPath());
						pathBase = Paths.get(new File(iManager.getDatasetsFolder()).getCanonicalPath());
						if(selectedFile.isDirectory() || selectedFile.getName().endsWith(".csv") ||
								selectedFile.getName().endsWith(".arff")){
							refFile = selectedFile;
							reload();
						} else JOptionPane.showMessageDialog(lFrame, "'" + pathBase.relativize(pathAbsolute).toString() + "' is neither a folder nor a valid file");
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
			} } );
		filePanel.add(button);
		
		JLabel lbl = new JLabel(refFile != null ? refFile.getPath() : "not set");
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
		tvField.setText(tvSplit > 0 ? String.valueOf(tvSplit) : "not set");
		tvField.setFont(labelFont);
		tvField.setColumns(10);
		tvField.getDocument().addDocumentListener(new DocumentListener() {  
			public void changedUpdate(DocumentEvent e) { workOnUpdate(); }	  
			public void removeUpdate(DocumentEvent e) { workOnUpdate(); }
			public void insertUpdate(DocumentEvent e) { workOnUpdate(); }
			public void workOnUpdate() {
				if(AppUtility.isNumber(tvField.getText())){
					tvSplit = Double.valueOf(tvField.getText());
				}
			}  });
		tvPanel.add(tvField);
		
		containerPanel.add(tvPanel);
		
		JPanel trPanel = new JPanel();
		trPanel.setBackground(Color.WHITE);
		trPanel.setLayout(new GridLayout(1, 2, 20, 0));
		trPanel.setBorder(new EmptyBorder(10, 40, 10, 40));
		
		lbl = new JLabel("Train Rows:");
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Specify name for the new loader. Changing the name will reset setup.");
		trPanel.add(lbl);
		
		JTextField trField = new JTextField();
		trField.setText(trainRows > 0 ? String.valueOf(trainRows) : "not set");
		trField.setFont(labelFont);
		trField.setColumns(10);
		trField.getDocument().addDocumentListener(new DocumentListener() {  
			public void changedUpdate(DocumentEvent e) { workOnUpdate(); }	  
			public void removeUpdate(DocumentEvent e) { workOnUpdate(); }
			public void insertUpdate(DocumentEvent e) { workOnUpdate(); }
			public void workOnUpdate() {
				if(AppUtility.isInteger(trField.getText()) && Integer.parseInt(trField.getText()) > 0){
					trainRows = Integer.parseInt(trField.getText());
				}
			}  });
		trPanel.add(trField);
		
		containerPanel.add(trPanel);
		
		JPanel labelPanel = new JPanel();
		labelPanel.setBackground(Color.WHITE);
		labelPanel.setLayout(new GridLayout(1, 2, 20, 0));
		labelPanel.setBorder(new EmptyBorder(10, 40, 10, 40));
				
		lbl = new JLabel("Label Feature:");
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Set feature  to be used as label.");
		
		labelPanel.add(lbl);
		
		JTextField labelField = new JTextField();
		labelField.setText(labelName != null ? labelName : "not set");
		labelField.setFont(labelFont);
		labelField.setColumns(10);
		labelField.getDocument().addDocumentListener(new DocumentListener() {  
			public void changedUpdate(DocumentEvent e) { workOnUpdate(); }	  
			public void removeUpdate(DocumentEvent e) { workOnUpdate(); }
			public void insertUpdate(DocumentEvent e) { workOnUpdate(); }
			public void workOnUpdate() {
				if(labelField.getText() != null && labelField.getText().length() > 0){
					labelName = labelField.getText();
				}
			}  });
		labelPanel.add(labelField);
		
		containerPanel.add(labelPanel);		
		
		JPanel tagsPanel = new JPanel();
		tagsPanel.setBackground(Color.WHITE);
		tagsPanel.setEnabled(loader != null);
		tagsPanel.setLayout(new GridLayout(1, 2, 20, 0));
		tagsPanel.setBorder(new EmptyBorder(10, 40, 10, 40));
		
		lbl = new JLabel("Normal Tags in '" + (labelName != null ? labelName : "label") + "' Feature:");
		lbl.setFont(bigFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Specify tag that identifies normal data. Others will be considered anomalies.");
		tagsPanel.add(lbl);
		
		JTextField tagsField = new JTextField();
		tagsField.setText(normalTag != null ? normalTag : "not set");
		tagsField.setFont(labelFont);
		tagsField.setColumns(10);
		tagsField.getDocument().addDocumentListener(new DocumentListener() {  
			public void changedUpdate(DocumentEvent e) { workOnUpdate(); }	  
			public void removeUpdate(DocumentEvent e) { workOnUpdate(); }
			public void insertUpdate(DocumentEvent e) { workOnUpdate(); }
			public void workOnUpdate() {
				if(tagsField.getText() != null && tagsField.getText().length() > 0)
					normalTag = tagsField.getText();
			}  });
		tagsPanel.add(tagsField);
		
		containerPanel.add(tagsPanel);
		
		JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		fPanel.setLayout(new GridLayout(1, 3, 20, 0));
		fPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
		
		button = new JButton("Create Loaders");
		button.setVisible(true);
		button.setFont(new Font(button.getFont().getName(), Font.BOLD, 16));
		button.setEnabled(labelName != null && refFile != null && (tvSplit > 0 || trainRows > 0));
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				List<PreferencesManager> loaders = buildLoaders();
				JOptionPane.showMessageDialog(lFrame, "Loaders Created",
					    "New Loaders: " + loaders.size(), JOptionPane.INFORMATION_MESSAGE);
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
		
		button = new JButton("Exit");
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
	
	private List<PreferencesManager> buildLoaders(){
		File[] candidateLoaders = null;
		List<PreferencesManager> loaders = new LinkedList<>();
		try{
			if(refFile != null){
				if(refFile.isDirectory()){
					candidateLoaders = scanForFiles(refFile);
				} else if(refFile.getName().endsWith(".csv") || refFile.getName().endsWith(".arff"))
					candidateLoaders = new File[]{refFile};
				else candidateLoaders = new File[]{};
				for(File candidate : candidateLoaders){
					PreferencesManager newLoader = createLoader(candidate);
					if(newLoader != null)
						loaders.add(newLoader);
				} 
			}
		} catch(IOException ex){
			
		}
		return loaders;
	}

	private PreferencesManager createLoader(File candidate) throws IOException {
		PreferencesManager loaderPref = null;
		if(candidate != null && candidate.isFile()){
			Path pathAbsolute = Paths.get(candidate.getCanonicalPath());
			Path pathBase = Paths.get(new File(iManager.getDatasetsFolder()).getCanonicalPath());
			String cleanName = candidate.getName().substring(0, candidate.getName().indexOf("."));
			if(candidate.getName().toUpperCase().endsWith(".CSV")){
				loaderPref = iManager.generateDefaultLoaderPreferences(cleanName, LoaderType.CSV, pathBase.relativize(pathAbsolute).toString());
			} else if(candidate.getName().toUpperCase().endsWith(".ARFF")){
				loaderPref = iManager.generateDefaultLoaderPreferences(cleanName, LoaderType.ARFF, pathBase.relativize(pathAbsolute).toString());
			} else {
				AppLogger.logError(getClass(), "LoaderError", "Unable to parse file '" + candidate.getName() + "'");
			}
			if(loaderPref != null){
				loaderPref.updatePreference(FileLoader.LABEL_COLUMN, labelName, true);
				loaderPref.updatePreference(FileLoader.TRAIN_PARTITION, "0 - " + trainRows, true);
				loaderPref.updatePreference(FileLoader.VALIDATION_PARTITION, trainRows + " - 400000", true);
				loaderPref.updatePreference(FileLoader.NORMAL_TAG, normalTag, true, true);
				loaderPref.removePreference(FileLoader.FAULTY_TAGS, true);
			}
		}
		return loaderPref;
	}

	private File[] scanForFiles(File mainFile) {
		List<String> csvFiles = findFiles(Paths.get(mainFile.getAbsolutePath()), ".csv");
		List<String> arffFiles = findFiles(Paths.get(mainFile.getAbsolutePath()), ".arff");
		csvFiles.addAll(arffFiles);
		File[] files = new File[csvFiles.size()];
		for(int i=0; i<csvFiles.size(); i++){
			files[i] = new File(csvFiles.get(i));
		}
		return files;
	}
	
	public static List<String> findFiles(Path path, String fileExtension){
		List<String> result = new LinkedList<>();    
		if (Files.isDirectory(path)) {
	        try (Stream<Path> walk = Files.walk(path)) {
	            result = walk
	                    .filter(p -> !Files.isDirectory(p))
	                    // this is a path, not string,
	                    // this only test if path end with a certain path
	                    //.filter(p -> p.endsWith(fileExtension))
	                    // convert path to string first
	                    .map(p -> p.toString().toLowerCase())
	                    .filter(f -> f.endsWith(fileExtension))
	                    .collect(Collectors.toList());
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	    return result;
	}
	
}
