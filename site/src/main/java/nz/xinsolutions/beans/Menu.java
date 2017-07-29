package nz.xinsolutions.beans;

import org.hippoecm.hst.content.beans.Node;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

import java.util.List;

@HippoEssentialsGenerated(internalName = "xinmods:menu")
@Node(jcrType = "xinmods:menu")
public class Menu extends BaseDocument {
    @HippoEssentialsGenerated(internalName = "xinmods:title")
    public String getTitle() {
        return getProperty("xinmods:title");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:maxPerColumn")
    public Long getMaxPerColumn() {
        return getProperty("xinmods:maxPerColumn");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:nMaxColumns")
    public Long getNMaxColumns() {
        return getProperty("xinmods:nMaxColumns");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:image")
    public GenericImageSet getImage() {
        return getLinkedBean("xinmods:image", GenericImageSet.class);
    }

    @HippoEssentialsGenerated(internalName = "xinmods:items")
    public List<Menuitem> getItems() {
        return getChildBeansByName("xinmods:items", Menuitem.class);
    }
}
