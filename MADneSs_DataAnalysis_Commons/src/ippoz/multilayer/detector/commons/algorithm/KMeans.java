package ippoz.multilayer.detector.commons.algorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Copyright 2016 Filipe Falcão Batista dos Santos
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
public class KMeans {

	// Number of Clusters. This metric should be related to the number of points
	private int NUM_CLUSTERS = 5;
	// Number of Points
	// Min and Max X and Y
	private static double MIN_COORDINATE_Y = 0;
	private static double MAX_COORDINATE_Y = 10;
	private static double MIN_COORDINATE_X = 0;
	private static double MAX_COORDINATE_X = 10;

	private double goodThreshould;

	private List<Point> points;
	private List<Cluster> clusters;
	private List<KMeansData> data;

	private int iteration;

	private String txt;
	private String indicatorName;
	private int count;
	
	private String readPath;
	private String savePath;
	private boolean training;
	
	public KMeans() {
		this.points = new ArrayList<Point>();
		this.clusters = new ArrayList<Cluster>();
		this.data = new ArrayList<KMeansData>();
		txt = "";
		goodThreshould = 1.1;
		MIN_COORDINATE_X = 0;
		
		/*
		 * Added for Call Python
		 */
		if (training) {
			readPath = "C:\\Users\\Caio\\Desktop\\Ezio Auditore\\UFAL_MultiLayer_AnomalyDetector\\K-mean Clustering\\K-mean Clustering\\trainData.csv";
			savePath = "C:\\Users\\Caio\\Desktop\\Ezio Auditore\\UFAL_MultiLayer_AnomalyDetector\\K-mean Clustering\\K-mean Clustering\\trainOutput.txt";

		} else {
			readPath = "C:\\Users\\Caio\\Desktop\\Ezio Auditore\\UFAL_MultiLayer_AnomalyDetector\\K-mean Clustering\\K-mean Clustering\\evalData.csv";
			savePath = "C:\\Users\\Caio\\Desktop\\Ezio Auditore\\UFAL_MultiLayer_AnomalyDetector\\K-mean Clustering\\K-mean Clustering\\evalOutput.txt";
		}

	}

	/**
	 * Initialize the process.
	 */
	public void init() {
		// Create Points
		if (data.size() > 1) {
			MIN_COORDINATE_Y = Double.valueOf(data.get(0).getValue());
			if (Double.isNaN(MIN_COORDINATE_Y)) {
				MIN_COORDINATE_Y = 0.0;
			}
			indicatorName = data.get(0).getIndicatorName();
		}

		/*
		 * Se for manter como dataseries, deixar descomentado
		 */
		MAX_COORDINATE_X = data.size();

		for (int i = 0; i < data.size(); i++) {
			/*
			 * Se for data series
			 */

			Point p = null;

			if (!Double.isNaN(Double.valueOf(data.get(i).getValue()))) {
				p = new Point((double) i + 10, Double.valueOf(data.get(i).getValue()));
			} else {
				p = new Point((double) i + 10, MAX_COORDINATE_Y);
			}
			// txt += (i + 10) + "," + Double.valueOf(data.get(i).getValue()) +
			// "\n";

			/*
			 * Se for plot YxY
			 */
			// Point p = new Point(Double.valueOf(data.get(i).getValue()),
			// txt += Double.valueOf(data.get(i).getValue()) + "," +
			// Double.valueOf(data.get(i).getValue()) + "\n";
			if (Double.valueOf(data.get(i).getValue()) < MIN_COORDINATE_Y) {
				// System.out.println("min before:" + MIN_COORDINATE_Y);
				MIN_COORDINATE_Y = Double.valueOf(data.get(i).getValue());
				// System.out.println("min after:" + MIN_COORDINATE_Y);
			}

			if (Double.valueOf(data.get(i).getValue()) > MAX_COORDINATE_Y) {
				MAX_COORDINATE_Y = Double.valueOf(data.get(i).getValue());
			}

			points.add(p);

		}

		// Create Clusters
		// Set Random Centroids
		int count = 0;
		for (int i = 0; i < NUM_CLUSTERS; i++) {
			if (!Double.isNaN(MIN_COORDINATE_X) && !Double.isNaN(MAX_COORDINATE_X)) {
				if (!Double.isNaN(MIN_COORDINATE_Y) && !Double.isNaN(MAX_COORDINATE_Y)) {
					Point centroid = Point.createRandomPoint(MIN_COORDINATE_X, MAX_COORDINATE_X, MIN_COORDINATE_Y,
							MAX_COORDINATE_Y);
					Cluster c = new Cluster(count);
					c.setCentroid(centroid);
					clusters.add(c);
					count++;
				} else {
					// System.out.println(MIN_COORDINATE_Y);
					// System.out.println(MAX_COORDINATE_Y);
				}

			} else {
				// System.out.println(MIN_COORDINATE_X);
				// System.out.println(MAX_COORDINATE_X);
			}
		}

		NUM_CLUSTERS = count;

		// Print Initial state
		// System.out.println("Initial state:");
		// plotClusters();
	}

	/**
	 * Print all the clusters.
	 */
	public void plotClusters() {
		for (Cluster c : clusters) {
			c.plotCluster();
			/*
			 * Coletando dados para salvar em arquivo;
			 */
			// txt += c.collectClusterData();

			// System.out.println("");
		}
	}

