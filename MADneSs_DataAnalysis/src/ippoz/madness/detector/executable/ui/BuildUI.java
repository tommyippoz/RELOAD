/**
 * 
 */
package ippoz.madness.detector.executable.ui;

import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.commons.algorithm.AlgorithmFamily;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.knowledge.sliding.SlidingPolicy;
import ippoz.madness.detector.commons.knowledge.sliding.SlidingPolicyType;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.AppUtility;
import ippoz.madness.detector.commons.support.PreferencesManager;
import ippoz.madness.detector.executable.DetectorMain;
import ippoz.madness.detector.loader.CSVPreLoader;
import ippoz.madness.detector.loader.Loader;
import ippoz.madness.detector.loader.MySQLLoader;
import ippoz.madness.detector.manager.DetectionManager;
import ippoz.madness.detector.manager.InputManager;
import ippoz.madness.detector.metric.MetricType;
import ippoz.madness.detector.output.DetectorOutput;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
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
public class BuildUI {
	
	private static final String SETUP_LABEL_PREFFILE = "Preferences File";
	
	private static final String SETUP_LABEL_METRIC = "Target Metric";
	
	private static final String SETUP_LABEL_OUTPUT = "Output Format";
	
	private static final String SETUP_IND_SELECTION = "Indicators Selection";
	
	private static final String SETUP_LABEL_FILTERING = "Filtering";
	
	private static final String SETUP_LABEL_FILTERING_THRESHOLD = "FPR Threshold";
	
	private static final String SETUP_LABEL_TRAINING = "Training";
	
	private static final String SETUP_KFOLD_VALIDATION = "K-Fold Cross Validation";
	
	private static final String SETUP_LABEL_SLIDING_POLICY = "Sliding Policy";
	
	private static final String SETUP_LABEL_WINDOW_SIZE = "Window Size";
	
	private static final String PATH_LABEL_INPUT_FOLDER = "Input Folder";
	
	private static final String PATH_LABEL_OUTPUT_FOLDER = "Output Folder";
	
	private static final String PATH_LABEL_CONF_FOLDER = "Configuration Folder";
	
	private static final String PATH_LABEL_SETUP_FOLDER = "Setup Folder";
	
	private static final String PATH_LABEL_SCORES_FOLDER = "Scores Folder";
	
	private static final String PATH_LABEL_DETECTION_PREFERENCES = "Detection Preferences";
	
	private JPanel headerPanel, setupPanel, pathPanel, dataAlgPanel, footerPanel;
	
	private Map<String, JPanel> setupMap, pathMap;

	private JFrame frame;
	
	private InputManager iManager;
	
	private boolean isUpdating;
	
	private Font bigFont;
	
	private Font labelFont;
	
	private Font smallLabelFont;
	
	private static Font titleFont = new Font("Times", Font.BOLD, 20);
	
	private int labelSpacing;
	
	private int bigLabelSpacing;

	public BuildUI(InputManager iManager){
		this.iManager = iManager;
		isUpdating = true;
		setupMap = new HashMap<String, JPanel>();
		pathMap = new HashMap<String, JPanel>();
		buildFrame();
		isUpdating = false;
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		
		bigFont = new Font("Times", Font.BOLD, (int)((18 + rate)/2));
		labelFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		smallLabelFont = new Font("Times", Font.PLAIN, (int)((14 + rate)/2));
		
		labelSpacing = (int)(frame.getHeight()/25);
		bigLabelSpacing = (int)(frame.getHeight()/18);
		
		//labelSpacing = (int)(660/25);
		//bigLabelSpacing = (int)(660/18);
	}
	
	private void buildFrame(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		AppLogger.logInfo(getClass(), "Screen dimension is " + screenSize.toString());
		frame = new JFrame();
		frame.setTitle("RELOAD Framework");
		frame.setIconImage(new ImageIcon(getClass().getResource("/RELOAD_Transparent.png")).getImage());
		if(screenSize.getWidth() > 1000)
			frame.setBounds(0, 0, (int)(screenSize.getWidth()*0.8), (int)(screenSize.getHeight()*0.65));
		else frame.setBounds(0, 0, 1280, 660);
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setLayout(null);
	}
	
