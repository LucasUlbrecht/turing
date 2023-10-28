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
package com.viglet.turing.connector.cms.mappers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.viglet.turing.connector.cms.beans.TuringTag;
import com.viglet.turing.connector.cms.beans.TuringTagMap;
import com.viglet.turing.connector.cms.util.TuringUtils;

public class CTDMappings {
	private TuringTagMap turingTagMap;
	private String classValidToIndex = null;
	private static Logger logger = LoggerFactory.getLogger(CTDMappings.class);

	// OLD getIndexAttrTag
	public List<TuringTag> getTuringTagBySrcAttr(String srcAttrName) {

		List<TuringTag> turingTags = new ArrayList<>();
		if (logger.isDebugEnabled())
			logger.debug("CTDMappings attribute: " + srcAttrName);

		if (turingTagMap != null) {
			for (TuringTag turingTag : TuringUtils.turingTagMapToSet(turingTagMap)) {
				if (turingTag != null && turingTag.getSrcXmlName().equals(srcAttrName))
					turingTags.add(turingTag);

			}
		}
		return turingTags;
	}

	// Get TagList
	public Set<String> getTagList() {
		Set<String> tagNames = new HashSet<>();

		for (TuringTag turingTag : TuringUtils.turingTagMapToSet(turingTagMap)) {
			tagNames.add(turingTag.getTagName());
		}		

		if (logger.isDebugEnabled()) {
			logger.debug("getIndexAttrs Tag Names");
			for (String tagName : tagNames)
				logger.debug(tagName);
		}

		return tagNames;
	}

	public CTDMappings(TuringTagMap turingTagMap) {
		this.turingTagMap = turingTagMap;
	}

	public TuringTagMap getTuringTagMap() {
		return turingTagMap;
	}

	public String getClassValidToIndex() {
		return classValidToIndex;
	}

	public void setClassValidToIndex(String classValidToIndex) {
		this.classValidToIndex = classValidToIndex;
	}

}