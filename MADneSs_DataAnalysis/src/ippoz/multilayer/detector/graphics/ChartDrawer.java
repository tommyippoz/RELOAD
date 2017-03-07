/**
 * 
 */
package ippoz.multilayer.detector.graphics;

import ippoz.multilayer.detector.commons.support.AppLogger;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

/**
 * The Class ChartDrawer.
 * The basic chart drawer. Uses the JFreeChart external library.
 *
 * @author Tommy
 */
public abstract class ChartDrawer {
	
	/** The basic chart. */
	protected JFreeChart chart;
	
	/**
	 * Instantiates a new chart drawer.
	 *
	 * @param chartTitle the chart title
	 * @param xLabel the x-axis label
	 * @param yLabel the y-axis label
	 * @param data the series data
	 */
	public ChartDrawer(String chartTitle, String xLabel, String yLabel, HashMap<String, TreeMap<Double, Double>> data){
		chart = createChart(chartTitle, xLabel, yLabel, createDataset(data), true, true);
		setupChart(data);
	}

	/**
	 * Setups chart.
	 *
	 * @param data the series data
	 */
	protected abstract void setupChart(HashMap<String, TreeMap<Double, Double>> data);

	/**
	 * Creates the chart.
	 *
	 * @param chartTitle the chart title
	 * @param xLabel the x-axis label
	 * @param yLabel the y-axis label
	 * @param dataset the JFreeChart dataset
	 * @param showLegend flag used to decide whether to show the legend
	 * @param createTooltip the create tooltip flag
	 * @return the chart
	 */
	protected abstract JFreeChart createChart(String chartTitle, String xLabel, String yLabel, Dataset dataset, boolean showLegend, boolean createTooltip);
	
	/**
	 * Creates the dataset.
	 *
	 * @param data the series data
	 * @return the dataset
	 */
	protected abstract Dataset createDataset(HashMap<String, TreeMap<Double, Double>> data);
	
	/**
	 * Saves chart to file.
	 *
	 * @param filename the filename
	 * @param width the chart width
	 * @param height the chart height
	 */
	public void saveToFile(String filename, int width, int height){
		File file = new File(filename);
		File parentFolder = new File(file.getParent());
		try {
			if(!parentFolder.exists())
				parentFolder.mkdirs();
			ChartUtilities.saveChartAsPNG(new File(filename), chart, width, height);
		} catch (IOException ex) {
			AppLogger.logException(getClass(), ex, "Unable to save chart to file");
		}
	}
}
