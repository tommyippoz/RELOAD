/**
 * 
 */
package ippoz.reload.executable.ui;

import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.decisionfunction.DecisionFunctionType;
import ippoz.reload.metric.Overlap_Metric;
import ippoz.reload.output.DetectorOutput;
import ippoz.reload.output.LabelledResult;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

/**
 * @author Tommy
 *
 */
public class OutputDetailFrame {
	
	private static int NUM_INTERVALS = 100;
	
	private JFrame detFrame;
	
	private DetectorOutput dOut;
	
	private JPanel mainPanel;
	
	private Font labelFont;
	
	private Font labelBoldFont;
	
	private ValueSeries algorithmScores;
	
	private JTextField intTextField;
	
	private JComboBox<DecisionFunctionType> cbDecisionFunction;
	
	private boolean isUpdating;
	
	private double minValue;
	
	private double maxValue;
	
	private double minRefValue;
	
	private double maxRefValue;
	
	private boolean logScale;
	
	private boolean norm;
	
	private boolean decisionFunctionFlag;
	
	private int numIntervals;
	
	private DecisionFunction dFunction;
	
	public OutputDetailFrame(DetectorOutput dOut) {
		this.dOut = dOut;
		
		norm = false;
		logScale = false;
		decisionFunctionFlag = false;
		
		numIntervals = NUM_INTERVALS;
		algorithmScores = createScoresValueSeries();
		dFunction = createDefaultDecisionFunction();
		
		setMinMaxValues();
		minValue = minRefValue;
		maxValue = maxRefValue;
		
		buildFrame();
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		labelFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelBoldFont = new Font("Times", Font.BOLD, (int)((15 + rate)/2));
	}
	
