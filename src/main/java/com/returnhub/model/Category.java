package com.returnhub.model;

public enum Category {
    ELECTRONICS("Electronics", "fas fa-mobile-screen-button"),
    WALLETS_BAGS("Wallets & Bags", "fas fa-wallet"),
    KEYS("Keys", "fas fa-key"),
    DOCUMENTS("Documents & Cards", "fas fa-id-card"),
    CLOTHING("Clothing & Apparel", "fas fa-shirt"),
    PETS("Pets & Animals", "fas fa-paw"),
    JEWELRY_ACCESSORIES("Jewelry & Watches", "fas fa-gem"),
    OTHER("Other Items", "fas fa-box-open");

    private final String displayName;
    private final String iconClass;

    Category(String displayName, String iconClass) {
        this.displayName = displayName;
        this.iconClass = iconClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconClass() {
        return iconClass;
    }
}
