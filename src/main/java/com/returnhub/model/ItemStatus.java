package com.returnhub.model;

public enum ItemStatus {
    OPEN("Open", "bg-blue-100 text-blue-800"),
    CLAIM_PENDING("Claim Pending", "bg-amber-100 text-amber-800"),
    RESOLVED("Resolved / Returned", "bg-emerald-100 text-emerald-800");

    private final String displayName;
    private final String badgeClass;

    ItemStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}
