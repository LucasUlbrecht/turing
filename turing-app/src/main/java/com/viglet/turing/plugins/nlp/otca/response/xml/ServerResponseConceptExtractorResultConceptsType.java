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
package com.viglet.turing.plugins.nlp.otca.response.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ServerResponseConceptExtractorResultConceptsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServerResponseConceptExtractorResultConceptsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element name="Concept" type="{}ServerResponseConceptExtractorResultConcept1Type"/>
 *           &lt;element name="ExtractedTerm" type="{}ServerResponseConceptExtractorResultConcept2Type"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServerResponseConceptExtractorResultConceptsType", propOrder = {
    "conceptOrExtractedTerm"
})
public class ServerResponseConceptExtractorResultConceptsType {

    @XmlElements({
        @XmlElement(name = "ExtractedTerm", type = ServerResponseConceptExtractorResultConcept2Type.class),
        @XmlElement(name = "Concept", type = ServerResponseConceptExtractorResultConcept1Type.class)
    })
    protected List<Object> conceptOrExtractedTerm;

    /**
     * Gets the value of the conceptOrExtractedTerm property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the conceptOrExtractedTerm property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConceptOrExtractedTerm().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServerResponseConceptExtractorResultConcept2Type }
     * {@link ServerResponseConceptExtractorResultConcept1Type }
     * 
     * 
     */
    public List<Object> getConceptOrExtractedTerm() {
        if (conceptOrExtractedTerm == null) {
            conceptOrExtractedTerm = new ArrayList<>();
        }
        return this.conceptOrExtractedTerm;
    }

}
