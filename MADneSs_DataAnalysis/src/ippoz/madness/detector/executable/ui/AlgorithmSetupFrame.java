/**
 * 
 */
package ippoz.madness.detector.executable.ui;

import ippoz.madness.detector.algorithm.DetectionAlgorithm;
import ippoz.madness.detector.decisionfunction.DecisionFunction;
import ippoz.madness.detector.decisionfunction.DecisionFunctionType;
import ippoz.madness.detector.manager.InputManager;
import ippoz.reload.commons.algorithm.AlgorithmType;
import ippoz.reload.commons.configuration.AlgorithmConfiguration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

/**
 * @author Tommy
 *
 */
public class AlgorithmSetupFrame {
	
	private JFrame asFrame;
	
	private int labelSpacing;
	
	private int bigLabelSpacing;
	
	private Font labelFont;
	
	private Font labelBoldFont;
	
	private JPanel algPanel;
	
	private InputManager iManager;
	
	private List<AlgorithmConfiguration> confList;
	
	private AlgorithmType algType;
	
	private String[] algParams;
	
	public AlgorithmSetupFrame( InputManager iManager, AlgorithmType algType, List<AlgorithmConfiguration> confList) {
		this.iManager = iManager;
		this.algType = algType;
		this.confList = confList;
		if(confList != null && confList.size() > 0){
			algParams = confList.get(0).listLabels().toArray(new String[confList.get(0).listLabels().size()]);
		}
		buildFrame();
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		
		labelFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelBoldFont = new Font("Times", Font.BOLD, (int)((15 + rate)/2));
		
		labelSpacing = (int)(asFrame.getHeight()/25);
		bigLabelSpacing = (int)(asFrame.getHeight()/18);
		
		algPanel = buildMainPanel();
	}

	public void setVisible(boolean b) {
		if(asFrame != null){
			asFrame.add(algPanel);
			asFrame.setLocationRelativeTo(null);
			asFrame.setVisible(b);
		}
	}

