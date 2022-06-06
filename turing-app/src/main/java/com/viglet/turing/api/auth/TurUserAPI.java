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

package com.viglet.turing.api.auth;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viglet.turing.bean.converse.auth.TurCurrentUser;
import com.viglet.turing.persistence.model.auth.TurGroup;
import com.viglet.turing.persistence.model.auth.TurUser;
import com.viglet.turing.persistence.repository.auth.TurGroupRepository;
import com.viglet.turing.persistence.repository.auth.TurUserRepository;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v2/user")
@Tag( name = "User", description = "User API")
public class TurUserAPI {

	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private TurUserRepository turUserRepository;
	@Autowired
	private TurGroupRepository turGroupRepository;

	@GetMapping
	public List<TurUser> turUserList() {
		return turUserRepository.findAll();
	}

	@GetMapping("/current")
	public TurCurrentUser turUserCurrent() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			boolean isAdmin = false;
			String currentUserName = authentication.getName();
			TurUser turUser = turUserRepository.findByUsername(currentUserName);
			turUser.setPassword(null);
			if (turUser.getTurGroups() != null) {
				for (TurGroup turGroup : turUser.getTurGroups()) {
					if (turGroup.getName().equals("Administrator"))
						isAdmin = true;
				}
			}
			TurCurrentUser turCurrentUser = new TurCurrentUser();
			turCurrentUser.setUsername(turUser.getUsername());
			turCurrentUser.setFirstName(turUser.getFirstName());
			turCurrentUser.setLastName(turUser.getLastName());
			turCurrentUser.setAdmin(isAdmin);

			return turCurrentUser;
		}

		return null;
	}

	@GetMapping("/{username}")
	public TurUser turUserEdit(@PathVariable String username) {
		TurUser turUser = turUserRepository.findByUsername(username);
		if (turUser != null) {
			turUser.setPassword(null);
			List<TurUser> turUsers = new ArrayList<>();
			turUsers.add(turUser);
			turUser.setTurGroups(turGroupRepository.findByTurUsersIn(turUsers));
		}
		return turUser;
	}

	@PutMapping("/{username}")
	public TurUser turUserUpdate(@PathVariable String username, @RequestBody TurUser turUser) {
		TurUser turUserEdit = turUserRepository.findByUsername(username);
		if (turUserEdit != null) {
			turUserEdit.setFirstName(turUser.getFirstName());
			turUserEdit.setLastName(turUser.getLastName());
			turUserEdit.setEmail(turUser.getEmail());
			if (turUser.getPassword() != null && turUser.getPassword().trim().length() > 0) {
				turUserEdit.setPassword(passwordEncoder.encode(turUser.getPassword()));
			}
			turUserEdit.setTurGroups(turUser.getTurGroups());
			turUserRepository.save(turUserEdit);
		}
		return turUserEdit;
	}

	@Transactional
	@DeleteMapping("/{username}")
	public boolean turUserDelete(@PathVariable String username) {
		turUserRepository.delete(username);
		return true;
	}

	@PostMapping("/{username}")
	public TurUser turUserAdd(@PathVariable String username, @RequestBody TurUser turUser) {
		if (turUser.getPassword() != null && turUser.getPassword().trim().length() > 0) {
			turUser.setPassword(passwordEncoder.encode(turUser.getPassword()));
		}

		turUserRepository.save(turUser);

		return turUser;
	}

	@GetMapping("/model")
	public TurUser turUserStructure() {
		return new TurUser();

	}

}
