package nz.xinsolutions.extensions;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.ResourceReference;
import org.hippoecm.addon.workflow.StdWorkflow;
import org.hippoecm.frontend.plugins.standards.icon.HippoIcon;
import org.hippoecm.frontend.plugins.standards.list.resolvers.CssClass;
import org.hippoecm.frontend.plugins.standards.list.resolvers.TitleAttribute;
import org.hippoecm.frontend.skin.Icon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class overrides the default StdWorkflow class used to create workflow menu items in the
 * Hippo CMS interface and allows users to forward to external applications in a new tab to do
 * more specific operations on them.
 */
public class JavascriptEnabledStdWorkflow extends StdWorkflow {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(JavascriptEnabledStdWorkflow.class);

    /**
     * Icon constant
     */
    private static final String ICON_ID = "icon";

    /**
     * Contains menu item title
     */
    private String title;

    /**
     * Contains enumeration identifier of the icon used in the item
     */
    private String iconId;

    /**
     * What submenu is this item a part of?
     */
    private String subMenu;

    /**
     * Initialise the data-members of this class
     *
     * @param id            is the identifier of this menu item
     * @param subMenu       is the submenu to fit the element in
     * @param iconId        is the icon identifier used for the item (look at Icon enum)
     * @param title         is the label used in the menu item
     * @param url           is the URL to send the user to when they click it.
     */
    public JavascriptEnabledStdWorkflow(String id, String subMenu, String iconId, String title, String url, String docPath) {

        super(id, id);

        this.iconId = iconId;
        this.title = title;
        this.subMenu = subMenu;

        LOG.debug("Initialising the javascript enabled workflow item: {} opens {}", this.title, url);

        url = replaceUrlMarkers(url, docPath);

        add(getTextLabelComponent(url));
        add(getIconComponent(url));
        add(onClickAttribute(url));


    }

    private String replaceUrlMarkers(String url, String docPath) {
        if (StringUtils.isNotBlank(url)) {
            url = url.replace("{path}", docPath);
        }
        return url;
    }

    /**
     * This method instantiates an ActionDisplay, which seems to be a defered renderer
     * for an item. It creates an icon.
     *
     * @param url       is the url to redirect people to when they click the icon
     * @return          the action display that sets up an icon
     */
    protected ActionDisplay getIconComponent(String url) {
        return new ActionDisplay("icon") {
            @Override
            protected void initialize() {

                add(CssClass.append(new AbstractReadOnlyModel<String>() {
                    @Override
                    public String getObject() {
                        return JavascriptEnabledStdWorkflow.this.isEnabled() ? "icon-enabled" : "icon-disabled";
                    }
                }));

                add(onClickAttribute(url));

                Component icon = getIcon(ICON_ID);
                if (icon == null) {
                    if (getIcon() != null) {
                        // Legacy custom override
                        icon = HippoIcon.fromResourceModel(ICON_ID, new LoadableDetachableModel<ResourceReference>() {
                            @Override
                            protected ResourceReference load() {
                                return getIcon();
                            }
                        });
                    } else {
                        icon = HippoIcon.fromSprite(ICON_ID, Icon.GEAR);
                    }
                }
                add(icon);
            }
        };
    }


    /**
     * This method instantiates an ActionDisplay, which seems to be a defered renderer
     * for an item. It creates a text label.
     *
     * @param url       is the url to redirect people to when they click the icon
     * @return          the action display that sets up an icon
     */
    protected ActionDisplay getTextLabelComponent(String url) {
        return new ActionDisplay("text") {

            @Override
            protected void initialize() {
                IModel<String> title = getTitle();
                Label titleLabel = new Label("text", title);
                titleLabel.add(TitleAttribute.set(getTooltip()));
                add(titleLabel);

                // add javascript event to go to url to the parent's parent container.
                add(onClickAttribute(url));
            }
        };
    }

    private AttributeAppender onClickAttribute(String url) {
        if (StringUtils.isNotBlank(url)) {
            return AttributeModifier.append("onclick", "javascript: window.open('" + url + "');");
        }

        return AttributeModifier.append("onclick", "javascript: alert('No `url` set for toolbar button');");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IModel<String> getTitle() {
        return new Model<>(title);
    }

    /**
     * {@inheritDoc}
     */
    public String getSubMenu() {
        return subMenu;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Component getIcon(String id) {
        return HippoIcon.fromSprite(id, Icon.valueOf(iconId));
    }
}
