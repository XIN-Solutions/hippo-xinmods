package nz.xinsolutions.workflow;

import org.hippoecm.repository.api.WorkflowContext;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.standardworkflow.FolderWorkflowImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
public class XinFolderWorkflowImpl extends FolderWorkflowImpl {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XinFolderWorkflowImpl.class);

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
    public XinFolderWorkflowImpl(WorkflowContext context, Session userSession, Session rootSession, Node subject) throws RepositoryException {
        super(context, userSession, rootSession, subject);

        this.subject = subject;
        this.isAdmin = userSession.getUserID().equals("admin");
        this.rootSession = rootSession;
    }

    // ------------------------------------------------------------------------------------------------
    //      Override each operation the default workflow allows and make sure the rules
    //      are such that this node can perform the action.
    // ------------------------------------------------------------------------------------------------


    @Override
    public void delete(String name) throws WorkflowException, RepositoryException {
        if (!this.canAccessAction("delete", name)) {
            throw new XinWorkflowException("You are not allowed to delete this folder.");
        }
        super.delete(name);
    }

    @Override
    public void rename(String name, String newName) throws WorkflowException, RepositoryException {
        if (!this.canAccessAction("rename", name)) {
            throw new XinWorkflowException("You are not allowed to rename this folder.");
        }
        super.rename(name, newName);
    }


    /**
     * @return true if the action can be undertaken for a particular
     */
    protected boolean canAccessAction(String actionName, String childName) {

        try {
            LOG.info("Attempting to check access for action '{}' at path '{}'", actionName, this.subject.getPath());

            if (this.isAdmin) {
                LOG.info(".. it's an administrator, letting request through.");
                return true;
            }

            XinWorkflowPermissions wfPerms = new XinWorkflowPermissions(this.rootSession, this.subject.getNode(childName));
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
