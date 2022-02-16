/**
 * 
 */
package ippoz.reload.ui;

import ippoz.reload.algorithm.DetectionAlgorithm;
import ippoz.reload.algorithm.type.BaseLearner;
import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.algorithm.type.MetaLearner;
import ippoz.reload.commons.algorithm.AlgorithmFamily;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.loader.LoaderType;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.PreferencesManager;
import ippoz.reload.executable.DetectorMain;
import ippoz.reload.manager.DetectionManager;
import ippoz.reload.manager.InputManager;
import ippoz.reload.metric.MetricType;
import ippoz.reload.output.DetectorOutput;

import java.awt.BorderLayout;
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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
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
	
	private static final String SETUP_LABEL_FILTERING = "Feature Selection";
	
	private static final String SETUP_LABEL_TRAINING = "Training";
	
	private static final String SETUP_KFOLD_VALIDATION = "K-Fold Cross Validation";
	
	private static final String SETUP_LABEL_SLIDING_POLICY = "Sliding Policy";
	
	private static final String SETUP_LABEL_WINDOW_SIZE = "Window Size";
	
	private static final String PATH_LABEL_INPUT_FOLDER = "Input Folder";
	
	private static final String PATH_LABEL_OUTPUT_FOLDER = "Output Folder";
	
	private static final String PATH_LABEL_CONF_FOLDER = "Configuration Folder";
	
	private static final String PATH_LABEL_SETUP_FOLDER = "Setup Folder";
	
	private static final String PATH_LABEL_LOADERS_FOLDER = "Loaders Folder";
	
	private static final String PATH_LABEL_SCORES_FOLDER = "Scores Folder";
	
	private static final String PATH_LABEL_DETECTION_PREFERENCES = "Detection Preferences";
	
	private static final String SETUP_LABEL_EVALUATION = "Evaluation";

	private static final String PATH_LABEL_DATASETS_FOLDER = "Datasets Folder";
	
	private static final String SETUP_FORCE_BASELEARNERS = "Force Training of BaseLearners";
	
	private static final String SETUP_FORCE_TRAINING = "Force Training";
	
	private static final String SETUP_FORCE_PARALLEL = "Multi-Threading Training";
	
	private static final String SETUP_PREDICT = "Predict Misclassifications";
	
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
		
		bigFont = new Font("Times", Font.BOLD, (int)((16 + rate)/2));
		labelFont = new Font("Times", Font.PLAIN, (int)((13 + rate)/2));
		smallLabelFont = new Font("Times", Font.PLAIN, (int)((12 + rate)/2));
		
		labelSpacing = (int)(frame.getHeight()/25);
		bigLabelSpacing = (int)(frame.getHeight()/20);
		
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
		//frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.getContentPane().setLayout(null);
	}
	
	public void reload() {
		isUpdating = true;
		frame.setVisible(false);
		frame.getContentPane().removeAll();
		setupMap = new HashMap<String, JPanel>();
		pathMap = new HashMap<String, JPanel>();
		iManager.reload();
		frame = buildJFrame();
		frame.setVisible(true);
		isUpdating = false;
	}
	
	public JFrame getFrame() {
		return frame;
	}
	
	public static String panelToPreference(String textName) {
		switch(textName){
			case SETUP_LABEL_PREFFILE:
				return null;
			case SETUP_LABEL_METRIC:
				return InputManager.METRIC;
			case SETUP_LABEL_OUTPUT:
				return InputManager.OUTPUT_FORMAT;
			case SETUP_LABEL_FILTERING:
				return InputManager.FILTERING_NEEDED_FLAG;
			case SETUP_LABEL_TRAINING:
				return InputManager.TRAIN_NEEDED_FLAG;
			case SETUP_LABEL_SLIDING_POLICY:
				return InputManager.SLIDING_POLICY;
			case SETUP_LABEL_WINDOW_SIZE:
				return InputManager.SLIDING_WINDOW_SIZE;
			case PATH_LABEL_INPUT_FOLDER:
				return InputManager.INPUT_FOLDER;
			case PATH_LABEL_DATASETS_FOLDER:
				return InputManager.DATASETS_FOLDER;
			case PATH_LABEL_LOADERS_FOLDER:
				return InputManager.LOADER_FOLDER;
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
		frame.getContentPane().setLayout(new BorderLayout(10, 10));
		frame.getContentPane().add(buildHeaderTab(), BorderLayout.NORTH);
		
		setupPanel = new JPanel();
		frame.getContentPane().add(buildSetupTab(headerPanel.getHeight() + labelSpacing/2), BorderLayout.WEST);
		
		pathPanel = new JPanel();
		frame.getContentPane().add(buildPathsTab(headerPanel.getHeight() + labelSpacing/2), BorderLayout.CENTER);
		
		dataAlgPanel = new JPanel();
		frame.getContentPane().add(buildAlgorithmsDatasetsTab(headerPanel.getHeight() + labelSpacing/2), BorderLayout.EAST);
		
		footerPanel = new JPanel();
		frame.getContentPane().add(buildFooterTab(headerPanel.getHeight() + Math.max(setupPanel.getHeight(), Math.max(pathPanel.getHeight(), dataAlgPanel.getHeight())) + labelSpacing), BorderLayout.SOUTH);
		
		//frame.setBounds(0, 0, frame.getWidth(), headerPanel.getHeight() + Math.max(setupPanel.getHeight(), Math.max(pathPanel.getHeight(), dataAlgPanel.getHeight())) + footerPanel.getHeight());
		frame.setLocationRelativeTo(null);
		
		isUpdating = false;
		
		return frame;
	}
	
	private JPanel buildHeaderTab(){
		headerPanel.setBackground(Color.WHITE);
		//headerPanel.setBounds(0, 0, frame.getWidth(), frame.getHeight()/5);
		headerPanel.setLayout(new GridLayout(1, 1));
		ImageIcon ii = new ImageIcon(getClass().getResource("/RELOAD_Transparent.png"));
		JLabel lblMadness = new JLabel(new ImageIcon(ii.getImage().getScaledInstance(320, 125, Image.SCALE_DEFAULT)));
		//lblMadness.setBounds(0, 10, frame.getWidth(), frame.getHeight()/6);
		lblMadness.setHorizontalAlignment(SwingConstants.CENTER);
		headerPanel.add(lblMadness);
		
		return headerPanel;
	}
	
	private JPanel buildFooterTab(int tabY){
		footerPanel.setBackground(Color.WHITE);
		//footerPanel.setBounds(frame.getWidth()/10, tabY, frame.getWidth()*4/5, (int) (frame.getHeight()*0.25));
		footerPanel.setLayout(new GridLayout(2, 1));
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2, 200, 10));
		panel.setBorder(new EmptyBorder(0, 200, 0, 200));
		panel.setBackground(Color.WHITE);
		
		ImageIcon ii = new ImageIcon(getClass().getResource("/reload.png"));
		JButton button = new JButton("", new ImageIcon(ii.getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT)));
		//button.setBounds(footerPanel.getWidth()*2/5, 0, footerPanel.getHeight()*2/5, footerPanel.getHeight()*2/5);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				reload();
			}
		} );
		panel.add(button);
		
		button = new JButton("RELOAD!");
		//button.setBounds(footerPanel.getWidth()/2, 0, footerPanel.getWidth()/6, footerPanel.getHeight()*2/5);
		button.setFont(bigFont);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				runExperiments();
			} } );
		panel.add(button);
		
		footerPanel.add(panel);
		
		JLabel lblFooter = new JLabel("Authors' Information and References");
		//lblFooter.setBounds(0, footerPanel.getHeight()/3, footerPanel.getWidth(), footerPanel.getHeight()/5);
		lblFooter.setFont(bigFont);
		lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
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
				int tot = 0;
				int index = 1;
				try { 
					tot = DetectorMain.readAlgorithmCombinations(iManager).size();
					List<PreferencesManager> activeLoaders = iManager.readLoaders();
					tot = tot*activeLoaders.size();
					AppLogger.logInfo(DetectorMain.class, tot + " RELOAD instances found.");
					List<DetectorOutput> outList = new ArrayList<DetectorOutput>(tot);
					long startTime = System.currentTimeMillis();
					for(PreferencesManager loaderPref : activeLoaders){
						Loader trainLoader = null, evalLoader = null;
						if(iManager.getFilteringFlag() || iManager.getTrainingFlag())
							trainLoader = iManager.buildLoader("train", loaderPref);
						if(iManager.getEvaluationFlag())
							evalLoader = iManager.buildLoader("validation", loaderPref);
						boolean filterFlag = true;
						for(LearnerType aList : DetectorMain.readAlgorithmCombinations(iManager)){
							runRELOAD(outList, new DetectionManager(iManager, aList, loaderPref, trainLoader, evalLoader, filterFlag), pBar, index++, tot);
							filterFlag = false;
						}
						if(trainLoader != null)
							trainLoader.flush();
						if(evalLoader != null)
							evalLoader.flush();
					}
					pBar.deleteFrame();
					AppLogger.logInfo(getClass(), "RELOAD Execution time: " + (System.currentTimeMillis() - startTime) + " ms");
					if(outList.size() > 0)
						showDetectorOutputs(outList);
					else AppLogger.logInfo(getClass(), "No outputs will be shown.");
				} catch(Exception ex) {
					AppLogger.logException(getClass(), ex, "");
				}
			}
			
			private void runRELOAD(List<DetectorOutput> outList, DetectionManager detManager, ProgressBar pBar, int index, int tot){
				long partialTime = System.currentTimeMillis();
				AppLogger.logInfo(DetectorMain.class, "Running RELOAD [" + index + "/" + tot + "]: '" + detManager.getTag() + "'");
				DetectorOutput newOut = DetectorMain.runRELOAD(detManager, iManager);
				final String loggedErrors = AppLogger.getErrorsSince(partialTime);
				if(loggedErrors != null){
					Thread t = new Thread(new Runnable(){
				        public void run(){
				        	JOptionPane.showMessageDialog(frame, loggedErrors, "Errors while running RELOAD", JOptionPane.ERROR_MESSAGE);
				        }
				    });
					t.start();
				}	
				if(iManager.getOutputFormat().equalsIgnoreCase("ui") && newOut != null)
					outList.add(newOut);
				pBar.moveNext();
				detManager.flush();
				detManager = null;
				
			}
		}).start();
	}
	
	private LearnerType fromOption(String optionText){
		if(optionText != null && optionText.length() > 0){
			String algName = optionText.substring(0, optionText.indexOf('[')).trim();
			return LearnerType.fromString(algName.trim());
		} else return null;
	}
	
	private JPanel printOptions(boolean isAlg, JPanel panel, String[] algorithms, int fromX, int tabY, int space) {
		JLabel lbl; 
		JButton jb;
		if(algorithms != null && algorithms.length > 0){
			for(String option : algorithms){
				JPanel innerPanel = new JPanel();
				innerPanel.setBackground(Color.WHITE);
				innerPanel.setLayout(new GridBagLayout());
				
				lbl = new JLabel(option);
				lbl.setFont(smallLabelFont);
				lbl.setHorizontalAlignment(SwingConstants.CENTER);
				
				JPanel innerInnerPanel = new JPanel();
				innerInnerPanel.setBackground(Color.WHITE);
				innerInnerPanel.setLayout(new GridLayout(1, (isAlg?3:2), 5, 0));
				
				if(isAlg){
					jb = new JButton("Meta");
					jb.setHorizontalAlignment(SwingConstants.CENTER);
					jb.addActionListener(new ActionListener() { 
						public void actionPerformed(ActionEvent e) { 
							if(!option.contains(".loader")) {
								try {
									LearnerType at = fromOption(option);
									MetaLearnerFrame mlf = new MetaLearnerFrame(iManager, at, BuildUI.this);
									mlf.setVisible(true);
								} catch(Exception ex){
									AppLogger.logException(getClass(), ex, "Unable to open algorithm '" + option + "' preferences");
								}
							} else {
								LoaderFrame lf;
								String type = option.split("@")[0].trim();
								String loaderName = option.split("@")[1].trim();
								try {
									lf = new LoaderFrame(iManager, iManager.getLoaderPreferencesByName(loaderName), LoaderType.valueOf(type));
									lf.setVisible(true);
								} catch(Exception ex){
									AppLogger.logException(getClass(), ex, "Unable to open loader '" + loaderName + "' preferences");
								}
							}
							
						} } );
					innerInnerPanel.add(jb);
				}
				
				jb = new JButton("#");
				jb.setHorizontalAlignment(SwingConstants.CENTER);
				if(!option.contains(".")){
					try {
						LearnerType at = fromOption(option);
						if(at == null || at instanceof MetaLearner)
							jb.setEnabled(false);
					} catch(Exception ex){
						AppLogger.logException(getClass(), ex, "Unable to open algorithm '" + option + "' preferences");
					}
					
				}
				jb.addActionListener(new ActionListener() { 
					public void actionPerformed(ActionEvent e) { 
						if(!option.contains(".")) {
							try {
								LearnerType at = fromOption(option);
								if(at != null && at instanceof BaseLearner){
									AlgorithmSetupFrame asf = new AlgorithmSetupFrame(iManager, at, iManager.loadConfiguration(at, null));
									asf.setVisible(true);
								}
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to open algorithm '" + option + "' preferences");
							}
						} else {
							LoaderFrame lf;
							String type = option.split("@")[0].trim();
							String loaderName = option.split("@")[1].trim();
							try {
								lf = new LoaderFrame(iManager, iManager.getLoaderPreferencesByName(loaderName), LoaderType.valueOf(type));
								lf.setVisible(true);
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to open loader '" + loaderName + "' preferences");
							}
						}
						
					} } );
				innerInnerPanel.add(jb);
					
				jb = new JButton("-");
				jb.setHorizontalAlignment(SwingConstants.CENTER);
				jb.addActionListener(new ActionListener() { 
					public void actionPerformed(ActionEvent e) { 
						if(option.contains(".loader")){
							iManager.removeDataset(option);
						} else {
							iManager.removeAlgorithm(fromOption(option));
						}
						reload();
					} } );
				innerInnerPanel.add(jb);
				
				GridBagConstraints c = new GridBagConstraints();
			    c.insets = new Insets(2, 2, 2, 2);
			    c.weighty = 1.0;
			    c.weightx = 1.0;
			    c.gridx = 0;
			    c.gridy = 0;
			    c.gridheight = 2;
			    c.fill = GridBagConstraints.BOTH; // Use both horizontal & vertical
			    innerPanel.add(lbl, c);
			    c.gridx = 1;
			    c.gridheight = 1;
			    c.gridwidth = 2;
			    c.fill = GridBagConstraints.HORIZONTAL; // Horizontal only
			    innerPanel.add(innerInnerPanel, c);
			    c.gridy = 1;
			    c.gridwidth = 1;
			    c.fill = GridBagConstraints.NONE; // Remember to reset to none
				
				panel.add(innerPanel);
			}
		}
		return panel;
	}
	
	private JPanel printOptions(boolean isAlg, JPanel panel, Map<String, Boolean> dsMap, int fromX, int fromY, int space){
		JLabel lbl; 
		JButton jb;
		if(dsMap != null && dsMap.size() > 0){
			for(String option : dsMap.keySet()){
				JPanel innerPanel = new JPanel();
				innerPanel.setBackground(Color.WHITE);
				innerPanel.setLayout(new GridBagLayout());
				
				lbl = new JLabel(option);
				lbl.setFont(smallLabelFont);
				lbl.setHorizontalAlignment(SwingConstants.CENTER);
				if(dsMap.get(option) != Boolean.TRUE)
					lbl.setEnabled(false);
				
				JPanel innerInnerPanel = new JPanel();
				innerInnerPanel.setBackground(Color.WHITE);
				innerInnerPanel.setLayout(new GridLayout(1, (isAlg?3:2), 5, 0));
				
				if(isAlg){
					jb = new JButton("Meta");
					jb.setHorizontalAlignment(SwingConstants.CENTER);
					jb.addActionListener(new ActionListener() { 
						public void actionPerformed(ActionEvent e) { 
							if(!option.contains(".loader")) {
								try {
									LearnerType at = fromOption(option);
									MetaLearnerFrame mlf = new MetaLearnerFrame(iManager, at, BuildUI.this);
									mlf.setVisible(true);
								} catch(Exception ex){
									AppLogger.logException(getClass(), ex, "Unable to open algorithm '" + option + "' preferences");
								}
							} else {
								LoaderFrame lf;
								String type = option.split("@")[0].trim();
								String loaderName = option.split("@")[1].trim();
								try {
									lf = new LoaderFrame(iManager, iManager.getLoaderPreferencesByName(loaderName), LoaderType.valueOf(type));
									lf.setVisible(true);
								} catch(Exception ex){
									AppLogger.logException(getClass(), ex, "Unable to open loader '" + loaderName + "' preferences");
								}
							}
							
						} } );
					innerInnerPanel.add(jb);
				}
				
				jb = new JButton("#");
				jb.setHorizontalAlignment(SwingConstants.CENTER);
				jb.addActionListener(new ActionListener() { 
					public void actionPerformed(ActionEvent e) { 
						if(!option.contains(".")) {
							try {
								LearnerType at = fromOption(option);
								AlgorithmSetupFrame asf = new AlgorithmSetupFrame(iManager, at, iManager.loadConfiguration(at, null));
								asf.setVisible(true);
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to open algorithm '" + option + "' preferences");
							}
						} else {
							LoaderFrame lf;
							String type = option.split("@")[0].trim();
							String loaderName = option.split("@")[1].trim();
							try {
								lf = new LoaderFrame(iManager, iManager.getLoaderPreferencesByName(loaderName), LoaderType.valueOf(type));
								lf.setVisible(true);
							} catch(Exception ex){
								AppLogger.logException(getClass(), ex, "Unable to open loader '" + loaderName + "' preferences");
							}
						}
						
					} } );
				innerInnerPanel.add(jb);
					
				jb = new JButton("-");
				jb.setHorizontalAlignment(SwingConstants.CENTER);
				jb.addActionListener(new ActionListener() { 
					public void actionPerformed(ActionEvent e) { 
						if(option.contains(".loader")){
							iManager.removeDataset(option);
						} else {
							iManager.removeAlgorithm(fromOption(option));
						}
						reload();
					} } );
				innerInnerPanel.add(jb);
				
				GridBagConstraints c = new GridBagConstraints();
			    c.insets = new Insets(2, 2, 2, 2);
			    c.weighty = 1.0;
			    c.weightx = 1.0;
			    c.gridx = 0;
			    c.gridy = 0;
			    c.gridheight = 2;
			    c.fill = GridBagConstraints.BOTH; // Use both horizontal & vertical
			    innerPanel.add(lbl, c);
			    c.gridx = 1;
			    c.gridheight = 1;
			    c.gridwidth = 2;
			    c.fill = GridBagConstraints.HORIZONTAL; // Horizontal only
			    innerPanel.add(innerInnerPanel, c);
			    c.gridy = 1;
			    c.gridwidth = 1;
			    c.fill = GridBagConstraints.NONE; // Remember to reset to none
				
				panel.add(innerPanel);
			}
		}
		return panel;
	}
	
	private void addToPanel(JPanel root, String tag, JPanel panel, Map<String, JPanel> refMap){
		panel.setBackground(Color.WHITE);
		root.add(panel);
		refMap.put(tag, panel);
	}
	
	private JPanel buildAlgorithmsDatasetsTab(int tabY){
		dataAlgPanel.setBackground(Color.WHITE);
		
		Map<String, Boolean> dsMap = getDatasets();
		
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Data Analysis", TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY);
		dataAlgPanel.setBorder(new CompoundBorder(tb, new EmptyBorder(0, 20, 0, 20)));
		dataAlgPanel.setLayout(new GridLayout(4 + getAlgorithms().length + dsMap.size(), 1, 10, 0));
		
		JLabel mainLabel = new JLabel("Loaders");
		mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainLabel.setFont(titleFont);
		dataAlgPanel.add(mainLabel, BorderLayout.NORTH);
		
		printOptions(false, dataAlgPanel, dsMap, dataAlgPanel.getWidth()/30, 2*labelSpacing, labelSpacing);
		
		JPanel seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setLayout(new GridLayout(1, 3, 50, 0));
		seePrefPanel.setBorder(new EmptyBorder(0, 40, 0, 40));
		//seePrefPanel.setBounds((int) (dataAlgPanel.getWidth()*0.02), (int)(labelSpacing*(getDatasets().length + 1) + bigLabelSpacing), (int) (dataAlgPanel.getWidth()*0.96), bigLabelSpacing);
		
		JButton button = new JButton("Create Loader");
		button.setVisible(true);
		button.setFont(labelFont);
		//button.setBounds(labelSpacing, 0, pathPanel.getWidth()/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				CreateLoaderFrame clf = new CreateLoaderFrame(iManager);
				clf.setVisible(true);
				
			} } );
		seePrefPanel.add(button);
		
		button = new JButton("Add Loader");
		button.setVisible(true);
		button.setFont(labelFont);
		//button.setBounds(labelSpacing, 0, pathPanel.getWidth()/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				JFileChooser jfc = new JFileChooser(new File(iManager.getLoaderFolder()).getAbsolutePath());
				jfc.setMultiSelectionEnabled(true);
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File[] selectedFiles = jfc.getSelectedFiles();
					if(selectedFiles != null){
						boolean updateFlag = false;
						for(File selectedFile : selectedFiles){
							Path pathAbsolute = Paths.get(selectedFile.getAbsolutePath());
					        Path pathBase = Paths.get(new File(iManager.getLoaderFolder()).getAbsolutePath());
							if(!selectedFile.isDirectory() && selectedFile.getName().endsWith(".loader")){
								iManager.addDataset(pathAbsolute.toString());
								updateFlag = true;
							} else JOptionPane.showMessageDialog(frame, "'" + pathBase.relativize(pathAbsolute).toString() + "' is not a '.loader' file");
						}
						if(updateFlag)
							reload();
					}
				}
			} } );
		
		seePrefPanel.add(button);
		button = new JButton("See Loaders");
		button.setVisible(true);
		button.setFont(labelFont);
		//button.setBounds(0, 0, pathPanel.getWidth()/5, labelSpacing);
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
		
		tabY = labelSpacing*(dsMap.size()) + 2*bigLabelSpacing + seePrefPanel.getHeight();
		
		mainLabel = new JLabel("Algorithms");
		//mainLabel.setBounds(dataAlgPanel.getWidth()/4, tabY, dataAlgPanel.getWidth()/2, labelSpacing);
		mainLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainLabel.setFont(titleFont);
		dataAlgPanel.add(mainLabel, BorderLayout.NORTH);
		
		tabY = tabY + labelSpacing;
		
		printOptions(true, dataAlgPanel, getAlgorithms(), dataAlgPanel.getWidth()/20, tabY, labelSpacing);
		
		tabY = tabY + (getAlgorithms().length)*labelSpacing;
		
		seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setLayout(new GridLayout(1, 2, 100, 0));
		seePrefPanel.setBorder(new EmptyBorder(0, 40, 0, 40));
		//seePrefPanel.setBounds((int) (dataAlgPanel.getWidth()*0.02), tabY, (int) (dataAlgPanel.getWidth()*0.96), bigLabelSpacing);
		
		button = new JButton("Add Algorithm");
		button.setVisible(true);
		button.setFont(labelFont);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				String[] algList = new String[DetectionAlgorithm.availableAlgorithms().size()];
				int i = 0;
				for(AlgorithmType at : DetectionAlgorithm.availableAlgorithms()){
					if(!Arrays.asList(getAlgorithms()).contains(at.toString()))
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
                    returnValue = returnValue + item + ", ";
                }
                returnValue = returnValue.length() > 0 ? returnValue.substring(0, returnValue.length()-2) : returnValue;
				if (returnValue != null && returnValue.length() > 0) {
				    iManager.addAlgorithm(returnValue);
				    reload();
				}
			} } );
		seePrefPanel.add(button);
		button = new JButton("Open Algorithms");
		button.setVisible(true);
		button.setFont(labelFont);
		//button.setBounds(0, 0, pathPanel.getWidth()/5, labelSpacing);
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
		
		//dataAlgPanel.setBounds(dataAlgPanel.getX(), dataAlgPanel.getY(), dataAlgPanel.getWidth(), tabY);
		
		return dataAlgPanel;
	}

	private Map<String, Boolean> getDatasets() {
		List<PreferencesManager> lList = iManager.readLoaders();
		Map<String, Boolean> dsMap = new HashMap<>();
		for(PreferencesManager lPref : lList){
			boolean isValid = true; // iManager.isValid(lPref);
			if(lPref.getPreference(Loader.LOADER_TYPE) != null){
				if(lPref.getPreference(Loader.LOADER_TYPE).equals("CSVALL")){
					dsMap.put("CSV @ " + lPref.getFilename(), isValid);
				} else {
					dsMap.put(lPref.getPreference(Loader.LOADER_TYPE) + " @ " + lPref.getFilename(), isValid);
				}
			}
		}
		return dsMap;
	}

	private String[] getAlgorithms(){
		int i = 0;
		List<AlgorithmFamily> family = new LinkedList<AlgorithmFamily>();
		List<LearnerType> aComb = DetectorMain.readAlgorithmCombinations(iManager);
		String[] algStrings = new String[aComb.size()];
		for(LearnerType aList : aComb){
			try {
				family = DetectionAlgorithm.getFamily(aList);
			} catch(Exception ex){
				family.add(AlgorithmFamily.MIXED);
			}
			algStrings[i++] = aList.toString() + " " + Arrays.toString(family.toArray());
		}
		return algStrings;
	}
	
	private JPanel buildPathsTab(int tabY){
		pathPanel.setBackground(Color.WHITE);
		
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Paths", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY);
		//pathPanel.setBounds(frame.getWidth()/3 + 10, tabY, frame.getWidth()/3 - 20, 9*bigLabelSpacing + 2*labelSpacing);
		pathPanel.setBorder(new CompoundBorder(tb, new EmptyBorder(0, 20, 0, 20)));
		pathPanel.setLayout(new GridLayout(10, 1, 100, 10));
		
		addToPanel(pathPanel, SETUP_LABEL_PREFFILE, createLPanel(SETUP_LABEL_PREFFILE, pathPanel, bigLabelSpacing, DetectorMain.DEFAULT_PREF_FILE, "Name of the main file for RELOAD preferences. Relative path from the JAR location."), setupMap);
		addToPanel(pathPanel, PATH_LABEL_INPUT_FOLDER, createFCHPanel(PATH_LABEL_INPUT_FOLDER, pathPanel, 2*bigLabelSpacing, iManager.getInputFolder(), true, "Name of the 'input folder' for RELOAD preferences. Relative path from the JAR location."), pathMap);
		addToPanel(pathPanel, PATH_LABEL_OUTPUT_FOLDER, createFCHPanel(PATH_LABEL_OUTPUT_FOLDER, pathPanel, 3*bigLabelSpacing, iManager.getOutputFolder(), true, "Name of the 'output folder' for RELOAD preferences, where output files will be placed. Relative path from the JAR location."), pathMap);
		addToPanel(pathPanel, PATH_LABEL_CONF_FOLDER, createFCHPanel(PATH_LABEL_CONF_FOLDER, pathPanel, 4*bigLabelSpacing, iManager.getConfigurationFolder(), true, "Name of the 'configuration folder' for RELOAD preferences. Relative path from the JAR location."), pathMap);
		addToPanel(pathPanel, PATH_LABEL_DATASETS_FOLDER, createFCHPanel(PATH_LABEL_DATASETS_FOLDER, pathPanel, 5*bigLabelSpacing, iManager.getDatasetsFolder(), true, "Name of the folder containing datasets. Relative path from 'input folder'."), pathMap);
		addToPanel(pathPanel, PATH_LABEL_LOADERS_FOLDER, createFCHPanel(PATH_LABEL_LOADERS_FOLDER, pathPanel, 5*bigLabelSpacing, iManager.getLoaderFolder(), true, "Name of the folder containing loaders."), pathMap);
		addToPanel(pathPanel, PATH_LABEL_SETUP_FOLDER, createFCHPanel(PATH_LABEL_SETUP_FOLDER, pathPanel, 6*bigLabelSpacing, iManager.getSetupFolder(), true, "Name of the ''setup folder' for RELOAD preferences. Relative path from the JAR location."), pathMap);
		addToPanel(pathPanel, PATH_LABEL_SCORES_FOLDER, createFCHPanel(PATH_LABEL_SCORES_FOLDER, pathPanel, 7*bigLabelSpacing, iManager.getScoresFolder(), true, "Name of the folder containing partial scores. Relative path from the JAR location."), pathMap);
		addToPanel(pathPanel, PATH_LABEL_DETECTION_PREFERENCES, createFCHPanel(PATH_LABEL_DETECTION_PREFERENCES, pathPanel, 8*bigLabelSpacing, iManager.getDetectionPreferencesFile(), false, "Name of the file for scoring preferences, to aggregate different checkers. Relative path from 'input folder'."), pathMap);
		
		JPanel seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setLayout(new GridLayout(1, 1));
		//seePrefPanel.setBounds((int) (setupPanel.getWidth()*0.02), 9*bigLabelSpacing, (int)(setupPanel.getWidth()*0.96), bigLabelSpacing);
		
		JButton button = new JButton("Open RELOAD Preferences");
		button.setVisible(true);
		button.setFont(labelFont);
		//button.setBounds(0, 0, pathPanel.getWidth()*2/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				try {
					Desktop.getDesktop().open(new File(DetectorMain.DEFAULT_PREF_FILE));
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
		//setupPanel.setBounds(10, tabY, frame.getWidth()/3 - 20, 7*optionSpacing + 6*bigLabelSpacing);
		setupPanel.setBorder(new CompoundBorder(tb, new EmptyBorder(0, 20, 0, 20)));
		setupPanel.setLayout(new GridLayout(11, 1, 50, 0));
		
		addToPanel(setupPanel, SETUP_LABEL_METRIC, createLCBPanel(SETUP_LABEL_METRIC, setupPanel, optionSpacing, MetricType.values(), iManager.getMetricType(), InputManager.METRIC, "Reference metric to be used to decide if a combination of algorithms' parameters is better than another."), setupMap);
		addToPanel(setupPanel, SETUP_LABEL_METRIC, createLCBPanel(SETUP_LABEL_OUTPUT, setupPanel, optionSpacing, new String[]{"ui", "basic", "text", "image"}, iManager.getOutputFormat(), InputManager.OUTPUT_FORMAT, "Output Type, either i) ui, ii) print just final results, iii) text verbose, iv) image files"), setupMap);
		
		JPanel seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setLayout(new GridLayout(1, 1));
		//seePrefPanel.setBounds((int) (setupPanel.getWidth()*0.02), 4*optionSpacing - optionSpacing/3, (int) (setupPanel.getWidth()*0.96), bigLabelSpacing);
		
		JButton button = new JButton("Feature Selection Strategies");
		button.setFont(labelFont);
		//button.setBounds(0, 0, setupPanel.getWidth()*3/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				FeatureSelectionFrame fsf;
				try {
					fsf = new FeatureSelectionFrame(iManager, iManager.getFeatureSelectors());
					fsf.setVisible(true);
				} catch(Exception ex){
					AppLogger.logException(getClass(), ex, "Unable to open feature selection preferences");
				}
			} } );
		seePrefPanel.add(button);
		seePrefPanel.setVisible(iManager.getFilteringFlag());		
		
		addToPanel(setupPanel, SETUP_LABEL_FILTERING, createLCKPanel(SETUP_LABEL_FILTERING, setupPanel, 3*optionSpacing, iManager.getFilteringFlag(), new JPanel[]{seePrefPanel}, InputManager.FILTERING_NEEDED_FLAG, "Specifies if Feature Selection is needed.", true, true), setupMap);
		setupPanel.add(seePrefPanel);
		
		comp = createLCKPanel(SETUP_PREDICT, setupPanel, 2*optionSpacing, iManager.getPredictMisclassificationsFlag(), new JPanel[]{}, InputManager.PREDICT_MISCLASSIFICATIONS, "Specifies if Misclassification Prediction should be applied.", true, false);
		comp.setVisible(iManager.getFilteringFlag());
		addToPanel(setupPanel, SETUP_PREDICT, comp, setupMap);
		
		//comp = createLCBPanel(SETUP_IND_SELECTION, setupPanel, 5*optionSpacing, InputManager.getIndicatorSelectionPolicies(), iManager.getDataSeriesBaseDomain(), InputManager.INDICATOR_SELECTION, "<html><p>Specifies the policy to aggregate selected features. <br> 'NONE' just takes all the selected features individually, <br> 'UNION' considers the n-dimensional space composed by all the n selected features (all at once), <br> 'SIMPLE' merges 'NONE' and 'UNION', <br> 'MULTIPLE_UNION' considers j-dimensional subspaces (0 &lt j &lt= n), constituted by the j top-ranked features, <br> 'PEARSON' extends 'NONE' by considering couples, triples, quadruples, etc. of features that have a pearson correlation stronger than a given threshold, while <br> 'ALL' merges 'PEARSON' and 'UNION'.</p></html>");
		//comp.setVisible(iManager.getTrainingFlag());
		//addToPanel(setupPanel, SETUP_IND_SELECTION, comp, setupMap);
		
		comp = createLTPanel(SETUP_KFOLD_VALIDATION, setupPanel, 7*optionSpacing, Integer.toString(iManager.getKFoldCounter()), InputManager.KFOLD_COUNTER, iManager, "<html><p>Specifies the K value for the K-Fold parameter. <br> Briefly, k-fold cross-validation is a resampling procedure used to evaluate machine learning models on a limited data sample. <br> The procedure has a single parameter called k that refers to the number of groups that a given data sample is to be split into. <br> As such, the procedure is often called k-fold cross-validation. <br> When a specific value for k is chosen, it may be used in place of k in the reference to the model, such as k=10 becoming 10-fold cross-validation.</p></html>", true);
		comp.setVisible(iManager.getTrainingFlag());
		addToPanel(setupPanel, SETUP_LABEL_TRAINING, createLCKPanel(SETUP_LABEL_TRAINING, setupPanel, 5*optionSpacing, iManager.getTrainingFlag(), comp, InputManager.TRAIN_NEEDED_FLAG, "Specifies if Training is needed.", true, true), setupMap);
		addToPanel(setupPanel, SETUP_KFOLD_VALIDATION, comp, setupMap);
		
		boolean[] result = hasAlgorithmType();
		boolean hasBase = result[0];
		boolean hasMeta = result[1];
		//boolean hasSliding = result[2];
		comp = createLCKPanel(SETUP_FORCE_TRAINING, setupPanel, 5*optionSpacing, iManager.getForceTrainingFlag(), comp, InputManager.FORCE_TRAINING, "Specifies if existing data about a past training of this algorithm can be re-used.", hasBase, false);
		comp.setVisible(iManager.getTrainingFlag());
		addToPanel(setupPanel, SETUP_FORCE_TRAINING, comp, setupMap);
		
		comp = createLCKPanel(SETUP_FORCE_PARALLEL, setupPanel, 5*optionSpacing, iManager.getParallelTrainingFlag(), comp, InputManager.PARALLEL_TRAINING, "Specifies if explouts CPU multi-threading.", iManager.getForceTrainingFlag(), false);
		comp.setVisible(iManager.getTrainingFlag());
		addToPanel(setupPanel, SETUP_FORCE_PARALLEL, comp, setupMap);
		
		comp = createLCKPanel(SETUP_FORCE_BASELEARNERS, setupPanel, 5*optionSpacing, iManager.getForceBaseLearnersFlag(), comp, InputManager.FORCE_TRAINING_BASELEARNERS, "Specifies if, during training of a meta-learner, all base-learners need to be trained or if existring results could be used to speedup the process.", hasMeta, false);
		comp.setVisible(iManager.getTrainingFlag());
		addToPanel(setupPanel, SETUP_FORCE_BASELEARNERS, comp, setupMap);
	
		/*comp = createLTPanel(SETUP_LABEL_SLIDING_POLICY, setupPanel, 8*optionSpacing, iManager.getSlidingPolicies(), InputManager.SLIDING_POLICY, iManager, "<html><p>(ONLY if using sliding window algorithms) <br> Specifies the policy that makes the window slide.</p></html>", hasSliding);
		comp.setVisible(iManager.getTrainingFlag());
		addToPanel(setupPanel, SETUP_LABEL_SLIDING_POLICY, comp, setupMap);
		
		comp = createLTPanel(SETUP_LABEL_WINDOW_SIZE, setupPanel, 9*optionSpacing, iManager.getSlidingWindowSizes(), InputManager.SLIDING_WINDOW_SIZE, iManager, "<html><p>(ONLY if using sliding window algorithms) <br> Specifies the size of the sliding window.</p></html>", hasSliding);
		comp.setVisible(iManager.getTrainingFlag());
		addToPanel(setupPanel, SETUP_LABEL_WINDOW_SIZE, comp, setupMap);
		
		seePrefPanel = new JPanel();
		seePrefPanel.setBackground(Color.WHITE);
		seePrefPanel.setLayout(new GridLayout(1, 1));
		//seePrefPanel.setBounds((int) (setupPanel.getWidth()*0.02), 11*optionSpacing, (int) (setupPanel.getWidth()*0.96), bigLabelSpacing);
		
		button = new JButton("Open Optimization Preferences");
		button.setVisible(true);
		button.setFont(labelFont);
		//button.setBounds(0, 0, setupPanel.getWidth()*3/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				try {
					Desktop.getDesktop().open(new File(iManager.getInputFolder() + iManager.getDetectionPreferencesFile()));
				} catch (IOException e1) {
					AppLogger.logException(getClass(), e1, "");
				}
			} } );
		seePrefPanel.setVisible(iManager.getOptimizationFlag());
		seePrefPanel.add(button);
		
		addToPanel(setupPanel, SETUP_LABEL_FILTERING, createLCKPanel(SETUP_LABEL_OPTIMIZATION, setupPanel, 10*optionSpacing, iManager.getOptimizationFlag(), seePrefPanel, InputManager.OPTIMIZATION_NEEDED_FLAG, "Specifies if Optimization is needed."), setupMap);
		
		setupPanel.add(seePrefPanel);*/
		
		addToPanel(setupPanel, SETUP_LABEL_EVALUATION, createLCKPanel(SETUP_LABEL_EVALUATION, setupPanel, (int)(12.5*optionSpacing), iManager.getEvaluationFlag(), new JPanel[]{}, InputManager.EVALUATION_NEEDED_FLAG, "Specifies if Evaluation is needed.", true, true), setupMap);
		
		return setupPanel;
	}
	
	private boolean[] hasAlgorithmType() {
		boolean hasMeta = false, hasSliding = false, hasBase = false;
		List<LearnerType> algList = DetectorMain.readAlgorithmCombinations(iManager);
		if(algList != null){
			for(LearnerType lType : algList){
				if(lType.isSliding())
					hasSliding = true;
				else if(lType instanceof MetaLearner)
					hasMeta = true;
				else hasBase = true;
			}
		}
		return new boolean[]{hasBase, hasMeta, hasSliding};
	}


	public JPanel createLPanel(String textName, JPanel root, int panelY, String textFieldText, String tooltipText){
		return createLPanel(false, textName, root, (int) (root.getWidth()*0.02), panelY, textFieldText, tooltipText);
	}
	
	public JPanel createLPanel(boolean bold, String textName, JPanel root, int panelX, int panelY, String textFieldText, String tooltipText){
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		//panel.setBounds(panelX, panelY, (int) (root.getWidth()*0.96), 30);
		panel.setLayout(new GridLayout(1, 2));
		
		JLabel lbl = new JLabel(textName);
		lbl.setFont(labelFont);
		if(bold)
			lbl.setFont(lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));
		//lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(tooltipText != null)
			lbl.setToolTipText(tooltipText);
		panel.add(lbl);
		
		JLabel lbldata = new JLabel(textFieldText);
		lbldata.setFont(labelFont);
		//lbldata.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, labelSpacing);
		lbldata.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbldata);
		
		return panel;
	}
	
	private JPanel createLTPanel(String textName, JPanel root, int panelY, String textFieldText, String fileTag, InputManager iManager, String tooltipText, boolean isEnabled){
		JPanel panel = new JPanel();
		//panel.setBounds((int) (root.getWidth()*0.02), panelY, (int) (root.getWidth()*0.96), labelSpacing);
		panel.setLayout(new GridLayout(1, 2));
		
		JLabel lbl = new JLabel(textName);
		lbl.setFont(labelFont);
		//lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(tooltipText != null)
			lbl.setToolTipText(tooltipText);
		panel.add(lbl);
		
		JTextField textField = new JTextField();
		textField.setText(textFieldText);
		textField.setFont(labelFont);
		//textField.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, labelSpacing);
		textField.setEnabled(isEnabled);
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
	        		iManager.updatePreference(fileTag, textField.getText(), false, true);
	        	}
			}
		});
		
		panel.add(textField);
		root.add(panel);
		
		return panel;
	}
	
	private JPanel createFCHPanel(String textName, JPanel root, int panelY, String textFieldText, boolean folderFlag, String tooltipText){
		JPanel panel = new JPanel();
		//panel.setBounds((int) (root.getWidth()*0.02), panelY, (int) (root.getWidth()*0.96), bigLabelSpacing);
		panel.setLayout(new GridLayout(1, 2));
		
		JLabel lbl = new JLabel(textName);
		//lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, labelSpacing);
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(tooltipText != null)
			lbl.setToolTipText(tooltipText);
		panel.add(lbl);
		
		JButton button = new JButton(textFieldText);
		button.setVisible(true);
		//button.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, labelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				JFileChooser jfc = new JFileChooser(new File("").getAbsolutePath());
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = jfc.getSelectedFile();
					Path pathAbsolute = Paths.get(selectedFile.getAbsolutePath());
			        Path pathBase = Paths.get(new File("").getAbsolutePath());
					if(!folderFlag || selectedFile.isDirectory()){
						button.setText(pathBase.relativize(pathAbsolute).toString());
						iManager.updatePreference(panelToPreference(textName), pathBase.relativize(pathAbsolute).toString().replace('\\', File.separatorChar), true, true);
					} else JOptionPane.showMessageDialog(frame, "'" + pathBase.relativize(pathAbsolute).toString() + "' is not a folder");
				}
			} } );
		panel.add(button);
		
		root.add(panel);
		
		return panel;
	}
	
	private JPanel createLCKPanel(String textName, JPanel root, int panelY, boolean checked, JPanel comp, String fileTag, String tooltipText, boolean isEnabled, boolean isBold){
		return createLCKPanel(textName, root, panelY, checked, new JPanel[]{comp}, fileTag, tooltipText, isEnabled, isBold);
	}
	
	private JPanel createLCKPanel(String textName, JPanel root, int panelY, boolean checked, JPanel[] comp, String fileTag, String tooltipText, boolean isEnabled, boolean isBold){
		JPanel panel = new JPanel();
		//panel.setBounds((int) (root.getWidth()*0.02), panelY, (int) (root.getWidth()*0.96), labelSpacing);
		panel.setLayout(new GridLayout(1, 1));
		
		JCheckBox cb = new JCheckBox(textName);
		cb.setSelected(checked);
		cb.setFont(isBold ? bigFont : smallLabelFont);
		cb.setEnabled(isEnabled);
		//cb.setBounds(root.getWidth()/4, 0, root.getWidth()/2, labelSpacing);
		cb.setHorizontalAlignment(SwingConstants.CENTER);
		if(tooltipText != null){
			cb.setToolTipText(tooltipText);
		}
		
		cb.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent event) {
		        JCheckBox cb = (JCheckBox) event.getSource();
		        if(comp != null && comp.length > 0){
			        for(JPanel linkedPanel : comp){
			        	if(linkedPanel != null)
			        		linkedPanel.setVisible(cb.isSelected());
			        }
		        }
		        if(!isUpdating){
		        	iManager.updatePreference(fileTag, cb.isSelected() ? "1" : "0", true, true);
		        	reload();
		        }
		        	
		    }
		});
		
		panel.add(cb);
		
		root.add(panel);
		
		return panel;
	}
	
	private JPanel createLCBPanel(String textName, JPanel root, int panelY, Object[] itemList, Object selected, String fileTag, String tooltipText){
		JPanel panel = new JPanel();
		//panel.setBounds((int) (root.getWidth()*0.02), panelY, (int) (root.getWidth()*0.96), labelSpacing);
		panel.setLayout(new GridLayout(1, 2));
		
		JLabel lbl = new JLabel(textName);
		lbl.setFont(labelFont);
		//lbl.setBounds(root.getWidth()/10, 0, root.getWidth()*2/5, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(tooltipText != null)
			lbl.setToolTipText(tooltipText);
		panel.add(lbl);
		
		JComboBox<Object> comboBox = new JComboBox<Object>();
		comboBox.setFont(labelFont);
		//comboBox.setBounds(root.getWidth()/2, 0, root.getWidth()*2/5, labelSpacing);

		if(itemList != null){
			for(Object ob : itemList){
				comboBox.addItem(ob);
			}
			comboBox.addActionListener (new ActionListener () {
			    public void actionPerformed(ActionEvent e) {
			        if(!isUpdating){
			        	String newValue = comboBox.getSelectedItem().toString();
			        	if(comboBox.getSelectedItem().toString().equals("FSCORE") || comboBox.getSelectedItem().toString().equals("SAFESCORE")){
			        		String s = (String)JOptionPane.showInputDialog(
				                    frame, "Set parameter beta for F-Score or Safe-Score (beta > 0)", "FSCORE / SAFESCORE beta",
				                    JOptionPane.PLAIN_MESSAGE, null, null, "");
							if ((s != null) && (s.trim().length() > 0) && AppUtility.isNumber(s.trim())) {
								newValue = newValue + "(" + s + ")";
							} else newValue = newValue + "(1)";
			        	}
			        	if(comboBox.getSelectedItem().toString().equals("CONFIDENCE_ERROR")){
			        		String s = (String)JOptionPane.showInputDialog(
				                    frame, "Set parameter P for Confidence Error (P in [0, 1]). the higher the P, the more relevance to FN", "Confidence Error P",
				                    JOptionPane.PLAIN_MESSAGE, null, null, "");
							if ((s != null) && (s.trim().length() > 0) && AppUtility.isNumber(s.trim())) {
								newValue = newValue + "(" + s + ")";
							} else newValue = newValue + "(1)";
			        	}
			        	if(comboBox.getSelectedItem().toString().equals("NO_PREDICTION")){
			        		String s = (String)JOptionPane.showInputDialog(
				                    frame, "Set Tolerable Hazard Rate % [0, 100] for No-Prediction Area.", "Tolerable Hazard Rate",
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
			        	iManager.updatePreference(fileTag, newValue, false, true);
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
		if(outList.size() > 0){
			of.buildSummaryPanel(outList);
			for(DetectorOutput dOut : outList){
				of.addOutput(dOut);
			}
		}
		of.setVisible(true);
	}
	
}
