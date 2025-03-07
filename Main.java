import java.util.*;
import java.util.function.BiFunction;
import java.math.BigInteger;

public class Main {

    private static final Map<String, Integer> priorities = new HashMap<>();
    static Map<String, BiFunction<BigInteger, BigInteger, BigInteger>> operations = new HashMap<>();
    static HashMap<String, BigInteger> variables = new HashMap<>();

    private static void setUpPriorities() {
        priorities.put("+", 1);
        priorities.put("-", 1);
        priorities.put("*", 2);
        priorities.put("/", 2);
    }

    private static void setUpOperation() {
        operations.put("+", BigInteger::add);
        operations.put("-", BigInteger::subtract);
        operations.put("*", BigInteger::multiply);
        operations.put("/", BigInteger::divide);
    }

    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private static void verifyUserInput(String[] input) throws IllegalArgumentException {
        String availableCharacters = "0123456789+-*/=abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ()";
        for (String token : input) {
            for (int i = 0; i < token.length(); i++) {
                if (!availableCharacters.contains(String.valueOf(token.charAt(0)))) {
                    throw new IllegalArgumentException("Error: Unexpected token ('" + token + "')");
                }
            }
        }
    }

    public static String[] toReverserPolishNotation(String[] input) {
        if (input.length > 1 && isNumeric(input[0]) && isNumeric(input[1])) {
            return input;
        } else if (input.length > 3 && "=".equals(input[1]) && isNumeric(input[2]) && isNumeric(input[3])) {
            return input;
        }
        Stack<String> stackOfOperations = new Stack<>();
        StringBuilder result = new StringBuilder();
        for (String str : input) {
            if (str.equals("(")) {
                stackOfOperations.push("(");
            } else if (str.equals(")")) {
                while (!stackOfOperations.isEmpty() && !"(".equals(stackOfOperations.peek())) {
                    result.append(stackOfOperations.pop()).append(" ");
                }
                if (stackOfOperations.isEmpty()) {
                    throw new IllegalArgumentException("Error: wrong expression");
                }
                stackOfOperations.pop();
            } else if ("+-*/".contains(String.valueOf(str.charAt(0)))) {
                while (!stackOfOperations.isEmpty() && !"(".equals(stackOfOperations.peek()) &&
                        priorities.get(stackOfOperations.peek()) >= priorities.get(str)) {
                    result.append(stackOfOperations.pop()).append(" ");
                }
                stackOfOperations.push(str);
            } else {
                result.append(str).append(" ");
            }
        }
        while (!stackOfOperations.isEmpty()) {
            result.append(stackOfOperations.pop()).append(" ");
        }

        return result.toString().split(" ");
    }

    private static BigInteger calculateResult(String[] input) throws IllegalArgumentException {
        Stack<BigInteger> stack = new Stack<>();
        for (String token : input) {
            if (Character.isDigit(token.charAt(0))) {
                stack.push(new BigInteger(token));
            } else if (operations.containsKey(token)) {
                if (stack.size() < 2) {
                    throw new IllegalArgumentException("Error: Not enough operands for operation '" + token + "'");
                }
                BigInteger secondNumber = stack.pop();
                BigInteger firstNumber = stack.pop();
                if ("/".equals(token) && secondNumber.equals(BigInteger.ZERO)) {
                    throw new IllegalArgumentException("Error: Division by zero");
                }
                BigInteger result = operations.get(token).apply(firstNumber, secondNumber);
                stack.push(result);
            } else if (token.equals("=")) {
                if (isNumeric(input[0])) {
                    throw new IllegalArgumentException("Error: Wrong variable name");
                } else {
                    String variableName = input[0];
                    BigInteger value = calculateResult(Arrays.copyOfRange(input, 2, input.length));
                    variables.put(variableName, value);
                    return value;
                }
            } else if (variables.containsKey(token)) {
                stack.push(variables.get(token));
            } else {
                variables.put(token, BigInteger.ZERO);
                stack.push(variables.get(token));
            }
        }

        if (stack.size() > 1) {
            throw new IllegalArgumentException("Error: Wrong expression");
        }

        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Error: No value to assign to a variable");
        }

        return stack.pop();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String userInput;
        setUpPriorities();
        setUpOperation();
        while (true) {
            userInput = scanner.nextLine().trim();
            String[] inputList = userInput.split(" ");
            if (userInput.isEmpty() || "quit".equals(userInput)) {
                break;
            }
            try {
                verifyUserInput(inputList);
                System.out.println(calculateResult(toReverserPolishNotation(inputList)));
            } catch (IllegalArgumentException exception) {
                System.err.println(exception.getMessage());
            }
        }
    }
}
