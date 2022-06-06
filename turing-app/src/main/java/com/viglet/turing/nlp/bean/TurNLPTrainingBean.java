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
package com.viglet.turing.nlp.bean;

public class TurNLPTrainingBean {

	private String term;
	private boolean ignore;
	private String ner;
	private String convertTo;
	
	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	public String getNer() {
		return ner;
	}

	public void setNer(String ner) {
		this.ner = ner;
	}

	
	public String getConvertTo() {
		return convertTo;
	}

	public void setConvertTo(String convertTo) {
		this.convertTo = convertTo;
	}

	public String toString() {
		return String.format("%s %b %s",this.getTerm(),this.isIgnore(), this.getNer());
	}
}
