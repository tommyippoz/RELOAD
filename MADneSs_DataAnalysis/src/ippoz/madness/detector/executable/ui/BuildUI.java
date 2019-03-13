/**
 * 
 */
package ippoz.madness.detector.executable.ui;

import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.commons.algorithm.AlgorithmType;
import ippoz.madness.detector.commons.knowledge.sliding.SlidingPolicy;
import ippoz.madness.detector.commons.support.AppLogger;
import ippoz.madness.detector.commons.support.PreferencesManager;
import ippoz.madness.detector.executable.DetectorMain;
import ippoz.madness.detector.loader.CSVPreLoader;
import ippoz.madness.detector.loader.Loader;
import ippoz.madness.detector.loader.MySQLLoader;
import ippoz.madness.detector.manager.DetectionManager;
import ippoz.madness.detector.manager.InputManager;
import ippoz.madness.detector.metric.Metric;
import ippoz.madness.detector.metric.MetricType;
import ippoz.madness.detector.output.DetectorOutput;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * @author Tommy
 *
 */
public class BuildUI {
	
	private static final String SETUP_LABEL_PREFFILE = "Preferences File";
	
	private static final String SETUP_LABEL_METRIC = "Target Metric";
	
	private static final String SETUP_LABEL_OUTPUT = "Output Format";
	
	private static final String SETUP_LABEL_FILTERING = "Filtering";
	
	private static final String SETUP_LABEL_FILTERING_THRESHOLD = "FPR Threshold";
	
	private static final String SETUP_LABEL_TRAINING = "Training";
	
	private static final String SETUP_KFOLD_VALIDATION = "K-Fold Cross Validation";
	
	private static final String SETUP_LABEL_SLIDING_POLICY = "Sliding Policy";
	
	private static final String SETUP_LABEL_WINDOW_SIZE = "Window Size";
	
	private static final String PATH_LABEL_INPUT_FOLDER = "Input Folder";
	
	private static final String PATH_LABEL_OUTPUT_FOLDER = "Output Folder";
	
	private static final String PATH_LABEL_CONF_FOLDER = "Configiuration Folder";
	
	private static final String PATH_LABEL_SETUP_FOLDER = "Setup Folder";
	
	private static final String PATH_LABEL_SCORES_FOLDER = "Scores Folder";
	
	private static final String PATH_LABEL_DETECTION_PREFERENCES = "Detection Preferences";
	
	private JPanel headerPanel, setupPanel, pathPanel, dataAlgPanel, footerPanel;
	
	private Map<String, JPanel> setupMap, pathMap;

	private JFrame frame;
	
	private InputManager iManager;
	
	private boolean isUpdating;

	public BuildUI(InputManager iManager){
		this.iManager = iManager;
		isUpdating = true;
		setupMap = new HashMap<String, JPanel>();
		pathMap = new HashMap<String, JPanel>();
		buildFrame();
		isUpdating = false;
	}
	
	private void buildFrame(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame = new JFrame();
		frame.setTitle("RELOAD Framework");
		frame.setIconImage(new ImageIcon(getClass().getResource("/RELOAD_Transparent.png")).getImage());
		if(screenSize.getWidth() > 1600)
			frame.setBounds(0, 0, (int)(screenSize.getWidth()*0.75), (int)(screenSize.getHeight()*0.75));
		else frame.setBounds(0, 0, 800, 480);
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
		frame.getContentPane().add(buildSetupTab(headerPanel.getHeight()));
		
		pathPanel = new JPanel();
		frame.getContentPane().add(buildPathsTab(headerPanel.getHeight()));
		
		dataAlgPanel = new JPanel();
		frame.getContentPane().add(buildAlgorithmsDatasetsTab(headerPanel.getHeight()));
		
		footerPanel = new JPanel();
		frame.getContentPane().add(buildFooterTab(headerPanel.getHeight() + Math.max(setupPanel.getHeight(), Math.max(pathPanel.getHeight(), dataAlgPanel.getHeight()))));
		
		frame.setBounds(0, 0, frame.getWidth(), headerPanel.getHeight() + Math.max(setupPanel.getHeight(), Math.max(pathPanel.getHeight(), dataAlgPanel.getHeight())) + footerPanel.getHeight());
		frame.setLocationRelativeTo(null);
		
		isUpdating = false;
		
		return frame;
	}
	
