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

package com.viglet.turing.connector.aem.commons.ext;

import com.viglet.turing.connector.aem.commons.TurAemCommonsUtils;
import com.viglet.turing.connector.aem.commons.TurAemObject;
import com.viglet.turing.connector.aem.commons.context.TurAemSourceContext;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Slf4j
public class TurAemExtDeltaDate implements TurAemExtDeltaDateInterface {
    @Override
    public Date consume(TurAemObject aemObject, TurAemSourceContext turAemSourceContext) {
		log.debug("Executing TurAemExtDeltaDate");
		return TurAemCommonsUtils.getDeltaDate(aemObject);
    }

}
