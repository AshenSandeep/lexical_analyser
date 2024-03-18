import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Main {
    public static void main(String[] args) throws Exception {
        // Scanner scanner = new Scanner(System.in);
        // System.out.println("Enter file name : ");
        String fileName = "input.txt";
        // scanner.nextLine();
        File file = new FileHandler(fileName).getFile();
        LexiacalAnalyser analyser = new LexiacalAnalyser(file);
        List<Token> tokens = analyser.analyse();
        printTokens(tokens);
        // scanner.close();
    }

    private static void printTokens(List<Token> tokens) {
        for (Token token : tokens) {
            System.out.println(token.value);
        }
    }
}

enum TokenType {
    KEYWORD,
    IDENTIFIER,
    OPERATOR,
    INTEGER,
    STRING,
    PUNCTUATION,
    COMMENT,
}

class AnalyserException extends Exception {
    public AnalyserException(String error) {
        super(error);
    }
}

class Token {
    private TokenType tokenType;
    String value;

    public Token(TokenType tokenType, String value) {
        this.tokenType = tokenType;
        this.value = value;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getTokeValue() {
        return value;
    }
}

class FileHandler {
    private String fileName;

    public FileHandler(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() throws Exception {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException(fileName);
        }

        if (file.isDirectory()) {
            throw new Exception("This is a directory");
        }

        if (!file.canRead()) {
            throw new Exception("Cannot read " + fileName);
        }
        return file;
    }
}

class LexiacalAnalyser {
    private File file;
    private List<Token> tokens;

    public LexiacalAnalyser(File file) {
        this.file = file;
        tokens = new ArrayList<Token>();
    }

    public List<Token> analyse() throws Exception {
        tokens.clear();
        String line;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        int lineNumber = 1;
        while ((line = bufferedReader.readLine()) != null) {
            try {
                String trimmedLine = line.trim();
                // System.out.println(trimmedLine.length());
                scanLine(trimmedLine);
                lineNumber++;
            } catch (AnalyserException e) {
                System.out.println("Error on line : " + lineNumber + " : " + e.getMessage());
                break;
            }
        }

        bufferedReader.close();
        return tokens;
    }

    private void scanLine(String line) throws AnalyserException {
        String letters = "[a-zA-Z]";
        String digits = "[0-9]";
        String operators = "[+\\-*<>&.@\\/:=~|$!#%^_\\[\\]{}\"'`?]";
        String escapeCharacters = "(\\\\'|\\\\t|\\\\n|\\\\\\\\)";

        Pattern identifierPattern = Pattern.compile(String.format("%s(%s|%s|_)*", letters, letters, digits));
        Pattern integerPattern = Pattern.compile(String.format("(%s)+", digits));
        Pattern punctuationPattern = Pattern.compile("[(),;]");
        Pattern operaterPattern = Pattern.compile(String.format("%s+", operators));
        Pattern spacesPattern = Pattern.compile("(\s|\t)+");
        Pattern stringPattern = Pattern.compile(String.format("''(%s|%s|%s|%s|%s|%s)*''", letters, digits, operators,
                escapeCharacters, "(\s|\t)+", "[(),;]"));
        Pattern commentPattern = Pattern
                .compile("//.*");

        int currentCharIndex = 0;

        while (currentCharIndex < line.length()) {
            char currentCharacter = line.charAt(currentCharIndex);
            String charAsString = String.valueOf(currentCharacter);
            String currentSubString = line.substring(currentCharIndex);
            // Comment found
            Matcher commentMatcher = commentPattern.matcher(line.substring(currentCharIndex));
            if (commentMatcher.find()) {
                String matchGroup = commentMatcher.group();
                currentCharIndex += commentMatcher.group().length();
                continue;
            }

            // Spaces found
            Matcher spaceMatcher = spacesPattern.matcher(line.substring(currentCharIndex));
            if (spaceMatcher.find()) {
                currentCharIndex += spaceMatcher.group().length();
                continue;
            }

            // keyword/identifier found
            Matcher identiferMatcher = identifierPattern.matcher(line.substring(currentCharIndex));
            if (identiferMatcher.find()) {
                String identifier = identiferMatcher.group();
                String[] keywords = { "let", "in", "fn", "where", "aug", "or", "not", "gr", "ge", "ls",
                        "le", "eq", "ne", "true", "false", "nil", "dummy", "within", "and", "rec", "Sum" };

                if (Arrays.asList(keywords).contains(identifier)) {
                    tokens.add(new Token(TokenType.KEYWORD, identifier));
                }

                else {
                    tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                }
                currentCharIndex += identifier.length();
                continue;
            }

            // integer found
            Matcher integerMatcher = integerPattern.matcher(line.substring(currentCharIndex));
            if (integerMatcher.find()) {
                String integer = integerMatcher.group();
                tokens.add(new Token(TokenType.INTEGER, integer));
                currentCharIndex += integer.length();
                continue;
            }

            // operator found
            Matcher operatorMatcher = operaterPattern.matcher(line.substring(currentCharIndex));
            if (operatorMatcher.find()) {
                String operator = operatorMatcher.group();
                tokens.add(new Token(TokenType.OPERATOR, operator));
                currentCharIndex += operator.length();
                continue;
            }

            // Strings found
            Matcher stringMatcher = stringPattern.matcher(line.substring(currentCharIndex));
            if (stringMatcher.find()) {
                String str = stringMatcher.group();
                tokens.add(new Token(TokenType.STRING, str));
                currentCharIndex += str.length();
                continue;
            }
            //Punctuation found updated

            Matcher punctuationMatcher = punctuationPattern.matcher((line.substring(currentCharacter)));
            if(punctuationMatcher.find()){
                String punch = punctuationMatcher.group();
                tokens.add(new Token(TokenType.PUNCTUATION, punch));
                currentCharIndex+= punch.length();
                continue;
            }
            // Punctuation found
            // if (",;".contains(charAsString)) {
            //     tokens.add(new Token(TokenType.PUNCTUATION, charAsString));
            //     currentCharIndex++;
            //     continue;
            // }
            throw new AnalyserException("Unable identify character " + charAsString + " at index " + currentCharIndex);
        }
    }
}-