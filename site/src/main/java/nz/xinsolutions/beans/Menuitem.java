package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;
import nz.xinsolutions.beans.GenericImageSet;
import org.hippoecm.hst.content.beans.standard.HippoBean;

/** 
 * TODO: Beanwriter: Failed to create getter for node type: hippo:compound
 */
@HippoEssentialsGenerated(internalName = "xinmods:menuitem")
@Node(jcrType = "xinmods:menuitem")
public class Menuitem extends HippoCompound {
    @HippoEssentialsGenerated(internalName = "xinmods:item")
    public String getItem() {
        return getProperty("xinmods:item");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:externalUrl")
    public String getExternalUrl() {
        return getProperty("xinmods:externalUrl");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:openInNewWindow")
    public Boolean getOpenInNewWindow() {
        return getProperty("xinmods:openInNewWindow");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:icon")
    public GenericImageSet getIcon() {
        return getLinkedBean("xinmods:icon", GenericImageSet.class);
    }

    @HippoEssentialsGenerated(internalName = "xinmods:internalLink")
    public HippoBean getInternalLink() {
        return getLinkedBean("xinmods:internalLink", HippoBean.class);
    }
}
