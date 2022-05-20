package org.hippoecm.frontend.plugins.cms.dev.updater

import org.onehippo.repository.update.BaseNodeUpdateVisitor

import javax.jcr.Node

/**
 * Taken from the Bloomreach website:
 *
 * https://documentation.bloomreach.com/12/library/concepts/update/groovy-update-script-examples.html#Create_security_domains
 * Example 3: Create security domains for groups to only be able to read/author/edit in a list of CMS locations
 */
class CreateLocationAuthorEditorSecurityModel extends BaseNodeUpdateVisitor {

    //This will determine the name of the groups and the domain rules
    String DOMAIN_NAME = "Site2"

    //domain-name-authors will have content creation privileges in these locations and editors will have content creation and publication rights. Both editors and authors will only see content in these locations
    String[] DOMAIN_LOCATIONS = [
            "content/documents/site2",
            "content/assets/site2",
            "content/gallery/site2"
    ]

    boolean doUpdate(Node node) {

        log.debug "Creating/updating domain " + DOMAIN_NAME

        String domainName = DOMAIN_NAME.toLowerCase().replaceAll(" ", "-");

        boolean valid = true

        //If not all the folders exist then stop and log an error
        valid = checkFoldersExist(node, DOMAIN_LOCATIONS)

        if (!valid) {
            log.error "Not all domain locations exist. Please ensure all domain locations are present before executing this script."
        }

        //create the domain-name-authors and domain-name editors groups
        if (valid) {
            valid = createGroups(node, domainName)
        }

        //create domains with readwrite privileges
        if (valid) {
            valid = createReadWriteDomains(node, domainName)
        }

        //create domains with author privileges
        if (valid) {
            valid = createAuthorDomains(node, domainName)
        }

        //create domains with editor privileges
        if (valid) {
            valid = createEditorDomains(node, domainName)
        }

        return valid
    }

    /**
     * Check if the node has the list of paths provided
     * @param node the node to check from
     * @param locations the relative paths
     * @return true iff node has all the relative paths in locations[]
     */
    boolean checkFoldersExist(Node node, String[] locations) {

        boolean validated = true

        for (String location in locations) {
            validated &= node.hasNode(location)
        }

        return validated

    }

    /**
     * Create editor and author groups
     * @param node the root node /
     * @param domainName the name of the domain to create the groups for
     * @return true if no errors
     */
    boolean createGroups(Node node, String domainName) {

        Node groupsNode = node.getNode("hippo:configuration/hippo:groups")

        String editors = domainName + "-editors"
        String authors = domainName + "-authors"

        if (!groupsNode.hasNode(editors)) {
            log.debug("Creating group " + editors)

            Node editorGroup = groupsNode.addNode(editors, "hipposys:group")

            editorGroup.setProperty("hipposys:securityprovider", "internal")
        }

        if (!groupsNode.hasNode(authors)) {
            log.debug("Creating group " + authors)

            Node authorGroup = groupsNode.addNode(authors, "hipposys:group")

            authorGroup.setProperty("hipposys:securityprovider", "internal")
        }

        return true
    }

    /**
     * Remove editor and author groups
     * @param node the root node /
     * @param domainName the name of the domain to remove the groups for
     * @return true if no errors
     */
    boolean removeGroups(Node node, String domainName) {

        Node groupsNode = node.getNode("hippo:configuration/hippo:groups")

        String editors = domainName + "-editors"
        String authors = domainName + "-authors"

        if (groupsNode.hasNode(editors)) {

            log.debug("Removing group " + editors)

            Node editorGroup = groupsNode.getNode(editors)

            editorGroup.remove()
        }

        if (groupsNode.hasNode(authors)) {

            log.debug("Removing group " + authors)

            Node authorGroup = groupsNode.getNode(authors)

            authorGroup.remove()
        }

        return true
    }

    /**
     * Remove readwrite domains
     * @param node the root node /
     * @param domainName the name of the domain to remove the readwrite domains for
     * @return true if no errors
     */
    boolean removeReadWriteDomains(Node node, String domainName) {

        Node domainsNode = node.getNode("hippo:configuration/hippo:domains")

        String baseDomainPath = domainName + "-readwrite"

        Node baseDomain;

        if (domainsNode.hasNode(baseDomainPath)) {
            log.debug("Removing domain " + baseDomainPath)
            baseDomain = domainsNode.getNode(baseDomainPath)
            baseDomain.remove()
        }

        return true
    }

