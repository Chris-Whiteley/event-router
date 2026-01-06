package io.github.chriswhiteley.eventrouter;

public interface  RemoteHandler extends Handler {
    String getRemoteService();
    String getRemoteServicesDomain();

    /**
     * @param domainA - domain name
     * @param domainB - domain name
     * @return true if domainA and domainB are in the same branch false otherwise
     */
    static boolean domainsInSameBranch(String domainA, String domainB) {
        if (domainA == null || domainA.isBlank()) return false;
        if (domainB == null || domainB.isBlank()) return false;
        return domainA.startsWith(domainB) || domainB.startsWith(domainA);
    }
}
