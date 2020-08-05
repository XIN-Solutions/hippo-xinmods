package nz.xinsolutions.queries.engine.interpret;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *  Describes the state our filter is in, so that we can make correct decisions regarding
 *  the way filters are created and compounded.
 *
 */
public enum CompoundBehaviour {

    /**
     * The compoound behaviour equals that of the very first WHERE statement
     */
    Toplevel,

    /**
     * We're currently inside an 'or' expression
     */
    Or,

    /**
     * We're currently inside an 'and' expression
     */
    And


}