    /**
     * Remove author domains
     * @param node the root node /
     * @param domainName the name of the domain to remove the author domains for
     * @return true if no errors
     */
    boolean removeAuthorDomains(Node node, String domainName) {

        Node domainsNode = node.getNode("hippo:configuration/hippo:domains")

        String baseDomainPath = domainName + "-author"

        Node baseDomain;

        if (domainsNode.hasNode(baseDomainPath)) {
            log.debug("Removing domain " + baseDomainPath)
            baseDomain = domainsNode.getNode(baseDomainPath)
            baseDomain.remove()
        }

        return true
    }


    /**
     * Remove editor domains
     * @param node the root node /
     * @param domainName the name of the domain to remove the editor domains for
     * @return true if no errors
     */
    boolean removeEditorDomains(Node node, String domainName) {

        Node domainsNode = node.getNode("hippo:configuration/hippo:domains")

        String baseDomainPath = domainName + "-editor"

        Node baseDomain;

        if (domainsNode.hasNode(baseDomainPath)) {
            log.debug("Removing domain " + baseDomainPath)
            baseDomain = domainsNode.getNode(baseDomainPath)
            baseDomain.remove()
        }
    }

    /**
     * Create readwrite domains.
     * @param node the root node /
     * @param domainName the name of the domain to create the readwrite domains for
     * @return true if no errors
     */
    boolean createReadWriteDomains(Node node, String domainName) {

        Node baseDomain = createBaseDomain(node, domainName, "-readwrite")

        //These domains are needed to be able to create documents
        createTypeNameRule(baseDomain, "hippo:handle");
        createTypeNameRule(baseDomain, "hippo:facetsearch");
        createTypeNameRule(baseDomain, "hippo:facetselect");
        createTypeNameRule(baseDomain, "hippo:mirror");
        createTypeNameRule(baseDomain, "nt:unstructured");

        Node documentHolderRule = baseDomain.addNode("document-holder", "hipposys:domainrule")

        Node matchHolderRule = documentHolderRule.addNode("match_holder_with_username", "hipposys:facetrule")

        matchHolderRule.setProperty("hipposys:equals", true)
        matchHolderRule.setProperty("hipposys:filter", false)
        matchHolderRule.setProperty("hipposys:facet", "hippostd:holder")
        matchHolderRule.setProperty("hipposys:type", "String")
        matchHolderRule.setProperty("hipposys:value", "__user__")

        Node selfRule = baseDomain.addNode("self", "hipposys:domainrule")

        Node matchNodeNameRule = selfRule.addNode("match_nodename_with_username", "hipposys:facetrule")

        matchNodeNameRule.setProperty("hipposys:equals", true)
        matchNodeNameRule.setProperty("hipposys:filter", false)
        matchNodeNameRule.setProperty("hipposys:facet", "nodename")
        matchNodeNameRule.setProperty("hipposys:type", "Name")
        matchNodeNameRule.setProperty("hipposys:value", "__user__")

        Node typeHippoUserRule = selfRule.addNode("type-hippo-user", "hipposys:facetrule")

        typeHippoUserRule.setProperty("hipposys:equals", true)
        typeHippoUserRule.setProperty("hipposys:filter", false)
        typeHippoUserRule.setProperty("hipposys:facet", "nodetype")
        typeHippoUserRule.setProperty("hipposys:type", "Name")
        typeHippoUserRule.setProperty("hipposys:value", "hipposys:user")

        createRulesForParameterLocations(baseDomain)

        //create the authrole node
        if (!baseDomain.hasNode("hipposys:authrole")) {
            Node authRoleNode = baseDomain.addNode("hipposys:authrole", "hipposys:authrole")

            authRoleNode.setProperty("hipposys:role", "readwrite")
            String[] groups = [domainName + "-editors", domainName + "-authors"]
            authRoleNode.setProperty("hipposys:groups", groups)
        }

        return true
    }

