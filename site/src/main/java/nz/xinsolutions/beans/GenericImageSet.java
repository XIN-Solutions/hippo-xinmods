package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageSet;
import org.hippoecm.hst.content.beans.standard.HippoGalleryImageBean;

@HippoEssentialsGenerated(internalName = "xinmods:genericimageset")
@Node(jcrType = "xinmods:genericimageset")
public class GenericImageSet extends HippoGalleryImageSet {
    @HippoEssentialsGenerated(internalName = "xinmods:mobile")
    public HippoGalleryImageBean getMobile() {
        return getBean("xinmods:mobile", HippoGalleryImageBean.class);
    }

    @HippoEssentialsGenerated(internalName = "xinmods:tablet")
    public HippoGalleryImageBean getTablet() {
        return getBean("xinmods:tablet", HippoGalleryImageBean.class);
    }

    @HippoEssentialsGenerated(internalName = "xinmods:desktop")
    public HippoGalleryImageBean getDesktop() {
        return getBean("xinmods:desktop", HippoGalleryImageBean.class);
    }

    @HippoEssentialsGenerated(internalName = "xinmods:widescreen")
    public HippoGalleryImageBean getWidescreen() {
        return getBean("xinmods:widescreen", HippoGalleryImageBean.class);
    }
}
