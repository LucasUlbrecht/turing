/*
 * Copyright (C) 2016-2021 the original author or authors. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.viglet.turing.client.sn;

import com.viglet.turing.commons.sn.bean.TurSNSiteSearchDocumentBean;

/**
 * Get Info about a document result.
 * 
 * @author Alexandre Oliveira
 * 
 * @since 0.3.4
 */
public class TurSNDocument {

	private TurSNSiteSearchDocumentBean turSNSiteSearchDocumentBean;

	public Object getFieldValue(String field) {
		if (turSNSiteSearchDocumentBean != null && turSNSiteSearchDocumentBean.getFields() != null && turSNSiteSearchDocumentBean.getFields().containsKey(field)) {
			return turSNSiteSearchDocumentBean.getFields().get(field);
		} else {
			return null;
		}
	}

	public TurSNSiteSearchDocumentBean getContent() {
		return turSNSiteSearchDocumentBean;
	}

	public void setContent(TurSNSiteSearchDocumentBean turSNSiteSearchDocumentBean) {
		this.turSNSiteSearchDocumentBean = turSNSiteSearchDocumentBean;
	}

}
