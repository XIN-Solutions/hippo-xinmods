package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import nz.xinsolutions.beans.GenericImageSet;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

/** 
 * TODO: Beanwriter: Failed to create getter for node type: hippo:compound
 */
@XmlRootElement(name = "content")
@XmlAccessorType(XmlAccessType.NONE)
@HippoEssentialsGenerated(internalName = "xinmods:content")
@Node(jcrType = "xinmods:content")
public class Content extends BaseDocument {
    @XmlElement
    @HippoEssentialsGenerated(internalName = "xinmods:title")
    public String getTitle() {
        return getProperty("xinmods:title");
    }

    @XmlElement
    @HippoEssentialsGenerated(internalName = "xinmods:description")
    public String getDescription() {
        return getProperty("xinmods:description");
    }

    @XmlElement
    @HippoEssentialsGenerated(internalName = "xinmods:pageTitle")
    public String getPageTitle() {
        return getProperty("xinmods:pageTitle");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:thumbnail")
    public GenericImageSet getThumbnail() {
        return getLinkedBean("xinmods:thumbnail", GenericImageSet.class);
    }
}
