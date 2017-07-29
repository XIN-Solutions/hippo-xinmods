package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoBean;
import nz.xinsolutions.beans.GenericImageSet;

@HippoEssentialsGenerated(internalName = "xinmods:siteconfig")
@Node(jcrType = "xinmods:siteconfig")
public class Siteconfig extends BaseDocument {
    @HippoEssentialsGenerated(internalName = "xinmods:siteName")
    public String getSiteName() {
        return getProperty("xinmods:siteName");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:headerMenu")
    public HippoBean getHeaderMenu() {
        return getLinkedBean("xinmods:headerMenu", HippoBean.class);
    }

    @HippoEssentialsGenerated(internalName = "xinmods:footerMenu")
    public HippoBean getFooterMenu() {
        return getLinkedBean("xinmods:footerMenu", HippoBean.class);
    }

    @HippoEssentialsGenerated(internalName = "xinmods:logo")
    public GenericImageSet getLogo() {
        return getLinkedBean("xinmods:logo", GenericImageSet.class);
    }

    @HippoEssentialsGenerated(internalName = "xinmods:favicon")
    public GenericImageSet getFavicon() {
        return getLinkedBean("xinmods:favicon", GenericImageSet.class);
    }

    @HippoEssentialsGenerated(internalName = "xinmods:homepage")
    public HippoBean getHomepage() {
        return getLinkedBean("xinmods:homepage", HippoBean.class);
    }
}
