/**
 * 
 */
package ippoz.reload.executable.ui;

import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.support.AppUtility;
import ippoz.reload.featureselection.FeatureSelectorType;
import ippoz.reload.output.DetectorOutput;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Tommy
 *
 */
public class FeaturesFrame {

	private JFrame fFrame;

	private Font labelFont;

	private JPanel featurePanel;

	private DetectorOutput dOut;

	private Map<DataSeries, Map<FeatureSelectorType, Double>> fScores;

	private List<FeatureSelectorType> columns;

	private List<DataSeries> rows;

	public FeaturesFrame(DetectorOutput dOut) {
		this.dOut = dOut;
		fScores = dOut.getSelectedFeatures();
		rows = new ArrayList<DataSeries>(fScores.keySet());
		
		if(rows != null && rows.size() > 0 && rows.get(0) != null && rows.get(0).size() > 0)
			columns = new ArrayList<FeatureSelectorType>(fScores.get(rows.get(0)).keySet());
		else columns = new ArrayList<FeatureSelectorType>();

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

	private void buildFrame() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		fFrame = new JFrame();
		fFrame.setTitle("Selected Features for '" + dOut.getDataset() + "'");
		if (screenSize.getWidth() > 1000)
			fFrame.setBounds(0, 0, (int) (screenSize.getWidth() * 0.3),
					(int) (screenSize.getHeight() * 0.5));
		else
			fFrame.setBounds(0, 0, 300, 450);
		fFrame.setBackground(Color.WHITE);
		fFrame.setResizable(false);
	}

	private JPanel buildMainPanel() {
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.setAlignmentX(SwingConstants.CENTER);

		// BODY

		JPanel fPanel = new JPanel();
		fPanel.setBackground(Color.WHITE);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2),
				" Features Detail ", TitledBorder.CENTER, TitledBorder.CENTER,
				new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		fPanel.setBorder(tb);
		fPanel.setLayout(new GridLayout(2, 2));

		JLabel lbl = new JLabel("Selected Features: " + String.valueOf(fScores.size()));
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		fPanel.add(lbl);
		
		lbl = new JLabel("Used Features: " + String.valueOf(dOut.getUsedFeatures().size()));
		lbl.setFont(labelFont);
		lbl.setFont(lbl.getFont().deriveFont(lbl.getFont().getStyle() | Font.BOLD));
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		fPanel.add(lbl);
		
		lbl = new JLabel("Feature Aggregation: ");
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		fPanel.add(lbl);
		
		lbl = new JLabel(dOut.getFeatureAggregationPolicy());
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		fPanel.add(lbl);
		
		containerPanel.add(fPanel);

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

		JScrollPane scroll = new JScrollPane(table);
		containerPanel.add(scroll);

		return containerPanel;
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
			return columns.size() + 1;
		}

		public int getRowCount() {
			return rows.size();
		}

		public String getColumnName(int col) {
			if (col == 0)
				return "Feature";
			else
				return columns.get(col - 1).toString();
		}

		public Object getValueAt(int row, int col) {
			if (col == 0) {
				return rows.get(row).getName();
			} else {
				Double val = fScores.get(rows.get(row)).get(
						columns.get(col - 1));
				if (val != null) {
					return Double.valueOf(AppUtility.formatDouble(val, 3));
				} else
					return "";
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
