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
package com.viglet.turing.persistence.model.auth;

import java.io.Serializable;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * The persistent class for the TurRole database table.
 * 
 */
@Entity
@Table(name = "turRole")
@NamedQuery(name = "TurRole.findAll", query = "SELECT r FROM TurRole r")
public class TurRole implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GenericGenerator(name = "UUID", strategy = "com.viglet.turing.jpa.TurUUIDGenerator")
	@GeneratedValue(generator = "UUID")

	@Column(name = "id", updatable = false, nullable = false)
	private String id;


	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(nullable = true, length = 255)
	private String description;

	@ManyToMany
	private Set<TurGroup> turGroups = new HashSet<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public Set<TurGroup> getTurGroups() {
		return this.turGroups;
	}

	public void setTurGroups(Set<TurGroup> turGroups) {
		this.turGroups.clear();
		if (turGroups != null) {
			this.turGroups.addAll(turGroups);
		}
	}

}