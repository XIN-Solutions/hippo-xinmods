package nz.xinsolutions.rest.model;

/**
 * Author: Marnix Kok <marnix@elevate.net.nz>
 * <p>
 * Purpose:
 */
public class ItemProperty {

    Object value;
    ItemType type;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }
}
