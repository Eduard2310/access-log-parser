import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogEntry {
    public enum HttpMethod {
        GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE, CONNECT, UNKNOWN
    }

    private final String ipAddr;
    private final String remoteLogname;
    private final String remoteUser;
    private final LocalDateTime time;
    private final HttpMethod method;
    private final String path;
    private final int responseCode;
    private final int responseSize;
    private final String referer;
    private final UserAgent userAgent;

    public LogEntry(String logLine) {
        Pattern pattern = Pattern.compile(
                "^(\\S+)\\s+(\\S+)\\s+(\\S+)\\s+\\[([^\\]]+)]\\s+\"(\\S+)\\s+(\\S+)\\s+(\\S+)\"\\s+(\\S+)\\s+(\\S+)\\s+\"([^\"]*)\"\\s+\"([^\"]*)\""
        );
        Matcher matcher = pattern.matcher(logLine);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Некорректная строка лога: " + logLine);
        }
        this.ipAddr = matcher.group(1);
        this.remoteLogname = matcher.group(2);
        this.remoteUser = matcher.group(3);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(matcher.group(4), formatter);
        this.time = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();

        String methodStr = matcher.group(5);
        HttpMethod parsedMethod;
        try {
            parsedMethod = HttpMethod.valueOf(methodStr);
        } catch (IllegalArgumentException e) {
            parsedMethod = HttpMethod.UNKNOWN;
        }
        this.method = parsedMethod;

        this.path = matcher.group(6);

        String codeStr = matcher.group(8);
        this.responseCode = codeStr.equals("-") ? 0 : Integer.parseInt(codeStr);

        String sizeStr = matcher.group(9);
        int size = 0;
        try {
            size = sizeStr.equals("-") ? 0 : Integer.parseInt(sizeStr);
        } catch (NumberFormatException e) {
            size = 0;
        }
        this.responseSize = Math.max(0, size);
        this.referer = matcher.group(10);
        this.userAgent = new UserAgent(matcher.group(11));

    }

    public String getIpAddr() {
        return ipAddr;
    }
    public String getRemoteLogname() {
        return remoteLogname;
    }
    public String getRemoteUser() {
        return remoteUser;
    }
    public LocalDateTime getTime() {
        return time;
    }
    public HttpMethod getMethod() {
        return method;
    }
    public String getPath() {
        return path;
    }
    public int getResponseCode() {
        return responseCode;
    }
    public int getResponseSize() {
        return responseSize;
    }
    public String getReferer() {
        return referer;
    }
    public UserAgent getUserAgent() {
        return userAgent;
    }
    public String getPage() {
        return getPath();
    }
    public String getOperatingSystem() {
        return userAgent != null ? userAgent.getOperatingSystem() : "Unknown";
    }

    public boolean isBot() {
        return userAgent != null && userAgent.isBot();
    }
}
