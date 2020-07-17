package nz.xinsolutions.beans;

/**
 * Author: Marnix Kok <mkok@btes.co.nz>
 * Date: 31/01/18.
 *
 *      Clone package payload
 */
public class ClonePackagePayload {

    private String fromPackage;
    private String toPackage;

    public String getFromPackage() {
        return fromPackage;
    }

    public void setFromPackage(String fromPackage) {
        this.fromPackage = fromPackage;
    }

    public String getToPackage() {
        return toPackage;
    }

    public void setToPackage(String toPackage) {
        this.toPackage = toPackage;
    }
}
