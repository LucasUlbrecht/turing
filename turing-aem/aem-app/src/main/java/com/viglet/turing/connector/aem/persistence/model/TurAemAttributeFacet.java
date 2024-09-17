/*
 *
 * Copyright (C) 2016-2024 the original author or authors.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.viglet.turing.connector.aem.persistence.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.viglet.turing.spring.jpa.TurUuid;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

/**
 * @author Alexandre Oliveira
 * @since 0.3.9
 */
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "aem_attribute_facet")
@JsonIgnoreProperties({"turAemSource"})
public class TurAemAttributeFacet implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @TurUuid
    @Column(name = "id", nullable = false)
    private String id;
    private Locale locale;
    private String facetName;
    @ManyToOne
    @JoinColumn(name = "aem_attribute_map_id", nullable = false)
    private TurAemAttributeMapping turAemAttributeMapping;
}
