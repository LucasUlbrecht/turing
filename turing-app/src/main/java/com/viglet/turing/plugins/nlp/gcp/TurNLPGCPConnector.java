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
package com.viglet.turing.plugins.nlp.gcp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viglet.turing.commons.utils.TurCommonsUtils;
import com.viglet.turing.nlp.TurNLPEntityRequest;
import com.viglet.turing.nlp.TurNLPRequest;
import com.viglet.turing.plugins.nlp.TurNLPPlugin;
import com.viglet.turing.plugins.nlp.gcp.request.TurNLPGCPDocumentRequest;
import com.viglet.turing.plugins.nlp.gcp.request.TurNLPGCPEncodingResponse;
import com.viglet.turing.plugins.nlp.gcp.request.TurNLPGCPRequest;
import com.viglet.turing.plugins.nlp.gcp.request.TurNLPGCPTypeResponse;
import com.viglet.turing.plugins.nlp.gcp.response.TurNLPGCPEntityResponse;
import com.viglet.turing.plugins.nlp.gcp.response.TurNLPGCPResponse;
import com.viglet.turing.solr.TurSolrField;

/**
 * 
 * @author Alexandre Oliveira
 * 
 * @since 0.3.6
 *
 */
@Component
public class TurNLPGCPConnector implements TurNLPPlugin {
	private static final Logger logger = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public Map<String, List<String>> processAttributesToEntityMap(TurNLPRequest turNLPRequest) {
		return this.request(turNLPRequest);
	}

	public Map<String, List<String>> request(TurNLPRequest turNLPRequest) {
		Map<String, Set<String>> entityList = new HashMap<>();
		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
			URL serverURL = new URL(String.format("%s?key=%s", turNLPRequest.getTurNLPInstance().getEndpointURL(),
					turNLPRequest.getTurNLPInstance().getKey()));
			HttpPost httpPost = new HttpPost(serverURL.toString());
			for (Object attrValue : turNLPRequest.getData().values()) {
				String text = TurSolrField.convertFieldToString(attrValue);
				
				List<String> newLineList =  new ArrayList<>();
				Arrays.asList(text.split("\n")).forEach(newLine -> {
					List<String> textWords = Arrays.asList(newLine.split(" ")); 
					List<String> convertTextWords = new ArrayList<>();
					textWords.forEach(word -> {
						String checkWord = word.trim();
						if (StringUtils.isAllUpperCase(checkWord)) {
							convertTextWords.add(StringUtils.capitalize(word.toLowerCase()));
						}
						else {
							convertTextWords.add(word);
						}
						
					});
					String newText =  StringUtils.join(convertTextWords, " ");
					newLineList.add(newText);
				});
				String newTextNewLine =  StringUtils.join(newLineList, "\n");
				
				TurNLPGCPDocumentRequest turNLPGCPDocumentRequest = new TurNLPGCPDocumentRequest(
						TurNLPGCPTypeResponse.PLAIN_TEXT, turNLPRequest.getTurNLPInstance().getLanguage(),
						newTextNewLine);
				TurNLPGCPRequest turNLPGCPRequest = new TurNLPGCPRequest(turNLPGCPDocumentRequest,
						TurNLPGCPEncodingResponse.UTF16);
				httpPost.addHeader("Content-Type", "application/json");
				httpPost.setEntity(new StringEntity(convertObjectToJson(turNLPGCPRequest), StandardCharsets.UTF_8));
				entityList = updateEntityList(httpclient.execute(httpPost).getEntity(), turNLPRequest.getEntities());
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		return this.generateEntityMapFromSentenceTokens(turNLPRequest, entityList);

	}

	private String convertObjectToJson(TurNLPGCPRequest turNLPGCPRequest)
			throws UnsupportedEncodingException, JsonProcessingException {
		return new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(turNLPGCPRequest);
	}

	private Map<String, Set<String>> updateEntityList(HttpEntity entity,
			List<TurNLPEntityRequest> turNLPEntitiesRequest) {
		Map<String, Set<String>> entityList = new HashMap<>();
		if (entity != null) {
			String jsonResponse;
			try {
				jsonResponse = new String(entity.getContent().readAllBytes(), StandardCharsets.UTF_8);
				if (TurCommonsUtils.isJSONValid(jsonResponse)) {
					entityList = this.getEntities(new ObjectMapper().readValue(jsonResponse, TurNLPGCPResponse.class),
							turNLPEntitiesRequest);
				}
			} catch (UnsupportedOperationException | IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return entityList;
	}

	public Map<String, List<String>> generateEntityMapFromSentenceTokens(TurNLPRequest turNLPRequest,
			Map<String, Set<String>> entityList) {
		Map<String, List<String>> entityAttributes = new HashMap<>();
		turNLPRequest.getEntities()
				.forEach(nlpVendorEntity -> entityAttributes.put(
						nlpVendorEntity.getTurNLPVendorEntity().getTurNLPEntity().getInternalName(),
						entityList.containsKey(nlpVendorEntity.getTurNLPVendorEntity().getName())
								? List.copyOf(entityList.get(nlpVendorEntity.getTurNLPVendorEntity().getName()))
								: new ArrayList<>()));

		logger.debug("CGP NLP getAttributes: {}", entityAttributes);
		return entityAttributes;
	}

	public Map<String, Set<String>> getEntities(TurNLPGCPResponse turNLPGCPResponse,
			List<TurNLPEntityRequest> turNLPEntitiesRequest) {
		Map<String, Set<String>> entityList = new HashMap<>();
		
		turNLPGCPResponse.getEntities().forEach(entity -> {
			TurNLPEntityRequest turNLPEntityRequest = getTurNLPEntityRequest(turNLPEntitiesRequest, entity);
			if (turNLPEntityRequest != null) {
				entity.getMentions().forEach(mention -> {
					if (turNLPEntityRequest.getTypes() == null
							|| turNLPEntityRequest.getTypes().contains(mention.getType().toString())) {
						handleEntity(entityList, entity);
					}
				});
			}
		});
		return entityList;
	}

	private TurNLPEntityRequest getTurNLPEntityRequest(List<TurNLPEntityRequest> turNLPEntitiesRequest,
			TurNLPGCPEntityResponse entity) {
		return turNLPEntitiesRequest.stream().filter(turNLPEntityRequestIn -> turNLPEntityRequestIn
				.getTurNLPVendorEntity().getName().equals(entity.getType().toString())).findFirst().orElse(null);
	}

	private void handleEntity(Map<String, Set<String>> entityList, TurNLPGCPEntityResponse entity) {
		if (entityList.containsKey(entity.getType().toString())) {
			entityList.get(entity.getType().toString()).add(entity.getName().toLowerCase().trim());
		} else {
			Set<String> valueList = new HashSet<>();
			valueList.add(entity.getName().toLowerCase().trim());
			entityList.put(entity.getType().toString(), valueList);
		}

	}
}