/**
 * 
 */
package ippoz.reload.algorithm;

import ippoz.reload.commons.configuration.AlgorithmConfiguration;
import ippoz.reload.commons.dataseries.DataSeries;
import ippoz.reload.commons.dataseries.MultipleDataSeries;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.snapshot.DataSeriesSnapshot;
import ippoz.reload.commons.knowledge.snapshot.MultipleSnapshot;
import ippoz.reload.commons.knowledge.snapshot.Snapshot;
import ippoz.utils.logging.AppLogger;

import java.util.List;

/**
 * @author Tommy
 *
 */
public abstract class DataSeriesExternalAlgorithm extends DataSeriesDetectionAlgorithm {

	private static final String MINMAX = "MINMAX";
	
	protected boolean needNormalization;
	
	protected double[][] minmax;
	
	public DataSeriesExternalAlgorithm(DataSeries dataSeries, AlgorithmConfiguration conf, boolean needNormalization) {
		super(dataSeries, conf);
		this.needNormalization = needNormalization;
		if(conf.hasItem(MINMAX))
			loadMinMax(conf.getItem(MINMAX));
	}

	protected void loadMinMax(String item) {
		int i = 0;
		if(item != null && item.trim().length() > 0){
			if(item.contains(";")){
				minmax = new double[item.split(";").length][2];
				for(String splitted : item.split(";")){
					minmax[i][0] = Double.valueOf(splitted.trim().split(",")[0]);
					minmax[i][1] = Double.valueOf(splitted.trim().split(",")[1]);
					i++;
				}
			} else if(item.contains(",")){
				minmax = new double[1][2];
				minmax[0][0] = Double.valueOf(item.trim().split(",")[0]);
				minmax[0][1] = Double.valueOf(item.trim().split(",")[1]);
			} else minmax = null;
		} else minmax = null;
	}

	@Override
	protected void printImageResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void printTextResults(String outFolderName, String expTag) {
		// TODO Auto-generated method stub
		
	}
	
	protected double[][] convertKnowledgeIntoMatrix(List<Knowledge> kList, boolean includeFaulty) {
		return convertSnapshotListIntoMatrix(Knowledge.toSnapList(kList, getDataSeries()), includeFaulty);
	}
	
	protected String[] extractLabels(List<Knowledge> kList, boolean includeFaulty) {
		return extractLabels(includeFaulty, Knowledge.toSnapList(kList, getDataSeries()));
	}
	
	public static String[] extractLabels(boolean includeFaulty, List<Snapshot> kSnapList) {
		int insertIndex = 0;
		String[] anomalyLabels;
		if(includeFaulty)
			anomalyLabels = new String[kSnapList.size()];
		else anomalyLabels = new String[Knowledge.goldenPointsSize(kSnapList)]; 
		if(anomalyLabels.length > 0) {
			for(int i=0;i<kSnapList.size();i++){
				if(includeFaulty || !includeFaulty && kSnapList.get(i).getInjectedElement() == null) {
					anomalyLabels[insertIndex] = kSnapList.get(i).getInjectedElement() == null ? "no" : "yes";
					insertIndex++;
				}
			}
		}
		return anomalyLabels;
	}
	
	protected double[][] convertSnapshotListIntoMatrix(List<Snapshot> kSnapList, boolean includeFaulty) {
		int insertIndex = 0;
		double[][] dataMatrix;
		minmax = new double[getDataSeries().size()][2];
		if(includeFaulty)
			dataMatrix = new double[kSnapList.size()][getDataSeries().size()];
		else dataMatrix = new double[Knowledge.goldenPointsSize(kSnapList)][getDataSeries().size()]; 
		if(dataMatrix.length > 0) {
			for(int i=0;i<kSnapList.size();i++){
				if(includeFaulty || !includeFaulty && kSnapList.get(i).getInjectedElement() == null) {
					if(getDataSeries().size() == 1){
						dataMatrix[insertIndex][0] = ((DataSeriesSnapshot)kSnapList.get(i)).getSnapValue().getFirst();
						if(insertIndex == 0){
							minmax[0][0] = dataMatrix[insertIndex][0];
							minmax[0][1] = dataMatrix[insertIndex][0];
						} else {
							if(dataMatrix[insertIndex][0] < minmax[0][0])
								minmax[0][0] = dataMatrix[insertIndex][0];
							if(dataMatrix[insertIndex][0] > minmax[0][1])
								minmax[0][1] = dataMatrix[insertIndex][0];
						}
					} else {
						for(int j=0;j<getDataSeries().size();j++){
							if(((MultipleSnapshot)kSnapList.get(i)).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue() == null){
								AppLogger.logError(getClass(), "UnrecognizableSnapshot", ((MultipleDataSeries)getDataSeries()).getSeries(j).getName() + " - " + i + " - " + j);
								dataMatrix[insertIndex][j] = 0.0;
							} else dataMatrix[insertIndex][j] = ((MultipleSnapshot)kSnapList.get(i)).getSnapshot(((MultipleDataSeries)getDataSeries()).getSeries(j)).getSnapValue().getFirst();
							if(insertIndex == 0){
								minmax[j][0] = dataMatrix[insertIndex][0];
								minmax[j][1] = dataMatrix[insertIndex][0];
							} else {
								if(dataMatrix[insertIndex][0] < minmax[j][0])
									minmax[j][0] = dataMatrix[insertIndex][0];
								if(dataMatrix[insertIndex][0] > minmax[j][1])
									minmax[j][1] = dataMatrix[insertIndex][0];
							}
						}
					}
					insertIndex++;
				}
			}
			if(needNormalization){
				for(int j=0;j<getDataSeries().size();j++){
					if(minmax[j][1] - minmax[j][0] >= 100){
						for(int i=0;i<dataMatrix.length;i++){
							dataMatrix[i][j] = (dataMatrix[i][j] - minmax[j][0])/(minmax[j][1] - minmax[j][0]);
						}
					}
				}
				conf.addItem(MINMAX, minmaxToString());
			}
		}
		return dataMatrix;
	}
	
	private String minmaxToString() {
		String mm = "";
		for(int i=0;i<minmax.length;i++){
			mm = mm + minmax[i][0] + "," + minmax[i][1] + ";";
		}
		return mm.substring(0, mm.length()-1);
	}

}
