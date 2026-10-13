package ru.edu.pr02;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SelfCheck {
    private static int passed = 0;
    private static int count = 0;

    public static void main(String[] args) {
        // исходная проверка из задания
        check("total and overtime", TimesheetAnalyzer.total(new int[]{8, 8, 8, 8, 8}) == 40
                && TimesheetAnalyzer.overtimeDays(new int[]{9, 8, 7, 10, 8}) == 2);
        checkParse();
        checkCalculations();
        System.out.println("Unit: " + passed + "/" + count + " passed");
        runFile(args.length > 0 ? args[0] : "data/sample.csv");
    }

    private static void checkParse() {
        check("typical line", Arrays.equals(TimesheetAnalyzer.parseHours("8;9;10;8;8"), new int[]{8, 9, 10, 8, 8}));
        // границы значения: 0 и 16 можно, -1 и 17 нельзя
        check("value 0", Arrays.equals(TimesheetAnalyzer.parseHours("0;0;0;0;0"), new int[]{0, 0, 0, 0, 0}));
        check("value 16", Arrays.equals(TimesheetAnalyzer.parseHours("16;16;16;16;16"), new int[]{16, 16, 16, 16, 16}));
        expectError("value 17", "8;8;17;8;8", "позиции 3");
        expectError("value -1", "-1;8;8;8;8", "позиции 1");
        // пустая строка и пустое значение
        expectError("empty line", "", "пустая строка");
        expectError("empty value", "8;;8;8;8", "позиции 2");
        // неверный формат
        expectError("letters", "8;abc;8;8;8", "позиции 2");
        expectError("fraction", "8;8;8;8.5;8", "позиции 4");
        expectError("space", "8; 9;8;8;8", "позиции 2");
        expectError("huge number", "8;8;99999999999;8;8", "позиции 3");
        // структура: 4 и 6 значений, лишняя ; в конце
        expectError("4 values", "8;8;8;8", "позиции 5");
        expectError("6 values", "8;8;8;8;8;8", "позиции 6");
        expectError("trailing ;", "8;8;8;8;8;", "позиции 6");
        // конфликт: две ошибки в одной строке - сообщается первая позиция
        expectError("two errors", "8;abc;17;8;8", "позиции 2");
    }

    private static void checkCalculations() {
        int[] week = {8, 9, 10, 8, 8};
        check("total", TimesheetAnalyzer.total(week) == 43);
        check("average", TimesheetAnalyzer.average(week) == 8.6);
        check("average zeros", TimesheetAnalyzer.average(new int[]{0, 0, 0, 0, 0}) == 0.0);
        // граница переработки: 8 часов - нет, 9 - да
        check("overtime 8", TimesheetAnalyzer.overtimeDays(new int[]{8, 8, 8, 8, 8}) == 0);
        check("overtime 9", TimesheetAnalyzer.overtimeDays(new int[]{9, 8, 8, 8, 8}) == 1);
        check("overtime days", TimesheetAnalyzer.overtimeDays(week) == 2);
        // недельная переработка: 40 - ноль, 41 - один час, меньше 40 - не уходит в минус
        check("weekly 40", TimesheetAnalyzer.weeklyOvertime(new int[]{8, 8, 8, 8, 8}) == 0);
        check("weekly 41", TimesheetAnalyzer.weeklyOvertime(new int[]{9, 8, 8, 8, 8}) == 1);
        check("weekly less 40", TimesheetAnalyzer.weeklyOvertime(new int[]{4, 4, 4, 4, 4}) == 0);
        check("weekly max", TimesheetAnalyzer.weeklyOvertime(new int[]{16, 16, 16, 16, 16}) == 40);
        // повтор: одинаковые строки дают одинаковый отчет
        check("repeat", TimesheetAnalyzer.buildReport("8;9;10;8;8").equals(TimesheetAnalyzer.buildReport("8;9;10;8;8")));
    }

    private static void check(String name, boolean ok) {
        count++;
        if (ok) {
            passed++;
            System.out.println("PASS " + name);
        } else {
            System.out.println("FAIL " + name);
        }
    }

    // ждем IllegalArgumentException, в сообщении должна быть нужная позиция
    private static void expectError(String name, String line, String expectedPart) {
        try {
            TimesheetAnalyzer.parseHours(line);
            check(name, false);
        } catch (IllegalArgumentException e) {
            check(name, e.getMessage().contains(expectedPart));
        }
    }

    // отчет по каждой строке файла
    private static void runFile(String fileName) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Paths.get(fileName));
        } catch (IOException e) {
            System.out.println("Не удалось прочитать файл " + fileName);
            return;
        }
        System.out.println();
        System.out.println("Табели из файла " + fileName + ":");
        for (int i = 0; i < lines.size(); i++) {
            System.out.println();
            System.out.println("Строка " + (i + 1) + " (" + lines.get(i) + "):");
            System.out.println(TimesheetAnalyzer.buildReport(lines.get(i)));
        }
    }
}
