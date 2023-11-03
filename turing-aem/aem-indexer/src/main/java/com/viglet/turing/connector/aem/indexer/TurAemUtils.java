package com.viglet.turing.connector.aem.indexer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class TurAemUtils {
    public static boolean hasProperty(Node node, String property) throws RepositoryException {
        return node.hasProperty(property) && node.getProperty(property) != null;
    }
}