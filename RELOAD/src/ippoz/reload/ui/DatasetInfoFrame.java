/**
 * 
 */
package ippoz.reload.ui;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.indicator.Indicator;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.loader.info.DatasetInfo;
import ippoz.reload.commons.loader.info.FeatureInfo;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.featureselection.FeatureSelector;
import ippoz.reload.featureselection.FeatureSelectorType;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author Tommy
 *
 */
public class DatasetInfoFrame {
	
	private static int NUM_INTERVALS = 100;

	private JFrame fFrame;

	private Font labelFont;

	private JPanel featurePanel;

	private Loader loader;
	
	private DatasetInfo dInfo;
	
	private List<String> features;
	
	private int selectedFeature;
	
	private boolean isUpdating;
	
	private Map<String, Map<FeatureSelectorType, Double>> fsScores;
	
	private static DecimalFormat df3 = new DecimalFormat("0.###");

	public DatasetInfoFrame(Loader loader) {
		this.loader = loader;
		
		dInfo = loader.generateDatasetInfo();
		features = dInfo.getFeatures();
		
		selectedFeature = 0;
		isUpdating = false;
		fsScores = new HashMap<>();
		
		buildFrame();

		double rate = 18 * Toolkit.getDefaultToolkit().getScreenSize()
				.getHeight() / 1080;

		labelFont = new Font("Times", Font.PLAIN, (int) ((16 + rate) / 2));

		featurePanel = buildMainPanel();
	}

	public void setVisible(boolean b) {
		if (fFrame != null) {
			fFrame.add(featurePanel);
			fFrame.setLocationRelativeTo(null);
			fFrame.setVisible(b);
		}
	}
	
	private void reload() {
		isUpdating = true;
		fFrame.setVisible(false);
		fFrame.getContentPane().removeAll();
		buildFrame();
		featurePanel = buildMainPanel();
		setVisible(true);
		isUpdating = false;
	}

