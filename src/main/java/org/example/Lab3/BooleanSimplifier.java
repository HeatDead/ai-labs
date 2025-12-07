package org.example.Lab3;

public class BooleanSimplifier {

    public static void main(String[] args) {
        String expression = "(A or (not A)) and B";
        String simplified = simplify(expression);
        System.out.println("Изначальное выражение: " + expression);
        System.out.println("Упрощенное выражение: " + simplified.toUpperCase());
    }

    public static String simplify(String expression) {
        // Приведем к нижнему регистру для единообразия, но сохраним пробелы
        String expr = expression.toLowerCase().trim();

        // Удалим лишние пробелы вокруг операторов и скобок
        expr = expr.replaceAll("\\s+", " ");
        expr = expr.replaceAll("\\(\\s+", "(");
        expr = expr.replaceAll("\\s+\\)", ")");

        // Начнем с самых вложенных скобок, применяя законы логики
        // Будем повторять, пока есть изменения
        String prevExpr;
        do {
            prevExpr = expr;

            // 1. Упрощаем выражения в скобках
            expr = simplifyParentheses(expr);

            // 2. Применяем законы де Моргана
            expr = applyDeMorgan(expr);

            // 3. Применяем закон исключенного третьего и другие
            expr = applyBasicLaws(expr);

            // 4. Убираем двойные отрицания
            expr = removeDoubleNegation(expr);

            // 5. Упрощаем константы true/false
            expr = simplifyConstants(expr);

        } while (!expr.equals(prevExpr));

        return expr;
    }

    private static String simplifyParentheses(String expr) {
        // Ищем самые внутренние скобки
        int start = expr.lastIndexOf("(");
        if (start == -1) return expr;

        int end = expr.indexOf(")", start);
        if (end == -1) return expr;

        String inside = expr.substring(start + 1, end);
        String simplifiedInside = simplify(inside); // рекурсивно упрощаем внутреннее выражение

        // Собираем новое выражение
        String before = expr.substring(0, start);
        String after = expr.substring(end + 1);

        // Если упрощенное выражение в скобках - это одиночный терм, убираем скобки
        if (isSingleTerm(simplifiedInside)) {
            return before + simplifiedInside + after;
        } else {
            return before + "(" + simplifiedInside + ")" + after;
        }
    }

    private static boolean isSingleTerm(String expr) {
        // Проверяем, является ли выражение одиночным термом (без операторов и скобок)
        expr = expr.trim();
        return !expr.contains(" ") && !expr.contains("(") && !expr.contains(")") &&
                !expr.equals("and") && !expr.equals("or") && !expr.equals("not");
    }

    private static String applyDeMorgan(String expr) {
        // Применяем законы де Моргана: not (A and B) -> (not A) or (not B)
        // и not (A or B) -> (not A) and (not B)

        // Упростим: заменим not (a and b) на (not a) or (not b)
        expr = expr.replaceAll("not\\s*\\(\\s*([a-z0-9]+)\\s+and\\s+([a-z0-9]+)\\s*\\)",
                "(not $1) or (not $2)");
        // Упростим: заменим not (a or b) на (not a) and (not b)
        expr = expr.replaceAll("not\\s*\\(\\s*([a-z0-9]+)\\s+or\\s+([a-z0-9]+)\\s*\\)",
                "(not $1) and (not $2)");

        return expr;
    }

    private static String applyBasicLaws(String expr) {
        // Закон исключенного третьего: A or (not A) = true
        expr = expr.replaceAll("([a-z0-9]+)\\s+or\\s+\\(\\s*not\\s+\\1\\s*\\)", "true");
        expr = expr.replaceAll("\\(\\s*not\\s+([a-z0-9]+)\\s*\\)\\s+or\\s+\\1", "true");

        // Закон противоречия: A and (not A) = false
        expr = expr.replaceAll("([a-z0-9]+)\\s+and\\s+\\(\\s*not\\s+\\1\\s*\\)", "false");
        expr = expr.replaceAll("\\(\\s*not\\s+([a-z0-9]+)\\s*\\)\\s+and\\s+\\1", "false");

        // Идемпотентность: A and A = A, A or A = A
        expr = expr.replaceAll("([a-z0-9]+)\\s+and\\s+\\1", "$1");
        expr = expr.replaceAll("([a-z0-9]+)\\s+or\\s+\\1", "$1");

        // Упрощение с true/false
        expr = expr.replaceAll("true\\s+and\\s+true", "true");
        expr = expr.replaceAll("false\\s+or\\s+false", "false");
        expr = expr.replaceAll("true\\s+and\\s+([a-z0-9]+)", "$1");
        expr = expr.replaceAll("([a-z0-9]+)\\s+and\\s+true", "$1");
        expr = expr.replaceAll("false\\s+or\\s+([a-z0-9]+)", "$1");
        expr = expr.replaceAll("([a-z0-9]+)\\s+or\\s+false", "$1");
        expr = expr.replaceAll("true\\s+or\\s+([a-z0-9]+)", "true");
        expr = expr.replaceAll("([a-z0-9]+)\\s+or\\s+true", "true");
        expr = expr.replaceAll("false\\s+and\\s+([a-z0-9]+)", "false");
        expr = expr.replaceAll("([a-z0-9]+)\\s+and\\s+false", "false");

        return expr;
    }

    private static String removeDoubleNegation(String expr) {
        // Убираем двойное отрицание: not not A = A
        return expr.replaceAll("not\\s+not\\s+([a-z0-9]+)", "$1")
                .replaceAll("not\\s+\\(\\s*not\\s+([a-z0-9]+)\\s*\\)", "$1");
    }

    private static String simplifyConstants(String expr) {
        // Упрощаем выражения с константами
        // Если всё выражение равно true или false, возвращаем его
        if (expr.equals("true") || expr.equals("false")) {
            return expr;
        }

        // Убираем скобки вокруг true/false, если они есть
        expr = expr.replaceAll("\\(\\s*true\\s*\\)", "true");
        expr = expr.replaceAll("\\(\\s*false\\s*\\)", "false");

        // Если выражение вида (true) and X, упрощаем
        expr = expr.replaceAll("\\(\\s*true\\s*\\)\\s+and", "true and");
        expr = expr.replaceAll("and\\s+\\(\\s*true\\s*\\)", "and true");

        return expr;
    }
}
