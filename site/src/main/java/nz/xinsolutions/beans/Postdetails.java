package nz.xinsolutions.beans;

import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;
import java.util.Calendar;

@HippoEssentialsGenerated(internalName = "xinmods:postdetails")
@Node(jcrType = "xinmods:postdetails")
public class Postdetails extends HippoCompound {
    @HippoEssentialsGenerated(internalName = "xinmods:startDate")
    public Calendar getStartDate() {
        return getProperty("xinmods:startDate");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:endDate")
    public Calendar getEndDate() {
        return getProperty("xinmods:endDate");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:time")
    public String getTime() {
        return getProperty("xinmods:time");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:contactName")
    public String getContactName() {
        return getProperty("xinmods:contactName");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:locationName")
    public String getLocationName() {
        return getProperty("xinmods:locationName");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:email")
    public String getEmail() {
        return getProperty("xinmods:email");
    }

    @HippoEssentialsGenerated(internalName = "xinmods:address")
    public String getAddress() {
        return getProperty("xinmods:address");
    }
}
