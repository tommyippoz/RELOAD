/**
 * 
 */
package ippoz.reload.executable.ui;

import ippoz.reload.featureselection.FeatureSelector;
import ippoz.reload.featureselection.FeatureSelectorType;
import ippoz.reload.featureselection.VarianceFeatureSelector;
import ippoz.reload.manager.InputManager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
public class FeatureSelectionFrame {
	
	private JFrame fsFrame;
	
	private int labelSpacing;
	
	private int bigLabelSpacing;
	
	private Font labelFont;
	
	private Font labelBoldFont;
	
	private JPanel fsPanel;
	
	private InputManager iManager;
	
	private List<FeatureSelector> fsList;

	public FeatureSelectionFrame(InputManager iManager, List<FeatureSelector> featureSelectors) {
		this.iManager = iManager;
		fsList = featureSelectors;
		
		buildFrame();
		
		double rate = 18*Toolkit.getDefaultToolkit().getScreenSize().getHeight()/1080;
		
		labelFont = new Font("Times", Font.PLAIN, (int)((16 + rate)/2));
		labelBoldFont = new Font("Times", Font.BOLD, (int)((15 + rate)/2));
		
		labelSpacing = (int)(fsFrame.getHeight()/25);
		bigLabelSpacing = (int)(fsFrame.getHeight()/18);
		
		fsPanel = buildMainPanel();
	}

	public void setVisible(boolean b) {
		if(fsFrame != null){
			fsFrame.add(fsPanel);
			fsFrame.setLocationRelativeTo(null);
			fsFrame.setVisible(b);
		}
	}

	private void buildFrame(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		fsFrame = new JFrame();
		fsFrame.setTitle("Feature Selection Strategies");
		if(screenSize.getWidth() > 1000)
			fsFrame.setBounds(0, 0, (int)(screenSize.getWidth()*0.5), (int)(screenSize.getHeight()*0.5));
		else fsFrame.setBounds(0, 0, 500, 600);
		fsFrame.setBackground(Color.WHITE);
		fsFrame.setResizable(false);
	}
	
	private JPanel buildMainPanel() {	
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBounds(0, 0, fsFrame.getWidth() - 10, fsFrame.getHeight() - 10);
		containerPanel.setLayout(null);
		
		// EXPLANATION
		
		JPanel expPanel = new JPanel();
		expPanel.setBackground(Color.WHITE);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Description of Feature Selectors", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		expPanel.setBounds((int)(containerPanel.getWidth()*0.02), labelSpacing/2, (int)(containerPanel.getWidth()*0.96), (FeatureSelectorType.values().length+1)*2*bigLabelSpacing + labelSpacing);
		expPanel.setBorder(tb);
		expPanel.setLayout(null);
		
		JLabel lbl = new JLabel("<html> " + FeatureSelector.explainSelectors() + " <br> " + 
				"Press Add # to add a new configuration below the clicked row. <br>" + 
				"Press Remove # to remove the configuration reported in the clicked row. </html>");
		lbl.setFont(labelFont);
		lbl.setBounds((int)(expPanel.getWidth()*0.02), labelSpacing/2, (int)(expPanel.getWidth()*0.95), (FeatureSelectorType.values().length+1)*2*bigLabelSpacing);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		
		expPanel.add(lbl);
		
		containerPanel.add(expPanel);
		
		// MAIN
		
		JTable table = new JTable(new MyTableModel());
        table.setFillsViewportHeight(true);
        JComboBox<FeatureSelectorType> cb = new JComboBox<FeatureSelectorType>(FeatureSelectorType.values());
        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(cb));
        table.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox(), table));
        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), table));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for(int x=0;x<table.getColumnCount();x++){
        	table.getColumnModel().getColumn(x).setCellRenderer(centerRenderer);
        	table.getColumnModel().getColumn(x).setHeaderRenderer(centerRenderer);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBounds(containerPanel.getWidth()/8, expPanel.getHeight() + labelSpacing, containerPanel.getWidth()*3/4, (int)table.getPreferredSize().getHeight() + 2*labelSpacing);
        containerPanel.add(scroll);
		
		// FOOTER
        
        JPanel fPanel = new JPanel();
        fPanel.setBackground(Color.WHITE);
		tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2), "Apply Changes", TitledBorder.CENTER, TitledBorder.CENTER, new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		fPanel.setBounds(containerPanel.getWidth()/4, expPanel.getHeight() + scroll.getHeight() + labelSpacing, containerPanel.getWidth()/2, 2*bigLabelSpacing);
		fPanel.setBorder(tb);
		fPanel.setLayout(null);
		
		JButton button = new JButton("Save Changes");
		button.setVisible(true);
		button.setFont(labelBoldFont);
		button.setBounds(labelSpacing, labelSpacing, fPanel.getWidth()/2 - 60, bigLabelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				iManager.updateFeatureSelectionPolicies(fsList);
			} } );	
		fPanel.add(button);
		
		button = new JButton("Exit");
		button.setVisible(true);
		button.setFont(labelBoldFont);
		button.setBounds(fPanel.getWidth()/2 + 30, labelSpacing, fPanel.getWidth()/2 - 60, bigLabelSpacing);
		button.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) { 
				fsFrame.setVisible(false);
			} } );	
		fPanel.add(button);
		
		containerPanel.add(fPanel);
		
		if(fsFrame.getHeight() < expPanel.getHeight() + scroll.getHeight() + fPanel.getHeight() + 70)
			fsFrame.setBounds(fsFrame.getX(), fsFrame.getY(), fsFrame.getWidth(), expPanel.getHeight() + scroll.getHeight() + fPanel.getHeight() + 70);
        		
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
			return 4;
		}
		
		public int getRowCount() {
			return fsList.size();
		}
		
		public String getColumnName(int col) {
			switch(col){
				case 0:
					return "Selector Type";
				case 1:
					return "Threshold";
				case 2:
					return "Add Item";
				case 3:
					return "Remove Item";
				default:
					return "-";
			}
		}
		
		public Object getValueAt(int row, int col) {
			Object ob;
			switch(col){
			case 0:
				ob = fsList.get(row).getFeatureSelectorType();
				break;
			case 1:
				ob = fsList.get(row).getSelectorThreshold();
				break;
			case 2:
				ob = "Add " + row;
				break;
			case 3:
				ob = "Remove " + row;
				break;
			default:
				ob = "-";
			}
			return ob;
		}
		
		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
		
		public boolean isCellEditable(int row, int col) {
			return true;
		}
		
		public void setValueAt(Object aValue, int row, int col) {
			double threshold;
			FeatureSelectorType fst;
			if(col == 0){
				try {
					if(aValue instanceof FeatureSelectorType)
						fst = (FeatureSelectorType)aValue;
					else fst = FeatureSelectorType.valueOf(aValue.toString().trim());
		        	threshold = fsList.get(row).getSelectorThreshold();
		        	fsList.remove(row);
		        	fsList.add(row, FeatureSelector.createSelector(fst, threshold));
				} catch(Exception ex){
					
				}
	        } else if(col == 1){
				try {
					if(aValue instanceof Double)
						threshold = (Double)aValue;
					else threshold = Double.valueOf(aValue.toString().trim());
					fsList.get(row).updateSelectorThreshold(threshold);
				} catch(Exception ex){
					
				}
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
					fsList.add(index+1, new VarianceFeatureSelector(1.0));	
		    	} else fsList.remove(index);
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
