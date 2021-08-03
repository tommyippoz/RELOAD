/**
 * 
 */
package ippoz.reload.ui;

import ippoz.reload.algorithm.configuration.BasicConfiguration;
import ippoz.reload.evaluation.AlgorithmModel;
import ippoz.reload.output.DetectorOutput;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.text.DecimalFormat;
import java.util.List;

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
public class TrainingDetailFrame {

	private JFrame tdFrame;

	private Font labelFont;

	private JPanel trainPanel;

	private DetectorOutput dOut;
	
	private List<AlgorithmModel> voterList;

	public TrainingDetailFrame(DetectorOutput dOut) {
		this.dOut = dOut;
		voterList = dOut.getTrainingModels();

		buildFrame();

		double rate = 18 * Toolkit.getDefaultToolkit().getScreenSize()
				.getHeight() / 1080;

		labelFont = new Font("Times", Font.PLAIN, (int) ((16 + rate) / 2));

		trainPanel = buildMainPanel();
	}

	public void setVisible(boolean b) {
		if (tdFrame != null) {
			tdFrame.add(trainPanel);
			tdFrame.setLocationRelativeTo(null);
			tdFrame.setVisible(b);
		}
	}

	private void buildFrame() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		tdFrame = new JFrame();
		tdFrame.setTitle("Train Detail for '" + dOut.getDataset() + "'");
		if (screenSize.getWidth() > 1000)
			tdFrame.setBounds(0, 0, (int) (screenSize.getWidth() * 0.7),
					(int) (screenSize.getHeight() * 0.5));
		else
			tdFrame.setBounds(0, 0, 600, 450);
		tdFrame.setBackground(Color.WHITE);
		tdFrame.setResizable(false);
	}

	private JPanel buildMainPanel() {
		JPanel containerPanel = new JPanel();
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
		containerPanel.setAlignmentX(SwingConstants.CENTER);

		// BODY

		JPanel outPanel = new JPanel();
		outPanel.setBackground(Color.WHITE);
		outPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
		
		JPanel fPanel = new JPanel();
		fPanel.setBackground(Color.WHITE);
		TitledBorder tb = new TitledBorder(new LineBorder(Color.DARK_GRAY, 2),
				" Training Detail ", TitledBorder.CENTER, TitledBorder.CENTER,
				new Font("Times", Font.BOLD, 16), Color.DARK_GRAY);
		fPanel.setBorder(tb);
		fPanel.setLayout(new GridLayout(4, 1));
		((GridLayout)fPanel.getLayout()).setVgap(10);

		JLabel lbl = new JLabel("Used Features: " + String.valueOf(dOut.getSelectedSeries().size()));
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		lbl.setBorder(new EmptyBorder(0, tdFrame.getWidth()/10, 0, tdFrame.getWidth()/10));
		fPanel.add(lbl);
		
		lbl = new JLabel("Algorithm(s): " + dOut.getAlgorithm());
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		fPanel.add(lbl);
		
		lbl = new JLabel("K-Fold: " + dOut.getKFold());
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		fPanel.add(lbl);
		
		lbl = new JLabel("Metric: " + dOut.getReferenceMetric().getName());
		lbl.setFont(labelFont);
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
		fPanel.add(lbl);
		
		outPanel.add(fPanel);
		
		containerPanel.add(outPanel);

		// TABLE

		JTable table = new JTable(new MyTableModel());
		table.setFillsViewportHeight(true);
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment(JLabel.CENTER);
		for (int x = 0; x < table.getColumnCount(); x++) {	
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
		
		private final DecimalFormat df = new DecimalFormat("#0.000");

		private String[] columnNames = {"Data Series", "Algorithm", "Metric Score", "Hyper-Parameters", "Decision Function"};
		
		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return voterList.size();
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			AlgorithmModel av = voterList.get(row);
			switch(col){
				case 0:
					return av.getDataSeries().getSanitizedName();
				case 1:
					return String.valueOf(av.getAlgorithmType());
				case 2:
					return df.format(av.getMetricScore().getDoubleValue());
				case 3:
					return av.getMainConfString();
				case 4:
					return av.getAlgorithmConfiguration().getItem(BasicConfiguration.THRESHOLD);
			}
			return null;
		}

		public Class<?> getColumnClass(int c) {
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object aValue, int row, int col) {

		}

	}

}
