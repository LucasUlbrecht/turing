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

package com.viglet.turing.persistence.model.nlp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.viglet.turing.persistence.model.nlp.term.TurTerm;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * The persistent class for the turEntities database table.
 * 
 */
@Getter
@Setter
@Entity
@Table(name = "nlp_entity")
@JsonIgnoreProperties({ "turNLPInstanceEntities", "turNLPVendorEntities" } )
public class TurNLPEntity implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "internal_name", nullable = false, length = 50)
	private String internalName;
	
	@Column(name = "collection_name", nullable = false, length = 50)
	private String collectionName;

	@Column(nullable = false)
	private String description;


	@Column(nullable = false)
	private int local;

	@Column(nullable = false, length = 50)
	private String name;
	
	@Column
	private int enabled;		

	public TurNLPEntity() {
		super();
	}
	public TurNLPEntity(String internalName, String name, String description,  String collectionName, int local, int enabled) {
		super();
		this.collectionName = collectionName;
		this.description = description;
		this.internalName = internalName;
		this.local = local;
		this.name = name;
		this.enabled = enabled;
	}

	// bi-directional many-to-one association to TurNLPVendorEntity
	@OneToMany(mappedBy = "turNLPEntity", orphanRemoval = true)
	@Cascade({ CascadeType.ALL })
	@Fetch(org.hibernate.annotations.FetchMode.JOIN)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<TurNLPVendorEntity> turNLPVendorEntities;
	
	
	// bi-directional many-to-one association to TurTerm
	@OneToMany(mappedBy = "turNLPEntity", orphanRemoval = true)
	@Cascade({ CascadeType.ALL })
	@Fetch(org.hibernate.annotations.FetchMode.JOIN)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Set<TurTerm> turTerms;
}
