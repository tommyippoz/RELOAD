package ippoz.multilayer.detector.commons.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
public class Point {

	private double x = 0;
	private double y = 0;
	private int cluster_number = 0;

	public Point(double x, double y) {
		this.setX(x);
		this.setY(y);
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getX() {
		return this.x;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getY() {
		return this.y;
	}

	public void setCluster(int n) {
		this.cluster_number = n;
	}

	public int getCluster() {
		return this.cluster_number;
	}

	/**
	 * Calculates the Euclidean Distance between two given points.
	 *
	 * @param p1
	 *            The first point.
	 * @param p2
	 *            The second point.
	 * @return The Euclidean Distance between first and second points.
	 */
	public static double distance(Point p1, Point p2) {
		return Math.sqrt(Math.pow((p2.getY() - p1.getY()), 2) + Math.pow((p2.getX() - p1.getX()), 2));
	}

	/**
	 * Creates a random point in the interval of two given numbers.
	 *
	 * @param min
	 *            Min x and y.
	 * @param max
	 *            Max x and y.
	 * @return A random point in the given interval.
	 */
	static Point createRandomPoint(double minX, double maxX, double minY, double maxY) {
		Random r = new Random();
		// System.out.println(minX + " " + maxX);
		double x = 0.0;
		double y = 0.0;
		
		if(Double.isNaN(minY) || Double.isNaN(maxY)){
			return null;
		}

		try {
			x = ThreadLocalRandom.current().nextDouble(minX, maxX + 1);
			y = ThreadLocalRandom.current().nextDouble(minY, maxY + 1);
		} catch (Exception e) {
			//System.out.println(minY);
		}

		return new Point(x, y);

	}

	/**
	 * Point toString.
	 *
	 * @return A String that represents a point.
	 */
	public String toString() {
		return x + ", " + y;
	}
	
	public double calculateLinearDistancia(double y1, double y2){
		if(y1 > y2){
			return y1-y2;
		} else {
			return y2-y1;
		}
		
	}

}
