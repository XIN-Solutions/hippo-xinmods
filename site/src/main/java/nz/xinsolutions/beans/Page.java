package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
@HippoEssentialsGenerated(internalName = "xinmods:page")
@Node(jcrType = "xinmods:page")
public class Page extends BaseDocument {
    @XmlElement
    @HippoEssentialsGenerated(internalName = "xinmods:title")
    public String getTitle() {
        return getProperty("xinmods:title");
    }

    @XmlElement
    @HippoEssentialsGenerated(internalName = "xinmods:teaser")
    public String getTeaser() {
        return getProperty("xinmods:teaser");
    }

    @XmlElement
    @HippoEssentialsGenerated(internalName = "xinmods:html")
    public String getHtml() {
        return getProperty("xinmods:html");
    }
    
    @XmlElement
    public String getUUID() {
        return getCanonicalHandleUUID();
    }
}