	private JPanel buildHeaderTab(){
		headerPanel.setBackground(Color.WHITE);
		headerPanel.setBounds(0, 0, frame.getWidth(), 145);
		headerPanel.setLayout(null);
		ImageIcon ii = new ImageIcon(getClass().getResource("/RELOAD_Transparent.png"));
		JLabel lblMadness = new JLabel(new ImageIcon(ii.getImage().getScaledInstance(320, 125, Image.SCALE_DEFAULT)));
		lblMadness.setBounds(0, 10, frame.getWidth(), 125);
		lblMadness.setHorizontalAlignment(SwingConstants.CENTER);
		headerPanel.add(lblMadness);
		
		return headerPanel;
	}
	
	private JPanel buildFooterTab(int tabY){
		footerPanel.setBackground(Color.WHITE);
		footerPanel.setBounds(frame.getWidth()/10, tabY, frame.getWidth()*4/5, 100);
		footerPanel.setLayout(null);
		
		ImageIcon ii = new ImageIcon(getClass().getResource("/reload.png"));
		JButton button = new JButton("", new ImageIcon(ii.getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT)));
		button.setBounds(footerPanel.getWidth()*2/5, 0, 40, 40);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				reload();
			}
		} );
		footerPanel.add(button);
		
		button = new JButton("RELOAD!");
		button.setBounds(footerPanel.getWidth()*2/5 + 65, 0, footerPanel.getWidth()/5 - 65, 40);
		button.setFont(new Font("Times", Font.BOLD, 15));
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				runExperiments();
			} } );
		footerPanel.add(button);
		
		JLabel lblFooter = new JLabel("Authors' Information and References");
		lblFooter.setBounds(0, 40, footerPanel.getWidth(), 20);
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
					for(PreferencesManager loaderPref : DetectorMain.readLoaders(iManager)){
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
					AppLogger.logException(DetectorMain.class, ex, "");
				}
			}
		}).start();
	}
	
	private void printOptions(JPanel panel, String[] options, int fromX, int fromY, int space){
		JLabel lbl;
		JButton jb;
		int i = 0;
		int buttonsSpace = 35;
		if(options != null){
			for(String option : options){
				lbl = new JLabel(option);
				lbl.setBounds(fromX, fromY + i*space, panel.getWidth() - fromX - buttonsSpace, 20);
				lbl.setHorizontalAlignment(SwingConstants.CENTER);
				panel.add(lbl);
				jb = new JButton("-");
				jb.setBounds(panel.getWidth() - fromX - buttonsSpace, fromY + i*space, buttonsSpace, 20);
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
		int labelSpacing = 30;
		dataAlgPanel.setBackground(Color.WHITE);
		
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Data Analysis", TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY);
		dataAlgPanel.setBounds(frame.getWidth()*2/3 + 10, tabY, frame.getWidth()/3 - 20, 100 + labelSpacing*(getDatasets().length + getAlgorithms().length + 2));
		dataAlgPanel.setBorder(tb);
		dataAlgPanel.setLayout(null);
		
		JLabel mainLabel = new JLabel("Datasets");
		mainLabel.setBounds(dataAlgPanel.getWidth()/4, labelSpacing, dataAlgPanel.getWidth()/2, 25);
		mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainLabel.setFont(new Font("Times", Font.BOLD, 20));
		dataAlgPanel.add(mainLabel);
		
		printOptions(dataAlgPanel, getDatasets(), 20, 2*labelSpacing, labelSpacing);
		
		JPanel seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setBounds((int) (dataAlgPanel.getWidth()*0.01), 20 + labelSpacing*(getDatasets().length + 1), (int) (dataAlgPanel.getWidth()*0.98), labelSpacing + 1);
		
		JButton button = new JButton("Add Dataset");
		button.setVisible(true);
		button.setBounds(25, 0, pathPanel.getWidth()/5, 25);
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
		button = new JButton("See Datasets");
		button.setVisible(true);
		button.setBounds(0, 0, pathPanel.getWidth()/5, 25);
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
		
		tabY = labelSpacing*(getDatasets().length + 2) + 40;
		
		mainLabel = new JLabel("Algorithms");
		mainLabel.setBounds(dataAlgPanel.getWidth()/4, tabY, dataAlgPanel.getWidth()/2, 25);
		mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainLabel.setFont(new Font("Times", Font.BOLD, 20));
		dataAlgPanel.add(mainLabel);
		
		printOptions(dataAlgPanel, getAlgorithms(), 20, tabY + 30, labelSpacing);
		
		seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setBounds((int) (dataAlgPanel.getWidth()*0.01), 60 + labelSpacing*(getDatasets().length + getAlgorithms().length + 2), (int) (dataAlgPanel.getWidth()*0.98), labelSpacing + 1);
		
		button = new JButton("Add Algorithm");
		button.setVisible(true);
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
		button.setBounds(0, 0, pathPanel.getWidth()/5, 25);
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
		
		return dataAlgPanel;
	}
	
	private String[] getDatasets() {
		int i = 0;
		List<PreferencesManager> lList = DetectorMain.readLoaders(iManager);
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
		List<List<AlgorithmType>> aComb = DetectorMain.readAlgorithmCombinations(iManager);
		String[] algStrings = new String[aComb.size()];
		for(List<AlgorithmType> aList : aComb){
			algStrings[i++] = aList.toString().substring(1, aList.toString().length()-1) + " (" + DetectionAlgorithm.getFamily(AlgorithmType.valueOf(aList.toString().substring(1, aList.toString().length()-1))) + ")";
		}
		return algStrings;
	}
	
	private JPanel buildPathsTab(int tabY){
		int labelSpacing = 35;
		pathPanel.setBackground(Color.WHITE);
		
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Paths", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY);
		pathPanel.setBounds(frame.getWidth()/3 + 10, tabY, frame.getWidth()/3 - 20, 8*labelSpacing + 10);
		pathPanel.setBorder(tb);
		pathPanel.setLayout(null);
		
		addToPanel(pathPanel, PATH_LABEL_INPUT_FOLDER, createFCHPanel(PATH_LABEL_INPUT_FOLDER, pathPanel, labelSpacing, iManager.getInputFolder(), true), pathMap);
		addToPanel(pathPanel, PATH_LABEL_OUTPUT_FOLDER, createFCHPanel(PATH_LABEL_OUTPUT_FOLDER, pathPanel, 2*labelSpacing, iManager.getOutputFolder(), true), pathMap);
		addToPanel(pathPanel, PATH_LABEL_CONF_FOLDER, createFCHPanel(PATH_LABEL_CONF_FOLDER, pathPanel, 3*labelSpacing, iManager.getConfigurationFolder(), true), pathMap);
		addToPanel(pathPanel, PATH_LABEL_SETUP_FOLDER, createFCHPanel(PATH_LABEL_SETUP_FOLDER, pathPanel, 4*labelSpacing, iManager.getSetupFolder(), true), pathMap);
		addToPanel(pathPanel, PATH_LABEL_SCORES_FOLDER, createFCHPanel(PATH_LABEL_SCORES_FOLDER, pathPanel, 5*labelSpacing, iManager.getScoresFolder(), true), pathMap);
		addToPanel(pathPanel, PATH_LABEL_DETECTION_PREFERENCES, createFCHPanel(PATH_LABEL_DETECTION_PREFERENCES, pathPanel, 6*labelSpacing, iManager.getDetectionPreferencesFile(), false), pathMap);
		
		JPanel seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setBounds((int) (setupPanel.getWidth()*0.01), 7*labelSpacing, (int) (setupPanel.getWidth()*0.98), labelSpacing+1);
		
		JButton button = new JButton("Open Scoring Preferences");
		button.setVisible(true);
		button.setBounds(0, 0, pathPanel.getWidth()*2/5, 25);
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
		int labelSpacing = 30;
		JPanel comp;
		setupPanel.setBackground(Color.WHITE);
		
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Setup", TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY);
		setupPanel.setBounds(10, tabY, frame.getWidth()/3 - 20, 11*labelSpacing + 15);
		setupPanel.setBorder(tb);
		setupPanel.setLayout(null);
		
		addToPanel(setupPanel, SETUP_LABEL_PREFFILE, createLPanel(SETUP_LABEL_PREFFILE, setupPanel, labelSpacing, DetectorMain.DEFAULT_PREF_FILE), setupMap);
		
		addToPanel(setupPanel, SETUP_LABEL_METRIC, createLCBPanel(SETUP_LABEL_METRIC, setupPanel, 2*labelSpacing, MetricType.values(), iManager.getMetricType(), InputManager.METRIC), setupMap);
		addToPanel(setupPanel, SETUP_LABEL_OUTPUT, createLCBPanel(SETUP_LABEL_OUTPUT, setupPanel, 3*labelSpacing, new String[]{"null", "TEXT", "IMAGE"}, iManager.getOutputFormat(), InputManager.OUTPUT_FORMAT), setupMap);
		
		comp = createLTPanel(SETUP_LABEL_FILTERING_THRESHOLD, setupPanel, 5*labelSpacing, Double.toString(iManager.getFilteringTreshold()), InputManager.FILTERING_TRESHOLD);
		comp.setVisible(iManager.getFilteringFlag());
		addToPanel(setupPanel, SETUP_LABEL_FILTERING, createLCKPanel(SETUP_LABEL_FILTERING, setupPanel, 4*labelSpacing, iManager.getFilteringFlag(), comp, InputManager.FILTERING_NEEDED_FLAG), setupMap);
		addToPanel(setupPanel, SETUP_LABEL_FILTERING_THRESHOLD, comp, setupMap);
		
		comp = createLTPanel(SETUP_KFOLD_VALIDATION, setupPanel, 7*labelSpacing, Integer.toString(iManager.getKFoldCounter()), InputManager.KFOLD_COUNTER);
		comp.setVisible(iManager.getTrainingFlag());
		addToPanel(setupPanel, SETUP_LABEL_TRAINING, createLCKPanel(SETUP_LABEL_TRAINING, setupPanel, 6*labelSpacing, iManager.getTrainingFlag(), comp, InputManager.TRAIN_NEEDED_FLAG), setupMap);
		addToPanel(setupPanel, SETUP_KFOLD_VALIDATION, comp, setupMap);
		
		addToPanel(setupPanel, SETUP_LABEL_SLIDING_POLICY, createLTPanel(SETUP_LABEL_SLIDING_POLICY, setupPanel, 8*labelSpacing, iManager.getSlidingPolicies(), InputManager.SLIDING_POLICY), setupMap);
		addToPanel(setupPanel, SETUP_LABEL_WINDOW_SIZE, createLTPanel(SETUP_LABEL_WINDOW_SIZE, setupPanel, 9*labelSpacing, iManager.getSlidingWindowSizes(), InputManager.SLIDING_WINDOW_SIZE), setupMap);
		
		JPanel seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setBounds((int) (setupPanel.getWidth()*0.01), 10*labelSpacing, (int) (setupPanel.getWidth()*0.98), labelSpacing+1);
		
		JButton button = new JButton("Open Preferences");
		button.setVisible(true);
		button.setBounds(0, 0, setupPanel.getWidth()*2/5, 25);
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
	
	private JPanel createLPanel(String textName, JPanel root, int panelY, String textFieldText){
		return createLPanel(false, textName, root, (int) (root.getWidth()*0.01), panelY, textFieldText);
	}
	
	private JPanel createLPanel(boolean bold, String textName, JPanel root, int panelX, int panelY, String textFieldText){
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBounds(panelX, panelY, (int) (root.getWidth()*0.98), 25);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel(textName);
		if(bold)
			lbl.setFont(lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));
		lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, 20);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		JLabel lbldata = new JLabel(textFieldText);
		lbldata.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, 25);
		lbldata.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbldata);
		
		return panel;
	}
	
	private JPanel createLTPanel(String textName, JPanel root, int panelY, String textFieldText, String fileTag){
		JPanel panel = new JPanel();
		panel.setBounds((int) (root.getWidth()*0.01), panelY, (int) (root.getWidth()*0.98), 25);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel(textName);
		lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, 20);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		JTextField textField = new JTextField();
		textField.setText(textFieldText);
		textField.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, 25);
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
		panel.setBounds((int) (root.getWidth()*0.01), panelY, (int) (root.getWidth()*0.98), 25);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel(textName);
		lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, 20);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		JButton button = new JButton(textFieldText);
		button.setVisible(true);
		button.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, 25);
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
		panel.setBounds((int) (root.getWidth()*0.01), panelY, (int) (root.getWidth()*0.98), 25);
		panel.setLayout(null);
		
		JCheckBox cb = new JCheckBox(textName);
		cb.setSelected(checked);
		cb.setBounds(root.getWidth()/4, 0, root.getWidth()/2, 20);
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
		panel.setBounds((int) (root.getWidth()*0.01), panelY, (int) (root.getWidth()*0.98), 25);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel(textName);
		lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, 20);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		JComboBox<Object> comboBox = new JComboBox<Object>();
		comboBox.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, 25);

		if(itemList != null){
			for(Object ob : itemList){
				comboBox.addItem(ob);
			}
			comboBox.addActionListener (new ActionListener () {
			    public void actionPerformed(ActionEvent e) {
			        if(!isUpdating){
			        	iManager.updatePreference(fileTag, comboBox.getSelectedItem().toString(), true);
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
		OutputFrame of = new OutputFrame(outList.size());
		of.buildSummaryPanel(outList);
		for(DetectorOutput dOut : outList){
			of.addOutput(dOut);
		}
		of.setVisible(true);
	}
	
	private class OutputFrame {
		
		private JFrame outFrame;
		
		private JTabbedPane tabbedPane;
		
		private static final int labelSpacing = 25;
		
		private int panelNumber; 
		
		public OutputFrame(int panelNumber) {
			this.panelNumber = panelNumber;
			buildFrame();
			buildTabbedPanel();
		}

		private void buildTabbedPanel() {
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			tabbedPane.setBackground(Color.WHITE);
			tabbedPane.setBounds(0, labelSpacing + 40, outFrame.getWidth() - 10, outFrame.getHeight() - labelSpacing - 50);		
		}

		public void addOutput(DetectorOutput dOut) {
			JPanel outPanel = buildOutputPanel(dOut);
			tabbedPane.addTab("DB: " + dOut.getDataset() + " - Alg: " + dOut.getAlgorithm().replace("[", "").replace("]", ""), outPanel);
		}

		public void setVisible(boolean b) {
			if(outFrame != null){
				outFrame.getContentPane().setBackground(Color.WHITE);
				outFrame.getContentPane().add(tabbedPane);
				if(panelNumber > 4){
					outFrame.setBounds(outFrame.getX(), outFrame.getY(), outFrame.getWidth(), outFrame.getHeight() + (panelNumber / 4)*labelSpacing);
				}
				tabbedPane.setForeground(Color.WHITE);
				outFrame.setLocationRelativeTo(null);
				outFrame.setVisible(b);
			}
		}

		private void buildFrame(){
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			outFrame = new JFrame();
			outFrame.setTitle("Summary");
			if(screenSize.getWidth() > 1600)
				outFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.5), (int)(screenSize.getHeight()*0.5));
			else outFrame.setBounds(0, 0, 800, 480);
			outFrame.setBackground(Color.WHITE);
			outFrame.setResizable(false);
			frame.getContentPane().setLayout(new GridLayout(1, 1));
		}
		
		public void buildSummaryPanel(List<DetectorOutput> dOutList){
			JPanel summaryPanel = new JPanel();
			summaryPanel.setBackground(Color.WHITE);
			summaryPanel.setBounds(0, 0, tabbedPane.getWidth() - 10, tabbedPane.getHeight() - 10);
			summaryPanel.setLayout(null);
			
			JPanel fPanel = new JPanel();
			fPanel.setBackground(Color.WHITE);
			TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Common Setups", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
			fPanel.setBounds(summaryPanel.getWidth()/4, 0, summaryPanel.getWidth()/2, labelSpacing + 30);
			fPanel.setBorder(tb);
			fPanel.setLayout(null);
			fPanel.add(createLPanel(true, "Metric", fPanel, (int) (0.01*fPanel.getWidth()), 20, dOutList.get(0).getReferenceMetric().getMetricName()));			
			summaryPanel.add(fPanel);
			
			summaryPanel.add(buildOutputSummaryPanel(null, summaryPanel, 0));
			int i = 1;
			for(DetectorOutput dOut : dOutList){
				summaryPanel.add(buildOutputSummaryPanel(dOut, summaryPanel, i++));
			}
			tabbedPane.add("Summary", summaryPanel);
		}
		
		private JPanel buildOutputSummaryPanel(DetectorOutput dOut, JPanel root, int i){
			int elements = 7;
			JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			panel.setBounds((int) (root.getWidth()*0.01), labelSpacing*(i+1) + 40, (int) (root.getWidth()*0.98), labelSpacing);
			panel.setLayout(null);
			
			JLabel lbl = new JLabel(dOut != null ? dOut.getDataset() : "Dataset");
			lbl.setFont(new Font(lbl.getFont().getName(), dOut == null ? Font.BOLD : Font.PLAIN, 12));
			lbl.setBounds(0, 0, root.getWidth()/elements, labelSpacing);
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(lbl);
			
			lbl = new JLabel(dOut != null ? dOut.getAlgorithm().replace("[", "").replace("]", "") : "Algorithm");
			lbl.setFont(new Font(lbl.getFont().getName(), dOut == null ? Font.BOLD : Font.PLAIN, 12));
			lbl.setBounds(root.getWidth()/elements, 0, 2*root.getWidth()/elements, labelSpacing);
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(lbl);
			
			lbl = new JLabel(dOut != null ? dOut.getBestSetup() : "Best Configuration");
			lbl.setFont(new Font(lbl.getFont().getName(), dOut == null ? Font.BOLD : Font.PLAIN, 12));
			lbl.setBounds(root.getWidth()*3/elements, 0, root.getWidth()/elements, labelSpacing);
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(lbl);
			
			lbl = new JLabel(dOut != null ? dOut.getBestRuns() : "Best Runs");
			lbl.setFont(new Font(lbl.getFont().getName(), dOut == null ? Font.BOLD : Font.PLAIN, 12));
			lbl.setBounds(root.getWidth()*4/elements, 0, root.getWidth()/elements, labelSpacing);
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(lbl);
			
			lbl = new JLabel(dOut != null ? dOut.getFaultsRatioString() : "Attacks Ratio");
			lbl.setFont(new Font(lbl.getFont().getName(), dOut == null ? Font.BOLD : Font.PLAIN, 12));
			lbl.setBounds(root.getWidth()*5/elements, 0, root.getWidth()/elements, labelSpacing);
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(lbl);
			
			lbl = new JLabel(dOut != null ? String.valueOf(dOut.getFormattedBestScore()) : "Best Score");
			lbl.setFont(new Font(lbl.getFont().getName(), dOut == null ? Font.BOLD : Font.PLAIN, 12));
			lbl.setBounds(root.getWidth()*6/elements, 0, root.getWidth()/elements, labelSpacing);
			lbl.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(lbl);
			
			return panel;
		}
		
		private JPanel buildOutputPanel(DetectorOutput dOut) {	
			JPanel containerPanel = new JPanel();
			containerPanel.setBackground(Color.WHITE);
			containerPanel.setBounds(0, 0, tabbedPane.getWidth() - 10, tabbedPane.getHeight() - 10);
			containerPanel.setLayout(null);
			
			JPanel miscPanel = new JPanel();
			miscPanel.setBackground(Color.WHITE);
			TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Setup", TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
			miscPanel.setBounds(5, 10, containerPanel.getWidth()/2 - 10, labelSpacing*3+30);
			miscPanel.setBorder(tb);
			miscPanel.setLayout(null);
			miscPanel.add(createLPanel(true, "Dataset", miscPanel, (int) (0.01*miscPanel.getWidth()), 20, dOut.getDataset()));
			miscPanel.add(createLPanel(true, "Algorithm", miscPanel, (int) (0.01*miscPanel.getWidth()), labelSpacing + 20, dOut.getAlgorithm().replace("[", "").replace("]", "")));
			miscPanel.add(createLPanel(true, "Metric", miscPanel, (int) (0.01*miscPanel.getWidth()), labelSpacing*2 + 20, dOut.getReferenceMetric().getMetricName()));			
			containerPanel.add(miscPanel);
			
			miscPanel = new JPanel();
			miscPanel.setBackground(Color.WHITE);
			tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Details", TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
			miscPanel.setBounds(containerPanel.getWidth()/2 + 5, 10, containerPanel.getWidth()/2 - 10, labelSpacing*3+30);
			miscPanel.setBorder(tb);
			miscPanel.setLayout(null);
			miscPanel.add(createLPanel(true, "Best Setup", miscPanel, (int) (0.01*miscPanel.getWidth()), 20, dOut.getBestSetup()));
			miscPanel.add(createLPanel(true, "Runs", miscPanel, (int) (0.01*miscPanel.getWidth()), labelSpacing + 20, dOut.getBestRuns()));
			miscPanel.add(createLPanel(true, "Best Score (" + dOut.getReferenceMetric().getMetricShortName() + ")", miscPanel, (int) (0.01*miscPanel.getWidth()), labelSpacing*2 + 20, String.valueOf(dOut.getBestScore())));			
			containerPanel.add(miscPanel);
			   
	        String[] columnNames = new String[dOut.getEvaluationMetrics().length + 3];
	        columnNames[0] = "Voter";
	        columnNames[1] = "Anomaly";
	        columnNames[2] = "Checkers";
			int i = 3;
	        for(Metric met : dOut.getEvaluationMetrics()){
				columnNames[i++] = met.getMetricType() != null ? met.getMetricShortName() : "AUC";
			}	 
	        
	        JTable table = new JTable(dOut.getEvaluationGrid(), columnNames);
	        table.setFillsViewportHeight(true);
	        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
	        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
	        for(int x=0;x<table.getColumnCount();x++){
	        	table.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
	        	table.getColumnModel().getColumn(x).setHeaderRenderer(centerRenderer);
	        }
	        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	        resizeColumnWidth(table);

	        JScrollPane scroll = new JScrollPane(table);
	        scroll.setBounds(5, miscPanel.getHeight() + 20, containerPanel.getWidth()-10, (int)table.getPreferredSize().getHeight() + 50);
	        containerPanel.add(scroll);
	        
	        JPanel fPanel = new JPanel();
	        fPanel.setBackground(Color.WHITE);
			tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Additional Files", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
			fPanel.setBounds(outFrame.getWidth()/4, miscPanel.getHeight() + scroll.getHeight() + 20, outFrame.getWidth()/2, labelSpacing + 30);
			fPanel.setBorder(tb);
			fPanel.setLayout(null);
			
			JButton button = new JButton("Open Output Folder");
			button.setVisible(true);
			button.setBounds(miscPanel.getWidth()/4, 20, miscPanel.getWidth()/2, labelSpacing);
			button.addActionListener(new ActionListener() { 
				public void actionPerformed(ActionEvent e) { 
					Desktop desktop = Desktop.getDesktop();
			        File dirToOpen = new File(dOut.buildPath(iManager.getOutputFolder()));
			        try {
			            desktop.open(dirToOpen);
			        } catch (IOException ex) {
			        	JOptionPane.showMessageDialog(outFrame, "ERROR: Unable to open '" + dOut.buildPath(iManager.getOutputFolder()) + "'");
					}
				} } );	
			fPanel.add(button);
			containerPanel.add(fPanel);
			
			return containerPanel;
		}
		
		public void resizeColumnWidth(JTable table) {
		    final TableColumnModel columnModel = table.getColumnModel();
		    int width = 60; // Min width
		    
		    for (int column = 0; column < table.getColumnCount(); column++) {
		        
		        for (int row = 0; row < table.getRowCount(); row++) {
		            TableCellRenderer renderer = table.getCellRenderer(row, column);
		            Component comp = table.prepareRenderer(renderer, row, column);
		            width = Math.max(comp.getPreferredSize().width +1 , width);
		        }
		        if(width > 100)
		            width = 100;
		        columnModel.getColumn(column).setPreferredWidth(width);
		    }
		}
		
	}

}
