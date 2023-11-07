package com.viglet.turing.connector.aem.indexer;

import ch.qos.logback.classic.Level;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.viglet.turing.client.sn.TurSNServer;
import com.viglet.turing.client.sn.credentials.TurUsernamePasswordCredentials;
import com.viglet.turing.client.sn.job.TurSNJobAction;
import com.viglet.turing.client.sn.job.TurSNJobItem;
import com.viglet.turing.client.sn.job.TurSNJobItems;
import com.viglet.turing.client.sn.job.TurSNJobUtils;
import com.viglet.turing.connector.aem.indexer.conf.AemHandlerConfiguration;
import com.viglet.turing.connector.aem.indexer.ext.ExtContentInterface;
import com.viglet.turing.connector.cms.beans.*;
import com.viglet.turing.connector.cms.config.IHandlerConfiguration;
import com.viglet.turing.connector.cms.config.TurSNSiteConfig;
import com.viglet.turing.connector.cms.mappers.CTDMappings;
import com.viglet.turing.connector.cms.mappers.MappingDefinitions;
import com.viglet.turing.connector.cms.mappers.MappingDefinitionsProcess;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.apache.jackrabbit.JcrConstants.JCR_PRIMARYTYPE;

public class TurAEMIndexerTool {

    public static final String CONTENT_FRAGMENT = "content-fragment";
    private static final JCommander jCommander = new JCommander();
    private static final Logger logger = LoggerFactory.getLogger(TurAEMIndexerTool.class);
    public static final String STATIC_FILE = "static-file";

    @Parameter(names = {"--host",
            "-h"}, description = "The host on which Content Management server is installed.", required = true)
    private String hostAndPort = null;

    @Parameter(names = {"--username",
            "-u"}, description = "A username to log in to the Content Management Server.", required = true)
    private String username = null;

    @Parameter(names = {"--password", "-p"}, description = "The password for the user name.", required = true)
    private String password = null;

    @Parameter(names = {"--all", "-a"}, description = "Index all instances of all content types and object types.")
    private boolean allObjectTypes = false;

    @Parameter(names = {"--content-type",
            "-c"}, description = "The XML name of the content type or object type whose instances are to be indexed.")
    private String contentType = null;

    @Parameter(names = {"--guids",
            "-g"}, description = "The path to a file containing the GUID(s) of content instances or static files to be indexed.")
    private String guidFilePath = null;

    @Parameter(names = {"--site-path", "-s"}, description = "AEM site path.", required = false)
    private String sitePath = "/content/we-retail";

    @Parameter(names = "--delivered", description = "Publish delivery or author site", help = true)
    private boolean delivered = false;

    @Parameter(names = {"--page-size",
            "-z"}, description = "The page size. After processing a page the processed count is written to an offset file."
            + " This helps the indexer to resume from that page even after failure. ")
    private int pageSize = 50;

    @Parameter(names = "--debug", description = "Change the log level to debug", help = true)
    private boolean debug = false;

    @Parameter(names = "--property", description = "Property file location path", help = true)
    private String propertyPath = "turing-aem.properties";
    @Parameter(names = "--sitemap", description = "Connect to AEM using sitemap", help = true)
    private boolean sitemap = false;
    @Parameter(names = "--show-output", description = "Property file location path", help = true)
    private boolean showOutput = false;
    @Parameter(names = "--help", description = "Print usage instructions", help = true)
    private boolean help = false;

    private static final String CQ_PAGE = "cq:Page";
    private static final String DAM_ASSET = "dam:Asset";
    private static int processed = 0;
    private static int currentPage = 0;
    private static long start;
    private AemHandlerConfiguration config = null;
    private String siteName;

    public static void main(String... argv) {
        TurAEMIndexerTool turAEMIndexerTool = new TurAEMIndexerTool();
        jCommander.addObject(turAEMIndexerTool);
        try {
            jCommander.parse(argv);

            if (turAEMIndexerTool.debug) {
                ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.
                        getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
                root.setLevel(Level.DEBUG);
            }

            if (turAEMIndexerTool.help) {
                jCommander.usage();
                return;
            }

            jCommander.getConsole().println("Viglet Turing AEM Indexer Tool.");

            turAEMIndexerTool.run();
        } catch (ParameterException e) {
            logger.info("Error: " + e.getLocalizedMessage());
            jCommander.usage();
        }
    }

