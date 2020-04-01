/**
 * 
 */
package ippoz.reload.ui;

import ippoz.reload.manager.InputManager;
import ippoz.reload.metric.Metric;
import ippoz.reload.output.DetectorOutput;

import java.awt.BorderLayout;
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
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
	}
	
	public void buildSummaryPanel(List<DetectorOutput[]> outList){
		JPanel summaryPanel = new JPanel();
		summaryPanel.setBackground(Color.WHITE);
		//summaryPanel.setBounds(0, 0, tabbedPane.getWidth() - 10, tabbedPane.getHeight() - 10);
		summaryPanel.setLayout(new BorderLayout());
		summaryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel fPanel = new JPanel();
		fPanel.setBackground(Color.WHITE);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Common Setups ", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		//fPanel.setBounds(summaryPanel.getWidth()/4, 0, summaryPanel.getWidth()/2, 3*labelSpacing);
		fPanel.setBorder(new CompoundBorder(new EmptyBorder(0, 200, 0, 200), tb));
		fPanel.setLayout(new GridLayout(1, 1));
		fPanel.add(createLPanel(true, "Metric", fPanel, (int) (0.01*fPanel.getWidth()), labelSpacing, (outList != null && outList.size() > 0 && outList.get(0) != null ? (outList.get(0)[0] != null ? outList.get(0)[0].getReferenceMetric().getMetricName() : (outList.get(0)[1] != null ? outList.get(0)[1].getReferenceMetric().getMetricName() : "-")) : "-"), null));			
		summaryPanel.add(fPanel, BorderLayout.NORTH);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setBackground(Color.WHITE);
		//contentPanel.setBounds(0, 0, summaryPanel.getWidth(), outList.size()*labelSpacing);
		contentPanel.setLayout(new GridLayout(outList.size()+1, 1));
		
		contentPanel.add(buildOutputSummaryPanel(null, summaryPanel, 0, fPanel.getHeight() + labelSpacing), BorderLayout.CENTER);
		int i = 0;
		for(DetectorOutput[] dOuts : outList){
			contentPanel.add(buildOutputSummaryPanel((dOuts[1] != null ? dOuts[1] : dOuts[0]), contentPanel, i++, 0));
		}
		
		JScrollPane scroll = new JScrollPane(contentPanel);
        //scroll.setBounds(0, fPanel.getHeight() + 2*labelSpacing, contentPanel.getWidth(), summaryPanel.getHeight() - fPanel.getHeight() - 3*labelSpacing);
		
		summaryPanel.add(scroll, BorderLayout.CENTER);
		
		tabbedPane.add("Summary", summaryPanel);
	}
	
	private JPanel buildOutputSummaryPanel(DetectorOutput dOut, JPanel root, int i, int tabY){
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new GridLayout(1, 5, 50, 5));
		
		JLabel lbl = new JLabel(dOut != null ? dOut.getDataset() : "Dataset");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		//lbl.setBounds(0, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Name of the loader used by RELOAD");
		panel.add(lbl);
		
		lbl = new JLabel(dOut != null ? dOut.getAlgorithm().replace("[", "").replace("]", "") : "Algorithm");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		//lbl.setBounds(panel.getWidth()/elements, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Algorithm used by RELOAD");
		panel.add(lbl);
		
		lbl = new JLabel(dOut != null ? String.valueOf(dOut.getUsedFeatures().size()) : "Selected Features");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		//lbl.setBounds(panel.getWidth()*3/elements, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Amount of features (simple or combined) used by the Algorithm. Note that this amount depends on the Feature Aggregation policy chosen.");
		panel.add(lbl);
		
		lbl = new JLabel(dOut != null ? dOut.getFaultsRatioString() : "Attacks Ratio");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		//lbl.setBounds(panel.getWidth()*4/elements, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("% of Faults/Attacks in the Evaluation Set");
		panel.add(lbl);
		
		lbl = new JLabel(dOut != null ? String.valueOf(dOut.getFormattedBestScore()) : "Metric Score");
		if(dOut == null)
			lbl.setFont(labelBoldFont);
		else lbl.setFont(smallLabelFont);
		//lbl.setBounds(panel.getWidth()*5/elements, 0, panel.getWidth()/elements, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setToolTipText("Metric score on the Evaluation Set");
		panel.add(lbl);
		
		return panel;
	}
	
	private JPanel buildOutputPanel(DetectorOutput oOut, DetectorOutput dOut) {	
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		//containerPanel.setBounds(0, 0, tabbedPane.getWidth() - 10, tabbedPane.getHeight() - 10);
		containerPanel.setLayout(new BorderLayout());
		
		JPanel headerPanel = new JPanel();
		headerPanel.setBackground(Color.WHITE);
		headerPanel.setLayout(new GridLayout(1, 2));
		headerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
		
		JPanel miscPanel = new JPanel();
		miscPanel.setBackground(Color.WHITE);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Setup ", TitledBorder.LEFT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		//miscPanel.setBounds(5, 10, containerPanel.getWidth()/2 - 10, 3*labelSpacing + 2*bigLabelSpacing);
		miscPanel.setBorder(tb);
		miscPanel.setLayout(new GridLayout(3, 1, 10, 5));
		
		miscPanel.add(createLPanel(true, "Dataset", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing, dOut != null ? dOut.getDataset() : oOut.getDataset(), "Name of the loader used bt RELOAD to calculate this score"));
		miscPanel.add(createLPanel(true, "Algorithm", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing + bigLabelSpacing, dOut != null ? dOut.getAlgorithm().replace("[", "").replace("]", "") : oOut.getAlgorithm().replace("[", "").replace("]", ""), "Algorithm used by RELOAD"));
		miscPanel.add(createLPanel(true, "Metric", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing+ 2*bigLabelSpacing, dOut != null ? dOut.getReferenceMetric().getMetricName() : oOut.getReferenceMetric().getMetricName(), "Metric used by RELOAD"));			
		
		headerPanel.add(miscPanel);
		
		miscPanel = new JPanel();
		miscPanel.setBackground(Color.WHITE);
		
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Details ", TitledBorder.RIGHT, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		miscPanel.setBorder(tb);
		miscPanel.setLayout(new GridLayout(3, 1, 10, 5));

		miscPanel.add(createLPanel(true, "Best Setup", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing, dOut != null ? dOut.getVoter().toString() : oOut.getVoter().toString(), "Setup used to aggregate different anomaly checkers, if more than one"));
		miscPanel.add(createLPanel(true, "Evaluation Runs", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing + bigLabelSpacing, dOut != null ? dOut.getBestRuns() : oOut.getBestRuns(), "Runs used for Evaluation"));
		miscPanel.add(createLPanel(true, "Metric Score", miscPanel, (int) (0.02*miscPanel.getWidth()), labelSpacing + 2*bigLabelSpacing, dOut != null ? String.valueOf(dOut.getBestScore()) : String.valueOf(oOut.getBestScore()), "Metric Score on Evaluation Runs"));			
		
		headerPanel.add(miscPanel);
		
		containerPanel.add(headerPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setBackground(Color.WHITE);
		centerPanel.setLayout(new GridLayout(2, 1, 0, 20));
		centerPanel.setBorder(new EmptyBorder(20, 10, 20, 10));
		   
        String[] columnNames = new String[(dOut != null ? dOut.getEvaluationMetrics().length : oOut.getEvaluationMetrics().length) + 2];
        columnNames[0] = "Voter";
        columnNames[1] = "# Checkers";
		int i = 2;
        for(Metric met : (dOut != null ? dOut.getEvaluationMetrics() : oOut.getEvaluationMetrics())){
			columnNames[i++] = met.getMetricShortName();
		}	 
        
        int yDist = miscPanel.getHeight() + labelSpacing/2;
        
        JTable table;
        int refWidth = -1;
        JScrollPane scroll1 = null, scroll3 = null;
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );
        
        if(oOut != null){
        	JPanel optPanel = new JPanel();
    		optPanel.setBackground(Color.WHITE);
    		optPanel.setLayout(new BorderLayout());
    		
        	JLabel lbl = new JLabel("Optimization Results (Average per Run)");
    		lbl.setFont(labelFont);
    		lbl.setFont(lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));
    		lbl.setBounds(containerPanel.getWidth()/5, yDist, containerPanel.getWidth()*3/5, bigLabelSpacing);
    		lbl.setHorizontalAlignment(SwingConstants.CENTER);
    		optPanel.add(lbl, BorderLayout.NORTH);
    		
    		yDist = yDist + bigLabelSpacing;
    		
	        table = new JTable(oOut.getOptimizationGrid(oOut.buildPath(iManager.getOutputFolder())), columnNames);
	        table.setFillsViewportHeight(true);
	        for(int x=0;x<table.getColumnCount();x++){
	        	table.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
	        	table.getColumnModel().getColumn(x).setHeaderRenderer(centerRenderer);
	        }
	        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	        refWidth = resizeColumnWidth(table, -1);
	
	        scroll1 = new JScrollPane(table);
	        //scroll1.setBounds(5, yDist, containerPanel.getWidth()-10, (int)table.getPreferredSize().getHeight() + 2*labelSpacing);
	        optPanel.add(scroll1, BorderLayout.CENTER);
	        yDist = yDist + scroll1.getHeight();
	        
	        centerPanel.add(optPanel);
        }
        
        if(dOut != null) {
        	JPanel evPanel = new JPanel();
        	evPanel.setBackground(Color.WHITE);
        	evPanel.setLayout(new BorderLayout());
    		
        	JLabel lbl = new JLabel("Evaluation Results (Global)");
    		lbl.setFont(labelFont);
    		lbl.setFont(lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));
    		lbl.setBounds(containerPanel.getWidth()/5, yDist, containerPanel.getWidth()*3/5, bigLabelSpacing);
    		lbl.setHorizontalAlignment(SwingConstants.CENTER);
    		evPanel.add(lbl, BorderLayout.NORTH);
    		
    		yDist = yDist + bigLabelSpacing;
    		
	        table = new JTable(dOut.getEvaluationGrid(dOut.buildPath(iManager.getOutputFolder())), columnNames);
	        table.setFillsViewportHeight(true);
	        for(int x=0;x<table.getColumnCount();x++){
	        	table.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
	        	table.getColumnModel().getColumn(x).setHeaderRenderer(centerRenderer);
	        }
	        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	        resizeColumnWidth(table, refWidth);
	        
	        scroll3 = new JScrollPane(table);
	        //scroll3.setBounds(5, yDist, containerPanel.getWidth()-10, (int)table.getPreferredSize().getHeight() + 2*labelSpacing);
	        evPanel.add(scroll3, BorderLayout.CENTER);
	        yDist = yDist + scroll3.getHeight() + labelSpacing/2;
	        
	        centerPanel.add(evPanel);
        }
        
        containerPanel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), " Detailed Outputs ", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		//fPanel.setBounds(outFrame.getWidth()/5, yDist, outFrame.getWidth()*3/5, 2*bigLabelSpacing);
		fPanel.setBorder(tb);
		fPanel.setLayout(new GridLayout(1, 4, 20, 0));
		
		JButton button = new JButton("Output Folder");
		button.setVisible(true);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				Desktop desktop = Desktop.getDesktop();
		        File dirToOpen = new File(dOut != null ? dOut.buildPath(iManager.getOutputFolder()) : oOut.buildPath(iManager.getOutputFolder()));
		        try {
		            desktop.open(dirToOpen);
		        } catch (IOException ex) {
		        	JOptionPane.showMessageDialog(outFrame, "ERROR: Unable to open '" + (dOut != null ? dOut.buildPath(iManager.getOutputFolder()) : oOut.buildPath(iManager.getOutputFolder())) + "'");
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
		
		containerPanel.add(fPanel, BorderLayout.SOUTH);
		
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
		return createLPanel(false, textName, root, (int) (root.getWidth()*0.02), panelY, textFieldText, null);
	}
	
	public JPanel createLPanel(String textName, JPanel root, int panelY, String textFieldText, String tooltipText){
		return createLPanel(false, textName, root, (int) (root.getWidth()*0.02), panelY, textFieldText, tooltipText);
	}
	
	public JPanel createLPanel(boolean bold, String textName, JPanel root, int panelX, int panelY, String textFieldText, String tooltipText){
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		//panel.setBounds(panelX, panelY, (int) (root.getWidth()*0.96), labelSpacing);
		panel.setLayout(new GridLayout(1, 2, 20, 5));
		
		JLabel lbl = new JLabel(textName);
		lbl.setFont(labelFont);
		if(bold)
			lbl.setFont(lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));
		//lbl.setBounds(panel.getWidth()/10, 0, panel.getWidth()*3/10, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		if(tooltipText != null)
			lbl.setToolTipText(tooltipText);
		panel.add(lbl);
		
		JLabel lbldata = new JLabel(textFieldText);
		lbldata.setFont(labelFont);
		//lbldata.setBounds(panel.getWidth()/5*2, 0, panel.getWidth()/2, labelSpacing);
		lbldata.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lbldata);
		
		return panel;
	}
	
}
