package nz.xinsolutions.rest.model;

import java.util.Map;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *      Describes a request for writing an item into a collection.
 *
 */
public class CollectionsWriteRequest {

    ItemSaveMode saveMode;
    Map<String, ItemProperty> values;

    // -------------------------------------------------------
    //      Getters and Setters
    // -------------------------------------------------------

    public ItemSaveMode getSaveMode() {
        return saveMode;
    }

    public void setSaveMode(ItemSaveMode saveMode) {
        this.saveMode = saveMode;
    }

    public Map<String, ItemProperty> getValues() {
        return values;
    }

    public void setValues(Map<String, ItemProperty> values) {
        this.values = values;
    }
}
