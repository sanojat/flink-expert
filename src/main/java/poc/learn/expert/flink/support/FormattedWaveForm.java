package poc.learn.expert.flink.support;

public class FormattedWaveForm{

	private String id;
	private String patientname;
	private String reference;
	private String mdccode;
	private String effectivedatetime;
	private Double period;
	private Integer dimensions;
	private String data;
	private String devicereference;
	private Double factor;
	private Long effectiveTime;

	public Double getPeriod() {
		return period;
	}

	public void setPeriod(Double period) {
		this.period = period;
	}

	public Integer getDimensions() {
		return dimensions;
	}

	public void setDimensions(Integer dimensions) {
		this.dimensions = dimensions;
	}

	public Double getFactor() {
		return factor;
	}

	public void setFactor(Double factor) {
		this.factor = factor;
	}

	public Long getEffectiveTime() {
		return effectiveTime;
	}

	public void setEffectiveTime(Long effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPatientname() {
		return patientname;
	}

	public void setPatientname(String patientname) {
		this.patientname = patientname;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getMdccode() {
		return mdccode;
	}

	public void setMdccode(String mdccode) {
		this.mdccode = mdccode;
	}

	public String getEffectivedatetime() {
		return effectivedatetime;
	}

	public void setEffectivedatetime(String effectivedatetime) {
		this.effectivedatetime = effectivedatetime;
	}


	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getDevicereference() {
		return devicereference;
	}

	public void setDevicereference(String devicereference) {
		this.devicereference = devicereference;
	}

}
