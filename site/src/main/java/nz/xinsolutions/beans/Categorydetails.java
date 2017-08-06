package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;
import org.hippoecm.hst.content.beans.standard.HippoBean;

@HippoEssentialsGenerated(internalName = "xinmods:categorydetails")
@Node(jcrType = "xinmods:categorydetails")
public class Categorydetails extends HippoCompound {
    @HippoEssentialsGenerated(internalName = "xinmods:description")
    public String getDescription() {
        return getProperty("xinmods:description");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:baseFolder")
    public HippoBean getBaseFolder() {
        return getLinkedBean("xinmods:baseFolder", HippoBean.class);
    }
}
