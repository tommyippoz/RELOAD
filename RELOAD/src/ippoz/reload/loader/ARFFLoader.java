/**
 * 
 */
package ippoz.reload.loader;

import ippoz.reload.commons.knowledge.data.MonitoredData;

import java.util.List;

/**
 * The Class ARFFLoader. Allows loading Knowledge from ARFF Files.
 *
 * @author Tommy
 */
public class ARFFLoader extends SimpleLoader {

	/**
	 * Instantiates a new ARFF loader.
	 *
	 * @param runs the runs
	 */
	public ARFFLoader(List<Integer> runs) {
		super(runs);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#fetch()
	 */
	@Override
	public List<MonitoredData> fetch() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#getRuns()
	 */
	@Override
	public String getRuns() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see ippoz.reload.loader.Loader#getName()
	 */
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
