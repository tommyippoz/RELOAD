/**
 * 
 */
package ippoz.multilayer.detector.graphics;

import ippoz.multilayer.detector.voter.ExperimentVoter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.TextAnchor;

/**
 * The Class HistogramChartDrawer.
 *
 * @author Tommy
 */
public class HistogramChartDrawer extends ChartDrawer {
	
	/** The anomaly threshold. */
	private double anomalyTreshold;
	
	/** The algorithm convergence time. */
	private double algConvTime;

	/**
	 * Instantiates a new histogram chart drawer.
	 *
	 * @param chartTitle the chart title
	 * @param xLabel the x-axis label
	 * @param yLabel the y-axis label
	 * @param data the series data
	 * @param anomalyTreshold the anomaly threshold
	 * @param algConvTime the algorithm convergence time
	 */
	public HistogramChartDrawer(String chartTitle, String xLabel, String yLabel, HashMap<String, TreeMap<Double, Double>> data, double anomalyTreshold, double algConvTime) {
		super(chartTitle, xLabel, yLabel, data);
		this.anomalyTreshold = anomalyTreshold;
		this.algConvTime = algConvTime;
		addAnomalyTreshold();
		addConvergenceTreshold();
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.graphics.ChartDrawer#setupChart(java.util.HashMap)
	 */
	@Override
	protected void setupChart(HashMap<String, TreeMap<Double, Double>> data) {
		XYBarRenderer renderer = new CustomRenderer(new Paint[] {Color.BLUE, Color.RED, Color.YELLOW}, data.get(ExperimentVoter.FAILURE_LABEL));
        renderer.setBaseItemLabelGenerator(new StandardXYItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        ((XYPlot) chart.getPlot()).setRenderer(renderer);
	}
	
	/**
	 * Adds the anomaly threshold.
	 */
	private void addAnomalyTreshold(){
		ValueMarker rangeMarker = new ValueMarker(anomalyTreshold);
		rangeMarker.setPaint(Color.black);
		rangeMarker.setLabel("Anomaly Treshold"); 
		rangeMarker.setLabelFont(rangeMarker.getLabelFont().deriveFont(rangeMarker.getLabelFont().getStyle(), 20));
		rangeMarker.setStroke(new BasicStroke(2.0f));        
        rangeMarker.setLabelOffset(new RectangleInsets(10,10,10,50));
        rangeMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
        rangeMarker.setLabelTextAnchor(TextAnchor.BOTTOM_LEFT);
		((XYPlot)chart.getPlot()).addRangeMarker(rangeMarker);
	}
	
	/**
	 * Adds the convergence threshold.
	 */
	private void addConvergenceTreshold(){
		ValueMarker domainMarker = new ValueMarker(algConvTime);
		domainMarker.setPaint(Color.black);
		domainMarker.setLabel("Convergence Time"); 
		domainMarker.setLabelFont(domainMarker.getLabelFont().deriveFont(domainMarker.getLabelFont().getStyle(), 20));
		domainMarker.setStroke(new BasicStroke(2.0f));        
        domainMarker.setLabelOffset(new RectangleInsets(10,10,10,100));
        domainMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        domainMarker.setLabelTextAnchor(TextAnchor.HALF_ASCENT_CENTER);
		((XYPlot)chart.getPlot()).addDomainMarker(domainMarker);
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.graphics.ChartDrawer#createChart(java.lang.String, java.lang.String, java.lang.String, org.jfree.data.general.Dataset, boolean, boolean)
	 */
	@Override
	protected JFreeChart createChart(String chartTitle, String xLabel, String yLabel, Dataset dataset, boolean showLegend, boolean createTooltip) {
		return ChartFactory.createXYBarChart(chartTitle, xLabel, false, yLabel, (IntervalXYDataset) dataset, PlotOrientation.VERTICAL, showLegend, createTooltip, false);	
	}

	/* (non-Javadoc)
	 * @see ippoz.multilayer.detector.graphics.ChartDrawer#createDataset(java.util.HashMap)
	 */
	@Override
	protected Dataset createDataset(HashMap<String, TreeMap<Double, Double>> data) {
		XYSeries current;
		XYSeriesCollection dataset = new XYSeriesCollection();
		for(String seriesName : data.keySet()){
			if(seriesName.equals(ExperimentVoter.ANOMALY_SCORE_LABEL)){
				current = new XYSeries(seriesName);
				for(Double key : data.get(seriesName).keySet()){
					current.add(key, data.get(seriesName).get(key));
				}
				dataset.addSeries(current);
			}
		}
		return dataset;
	}
	
	/**
	 * The Class CustomRenderer.
	 * This is used to paint columns of the bar chart depending on custom options.
	 */
	@SuppressWarnings("serial")
	private class CustomRenderer extends XYBarRenderer {

		/** The failure map. */
		private TreeMap<Double, Double> failureMap;
        
        /** The colours. */
        private Paint[] colors;

        /**
         * Instantiates a new custom renderer.
         *
         * @param colors the colours
         * @param failureMap the failure map
         */
        public CustomRenderer(final Paint[] colors, TreeMap<Double, Double> failureMap) {
            this.colors = colors;
            this.failureMap = failureMap;
        }

        /* (non-Javadoc)
         * @see org.jfree.chart.renderer.AbstractRenderer#getItemPaint(int, int)
         */
        public Paint getItemPaint(final int row, final int column) {
        	double result = isInMap(column);
        	if(Double.isNaN(result))
        		return colors[0];
        	if(result > 0)
        		return colors[1];
        	else return colors[2];
        }

		/**
		 * Checks if this column indicates an anomaly.
		 *
		 * @param column the column
		 * @return true, if is in map
		 */
		private double isInMap(int column) {
			if(failureMap != null){
				for(Double val : failureMap.keySet()){
					if(val.intValue() == column)
						return failureMap.get(val);
				}
			}
			return Double.NaN;
		}
    }

}