	private void reload() {
		isUpdating = true;
		frame.setVisible(false);
		frame.getContentPane().removeAll();
		setupMap = new HashMap<String, JPanel>();
		pathMap = new HashMap<String, JPanel>();
		frame = buildJFrame();
		frame.setVisible(true);
		isUpdating = false;
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	private String panelToPreference(String textName) {
		switch(textName){
			case SETUP_LABEL_PREFFILE:
				return null;
			case SETUP_LABEL_METRIC:
				return InputManager.METRIC;
			case SETUP_LABEL_OUTPUT:
				return InputManager.OUTPUT_FORMAT;
			case SETUP_LABEL_FILTERING:
				return InputManager.FILTERING_NEEDED_FLAG;
			case SETUP_LABEL_FILTERING_THRESHOLD:
				return InputManager.FILTERING_TRESHOLD;
			case SETUP_LABEL_TRAINING:
				return InputManager.TRAIN_NEEDED_FLAG;
			case SETUP_LABEL_SLIDING_POLICY:
				return InputManager.SLIDING_POLICY;
			case SETUP_LABEL_WINDOW_SIZE:
				return InputManager.SLIDING_WINDOW_SIZE;
			case PATH_LABEL_INPUT_FOLDER:
				return InputManager.INPUT_FOLDER;
			case PATH_LABEL_OUTPUT_FOLDER:
				return InputManager.OUTPUT_FOLDER;
			case PATH_LABEL_CONF_FOLDER:
				return InputManager.CONF_FILE_FOLDER;
			case PATH_LABEL_SETUP_FOLDER:
				return InputManager.SETUP_FILE_FOLDER;
			case PATH_LABEL_SCORES_FOLDER:
				return InputManager.SCORES_FILE_FOLDER;
			case PATH_LABEL_DETECTION_PREFERENCES:
				return InputManager.DETECTION_PREFERENCES_FILE;
		}
		return null;
	}
	
	public JFrame buildJFrame(){
		
		isUpdating = true;
		
		headerPanel = new JPanel();
		frame.getContentPane().add(buildHeaderTab());
		
		setupPanel = new JPanel();
		frame.getContentPane().add(buildSetupTab(headerPanel.getHeight() + labelSpacing/2));
		
		pathPanel = new JPanel();
		frame.getContentPane().add(buildPathsTab(headerPanel.getHeight() + labelSpacing/2));
		
		dataAlgPanel = new JPanel();
		frame.getContentPane().add(buildAlgorithmsDatasetsTab(headerPanel.getHeight() + labelSpacing/2));
		
		footerPanel = new JPanel();
		frame.getContentPane().add(buildFooterTab(headerPanel.getHeight() + Math.max(setupPanel.getHeight(), Math.max(pathPanel.getHeight(), dataAlgPanel.getHeight())) + labelSpacing));
		
		frame.setBounds(0, 0, frame.getWidth(), headerPanel.getHeight() + Math.max(setupPanel.getHeight(), Math.max(pathPanel.getHeight(), dataAlgPanel.getHeight())) + footerPanel.getHeight());
		frame.setLocationRelativeTo(null);
		
		isUpdating = false;
		
		return frame;
	}
	
	private JPanel buildHeaderTab(){
		headerPanel.setBackground(Color.WHITE);
		headerPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight()/5);
		headerPanel.setLayout(null);
		ImageIcon ii = new ImageIcon(getClass().getResource("/RELOAD_Transparent.png"));
		JLabel lblMadness = new JLabel(new ImageIcon(ii.getImage().getScaledInstance(320, 125, Image.SCALE_DEFAULT)));
		lblMadness.setBounds(0, 10, frame.getWidth(), frame.getHeight()/6);
		lblMadness.setHorizontalAlignment(SwingConstants.CENTER);
		headerPanel.add(lblMadness);
		
		return headerPanel;
	}
	
