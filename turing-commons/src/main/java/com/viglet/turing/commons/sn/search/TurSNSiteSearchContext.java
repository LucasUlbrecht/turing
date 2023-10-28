package com.viglet.turing.commons.sn.search;

import java.net.URI;

import com.viglet.turing.commons.se.TurSEParameters;
import com.viglet.turing.commons.sn.bean.TurSNSitePostParamsBean;

public class TurSNSiteSearchContext {
	private String siteName;
	private TurSEParameters turSEParameters;
	private String locale;
	private TurSNSitePostParamsBean turSNSitePostParamsBean;
	private URI uri;

	public TurSNSiteSearchContext(String siteName, TurSEParameters turSEParameters, String locale, URI uri,TurSNSitePostParamsBean turSNSitePostParamsBean) {
		super();
		this.siteName = siteName;
		this.turSEParameters = turSEParameters;
		this.locale = locale;
		this.uri = uri;
		this.turSNSitePostParamsBean = turSNSitePostParamsBean;
	}

	public TurSNSiteSearchContext(String siteName, TurSEParameters turSEParameters, String locale, URI uri) {
		this(siteName, turSEParameters, locale, uri, new TurSNSitePostParamsBean());
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public TurSEParameters getTurSEParameters() {
		return turSEParameters;
	}

	public void setTurSEParameters(TurSEParameters turSEParameters) {
		this.turSEParameters = turSEParameters;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public TurSNSitePostParamsBean getTurSNSitePostParamsBean() {
		return turSNSitePostParamsBean;
	}

	public void setTurSNSitePostParamsBean(TurSNSitePostParamsBean turSNSitePostParamsBean) {
		this.turSNSitePostParamsBean = turSNSitePostParamsBean;
	}
}