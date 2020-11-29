package nz.xinsolutions.editing.helptext;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.util.string.Strings;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.render.RenderPlugin;

/**
 * Author: Marnix Kok <marnix@xinsolutions.co.nz>
 *
 * Purpose:
 *
 *      To just render a help text in the document.
 */
public class HelpTextTemplatePlugin extends RenderPlugin<String> {

    public HelpTextTemplatePlugin(final IPluginContext context, final IPluginConfig config) {
        super(context, config);
        String helpText = config.getString("text");
        add(new Label("value", new NewLinesToBrModel(helpText)).setEscapeModelStrings(false));

    }


    private static class NewLinesToBrModel extends AbstractReadOnlyModel<String> {

        String text;

        NewLinesToBrModel(String text) {
            this.text = text;
        }

        @Override
        public String getObject() {
            if (text == null) {
                return null;
            }
            return Strings.replaceAll(text, "\n", "<br/>").toString();
        }
    }
}