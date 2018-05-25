import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.*;
import java.util.*;
import java.lang.Math;

public class Main {
	public static void main(String[] args) {
		try {
            // Read content from file
			File file = new File("test.vc");
			FileReader fileReader = new FileReader(file);
			StringBuffer stringBuffer = new StringBuffer();
			int numCharsRead;
			char[] charArray = new char[1024];
			while ((numCharsRead = fileReader.read(charArray)) > 0) {
				stringBuffer.append(charArray, 0, numCharsRead);
			}
			fileReader.close();
            System.out.println("Contents of file:");
            String content = stringBuffer.toString();
            System.out.println(content);

            // Process the content
            Solver solver = new Solver();
            content = solver.eliminateEndLine(content);
            solver.process(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    static class Solver {
        private boolean isBreaker(char c) {
            if (c == ' ') return true;
            return false;
        }

        private boolean isFraction(char c) {
            if (c == '.') return true;
            return false;
        }

        private boolean isLetter(char c) {
            if ('a' <= c && c <= 'z') return true;
            if ('Z' <= c && c <= 'Z') return true;
            if (c == '_') return true;
            return false;
        }

        private boolean isDigit(char c) {
            if ('0' <= c && c <= '9') return true;
            return false;
        }

        private boolean accept(char c) {
            if (c == '.') return true; // dot
            if (isLetter(c)) return true;
            if (isDigit(c)) return true;
            String operators = new String("+-*/><=!|&"); // chars representing operators
            for (int i = 0; i < operators.length(); i++) {
                if (c == operators.charAt(i)) {
                    return true;
                }
            }
            String separators = new String("{}[]();,"); // chars representing separators
            for (int i = 0; i < separators.length(); i++) {
                if (c == separators.charAt(i)) {
                    return true;
                }
            }
            return false;
        }

        public String eliminateEndLine(String s) {
            // Convert all the chars that is not "accepted" to ' ' (space), e.g. '\n' --> ' '
            char[] tmp = s.toCharArray();
            for (int i = 0; i < s.length(); i++) {
                if (!accept(s.charAt(i))) {
                    tmp[i] = ' ';
                }
            }
            return String.valueOf(tmp);
        }

        private int identifierEvaluate(String s) {
            // Just two types of values: -1 or 1
            if (s.length() == 0) return -1;
            if (!isLetter(s.charAt(0))) return -1;
            for (int i = 1; i < s.length(); i++) {
                if (!isLetter(s.charAt(i)) && !isDigit(s.charAt(i))) {
                    return -1;
                }
            }
            return 1;
        }
//kiem tra dinh dang kieu du lieu
        private int keywordEvaluate(String s) {
            String keywords[] = {"boolean", "break", "continue", "else", "for", "float", "if", "int", "return", "void", "while"};
            for (int i = 0; i < keywords.length; i++) {
                if (s.equals(keywords[i])) {
                    return 1;
                }
            }
            for (int i = 0; i < keywords.length; i++) {
                String tmp = new String();
                tmp = keywords[i].substring(0, Math.min(keywords[i].length(), s.length()));
                if (s.equals(tmp)) {
                    return 0;
                }
            }
            return -1;
        }

        private int operatorEvaluate(String s) {
            String operators[] = {"+", "-", "*", "/", "<", "<=", ">", ">=", "==", "!=", "||", "&&", "!", "="};
            for (int i = 0; i < operators.length; i++) {
                if (s.equals(operators[i])) {
                    return 1;
                }
            }
            for (int i = 0; i < operators.length; i++) {
                String tmp = new String();
                tmp = operators[i].substring(0, Math.min(operators[i].length(), s.length()));
                if (s.equals(tmp)) {
                    return 0;
                }
            }
            return -1;
        }

        private int separatorEvaluate(String s) {
            // Just two types of values: -1 or 1
            String tmp = new String("{}()[];,");
            if (s.length() != 1) return -1;
            for (int i = 0; i < tmp.length(); i++) {
                if (s.charAt(0) == tmp.charAt(i)) {
                    return 1;
                }
            }
            return -1;
        }

        private int intLiteralEvaluate(String s) {
            // Just two types of values: -1 or 1
            if (s.length() == 0) return -1;
            for (int i = 0; i < s.length(); i++) {
                if (!isDigit(s.charAt(i))) {
                    return -1;
                }
            }
            return 1;
        }

        private int exponentEvaluate(String s) {
            if (s.length() == 0) return 1; // Special case: return 1 when length == 0 (because exponent?)
            if (s.length() == 1) return -1; // Length must be >= 2
            if (s.charAt(0) != 'E' || s.charAt(0) != 'e') return -1;

            String tmp = new String();
            if (s.charAt(1) == '+' || s.charAt(1) == '-') {
                if (s.length() == 2) {
                    return 0;
                } else {
                    tmp = s.substring(2); // e.g.: e+... --> ...
                }
            } else {
                tmp = s.substring(1); // e.g.: e... --> ...
            }

            if (tmp.length() == 0) {
                return 0; // e.g.: "e+" --> "", return value = 0 
            } 
            return intLiteralEvaluate(tmp);
        }

        private int floatLiteralEvaluate(String s) {
            if (s.length() == 0) return -1;
            int fractionPosition = -1;
            for (int i = 0; i < s.length(); i++) {
                if (isFraction(s.charAt(i))) {
                    fractionPosition = i;
                    break;
                }
            }
            if (fractionPosition == -1) {
                return intLiteralEvaluate(s);
            }

            int val1;
            if (fractionPosition == 0) {
                val1 = 1;
            } else {
                val1 = intLiteralEvaluate(s.substring(0, fractionPosition));
            }
            String s1 = new String();
            s1 = s.substring(fractionPosition + 1);
            int val2 = exponentEvaluate(s1);
            return Math.min(val1, val2);
        }

        private int canBeValidToken(String s, char c) { // Maximum value of evaluations: (-1, 0, 1) = (can't be, can be, be) valid token
            int val1 = identifierEvaluate(s + c);
            int val2 = keywordEvaluate(s + c);
            int val3 = operatorEvaluate(s + c);
            int val4 = separatorEvaluate(s + c);
            int val5 = intLiteralEvaluate(s + c);
            int val6 = floatLiteralEvaluate(s + c);
            return Math.max(Math.max(Math.max(val1, val2), Math.max(val3, val4)), Math.max(val5, val6));
        }

        private int printTypeOfToken(String s) {
            int val[] = new int[10];
            // Note: swap keyword and identifier in order to satisfy priority
            val[1] = keywordEvaluate(s);
            val[2] = identifierEvaluate(s);
            val[3] = operatorEvaluate(s);
            val[4] = separatorEvaluate(s);
            val[5] = intLiteralEvaluate(s);
            val[6] = floatLiteralEvaluate(s);
            int maxValue = Math.max(Math.max(Math.max(val[1], val[2]), Math.max(val[3], val[4])), Math.max(val[5], val[6]));
            
            if (maxValue != 1) { // s is none-type
                return 0; // Fail
            }

            String types[] = {"", "keyword", "identifier", "operator", "separator", "int_literal", "float_literal"};
            for (int i = 1; i <= 6; i++) {
                if (val[i] == maxValue) {
                    System.out.print(types[i]);
                    break;
                }
            }
            return 1; // Success
        }

        public void process(String content) {
            // Initialize
            Map<String, String> map = new HashMap<String, String>();  
            String generalType[] = {"boolean", "break", "continue", "else", "for", "float", "if", "int", "return", "void", "while",
                                    "+", "-", "*", "/", "<", "<=", ">", ">=", "==", "!=", "||", "&&", "!", "=",
                                    "{", "}", "(", ")", "[", "]", ";", ","
                                    };
            String detailType[] = {"boolean_keyword", "break_keyword", "continue_keyword", "else_keyword", "for_keyword", 
                                    "float_keyword", "if_keyword", "int_keyword", "return_keyword", "void_keyword", "while_keyword",
                                    "plus_operator", "minus_operator", "multiply_operator", "divide_operator", "less_operator", 
                                    "less_or_equal_operator", "greater_operator", "greater_or_equal_operator", 
                                    "equal_operator", "not_equal_operator", "or_operator", "and_operator", "not_operator", "assignment_operator",
                                    "left_curly_bracket", "right_curly_bracket", "left_round_bracket", 
                                    "right_round_bracket", "left_square_bracket", "right_square_bracket", "semicolon", "comma"
                                    };
            for (int i = 0; i < generalType.length; i++) {
                map.put(generalType[i], detailType[i]);
            }

            // Begin processing
            System.out.println("After eliminating:");
            System.out.println(content);
            System.out.println("Process:");
            String currentToken = new String("");
            for (int i = 0; i < content.length(); i++) {
                if (content.charAt(i) == ' ') {
                    if (currentToken.length() != 0) {
                        System.out.print(currentToken + " ");  // Note: these commands in this block is nearly identical with the under commands
                        int result = printTypeOfToken(currentToken);

                        if (result == 0) {
                            System.out.print("Error: ");
                            System.out.print(currentToken);
                            System.out.print(" is none-type.");
                            return;
                        }

                        if (map.get(currentToken) != null) {
                            System.out.print(" " + map.get(currentToken));
                        }
                        System.out.println();
                        currentToken = "";
                    }
                } else if (canBeValidToken(currentToken, content.charAt(i)) < 0) { // It is surely not a valid token
                    System.out.print(currentToken + " "); // Note: these commands in this block is nearly identical with the above commands
                    int result = printTypeOfToken(currentToken);

                    if (result == 0) {
                        System.out.print("Error: ");
                        System.out.print(currentToken);
                        System.out.print(" is none-type.");
                        return;
                    }

                    if (map.get(currentToken) != null) {
                        System.out.print(" " + map.get(currentToken));
                    }
                    System.out.println();
                    currentToken = "" + content.charAt(i);
                } else {
                    currentToken += content.charAt(i);
                }
            }
        }
    }
}