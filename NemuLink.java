import java.util.*;
import java.util.regex.*;
import java.util.Scanner;
import java.nio.file.*;
import java.io.IOException;
import java.util.Base64;

public class NemuLink {
    private static Map<String, Object> variables = new HashMap<>();
    private static int lineNumber = 0;
    private static boolean interactiveMode = true;
    private static final String VERSION = "NemuLink r1.0.5";
    
    public static void main(String[] args) {
        if (args.length > 0) {
            // 文件模式
            interactiveMode = false;
            try {
                String program = String.join("\n", Files.readAllLines(Paths.get(args[0])));
                execute(program);
            } catch (IOException e) {
                System.err.println("无法读取文件: " + args[0]);
                System.exit(1);
            }
        } else {
            // 交互模式
            startInteractiveSession();
        }
    }
    
    private static void startInteractiveSession() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("NemuLink 交互式解释器 (" + VERSION + ")");
        System.out.println("输入代码执行，或输入以下命令:");
        System.out.println("  /help    - 显示帮助");
        System.out.println("  /vars    - 显示所有变量");
        System.out.println("  /clear   - 清除所有变量");
        System.out.println("  /ver     - 显示版本号");
        System.out.println("  /game    - 开始数字猜谜游戏");
        System.out.println("  /exit    - 退出");
        
        while (true) {
            System.out.print("NlkShell> ");
            String input = scanner.nextLine().trim();
            
            if (input.startsWith("/")) {
                handleMetaCommand(input, scanner);
                continue;
            }
            
            if (input.equalsIgnoreCase("exit")) {
                break;
            }
            
            try {
                execute(input);
            } catch (Exception e) {
                System.err.println("错误: " + e.getMessage());
                if (!interactiveMode) {
                    System.exit(1);
                }
            }
        }
        