	private void buildFrame() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		fFrame = new JFrame();
		fFrame.setTitle("Dataset Info for '" + loader.getLoaderName() + "'");
		if (screenSize.getWidth() > 1000)
			fFrame.setBounds(0, 0, (int) (screenSize.getWidth() * 0.8),
					(int) (screenSize.getHeight() * 0.7));
		else
			fFrame.setBounds(0, 0, 600, 500);
		fFrame.setBackground(Color.WHITE);
		fFrame.setResizable(true);
	}

	private JPanel buildMainPanel() {
		
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		containerPanel.setLayout(new GridLayout(2, 1));
		containerPanel.setAlignmentX(SwingConstants.CENTER);

		JPanel bodyPanel = new JPanel();
		bodyPanel.setBackground(Color.WHITE);
		bodyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		bodyPanel.setLayout(new GridLayout(1, 2));
		
		// TABLE

		JTable table = new JTable(new MyTableModel());
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		for (int x = 0; x < table.getColumnCount(); x++) {
			table.getColumnModel().getColumn(x).setWidth(200);
			table.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
			table.getColumnModel().getColumn(x).setHeaderRenderer(centerRenderer);
		}
		
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	        	if(!isUpdating && !event.getValueIsAdjusting() && table.getSelectedRow() != -1){
	        		selectedFeature = table.getSelectedRow();
	        		reload();
	        	}
	        }
	    });

		JScrollPane scroll = new JScrollPane(table);
		bodyPanel.add(scroll);
		bodyPanel.add(buildChartPanel(features.get(selectedFeature)));
		
		containerPanel.add(bodyPanel);
		
		JPanel otherPanel = new JPanel();
		otherPanel.setBackground(Color.WHITE);
		otherPanel.setLayout(new GridLayout(2, 1));
		
		JPanel innerPanel = new JPanel();
		innerPanel.setBackground(Color.WHITE);
		innerPanel.setLayout(new GridLayout(2, 1));
		
		JPanel hPanel = new JPanel();
		hPanel.setBackground(Color.WHITE);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2),
				" Dataset Detail ", TitledBorder.CENTER, TitledBorder.CENTER,
				new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		hPanel.setBorder(new CompoundBorder(new EmptyBorder(0, 80, 0, 80), tb));
		hPanel.setLayout(new GridLayout(1, 6));

		JLabel lbl = new JLabel("Features: " + String.valueOf(features.size()));
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		hPanel.add(lbl);
		
		lbl = new JLabel("Data Points: " + dInfo.getDataPoints());
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		hPanel.add(lbl);
		
		lbl = new JLabel("Size (MB): " + String.valueOf(loader.getMBSize()));
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		hPanel.add(lbl);
		
		lbl = new JLabel("Anomaly Rate: " + df3.format(dInfo.getAnomalyRatio()) + " %");
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		hPanel.add(lbl);
		
		lbl = new JLabel("Skip Rate: " + df3.format(dInfo.getSkipRatio()) + " %");
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		hPanel.add(lbl);
		
		lbl = new JLabel("Anomaly Tags: TBA");
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		hPanel.add(lbl);
		
		innerPanel.add(hPanel);
		
		JPanel footerPanel = new JPanel();
		footerPanel.setBackground(Color.WHITE);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2),
				" Feature Ranking ", TitledBorder.CENTER, TitledBorder.CENTER,
				new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		footerPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), tb));
		footerPanel.setLayout(new GridLayout(2, 1));
		
		JPanel cbPanel = new JPanel();
		cbPanel.setBackground(Color.WHITE);
		cbPanel.setLayout(new GridLayout(1, FeatureSelectorType.values().length));
		
		Map<FeatureSelectorType, JCheckBox> cbMap = new HashMap<>();
		for(FeatureSelectorType fst : FeatureSelectorType.values()){
			JCheckBox cb = new JCheckBox(fst.toString());
			cb.setSelected(false);
			cb.setAlignmentX(SwingConstants.CENTER);
			cbMap.put(fst, cb);
			cbPanel.add(cb);
		}
		
		footerPanel.add(cbPanel);
		
		JButton button = new JButton("Calculate Feature Rank");
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				List<Knowledge> kList = Knowledge.generateKnowledge(loader.fetch());
				List<DataSeries> dsList = generateDsList();
				for(FeatureSelectorType fst : cbMap.keySet()){
					if(cbMap.get(fst).isSelected()){
						FeatureSelector fs = FeatureSelector.createSelector(fst, 100, true);
						fs.applyFeatureSelection(dsList, kList);
						for(DataSeries ds : dsList){
							String fName = ds.getName();
							if(!fsScores.containsKey(fName))
								fsScores.put(fName, new HashMap<>());
							fsScores.get(fName).put(fst, fs.getScoreFor(ds));
						}
						AppLogger.logInfo(getClass(), "Feature Rank calculated for " + fst.toString());
					}
				}
				
				reload();
			}

			private List<DataSeries> generateDsList() {
				List<DataSeries> list = new LinkedList<>();
				for(String fName : features){
					list.add(new DataSeries(new Indicator(fName, Double.class)));
				}
				return list;
			}
		} );
		
		footerPanel.add(button);
		
		innerPanel.add(footerPanel);
		
		otherPanel.add(innerPanel);
		
		JTable fTable = new JTable(new FeatureRanksTableModel());
		fTable.setFillsViewportHeight(true);
		fTable.setAutoCreateRowSorter(true);

		for (int x = 0; x < fTable.getColumnCount(); x++) {
			fTable.getColumnModel().getColumn(x).setWidth(200);
			fTable.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
			fTable.getColumnModel().getColumn(x).setHeaderRenderer(centerRenderer);
		}

		JScrollPane scroll2 = new JScrollPane(fTable);				
		otherPanel.add(scroll2);
		
		containerPanel.add(otherPanel);
		
		//containerPanel.add(scroll2);

		return containerPanel;
	}
	
	private JPanel buildChartPanel(String featureName){
		FeatureInfo fInfo = dInfo.getInfoFor(featureName);
		if(fInfo != null){
			Map<Double, Integer> allMap = fInfo.getAllMap();
			int countInf = fInfo.countInfinite();
			int numIntervals = allMap.size();		
			if(numIntervals <= 0 || numIntervals > 1000){
				numIntervals = NUM_INTERVALS;
			}
			
			// Generate the graph
			IntervalXYDataset dataset = (IntervalXYDataset)createSeries(fInfo, numIntervals, allMap);
			JFreeChart chart = ChartFactory.createXYBarChart(
					"Distribution of Feature '" + featureName + "' (" + countInf + " infinite)", 
					"", false, featureName, dataset, PlotOrientation.VERTICAL, true, true, false);
			((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(0, Color.RED);
			((XYPlot) chart.getPlot()).getRenderer().setSeriesPaint(1, Color.BLUE);
			
			// Set bar size
			NumberAxis domain = (NumberAxis) ((XYPlot) chart.getPlot()).getDomainAxis();
			double scaling = 1.0 - domain.getRange().getLength() / numIntervals;
			XYPlot categoryPlot = (XYPlot) chart.getPlot();
			XYBarRenderer br = (XYBarRenderer) categoryPlot.getRenderer();
			if(scaling > 0 && scaling < 1)
				br.setMargin(scaling);
			
			return new ChartPanel(chart);
		}
		
		return new JPanel();
		   
		
	}

	private XYSeriesCollection createSeries(FeatureInfo fInfo, int numIntervals, Map<Double, Integer> allMap){
		double[] normalCount = new double[numIntervals];
		double[] anomalyCount = new double[numIntervals];
		
		double max = fInfo.getMaxFinite();
		double min = fInfo.getMinFinite();
		
		// Normal
		Map<Double, Integer> map = fInfo.getNormalMap();
		for(Double currentScore : map.keySet()){
			if(Double.isFinite(currentScore)){
				double normalizedScore = (currentScore - min) / (max - min);
				int dataIndex = (int) (normalizedScore*numIntervals);
				if(dataIndex >= numIntervals)
					dataIndex = numIntervals - 1;
				normalCount[dataIndex] = normalCount[dataIndex] + map.get(currentScore);
			}
		}
		// Anomaly
		map = fInfo.getAnomalyMap();
		for(Double currentScore : map.keySet()){
			if(Double.isFinite(currentScore)){
				double normalizedScore = (currentScore - min) / (max - min);
				int dataIndex = (int) (normalizedScore*numIntervals);
				if(dataIndex >= numIntervals)
					dataIndex = numIntervals - 1;
				anomalyCount[dataIndex] = anomalyCount[dataIndex] + map.get(currentScore);
			}
		}
		
		// Create Normal/Anomaly Series
		XYSeries trueSeries = new XYSeries("Anomaly Series");
		XYSeries falseSeries = new XYSeries("Normal Series");
		double intervalSize = (max - min) / numIntervals;	
		for(int i=0;i<numIntervals;i++){
			if(normalCount[i] > 0)
				falseSeries.add(min + intervalSize/2 + i*intervalSize, normalCount[i] + anomalyCount[i]);
			if(anomalyCount[i] > 0)
				trueSeries.add(min + intervalSize/2 + i*intervalSize, anomalyCount[i]);
		}
		
		// Add the series to your data set
		XYSeriesCollection dataset = new XYSeriesCollection();
		dataset.addSeries(trueSeries);
		dataset.addSeries(falseSeries);
		
		return dataset;
	}

	public JPanel createLPanel(boolean bold, String textName, JPanel root,
			int panelX, int panelY, String textFieldText) {
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBounds(panelX, panelY, (int) (root.getWidth() * 0.96), 30);
		panel.setLayout(null);

		JLabel lbl = new JLabel(textName);
		lbl.setFont(labelFont);
		if (bold)
			lbl.setFont(lbl.getFont().deriveFont(
					lbl.getFont().getStyle() | Font.BOLD));
		lbl.setBounds(panel.getWidth() / 10, 0, panel.getWidth() * 3 / 10, 30);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);

		JLabel lbldata = new JLabel(textFieldText);
		lbldata.setFont(labelFont);
		lbldata.setBounds(panel.getWidth() / 5 * 2, 0, panel.getWidth() / 2, 30);
		lbldata.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbldata);

		return panel;
	}

	private class MyTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		public int getColumnCount() {
			return 7;
		}

		public int getRowCount() {
			return features.size();
		}

		public String getColumnName(int col) {
			switch(col){
				case 0:
					return "Feature";
				case 1:
					return "Avg All";
				case 2:
					return "Std All";
				case 3:
					return "Avg Normal";
				case 4:
					return "Std Normal";
				case 5:
					return "Avg Anomaly";
				case 6:
					return "Std Anomaly";
			}
			return "";
		}

		public Object getValueAt(int row, int col) {
			FeatureInfo fInfo = dInfo.getInfoFor(features.get(row));
			switch(col){
				case 0:
					return features.get(row);
				case 1:
					return df3.format(fInfo.getAllAverage());
				case 2:
					return df3.format(fInfo.getAllStd());
				case 3:
					return df3.format(fInfo.getNormalAverage());
				case 4:
					return df3.format(fInfo.getNormalStd());
				case 5:
					return df3.format(fInfo.getAnomalyAverage());
				case 6:
					return df3.format(fInfo.getAnomalyStd());
			}
			return null;
		}

		public Class<?> getColumnClass(int c) {
			if(c == 0)
				return String.class;
			else return Double.class;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object aValue, int row, int col) {

		}

	}
	
	private class FeatureRanksTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		FeatureSelectorType[] fsList = FeatureSelectorType.values();
		
		public int getColumnCount() {
			return fsList.length + 1;
		}

		public int getRowCount() {
			return features.size();
		}

		public String getColumnName(int col) {
			switch(col){
				case 0:
					return "Feature";
				default:
					if(col <= fsList.length){
						return fsList[col-1].toString();
					} else return "";
			}
		}

		public Object getValueAt(int row, int col) {
			String featureName = features.get(row);
			switch(col){
				case 0:
					return featureName;
				default:
					if(fsScores.size() > 0 && fsScores.containsKey(featureName) && col <= fsList.length){
						Double num = fsScores.get(featureName).get(fsList[col-1]);
						if(num != null)
							return df3.format(fsScores.get(featureName).get(fsList[col-1]));
						else return "-";
					} else return "n.a.";
			}
		}

		public Class<?> getColumnClass(int c) {
			if(c == 0)
				return String.class;
			else return Double.class;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object aValue, int row, int col) {

		}

	}

}
