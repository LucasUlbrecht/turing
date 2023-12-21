package com.viglet.turing.connector.cms.config;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;

public interface IHandlerConfiguration {

	URL getTuringURL();
	
	String getProviderName();
	
	TurSNSiteConfig getDefaultSNSiteConfig();

	TurSNSiteConfig getSNSiteConfig(String site);

	TurSNSiteConfig getSNSiteConfig(String site, String locale);

	String getMappingsXML();

	List<String> getSitesAssocPriority();

	String getCDAContextName();

	String getCDAURLPrefix();

	String getCDAURLPrefix(String site);

	String getCDAContextName(String site);

	String getApiKey();

	Path getFileSourcePath();
}
