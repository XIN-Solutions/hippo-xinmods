package org.hippoecm.frontend.plugins.cms.admin.updater

import org.onehippo.repository.update.BaseNodeUpdateVisitor
import javax.jcr.Node
import javax.jcr.RepositoryException
import javax.jcr.Session

/**
 * Update the xinmods:collectionitem elements with a proper hippo:paths
 * value. This is useful when you import new content that was created in a different environment.
 *
 * Select "Xpath query" with value `//element(*, xinmods:collectionitem)`
 */
class UpdateCollectionItems extends BaseNodeUpdateVisitor {

    Session session;
    String basePath;

    /**
     * Called when initialising
     */
    void initialize(Session session) {
        this.session = session;
        this.basePath = parametersMap.get("basePath") ?: null;

        if (this.basePath) {
            log.debug "Will update collection item nodes under ${basePath}"
        }
    }

    boolean logSkippedNodePaths() {
        return false // don't log skipped node paths
    }

    boolean skipCheckoutNodes() {
        return false // return true for readonly visitors and/or updates unrelated to versioned content
    }

    Node firstNode(final Session session) throws RepositoryException {
        return null // implement when using custom node selection/navigation
    }

    Node nextNode() throws RepositoryException {
        return null // implement when using custom node selection/navigation
    }

    boolean doUpdate(Node node) {

        // skip over paths we dont care about
        if (this.basePath && !node.path.startsWith(this.basePath)) {
            return;
        }

        def paths = this.getAncestors(node)
        log.debug "Updating node ${node.path}"
        node.setProperty("hippo:paths", paths as String[])
        return true
    }

    boolean undoUpdate(Node node) {
        throw new UnsupportedOperationException('Updater does not implement undoUpdate method')
    }


    /**
     * Get the ancestor uuids
     */
    def getAncestors(Node node) {
        Node current = node

        def allPaths = [];

        while (true) {
            allPaths << current.getIdentifier();

            if (current.isSame(session.rootNode)) {
                break;
            }
            current = current.parent;
        }

        return allPaths
    }

}