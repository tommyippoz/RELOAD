package ippoz.multilayer.detector.algorithm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import ippoz.multilayer.detector.commons.algorithm.AlgorithmType;
import ippoz.multilayer.detector.commons.algorithm.KMeans;
import ippoz.multilayer.detector.commons.algorithm.KMeansData;
import ippoz.multilayer.detector.commons.algorithm.Point;
import ippoz.multilayer.detector.commons.configuration.AlgorithmConfiguration;
import ippoz.multilayer.detector.commons.data.DataSeriesSnapshot;
import ippoz.multilayer.detector.commons.data.Snapshot;
import ippoz.multilayer.detector.commons.dataseries.DataSeries;
import ippoz.multilayer.detector.commons.service.ServiceCall;
import ippoz.multilayer.detector.commons.support.PreferencesManager;

public class KMeansClusteringChecker extends DataSeriesDetectionAlgorithm implements AutomaticTrainingAlgorithm {

	int i;

	public KMeansClusteringChecker(DataSeries dataSeries, AlgorithmConfiguration conf) {
		super(dataSeries, conf);
		i = 10;
	}

	@Override
	protected double evaluateDataSeriesSnapshot(DataSeriesSnapshot sysSnapshot) {

		if (Double.isNaN(sysSnapshot.getSnapValue())) {
			return 0;
		}

		//HashMap<String, String> clusterMap = null;
		String s = "";
		
		//System.out.println(conf.getRawItem(sysSnapshot.getDataSeries().getName()));

		/*if (conf.getRawItem(sysSnapshot.getDataSeries().getName()) instanceof HashMap) {
			//clusterMap = (HashMap<String, String>) conf.getRawItem(sysSnapshot.getDataSeries().getName());
			if (sysSnapshot.getDataSeries().getName().contains("SystemCpuLoad#DIFFERENCE#JVM)/(ProcessCpuLoad#DIFFERENCE#JVM")) {
				 System.out.println(conf);
			}
			
		} else {
			s = (String) conf.getRawItem(sysSnapshot.getDataSeries().getName());
			if (sysSnapshot.getDataSeries().getName()
					.contains("SystemCpuLoad#DIFFERENCE#JVM)/(ProcessCpuLoad#DIFFERENCE#JVM")) {
				 System.out.println(conf);
			}
			return 0;
		}*/
		String trainingClusters = "";

		for (ServiceCall sc : sysSnapshot.getServiceCalls()) {
			trainingClusters = conf.getItem(sc.getServiceName());

			if (trainingClusters == null || trainingClusters.equals("")) {
				return 0;
			}

			String[] tClusters = trainingClusters.split(",");

			int count = 0;

			Point p = null;
			double value = sysSnapshot.getSnapValue();

			for (int i = 0; i < tClusters.length; i++) {

				if (tClusters[i].contains("centroid")) {
					i++;
					double axisX = Double.parseDouble(tClusters[i]);
					i++;
					double axisY = Double.parseDouble(tClusters[i]);
					p = new Point(axisX, axisY);
				} else {

					if (tClusters[i] == null || tClusters[i].equals("")) {
						continue;
					}

					double trainingThreshould = Double.parseDouble(tClusters[i]);
					Point p1 = new Point(p.getX(), value);
					double distance = p1.calculateLinearDistancia(p.getY(), p1.getY());

					if (Double.compare(distance, trainingThreshould) > 0) {
						// System.out.println("P: " + p.getX() + "," +
						// p.getY());
						// System.out.println("P1: " + p1.getX() + "," +
						// p1.getY());
						// System.out.println("Distance: " + distance);
						// System.out.println("Threshold: "
						// +trainingThreshould);
						count++;
					}
				}

			}

			if (count == (tClusters.length / 4)) {
				// System.out.println("1");
				return 1;
			}

		}
		// System.out.println("0");
		return 0;
	}

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub

	}

	@Override
	public AlgorithmConfiguration automaticTraining(HashMap<String, LinkedList<Snapshot>> algExpSnapshots) {

		AlgorithmConfiguration ac = new AlgorithmConfiguration(AlgorithmType.KMEANS);
		List<KMeansData> kmeansData = new ArrayList<KMeansData>();
		String indName = null;
		HashMap<String, List<KMeansData>> kdMap = new HashMap<String, List<KMeansData>>();
		int i = 0;
		for (LinkedList<Snapshot> expSnapList : algExpSnapshots.values()) {

			for (Snapshot snap : expSnapList) {
				DataSeriesSnapshot dss = (DataSeriesSnapshot) snap;
				indName = dss.getDataSeries().getName();
				for (ServiceCall sc : snap.getServiceCalls()) {
					KMeansData kmd = new KMeansData(sc.getServiceName(), dss.getDataSeries().getName(),
							snap.getTimestamp().toString(), sc.getServiceName(), dss.getSnapValue().toString(),
							dss.getDataSeries().getLayerType().toString());

					if (kdMap.get(sc.getServiceName()) == null) {
						ArrayList<KMeansData> kdList = new ArrayList<KMeansData>();
						if (!Double.isNaN(Double.parseDouble(kmd.getValue()))) {
							if (!kmd.getValue().equals("NaN")) {
								kdList.add(kmd);
								i++;
								kdMap.put(sc.getServiceName(), kdList);
							}
						}
					} else {
						if (!Double.isNaN(Double.parseDouble(kmd.getValue()))) {
							if (!kmd.getValue().equals("NaN")) {
								kdMap.get(sc.getServiceName()).add(kmd);
							}
						}
					}
				}
			}
		}

		//HashMap<String, String> clusterMap = new HashMap<String, String>();

		String output = "";

		for (String s : kdMap.keySet()) {
			kmeansData = kdMap.get(s);

			//for (KMeansData km : kmeansData) {
				//output += km.toString() + "\n";
			//}

			KMeans kmeans = new KMeans();
			kmeans.setData(kmeansData);
			kmeans.init();
			kmeans.calculate();
			kmeans.setThreshould();

			String td = kmeans.saveData();
			if (td.contains("centroid")) {
				ac.addItem(s, td);
				// System.out.println("Result: " + td);
			}

		}

		return ac;
	}

	private void writeTxt(String out) {

		PreferencesManager pref = new PreferencesManager("detector.preferences");
		String outputFolder = pref.getPreference("OUTPUT_FOLDER");

		BufferedWriter writer = null;

		Random rd = new Random();

		try {

			writer = new BufferedWriter(new FileWriter(outputFolder + "KMEANS" + ".txt", true));
			writer.write(out);

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
