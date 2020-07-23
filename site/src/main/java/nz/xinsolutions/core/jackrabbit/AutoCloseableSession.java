package nz.xinsolutions.core.jackrabbit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.VersionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.AccessControlException;

/**
 * Simple wrapper object for auto closable jcr sessions, to use within try/catch statements.
 * The original session can be retrieved with <code>getSession()</code>.
 */
public class AutoCloseableSession implements AutoCloseable, Session {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AutoCloseableSession.class);

    private Session session;

    /**
     * Initialise data-members
     * @param wrapMe    is the session instance to wrap
     */
    public AutoCloseableSession(Session wrapMe) {
        this.session = wrapMe;
    }

    /**
     * Closing the JCR session if it's still alive
     * @throws Exception
     */
    @Override
    public void close() throws Exception {

        LOG.debug("Closing autocloseable session");
        if (this.session != null && this.session.isLive()) {
            this.session.logout();
        }
    }

    /**
     * @return the original session object
     */
    public Session getSession() {
        return session;
    }

    @Override
    public Repository getRepository() {
        return this.session.getRepository();
    }

    @Override
    public String getUserID() {
        return this.session.getUserID();
    }

    @Override
    public String[] getAttributeNames() {
        return this.session.getAttributeNames();
    }

    @Override
    public Object getAttribute(String name) {
        return this.session.getAttribute(name);
    }

    @Override
    public Workspace getWorkspace() {
        return this.session.getWorkspace();
    }

    @Override
    public Node getRootNode() throws RepositoryException {
        return this.session.getRootNode();
    }

    @Override
    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        return this.session.impersonate(credentials);
    }

    @Override
    public Node getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
        return this.session.getNodeByUUID(uuid);
    }

    @Override
    public Node getNodeByIdentifier(String id) throws ItemNotFoundException, RepositoryException {
        return this.session.getNodeByIdentifier(id);
    }

    @Override
    public Item getItem(String absPath) throws PathNotFoundException, RepositoryException {
        return this.session.getItem(absPath);
    }

    @Override
    public Node getNode(String absPath) throws PathNotFoundException, RepositoryException {
        return this.session.getNode(absPath);
    }

    @Override
    public Property getProperty(String absPath) throws PathNotFoundException, RepositoryException {
        return this.session.getProperty(absPath);
    }

    @Override
    public boolean itemExists(String absPath) throws RepositoryException {
        return this.session.itemExists(absPath);
    }

    @Override
    public boolean nodeExists(String absPath) throws RepositoryException {
        return this.session.nodeExists(absPath);
    }

    @Override
    public boolean propertyExists(String absPath) throws RepositoryException {
        return this.session.propertyExists(absPath);
    }

    @Override
    public void move(String srcAbsPath, String destAbsPath) throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException, LockException, RepositoryException {
        this.session.move(srcAbsPath, destAbsPath);
    }

    @Override
    public void removeItem(String absPath) throws VersionException, LockException, ConstraintViolationException, AccessDeniedException, RepositoryException {
        this.session.removeItem(absPath);
    }

    @Override
    public void save() throws AccessDeniedException, ItemExistsException, ReferentialIntegrityException, ConstraintViolationException, InvalidItemStateException, VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        this.session.save();
    }

    @Override
    public void refresh(boolean keepChanges) throws RepositoryException {
        this.session.refresh(keepChanges);
    }

    @Override
    public boolean hasPendingChanges() throws RepositoryException {
        return this.session.hasPendingChanges();
    }

    @Override
    public ValueFactory getValueFactory() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.session.getValueFactory();
    }

    @Override
    public boolean hasPermission(String absPath, String actions) throws RepositoryException {
        return this.session.hasPermission(absPath, actions);
    }

    @Override
    public void checkPermission(String absPath, String actions) throws AccessControlException, RepositoryException {
        this.session.checkPermission(absPath, actions);
    }

    @Override
    public boolean hasCapability(String methodName, Object target, Object[] arguments) throws RepositoryException {
        return this.session.hasCapability(methodName, target, arguments);
    }

    @Override
    public ContentHandler getImportContentHandler(String parentAbsPath, int uuidBehavior) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, RepositoryException {
        return this.session.getImportContentHandler(parentAbsPath, uuidBehavior);
    }

    @Override
    public void importXML(String parentAbsPath, InputStream in, int uuidBehavior) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, VersionException, InvalidSerializedDataException, LockException, RepositoryException {
        this.session.importXML(parentAbsPath, in, uuidBehavior);
    }

    @Override
    public void exportSystemView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException {
        this.session.exportSystemView(absPath, contentHandler, skipBinary, noRecurse);
    }

    @Override
    public void exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException {
        this.session.exportSystemView(absPath, out, skipBinary, noRecurse);
    }

    @Override
    public void exportDocumentView(String absPath, ContentHandler contentHandler, boolean skipBinary, boolean noRecurse) throws PathNotFoundException, SAXException, RepositoryException {
        this.session.exportDocumentView(absPath, contentHandler, skipBinary, noRecurse);
    }

    @Override
    public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse) throws IOException, PathNotFoundException, RepositoryException {
        this.session.exportDocumentView(absPath, out, skipBinary, noRecurse);
    }

    @Override
    public void setNamespacePrefix(String prefix, String uri) throws NamespaceException, RepositoryException {
        this.session.setNamespacePrefix(prefix, uri);
    }

    @Override
    public String[] getNamespacePrefixes() throws RepositoryException {
        return this.session.getNamespacePrefixes();
    }

    @Override
    public String getNamespaceURI(String prefix) throws NamespaceException, RepositoryException {
        return this.session.getNamespaceURI(prefix);
    }

    @Override
    public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException {
        return this.session.getNamespacePrefix(uri);
    }

    @Override
    public void logout() {
        this.session.logout();
    }

    @Override
    public boolean isLive() {
        return this.session.isLive();
    }

    @Override
    public void addLockToken(String lt) {
        this.session.addLockToken(lt);
    }

    @Override
    public String[] getLockTokens() {
        return this.session.getLockTokens();
    }

    @Override
    public void removeLockToken(String lt) {
        this.session.removeLockToken(lt);
    }

    @Override
    public AccessControlManager getAccessControlManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.session.getAccessControlManager();
    }

    @Override
    public RetentionManager getRetentionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return this.session.getRetentionManager();
    }
}
