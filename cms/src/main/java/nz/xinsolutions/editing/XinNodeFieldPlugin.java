package nz.xinsolutions.editing;

import org.apache.wicket.markup.repeater.Item;
import org.hippoecm.frontend.editor.plugins.field.*;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.IRenderService;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *      A small override to close the compound elements by default when editing.
 *      org.hippoecm.frontend.editor.plugins.field.NodeFieldPlugin
 *
 */
public class XinNodeFieldPlugin extends NodeFieldPlugin {

    private final FlagList collapsedItems = new FlagList();

    /**
     * Initialise data-members
     *
     * @param context
     * @param config
     */
    public XinNodeFieldPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);
    }


    @Override
    protected void populateViewItem(final Item<IRenderService> item, final JcrNodeModel model) {
        if (helper.isCompoundField()) {
            item.add(new CollapsibleFieldContainer("fieldContainer", item, this));
        } else {
            item.add(new FieldContainer("fieldContainer", item));
        }
    }


    /**
     * Populate the edit item.
     *
     * @param item
     * @param model
     */
    @Override
    protected void populateEditItem(final Item<IRenderService> item, final JcrNodeModel model) {

        if (helper.isCompoundField()) {

            final boolean isCollapsed = true; // collapsedItems.get(item.getIndex());

            item.add(new EditableCollapsibleFieldContainer("fieldContainer", item, model, this, isCollapsed) {
                @Override
                protected void onCollapse(final boolean isCollapsed) {
                    collapsedItems.set(item.getIndex(), isCollapsed);
                }
            });

        }
        else {
            item.add(new EditableNodeFieldContainer("fieldContainer", item, model, this));
        }

    }

}
