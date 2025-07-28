import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class Statistics {
    private long totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;

    private final Set<String> pages = new HashSet<>();
    private final Set<String> nonExistentPages = new HashSet<>();
    private final Set<String> uniqueUserIPs = new HashSet<>();
    private final Set<String> referringDomains = new HashSet<>();

    private final Map<String, Integer> osStats = new HashMap<>();
    private final Map<String, Integer> browserStats = new HashMap<>();
    private final Map<LocalDateTime, Integer> secondVisitMap = new HashMap<>();
    private final Map<String, Integer> userVisitCounts = new HashMap<>();


    private int totalOSCount = 0;
    private int totalBrowserCount = 0;
    private long usersVisits = 0;
    private long errorCount = 0;


    private static final Pattern BOT_PATTERN = Pattern.compile(".*bot.*", Pattern.CASE_INSENSITIVE);

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

        boolean isBot = entry.isBot();

        if (entry.getResponseCode() == 200) {
            pages.add(entry.getPage());
        }
        if (entry.getResponseCode() == 404) {
            nonExistentPages.add(entry.getPage());
        }

        String os = entry.getOperatingSystem();
        if (os != null && !os.isEmpty()) {
            osStats.put(os, osStats.getOrDefault(os, 0) + 1);
            totalOSCount++;
        }

        String browser = entry.getUserAgent().getBrowserType();
        if (browser != null && !browser.isEmpty()) {
            browserStats.put(browser, browserStats.getOrDefault(browser, 0) + 1);
            totalBrowserCount++;
        }

        int code = entry.getResponseCode();
        if (code >= 400 && code < 600) {
            errorCount++;
        }

        if (!isBot) {
            usersVisits++;
            uniqueUserIPs.add(entry.getIpAddr());
            userVisitCounts.put(entry.getIpAddr(), userVisitCounts.getOrDefault(entry.getIpAddr(), 0) + 1);
            LocalDateTime second = entry.getTime().withNano(0);
            secondVisitMap.put(second, secondVisitMap.getOrDefault(second, 0) + 1);
        }

        String referer = entry.getReferer();
        if (referer != null && !referer.equals("-") && !referer.isEmpty()) {
            try {
                java.net.URI uri = new java.net.URI(referer);
                String domain = uri.getHost();
                if (domain != null) {
                    referringDomains.add(domain.replaceFirst("^www\\.", ""));
                }
            } catch (Exception ignored) {

            }
        }
    }

    private long hoursBetweenTimes() {
        if (minTime == null || maxTime == null || !minTime.isBefore(maxTime)) {
            return 1;
        }
        long hours = Duration.between(minTime, maxTime).toHours();
        return Math.max(hours, 1);
    }

    public double getTrafficRate() {
        return (double) totalTraffic / hoursBetweenTimes();
    }

    public double getAverageVisitsPerHours() {
        return (double) usersVisits / hoursBetweenTimes();
    }

    public double getAverageErrorsPerHours() {
        return (double) errorCount / hoursBetweenTimes();
    }

    public double getAverageVisitsPerUsers() {
        if (uniqueUserIPs.isEmpty())
            return 0.0;
        return (double) usersVisits / uniqueUserIPs.size();
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

    public List<String> getAllNonExistentPages() {
        return new ArrayList<>(nonExistentPages);
    }

    public Map<String, Double> getOperatingSystemStats() {
        Map<String, Double> stats = new HashMap<>();
        if (totalOSCount == 0) return stats;
        for (Map.Entry<String, Integer> entry : osStats.entrySet()) {
            stats.put(entry.getKey(), entry.getValue() / (double) totalOSCount);
        }
        return stats;
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

    public int getPeakVisitsPerSecond() {
        int max = 0;
        for (int count : secondVisitMap.values()) {
            if (count > max) max = count;
        }
        return max;
    }

    public List<String> getReferringDomains() {
        return new ArrayList<>(referringDomains);
    }

    public int getMaxVisitsPerUser() {
        int max = 0;
        for (int count : userVisitCounts.values()) {
            if (count > max) max = count;
        }
        return max;
    }
}
