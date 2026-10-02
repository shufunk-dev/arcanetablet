package net.arcanetablet.data;

public enum ArcaneCategory {
    ENVIRONMENT("Environmental", "Weather & Time Rites", 0xFF00E5FF),
    NAVIGATION("Navigation", "Travel & Landmark Discovery", 0xFFFFD700),
    SANCTUARY("Sanctuary", "Mob & Crowd Mitigation", 0xFF00FF88),
    PRESERVATION("Preservation", "High-Stakes Emergency Magic", 0xFFFF5555);

    private final String displayName;
    private final String description;
    private final int themeColor;

    ArcaneCategory(String displayName, String description, int themeColor) {
        this.displayName = displayName;
        this.description = description;
        this.themeColor = themeColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getThemeColor() {
        return themeColor;
    }
}
