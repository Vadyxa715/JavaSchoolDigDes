package com.digdes.school;

import com.digdes.school.enums.Columns;
import com.digdes.school.enums.ComparisonOperators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JavaSchoolStarter {
    //Дефолтный конструктор
    public JavaSchoolStarter() {

    }

    private static List<Map<String, Object>> data = new ArrayList<>();


    //На вход запрос, на выход результат выполнения запроса
    public List<Map<String, Object>> execute(String request) throws Exception {
        String word = request.replaceFirst("^\\s*", "").substring(0, 6).toLowerCase();
        return switch (word) {
            case "insert" -> insert(request);
            case "update" -> update(request);
            case "delete" -> delete(request);
            case "select" -> select(request);
            default -> throw new Exception("Некорректная команда.");
        };
    }

    public static void viewData(List<Map<String, Object>> maps) {
        String leftAlignFormat = "| %-2s | %-14s | %-10s | %-13s | %-9s |%n";

        System.out.format("+----+----------------+------------+---------------+-----------+%n");
        System.out.format("| id | lastName       |    age     |      cost     |   active  |%n");
        System.out.format("+----+----------------+------------+---------------+-----------+%n");
        for (Map<String, Object> s : maps) {
            String a = String.valueOf(s.get(Columns.ID.getStr())),
                    b = String.valueOf(s.get(Columns.LASTNAME.getStr())),
                    c = String.valueOf(s.get(Columns.AGE.getStr())),
                    d = String.valueOf(s.get(Columns.COST.getStr())),
                    e = String.valueOf(s.get(Columns.ACTIVE.getStr()));
            System.out.format(leftAlignFormat, a, b, c, d, e);
        }
        System.out.format("+----+----------------+------------+---------------+-----------+%n");
    }

    private static List<Map<String, Object>> insert(String s) throws Exception {
        Map<String, Object> row = new HashMap<>();
        Pattern pattern = Pattern.compile("insert" + "(\\s)" + "values" + "(?!\\S)");
        Matcher matcher = pattern.matcher(s.toLowerCase());
        if (!matcher.find() || s.contains("where")) {
            throw new Exception("Ошибка. Запрос не соответсвует формату.");
        }
        int i = s.indexOf('\'');
        if (i == -1) {
            throw new Exception("Некорректный запрос.");
        }
        String word = s.substring(i);
        List<String> arr = List.of(word.split(","));
        for (String st : arr) {
            var valid = validateCandidate(st);
            row.put(valid.getKey(), valid.getValue());
        }
        data.add(row);
        return List.of(row);
    }

    private static Map.Entry<String, Object> validateCandidate(String s) throws Exception {
        String[] s1 = s.split(">=|<=|!=|=|>|<|like|ilike");

        String column = validationStringName(s1[0]).toLowerCase();

        Columns columns = Columns.of(column);
        if (columns == null) {
            throw new Exception("Ошибка. Такого \"" + s1[0] + "\" поля не существует.");
        }
        Object value = "null";
        if (s1[1].equals("null")) {
            return Map.entry(column, value);
        }
        switch (columns) {
            case ID:
            case AGE:
                try {
                    value = Long.parseLong(s1[1]);
                } catch (NumberFormatException e) {
                    throw new Exception("Данный формат не соответствует столбцу \"" + s1[0] + "\". Ожидается тип \"Long\"");
                }
                break;
            case COST:
                try {
                    value = Double.parseDouble(s1[1]);
                } catch (NumberFormatException e) {
                    throw new Exception("Данный формат не соответствует столбцу \"" + s1[0] + "\". Ожидается тип \"Double\"");
                }
                break;
            case ACTIVE:
                try {
                    if (!(s1[1].equalsIgnoreCase("true") || s1[1].equalsIgnoreCase("false"))) {
                        throw new Exception("Некорректный запрос.");
                    }
                    value = Boolean.parseBoolean(s1[1]);
                } catch (NumberFormatException e) {
                    throw new Exception("Данный формат не соответствует столбцу \"" + s1[0] + "\". Ожидается тип \"Boolean\"");
                }
                break;
            case LASTNAME:
                value = validationStringName(s1[1]);
                break;
            default:
                throw new Exception("Данной колонки \"" + s1[0] + "\" нет в таблице.");
        }
        return Map.entry(column, value);
    }

    private static String validationStringName(String s) throws Exception {
        String name = s.replaceAll(" ", "");
        long count = name.codePoints().filter(ch -> ch == '\'').count();
        if (count != 2) {
            throw new Exception("Ошибка. Некорректный запрос.");
        }
        char first = name.charAt(0);
        char last = name.charAt(name.length() - 1);
        if (first == last && last == '\'') {
            return name.substring(1, name.length() - 1);
        } else
            throw new Exception("Ошибка. Некорректный запрос.");
    }

    private static List<Map<String, Object>> update(String s) throws Exception {
        Pattern patternUpVal = Pattern.compile("update" + "(\\s)" + "values" + "(?!\\S)");
        Matcher matcherUpVal = patternUpVal.matcher(s.toLowerCase());
        if (!matcherUpVal.find()) {
            throw new Exception("Ошибка. Запрос не соответсвует формату.");
        }
        int i = s.indexOf('\'');
        if (i == -1) {
            throw new Exception("Некорректный запрос.");
        }
        String word = s.substring(i, s.length());
        String equalsWord = word.toLowerCase();
        List<Map.Entry<String, Object>> newValues;
        List<Map<String, Object>> result;
        if (equalsWord.contains("where")) {
            List<String> arr = List.of(word.split("(?i)where"));
            newValues = setNewValues(arr.get(0));
            result = data.stream()
                    .filter(x -> {
                        try {
                            return where(x, arr.get(1));
                        } catch (Exception e) {
                            throw new RuntimeException("Некорректные условия. " + e.getMessage());
                        }
                    }).collect(Collectors.toList());
            result.forEach(sup -> {
                for (Map.Entry<String, Object> ss : newValues) {
                    sup.put(ss.getKey(), ss.getValue());
                }
            });
        } else {
            result = data;
            newValues = setNewValues(word);
            for (Map<String, Object> r : result) {
                for (Map.Entry<String, Object> ss : newValues) {
                    if (!ss.getValue().equals("null")) {
                        r.put(ss.getKey(), ss.getValue());
                    } else r.put(ss.getKey(), null);
                }
            }
        }
        return result;
    }


    private static List<Map.Entry<String, Object>> setNewValues(String s) throws Exception {
        List<String> arr = List.of(s.split(","));
        List<Map.Entry<String, Object>> entryList = new ArrayList<>();
        for (String st : arr) {
            var valid = validateCandidate(st);
            entryList.add(valid);
        }
        return entryList;
    }


    private static List<Map<String, Object>> delete(String s) throws Exception {
        String equalsWord = s.toLowerCase();
        List<Map<String, Object>> result;
        if (equalsWord.contains("where")) {
            int i = s.indexOf('\'');
            if (i == -1) {
                throw new Exception("Некорректный запрос.");
            }
            String word = s.substring(i, s.length());
            result = data.stream()
                    .filter(x -> {
                        try {
                            return where(x, word);
                        } catch (Exception e) {
                            throw new RuntimeException("Некорректные условия. " + e.getMessage());
                        }
                    }).collect(Collectors.toList());
            data.removeIf(sip -> {
                try {
                    return where(sip, word);
                } catch (Exception e) {
                    throw new RuntimeException("Некорректные условия. " + e.getMessage());
                }
            });
        } else {
            result = new ArrayList<>(data);
            data.clear();
        }
        return result;
    }

    private static List<Map<String, Object>> select(String s) throws Exception {
        String equalsWord = s.toLowerCase();
        List<Map<String, Object>> result;
        if (equalsWord.contains("where")) {
            int i = s.indexOf('\'');
            if (i == -1) {
                throw new Exception("Некорректный запрос.");
            }
            String word = s.substring(i, s.length());
            result = data.stream()
                    .filter(x -> {
                        try {
                            return where(x, word);
                        } catch (Exception e) {
                            throw new RuntimeException("Некорректные условия. " + e.getMessage());
                        }
                    }).collect(Collectors.toList());
        } else {
            result = data;
        }
        return result;
    }

    private static boolean where(Map<String, Object> m, String s) throws Exception {
        Boolean equal;
        if (s.matches(".*(?i)and.*")) {
            String[] arr = s.split(" (?i)and ");
            equal = where(m, arr[0]) && where(m, arr[1]);
        } else if (s.matches(".*(?i)or.*")) {
            String[] arr1 = s.split(" (?i)or ");
            equal = where(m, arr1[0]) || where(m, arr1[1]);
        } else {
            return where1(m, s);
        }

        return equal;
    }

    private static boolean where1(Map<String, Object> m, String s) throws Exception {
        ComparisonOperators comparisonOperators = getComparisonOperator(s);
        if (comparisonOperators == null) {
            throw new Exception("Ошибка. Такого \"" + s + "\" оператора не существует.");
        }
        Map.Entry<String, Object> bus = validateCandidate(s);
        String key = bus.getKey();
        Object value = bus.getValue();
        if (value.equals("null") && !(comparisonOperators == ComparisonOperators.EQUAL || comparisonOperators == ComparisonOperators.NOT_EQUAL)) {
            throw new Exception("Некорректный запрос. Нельзя сравнивать с \"NULL\".");
        }
        try {
            switch (comparisonOperators) {
                case LESS:
                    if (value.getClass() == Long.class) {
                        return ((Number) value).longValue() > ((Number) m.get(key)).longValue();
                    } else if (value.getClass() == Double.class) {
                        return ((Number) value).doubleValue() > ((Number) m.get(key)).doubleValue();
                    }
                case GREATER:
                    if (value.getClass() == Long.class) {
                        return ((Number) value).longValue() < ((Number) m.get(key)).longValue();
                    } else if (value.getClass() == Double.class) {
                        return ((Number) value).doubleValue() < ((Number) m.get(key)).doubleValue();
                    }
                case EQUAL:
                    return m.get(key).equals(value);

                case LESS_OR_EQUAL:
                    if (value.getClass() == Long.class) {
                        return ((Number) value).longValue() >= ((Number) m.get(key)).longValue();
                    } else if (value.getClass() == Double.class) {
                        return ((Number) value).doubleValue() >= ((Number) m.get(key)).doubleValue();
                    }

                case GREATER_OR_EQUAL:
                    if (value.getClass() == Long.class) {
                        return ((Number) value).longValue() <= ((Number) m.get(key)).longValue();
                    } else if (value.getClass() == Double.class) {
                        return ((Number) value).doubleValue() <= ((Number) m.get(key)).doubleValue();
                    }

                case NOT_EQUAL:
                    if (m.get(key) == null) {
                        return true;
                    }
                    return !m.get(key).equals(value);

                case LIKE:
                    return String.valueOf(m.get(key)).matches(String.valueOf(value).replaceAll("%", ".*"));

                case I_LIKE:
                    return String.valueOf(m.get(key)).toLowerCase().matches(String.valueOf(value).replaceAll("%", ".*"));
            }
        } catch (NullPointerException e) {
            return false;
        }
        return false;
    }

    private static ComparisonOperators getComparisonOperator(String command) {
        if (command.matches(".*(?i)ilike.*")) {
            return ComparisonOperators.I_LIKE;
        } else if (command.matches(".*(?i)like.*")) {
            return ComparisonOperators.LIKE;
        } else if (command.contains("!=")) {
            return ComparisonOperators.NOT_EQUAL;
        } else if (command.contains(">=")) {
            return ComparisonOperators.GREATER_OR_EQUAL;
        } else if (command.contains("<=")) {
            return ComparisonOperators.LESS_OR_EQUAL;
        } else if (command.contains("=")) {
            return ComparisonOperators.EQUAL;
        } else if (command.contains(">")) {
            return ComparisonOperators.GREATER;
        } else if (command.contains("<")) {
            return ComparisonOperators.LESS;
        } else {
            return null;
        }
    }

}

