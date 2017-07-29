package nz.xinsolutions.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

@HippoEssentialsGenerated(internalName = "xinmods:textImageComponent")
@Node(jcrType = "xinmods:textImageComponent")
public class TextImageComponent extends HippoCompound {
    @HippoEssentialsGenerated(internalName = "xinmods:html")
    public String getHtml() {
        return getProperty("xinmods:html");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:imageLocation")
    public String getImageLocation() {
        return getProperty("xinmods:imageLocation");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:textWrap")
    public Boolean getTextWrap() {
        return getProperty("xinmods:textWrap");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:image")
    public GenericImageSet getImage() {
        return getLinkedBean("xinmods:image", GenericImageSet.class);
    }
}
