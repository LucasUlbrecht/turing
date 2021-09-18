/*
 * Copyright (C) 2016-2021 the original author or authors. 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.viglet.turing.api.sn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viglet.turing.persistence.model.nlp.TurNLPEntity;
import com.viglet.turing.persistence.model.nlp.TurNLPInstanceEntity;
import com.viglet.turing.persistence.model.sn.TurSNSite;
import com.viglet.turing.persistence.model.sn.TurSNSiteField;
import com.viglet.turing.persistence.model.sn.TurSNSiteFieldExt;
import com.viglet.turing.persistence.repository.nlp.TurNLPEntityRepository;
import com.viglet.turing.persistence.repository.nlp.TurNLPInstanceEntityRepository;
import com.viglet.turing.persistence.repository.sn.TurSNSiteFieldExtRepository;
import com.viglet.turing.persistence.repository.sn.TurSNSiteFieldRepository;
import com.viglet.turing.persistence.repository.sn.TurSNSiteRepository;
import com.viglet.turing.se.field.TurSEFieldType;
import com.viglet.turing.sn.TurSNFieldType;
import com.viglet.turing.sn.template.TurSNTemplate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/sn/{snSiteId}/field/ext")
@Api(tags = "Semantic Navigation Field Ext", description = "Semantic Navigation Field Ext API")
public class TurSNSiteFieldExtAPI {
	private static final Log logger = LogFactory.getLog(TurSNSiteFieldExtAPI.class);
	@Autowired
	TurSNSiteRepository turSNSiteRepository;
	@Autowired
	TurSNSiteFieldExtRepository turSNSiteFieldExtRepository;
	@Autowired
	TurSNSiteFieldRepository turSNSiteFieldRepository;
	@Autowired
	TurNLPInstanceEntityRepository turNLPInstanceEntityRepository;
	@Autowired
	TurNLPEntityRepository turNLPEntityRepository;
	@Autowired
	TurSNTemplate turSNTemplate;

	@ApiOperation(value = "Semantic Navigation Site Field Ext List")
	@Transactional
	@GetMapping
	public List<TurSNSiteFieldExt> turSNSiteFieldExtList(@PathVariable String snSiteId) {
		return turSNSiteRepository.findById(snSiteId).map(turSNSite -> {
			Map<String, TurSNSiteField> fieldMap = new HashMap<>();
			Map<String, TurNLPEntity> nerMap = new HashMap<>();
			Map<String, TurNLPEntity> thesaurusMap = new HashMap<>();

			List<TurSNSiteField> turSNSiteFields = turSNSiteFieldRepository.findByTurSNSite(turSNSite);

			if (turSNSite.getTurNLPInstance() != null) {
				List<TurNLPInstanceEntity> turNLPInstanceEntities = turNLPInstanceEntityRepository
						.findByTurNLPInstanceAndEnabled(turSNSite.getTurNLPInstance(), 1);
				turNLPInstanceEntities.forEach(turNLPInstanceEntity -> {
					TurNLPEntity turNLPEntity = turNLPInstanceEntity.getTurNLPEntity();
					nerMap.put(turNLPEntity.getId(), turNLPEntity);
				});
			} else {
				turSNSiteFieldExtRepository.deleteByTurSNSiteAndSnType(turSNSite, TurSNFieldType.NER);
			}
			List<TurNLPEntity> turNLPEntityThesaurus = turNLPEntityRepository.findByLocal(1);

			if (turSNSiteFields.isEmpty()) {
				turSNTemplate.createSEFields(turSNSite);
			} else {
				turSNSiteFields.forEach(turSNSiteField -> fieldMap.put(turSNSiteField.getId(), turSNSiteField));
			}

			turNLPEntityThesaurus.forEach(turNLPEntityThesaurusSingle -> thesaurusMap
					.put(turNLPEntityThesaurusSingle.getId(), turNLPEntityThesaurusSingle));

			List<TurSNSiteFieldExt> turSNSiteFieldExts = this.turSNSiteFieldExtRepository.findByTurSNSite(turSNSite);

			for (TurSNSiteFieldExt turSNSiteFieldExt : turSNSiteFieldExts) {
				switch (turSNSiteFieldExt.getSnType()) {
				case SE:
					if (fieldMap.containsKey(turSNSiteFieldExt.getExternalId())) {
						fieldMap.remove(turSNSiteFieldExt.getExternalId());
					}
					break;
				case NER:
					if (nerMap.containsKey(turSNSiteFieldExt.getExternalId())) {
						nerMap.remove(turSNSiteFieldExt.getExternalId());
					}
					break;

				case THESAURUS:
					if (thesaurusMap.containsKey(turSNSiteFieldExt.getExternalId())) {
						thesaurusMap.remove(turSNSiteFieldExt.getExternalId());
					}
					break;
				}
			}

			for (TurSNSiteField turSNSiteField : fieldMap.values()) {
				TurSNSiteFieldExt turSNSiteFieldExt = new TurSNSiteFieldExt();
				turSNSiteFieldExt.setEnabled(0);
				turSNSiteFieldExt.setName(turSNSiteField.getName());
				turSNSiteFieldExt.setDescription(turSNSiteField.getDescription());
				turSNSiteFieldExt.setFacet(0);
				turSNSiteFieldExt.setFacetName(turSNSiteField.getName());
				turSNSiteFieldExt.setHl(0);
				turSNSiteFieldExt.setMultiValued(turSNSiteField.getMultiValued());
				turSNSiteFieldExt.setMlt(0);
				turSNSiteFieldExt.setExternalId(turSNSiteField.getId());
				turSNSiteFieldExt.setSnType(TurSNFieldType.SE);
				turSNSiteFieldExt.setType(turSNSiteField.getType());
				turSNSiteFieldExt.setTurSNSite(turSNSite);
				turSNSiteFieldExtRepository.save(turSNSiteFieldExt);

				turSNSiteFieldExts.add(turSNSiteFieldExt);
			}

			nerMap.values().forEach(turNLPEntity -> addTurSNSiteFieldExt(TurSNFieldType.NER, turSNSite,
					turSNSiteFieldExts, turNLPEntity));
			thesaurusMap.values().forEach(turNLPEntity -> addTurSNSiteFieldExt(TurSNFieldType.THESAURUS, turSNSite,
					turSNSiteFieldExts, turNLPEntity));

			return turSNSiteFieldExtRepository.findByTurSNSite(turSNSite);

		}).orElse(new ArrayList<>());

	}

	private void addTurSNSiteFieldExt(TurSNFieldType turSNFieldType, TurSNSite turSNSite,
			List<TurSNSiteFieldExt> turSNSiteFieldExts, TurNLPEntity turNLPEntity) {
		TurSNSiteFieldExt turSNSiteFieldExt = new TurSNSiteFieldExt();
		turSNSiteFieldExt.setEnabled(0);
		turSNSiteFieldExt.setName(turNLPEntity.getInternalName());
		turSNSiteFieldExt.setDescription(turNLPEntity.getDescription());
		turSNSiteFieldExt.setFacet(0);
		turSNSiteFieldExt.setFacetName(turNLPEntity.getName());
		turSNSiteFieldExt.setHl(0);
		turSNSiteFieldExt.setMultiValued(1);
		turSNSiteFieldExt.setMlt(0);
		turSNSiteFieldExt.setExternalId(turNLPEntity.getId());
		turSNSiteFieldExt.setSnType(turSNFieldType);
		turSNSiteFieldExt.setType(TurSEFieldType.STRING);
		turSNSiteFieldExt.setTurSNSite(turSNSite);
		turSNSiteFieldExtRepository.save(turSNSiteFieldExt);

		turSNSiteFieldExts.add(turSNSiteFieldExt);
	}

	@ApiOperation(value = "Show a Semantic Navigation Site Field Ext")
	@GetMapping("/{id}")
	public TurSNSiteFieldExt turSNSiteFieldExtGet(@PathVariable String snSiteId, @PathVariable String id) {
		return turSNSiteFieldExtRepository.findById(id).orElse(new TurSNSiteFieldExt());
	}

	@ApiOperation(value = "Update a Semantic Navigation Site Field Ext")
	@PutMapping("/{id}")
	public TurSNSiteFieldExt turSNSiteFieldExtUpdate(@PathVariable String snSiteId, @PathVariable String id,
			@RequestBody TurSNSiteFieldExt turSNSiteFieldExt) {
		return this.turSNSiteFieldExtRepository.findById(id).map(turSNSiteFieldExtEdit -> {
			turSNSiteFieldExtEdit.setFacetName(turSNSiteFieldExt.getFacetName());
			turSNSiteFieldExtEdit.setMultiValued(turSNSiteFieldExt.getMultiValued());
			turSNSiteFieldExtEdit.setName(turSNSiteFieldExt.getName());
			turSNSiteFieldExtEdit.setDescription(turSNSiteFieldExt.getDescription());
			turSNSiteFieldExtEdit.setType(turSNSiteFieldExt.getType());
			turSNSiteFieldExtEdit.setFacet(turSNSiteFieldExt.getFacet());
			turSNSiteFieldExtEdit.setHl(turSNSiteFieldExt.getHl());
			turSNSiteFieldExtEdit.setEnabled(turSNSiteFieldExt.getEnabled());
			turSNSiteFieldExtEdit.setMlt(turSNSiteFieldExt.getMlt());
			turSNSiteFieldExtEdit.setExternalId(turSNSiteFieldExt.getExternalId());
			turSNSiteFieldExtEdit.setRequired(turSNSiteFieldExt.getRequired());
			turSNSiteFieldExtEdit.setDefaultValue(turSNSiteFieldExt.getDefaultValue());
			turSNSiteFieldExtEdit.setNlp(turSNSiteFieldExt.getNlp());
			turSNSiteFieldExtEdit.setSnType(turSNSiteFieldExt.getSnType());
			this.turSNSiteFieldExtRepository.save(turSNSiteFieldExtEdit);

			this.updateExternalField(turSNSiteFieldExt);

			return turSNSiteFieldExtEdit;
		}).orElse(new TurSNSiteFieldExt());

	}

	@Transactional
	@ApiOperation(value = "Delete a Semantic Navigation Site Field Ext")
	@DeleteMapping("/{id}")
	public boolean turSNSiteFieldExtDelete(@PathVariable String snSiteId, @PathVariable String id) {
		return this.turSNSiteFieldExtRepository.findById(id).map(turSNSiteFieldExtEdit -> {
			switch (turSNSiteFieldExtEdit.getSnType()) {
			case SE:
				this.turSNSiteFieldRepository.delete(turSNSiteFieldExtEdit.getExternalId());

				break;
			default:
				break;
			}

			this.turSNSiteFieldExtRepository.delete(id);

			return true;
		}).orElse(false);

	}

	@ApiOperation(value = "Create a Semantic Navigation Site Field Ext")
	@PostMapping
	public TurSNSiteFieldExt turSNSiteFieldExtAdd(@PathVariable String snSiteId,
			@RequestBody TurSNSiteFieldExt turSNSiteFieldExt) {

		return createSEField(snSiteId, turSNSiteFieldExt);

	}

	private TurSNSiteFieldExt createSEField(String snSiteId, TurSNSiteFieldExt turSNSiteFieldExt) {
		return turSNSiteRepository.findById(snSiteId).map(turSNSite -> {
			TurSNSiteField turSNSiteField = new TurSNSiteField();
			turSNSiteField.setDescription(turSNSiteFieldExt.getDescription());
			turSNSiteField.setMultiValued(turSNSiteFieldExt.getMultiValued());
			turSNSiteField.setName(turSNSiteFieldExt.getName());
			turSNSiteField.setType(turSNSiteFieldExt.getType());
			turSNSiteField.setTurSNSite(turSNSite);
			this.turSNSiteFieldRepository.save(turSNSiteField);

			turSNSiteFieldExt.setTurSNSite(turSNSite);
			turSNSiteFieldExt.setSnType(TurSNFieldType.SE);
			turSNSiteFieldExt.setExternalId(turSNSiteField.getId());

			this.turSNSiteFieldExtRepository.save(turSNSiteFieldExt);

			return turSNSiteFieldExt;

		}).orElse(new TurSNSiteFieldExt());

	}

	public void updateExternalField(TurSNSiteFieldExt turSNSiteFieldExt) {
		switch (turSNSiteFieldExt.getSnType()) {
		case SE:
			turSNSiteFieldRepository.findById(turSNSiteFieldExt.getExternalId()).ifPresent(turSNSiteField -> {
				turSNSiteField.setDescription(turSNSiteFieldExt.getDescription());
				turSNSiteField.setMultiValued(turSNSiteFieldExt.getMultiValued());
				turSNSiteField.setName(turSNSiteFieldExt.getName());
				turSNSiteField.setType(turSNSiteFieldExt.getType());
				this.turSNSiteFieldRepository.save(turSNSiteField);
			});

			break;

		case NER:
			turNLPEntityRepository.findById(turSNSiteFieldExt.getExternalId()).ifPresent(turNLPEntityNER -> {
				turNLPEntityNER.setDescription(turSNSiteFieldExt.getDescription());
				turNLPEntityNER.setInternalName(turSNSiteFieldExt.getName());
				this.turNLPEntityRepository.save(turNLPEntityNER);
			});

			break;

		case THESAURUS:
			turNLPEntityRepository.findById(turSNSiteFieldExt.getExternalId()).ifPresent(turNLPEntityThesaurus -> {
				turNLPEntityThesaurus.setDescription(turSNSiteFieldExt.getDescription());
				turNLPEntityThesaurus.setInternalName(turSNSiteFieldExt.getName());
				this.turNLPEntityRepository.save(turNLPEntityThesaurus);
			});

			break;
		}
	}

	@GetMapping("/create")
	public List<TurSNSite> turSNSiteFieldExtCreate(@PathVariable String snSiteId) {
		return turSNSiteRepository.findById(snSiteId).map(turSNSite -> {
			List<TurSNSiteFieldExt> turSNSiteFieldExts = turSNSiteFieldExtRepository
					.findByTurSNSiteAndEnabled(turSNSite, 1);
			turSNSiteFieldExts.forEach(turSNSiteFieldExt -> this.createField(turSNSite, turSNSiteFieldExt));
			return this.turSNSiteRepository.findAll();
		}).orElse(new ArrayList<>());
	}

	public void createField(TurSNSite turSNSite, TurSNSiteFieldExt turSNSiteFieldExts) {

		JSONObject jsonAddField = new JSONObject();
		String fieldName = null;

		if (turSNSiteFieldExts.getSnType() == TurSNFieldType.NER) {
			fieldName = String.format("turing_entity_%s", turSNSiteFieldExts.getName());
		} else {
			fieldName = turSNSiteFieldExts.getName();
		}

		jsonAddField.put("name", fieldName);

		jsonAddField.put("indexed", true);
		jsonAddField.put("stored", true);
		if (turSNSiteFieldExts.getMultiValued() == 1) {
			jsonAddField.put("type", "string");
			jsonAddField.put("multiValued", true);
		} else {
			if (turSNSiteFieldExts.getType().equals(TurSEFieldType.DATE)) {
				jsonAddField.put("type", "pdate");
			} else {
				jsonAddField.put("type", "text_general");
			}
			jsonAddField.put("multiValued", false);
		}
		JSONObject json = new JSONObject();
		json.put("add-field", jsonAddField);
		// json.put("replace-field", jsonAddField);
		HttpPost httpPost = new HttpPost(String.format("http://%s:%d/solr/%s/schema",
				turSNSite.getTurSEInstance().getHost(), turSNSite.getTurSEInstance().getPort(), turSNSite.getCore()));
		StringEntity entity;
		try {
			entity = new StringEntity(json.toString());

			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			try (CloseableHttpClient client = HttpClients.createDefault()) {
				client.execute(httpPost);
			}

			this.copyField(turSNSite, fieldName, "_text_");
		} catch (IOException e) {
			logger.error(e);
		}
	}

	public void copyField(TurSNSite turSNSite, String field, String dest) {

		JSONObject jsonAddField = new JSONObject();
		jsonAddField.put("source", field);

		jsonAddField.put("dest", dest);
		JSONObject json = new JSONObject();
		json.put("add-copy-field", jsonAddField);

		HttpPost httpPost = new HttpPost(String.format("http://%s:%d/solr/%s/schema",
				turSNSite.getTurSEInstance().getHost(), turSNSite.getTurSEInstance().getPort(), turSNSite.getCore()));
		try {
			StringEntity entity = new StringEntity(json.toString());
			httpPost.setEntity(entity);
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			try (CloseableHttpClient client = HttpClients.createDefault()) {
				client.execute(httpPost);
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}
}