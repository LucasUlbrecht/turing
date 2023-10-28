/*
 * Copyright (C) 2016-2022 the original author or authors. 
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.viglet.turing.api.sn.bean;

import java.util.List;

import com.viglet.turing.persistence.repository.sn.metric.TurSNSiteMetricAccessTerm;

/**
 * 
 * @author Alexandre Oliveira
 * 
 * @since 0.3.6
 *
 */
public class TurSNSiteMetricsTopTermsBean {

	private List<TurSNSiteMetricAccessTerm> topTerms;

	private int totalTermsPeriod;

	private int totalTermsPreviousPeriod;

	private int variationPeriod;

	public TurSNSiteMetricsTopTermsBean(List<TurSNSiteMetricAccessTerm> metricsTerms, int totalTermsPeriod,
			int totalTermsPreviousPeriod) {
		super();
		this.topTerms = metricsTerms;
		this.totalTermsPeriod = totalTermsPeriod;
		this.totalTermsPreviousPeriod = totalTermsPreviousPeriod;
		if (totalTermsPreviousPeriod == 0) {
			this.variationPeriod = 0;
		} else {
			float total = ((float) totalTermsPeriod / (float) totalTermsPreviousPeriod);
			this.variationPeriod = (int) ((total < 1) ? (-1) * (1 - total) * 100 : (total * 100) - 100);
		}
	}

	public int getTotalTermsPeriod() {
		return totalTermsPeriod;
	}

	public void setTotalTermsPeriod(int totalTermsPeriod) {
		this.totalTermsPeriod = totalTermsPeriod;
	}

	public List<TurSNSiteMetricAccessTerm> getTopTerms() {
		return topTerms;
	}

	public void setTopTerms(List<TurSNSiteMetricAccessTerm> topTerms) {
		this.topTerms = topTerms;
	}

	public int getTotalTermsPreviousPeriod() {
		return totalTermsPreviousPeriod;
	}

	public void setTotalTermsPreviousPeriod(int totalTermsPreviousPeriod) {
		this.totalTermsPreviousPeriod = totalTermsPreviousPeriod;
	}

	public int getVariationPeriod() {
		return variationPeriod;
	}

	public void setVariationPeriod(int variationPeriod) {
		this.variationPeriod = variationPeriod;
	}

}