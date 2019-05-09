/**
 * 
 */
package ippoz.reload.executable.ui;

import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.commons.support.ValueSeries;
import ippoz.reload.decisionfunction.AnomalyResult;
import ippoz.reload.decisionfunction.DecisionFunction;
import ippoz.reload.decisionfunction.DecisionFunctionType;
import ippoz.reload.output.DetectorOutput;
import ippoz.reload.output.LabelledResult;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

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
	
	private boolean logScale;
	
	private boolean decisionFunctionFlag;
	
	private int numIntervals;
	
	private DecisionFunction dFunction;
	
	public OutputDetailFrame(DetectorOutput dOut) {
		this.dOut = dOut;
		
		logScale = false;
		decisionFunctionFlag = false;
		numIntervals = NUM_INTERVALS;
		algorithmScores = createScoresValueSeries();
		dFunction = createDefaultDecisionFunction();
		
		buildFrame();
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		labelFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelBoldFont = new Font("Times", Font.BOLD, (int)((15 + rate)/2));
	}
	
	private DecisionFunction createDefaultDecisionFunction() {
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(list != null && list.size() > 0){
				if(list.get(0) != null){
					return DecisionFunction.getClassifier(algorithmScores, list.get(0).getValue().getDecisionFunction().getClassifierTag());
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
			detFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.6), (int)(screenSize.getHeight()*0.6));
		else detFrame.setBounds(0, 0, 600, 360);
		detFrame.setBackground(Color.WHITE);
		detFrame.setResizable(false);
	}
	
	public void buildMainPanel(){
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBounds(0, 0, detFrame.getWidth() - 10, detFrame.getHeight() - 10);
		mainPanel.setLayout(new BorderLayout());
		
		mainPanel.add(buildChartPanel(), BorderLayout.CENTER);
		
		JPanel footerPanel = new JPanel();
		footerPanel.setBackground(Color.WHITE);
		footerPanel.setBorder(new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Parameters", TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY));
		footerPanel.setLayout(new GridLayout(1, 6));
		
		JLabel lbl = new JLabel("Number of Rectangles");
		lbl.setFont(labelFont);
		lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		footerPanel.add(lbl);
		
		intTextField = new JTextField(String.valueOf(numIntervals));
		intTextField.setFont(labelFont);
		footerPanel.add(intTextField);
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
		logCB.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent event) {
		    	logScale = !logScale;
		        reload();
		    }
		});
		footerPanel.add(logCB);
		
		String suggDecision = null;
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(list != null && list.size() > 0){
				if(list.get(0) != null){
					suggDecision = list.get(0).getValue().getDecisionFunction().getClassifierName();
					break;
				}
			}
		}
		
		lbl = new JLabel("Decision Function");
		lbl.setFont(labelFont);
		lbl.setBorder(new EmptyBorder(0, 10, 0, 10));
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		footerPanel.add(lbl);
		
		cbDecisionFunction = new JComboBox<DecisionFunctionType>(DecisionFunctionType.values());
		if(dFunction != null)
			cbDecisionFunction.setSelectedItem(dFunction.getClassifierType());
		else cbDecisionFunction.setSelectedItem(suggDecision);
		cbDecisionFunction.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		        if(!isUpdating){
		        	String newValue = cbDecisionFunction.getSelectedItem().toString();
		        	
		        	String descriptionString = "Insert parameter for " + newValue;
		        	if(newValue.contains("IQR") || newValue.contains("CONF")){
		        		descriptionString = "Insert range parameter (range >= 0) for " + newValue;
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
		        	
		        	dFunction = DecisionFunction.getClassifier(algorithmScores, newValue);
		        	if(dFunction != null){
		        		cbDecisionFunction.setSelectedItem(dFunction.getClassifierType());
		        		reload();
		        	} else {
		        		
		        	}
		    	}
		    }
		});
		footerPanel.add(cbDecisionFunction);
		
		JCheckBox decisionFunctionCB = new JCheckBox("Apply Decision");
		decisionFunctionCB.setSelected(decisionFunctionFlag);
		decisionFunctionCB.setBorder(new EmptyBorder(0, 10, 0, 10));
		decisionFunctionCB.setFont(labelFont);
		decisionFunctionCB.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent event) {
		    	decisionFunctionFlag = !decisionFunctionFlag;
		        reload();
		    }
		});
		
		footerPanel.add(decisionFunctionCB);
		
		//footerPanel.add(panel);
		
		mainPanel.add(footerPanel, BorderLayout.NORTH);
		
		// TODO
		
		if(dFunction != null && decisionFunctionFlag){	
			
			JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			panel.setLayout(new GridLayout(3, 1));
			panel.setBorder(new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Metric Scores", TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 20), Color.DARK_GRAY));
			
			JLabel label = new JLabel("Metric scores for " + dFunction.getClassifierName());
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
		
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			for(LabelledResult lr : list){
				if(Double.isFinite(lr.getValue().getScore())){
					AnomalyResult aRes = dFunction.classifyScore(lr.getValue(), false);
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
		
		DecimalFormat df = new DecimalFormat("#0.00"); 
		outString = "TP: " + tp + ", TN: " + tn + " FP: " + fp + " FN: " + fn;
		double fpr = fp/(fp+tn);
		outString = outString + " FPR: " + df.format(fpr);
		double p = tp/(fp+tp);
		outString = outString + " P: " + df.format(p);
		double r = tp/(fn+tp);
		outString = outString + " R: " + df.format(r);
		outString = outString + " F1: " + df.format(2*p*r/(p+r));
		outString = outString + " ACC: " + df.format((tp+tn)/(fn+tn+fp+tp));
		outString = outString + " MCC: " + df.format((tp*tn - fp*fn)/Math.sqrt((tp + fp)*(tp + fn)*(tn + fp)*(tn + fn)));
		outString = outString + " AUC: " + df.format((r * fpr) / 2 + (r + 1) * (1 - fpr) / 2);

		return outString;
	}

	private ChartPanel buildChartPanel(){
		double minValue = Double.POSITIVE_INFINITY;
		double maxValue = Double.NEGATIVE_INFINITY;
		int countN = 0;
		int countA = 0;
		int countErr = 0;
		
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(containsPostiveLabel(list)){
				for(LabelledResult lr : list){
					if(Double.isFinite(lr.getValue().getScore())){
						if(lr.getValue().getScore() > maxValue)
							maxValue = lr.getValue().getScore();
						if(lr.getValue().getScore() < minValue)
							minValue = lr.getValue().getScore();
						if(lr.getLabel())
							countA++;
						else countN++;
					}
				}
			} else countErr = countErr + list.size();
		}

		if(numIntervals <= 0){
			numIntervals = NUM_INTERVALS;
		}
		
		IntervalXYDataset dataset;
		if(dFunction != null && decisionFunctionFlag)
			dataset = (IntervalXYDataset)createConfusionMatrixSeries(minValue, maxValue);
		else dataset = (IntervalXYDataset)createSeries(minValue, maxValue);

		// Generate the graph
		JFreeChart chart = ChartFactory.createXYBarChart(
				"Scores of '" + dOut.getAlgorithm().replace("[", "").replace("]", "") + "' on '" + dOut.getDataset() + "' with " + countN + " normal and " + countA + " anomalies (" + countErr + " discarded)", 
				"", false, dOut.getAlgorithm().replace("[", "").replace("]", "") + " score", dataset, 
				PlotOrientation.VERTICAL, true, true, false);
		   
		((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(0, Color.RED);
		((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(1, Color.BLUE);
		if(((XYPlot) chart.getPlot()).getSeriesCount() > 2){
			((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(2, Color.GREEN);
			((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(3, Color.YELLOW);
		}
		
		LogAxis yAxis = new LogAxis("Y");
		yAxis.setBase(2);
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		if(logScale){	
			((XYPlot) chart.getPlot()).setRangeAxis(yAxis);
		}
		   
		return new ChartPanel(chart);
	}
	
	private XYSeriesCollection createSeries(double minValue, double maxValue){
		double[] normalCount = new double[numIntervals];
		double[] anomalyCount = new double[numIntervals];
		
		
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(containsPostiveLabel(list)){
				for(LabelledResult lr : list){
					if(Double.isFinite(lr.getValue().getScore())){
						double normalizedScore = (lr.getValue().getScore() - minValue) / (maxValue - minValue);
						int dataIndex = (int) (normalizedScore*numIntervals);
						if(dataIndex == numIntervals)
							dataIndex--;
						if(lr.getLabel()){
							anomalyCount[dataIndex]++;
						} else {
							normalCount[dataIndex]++;
						}
					}
				}
			}
		}
		
		XYSeries trueSeries = new XYSeries("Anomaly Series");
		XYSeries falseSeries = new XYSeries("Normal Series");
		
		for(int i=0;i<numIntervals;i++){
			if(normalCount[i] > 0)
				falseSeries.add(i, normalCount[i] + anomalyCount[i]);
			if(anomalyCount[i] > 0)
				trueSeries.add(i, anomalyCount[i]);
		}
		
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		dataset.addSeries(trueSeries);
		dataset.addSeries(falseSeries);
		
		return dataset;
	}
	
	private XYSeriesCollection createConfusionMatrixSeries(double minValue, double maxValue){
		double[] tpCount = new double[numIntervals];
		double[] fpCount = new double[numIntervals];
		double[] tnCount = new double[numIntervals];
		double[] fnCount = new double[numIntervals];
		
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			if(containsPostiveLabel(list)){
				for(LabelledResult lr : list){
					if(Double.isFinite(lr.getValue().getScore())){
						double normalizedScore = (lr.getValue().getScore() - minValue) / (maxValue - minValue);
						AnomalyResult aRes = dFunction.classifyScore(lr.getValue(), false);
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
		
		XYSeries tpSeries = new XYSeries("TP Series");
		XYSeries fpSeries = new XYSeries("FP Series");
		XYSeries tnSeries = new XYSeries("TN Series");
		XYSeries fnSeries = new XYSeries("FN Series");
		
		for(int i=0;i<numIntervals;i++){
			if(tpCount[i] > 0)
				tpSeries.add(i, tpCount[i]);
			if(tpCount[i] + fnCount[i] > 0)
				fnSeries.add(i, tpCount[i] + fnCount[i]);
			if(tpCount[i] + fnCount[i] + fpCount[i] > 0)
				fpSeries.add(i, tpCount[i] + fnCount[i] + fpCount[i]);
			if(tpCount[i] + fnCount[i] + fpCount[i] + tnCount[i] > 0)
				tnSeries.add(i, tpCount[i] + fnCount[i] + fpCount[i] + tnCount[i]);
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