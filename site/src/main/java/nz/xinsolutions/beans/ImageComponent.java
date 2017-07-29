package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;
import nz.xinsolutions.beans.GenericImageSet;

@HippoEssentialsGenerated(internalName = "xinmods:imageComponent")
@Node(jcrType = "xinmods:imageComponent")
public class ImageComponent extends HippoCompound {
    @HippoEssentialsGenerated(internalName = "xinmods:description")
    public String getDescription() {
        return getProperty("xinmods:description");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:credit")
    public String getCredit() {
        return getProperty("xinmods:credit");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:image")
    public GenericImageSet getImage() {
        return getLinkedBean("xinmods:image", GenericImageSet.class);
    }
}
