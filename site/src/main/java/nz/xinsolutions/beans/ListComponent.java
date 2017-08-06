package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;
import org.hippoecm.hst.content.beans.standard.HippoBean;

@HippoEssentialsGenerated(internalName = "xinmods:listComponent")
@Node(jcrType = "xinmods:listComponent")
public class ListComponent extends HippoCompound {
    @HippoEssentialsGenerated(internalName = "xinmods:title")
    public String getTitle() {
        return getProperty("xinmods:title");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:tags")
    public String[] getTags() {
        return getProperty("xinmods:tags");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:tagSearchType")
    public String getTagSearchType() {
        return getProperty("xinmods:tagSearchType");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:titleLink")
    public HippoBean getTitleLink() {
        return getLinkedBean("xinmods:titleLink", HippoBean.class);
    }

    @HippoEssentialsGenerated(internalName = "xinmods:basePath")
    public HippoBean getBasePath() {
        return getLinkedBean("xinmods:basePath", HippoBean.class);
    }
}