    /**
     *
     * @param node the root node /
     * @param domainName the name of the domain to create the sub domains for
     * @param domainSuffix to identify the subdomain
     * @return
     */
    private Node createBaseDomain(Node node, String domainName, String domainSuffix) {
        Node domainsNode = node.getNode("hippo:configuration/hippo:domains")

        String baseDomainPath = domainName + domainSuffix

        Node baseDomain;

        //If exists, re-create in order to update
        if (domainsNode.hasNode(baseDomainPath)) {
            baseDomain = domainsNode.getNode(baseDomainPath)
            baseDomain.remove()
        }

        baseDomain = domainsNode.addNode(baseDomainPath, "hipposys:domain")
        baseDomain
    }

    /**
     * Create author domains.
     * @param node the root node /
     * @param domainName the name of the domain to create the author domains for
     * @return true if no errors
     */
    boolean createAuthorDomains(Node node, String domainName) {

        Node baseDomain = createBaseDomain(node, domainName, "-author")

        //These domains are needed to be able to create documents
        createNodeRule(baseDomain, "hippo:configuration/hippo:queries/hippo:templates/new-file-folder/hippostd:templates/asset gallery");
        createNodeRule(baseDomain, "hippo:configuration/hippo:queries/hippo:templates/new-folder/hippostd:templates/hippostd:folder");
        createNodeRule(baseDomain, "hippo:configuration/hippo:queries/hippo:templates/new-image-folder/hippostd:templates/image gallery");
        createNodeRule(baseDomain, "hippo:configuration/hippo:queries/hippo:templates/new-translated-folder/hippostd:templates/hippostd:folder");
        createNodetypeNameRule(baseDomain, "hippo:request");

        createRulesForParameterLocations(baseDomain)

        //create the authrole node
        if (!baseDomain.hasNode("hipposys:authrole")) {
            Node authRoleNode = baseDomain.addNode("hipposys:authrole", "hipposys:authrole")

            authRoleNode.setProperty("hipposys:role", "author")
            String[] groups = [domainName + "-editors", domainName + "-authors"]
            authRoleNode.setProperty("hipposys:groups", groups)
        }

        return true
    }

    private void createRulesForParameterLocations(Node baseDomain) {
        for (String location : DOMAIN_LOCATIONS) {
            String[] folders = location.split("/")
            String baseFolder = ""
            for (int i = 0; i < folders.length; i++) {

                baseFolder += "/" + folders[i]

                if (!"/".equals(folders[i]) && !"".equals(folders[i])) {
                    if (i == folders.length - 1) {
                        //leaf nodes need to be paths, this will give permissions to subfolders too
                        createPathRule(baseDomain, baseFolder)
                    } else {
                        //parent folders are mentioned alone
                        createNodeRule(baseDomain, baseFolder)
                    }
                }
            }
        }
    }

    /**
     * Create editor domains.
     * @param node the root node /
     * @param domainName the name of the domain to create the editor domains for
     * @return true if no errors
     */
    boolean createEditorDomains(Node node, String domainName) {
        Node baseDomain = createBaseDomain(node, domainName, "-editor")

        createNodetypeNameRule(baseDomain, "hippo:request");

        //Create rules for each parameter location
        for (String location : DOMAIN_LOCATIONS) {
            createPathRule(baseDomain, location)
        }

        //Create authrole
        if (!baseDomain.hasNode("hipposys:authrole")) {
            Node authRoleNode = baseDomain.addNode("hipposys:authrole", "hipposys:authrole")

            authRoleNode.setProperty("hipposys:role", "editor")
            String[] groups = [domainName + "-editors"]
            authRoleNode.setProperty("hipposys:groups", groups)
        }

        return true
    }

    /**
     * Create a node-by-uuid domain rule
     * @param node the domain node
     * @param nodePath the path of the node to create the rule for
     * @return true if no errors
     */
    boolean createNodeRule(Node node, String nodePath) {

        if (nodePath.startsWith("/")) {
            nodePath = nodePath.substring(1)
        }

        String nodeName = nodePath.replaceAll("/", "-").replaceAll(" ", "").replaceAll(":", "") + "-node"

        if (!node.hasNode(nodeName)) {
            log.debug("Creating node domain rule for " + nodePath)

            Node domainRule = node.addNode(nodeName, "hipposys:domainrule")

            Node facetRule = domainRule.addNode("node-by-uuid", "hipposys:facetrule")

            facetRule.setProperty("hipposys:equals", true)
            facetRule.setProperty("hipposys:filter", false)
            facetRule.setProperty("hipposys:facet", "jcr:uuid")
            facetRule.setProperty("hipposys:type", "Reference")
            facetRule.setProperty("hipposys:value", "/" + nodePath)


        } else {
            log.debug("Node domain rule for " + nodePath + " already exists, skipping")
        }
    }

