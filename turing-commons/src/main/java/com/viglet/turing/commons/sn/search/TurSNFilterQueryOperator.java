package com.viglet.turing.commons.sn.search;

import java.io.Serializable;

public enum TurSNFilterQueryOperator implements Serializable {
    AND {
        @Override
        public String toString() {
            return "AND";
        }
    },
    OR {
        @Override
        public String toString() {
            return "OR";
        }
    },
    NONE {
        @Override
        public String toString() {
            return "NONE";
        }
    }
}