	private void buildFrame(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		asFrame = new JFrame();
		asFrame.setTitle("Setup of Algorithm '" + algType + "'");
		if(screenSize.getWidth() > 1000)
			asFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.5), (int)(screenSize.getHeight()*0.5));
		else asFrame.setBounds(0, 0, 500, 600);
		asFrame.setBackground(Color.WHITE);
		asFrame.setResizable(false);
	}
	
	private JPanel buildMainPanel() {	
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBounds(0, 0, asFrame.getWidth() - 10, asFrame.getHeight() - 10);
		containerPanel.setLayout(null);
		
		// HEADER
		
		JPanel headerPanel = new JPanel();
		headerPanel.setBounds(containerPanel.getWidth()/4, 0, containerPanel.getWidth()/2, bigLabelSpacing*3);
		headerPanel.setBackground(Color.WHITE);
		headerPanel.setLayout(null);
		
		JLabel lbl = new JLabel("Algorithm:");
		lbl.setFont(labelBoldFont);
		lbl.setBounds(0, 0, headerPanel.getWidth()/2, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		headerPanel.add(lbl);
		
		lbl = new JLabel(algType.toString());
		lbl.setFont(labelFont);
		lbl.setBounds(headerPanel.getWidth()/2, 0, headerPanel.getWidth()/2, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		headerPanel.add(lbl);
		
		lbl = new JLabel("Parameters:");
		lbl.setFont(labelBoldFont);
		lbl.setBounds(0, bigLabelSpacing, headerPanel.getWidth()/2, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		headerPanel.add(lbl);
		
		lbl = new JLabel(Arrays.toString(algParams));
		lbl.setFont(labelFont);
		lbl.setBounds(headerPanel.getWidth()/2, bigLabelSpacing, headerPanel.getWidth()/2, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		headerPanel.add(lbl);
		
		lbl = new JLabel("Combinations:");
		lbl.setFont(labelBoldFont);
		lbl.setBounds(0, 2*bigLabelSpacing, headerPanel.getWidth()/2, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		headerPanel.add(lbl);
		
		lbl = new JLabel(String.valueOf(confList.size()));
		lbl.setFont(labelFont);
		lbl.setBounds(headerPanel.getWidth()/2, 2*bigLabelSpacing, headerPanel.getWidth()/2, labelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		headerPanel.add(lbl);
		
		containerPanel.add(headerPanel);
		
		// EXPLANATION
		
		JPanel expPanel = new JPanel();
		expPanel.setBackground(Color.WHITE);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Description", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		expPanel.setBounds((int)(containerPanel.getWidth()*0.02), headerPanel.getHeight() + labelSpacing/2, (int)(containerPanel.getWidth()*0.96), 4*bigLabelSpacing + labelSpacing);
		expPanel.setBorder(tb);
		expPanel.setLayout(null);
		
		lbl = new JLabel("<html> " + DetectionAlgorithm.explainParameters(algType) + " <br> " + 
				"Press Add # to add a new configuration below the clicked row. " + 
				"Press Remove # to remove the configuration reported in the clicked row. </html>");
		lbl.setFont(labelFont);
		lbl.setBounds((int)(expPanel.getWidth()*0.02), labelSpacing/2, (int)(expPanel.getWidth()*0.95), 4*bigLabelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		expPanel.add(lbl);
		
		containerPanel.add(expPanel);
		
		// MAIN
		
		JTable table = new JTable(new MyTableModel());
        table.setFillsViewportHeight(true);
        JComboBox<DecisionFunctionType> cb = new JComboBox<DecisionFunctionType>(DecisionFunctionType.values());
        for(int i=0;i<algParams.length;i++){
        	if(algParams[i].equals("threshold")){
        		table.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(cb));
        		break;
        	}
        }
        table.getColumn("Add Item").setCellRenderer(new ButtonRenderer());
        table.getColumn("Add Item").setCellEditor(new ButtonEditor(new JCheckBox(), table));
        table.getColumn("Remove Item").setCellRenderer(new ButtonRenderer());
        table.getColumn("Remove Item").setCellEditor(new ButtonEditor(new JCheckBox(), table));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int x=0;x<table.getColumnCount();x++){
        	table.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
        	table.getColumnModel().getColumn(x).setHeaderRenderer(centerRenderer);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(containerPanel.getWidth()/8, headerPanel.getHeight() + expPanel.getHeight() + labelSpacing, containerPanel.getWidth()*3/4, (int)table.getPreferredSize().getHeight() + labelSpacing);
        containerPanel.add(scroll);
		
		// FOOTER
        
        JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Apply Changes", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		fPanel.setBounds(containerPanel.getWidth()/4, headerPanel.getHeight() + expPanel.getHeight() + scroll.getHeight() + labelSpacing, containerPanel.getWidth()/2, 2*bigLabelSpacing);
		fPanel.setBorder(tb);
		fPanel.setLayout(null);
		
		JButton button = new JButton("Save Changes");
		button.setVisible(true);
		button.setFont(labelBoldFont);
		button.setBounds(30, labelSpacing, fPanel.getWidth()/2 - 60, bigLabelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				iManager.updateConfiguration(algType, confList);
			} } );	
		fPanel.add(button);
		
		button = new JButton("Discard Changes");
		button.setVisible(true);
		button.setFont(labelBoldFont);
		button.setBounds(fPanel.getWidth()/2 + 30, labelSpacing, fPanel.getWidth()/2 - 60, bigLabelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				asFrame.setVisible(false);
			} } );	
		fPanel.add(button);
		
		containerPanel.add(fPanel);
		
		if(asFrame.getHeight() < headerPanel.getHeight() + expPanel.getHeight() + scroll.getHeight() + fPanel.getHeight() + 70)
			asFrame.setBounds(asFrame.getX(), asFrame.getY(), asFrame.getWidth(), headerPanel.getHeight() + expPanel.getHeight() + scroll.getHeight() + fPanel.getHeight() + 70);
        		
		return containerPanel;
	}
	
	public void resizeColumnWidth(JTable table) {
	    final TableColumnModel columnModel = table.getColumnModel();
	    int width = 200; // Min width
	    
	    for (int column = 0; column < table.getColumnCount(); column++) {
	        
	        for (int row = 0; row < table.getRowCount(); row++) {
	            TableCellRenderer renderer = table.getCellRenderer(row, column);
	            Component comp = table.prepareRenderer(renderer, row, column);
	            width = Math.max(comp.getPreferredSize().width + 10, width);
	        }
	        if(width > 100)
	            width = 100;
	        columnModel.getColumn(column).setPreferredWidth(width);
	    }
	}
	
	private class MyTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;
		
		public int getColumnCount() {
			return algParams.length + 2;
		}
		
		public int getRowCount() {
			return confList.size();
		}
		
		public String getColumnName(int col) {
			if(col < algParams.length)
				return algParams[col];
			else if(col == algParams.length)
				return "Add Item";
			else return "Remove Item";
		}
		
		public Object getValueAt(int row, int col) {
			Object ob;
			if(col == algParams.length)
				ob = "Add " + row;
			else if(col == algParams.length + 1)
				ob = "Remove " + row;
			else ob = confList.get(row).getItem(algParams[col]);
			return ob;
		}
		
		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
		
		public boolean isCellEditable(int row, int col) {
			return true;
		}
		
		public void setValueAt(Object aValue, int row, int col) {
	        if(col >= 0 && col < algParams.length){
	        	if(algParams[col].equals("threshold")){
		        	String s = (String)JOptionPane.showInputDialog(
		                    asFrame, "Set parameters for decision function '" + aValue.toString() + "'.\n" +
		                    		"Details: " + DecisionFunction.getParameterDetails(aValue.toString()),
		                    "Params for " + aValue.toString(), JOptionPane.PLAIN_MESSAGE, null, null, "");
					if ((s != null) && (s.length() > 0) && DecisionFunction.checkClassifier(s)) {
						confList.get(row).addItem(algParams[col], s);
					} else {
						String suggestedValue = "";
						switch(DecisionFunctionType.valueOf(aValue.toString())){
							case CLUSTER:
								suggestedValue = "VAR";
								break;
							case CONFIDENCE_INTERVAL:
								suggestedValue = "(1)";
								break;
							case DOUBLE_THRESHOLD:
								suggestedValue = "(1,1)";
								break;
							case IQR:
							case LEFT_IQR:
							case RIGHT_IQR:
								suggestedValue = "";
								break;
							case LOG_THRESHOLD:
								break;
							case STATIC_THRESHOLD:
								break;
							case THRESHOLD:
								suggestedValue = "0.9";
								break;
							default:
								break;
						}
						confList.get(row).addItem(algParams[col], aValue.toString() + suggestedValue);
					}
		        } else confList.get(row).addItem(algParams[col], aValue.toString());
	        }
	        fireTableCellUpdated(row, col);
	    }
	
	}
	
	private class ButtonRenderer extends JButton implements TableCellRenderer {

		private static final long serialVersionUID = 1L;

		public ButtonRenderer() {
		    setOpaque(true);
		  }

		  public Component getTableCellRendererComponent(JTable table, Object value,
		      boolean isSelected, boolean hasFocus, int row, int column) {
		    if (isSelected) {
		      setForeground(table.getSelectionForeground());
		      setBackground(table.getSelectionBackground());
		    } else {
		      setForeground(table.getForeground());
		      setBackground(UIManager.getColor("Button.background"));
		    }
		    setText((value == null) ? "" : value.toString());
		    return this;
		  }
		}

		/**
		 * @version 1.0 11/09/98
		 */

		private class ButtonEditor extends DefaultCellEditor {
		  
			private static final long serialVersionUID = 1L;

			protected JButton button;

		  private String label;

		  private boolean isPushed;
		  
		  private JTable table;

		  public ButtonEditor(JCheckBox checkBox, JTable table) {
		    super(checkBox);
		    this.table = table;
		    button = new JButton();
		    button.setOpaque(true);
		    button.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        fireEditingStopped();
		      }
		    });
		    

		  }

		  public Component getTableCellEditorComponent(JTable table, Object value,
		      boolean isSelected, int row, int column) {
		    if (isSelected) {
		      button.setForeground(table.getSelectionForeground());
		      button.setBackground(table.getSelectionBackground());
		    } else {
		      button.setForeground(table.getForeground());
		      button.setBackground(table.getBackground());
		    }
		    label = (value == null) ? "" : value.toString();
		    button.setText(label);
		    isPushed = true;
		    return button;
		  }

		  public Object getCellEditorValue() {
		    if (isPushed) {
		    	int index = Integer.parseInt(label.split(" ")[1]);
		    	if(label.contains("Add")){
		    		try {
						confList.add(index+1, (AlgorithmConfiguration) confList.get(index).clone());
						
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	} else confList.remove(index);
		    	((MyTableModel)table.getModel()).fireTableDataChanged();
		    }
		    isPushed = false;
		    return new String(label);
		  }

		  public boolean stopCellEditing() {
		    isPushed = false;
		    return super.stopCellEditing();
		  }

		  protected void fireEditingStopped() {
		    super.fireEditingStopped();
		  }
		}

}
