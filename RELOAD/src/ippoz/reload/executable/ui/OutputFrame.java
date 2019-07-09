/**
 * 
 */
package ippoz.reload.executable.ui;

import ippoz.reload.manager.InputManager;
import ippoz.reload.metric.Metric;
import ippoz.reload.output.DetectorOutput;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * @author Tommy
 *
 */
public class OutputFrame {
	
	private JFrame outFrame;
	
	private JTabbedPane tabbedPane;
	
	private int labelSpacing;
	
	private int bigLabelSpacing;
	
	private Font smallLabelFont;
	
	private Font labelFont;
	
	private Font labelBoldFont;
	
	private int panelNumber; 
	
	private InputManager iManager;
	
	public OutputFrame(InputManager iManager, int panelNumber) {
		this.panelNumber = panelNumber;
		this.iManager = iManager;
		
		buildFrame();
		buildTabbedPanel();
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		
		smallLabelFont = new Font("Times", Font.PLAIN, (int)((13 + rate)/2));
		labelFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelBoldFont = new Font("Times", Font.BOLD, (int)((15 + rate)/2));
		
		labelSpacing = (int)(outFrame.getHeight()/25);
		bigLabelSpacing = (int)(outFrame.getHeight()/18);
		
	}

	private void buildTabbedPanel() {
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBackground(Color.WHITE);
		tabbedPane.setBounds(0, labelSpacing + 40, outFrame.getWidth() - 10, outFrame.getHeight() - labelSpacing - 50);		
	}

