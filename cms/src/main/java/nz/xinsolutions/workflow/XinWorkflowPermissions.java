package nz.xinsolutions.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;

import java.util.ArrayList;
import java.util.List;

import static nz.xinsolutions.workflow.WorkflowOutcome.Denied;
import static nz.xinsolutions.workflow.WorkflowOutcome.Granted;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      Helps determine whether a particular node can be operated on in a certain way.
 *
 */
public class XinWorkflowPermissions {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(XinWorkflowPermissions.class);

    public static final String OUTCOME_DENY = "deny";
    public static final String PROP_DEFAULT_OUTCOME = "defaultOutcome";
    public static final String WORKFLOW_ROOT_NODE = "/hippo:configuration/hippo:modules/xinmods/hippo:moduleconfig/workflow";

    /**
     * Subject node
     */
    private Node subject;

    /**
     * JCR session node to read configurations with
     */
    private Session session;

    /**
     * Initialise data-members
     *
     * @param session   the root session
     * @param subject   the subject node
     */
    public XinWorkflowPermissions(Session session, Node subject) {
        this.session = session;
        this.subject = subject;
    }


    /**
     * Determine whether an action can be performed by the subject supplied to the constructor.
     *
     * @param action the name of the action we're after.
     * @return the outcome of the workflow.
     * @throws RepositoryException
     */
    public WorkflowOutcome canPerformAction(String action) throws RepositoryException {

        Node actionNode = this.getWorkflowActionNode(action);

        // no such action? return default value.
        if (actionNode == null) {
            return this.defaultOutcome();
        }

        List<WorkflowAccessRule> rules = this.getRulesFromNode(actionNode);

        for (WorkflowAccessRule rule: rules) {

            // matches? return the workflow outcome to the callee.
            if (rule.matchesNode(subject)) {
                return rule.getOutcome();
            }

        }

        // nothing matches, so default outcome.
        return this.defaultOutcome();
    }

    /**
     * @return a list of workflow access rule instances based on the properties on this node
     * @throws RepositoryException
     */
    protected List<WorkflowAccessRule> getRulesFromNode(Node actionNode) throws RepositoryException {

        List<WorkflowAccessRule> rules = new ArrayList<>();

        PropertyIterator pIt = actionNode.getProperties();

        while (pIt.hasNext()) {
            Property prop = pIt.nextProperty();

            WorkflowAccessRule rule = new WorkflowAccessRule(prop);
            if (!rule.isValid()) {
                LOG.info("Skipping invalid rule: {}", prop.toString());
                continue;
            }

            // valid? let's add it to the list of rules.
            rules.add(rule);
        }

        // return the list
        return rules;
    }


    /**
     * @return the xinmods moduleconfig /workflow node.
     * @throws RepositoryException
     */
    protected Node getWorkflowRootNode() throws RepositoryException {
        String rootPath = WORKFLOW_ROOT_NODE;
        if (!this.session.nodeExists(rootPath)) {
            return null;
        }
        return this.session.getNode(rootPath);
    }



    /**
     * @return the xinmods moduleconfig /workflow node.
     * @throws RepositoryException
     */
    protected Node getWorkflowActionNode(String action) throws RepositoryException {
        String actionPath = WORKFLOW_ROOT_NODE + "/" + action;
        if (!this.session.nodeExists(actionPath)) {
            return null;
        }
        return this.session.getNode(actionPath);
    }


    /**
     * @return the workflow outcome
     */
    protected WorkflowOutcome defaultOutcome() throws RepositoryException {

        Node node = this.getWorkflowRootNode();
        if (node == null) {
            LOG.info("Couldn't find xinmods moduleconfig workflow root node, returning 'Granted'");
            return Granted;
        }

        if (!node.hasProperty(PROP_DEFAULT_OUTCOME)) {
            LOG.info("Couldn't find xinmods moduleconfig workflow root node's defaultOutcome property, returning 'Granted''");
            return Granted;
        }

        // get the property
        Property outcomeProp = node.getProperty(PROP_DEFAULT_OUTCOME);
        return outcomeProp.getString().equals(OUTCOME_DENY) ? Denied : Granted;
    }


}