    private void run() {
        // createPersistence();

        config = new AemHandlerConfiguration(propertyPath);
        try {
            this.getRead();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void createPersistence() {
        String createTableSQL = "CREATE TABLE indexing (" +
                "id  VARCHAR(255) PRIMARY KEY," +
                "date DATE," +
                "status VARCHAR(255));";

        try (Connection connection = DriverManager.
                getConnection("jdbc:h2:./store/db/turing-aem-db", "sa", "");
             Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void getRead() throws Exception {
        if (isCTDIntoMapping(contentType, config)) {
            if (sitemap) {
                getNodeFromJson(sitePath);
            } else {
                Repository repository = JcrUtils.getRepository(hostAndPort + "/crx/server");
                Session session = repository.login(new SimpleCredentials(username, password.toCharArray()));
                try {
                    Node node = session.getNode(sitePath);
                    AemSite aemSite = new AemSite(node);
                    siteName = aemSite.getTitle();
                    start = System.currentTimeMillis();
                    getNode(node);
                    long elapsed = System.currentTimeMillis() - start;
                    jCommander.getConsole().println(String.format("%d items processed in %dms", processed, elapsed));
                } finally {
                    session.logout();
                }
            }
        } else {
            jCommander.getConsole()
                    .println(String.format("%s type is not configured in CTD Mapping XML file.", contentType));
        }
    }

    public static String ordinal(int i) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];

        }
    }

    private void getNodeFromJson(String nodePath) {

        try {
            URL url = new URL(hostAndPort + "/bin/turing-sitemap.json" + sitePath);
            String json = IOUtils.toString(url, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);
            if (!jsonArray.isEmpty() && (nodePath.startsWith("/content") || nodePath.equals("/"))) {
                jsonArray.forEach(item -> {
                    JSONObject jsonObject = (JSONObject) item;
                    String jsonUrl = jsonObject.getString("url");
                    if (processed == 0) {
                        currentPage++;
                        jCommander.getConsole().println(String.format("Processing %s item",
                                ordinal((currentPage * pageSize) - pageSize + 1)));
                    }
                    if (processed >= pageSize) {
                        long elapsed = System.currentTimeMillis() - start;
                        jCommander.getConsole()
                                .println(String.format("%d items processed in %dms", processed, elapsed));
                        processed = 0;
                        start = System.currentTimeMillis();
                    } else {
                        processed++;
                    }
                    AemObject aemObject = new AemObject(jsonUrl);
                    CTDMappings ctdMappings = getCTDMappingMap(config).get(contentType);
                    if (!delivered || aemObject.isDelivered()) {
                        final List<TurAttrDef> extAttributes;
                        try {
                            extAttributes = runCustomClassFromContentType(ctdMappings, aemObject);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            indexObject(aemObject, extAttributes);
                        } catch (RepositoryException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    getNodeFromJson(jsonUrl);
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void getNode(Node node) {
        try {
            if (node.hasNodes() && (node.getPath().startsWith("/content") || node.getPath().equals("/"))) {
                NodeIterator nodeIterator = node.getNodes();
                while (nodeIterator.hasNext()) {

                    Node nodeChild = nodeIterator.nextNode();
                    if (hasContentType(nodeChild, contentType) || contentType == null) {
                        if (processed == 0) {
                            currentPage++;
                            jCommander.getConsole().println(String.format("Processing %s item",
                                    ordinal((currentPage * pageSize) - pageSize + 1)));
                        }
                        if (processed >= pageSize) {
                            long elapsed = System.currentTimeMillis() - start;
                            jCommander.getConsole()
                                    .println(String.format("%d items processed in %dms", processed, elapsed));
                            processed = 0;
                            start = System.currentTimeMillis();
                        } else {
                            processed++;
                        }
                        AemObject aemObject = new AemObject(nodeChild);
                        CTDMappings ctdMappings = getCTDMappingMap(config).get(contentType);
                        if (!delivered || aemObject.isDelivered()) {
                            final List<TurAttrDef> extAttributes = runCustomClassFromContentType(ctdMappings, aemObject);
                            switch (Objects.requireNonNull(contentType)) {
                                case CQ_PAGE:
                                    indexObject(aemObject, extAttributes);
                                    break;
                                case DAM_ASSET:
                                    if (ctdMappings.getSubType() != null) {
                                        if (ctdMappings.getSubType().equals(CONTENT_FRAGMENT)
                                                && aemObject.isContentFragment()) {
                                            indexObject(new AemObject(nodeChild, "data/master"), extAttributes);
                                        } else if (ctdMappings.getSubType().equals(STATIC_FILE)) {
                                            indexObject(new AemObject(nodeChild, "metadata"), extAttributes);
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                    if (nodeChild.hasNodes()) {
                        getNode(nodeChild);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private List<TurAttrDef> runCustomClassFromContentType(CTDMappings ctdMappings, AemObject aemObject) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        List<TurAttrDef> extAttributes = new ArrayList<>();
        if (ctdMappings.getClassName() != null) {
            Object extAttribute = Class.forName(ctdMappings.getClassName())
                    .getDeclaredConstructor().newInstance();
            extAttributes = ((ExtContentInterface) extAttribute)
                    .consume(aemObject, config);
        }
        return extAttributes;
    }

    private void indexObject(AemObject aemObject, List<TurAttrDef> extAttributes) throws RepositoryException {
        MappingDefinitions mappingDefinitions = MappingDefinitionsProcess.getMappingDefinitions(config);
        List<TurAttrDef> turAttrDefList = prepareAttributeDefs(aemObject, config, mappingDefinitions);
        turAttrDefList.addAll(extAttributes);
        TurSNSiteConfig turSNSiteConfig = config.getDefaultSNSiteConfig();
        Map<String, Object> attributes = new HashMap<>();
        String locale = config.getLocaleByPath(turSNSiteConfig.getName(), aemObject.getNode().getPath());
        final TurSNJobItem turSNJobItem = new TurSNJobItem(TurSNJobAction.CREATE,
                locale);
        for (TurAttrDef turAttrDef : turAttrDefList) {
            String attributeName = turAttrDef.getTagName();
            turAttrDef.getMultiValue().forEach(attributeValue -> {
                if (attributes.containsKey(attributeName)) {
                    if (!(attributes.get(attributeName) instanceof ArrayList)) {
                        attributeAsList(attributes, attributeName, attributeValue);
                        turSNJobItem.setAttributes(attributes);
                    } else {
                        attributeAsObject(attributes, attributeName, attributeValue);
                    }
                } else {
                    attributes.put(turAttrDef.getTagName(), attributeValue);
                }
            });
        }
        attributes.put("site", siteName);

        turSNJobItem.setAttributes(attributes);
        TurSNJobItems turSNJobItems = new TurSNJobItems();
        turSNJobItems.add(turSNJobItem);
        TurUsernamePasswordCredentials credentials = new TurUsernamePasswordCredentials(config.getLogin(),
                config.getPassword());
        TurSNServer turSNServer;
        try {
            turSNServer = new TurSNServer(new URL(config.getTuringURL()), turSNSiteConfig.getName(),
                    locale, credentials);
            TurSNJobUtils.importItems(turSNJobItems, turSNServer, showOutput);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static boolean isCTDIntoMapping(String contentTypeName, IHandlerConfiguration config) {
        TurCTDMappingMap mappings = getCTDMappingMap(config);
        CTDMappings ctdMappings = mappings.get(contentTypeName);
        return ctdMappings != null;

    }

    public static TurCTDMappingMap getCTDMappingMap(IHandlerConfiguration config) {
        MappingDefinitions mappingDefinitions = MappingDefinitionsProcess.getMappingDefinitions(config);
        return mappingDefinitions.getMappingDefinitions();

    }

    private static List<TurAttrDef> prepareAttributeDefs(AemObject aemObject, IHandlerConfiguration config,
                                                         MappingDefinitions mappingDefinitions) {
        CTDMappings ctdMappings = mappingDefinitions.getMappingByContentType(aemObject.getType());
        List<TurAttrDef> attributesDefs = new ArrayList<>();

        for (String tag : ctdMappings.getTagList()) {

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("generateXMLToIndex: Tag: %s", tag));
            }
            for (TuringTag turingTag : ctdMappings.getTuringTagMap().get(tag)) {
                if (tag != null && turingTag != null && turingTag.getTagName() != null) {
                    TurAttrDefContext turAttrDefContext = new TurAttrDefContext(aemObject, turingTag, config,
                            mappingDefinitions);
                    try {
                        List<TurAttrDef> attributeDefsXML = TurAEMAttrXML.attributeXML(turAttrDefContext);
                        // Unique
                        if (turingTag.isSrcUniqueValues()) {
                            attributesDefs.add(getTurAttrDefUnique(turingTag, attributeDefsXML));
                        } else {
                            attributesDefs.addAll(attributeDefsXML);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        return attributesDefs;
    }

    private static TurAttrDef getTurAttrDefUnique(TuringTag turingTag, List<TurAttrDef> attributeDefsXML) {
        TurMultiValue multiValue = new TurMultiValue();
        for (TurAttrDef turAttrDef : attributeDefsXML) {
            for (String singleValue : turAttrDef.getMultiValue()) {
                if (!multiValue.contains(singleValue)) {
                    multiValue.add(singleValue);
                }
            }
        }
        return new TurAttrDef(turingTag.getTagName(), multiValue);
    }

    private static void attributeAsObject(Map<String, Object> attributes, String nodeName, String nodes) {
        @SuppressWarnings("unchecked")
        List<Object> attributeValues = (List<Object>) attributes.get(nodeName);
        attributeValues.add(nodes);
        attributes.put(nodeName, attributeValues);
    }

    private static void attributeAsList(Map<String, Object> attributes, String nodeName, String attributeValue) {
        List<Object> attributeValues = new ArrayList<>();
        attributeValues.add(attributes.get(nodeName));
        attributeValues.add(attributeValue);

        attributes.put(nodeName, attributeValues);
    }

    private static boolean hasContentType(Node nodeChild, String primaryType)
            throws RepositoryException {
        return primaryType != null && nodeChild.getProperty(JCR_PRIMARYTYPE).getString().equals(primaryType);
    }
}