	private void setMinMaxValues() {
		minRefValue = Double.POSITIVE_INFINITY;
		maxRefValue = Double.NEGATIVE_INFINITY;
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(containsPostiveLabel(list)) {
				for(LabelledResult lr : list){
					if(Double.isFinite(lr.getValue().getScore())){
						if(lr.getValue().getScore() > maxRefValue)
							maxRefValue = lr.getValue().getScore();
						if(lr.getValue().getScore() < minRefValue)
							minRefValue = lr.getValue().getScore();
					}
						
				}
			}
		}
	}
	
	private DecisionFunction createDefaultDecisionFunction() {
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(list != null && list.size() > 0){
				if(list.get(0) != null){
					return list.get(0).getValue().getDecisionFunction();
				}
			}
		}
		return null;
	}
	

	private ValueSeries createScoresValueSeries() {
		ValueSeries series = new ValueSeries();
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(containsPostiveLabel(list)) {
				for(LabelledResult lr : list){
					if(Double.isFinite(lr.getValue().getScore()))
						series.addValue(lr.getValue().getScore());
				}
			}
		}
		return series;
	}
	

	private boolean containsPostiveLabel(List<LabelledResult> list){
		for(LabelledResult lr : list){
			if(lr.getLabel())
				return true;
		}
		return false;
	}

	private void reload() {
		isUpdating = true;
		detFrame.setVisible(false);
		detFrame.getContentPane().removeAll();
		buildFrame();
		buildMainPanel();
		setVisible(true);
		isUpdating = false;
	}

	public void setVisible(boolean b) {
		if(detFrame != null){
			detFrame.getContentPane().setBackground(Color.WHITE);
			detFrame.add(mainPanel);
			detFrame.setLocationRelativeTo(null);
			detFrame.setVisible(b);
		}
	}

	private void buildFrame(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		detFrame = new JFrame();
		detFrame.setTitle("Detail and Plots of Outputs");
		if(screenSize.getWidth() > 1000)
			detFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.6), (int)(screenSize.getHeight()*0.75));
		else detFrame.setBounds(0, 0, 800, 480);
		detFrame.setBackground(Color.WHITE);
		detFrame.setResizable(false);
	}
	
	public void buildMainPanel(){
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBounds(0, 0, detFrame.getWidth() - 10, detFrame.getHeight() - 10);
		mainPanel.setLayout(new BorderLayout());
		
		mainPanel.add(buildChartPanel(), BorderLayout.CENTER);
		
		JPanel headerPanel = new JPanel();
		headerPanel.setBackground(Color.WHITE);
		headerPanel.setBorder(new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Parameters", TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY));
		headerPanel.setLayout(new GridLayout(2, 1));
		
		JPanel firstHeaderPanel = new JPanel();
		firstHeaderPanel.setBackground(Color.WHITE);
		firstHeaderPanel.setLayout(new GridLayout(1, 7));
		
		JLabel lbl = new JLabel("Number of Rectangles");
		lbl.setFont(labelFont);
		lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		firstHeaderPanel.add(lbl);
		
		intTextField = new JTextField(String.valueOf(numIntervals));
		intTextField.setFont(labelFont);
		firstHeaderPanel.add(intTextField);
		intTextField.setColumns(5);
		intTextField.getDocument().addDocumentListener(new DocumentListener() {
			  
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
				if (intTextField.getText() != null && intTextField.getText().length() > 0){
	        		if(AppUtility.isInteger(intTextField.getText()) && Integer.parseInt(intTextField.getText()) > 0) {
						numIntervals = Integer.parseInt(intTextField.getText());
		        		reload();
	        		}
	        	}
			}
		});
		
		JCheckBox logCB = new JCheckBox("Logaritmic y-Axis");
		logCB.setSelected(logScale);
		logCB.setBorder(new EmptyBorder(5, 10, 5, 10));
		logCB.setFont(labelFont);
		logCB.setHorizontalAlignment(SwingConstants.CENTER);
		logCB.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent event) {
		    	logScale = !logScale;
		        reload();
		    }
		});
		firstHeaderPanel.add(logCB);
		
		JCheckBox normCB = new JCheckBox("Normalize Scores");
		normCB.setSelected(norm);
		normCB.setBorder(new EmptyBorder(5, 10, 5, 10));
		normCB.setFont(labelFont);
		normCB.setHorizontalAlignment(SwingConstants.CENTER);
		normCB.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent event) {
		    	norm = !norm;
		        reload();
		    }
		});
		firstHeaderPanel.add(normCB);
		
		JPanel intervalPanel = new JPanel();
		intervalPanel.setBackground(Color.WHITE);
		intervalPanel.setLayout(new GridLayout(1, 2));
		
		DecimalFormat dfMin = new DecimalFormat(Double.isFinite(minValue) ? (Math.abs(minValue) > 1000000 || (minValue != 0 && Math.abs(minValue) < 0.001) ? "0.00E00" : "0.000") : "0.000"); 
		DecimalFormat dfMax = new DecimalFormat(Double.isFinite(maxValue) ? (Math.abs(maxValue) > 1000000 || (maxValue != 0 && Math.abs(maxValue) < 0.001) ? "0.00E00" : "0.000") : "0.000");
		
		lbl = new JLabel("Left Bound: " + dfMin.format(minValue));
		lbl.setFont(labelFont);
		lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	String s = (String)JOptionPane.showInputDialog(
	                    detFrame, "Set left threshold for algorithm scores to be shown (" + minRefValue + "<threshold<=" + dfMax.format(maxValue) + "). \n Leave blank if you want to default to the minimum.", "Scores Threshold",
	                    JOptionPane.PLAIN_MESSAGE, null, null, "");
				if ((s != null) && (s.trim().length() > 0) && AppUtility.isNumber(s.trim())) {
					double newThreshold = Double.valueOf(s.trim());
					if(newThreshold >= minRefValue && newThreshold <= maxValue){
						minValue = newThreshold;
						reload();
					} else JOptionPane.showMessageDialog(
	        				detFrame, "Value is not in the [" + minRefValue + ", " + dfMax.format(maxValue) + "] range", "Input Error",
		                    JOptionPane.ERROR_MESSAGE);
				} else if((s != null) && (s.trim().length() == 0)){
					minValue = minRefValue;
					reload();
				}
				else JOptionPane.showMessageDialog(detFrame, "Value is not a number", "Input Error", JOptionPane.ERROR_MESSAGE);
            }

        });		
		firstHeaderPanel.add(lbl);
		
		lbl = new JLabel("Right Bound: " + dfMax.format(maxValue));
		lbl.setFont(labelFont);
		lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	String s = (String)JOptionPane.showInputDialog(
	                    detFrame, "Set right threshold for algorithm scores to be shown (" + dfMin.format(minValue) + "<threshold<=" + maxRefValue + ")\n Leave blank if you want to default to the maximum.", "Scores Threshold",
	                    JOptionPane.PLAIN_MESSAGE, null, null, "");
				if ((s != null) && (s.trim().length() > 0) && AppUtility.isNumber(s.trim())) {
					double newThreshold = Double.valueOf(s.trim());
					if(newThreshold >= minValue && newThreshold <= maxRefValue){
						maxValue = newThreshold;
						reload();
					} else JOptionPane.showMessageDialog(
	        				detFrame, "Value is not in the [" + dfMin.format(minValue) + ", " + maxRefValue + "] range", "Input Error",
		                    JOptionPane.ERROR_MESSAGE);
				} else if((s != null) && (s.trim().length() == 0)){
					maxValue = maxRefValue;
					reload();
				}else JOptionPane.showMessageDialog(detFrame, "Value is not a number", "Input Error", JOptionPane.ERROR_MESSAGE);
            }

        });	
		intervalPanel.add(lbl);
		
		firstHeaderPanel.add(lbl);
		
		JPanel secondHeaderPanel = new JPanel();
		secondHeaderPanel.setBackground(Color.WHITE);
		secondHeaderPanel.setLayout(new GridLayout(1, 4));
		
		String suggDecision = null;
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(list != null && list.size() > 0){
				if(list.get(0) != null){
					suggDecision = list.get(0).getValue().getDecisionFunction().getName();
					break;
				}
			}
		}
		
		lbl = new JLabel("Decision Function");
		lbl.setFont(labelFont);
		lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	String desc = "Description of available Decision Functions \n";
            	for(DecisionFunctionType dft : DecisionFunctionType.values()){
            		desc = desc + "- " + dft.toString() + ": " + DecisionFunction.getParameterDetails(dft.toString()) + "\n";
            	}
            	JOptionPane.showMessageDialog(detFrame, desc,
					    "Decision Function Detail", JOptionPane.INFORMATION_MESSAGE);
            }

        });
		secondHeaderPanel.add(lbl);
		
		cbDecisionFunction = new JComboBox<DecisionFunctionType>(DecisionFunctionType.values());
		if(dFunction != null)
			cbDecisionFunction.setSelectedItem(dFunction.getDecisionFunctionType());
		else cbDecisionFunction.setSelectedItem(suggDecision);
		cbDecisionFunction.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		        if(!isUpdating){
		        	String newValue = cbDecisionFunction.getSelectedItem().toString();
		        	
		        	String descriptionString = "Insert parameter for " + newValue;
		        	if(newValue.contains("DOUBLE_THRESHOLD")){
		        		descriptionString = "Insert lower bound for " + newValue;
		        		String s = (String)JOptionPane.showInputDialog(
		        				detFrame, descriptionString, "Define Decision Function Parameter",
			                    JOptionPane.PLAIN_MESSAGE, null, null, "");
						if ((s != null) && (s.trim().length() > 0) && AppUtility.isNumber(s.trim())) {
							newValue = newValue + "(" + s + ",";
						} else newValue = newValue + "(0,";
						descriptionString = "Insert upper bound for " + newValue;
						s = (String)JOptionPane.showInputDialog(
		        				detFrame, descriptionString, "Define Decision Function Parameter",
			                    JOptionPane.PLAIN_MESSAGE, null, null, "");
						if ((s != null) && (s.trim().length() > 0) && AppUtility.isNumber(s.trim())) {
							newValue = newValue + s + ")";
						} else newValue = newValue + "1)";
		        	} else {
			        	if(newValue.contains("IQR") || newValue.contains("CONF")){
			        		descriptionString = "Insert range parameter (range >= 0) for " + newValue;
			        	} else if(newValue.contains("STATIC_THRESHOLD")){
			        		descriptionString = "Insert threshold parameter (any real number) for " + newValue;
			        	} else if(newValue.contains("THRESHOLD")){
			        		descriptionString = "Insert threshold parameter (0 <= threshold <= 1) for " + newValue;
			        	} else if(newValue.contains("CLUSTER")){
			        		descriptionString = "Insert cluster parameter, which depends either on VAR or STD of the nearest cluster, eventually multiplied for a given constant (e.g., 0.1VAR) for " + newValue;
			        	}
			        	
			        	String s = (String)JOptionPane.showInputDialog(
		        				detFrame, descriptionString, "Define Decision Function Parameter",
			                    JOptionPane.PLAIN_MESSAGE, null, null, "");
						if ((s != null) && (s.trim().length() > 0) && AppUtility.isNumber(s.trim())) {
							newValue = newValue + "(" + s + ")";
						} else newValue = newValue + "(1)";
		        	}
		        	
		        	dFunction = DecisionFunction.buildDecisionFunction(algorithmScores, newValue);
		        	if(dFunction != null){
		        		cbDecisionFunction.setSelectedItem(dFunction.getDecisionFunctionType());
		        		reload();
		        	} 
		    	}
		    }
		});
		secondHeaderPanel.add(cbDecisionFunction);
		
		JCheckBox decisionFunctionCB = new JCheckBox("Apply Decision");
		decisionFunctionCB.setSelected(decisionFunctionFlag);
		decisionFunctionCB.setBorder(new EmptyBorder(0, 10, 0, 10));
		decisionFunctionCB.setFont(labelFont);
		decisionFunctionCB.setHorizontalAlignment(SwingConstants.CENTER);
		decisionFunctionCB.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent event) {
		    	decisionFunctionFlag = !decisionFunctionFlag;
		        reload();
		    }
		});
		
		secondHeaderPanel.add(decisionFunctionCB);
		
		JButton butSave = new JButton("Save Data");
		butSave.setVisible(true);
		butSave.setFont(labelBoldFont);
		butSave.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				ChartPanel cp;
				try {
					JFileChooser jfc = new JFileChooser(new File(System.getProperty("user.dir")));
					int returnValue = jfc.showSaveDialog(null);
					if (returnValue == JFileChooser.APPROVE_OPTION) {
						File selectedFile = jfc.getSelectedFile();
						String basicPath = selectedFile.getAbsolutePath();
						if(basicPath.contains("."))
							basicPath = basicPath.split(".")[basicPath.split(".").length - 2];
						basicPath = basicPath + "_" + dOut.getAlgorithm() + "_" + dOut.getDataset();
						if(decisionFunctionFlag && dFunction != null)
							basicPath = basicPath + "_" + dFunction.getClassifierTag();
						cp = buildChartPanel();
						printDataset(((XYPlot) cp.getChart().getPlot()).getDataset(), basicPath + ".csv");
						ChartUtilities.saveChartAsPNG(new File(basicPath + ".png"), cp.getChart(), (int)cp.getPreferredSize().getWidth(), (int)cp.getPreferredSize().getHeight());
					}
				} catch (Exception ex){
					AppLogger.logException(getClass(), ex, "Unable to save graph to file");
				}
			}
			
			private void printDataset(XYDataset dataset, String filename){
				BufferedWriter writer;
				List<Double> xValues = new LinkedList<Double>();
				Map<Integer, Map<Double, Double>> map = new HashMap<>();
				try {
					for(int i=0;i<dataset.getSeriesCount();i++){
						map.put(i, new HashMap<>());
						for(int j=0;j<dataset.getItemCount(i);j++){
							map.get(i).put(dataset.getXValue(i, j), dataset.getYValue(i, j));
							if(!xValues.contains(dataset.getXValue(i, j)))
								xValues.add(dataset.getXValue(i, j));
						}
					}
					Collections.sort(xValues);
					writer = new BufferedWriter(new FileWriter(new File(filename)));
					writer.write("interval,");
					for(int i=0;i<dataset.getSeriesCount();i++){
						writer.write(dataset.getSeriesKey(i) + ",");
					}
					writer.write("\n");
					for(Double xValue : xValues){
						writer.write(xValue + ",");
						double diff = 0;
						for(int i=0;i<dataset.getSeriesCount();i++){
							if(map.get(i).containsKey(xValue)){
								writer.write((map.get(i).get(xValue) - diff) + ",");
								diff = map.get(i).get(xValue);
							} else {
								writer.write("0,");
								diff = 0;
							}
						}
						writer.write("\n");
					}
					
					writer.close();
				} catch(IOException ex){
					AppLogger.logException(getClass(), ex, "Unable to write summary files");
				}
			}
			
		} );	
		secondHeaderPanel.add(butSave);
		
		headerPanel.add(firstHeaderPanel);
		headerPanel.add(secondHeaderPanel);
		
		mainPanel.add(headerPanel, BorderLayout.NORTH);
		
		// TODO
		
		if(dFunction != null && decisionFunctionFlag){	
			
			JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			panel.setLayout(new GridLayout(3, 1));
			panel.setBorder(new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Metric Scores", TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY));
			
			JLabel label = new JLabel("Metric scores for " + dFunction.getName());
			label.setFont(labelBoldFont);
			label.setBorder(new EmptyBorder(0, 10, 0, 10));
			label.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(label);
			
			label = new JLabel(dFunction.toCompactString());
			label.setFont(labelBoldFont);
			label.setBorder(new EmptyBorder(0, 10, 0, 10));
			label.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(label);
			
			label = new JLabel(calculateMetrics());
			label.setFont(labelFont);
			label.setBorder(new EmptyBorder(0, 10, 0, 10));
			label.setHorizontalAlignment(SwingConstants.CENTER);
			panel.add(label);
			
			mainPanel.add(panel, BorderLayout.SOUTH);
		}
		
		mainPanel.validate();
		
	}

	private String calculateMetrics() {	
		String outString = null;
		double tp = 0, fp = 0, tn = 0, fn = 0;
		
		double currentMax = maxValue;
		if(containsInfiniteValues())
			currentMax = maxValue + maxValue/(numIntervals-1.0);
		
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(containsPostiveLabel(list)){
				for(LabelledResult lr : list){
					double currentScore;
					if(Double.isFinite(lr.getValue().getScore()))
						currentScore = lr.getValue().getScore();
					else currentScore = maxValue + maxValue/(numIntervals-1.0);
					if(currentScore >= minValue && currentScore <= currentMax){
						AnomalyResult aRes = dFunction.assignScore(lr.getValue(), false);
						if(lr.getLabel()){
							if(aRes == AnomalyResult.ANOMALY)
								tp++;
							else fn++;
						} else {
							if(aRes == AnomalyResult.ANOMALY)
								fp++;
							else tn++;
						}
					}
				}
			}
		}
		
		DecimalFormat df = new DecimalFormat("#0.00"); 
		outString = "TP: " + tp + ", TN: " + tn + " FP: " + fp + " FN: " + fn;
		double fpr = fp/(fp+tn);
		outString = outString + " FPR: " + (fp+tn > 0 ? df.format(fpr) : "0.00");
		double p = tp/(fp+tp);
		outString = outString + " P: " + (fp+tp > 0 ? df.format(p) : "0.00");
		double r = tp/(fn+tp);
		outString = outString + " R: " + (fn+tp > 0 ? df.format(r) : "0.00");
		outString = outString + " F1: " + (p+r > 0 ? df.format(2*p*r/(p+r)) : "0.00");
		outString = outString + " ACC: " + df.format((tp+tn)/(fn+tn+fp+tp));
		outString = outString + " MCC: " + df.format((tp*tn - fp*fn)/Math.sqrt((tp + fp)*(tp + fn)*(tn + fp)*(tn + fn)));
		outString = outString + " AUC: " + df.format((r * fpr) / 2 + (r + 1) * (1 - fpr) / 2);

		return outString;
	}

	private ChartPanel buildChartPanel(){
		int countErr = 0;
		int countInf = 0;
		
		List<Double> okList = new LinkedList<>();
		List<Double> anList = new LinkedList<>();
		
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(containsPostiveLabel(list)){
				for(LabelledResult lr : list){
					if(lr.getValue().getScore() >= minValue && (maxValue == maxRefValue || lr.getValue().getScore() <= maxValue)){
						if(lr.getLabel()){
							anList.add(lr.getValue().getScore());
						} else {
							okList.add(lr.getValue().getScore());
						}
						if(!Double.isFinite(lr.getValue().getScore()))
							countInf++;
					} else countErr++;
				}
			} else countErr = countErr + list.size();
		}

		if(numIntervals <= 0 || numIntervals > 100000){
			numIntervals = NUM_INTERVALS;
		}
		
		boolean infiniteFlag = containsInfiniteValues();
		IntervalXYDataset dataset;
		if(dFunction != null && decisionFunctionFlag)
			dataset = (IntervalXYDataset)createConfusionMatrixSeries(minValue, maxValue, infiniteFlag);
		else dataset = (IntervalXYDataset)createSeries(minValue, maxValue, infiniteFlag);
		
		// Generate the graph
		JFreeChart chart = ChartFactory.createXYBarChart(
				"Scores of '" + dOut.getAlgorithm().replace("[", "").replace("]", "") + "' on '" + dOut.getDataset() + "' with " + okList.size() + " normal and " + anList.size() + " anomalies \n(" + countErr + " discarded, " + countInf + " infinite, " + Overlap_Metric.calculateOverlap(okList, anList) + "% overlap)", 
				"", false, dOut.getAlgorithm().replace("[", "").replace("]", "") + " score", dataset, 
				PlotOrientation.VERTICAL, true, true, false);
		   
		((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(0, Color.RED);
		((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(1, Color.BLUE);
		if(((XYPlot) chart.getPlot()).getSeriesCount() > 2){
			((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(2, Color.GREEN);
			((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(3, Color.YELLOW);
		}
		
		// Setting decision function thresholds
		if(decisionFunctionFlag && dFunction != null){
			double[] thresholds = dFunction.getThresholds();
			if(thresholds != null){
				for(int i=0;i<thresholds.length;i++){
					ValueMarker domainMarker = new ValueMarker(norm ? 1 + (thresholds[i] - minValue) /(maxValue - minValue) : thresholds[i]);
					domainMarker.setPaint(Color.black);
					domainMarker.setLabel("THR (" + (i+1) + ")"); 
					domainMarker.setLabelFont(domainMarker.getLabelFont().deriveFont(domainMarker.getLabelFont().getStyle(), 16));
					domainMarker.setStroke(new BasicStroke(2.0f));        
			        domainMarker.setLabelOffset(new RectangleInsets(10,10,10,100));
			        domainMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			        domainMarker.setLabelTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
					((XYPlot)chart.getPlot()).addDomainMarker(domainMarker);
				}
			}
			
		}
		
		
		// Setting x axis range
		NumberAxis domain = (NumberAxis) ((XYPlot) chart.getPlot()).getDomainAxis();
	    if(!norm)
	    	domain.setRange(minValue > 0 ? minValue*0.99 : minValue*1.01, infiniteFlag ? maxValue + maxValue/(numIntervals > 1 ? numIntervals-1.0 : 0.9) : maxValue);
		
		// Set bar size
		double scaling = 1.0 - domain.getRange().getLength() / numIntervals;
		XYPlot categoryPlot = (XYPlot) chart.getPlot();
		XYBarRenderer br = (XYBarRenderer) categoryPlot.getRenderer();
		if(scaling > 0 && scaling < 1)
			br.setMargin(scaling);
		
		// Set log Y axis
		LogAxis yAxis = new LogAxis("Y");
		yAxis.setBase(2);
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		if(logScale){	
			((XYPlot) chart.getPlot()).setRangeAxis(yAxis);
		}
		   
		return new ChartPanel(chart);
	}
	
	private boolean containsInfiniteValues(){
		boolean infiniteFlag = false;
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(containsPostiveLabel(list)){
				for(LabelledResult lr : list){
					if(Double.isInfinite(lr.getValue().getScore()) || lr.getValue().getScore() > Double.MAX_VALUE - 10){
						infiniteFlag = true;
					}
				}
			}
		}
		return infiniteFlag;
	}
	
	private XYSeriesCollection createSeries(double minValue, double maxValue, boolean infiniteFlag){
		double[] normalCount = new double[numIntervals];
		double[] anomalyCount = new double[numIntervals];
		
		double currentMax = maxValue;
		if(infiniteFlag)
			currentMax = maxValue + maxValue/(numIntervals-1.0);
		
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(containsPostiveLabel(list)){
				for(LabelledResult lr : list){
					double currentScore;
					if(Double.isFinite(lr.getValue().getScore()))
						currentScore = lr.getValue().getScore();
					else currentScore = maxValue + maxValue/(numIntervals-1.0);
					if(currentScore >= minValue && currentScore <= currentMax){
						double normalizedScore = (lr.getValue().getScore() - minValue) / (currentMax - minValue);
						int dataIndex = (int) (normalizedScore*numIntervals);
						if(dataIndex >= numIntervals)
							dataIndex = numIntervals - 1;
						if(lr.getLabel()){
							anomalyCount[dataIndex]++;
						} else {
							normalCount[dataIndex]++;
						}
					}
				}
			}
		}
		
		XYSeries trueSeries = new XYSeries(norm ? "Anomaly Series (Normalized)" : "Anomaly Series");
		XYSeries falseSeries = new XYSeries(norm ? "Normal Series (Normalized)" : "Normal Series");
		
		double intervalSize = (maxValue - minValue) / numIntervals;
		
		for(int i=0;i<numIntervals;i++){
			if(normalCount[i] > 0)
				falseSeries.add(norm ? (i+0.5) : minValue + i*intervalSize, normalCount[i] + anomalyCount[i]);
			if(anomalyCount[i] > 0)
				trueSeries.add(norm ? (i+0.5) : minValue + i*intervalSize, anomalyCount[i]);
		}
		
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		dataset.addSeries(trueSeries);
		dataset.addSeries(falseSeries);
		
		return dataset;
	}
	
	private XYSeriesCollection createConfusionMatrixSeries(double minValue, double maxValue, boolean infiniteFlag){
		double[] tpCount = new double[numIntervals];
		double[] fpCount = new double[numIntervals];
		double[] tnCount = new double[numIntervals];
		double[] fnCount = new double[numIntervals];
		
		double currentMax = maxValue;
		if(infiniteFlag)
			currentMax = maxValue + maxValue/(numIntervals-1.0);
		
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(containsPostiveLabel(list)){
				for(LabelledResult lr : list){
					double currentScore;
					if(Double.isFinite(lr.getValue().getScore()))
						currentScore = lr.getValue().getScore();
					else currentScore = maxValue + maxValue/(numIntervals-1.0);
					if(currentScore >= minValue && currentScore <= currentMax){
						double normalizedScore = (currentScore - minValue) / (currentMax - minValue);
						AnomalyResult aRes = dFunction.assignScore(lr.getValue(), false);
						int dataIndex = (int) (normalizedScore*numIntervals);
						if(dataIndex == numIntervals)
							dataIndex--;
						if(lr.getLabel()){
							if(aRes == AnomalyResult.ANOMALY)
								tpCount[dataIndex]++;
							else fnCount[dataIndex]++;
						} else {
							if(aRes == AnomalyResult.ANOMALY)
								fpCount[dataIndex]++;
							else tnCount[dataIndex]++;
						}
					}
				}
			}
		}
		
		XYSeries tpSeries = new XYSeries(norm ? "TP Series (Normalized)" : "TP Series");
		XYSeries fpSeries = new XYSeries(norm ? "FP Series (Normalized)" : "FP Series");
		XYSeries tnSeries = new XYSeries(norm ? "TN Series (Normalized)" : "TN Series");
		XYSeries fnSeries = new XYSeries(norm ? "FN Series (Normalized)" : "FN Series");
		
		double intervalSize = (currentMax - minValue) / numIntervals;
		
		for(int i=0;i<numIntervals;i++){
			if(tpCount[i] > 0)
				tpSeries.add(norm ? (i + 0.5) : minValue + i*intervalSize, tpCount[i]);
			if(tpCount[i] + fnCount[i] > 0)
				fnSeries.add(norm ? (i + 0.5) : minValue + i*intervalSize, tpCount[i] + fnCount[i]);
			if(tpCount[i] + fnCount[i] + fpCount[i] > 0)
				fpSeries.add(norm ? (i + 0.5) : minValue + i*intervalSize, tpCount[i] + fnCount[i] + fpCount[i]);
			if(tpCount[i] + fnCount[i] + fpCount[i] + tnCount[i] > 0)
				tnSeries.add(norm ? (i + 0.5) : minValue + i*intervalSize, tpCount[i] + fnCount[i] + fpCount[i] + tnCount[i]);
		}
		
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		dataset.addSeries(tpSeries);
		dataset.addSeries(fnSeries);
		dataset.addSeries(fpSeries);		
		dataset.addSeries(tnSeries);
		
		return dataset;
	}
	
}