    /**
     * Create a jcr primary type by name rule
     * @param node the domain node
     * @param typeName the jcr prinmarytype to match
     * @return true if no errors
     */
    boolean createTypeNameRule(Node node, String typeName) {

        String nodeName = "type-" + typeName.replaceAll("/", "-").replaceAll(" ", "").replaceAll(":", "-")

        if (!node.hasNode(nodeName)) {
            log.debug("Creating type name domain rule for " + typeName)

            Node domainRule = node.addNode(nodeName, "hipposys:domainrule")

            Node facetRule = domainRule.addNode(nodeName, "hipposys:facetrule")

            facetRule.setProperty("hipposys:equals", true)
            facetRule.setProperty("hipposys:filter", false)
            facetRule.setProperty("hipposys:facet", "jcr:primaryType")
            facetRule.setProperty("hipposys:type", "Name")
            facetRule.setProperty("hipposys:value", typeName)


        } else {
            log.debug("Node type name rule for " + typeName + " already exists, skipping")
        }
    }

    /**
     * Create a nodetype by name rule
     * @param node the domain node
     * @param typeName the jcr prinmarytype to match
     * @return true if no errors
     */
    boolean createNodetypeNameRule(Node node, String typeName) {

        String nodeName = "nodetype-" + typeName.replaceAll("/", "-").replaceAll(" ", "").replaceAll(":", "-")

        if (!node.hasNode(nodeName)) {
            log.debug("Creating type name domain rule for " + typeName)

            Node domainRule = node.addNode(nodeName, "hipposys:domainrule")

            Node facetRule = domainRule.addNode(nodeName, "hipposys:facetrule")

            facetRule.setProperty("hipposys:equals", true)
            facetRule.setProperty("hipposys:filter", false)
            facetRule.setProperty("hipposys:facet", "nodetype")
            facetRule.setProperty("hipposys:type", "Name")
            facetRule.setProperty("hipposys:value", typeName)


        } else {
            log.debug("Node type name rule for " + typeName + " already exists, skipping")
        }
    }

    /**
     * Create a path-by-uuid domain rule
     * @param node the domain node
     * @param nodePath the path of the node to create the rule for
     * @return true if no errors
     */
    boolean createPathRule(Node node, String nodePath) {

        if (nodePath.startsWith("/")) {
            nodePath = nodePath.substring(1)
        }

        String nodeName = nodePath.replaceAll("/", "-").replaceAll(" ", "").replaceAll(":", "") + "-node"

        if (!node.hasNode(nodeName)) {
            log.debug("Creating node domain rule for " + nodePath)

            Node domainRule = node.addNode(nodeName, "hipposys:domainrule")

            Node facetRule = domainRule.addNode("path-by-uuid", "hipposys:facetrule")

            facetRule.setProperty("hipposys:equals", true)
            facetRule.setProperty("hipposys:filter", false)
            facetRule.setProperty("hipposys:facet", "jcr:path")
            facetRule.setProperty("hipposys:type", "Reference")
            facetRule.setProperty("hipposys:value", "/" + nodePath)


        } else {
            log.debug("Node domain rule for " + nodePath + " already exists, skipping")
        }
    }

    /**
     * Delete groups and authors created by the script
     * @param node
     * @return true if no errors
     */
    boolean undoUpdate(Node node) {

        String domainName = DOMAIN_NAME.toLowerCase().replaceAll(" ", "-");

        boolean valid = removeGroups(node, domainName)

        valid &= removeReadWriteDomains(node, domainName)
        valid &= removeAuthorDomains(node, domainName)
        valid &= removeEditorDomains(node, domainName)

        return valid
    }

}