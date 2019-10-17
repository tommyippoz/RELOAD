/**
 * 
 */
package ippoz.reload.algorithm.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.lmu.ifi.dbs.elki.data.model.KMeansModel;
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector;

/**
 * @author Tommy
 *
 */
public class GenericCluster {
	
	private double[] avg;
	
	private Double var;
	
	private Double distanceFromCenter;
	
	private List<double[]> points;
	
	private boolean isLarge;
	
	public String toConfiguration() {
		return Arrays.toString(avg) + ";" + var + ";" + distanceFromCenter + ";" + getSize() + ";" + isLarge;
	}

	public GenericCluster(List<ClusterableSnapshot> cSnaps) {
		avg = null;
		points = new LinkedList<double[]>();
		for(ClusterableSnapshot cs : cSnaps){
			points.add(cs.getPoint());
		}
		calculateAvg();
		calculateVar();
		calculateDistanceFromCenter();
	}

	public GenericCluster(String clString) {
		String[] splitted;
		if(clString != null){
			splitted = clString.split(";");
			var = Double.valueOf(splitted[1]);
			distanceFromCenter = Double.valueOf(splitted[2]);
			int size = Integer.parseInt(splitted[3]);
			points = new ArrayList<double[]>(size);
			for(int i=0;i<size;i++){
				points.add(null);
			}
			isLarge = Boolean.valueOf(splitted[4]);
			clString = splitted[0].replace("[", "").replace("]", "");
			splitted = clString.split(",");
			avg = new double[splitted.length];
			for(int i=0;i<avg.length;i++){
				avg[i] = Double.valueOf(splitted[i]);
			}
			
		}
	}

	public GenericCluster(double[] avg, double var, double dist, int size, boolean isLarge) {
		this.avg = avg;
		this.var = var;
		this.distanceFromCenter = dist;
		this.isLarge = isLarge;
		points = new ArrayList<double[]>(size);
		for(int i=0;i<size;i++){
			points.add(null);
		}
	}

	public GenericCluster(double[] avg, double var, List<double[]> points) {
		this.avg = avg;
		this.var = var;
		this.points = points;
		calculateDistanceFromCenter();
	}

	public double distanceFrom(double[] point){
		return euclideanDistance(avg, point);
	}

	private void calculateVar() {
		double val = 0;
		if(points != null && points.size() > 0){
			if(avg != null){
				for(double[] point : points){
					if(point != null){
						val = val + Math.pow(euclideanDistance(avg, point), 2);
					}
				}
			}
		}
		var = val;
	}
	
	private void calculateDistanceFromCenter() {
		double val = 0;
		if(points != null && points.size() > 0){
			if(avg != null){
				for(double[] point : points){
					if(point != null){
						val = val + euclideanDistance(avg, point);
					}
				}
				val = val / points.size();
			}
		}
		distanceFromCenter = val;
	}
	
	private double euclideanDistance(double[] d1, double[] d2){
		double res = 0;
		if(d1 == null || d2 == null)
			return Double.MAX_VALUE;
		if(d1.length == d2.length){
			for(int i=0;i<d1.length;i++){
				res = res + Math.pow(d1[i] - d2[i], 2);
			}
		}
		return Math.sqrt(res);
	}

	private void calculateAvg() {
		int count = 0;
		if(points != null && points.size() > 0 && points.get(0) != null){
			avg = new double[points.get(0).length];
			for(double[] point : points){
				if(point != null){
					for(int i=0;i<avg.length;i++){
						avg[i] = avg[i] + point[i];
					}
					count++;
				}
			}
			for(int i=0;i<avg.length;i++){
				avg[i] = avg[i]/count;
			} 
		}
		
	}
	
	public int getSize(){
		if(points != null)
			return points.size();
		else return 0;
	}
	
	public double getVar(){
		return var;
	}

	public void setLarge(boolean b) {
		isLarge = b;
	}

	public boolean isLarge() {
		return isLarge;
	}

	public Double getAvgDistanceFromCenter() {
		if(distanceFromCenter == null)
			calculateDistanceFromCenter();
		return distanceFromCenter;
	}
	
	public KMeansModel generateKMeansModel(){
		return new KMeansModel(new Vector(avg), var);
	}

	@Override
	public String toString() {
		return "GenericCluster [avg=" + Arrays.toString(avg) + ", var=" + var
				+ ", distanceFromCenter=" + distanceFromCenter + ", points="
				+ points.size() + ", isLarge=" + isLarge + "]";
	}

	
}
