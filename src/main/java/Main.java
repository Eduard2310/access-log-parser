import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        int requestCounter = 0;
        while (true) {
            System.out.print("Введите путь к файлу: ");
            String path = scanner.nextLine();
            File file = new File(path);
            /*boolean fileNotFound = file.exists();
            boolean isFolder = file.isDirectory();*/
            if (!file.exists() || file.isDirectory()) {
                System.out.println("Введен ошибочный путь - файл не существует/путь приводит к папке");
                continue;
            }
            requestCounter++;
            System.out.println("Путь указан верно");
            System.out.println("Это файл номер " + requestCounter);

            Statistics statistics = new Statistics();
            int totalLines = 0;
            int googlebotCount = 0;
            int yandexbotCount = 0;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    totalLines++;

                    if (line.length() > 1024) {
                        throw new TooLongLineException("В файле обнаружена строка длиной более 1024 символов: " + line.length());
                    }

                    try {
                        LogEntry entry = new LogEntry(line);
                        statistics.addEntry(entry);

                        //Подсчет ботов
                        String userAgentRaw = entry.getUserAgent().getRaw();
                        if (userAgentRaw.contains("Googlebot")) {
                            googlebotCount++;
                        }
                        if (userAgentRaw.contains("YandexBot")) {
                            yandexbotCount++;
                        }
                    } catch (Exception e) {
                        System.out.println("Некорректная строка лога: " + e.getMessage());
                    }
                }
            } catch (TooLongLineException ex) {
                System.out.println("Ошибка: " + ex.getMessage());
                break;
            } catch (Exception ex) {
                System.out.println("Ошибка при чтении  файла: " + ex.getMessage());
                continue;
            }

            //Вывод полной статистики по файлу
            System.out.println("Общее количество строк в файле: " + totalLines);
            System.out.println("-----");
            //Траффик
            double hourTraffic = statistics.getTrafficRate();
            System.out.println("Средний объем траффика за час: " + hourTraffic + " байт/час");
            System.out.println("Общий траффик: " + statistics.getTotalTraffic() + " байт");
            System.out.println("-----");
            //Посещения и ошибки
            System.out.println("Сруднее количество посещений в час: " + statistics.getAverageVisitsPerHour());
            System.out.println("Сруднее количество ошибок в час: " + statistics.getAverageErrorsPerHour());
            System.out.println("Сруднее посещаемость одним пользователем: " + statistics.getAverageVisitsPerUser());
            System.out.println("-----");
            //Страницы
            /*System.out.println("Существующие страницы:");
            for (String page : statistics.getAllPages()) {
                System.out.println(" - " + page);
            }
            System.out.println("-----");
            System.out.println("Несуществющие страницы:");
            for (String page : statistics.getAllNonExistentPages()) {
                System.out.println(" - " + page);
            }
            System.out.println("-----");
            */
            //ОС
            System.out.println("Статистика по операционным системам:");
            for (Map.Entry<String, Double> entry : statistics.getOperatingSystemStats().entrySet()) {
                System.out.printf("%s:%.2f%%\n", entry.getKey(), entry.getValue() * 100);
            }
            System.out.println("-----");
            //Браузеры
            System.out.println("Статистика браузеров:");
            for (Map.Entry<String, Double> entry : statistics.getBrowserStats().entrySet()) {
                System.out.printf("%s:%.2f%%\n", entry.getKey(), entry.getValue());
            }
            System.out.println("-----");
            //Боты
            if (totalLines > 0) {
                System.out.println("Доля запросов от Googlebot: " + ((double) googlebotCount / totalLines));
                System.out.println("Доля запросов от YandexBot: " + ((double) yandexbotCount / totalLines));
            }
            System.out.println("-----");
            System.out.println("Конец анализа файла");
        }
    }
}