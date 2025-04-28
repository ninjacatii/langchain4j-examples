package dev.langchain4j.example.util;
import java.util.List;
import java.util.regex.Pattern;

public class DomainMatcher {

    // 主匹配方法
    public static boolean matchesDomain(String domain, List<String> domainPatterns) {
        for (String pattern : domainPatterns) {
            // 将通配符模式转换为正则表达式
            String regex = convertWildcardToRegex(pattern);
            // 执行不区分大小写的正则匹配
            if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE)
                    .matcher(domain)
                    .matches()) {
                return true;
            }
        }
        return false;
    }

    // 通配符转正则表达式
    private static String convertWildcardToRegex(String pattern) {
        StringBuilder regex = new StringBuilder();
        regex.append("^"); // 匹配开始
        for (char c : pattern.toCharArray()) {
            switch (c) {
                case '*':   // 通配符* → 匹配任意字符
                    regex.append(".*");
                    break;
                case '?':   // 通配符? → 匹配单个字符
                    regex.append('.');
                    break;
                case '.':   // 转义正则特殊字符
                case '\\':
                case '+':
                case '$':
                case '^':
                case '[':
                case ']':
                case '(':
                case ')':
                case '{':
                case '}':
                case '|':
                    regex.append('\\').append(c);
                    break;
                default:    // 普通字符直接追加
                    regex.append(c);
            }
        }
        regex.append("$"); // 匹配结束
        return regex.toString();
    }

    // 示例用法
    public static void main(String[] args) {
        List<String> patterns = List.of("*.example.com", "api.*.net");
        String domain1 = "test.example.com";
        String domain2 = "api.service.net";

        System.out.println(matchesDomain(domain1, patterns)); // 输出 true
        System.out.println(matchesDomain(domain2, patterns)); // 输出 true
        System.out.println(matchesDomain("invalid.org", patterns)); // 输出 false
    }
}
