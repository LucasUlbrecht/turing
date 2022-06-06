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

package com.viglet.turing.api.sn.search;

import com.viglet.turing.commons.se.TurSEParameters;
import com.viglet.turing.commons.sn.bean.TurSNSearchLatestRequestBean;
import com.viglet.turing.commons.sn.bean.TurSNSiteLocaleBean;
import com.viglet.turing.commons.sn.bean.TurSNSitePostParamsBean;
import com.viglet.turing.commons.sn.bean.TurSNSiteSearchBean;
import com.viglet.turing.commons.sn.search.TurSNParamType;
import com.viglet.turing.commons.sn.search.TurSNSiteSearchContext;
import com.viglet.turing.persistence.model.sn.TurSNSite;
import com.viglet.turing.persistence.repository.sn.TurSNSiteRepository;
import com.viglet.turing.sn.TurSNSearchProcess;
import com.viglet.turing.sn.TurSNUtils;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sn/{siteName}/search")
@Tag(name = "Semantic Navigation Search", description = "Semantic Navigation Search API")
public class TurSNSiteSearchAPI {
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());
	@Autowired
	private TurSNSearchProcess turSNSearchProcess;
	@Autowired
	private TurSNSiteRepository turSNSiteRepository;

	@GetMapping
	public TurSNSiteSearchBean turSNSiteSearchSelectGet(@PathVariable String siteName,
			@RequestParam(required = false, name = TurSNParamType.QUERY) String q,
			@RequestParam(required = false, name = TurSNParamType.PAGE) Integer currentPage,
			@RequestParam(required = false, name = TurSNParamType.FILTER_QUERIES) List<String> fq,
			@RequestParam(required = false, name = TurSNParamType.SORT) String sort,
			@RequestParam(required = false, name = TurSNParamType.ROWS, defaultValue = "10") Integer rows,
			@RequestParam(required = false, name = TurSNParamType.GROUP) String group,
			@RequestParam(required = false, name = TurSNParamType.AUTO_CORRECTION_DISABLED, defaultValue = "0") Integer autoCorrectionDisabled,
			@RequestParam(required = false, name = TurSNParamType.LOCALE) String locale, HttpServletRequest request) {
		return turSNSearchProcess.search(new TurSNSiteSearchContext(siteName,
				new TurSEParameters(q, fq, currentPage, sort, rows, group, autoCorrectionDisabled), locale,
				TurSNUtils.requestToURI(request)));
	}

	@PostMapping
	public ResponseEntity<TurSNSiteSearchBean> turSNSiteSearchSelectPost(@PathVariable String siteName,
			@RequestParam(required = false, name = TurSNParamType.QUERY) String q,
			@RequestParam(required = false, name = TurSNParamType.PAGE) Integer currentPage,
			@RequestParam(required = false, name = TurSNParamType.FILTER_QUERIES) List<String> fq,
			@RequestParam(required = false, name = TurSNParamType.SORT) String sort,
			@RequestParam(required = false, name = TurSNParamType.ROWS, defaultValue = "10") Integer rows,
			@RequestParam(required = false, name = TurSNParamType.GROUP) String group,
			@RequestParam(required = false, name = TurSNParamType.AUTO_CORRECTION_DISABLED, defaultValue = "0") Integer autoCorrectionDisabled,
			@RequestParam(required = false, name = TurSNParamType.LOCALE) String locale,
			@RequestBody TurSNSitePostParamsBean turSNSitePostParamsBean, Principal principal,
			HttpServletRequest request) {
		if (principal != null) {
			turSNSitePostParamsBean.setTargetingRules(
					turSNSearchProcess.requestTargetingRules(turSNSitePostParamsBean.getTargetingRules()));
			return new ResponseEntity<>(turSNSearchProcess.search(new TurSNSiteSearchContext(siteName,
					new TurSEParameters(q, fq, currentPage, sort, rows, group, autoCorrectionDisabled), locale,
					TurSNUtils.requestToURI(request), turSNSitePostParamsBean)), HttpStatus.OK);
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	@GetMapping("locales")
	public List<TurSNSiteLocaleBean> turSNSiteSearchLocale(@PathVariable String siteName, HttpServletRequest request) {

		try {
			TurSNSite turSNSite = turSNSiteRepository.findByName(siteName);
			return turSNSearchProcess.responseLocales(turSNSite, new URI(String.format("/api/sn/%s/search", siteName)));
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		return Collections.emptyList();
	}

	@PostMapping("latest")
	public ResponseEntity<List<String>> turSNSiteSearchLatestImpersonate(@PathVariable String siteName,
			@RequestParam(required = false, name = TurSNParamType.ROWS, defaultValue = "5") Integer rows,
			@RequestParam(required = true, name = TurSNParamType.LOCALE) String locale,
			@RequestBody Optional<TurSNSearchLatestRequestBean> turSNSearchLatestRequestBean, Principal principal,
			HttpServletRequest request) {
		if (principal != null) {
			return new ResponseEntity<>(turSNSearchProcess.latestSearches(siteName, locale,
					isLatestImpersonate(turSNSearchLatestRequestBean) ? turSNSearchLatestRequestBean.get().getUserId()
							: principal.getName(),
					rows), HttpStatus.OK);
		}
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	}

	private boolean isLatestImpersonate(Optional<TurSNSearchLatestRequestBean> turSNSearchLatestRequestBean) {
		return turSNSearchLatestRequestBean.isPresent() && turSNSearchLatestRequestBean.get().getUserId() != null;
	}
}
