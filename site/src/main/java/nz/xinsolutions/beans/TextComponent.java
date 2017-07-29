package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;

@HippoEssentialsGenerated(internalName = "xinmods:textComponent")
@Node(jcrType = "xinmods:textComponent")
public class TextComponent extends HippoCompound {
    @HippoEssentialsGenerated(internalName = "xinmods:html")
    public String getHtml() {
        return getProperty("xinmods:html");
    }
}