	private JPanel buildFooterTab(int tabY){
		footerPanel.setBackground(Color.WHITE);
		footerPanel.setBounds(frame.getWidth()/10, tabY, frame.getWidth()*4/5, (int) (frame.getHeight()*0.25));
		footerPanel.setLayout(null);
		
		ImageIcon ii = new ImageIcon(getClass().getResource("/reload.png"));
		JButton button = new JButton("", new ImageIcon(ii.getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT)));
		button.setBounds(footerPanel.getWidth()*2/5, 0, footerPanel.getHeight()*2/5, footerPanel.getHeight()*2/5);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				reload();
			}
		} );
		footerPanel.add(button);
		
		button = new JButton("RELOAD!");
		button.setBounds(footerPanel.getWidth()/2, 0, footerPanel.getWidth()/6, footerPanel.getHeight()*2/5);
		button.setFont(bigFont);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				runExperiments();
			} } );
		footerPanel.add(button);
		
		JLabel lblFooter = new JLabel("Authors' Information and References");
		lblFooter.setBounds(0, footerPanel.getHeight()/3, footerPanel.getWidth(), footerPanel.getHeight()/5);
		lblFooter.setFont(labelFont);
		lblFooter.setHorizontalAlignment(SwingConstants.RIGHT);
		lblFooter.addMouseListener(new MouseAdapter()  
		{  
		    public void mouseClicked(MouseEvent e)  
		    {  
		    	JOptionPane.showMessageDialog(frame, "Rapid Evaluation of Anomaly Detectors (RELOAD) Framework\n"
		    			+ "For further information, please refer to the Resilient Computing Lab @ University of Florence, Italy\n"
		    			+ "Website: http://rcl.dsi.unifi.it/");
		    }  
		}); 
		footerPanel.add(lblFooter);
		
		return footerPanel;
	}

	protected void runExperiments() {
		ProgressBar pBar = new ProgressBar(frame, "Experiments Progress", 0, DetectorMain.getMADneSsIterations(iManager));
		new Thread(new Runnable() {
			public void run() {
				pBar.showBar();
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				List<DetectionManager> dmList;
				try {
					dmList = new LinkedList<DetectionManager>();
					for(PreferencesManager loaderPref : iManager.readLoaders()){
						for(List<AlgorithmType> aList : DetectorMain.readAlgorithmCombinations(iManager)){
							if(DetectorMain.hasSliding(aList)){
								for(Integer windowSize : DetectorMain.readWindowSizes(iManager)){
									for(SlidingPolicy sPolicy : DetectorMain.readSlidingPolicies(iManager)){
										dmList.add(new DetectionManager(iManager, aList, loaderPref, windowSize, sPolicy));
									}
								}
							} else {
								dmList.add(new DetectionManager(iManager, aList, loaderPref));
							}
						}
					}
					AppLogger.logInfo(DetectorMain.class, dmList.size() + " RELOAD instances found.");
					List<DetectorOutput> outList = new ArrayList<DetectorOutput>(dmList.size());
					for(int i=0;i<dmList.size();i++){
						AppLogger.logInfo(DetectorMain.class, "Running RELOAD [" + (i+1) + "/" + dmList.size() + "]: '" + dmList.get(i).getTag() + "'");
						outList.add(DetectorMain.runMADneSs(dmList.get(i)));
						pBar.moveNext();
					}
					pBar.deleteFrame();
					showDetectorOutputs(outList);
				} catch(Exception ex) {
					AppLogger.logException(getClass(), ex, "");
				}
			}
		}).start();
	}
	
	private void printOptions(JPanel panel, String[] options, int fromX, int fromY, int space){
		JLabel lbl;
		JButton jb;
		int i = 0;
		int buttonsSpace = (int)(space*1.3);
		if(options != null){
			for(String option : options){
				lbl = new JLabel(option);
				lbl.setBounds(fromX, fromY + i*space, panel.getWidth() - fromX - 3*buttonsSpace, space);
				lbl.setFont(smallLabelFont);
				lbl.setHorizontalAlignment(SwingConstants.CENTER);
				panel.add(lbl);
				
				jb = new JButton("#");
				jb.setBounds(panel.getWidth() - fromX - 2*buttonsSpace, fromY + i*space, buttonsSpace, space);
				jb.setHorizontalAlignment(SwingConstants.CENTER);
				jb.addActionListener(new ActionListener() { 
					public void actionPerformed(ActionEvent e) { 
						if(!option.contains(".")) {
							AlgorithmSetupFrame asf;
							String algName = option.split(" ")[0];
							try {
								AlgorithmType at = AlgorithmType.valueOf(algName);
								asf = new AlgorithmSetupFrame(iManager, at, iManager.loadConfiguration(at, 0, SlidingPolicy.getPolicy(SlidingPolicyType.FIFO)).get(at));
								asf.setVisible(true);
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to open algorithm '" + algName + "' preferences");
							}
						} else {
							LoaderFrame lf;
							String a = option.split("-")[1].trim();
							String b = a.split(" ")[0];
							try {
								lf = new LoaderFrame(iManager, iManager.getLoaderPreferencesByName(b));
								lf.setVisible(true);
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to open dataset '" + b + "' preferences");
							}
						}
						
					} } );
				panel.add(jb);
					
				jb = new JButton("-");
				jb.setBounds(panel.getWidth() - fromX - buttonsSpace, fromY + i*space, buttonsSpace, space);
				jb.setHorizontalAlignment(SwingConstants.CENTER);
				jb.addActionListener(new ActionListener() { 
					public void actionPerformed(ActionEvent e) { 
						if(option.contains(".")){
							iManager.removeDataset(option);
						} else {
							iManager.removeAlgorithm(option);
						}
						reload();
					} } );
				panel.add(jb);
				i++;
			}
		}
	}
	
	private void addToPanel(JPanel root, String tag, JPanel panel, Map<String, JPanel> refMap){
		panel.setBackground(Color.WHITE);
		root.add(panel);
		refMap.put(tag, panel);
	}
	
	private JPanel buildAlgorithmsDatasetsTab(int tabY){
		
		dataAlgPanel.setBackground(Color.WHITE);
		
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Data Analysis", TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY);
		dataAlgPanel.setBounds(frame.getWidth()*2/3 + 10, tabY, frame.getWidth()/3 - 20, frame.getHeight()/8 + labelSpacing*(getDatasets().length + getAlgorithms().length) + 2*bigLabelSpacing);
		dataAlgPanel.setBorder(tb);
		dataAlgPanel.setLayout(null);
		
		JLabel mainLabel = new JLabel("Loaders");
		mainLabel.setBounds(dataAlgPanel.getWidth()/4, labelSpacing, dataAlgPanel.getWidth()/2, labelSpacing);
		mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainLabel.setFont(titleFont);
		dataAlgPanel.add(mainLabel);
		
		printOptions(dataAlgPanel, getDatasets(), dataAlgPanel.getWidth()/30, 2*labelSpacing, labelSpacing);
		
		JPanel seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setBounds((int) (dataAlgPanel.getWidth()*0.02), (int)(labelSpacing*(getDatasets().length + 1) + bigLabelSpacing), (int) (dataAlgPanel.getWidth()*0.96), bigLabelSpacing);
		
		JButton button = new JButton("Create Loader");
		button.setVisible(true);
		button.setFont(labelFont);
		button.setBounds(labelSpacing, 0, pathPanel.getWidth()/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				LoaderFrame lf;
				String loaderName = null;
				String s = (String)JOptionPane.showInputDialog(
	                    frame, "Set name for the new loader", "Create Loader",
	                    JOptionPane.PLAIN_MESSAGE, null, null, "");
				if ((s != null) && (s.trim().length() > 0)) {
					loaderName = s.trim();
				} else {
					loaderName = "newLoader";
					AppLogger.logError(getClass(), "WrongLoaderFilename", "Loader name unspecified. Using default 'newLoader.loader'");;
				}
				loaderName = loaderName + ".loader";
				try {
					lf = new LoaderFrame(iManager, iManager.generateDefaultLoaderPreferences(loaderName));
					lf.setVisible(true);
				} catch(Exception ex){
					AppLogger.logException(getClass(), ex, "Unable to create loader '" + loaderName + "' preferences");
				}
				
			} } );
		seePrefPanel.add(button);
		
		button = new JButton("Add Loader");
		button.setVisible(true);
		button.setFont(labelFont);
		button.setBounds(labelSpacing, 0, pathPanel.getWidth()/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				JFileChooser jfc = new JFileChooser(new File(iManager.getLoaderFolder()).getAbsolutePath());
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					Path pathAbsolute = Paths.get(selectedFile.getAbsolutePath());
			        Path pathBase = Paths.get(new File(iManager.getLoaderFolder()).getAbsolutePath());
					if(!selectedFile.isDirectory() && selectedFile.getName().endsWith(".loader")){
						iManager.addDataset(pathBase.relativize(pathAbsolute).toString());
						reload();
					} else JOptionPane.showMessageDialog(frame, "'" + pathBase.relativize(pathAbsolute).toString() + "' is not a '.loader' file");
				}
			} } );
		
		seePrefPanel.add(button);
		button = new JButton("See Loaders");
		button.setVisible(true);
		button.setFont(labelFont);
		button.setBounds(0, 0, pathPanel.getWidth()/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				try {
					Desktop.getDesktop().open(new File(iManager.getSetupFolder() + "loaderPreferences.preferences"));
				} catch (IOException e1) {
					AppLogger.logException(getClass(), e1, "");
				}
			} } );
		seePrefPanel.add(button);
		
		dataAlgPanel.add(seePrefPanel);
		
		tabY = labelSpacing*(getDatasets().length) + 2*bigLabelSpacing + seePrefPanel.getHeight();
		
		mainLabel = new JLabel("Algorithms");
		mainLabel.setBounds(dataAlgPanel.getWidth()/4, tabY, dataAlgPanel.getWidth()/2, labelSpacing);
		mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainLabel.setFont(titleFont);
		dataAlgPanel.add(mainLabel);
		
		tabY = tabY + labelSpacing;
		
		printOptions(dataAlgPanel, getAlgorithms(), dataAlgPanel.getWidth()/20, tabY, labelSpacing);
		
		tabY = tabY + (getAlgorithms().length)*labelSpacing;
		
		seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setBounds((int) (dataAlgPanel.getWidth()*0.02), tabY, (int) (dataAlgPanel.getWidth()*0.96), bigLabelSpacing);
		
		button = new JButton("Add Algorithm");
		button.setVisible(true);
		button.setFont(new Font(pathPanel.getFont().getName(), Font.PLAIN, 16));
		button.setBounds(25, 0, pathPanel.getWidth()/5, 25);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				Object[] possibilities = new String[AlgorithmType.values().length];
				int i = 0;
				for(AlgorithmType at : AlgorithmType.values()){
					if(at != AlgorithmType.RCC && at != AlgorithmType.HIST && at != AlgorithmType.CONF 
						&& at != AlgorithmType.PEA && at != AlgorithmType.INV && at != AlgorithmType.WER
						&& at != AlgorithmType.TEST && !Arrays.asList(getAlgorithms()).contains(at.toString()))
					possibilities[i++] = at.toString();
				}
				String returnValue = (String)JOptionPane.showInputDialog(
				                    frame, "Choose an Algorithm", "Add Algorithm",
				                    JOptionPane.PLAIN_MESSAGE, null, possibilities, "");
				if (returnValue != null && returnValue.length() > 0) {
				    iManager.addAlgorithm(returnValue);
				    reload();
				}
			} } );
		seePrefPanel.add(button);
		button = new JButton("Open Algorithms");
		button.setVisible(true);
		button.setFont(new Font(pathPanel.getFont().getName(), Font.PLAIN, 16));
		button.setBounds(0, 0, pathPanel.getWidth()/5, 30);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				try {
					Desktop.getDesktop().open(new File(iManager.getSetupFolder() + "algorithmPreferences.preferences"));
				} catch (IOException e1) {
					AppLogger.logException(getClass(), e1, "");
				}
			} } );
		seePrefPanel.add(button);
		dataAlgPanel.add(seePrefPanel);
		
		tabY = tabY + seePrefPanel.getHeight() + labelSpacing;
		
		dataAlgPanel.setBounds(dataAlgPanel.getX(), dataAlgPanel.getY(), dataAlgPanel.getWidth(), tabY);
		
		return dataAlgPanel;
	}
	
	private String[] getDatasets() {
		int i = 0;
		List<PreferencesManager> lList = iManager.readLoaders();
		String[] dsStrings = new String[lList.size()];
		for(PreferencesManager lPref : lList){
			if(lPref.getPreference(Loader.LOADER_TYPE).equals("MYSQL"))
				dsStrings[i++] = "MySQL - " + lPref.getPreference(MySQLLoader.DB_NAME);
			else {
				dsStrings[i++] = "CSV - " + lPref.getFilename() + " (" + lPref.getPreference(CSVPreLoader.TRAIN_CSV_FILE) + ")";
			}
		}
		return dsStrings;
	}

	private String[] getAlgorithms(){
		int i = 0;
		AlgorithmFamily family;
		List<List<AlgorithmType>> aComb = DetectorMain.readAlgorithmCombinations(iManager);
		String[] algStrings = new String[aComb.size()];
		for(List<AlgorithmType> aList : aComb){
			try {
				family = DetectionAlgorithm.getFamily(AlgorithmType.valueOf(aList.toString().substring(1, aList.toString().length()-1)));
			} catch(Exception ex){
				family = AlgorithmFamily.MIXED;
			}
			algStrings[i++] = aList.toString().substring(1, aList.toString().length()-1) + " (" + family + ")";
		}
		return algStrings;
	}
	
	private JPanel buildPathsTab(int tabY){
		pathPanel.setBackground(Color.WHITE);
		
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Paths", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY);
		pathPanel.setBounds(frame.getWidth()/3 + 10, tabY, frame.getWidth()/3 - 20, 7*bigLabelSpacing + 2*labelSpacing);
		pathPanel.setBorder(tb);
		pathPanel.setLayout(null);
		
		addToPanel(pathPanel, PATH_LABEL_INPUT_FOLDER, createFCHPanel(PATH_LABEL_INPUT_FOLDER, pathPanel, bigLabelSpacing, iManager.getInputFolder(), true), pathMap);
		addToPanel(pathPanel, PATH_LABEL_OUTPUT_FOLDER, createFCHPanel(PATH_LABEL_OUTPUT_FOLDER, pathPanel, 2*bigLabelSpacing, iManager.getOutputFolder(), true), pathMap);
		addToPanel(pathPanel, PATH_LABEL_CONF_FOLDER, createFCHPanel(PATH_LABEL_CONF_FOLDER, pathPanel, 3*bigLabelSpacing, iManager.getConfigurationFolder(), true), pathMap);
		addToPanel(pathPanel, PATH_LABEL_SETUP_FOLDER, createFCHPanel(PATH_LABEL_SETUP_FOLDER, pathPanel, 4*bigLabelSpacing, iManager.getSetupFolder(), true), pathMap);
		addToPanel(pathPanel, PATH_LABEL_SCORES_FOLDER, createFCHPanel(PATH_LABEL_SCORES_FOLDER, pathPanel, 5*bigLabelSpacing, iManager.getScoresFolder(), true), pathMap);
		addToPanel(pathPanel, PATH_LABEL_DETECTION_PREFERENCES, createFCHPanel(PATH_LABEL_DETECTION_PREFERENCES, pathPanel, 6*bigLabelSpacing, iManager.getDetectionPreferencesFile(), false), pathMap);
		
		JPanel seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setBounds((int) (setupPanel.getWidth()*0.02), 7*bigLabelSpacing, (int)(setupPanel.getWidth()*0.96), bigLabelSpacing);
		
		JButton button = new JButton("Open Scoring Preferences");
		button.setVisible(true);
		button.setFont(labelFont);
		button.setBounds(0, 0, pathPanel.getWidth()*2/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				try {
					Desktop.getDesktop().open(new File(iManager.getInputFolder() + iManager.getDetectionPreferencesFile()));
				} catch (IOException e1) {
					AppLogger.logException(getClass(), e1, "");
				}
			} } );
		seePrefPanel.add(button);
		pathPanel.add(seePrefPanel);
		
		return pathPanel;
	}
	
	private JPanel buildSetupTab(int tabY){
		int optionSpacing = (int)((bigLabelSpacing + labelSpacing) / 2); 
		JPanel comp;
		setupPanel.setBackground(Color.WHITE);
		
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Setup ", TitledBorder.LEFT, TitledBorder.CENTER, titleFont, Color.DARK_GRAY);
		setupPanel.setBounds(10, tabY, frame.getWidth()/3 - 20, 6*optionSpacing + 6*bigLabelSpacing);
		setupPanel.setBorder(tb);
		setupPanel.setLayout(null);
		
		addToPanel(setupPanel, SETUP_LABEL_PREFFILE, createLPanel(SETUP_LABEL_PREFFILE, setupPanel, optionSpacing, DetectorMain.DEFAULT_PREF_FILE), setupMap);
		
		addToPanel(setupPanel, SETUP_LABEL_METRIC, createLCBPanel(SETUP_LABEL_METRIC, setupPanel, 2*optionSpacing, MetricType.values(), iManager.getMetricType(), InputManager.METRIC), setupMap);
		addToPanel(setupPanel, SETUP_LABEL_OUTPUT, createLCBPanel(SETUP_LABEL_OUTPUT, setupPanel, 3*optionSpacing, new String[]{"null", "TEXT", "IMAGE"}, iManager.getOutputFormat(), InputManager.OUTPUT_FORMAT), setupMap);
		addToPanel(setupPanel, SETUP_IND_SELECTION, createLCBPanel(SETUP_IND_SELECTION, setupPanel, 4*optionSpacing, InputManager.getIndicatorSelectionPolicies(), iManager.getDataSeriesDomain(), InputManager.INDICATOR_SELECTION), setupMap);
		
		comp = createLTPanel(SETUP_LABEL_FILTERING_THRESHOLD, setupPanel, 6*optionSpacing, Double.toString(iManager.getFilteringTreshold()), InputManager.FILTERING_TRESHOLD, iManager);
		comp.setVisible(iManager.getFilteringFlag());
		addToPanel(setupPanel, SETUP_LABEL_FILTERING, createLCKPanel(SETUP_LABEL_FILTERING, setupPanel, 5*optionSpacing, iManager.getFilteringFlag(), comp, InputManager.FILTERING_NEEDED_FLAG), setupMap);
		addToPanel(setupPanel, SETUP_LABEL_FILTERING_THRESHOLD, comp, setupMap);
		
		comp = createLTPanel(SETUP_KFOLD_VALIDATION, setupPanel, 8*optionSpacing, Integer.toString(iManager.getKFoldCounter()), InputManager.KFOLD_COUNTER, iManager);
		comp.setVisible(iManager.getTrainingFlag());
		addToPanel(setupPanel, SETUP_LABEL_TRAINING, createLCKPanel(SETUP_LABEL_TRAINING, setupPanel, 7*optionSpacing, iManager.getTrainingFlag(), comp, InputManager.TRAIN_NEEDED_FLAG), setupMap);
		addToPanel(setupPanel, SETUP_KFOLD_VALIDATION, comp, setupMap);
		
		addToPanel(setupPanel, SETUP_LABEL_SLIDING_POLICY, createLTPanel(SETUP_LABEL_SLIDING_POLICY, setupPanel, 9*optionSpacing, iManager.getSlidingPolicies(), InputManager.SLIDING_POLICY, iManager), setupMap);
		addToPanel(setupPanel, SETUP_LABEL_WINDOW_SIZE, createLTPanel(SETUP_LABEL_WINDOW_SIZE, setupPanel, 10*optionSpacing, iManager.getSlidingWindowSizes(), InputManager.SLIDING_WINDOW_SIZE, iManager), setupMap);
		
		JPanel seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setBounds((int) (setupPanel.getWidth()*0.02), 11*optionSpacing, (int) (setupPanel.getWidth()*0.96), bigLabelSpacing);
		
		JButton button = new JButton("Open Preferences");
		button.setVisible(true);
		button.setFont(bigFont);
		button.setBounds(0, 0, setupPanel.getWidth()*2/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				try {
					Desktop.getDesktop().open(new File(DetectorMain.DEFAULT_PREF_FILE));
				} catch (IOException e1) {
					AppLogger.logException(getClass(), e1, "");
				}
			} } );
		seePrefPanel.add(button);
		setupPanel.add(seePrefPanel);
		
		return setupPanel;
	}
	
	public JPanel createLPanel(String textName, JPanel root, int panelY, String textFieldText){
		return createLPanel(false, textName, root, (int) (root.getWidth()*0.02), panelY, textFieldText);
	}
	
	public JPanel createLPanel(boolean bold, String textName, JPanel root, int panelX, int panelY, String textFieldText){
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBounds(panelX, panelY, (int) (root.getWidth()*0.96), 30);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel(textName);
		lbl.setFont(labelFont);
		if(bold)
			lbl.setFont(lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));
		lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		JLabel lbldata = new JLabel(textFieldText);
		lbldata.setFont(labelFont);
		lbldata.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, labelSpacing);
		lbldata.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbldata);
		
		return panel;
	}
	
	private JPanel createLTPanel(String textName, JPanel root, int panelY, String textFieldText, String fileTag, InputManager iManager){
		JPanel panel = new JPanel();
		panel.setBounds((int) (root.getWidth()*0.02), panelY, (int) (root.getWidth()*0.96), labelSpacing);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel(textName);
		lbl.setFont(labelFont);
		lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		JTextField textField = new JTextField();
		textField.setText(textFieldText);
		textField.setFont(labelFont);
		textField.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, labelSpacing);
		panel.add(textField);
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
	        		iManager.updatePreference(fileTag, textField.getText(), true);
	        	}
			}
		});
		
		root.add(panel);
		
		return panel;
	}
	
	private JPanel createFCHPanel(String textName, JPanel root, int panelY, String textFieldText, boolean folderFlag){
		JPanel panel = new JPanel();
		panel.setBounds((int) (root.getWidth()*0.02), panelY, (int) (root.getWidth()*0.96), bigLabelSpacing);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel(textName);
		lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, labelSpacing);
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		JButton button = new JButton(textFieldText);
		button.setVisible(true);
		button.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				JFileChooser jfc = new JFileChooser(new File("").getAbsolutePath());
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					Path pathAbsolute = Paths.get(selectedFile.getAbsolutePath());
			        Path pathBase = Paths.get(new File("").getAbsolutePath());
					if(!folderFlag || selectedFile.isDirectory()){
						button.setText(pathBase.relativize(pathAbsolute).toString());
						iManager.updatePreference(panelToPreference(textName), pathBase.relativize(pathAbsolute).toString(), true);
					} else JOptionPane.showMessageDialog(frame, "'" + pathBase.relativize(pathAbsolute).toString() + "' is not a folder");
				}
			} } );
		panel.add(button);
		
		root.add(panel);
		
		return panel;
	}
	
	private JPanel createLCKPanel(String textName, JPanel root, int panelY, boolean checked, JPanel comp, String fileTag){
		JPanel panel = new JPanel();
		panel.setBounds((int) (root.getWidth()*0.02), panelY, (int) (root.getWidth()*0.96), labelSpacing);
		panel.setLayout(null);
		
		JCheckBox cb = new JCheckBox(textName);
		cb.setSelected(checked);
		cb.setFont(bigFont);
		cb.setBounds(root.getWidth()/4, 0, root.getWidth()/2, labelSpacing);
		cb.setHorizontalAlignment(SwingConstants.CENTER);
		
		if(comp != null){
			cb.addActionListener(new ActionListener() {
			    @Override
			    public void actionPerformed(ActionEvent event) {
			        JCheckBox cb = (JCheckBox) event.getSource();
			        comp.setVisible(cb.isSelected());
			        if(!isUpdating){
			        	iManager.updatePreference(fileTag, cb.isSelected() ? "1" : "0", true);
			        	reload();
			        }
			        	
			    }
			});
		}
		
		panel.add(cb);
		
		root.add(panel);
		
		return panel;
	}
	
	private JPanel createLCBPanel(String textName, JPanel root, int panelY, Object[] itemList, Object selected, String fileTag){
		JPanel panel = new JPanel();
		panel.setBounds((int) (root.getWidth()*0.02), panelY, (int) (root.getWidth()*0.96), labelSpacing);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel(textName);
		lbl.setFont(labelFont);
		lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		JComboBox<Object> comboBox = new JComboBox<Object>();
		comboBox.setFont(labelFont);
		comboBox.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, labelSpacing);

		if(itemList != null){
			for(Object ob : itemList){
				comboBox.addItem(ob);
			}
			comboBox.addActionListener (new ActionListener () {
			    public void actionPerformed(ActionEvent e) {
			        if(!isUpdating){
			        	String newValue = comboBox.getSelectedItem().toString();
			        	if(comboBox.getSelectedItem().toString().equals("FSCORE")){
			        		String s = (String)JOptionPane.showInputDialog(
				                    frame, "Set parameter beta for F-Score (beta > 0)", "FSCORE beta",
				                    JOptionPane.PLAIN_MESSAGE, null, null, "");
							if ((s != null) && (s.trim().length() > 0) && AppUtility.isNumber(s.trim())) {
								newValue = newValue + "(" + s + ")";
							} else newValue = newValue + "(1)";
			        	}
			        	if(comboBox.getSelectedItem().toString().equals("PEARSON")){
			        		String s = (String)JOptionPane.showInputDialog(
				                    frame, "Set threshold for Pearson Correlation Index (0<threshold<=1) ", "Pearson Index Threshold",
				                    JOptionPane.PLAIN_MESSAGE, null, null, "");
							if ((s != null) && (s.trim().length() > 0) && AppUtility.isNumber(s.trim())) {
								newValue = newValue + "(" + s + ")";
							} else newValue = newValue + "(0.9)";
			        	}
			        	iManager.updatePreference(fileTag, newValue, true);
			        	reload();
			    	}
			    }
			});
		}
		if(selected != null)
			comboBox.setSelectedItem(selected);
		panel.add(comboBox);
		
		root.add(panel);
		
		return panel;
	}
	
	private void showDetectorOutputs(List<DetectorOutput> outList) {
		OutputFrame of = new OutputFrame(iManager, outList.size());
		of.buildSummaryPanel(outList);
		for(DetectorOutput dOut : outList){
			of.addOutput(dOut);
		}
		of.setVisible(true);
	}
	
}
