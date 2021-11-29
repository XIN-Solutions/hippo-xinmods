package nz.xinsolutions.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoAsset;
import org.hippoecm.hst.content.beans.standard.HippoAssetBean;
import org.hippoecm.hst.content.beans.standard.HippoResourceBean;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      To represent the xinmods:assetset
 */
@HippoEssentialsGenerated(internalName = "xinmods:assetset")
@Node(jcrType = "xinmods:assetset")
public class GenericAsset extends HippoAsset implements HippoAssetBean {


    public GenericAsset() {
    }

    public HippoResourceBean getAsset() {
        return (HippoResourceBean) this.getBean("hippogallery:asset", HippoResourceBean.class);
    }
}
