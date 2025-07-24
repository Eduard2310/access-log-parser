import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        Statistics statistics = new Statistics();

        try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\efazylov\\Desktop\\home_work\\access.log"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    LogEntry entry = new LogEntry(line);
                    statistics.addEntry(entry);
                } catch (Exception e) {
                    System.out.println("Некорректная строка лога: " +e.getMessage());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Ошибка ввода-вывода: " + e.getMessage());
        }

        double hourTraffic = statistics.getTrafficRate();
        System.out.println("Средний объем траффика за час: " + hourTraffic + " байт/час");

        //отладка
        /*
        System.out.println("minTime: " + statistics.getMinTime());
        System.out.println("maxTime: " + statistics.getMaxTime());
        */

        //вывод страниц
        /*System.out.println("Страницы сайта:");
        for (String page : statistics.getAllPages()) {
            System.out.println("-" + page);
        }
        */

        System.out.println("Статистика по операционным системам:");
        Map<String, Double> osStats = statistics.getOperatingSystemStats();
        for (Map.Entry<String, Double> entry : osStats.entrySet()) {
            System.out.printf("%s:%.2f%n", entry.getKey(), entry.getValue());
        }



        Scanner scanner = new Scanner(System.in);
        int requestCounter = 0;
        while (true) {
            System.out.print("Введите путь к файлу: ");
            String path = scanner.nextLine();
            File file = new File(path);
            boolean fileNotFound = file.exists();
            boolean isFolder = file.isDirectory();
            if (!fileNotFound || isFolder) {
                System.out.println("Введен ошибочный путь - файл не существует/путь приводит к папке");
            } else {
                requestCounter++;
                System.out.println("Путь указан верно");
                System.out.println("Это файл номер " + requestCounter);

                int totalLines = 0;
                int googlebotCount = 0;
                int yandexbotCount = 0;

                try (FileReader fileReader = new FileReader(path);
                     BufferedReader reader = new BufferedReader(fileReader)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int length = line.length();
                        if (length > 1024) {
                            throw new TooLongLineException("В файле обнаружена строка длиной более 1024 символов: " + length);
                        }
                        totalLines++;

                        int startIndex  = line.indexOf('(');
                        int endIndex = line.indexOf(')');
                        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                            String firstBrackers = line.substring(startIndex + 1, endIndex);
                            String[] parts = firstBrackers.split(";");
                            if (parts.length >= 2) {
                                String fragment = parts[1].trim();
                                int slashIndex = fragment.indexOf('/');
                                String botName = (slashIndex != -1) ? fragment.substring(0, slashIndex) : fragment;
                                if (botName.equals("Googlebot")) {
                                    googlebotCount++;
                                } else if (botName.equals("YandexBot")) {
                                    yandexbotCount++;
                                }
                            }
                        }
                    }
                    System.out.println("Общее количество строк в файле: " + totalLines);
                    if (totalLines > 0) {
                        System.out.println("Доля запросов от YandexBot: " + ((double) yandexbotCount / totalLines));
                        System.out.println("Доля запросов от Googlebot: " + ((double) googlebotCount / totalLines));
                    }
                } catch (TooLongLineException ex) {
                    System.out.println("Ошибка: " + ex.getMessage());
                    break;
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Произошла ошибка при обработке файла", ex);
                }
            }
        }



    }
}
