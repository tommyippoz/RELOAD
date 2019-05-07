/**
 * 
 */
package ippoz.reload.executable.ui;

import ippoz.reload.commons.support.LabelledValue;
import ippoz.reload.output.DetectorOutput;
import ippoz.reload.output.LabelledResult;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
	
	private static int NUM_INTERVALS = 200;
	
	private JFrame detFrame;
	
	private DetectorOutput dOut;
	
	private JPanel mainPanel;
	
	private int labelSpacing;
	
	private int bigLabelSpacing;
	
	private Font smallLabelFont;
	
	private Font labelFont;
	
	private Font labelBoldFont;
	
	private boolean isUpdating;
	
	private JCheckBox logCB;
	
	private boolean logScale;
	
	public static void main(String[] args) {
		OutputDetailFrame of = new OutputDetailFrame(null);
		of.buildMainPanel2();
		of.setVisible(true);
	}
	
	public OutputDetailFrame(DetectorOutput dOut) {
		this.dOut = dOut;
		logScale = false;
		
		buildFrame();
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		
		smallLabelFont = new Font("Times", Font.PLAIN, (int)((13 + rate)/2));
		labelFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelBoldFont = new Font("Times", Font.BOLD, (int)((15 + rate)/2));
		
		labelSpacing = (int)(detFrame.getHeight()/25);
		bigLabelSpacing = (int)(detFrame.getHeight()/18);
		
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

		mainPanel.add(buildChartPanel() , BorderLayout.CENTER);
		
		JPanel footerPanel = new JPanel();
		footerPanel.setBackground(Color.WHITE);
		
		logCB = new JCheckBox("Logaritmic y-Axis");
		logCB.setSelected(logScale);
		logCB.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent event) {
		    	logScale = !logScale;
		        reload();
		    }
		});
		footerPanel.add(logCB);
		
		mainPanel.add(footerPanel, BorderLayout.SOUTH);
		
		mainPanel.validate();
		
	}
		
	private ChartPanel buildChartPanel(){
		double minValue = Double.POSITIVE_INFINITY;
		double maxValue = Double.NEGATIVE_INFINITY;
		
		double avg = 0;
		int count = 0;
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			for(LabelledResult lr : list){
				if(Double.isFinite(lr.getValue().getScore())){
					if(lr.getValue().getScore() > maxValue)
						maxValue = lr.getValue().getScore();
					if(lr.getValue().getScore() < minValue)
						minValue = lr.getValue().getScore();
					avg = avg + lr.getValue().getScore();
					count++;
				}
			}
		}
		System.out.println(avg/count);
		
		double[] normalCount = new double[NUM_INTERVALS];
		double[] anomalyCount = new double[NUM_INTERVALS];
		
		for(String expName : dOut.getLabelledScores().keySet()){
			List<LabelledResult> list = dOut.getLabelledScores().get(expName);
			for(LabelledResult lr : list){
				if(Double.isFinite(lr.getValue().getScore())){
					double normalizedScore = (lr.getValue().getScore() - minValue) / (maxValue - minValue);
					int dataIndex = (int) (normalizedScore*NUM_INTERVALS);
					if(dataIndex == NUM_INTERVALS)
						dataIndex--;
					if(lr.getLabel()){
						anomalyCount[dataIndex]++;
					} else {
						normalCount[dataIndex]++;
					}
				}
			}
		}
		
		XYSeries trueSeries = new XYSeries("Anomaly Series");
		XYSeries falseSeries = new XYSeries("Normal Series");
		
		for(int i=0;i<NUM_INTERVALS;i++){
			if(normalCount[i] > 0)
				falseSeries.add(i, normalCount[i] + anomalyCount[i]);
			if(anomalyCount[i] > 0)
				trueSeries.add(i, anomalyCount[i]);
		}
		
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		
		dataset.addSeries(trueSeries);
		dataset.addSeries(falseSeries);

		// Generate the graph
		JFreeChart chart = ChartFactory.createXYBarChart("title", "xlabel", false, "ylabel", (IntervalXYDataset) dataset, PlotOrientation.VERTICAL, true, true, false);
		   
		((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(0, Color.RED);
		((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(1, Color.BLUE);
		
		LogAxis yAxis = new LogAxis("Y");
		yAxis.setBase(2);
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		if(logCB != null && logCB.isSelected()){	
			((XYPlot) chart.getPlot()).setRangeAxis(yAxis);
		}
		   
		return new ChartPanel(chart);
	}
	
	public void buildMainPanel2(){
		mainPanel = new JPanel();
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setBounds(0, 0, detFrame.getWidth() - 10, detFrame.getHeight() - 10);
		
		mainPanel.setLayout(new BorderLayout());
	
	XYSeries series = new XYSeries("XYGraph");
	   series.add(1, 1);
	   series.add(1, 2);
	   series.add(2, -1);
	   series.add(3, 9);
	   series.add(4, 5);
	XYSeries series2 = new XYSeries("XYGraph2");
	   
	   series2.add(4, 10);

	// Add the series to your data set
	   XYSeriesCollection dataset = new XYSeriesCollection();
	   dataset.addSeries(series);
	   dataset.addSeries(series2);

	// Generate the graph
	   JFreeChart chart = ChartFactory.createXYBarChart("title", "xlabel", false, "ylabel", (IntervalXYDataset) dataset, PlotOrientation.VERTICAL, true, true, false);
	   
	   ((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(0, Color.red);
	   ((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(1, Color.green);
	   
		ChartPanel CP = new ChartPanel(chart);
	
	
		mainPanel.add(CP,BorderLayout.CENTER);
		mainPanel.validate();
	}
	
	public JPanel createLPanel(String textName, JPanel root, int panelY, String textFieldText){
		return createLPanel(false, textName, root, (int) (root.getWidth()*0.02), panelY, textFieldText);
	}
	
	public JPanel createLPanel(boolean bold, String textName, JPanel root, int panelX, int panelY, String textFieldText){
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBounds(panelX, panelY, (int) (root.getWidth()*0.96), labelSpacing);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel(textName);
		lbl.setFont(labelFont);
		if(bold)
			lbl.setFont(lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));
		lbl.setBounds(panel.getWidth()/10, 0, panel.getWidth()*3/10, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		JLabel lbldata = new JLabel(textFieldText);
		lbldata.setFont(labelFont);
		lbldata.setBounds(panel.getWidth()/5*2, 0, panel.getWidth()/2, labelSpacing);
		lbldata.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbldata);
		
		return panel;
	}
	
}