	/**
	 * Calculate the K Means with iterating method.
	 */
	public void calculate() {
		boolean finish = false;
		iteration = 0;

		// Add in new data, one at a time, recalculating centroids with each new
		// one.
		while (!finish) {
			// Clear cluster state
			clearClusters();

			List<Point> lastCentroids = getCentroids();

			// Assign points to the closer cluster
			assignCluster();

			// Calculate new centroids.
			calculateCentroids();

			List<Point> currentCentroids = getCentroids();

			// Calculates total distance between new and old Centroids
			double distance = 0;

			for (int i = 0; i < lastCentroids.size(); i++) {
				distance += Point.distance(lastCentroids.get(i), currentCentroids.get(i));
			}

			// System.out.println("");
			// System.out.println("Iteration: " + iteration);
			// txt += "Iteration: " + iteration + "\n";
			// System.out.println("\tCentroid distances: " + distance);
			// plotClusters();

			if (distance == 0) {
				finish = true;
			}
		}

	}

	/**
	 * Clear all the clusters.
	 */
	private void clearClusters() {
		for (Cluster cluster : clusters) {
			cluster.clear();
		}
	}

	/**
	 * Get centroids from all clusters.
	 *
	 * @return Centroids
	 */
	private List<Point> getCentroids() {
		List<Point> centroids = new ArrayList<Point>(NUM_CLUSTERS);

		for (Cluster cluster : clusters) {
			Point aux = cluster.getCentroid();
			Point point = new Point(aux.getX(), aux.getY());
			centroids.add(point);
		}

		return centroids;
	}

	/**
	 * Assign a point to the closest cluster.
	 */
	private void assignCluster() {
		double max = Double.MAX_VALUE;
		double min;
		int cluster = 0;
		double distance;
		Cluster aux = null;

		for (Point point : points) {
			min = max;

			for (int i = 0; i < NUM_CLUSTERS; i++) {
				Cluster c = clusters.get(i);
				distance = Point.distance(point, c.getCentroid());

				if (distance < min) {
					min = distance;
					cluster = i;
				}
			}

			point.setCluster(cluster);
			clusters.get(cluster).addPoint(point);

		}
	}

	/**
	 * Calculate the centroids.
	 */
	private void calculateCentroids() {
		double sumX = 0;
		double sumY = 0;

		for (Cluster cluster : clusters) {
			List<Point> list = cluster.getPoints();
			int n_points = list.size();

			for (Point point : list) {
				sumY += point.getY();
				sumX += point.getX();
			}

			Point centroid = cluster.getCentroid();

			if (n_points > 0) {
				double newY = sumY / n_points;
				centroid.setY(newY);
				double newX = sumX / n_points;
				centroid.setX(newX);
			}
		}
	}

	private void removeEmptyClusters() {
		if (iteration > 1) {

			ArrayList<Cluster> aux = new ArrayList<Cluster>();

			for (Cluster c : clusters) {

				if (c.getPoints().size() < 1) {
					points.remove(c.getCentroid());
					aux.add(c);
					NUM_CLUSTERS--;
				}

			}

			for (Cluster c : aux) {
				clusters.remove(c);
			}

		}

	}

	public void readData() {

		BufferedReader br = null;

		try {

			String sCurrentLine;

			// br = new BufferedReader(new FileReader(readPath));

			while ((sCurrentLine = br.readLine()) != null) {
				if (sCurrentLine.contains(indicatorName)) {
					String[] line = sCurrentLine.split(",");

					SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
					Date dt = null;

					dt = f.parse(line[3]);
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	public String saveData() {
		BufferedWriter writer = null;

		String txt = "";
		count = 0;
		// System.out.println(clusters.size());
		for (Cluster c : clusters) {
			if (c.getPoints().size() > 1) {
				txt += "centroid," + c.getCentroid().getX() + "," + c.getCentroid().getY() + ",";
				txt += c.getThreshold() + ",";

				count++;
			}
		}
		//System.out.println(indicatorName);
		if (indicatorName != null) {
			if (indicatorName.contains("Active Files")) {
				//plotClusters();
			}
		}

		if (count > 1) {

			if (indicatorName.equals("ProcessCpuTime222")) {
				System.out.println(indicatorName);

				// System.out.println("Result : " + txt);
				for (Cluster c : clusters) {

					if (c.getPoints().size() > 1) {
						System.out.println("Centroid: " + c.getCentroid());
						for (Point p : c.getPoints()) {
							if (Double.isNaN(p.getX()) || Double.isNaN(p.getY())) {
								c.getPoints().remove(p);
								continue;
							} else {
								System.out.println(p.getX() + "," + p.getY());
							}

						}
						// System.out.println(c.getPoints());
					}
				}
			}
		}
		return txt;
	}

	public int getCount() {
		return count;
	}

	public void setData(List<KMeansData> data) {
		this.data = data;
	}

	public void setThreshould() {

		for (Cluster c : clusters) {
			double maxDistance = 0;
			Point centroid = c.getCentroid();

			for (Point p : c.getPoints()) {
				if (p.calculateLinearDistancia(p.getY(), centroid.getY()) > maxDistance) {
					maxDistance = p.calculateLinearDistancia(p.getY(), centroid.getY());
				}
			}

			c.setThreshold(maxDistance * goodThreshould);
		}

	}

	public void saveDataTxt() {
		BufferedWriter writer = null;

		String txt = "";

		for (Cluster c : clusters) {
			if (c.getPoints().size() > 1) {
				txt += "Cluster," + c.getId() + "\n";
				txt += "centroid," + c.getCentroid().getX() + "," + c.getCentroid().getY() + "\n";
			}
		}

		try {
			writer = new BufferedWriter(new FileWriter(savePath));
			writer.write(txt);

		} catch (IOException e) {
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
			}
		}
	}
}
