package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;
import java.util.List;
import org.hippoecm.hst.content.beans.standard.HippoBean;

@HippoEssentialsGenerated(internalName = "xinmods:galleryComponent")
@Node(jcrType = "xinmods:galleryComponent")
public class GalleryComponent extends HippoCompound {
    @HippoEssentialsGenerated(internalName = "xinmods:description")
    public String getDescription() {
        return getProperty("xinmods:description");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:inGrid")
    public Boolean getInGrid() {
        return getProperty("xinmods:inGrid");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:assetFolders")
    public List<HippoBean> getAssetFolders() {
        return getLinkedBeans("xinmods:assetFolders", HippoBean.class);
    }
}
