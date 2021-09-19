package nz.xinsolutions.workflow;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      Describes a workflow access rule, basically a set of instructions that indicate
 *      a way to evaluate the node that is the subject of workflow and the outcome of the
 *      workflow evaluation (grant or deny) when the rule matches the subject.
 *
 */
public class WorkflowAccessRule {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(WorkflowAccessRule.class);

    /**
     * Defaults
     */
    public static final String DEFAULT_DESCRIPTION = "workflowrule";

    /**
     * Interesting index numbers for description parts
     */
    public static final int IDX_OUTCOME = 0;
    public static final int IDX_EVALTYPE = 1;
    public static final int IDX_DESCRIPTION = 2;
    public static final String EVAL_NODE_TYPE = "type";
    public static final String EVAL_HAS_MIXIN = "mixin";
    public static final String EVAL_IN_PATH = "path";

    /**
     * Rule's outcome
     */
    private WorkflowOutcome outcome;

    /**
     * How to evaluate the rule? type, mixin or path-regex
     */
    private String evalType;

    /**
     * The identifier of the rule we can use for debugging.
     */
    private String description;

    /**
     * Matching values for this eval type
     */
    private List<String> matchingValues;

    /**
     * Valid rule
     */
    private boolean valid;

    /**
     * Interpret incoming node property as a workflow access rule
     *
     * @param nodeProperty
     */
    public WorkflowAccessRule(Property nodeProperty) throws RepositoryException {
        String name = nodeProperty.getName();

        // extract name parts
        String[] nameParts = name.split("\\.");
        if (nameParts.length < IDX_DESCRIPTION || nameParts.length > 3) {
            this.valid = false;
            return;
        }

        initialiseRuleMembers(nodeProperty, nameParts);
    }

    /**
     * Initialise the data members for this rule based on the property information.
     *
     * @param nodeProperty
     * @param nameParts
     * @throws RepositoryException
     */
    protected void initialiseRuleMembers(Property nodeProperty, String[] nameParts) throws RepositoryException {
        // extract information
        String outcomeName = nameParts[IDX_OUTCOME];
        String evalType = nameParts[IDX_EVALTYPE];
        this.description = nameParts.length == 3? nameParts[IDX_DESCRIPTION] : DEFAULT_DESCRIPTION;

        if (!initialiseOutcome(outcomeName)) return;
        if (!initialiseEvaluationType(evalType)) return;
        if (!initialiseMatchingValues(nodeProperty)) return;

        // everything extracted successfully
        this.valid = true;
    }


    /**
     * Initialise outcome field
     * @return true if it all went well
     */
    protected boolean initialiseOutcome(String outcomeName) {
        // set the outcome
        if (outcomeName.equals("deny")) {
            this.outcome = WorkflowOutcome.Denied;
        }
        else if (outcomeName.equals("granted")) {
            this.outcome = WorkflowOutcome.Granted;
        }
        else {
            LOG.error("Unknown outcome type `{}` for rule named `{}`", outcomeName, this.description);
            this.valid = false;
            return false;
        }
        return true;
    }

    /**
     * @return true if all went well
     */
    protected boolean initialiseEvaluationType(String evalType) {
        switch (evalType) {
            case EVAL_NODE_TYPE:
            case EVAL_HAS_MIXIN:
            case EVAL_IN_PATH:
                this.evalType = evalType;
                break;

            default:
                LOG.error("Unknown evaluation type `{}` for rule named `{}`", evalType, this.description);
                this.valid = false;
                return false;
        }
        return true;
    }

    /**
     * @return true if initialised properly
     * @throws RepositoryException
     */
    protected boolean initialiseMatchingValues(Property nodeProperty) throws RepositoryException {
        if (nodeProperty.isMultiple()) {
            this.matchingValues = (
                Arrays.stream(nodeProperty.getValues())
                    .map(this::getValueString)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
            );

            if (CollectionUtils.isEmpty(this.matchingValues)) {
                this.valid = false;
                return false;
            }
        }
        else {
            this.matchingValues = Collections.singletonList(nodeProperty.getString());
        }

        return true;
    }

    /**
     * Evaluate whether `subject` matches the matching rule described by this class.
     *
     * @param subject the node subject to compare the rule information against
     * @return true if the subject matches the node description.
     */
    public boolean matchesNode(Node subject) throws RepositoryException {

        switch (this.evalType) {
            case EVAL_NODE_TYPE: return this.matchesNodeType(subject);
            case EVAL_HAS_MIXIN: return this.matchesMixin(subject);
            case EVAL_IN_PATH: return this.matchesPath(subject);
        }

        throw new IllegalArgumentException("Invalid eval type " + this.evalType);
    }

    /**
     * @return true if the subject matches the regex
     */
    protected boolean matchesPath(Node subject) throws RepositoryException {
        String path = subject.getPath();
        for (String matchVal: this.matchingValues) {
            boolean matched = path.matches(matchVal);
            if (matched) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true if the subject has a mixin
     * @throws RepositoryException
     */
    protected boolean matchesMixin(Node subject) throws RepositoryException {

        NodeType[] types = subject.getMixinNodeTypes();

        for (NodeType type: types) {
            String name = type.getName();
            if (this.matchingValues.contains(name)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return true if matches the node type allowed by matching values
     * @throws RepositoryException
     */
    protected boolean matchesNodeType(Node subject) throws RepositoryException {
        return this.matchingValues.contains(subject.getPrimaryNodeType().getName());
    }


    // -------------------------------------------------------------------------------------
    //      Getters
    // -------------------------------------------------------------------------------------

    public String getEvalType() {
        return evalType;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getMatchingValues() {
        return matchingValues;
    }

    public WorkflowOutcome getOutcome() {
        return this.outcome;
    }

    /**
     * @return true if the rule description is a valid rule.
     */
    public boolean isValid() {
        return valid;
    }

    // -------------------------------------------------------------------------------------
    //      Helper functions
    // -------------------------------------------------------------------------------------


    /**
     * @return the string value and catch exceptions that then returns null
     */
    protected String getValueString(Value value) {
        try {
            return value.getString();
        }
        catch (Exception ex) {
            return null;
        }
    }


}