        scanner.close();
        System.out.println("再见！");
    }
    
    private static void handleMetaCommand(String command, Scanner scanner) {
        switch (command.substring(1).toLowerCase()) {
            case "help":
                printHelp();
                break;
            case "vars":
                printVariables();
                break;
            case "clear":
                variables.clear();
                System.out.println("所有变量已清除");
                break;
            case "ver":
                System.out.println(VERSION + "bilibili@富士見ねむ");
                break;
            case "game":
                startGame();
                break;
            case "exit":
                System.exit(0);
                break;
            case "multiline":
                handleMultilineInput(scanner);
                break;
            default:
                System.out.println("未知命令: " + command);
        }
    }
    
    private static void startGame() {
        try {
            // 从bin目录加载游戏类
            Class<?> gameClass = Class.forName("NumberGuessGame");
            gameClass.getMethod("start").invoke(null);
        } catch (Exception e) {
            System.err.println("因为java或某些方面bug 目前本功能无法正常运行" + e.getMessage());
            System.err.println("游戏主类文件位于bin文件夹 可以正常运行");
        }
    }
    
    private static void handleMultilineInput(Scanner scanner) {
        System.out.println("多行输入模式 (输入单独一行的 '.' 结束)");
        StringBuilder program = new StringBuilder();
        
        while (true) {
            System.out.print("... ");
            String line = scanner.nextLine();
            if (line.trim().equals(".")) {
                break;
            }
            program.append(line).append("\n");
        }
        
        try {
            execute(program.toString());
        } catch (Exception e) {
            System.err.println("执行错误: " + e.getMessage());
        }
    }
    
    private static void printHelp() {
        System.out.println("NemuLink 语言帮助:");
        System.out.println("  prt \"<字符串>\"  - 输出字符串");
        System.out.println("  prt <数字>      - 输出数字");
        System.out.println("  set <变量> <值> - 设置变量");
        System.out.println("  if <条件> then <命令> [else <命令>] - 条件语句");
        System.out.println("数学表达式:");
        System.out.println("  add <a> <b>     - 加法");
        System.out.println("  sub <a> <b>     - 减法");
        System.out.println("  mul <a> <b>     - 乘法");
        System.out.println("  div <a> <b>     - 除法");
        System.out.println("Base64编解码:");
        System.out.println("  b64enc \"<字符串>\" - Base64编码");
        System.out.println("  b64dec \"<字符串>\" - Base64解码");
        System.out.println("比较运算符:");
        System.out.println("  eq, ne, gt, lt, ge, le");
        System.out.println("系统命令:");
        System.out.println("  /game - 开始数字猜谜游戏");
    }
    
    private static void printVariables() {
        if (variables.isEmpty()) {
            System.out.println("没有定义变量");
            return;
        }
        System.out.println("当前变量:");
        variables.forEach((k, v) -> System.out.printf("  %s = %s (%s)%n", 
            k, v, v.getClass().getSimpleName()));
    }
    
    public static void execute(String program) {
        String[] lines = program.split("\n");
        
        for (lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            String line = lines[lineNumber].trim();
            if (line.isEmpty() || line.startsWith("//")) continue;
            
            try {
                if (line.startsWith("prt ")) {
                    handlePrint(line.substring(4).trim());
                } else if (line.startsWith("set ")) {
                    handleSet(line.substring(4).trim());
                } else if (line.startsWith("if ")) {
                    handleIf(line.substring(3).trim());
                } else if (line.startsWith("b64enc ")) {
                    handleBase64Encode(line.substring(7).trim());
                } else if (line.startsWith("b64dec ")) {
                    handleBase64Decode(line.substring(7).trim());
                } else if (line.startsWith("add ") || line.startsWith("sub ") || 
                         line.startsWith("mul ") || line.startsWith("div ")) {
                    throw new RuntimeException("数学表达式必须嵌套在 prt 或 set 命令中使用");
                } else {
                    throw new RuntimeException("未知命令: " + line);
                }
            } catch (Exception e) {
                if (interactiveMode) {
                    System.err.println("第 " + (lineNumber+1) + " 行错误: " + e.getMessage());
                } else {
                    throw new RuntimeException("第 " + (lineNumber+1) + " 行错误: " + e.getMessage());
                }
            }
        }
    }
    
    private static void handlePrint(String args) {
        if (args.startsWith("\"") && args.endsWith("\"")) {
            System.out.println(args.substring(1, args.length()-1));
        } else {
            Object value = evaluateExpression(args);
            System.out.println(value != null ? value.toString() : "null");
        }
    }
    
    private static void handleSet(String args) {
        String[] parts = args.split("\\s+", 2);
        if (parts.length < 2) {
            throw new RuntimeException("无效的 set 命令");
        }
        
        String varName = parts[0];
        Object value = evaluateExpression(parts[1]);
        variables.put(varName, value);
        
        if (interactiveMode) {
            System.out.println(varName + " = " + value);
        }
    }
    
    private static void handleBase64Encode(String args) {
        if (!args.startsWith("\"") || !args.endsWith("\"")) {
            throw new RuntimeException("b64enc 需要一个带引号的字符串参数");
        }
        String str = args.substring(1, args.length()-1);
        String encoded = Base64.getEncoder().encodeToString(str.getBytes());
        System.out.println(encoded);
    }
    
    private static void handleBase64Decode(String args) {
        if (!args.startsWith("\"") || !args.endsWith("\"")) {
            throw new RuntimeException("b64dec 需要一个带引号的字符串参数");
        }
        String str = args.substring(1, args.length()-1);
        try {
            byte[] decoded = Base64.getDecoder().decode(str);
            System.out.println(new String(decoded));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的Base64字符串");
        }
    }
    
    private static void handleIf(String args) {
        String[] thenElseSplit = args.split(" then ", 2);
        if (thenElseSplit.length < 2) {
            throw new RuntimeException("无效的 if 命令 - 缺少 then");
        }
        
        String condition = thenElseSplit[0];
        String[] elseSplit = thenElseSplit[1].split(" else ", 2);
        String thenBlock = elseSplit[0];
        String elseBlock = elseSplit.length > 1 ? elseSplit[1] : null;
        
        boolean conditionResult = evaluateCondition(condition);
        
        if (conditionResult) {
            executeCommand(thenBlock);
        } else if (elseBlock != null) {
            executeCommand(elseBlock);
        }
    }
    
    private static boolean evaluateCondition(String condition) {
        String[] parts = condition.split("\\s+", 3);
        if (parts.length != 3) {
            throw new RuntimeException("无效的条件: " + condition);
        }
        
        String operator = parts[0];
        Object left = evaluateExpression(parts[1]);
        Object right = evaluateExpression(parts[2]);
        
        if (left instanceof Number && right instanceof Number) {
            double leftNum = ((Number)left).doubleValue();
            double rightNum = ((Number)right).doubleValue();
            
            switch (operator) {
                case "eq": return leftNum == rightNum;
                case "ne": return leftNum != rightNum;
                case "gt": return leftNum > rightNum;
                case "lt": return leftNum < rightNum;
                case "ge": return leftNum >= rightNum;
                case "le": return leftNum <= rightNum;
                default: throw new RuntimeException("未知的比较运算符: " + operator);
            }
        } else {
            String leftStr = left.toString();
            String rightStr = right.toString();
            
            switch (operator) {
                case "eq": return leftStr.equals(rightStr);
                case "ne": return !leftStr.equals(rightStr);
                default: throw new RuntimeException("运算符 " + operator + " 不支持字符串比较");
            }
        }
    }
    
    private static Object evaluateExpression(String expr) {
        if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return expr.substring(1, expr.length()-1);
        } else if (variables.containsKey(expr)) {
            return variables.get(expr);
        } else if (isNumber(expr)) {
            if (expr.contains(".")) {
                return Double.parseDouble(expr);
            } else {
                return Integer.parseInt(expr);
            }
        } else if (expr.startsWith("add ") || expr.startsWith("sub ") || 
                  expr.startsWith("mul ") || expr.startsWith("div ")) {
            return evaluateMath(expr);
        }
        throw new RuntimeException("未知的表达式: " + expr);
    }
    
    private static Object evaluateMath(String expr) {
        String[] parts = expr.split("\\s+");
        if (parts.length != 3) {
            throw new RuntimeException("无效的数学表达式: " + expr);
        }
        
        double left = getNumberValue(parts[1]);
        double right = getNumberValue(parts[2]);
        
        switch (parts[0]) {
            case "add": return left + right;
            case "sub": return left - right;
            case "mul": return left * right;
            case "div": 
                if (right == 0) throw new RuntimeException("除零错误");
                return left / right;
            default: throw new RuntimeException("未知的数学运算: " + parts[0]);
        }
    }
    
    private static double getNumberValue(String s) {
        Object val = evaluateExpression(s);
        if (val instanceof Number) {
            return ((Number)val).doubleValue();
        }
        throw new RuntimeException("需要数字但得到: " + val);
    }
    
    private static void executeCommand(String cmd) {
        if (cmd.startsWith("prt ")) {
            handlePrint(cmd.substring(4).trim());
        } else if (cmd.startsWith("set ")) {
            handleSet(cmd.substring(4).trim());
        } else {
            throw new RuntimeException("不支持的块内命令: " + cmd);
        }
    }
    
    private static boolean isNumber(String s) {
        return s.matches("-?\\d+(\\.\\d+)?");
    }
}