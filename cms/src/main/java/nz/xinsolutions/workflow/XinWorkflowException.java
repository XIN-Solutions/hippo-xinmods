package nz.xinsolutions.workflow;

import org.hippoecm.repository.api.WorkflowException;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      A variation of the workflow exception that overrides toString so that the
 *      message in the frontend is cleaner.
 *
 */
public class XinWorkflowException extends WorkflowException {

    public XinWorkflowException(String message) {
        super(message);
    }

    public XinWorkflowException(String message, Exception reason) {
        super(message, reason);
    }

    public String toString() {
        return this.getMessage();
    }
}
