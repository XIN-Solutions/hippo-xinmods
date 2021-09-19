package nz.xinsolutions.workflow;

import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.WorkflowContext;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.standardworkflow.DefaultWorkflowImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.rmi.RemoteException;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      To wrap the existing default workflow implementation with a mechanism
 *      that has some smarts regarding the actions that are allowed on
 *      documents of a type, with a mixin, or path of a certain regex
 *
 */
@SuppressWarnings("unused")
public class XinDefaultWorkflowImpl extends DefaultWorkflowImpl {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XinDefaultWorkflowImpl.class);

    /**
     * Node workflow subject
     */
    private Node subject;

    /**
     * The JCR root session
     */
    private Session rootSession;

    /**
     * True if it's an administrator
     */
    private boolean isAdmin;

    /**
     * {@inheritDoc}
     */
    public XinDefaultWorkflowImpl(WorkflowContext context, Session userSession, Session rootSession, Node subject) throws RepositoryException {
        super(context, userSession, rootSession, subject);

        if (subject.hasNode(subject.getName())) {
            this.subject = subject.getNode(subject.getName());
        }
        else {
            this.subject = subject;
        }

        this.isAdmin = userSession.getUserID().equals("admin");
        this.rootSession = rootSession;
    }

    // ------------------------------------------------------------------------------------------------
    //      Override each operation the default workflow allows and make sure the rules
    //      are such that this node can perform the action.
    // ------------------------------------------------------------------------------------------------

    @Override
    public void delete() throws WorkflowException, RepositoryException, RemoteException {
        if (!this.canAccessAction("delete")) {
            throw new XinWorkflowException("You are not allowed to delete this document.");
        }
        super.delete();
    }

    @Override
    public void archive() throws WorkflowException, RepositoryException, RemoteException {
        if (!this.canAccessAction("archive")) {
            throw new XinWorkflowException("You are not allowed to archive this document.");
        }
        super.archive();
    }

    @Override
    public void rename(String newName) throws WorkflowException, RepositoryException, RemoteException {
        if (!this.canAccessAction("rename")) {
            throw new XinWorkflowException("You are not allowed to rename this document.");
        }
        super.rename(newName);
    }

    @Override
    public void setDisplayName(String hippoName) throws WorkflowException, RepositoryException, RemoteException {
        if (!this.canAccessAction("displayName")) {
            throw new XinWorkflowException("You are not allowed to change the display name this document.");
        }
        super.setDisplayName(hippoName);
    }

    @Override
    public void copy(Document destination, String newName) throws RemoteException, WorkflowException, RepositoryException {
        if (!this.canAccessAction("copy")) {
            throw new XinWorkflowException("You are not allowed to copy this document.");
        }
        super.copy(destination, newName);
    }

    @Override
    public void move(Document destination, String newName) throws RemoteException, WorkflowException, RepositoryException {
        if (!this.canAccessAction("move")) {
            throw new XinWorkflowException("You are not allowed to move this document.");
        }
        super.move(destination, newName);
    }

    /**
     * @return true if the action can be undertaken for a particular
     */
    protected boolean canAccessAction(String actionName) {

        try {
            LOG.info("Attempting to check access for action '{}' at path '{}'", actionName, this.subject.getPath());

            if (this.isAdmin) {
                LOG.info(".. it's an administrator, letting request through.");
                return true;
            }

            XinWorkflowPermissions wfPerms = new XinWorkflowPermissions(this.rootSession, this.subject);
            return wfPerms.canPerformAction(actionName) == WorkflowOutcome.Granted;
        }
        catch (Exception ex) {
            LOG.error("Could not check access for action .. denying, caused by: ", ex);
            return false;
        }

    }


    /**
     * @return a new instance for us to check workflow permissions with
     */
    protected XinWorkflowPermissions newPermissionsInstance() {
        return new XinWorkflowPermissions(this.rootSession, this.subject);
    }

}
