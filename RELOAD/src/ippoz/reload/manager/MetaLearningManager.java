/**
 * 
 */
package ippoz.reload.manager;

import ippoz.reload.algorithm.type.LearnerType;
import ippoz.reload.commons.datacategory.DataCategory;
import ippoz.reload.commons.knowledge.Knowledge;
import ippoz.reload.commons.knowledge.KnowledgeType;
import ippoz.reload.commons.loader.Loader;
import ippoz.reload.commons.support.AppLogger;
import ippoz.reload.commons.support.PreferencesManager;
import ippoz.reload.featureselection.FeatureSelector;
import ippoz.reload.featureselection.FeatureSelectorType;
import ippoz.reload.manager.train.TrainerManager;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Tommy
 *
 */
public class MetaLearningManager extends DetectionManager {

	public MetaLearningManager(InputManager iManager, List<LearnerType> algTypes, PreferencesManager loaderPref) {
		super(iManager, algTypes, loaderPref);
		// TODO Auto-generated constructor stub
	}
	
	public void metaLearning(){
		featureSelection();
		train();
	}
	
	@Override
	public void featureSelection(){
		List<Knowledge> kList;
		FeatureSelectorManager fsm;
		String scoresFolderName;
		List<Loader> loaders;
		try {
			scoresFolderName = iManager.getMetaFolder();
			if(!new File(scoresFolderName).exists())
				new File(scoresFolderName).mkdirs();
			loaders = buildLoader("train");
			kList = Knowledge.generateKnowledge(loaders.get(0).fetch(), KnowledgeType.SINGLE, null, 0);
			fsm = new FeatureSelectorManager(Arrays.asList(FeatureSelector.createSelector(FeatureSelectorType.INFORMATION_GAIN, 100, true)), new DataCategory[]{DataCategory.PLAIN});
			fsm.selectFeatures(kList, scoresFolderName, loaderPref.getFilename());
			fsm.addLoaderInfo(loaders.get(0));
			fsm.saveSelectedFeatures(scoresFolderName, buildOutFilePrequel() + "_filtered.csv");
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to filter indicators");
		}
	}	

	@Override
	public void train() {
		TrainerManager tManager;
		Map<KnowledgeType, List<Knowledge>> kMap;
		List<Loader> loaders;
		try {
			loaders = buildLoader("train");
			kMap = generateKnowledge(loaders.iterator().next().fetch());
			if(!new File(iManager.getMetaFolder()).exists())
				new File(iManager.getMetaFolder()).mkdirs();
			if(!iManager.filteringResultExists(loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.')))){
				iManager.generateDataSeries(kMap, new DataCategory[]{DataCategory.PLAIN}, iManager.getMetaFolder() + buildOutFilePrequel() + "_filtered.csv");
			}
			tManager = new TrainerManager(iManager.getSetupFolder(), iManager.getDatasetsFolder(), "MULTIPLE_UNION", iManager.getMetaFolder(), loaderPref.getCompactFilename(), iManager.getMetaFolder(), kMap, iManager.loadConfigurations(algTypes, windowSize, sPolicy, true), metric, reputation, new DataCategory[]{DataCategory.PLAIN}, algTypes, iManager.loadSelectedDataSeriesString(iManager.getMetaFolder(), buildOutFilePrequel()), iManager.getKFoldCounter());
			tManager.addLoaderInfo(loaders.get(0));
			tManager.train(iManager.getMetaFolder() + buildOutFilePrequel(), getMetaLearningCSV());
			tManager.flush();
		} catch(Exception ex){
			AppLogger.logException(getClass(), ex, "Unable to train detector");
		}
	}
	
	@Override
	public String buildOutFilePrequel(){
		return loaderPref.getFilename().substring(0, loaderPref.getFilename().indexOf('.')) + "_" + algTypes.toString().substring(1, algTypes.toString().length()-1).replace(" ", "").replace(",", "_");
	}

}
