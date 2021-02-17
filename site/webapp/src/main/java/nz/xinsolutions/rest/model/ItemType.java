package nz.xinsolutions.rest.model;

import javax.jcr.PropertyType;

/**
 * Author: Marnix Kok <marnix@elevate.net.nz>
 *
 * Purpose:
 *
 *      To describe the type of a property written to the JCR.
 *
 */
public enum ItemType {

    String(PropertyType.STRING),
    Long(PropertyType.LONG),
    Double(PropertyType.DOUBLE),
    Boolean(PropertyType.BOOLEAN),
    Date(PropertyType.DATE),

;

    private int jcrPropType;

    /**
     * Initialise data-members
     * @param jcrPropType
     */
    ItemType(int jcrPropType) {
        this.jcrPropType = jcrPropType;
    }

    public int getJcrPropType() {
        return jcrPropType;
    }
}
