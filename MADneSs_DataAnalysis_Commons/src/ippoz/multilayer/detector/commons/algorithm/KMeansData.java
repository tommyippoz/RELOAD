package ippoz.multilayer.detector.commons.algorithm;

public class KMeansData {

	private String methodName;
	private String indicatorName;
	private String obTime;
	private String serviceName;
	private String value;
	private String layer;
	
	public KMeansData(String methodName, String indicatorName, String obTime, String serviceName, String value,
			String layer) {
		super();
		this.methodName = methodName;
		this.indicatorName = indicatorName;
		this.obTime = obTime;
		this.serviceName = serviceName;
		this.value = value;
		this.layer = layer;
	}
	
	
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getIndicatorName() {
		return indicatorName;
	}
	public void setIndicatorName(String indicatorName) {
		this.indicatorName = indicatorName;
	}
	public String getObTime() {
		return obTime;
	}
	public void setObTime(String obTime) {
		this.obTime = obTime;
	}
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getLayer() {
		return layer;
	}
	public void setLayer(String layer) {
		this.layer = layer;
	}


	@Override
	public String toString() {
		return indicatorName + "," + layer + "," + value + "," + obTime
				+ "," + serviceName;
	}
	
	
	
	
}
