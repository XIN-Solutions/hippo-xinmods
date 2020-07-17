package nz.xinsolutions.extensions;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.breadcrumb.IBreadCrumbModel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hippoecm.frontend.plugins.cms.admin.AdminBreadCrumbPanel;

public class XinInjectionAdminPlugin extends AdminBreadCrumbPanel implements IHeaderContributor {

    /**
     * Title
     */
    private String title;

    /**
     * Injection information
     */
    private InjectDto inject;

    /**
     * Initialise data-members
     *
     * @param id              panel identifier
     * @param breadCrumbModel breadcrumb model
     * @param inject          injection information
     */
    public XinInjectionAdminPlugin(String id, IBreadCrumbModel breadCrumbModel, String title, InjectDto inject) {
        super(id, breadCrumbModel);

        setOutputMarkupId(true);
        setRenderBodyOnly(true);

        this.title = title;
        this.inject = inject;


    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IModel<String> getTitle(Component component) {
        return new Model<>(this.title);
    }


    @Override
    public void onRender() {
        getWebResponse().write(this.inject.getContentsString());
        super.onRender();
    }


    /**
     * Add the proper JS and CSS tags at the top.
     */
    @Override
    public void renderHead(IHeaderResponse response) {
        this.inject.renderHead(response);
    }
}
