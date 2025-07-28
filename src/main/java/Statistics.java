import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class Statistics {
    private long totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;

    private HashSet<String> pages;
    private HashMap<String, Integer> osStats;
    private int totalOSCount;
    private HashSet<String> nonExistentPages = new HashSet<>();
    private HashMap<String, Integer> browserStats = new HashMap<>();
    private int totalBrowserCount = 0;

    private long usersVisits = 0;
    private long errorCount = 0;
    private HashSet<String> uniqueUserIPs = new HashSet<>();

    private static final Pattern BOT_PATTERN = Pattern.compile(".*bot.*", Pattern.CASE_INSENSITIVE);

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.pages = new HashSet<>();
        this.osStats = new HashMap<>();
        this.totalOSCount = 0;
    }

    public void addEntry(LogEntry entry) {
        LocalDateTime entryTime = entry.getTime();
        if (minTime == null || entryTime.isBefore(minTime)) {
            minTime = entryTime;
        }
        if (maxTime == null || entryTime.isAfter(maxTime)) {
            maxTime = entryTime;
        }
        int size = entry.getResponseSize();
        if (size < 0) size = 0;
        totalTraffic += size;

        String userAgentStr = entry.getUserAgent().getUserAgentString();
        boolean isBot = BOT_PATTERN.matcher(userAgentStr).matches();

        if (entry.getResponseCode() == 200) {
            pages.add(entry.getPage());
        }

        String os = entry.getOperatingSystem();
        if (os != null && !os.isEmpty()) {
            osStats.put(os, osStats.getOrDefault(os, 0) +1);
            totalOSCount++;
        }

        if (!isBot) {
            usersVisits++;
            uniqueUserIPs.add(entry.getIpAddr());
        }

        int code = entry.getResponseCode();
        if (code >= 400 && code < 600) {
            errorCount++;
        }

        if (entry.getResponseCode() == 404) {
            nonExistentPages.add(entry.getPage());
        }

        String browser = entry.getUserAgent().getBrowserType();
        if (browser != null && !browser.isEmpty()) {
            browserStats.put(browser, browserStats.getOrDefault(browser, 0) + 1);
            totalBrowserCount++;
        }
    }

    private long hoursBetweenTimes() {
        if (minTime == null || maxTime == null || !minTime.isBefore(maxTime)) {
            return 1;
        }
        long hours = Duration.between(minTime, maxTime).toHours();
        return  hours > 0 ? hours : 1;
    }

    public double getAverageVisitsPerHour() {
        return (double) usersVisits / hoursBetweenTimes();
    }

    public double getAverageErrorsPerHour() {
        return (double) errorCount / hoursBetweenTimes();
    }

    public double getAverageVisitsPerUser() {
        if (uniqueUserIPs.isEmpty())
            return 0.0;
        return (double) usersVisits / uniqueUserIPs.size();
    }

    public double getTrafficRate() {
        if (minTime == null || maxTime == null || !minTime.isBefore(maxTime)) return 0.0;
        long hours = Duration.between(minTime, maxTime).toHours();
        if (hours <= 0) hours = 1;
        return (double) totalTraffic / hours;
    }

    public long getTotalTraffic() {
        return totalTraffic;
    }
    public LocalDateTime getMinTime() {
        return minTime;
    }
    public LocalDateTime getMaxTime() {
        return maxTime;
    }

    public List<String> getAllPages() {
        return new ArrayList<>(pages);
    }

    public Map<String, Double> getOperatingSystemStats() {
        Map<String, Double> stats = new HashMap<>();
        if (totalOSCount == 0) return stats;
        for (Map.Entry<String, Integer> entry : osStats.entrySet()) {
            stats.put(entry.getKey(), entry.getValue() / (double) totalOSCount);
        }
        return  stats;
    }

    public List<String> getAllNonExistentPages() {
        return new ArrayList<>(nonExistentPages);
    }

    public Map<String, Double> getBrowserStats() {
        Map<String, Double> result = new HashMap<>();
        if (totalBrowserCount == 0)
            return result;
        for (Map.Entry<String, Integer> entry : browserStats.entrySet()) {
            result.put(entry.getKey(), entry.getValue() / (double) totalBrowserCount);
        }
        return result;
    }
}