	public void addOutput(DetectorOutput[] dOut) {
		if(dOut != null) {
			JPanel outPanel = buildOutputPanel(dOut[0], dOut[1]);
			if(dOut[1] != null) {
				tabbedPane.addTab("DB: " + dOut[1].getDataset() + " - Alg: " + dOut[1].getAlgorithm().replace("[", "").replace("]", ""), outPanel);
			} else tabbedPane.addTab("DB: " + dOut[0].getDataset() + " - Alg: " + dOut[0].getAlgorithm().replace("[", "").replace("]", ""), outPanel);
		}
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
		if(screenSize.getWidth() > 1000)
			outFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.6), (int)(screenSize.getHeight()*0.6));
		else outFrame.setBounds(0, 0, 600, 360);
		outFrame.setBackground(Color.WHITE);
		outFrame.setResizable(false);
	}
	
	public void buildSummaryPanel(List<DetectorOutput[]> outList){
		JPanel summaryPanel = new JPanel();
		summaryPanel.setBackground(Color.WHITE);
		summaryPanel.setBounds(0, 0, tabbedPane.getWidth() - 10, tabbedPane.getHeight() - 10);
		summaryPanel.setLayout(null);
		
		JPanel fPanel = new JPanel();
		fPanel.setBackground(Color.WHITE);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Common Setups ", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		fPanel.setBounds(summaryPanel.getWidth()/4, 0, summaryPanel.getWidth()/2, 3*labelSpacing);
		fPanel.setBorder(tb);
		fPanel.setLayout(null);
		fPanel.add(createLPanel(true, "Metric", fPanel, (int) (0.01*fPanel.getWidth()), labelSpacing, (outList != null && outList.size() > 0 && outList.get(0) != null ? (outList.get(0)[0] != null ? outList.get(0)[0].getReferenceMetric().getMetricName() : (outList.get(0)[1] != null ? outList.get(0)[1].getReferenceMetric().getMetricName() : "-")) : "-")));			
		summaryPanel.add(fPanel);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBounds(0, 0, summaryPanel.getWidth(), outList.size()*labelSpacing);
		contentPanel.setLayout(null);
		
		summaryPanel.add(buildOutputSummaryPanel(null, summaryPanel, 0, fPanel.getHeight() + labelSpacing));
		int i = 0;
		for(DetectorOutput[] dOuts : outList){
			contentPanel.add(buildOutputSummaryPanel((dOuts[1] != null ? dOuts[1] : dOuts[0]), contentPanel, i++, 0));
		}
		
		JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBounds(0, fPanel.getHeight() + 2*labelSpacing, contentPanel.getWidth(), summaryPanel.getHeight() - fPanel.getHeight() - 3*labelSpacing);
		
		summaryPanel.add(scroll);
		
		tabbedPane.add("Summary", summaryPanel);
	}
	
	private JPanel buildOutputSummaryPanel(DetectorOutput dOut, JPanel root, int i, int tabY){
		int elements = 6;
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBounds((int) (root.getWidth()*0.02), tabY + labelSpacing*(i), (int) (root.getWidth()*0.96), labelSpacing);
		panel.setLayout(null);
		
		JLabel lbl = new JLabel(dOut != null ? dOut.getDataset() : "Dataset");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		lbl.setBounds(0, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		lbl = new JLabel(dOut != null ? dOut.getAlgorithm().replace("[", "").replace("]", "") : "Algorithm");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		lbl.setBounds(panel.getWidth()/elements, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		lbl = new JLabel(dOut != null ? dOut.getBestSetup() : "Best Configuration");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		lbl.setBounds(panel.getWidth()*2/elements, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		lbl = new JLabel(dOut != null ? String.valueOf(dOut.getUsedFeatures().size()) : "Selected Features");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		lbl.setBounds(panel.getWidth()*3/elements, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		lbl = new JLabel(dOut != null ? dOut.getFaultsRatioString() : "Attacks Ratio");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		lbl.setBounds(panel.getWidth()*4/elements, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		lbl = new JLabel(dOut != null ? String.valueOf(dOut.getFormattedBestScore()) : "Best Score");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		lbl.setBounds(panel.getWidth()*5/elements, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbl);
		
		return panel;
	}
	
	private JPanel buildOutputPanel(DetectorOutput oOut, DetectorOutput dOut) {	
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBounds(0, 0, tabbedPane.getWidth() - 10, tabbedPane.getHeight() - 10);
		containerPanel.setLayout(null);
		
		JPanel miscPanel = new JPanel();
		miscPanel.setBackground(Color.WHITE);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Setup ", TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		miscPanel.setBounds(5, 10, containerPanel.getWidth()/2 - 10, 3*labelSpacing + 2*bigLabelSpacing);
		miscPanel.setBorder(tb);
		miscPanel.setLayout(null);
		miscPanel.add(createLPanel(true, "Dataset", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing, dOut != null ? dOut.getDataset() : oOut.getDataset()));
		miscPanel.add(createLPanel(true, "Algorithm", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing + bigLabelSpacing, dOut != null ? dOut.getAlgorithm().replace("[", "").replace("]", "") : oOut.getAlgorithm().replace("[", "").replace("]", "")));
		miscPanel.add(createLPanel(true, "Metric", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing+ 2*bigLabelSpacing, dOut != null ? dOut.getReferenceMetric().getMetricName() : oOut.getReferenceMetric().getMetricName()));			
		containerPanel.add(miscPanel);
		
		miscPanel = new JPanel();
		miscPanel.setBackground(Color.WHITE);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Details ", TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		miscPanel.setBounds(containerPanel.getWidth()/2 + 5, 10, containerPanel.getWidth()/2 - 10, 3*labelSpacing + 2*bigLabelSpacing);
		miscPanel.setBorder(tb);
		miscPanel.setLayout(null);
		miscPanel.add(createLPanel(true, "Best Setup", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing, dOut != null ? dOut.getBestSetup() : oOut.getBestSetup()));
		miscPanel.add(createLPanel(true, "Runs", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing + bigLabelSpacing, dOut != null ? dOut.getBestRuns() : oOut.getBestRuns()));
		miscPanel.add(createLPanel(true, "Best Score", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing + 2*bigLabelSpacing, dOut != null ? String.valueOf(dOut.getBestScore()) : String.valueOf(oOut.getBestScore())));			
		containerPanel.add(miscPanel);
		   
        String[] columnNames = new String[(dOut != null ? dOut.getEvaluationMetrics().length : oOut.getEvaluationMetrics().length) + 3];
        columnNames[0] = "Voter";
        columnNames[1] = "Anomaly";
        columnNames[2] = "Checkers";
		int i = 3;
        for(Metric met : (dOut != null ? dOut.getEvaluationMetrics() : oOut.getEvaluationMetrics())){
			columnNames[i++] = met.getMetricType() != null ? met.getMetricShortName() : "AUC";
		}	 
        
        int yDist = miscPanel.getHeight() + labelSpacing/2;
        
        JTable table;
        int refWidth = -1;
        JScrollPane scroll1 = null, scroll2 = null;
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        
        if(oOut != null){
        	JLabel lbl = new JLabel("Optimization Results");
    		lbl.setFont(labelFont);
    		lbl.setFont(lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));
    		lbl.setBounds(containerPanel.getWidth()*2/5, yDist, containerPanel.getWidth()/5, bigLabelSpacing);
    		lbl.setHorizontalAlignment(SwingConstants.CENTER);
    		containerPanel.add(lbl);
    		
    		yDist = yDist + bigLabelSpacing;
    		
	        table = new JTable(oOut.getEvaluationGrid(), columnNames);
	        table.setFillsViewportHeight(true);
	        for(int x=0;x<table.getColumnCount();x++){
	        	table.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
	        	table.getColumnModel().getColumn(x).setHeaderRenderer(centerRenderer);
	        }
	        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	        refWidth = resizeColumnWidth(table, -1);
	
	        scroll1 = new JScrollPane(table);
	        scroll1.setBounds(5, yDist, containerPanel.getWidth()-10, (int)table.getPreferredSize().getHeight() + 2*labelSpacing);
	        containerPanel.add(scroll1);
	        yDist = yDist + scroll1.getHeight();
        }
        
        if(dOut != null) {
        	JLabel lbl = new JLabel("Evaluation Results");
    		lbl.setFont(labelFont);
    		lbl.setFont(lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));
    		lbl.setBounds(containerPanel.getWidth()*2/5, yDist, containerPanel.getWidth()/5, bigLabelSpacing);
    		lbl.setHorizontalAlignment(SwingConstants.CENTER);
    		containerPanel.add(lbl);
    		
    		yDist = yDist + bigLabelSpacing;
    		
	        table = new JTable(dOut.getEvaluationGrid(), columnNames);
	        table.setFillsViewportHeight(true);
	        for(int x=0;x<table.getColumnCount();x++){
	        	table.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
	        	table.getColumnModel().getColumn(x).setHeaderRenderer(centerRenderer);
	        }
	        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	        resizeColumnWidth(table, refWidth);
	        
	        scroll2 = new JScrollPane(table);
	        scroll2.setBounds(5, yDist, containerPanel.getWidth()-10, (int)table.getPreferredSize().getHeight() + 2*labelSpacing);
	        containerPanel.add(scroll2);
	        yDist = yDist + scroll2.getHeight() + labelSpacing/2;
        }
        
        JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Detailed Outputs ", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		fPanel.setBounds(outFrame.getWidth()/5, yDist, outFrame.getWidth()*3/5, 2*bigLabelSpacing);
		fPanel.setBorder(tb);
		fPanel.setLayout(new GridLayout(1, 4));
		((GridLayout)fPanel.getLayout()).setHgap(20);
		
		JButton button = new JButton("Output Folder");
		button.setVisible(true);
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
		
		button = new JButton("Selected Features");
		button.setVisible(true);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				FeaturesFrame odf;
				if(dOut != null)
					odf = new FeaturesFrame(dOut);
				else odf = new FeaturesFrame(oOut);
				odf.setVisible(true);
			} } );	
		fPanel.add(button);
		
		button = new JButton("Training Detail");
		button.setVisible(true);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				TrainingDetailFrame odf;
				if(dOut != null)
					odf = new TrainingDetailFrame(dOut);
				else odf = new TrainingDetailFrame(oOut);
				odf.setVisible(true);
			} } );	
		fPanel.add(button);
		
		button = new JButton("Plot Results");
		button.setVisible(true);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				OutputDetailFrame odf;
				if(dOut != null)
					odf = new OutputDetailFrame(dOut);
				else odf = new OutputDetailFrame(oOut);
				odf.buildMainPanel();
				odf.setVisible(true);
			} } );	
		fPanel.add(button);
		
		containerPanel.add(fPanel);
		
		return containerPanel;
	}
	
	public int resizeColumnWidth(JTable table, int refWidth) {
	    final TableColumnModel columnModel = table.getColumnModel();
	    int width = 60; // Min width
	    
	    for (int column = 0; column < table.getColumnCount(); column++) {
	        
	        if(refWidth <= 0) {
		    	for (int row = 0; row < table.getRowCount(); row++) {
		            TableCellRenderer renderer = table.getCellRenderer(row, column);
		            Component comp = table.prepareRenderer(renderer, row, column);
		            width = Math.max(comp.getPreferredSize().width +1 , width);
		        }
		        if(width > 100)
		            width = 100;
	        } else width = refWidth;
	        columnModel.getColumn(column).setPreferredWidth(width);
	    }
	    
	    return width;
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
