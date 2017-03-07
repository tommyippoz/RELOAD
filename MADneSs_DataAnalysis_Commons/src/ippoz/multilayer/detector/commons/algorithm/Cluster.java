package ippoz.multilayer.detector.commons.algorithm;

import java.util.ArrayList;
import java.util.List;

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
public class Cluster {

	private List<Point> points;
	private Point centroid;
	private Double threshold;
	private int id;

	// Creates a new Cluster
	public Cluster(int id) {
		this.id = id;
		this.points = new ArrayList<Point>();
		this.centroid = null;
		this.threshold = 0.0;
	}

	public List<Point> getPoints() {
		return points;
	}

	public void addPoint(Point point) {
		points.add(point);
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}

	public Point getCentroid() {
		return centroid;
	}

	public void setCentroid(Point centroid) {
		this.centroid = centroid;
	}

	public Double getThreshold() {
		return threshold;
	}

	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	public int getId() {
		return id;
	}

	/**
	 * Remove all points from a cluster.
	 */
	public void clear() {
		points.clear();
	}

	/**
	 * Print the data of a Cluster.
	 */
	public void plotCluster() {
		
		if (points.size() > 1) {
			System.out.println("\tCluster: " + id);
			System.out.println("\tCentroid: " + centroid);
			System.out.println("\tThreshold: " + threshold);
			System.out.println("\tPoints:");

			for (Point p : points) {
				System.out.println("\t\t" + p + "\n");
				// s += p.getX() + "," + p.getY() + "\n";
			}
		}

	}

	String collectClusterData() {
		String s = "";

		for (Point p : points) {
			s += p.getX() + "," + p.getY() + "\n";
		}

		return s;

	}

}
