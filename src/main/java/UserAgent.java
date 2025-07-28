public class UserAgent {
    private final String raw;
    private final String osType;
    private final String browserType;

    public UserAgent(String userAgentString) {
        this.raw = userAgentString;
        this.osType = parseOsType(userAgentString);
        this.browserType = parseBrowserType(userAgentString);
    }

    private String parseOsType(String ua) {
        if (ua.contains("Windows"))
            return "Windows";
        if (ua.contains("Mac OS") || ua.contains("Macintosh"))
            return "macOS";
        if (ua.contains("Linux"))
            return "Linux";
        return "Other";
    }

    private String parseBrowserType(String ua) {
        if (ua.contains("Edge"))
            return "Edge";
        if (ua.contains("OPR") || ua.contains("Opera"))
            return "Opera";
        if (ua.contains("Chrome"))
            return "Chrome";
        if (ua.contains("Firefox"))
            return "Firefox";
        if (ua.contains("Safari") && !ua.contains("Chrome"))
            return "Safari";
        return "Other";
    }

    public String getRaw() {
        return raw;
    }
    public String getUserAgentString() {
        return raw;
    }
    public String getOsType() {
        return osType;
    }
    public String getBrowserType() {
        return browserType;
    }
    public String getOperatingSystem() {
        return getOsType();
    }
    public boolean isBot() {
        return raw.toLowerCase().contains("bot");
    }

    @Override
    public String toString() {
        return String.format("UserAgent[os=%s, browser=%s, raw=%s]", osType, browserType, raw);
    }
}
