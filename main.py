import re
from enum import Enum  


class CustomException(Exception):
    pass

class TokenType(Enum):
    KEYWORD = 1
    IDENTIFIER = 2
    INTEGER = 3
    OPERATOR = 4
    STRING = 5
    PUNCTUATION = 6

class Token:
    def _init_(self, token_type, value):
        self.token_type = token_type
        self.value = value


class LexicalAnalyser:
    def _init_(self, inputFileName):
        self.inputFileName = inputFileName
        self.tokens = []

    def scan(self):
        try:
            with open(self.inputFileName, 'r') as reader:
                lineCount = 0
                for line in reader:
                    lineCount += 1
                    try:
                        self.tokenizeLine(line.strip(), lineCount)
                    except CustomException as e:
                        raise CustomException(f"{e} in LINE: {lineCount}\nERROR in lexical_analysis.")
        except FileNotFoundError as e:
            print(f"File not found: {self.inputFileName}")

        return self.tokens

    def tokenizeLine(self, line, lineCount):
        digit = "[0-9]"
        letter = "[a-zA-Z]"
        operatorSymbol = "[+\\-*/<>&.@/:=~|$!#%^_\\[\\]{}\"`'\\?]"
        escape = "(\\\\'|\\\\t|\\\\n|\\\\\\\\)"

        identifierPattern = re.compile(f"{letter}({letter}|{digit}|_)*")
        integerPattern = re.compile(f"{digit}+")
        operatorPattern = re.compile(f"{operatorSymbol}+")
        punctuationPattern = re.compile("[(),;]")
        spacesPattern = re.compile(r"(\s|\t)+")

        stringPattern = re.compile(f"''({letter}|{digit}|{operatorSymbol}|{escape}|{punctuationPattern}|{spacesPattern})*''")
        commentPattern = re.compile("//.*")

        currentIndex = 0
        while currentIndex < len(line):
            currentChar = line[currentIndex]

            spaceMatcher = spacesPattern.match(line[currentIndex:])
            commentMatcher = commentPattern.match(line[currentIndex:])
            if commentMatcher:
                currentIndex += len(commentMatcher.group())
                continue
            if spaceMatcher:
                currentIndex += len(spaceMatcher.group())
                continue

            matcher = identifierPattern.match(line[currentIndex:])
            if matcher:
                identifier = matcher.group()
                keywords = [
                    "let", "in", "fn", "where", "aug", "or", "not", "gr", "ge", "ls",
                    "le", "eq", "ne", "true", "false", "nil", "dummy", "within", "and", "rec"
                ]
                if identifier in keywords:
                    self.tokens.append(Token(TokenType.KEYWORD, identifier))
                else:
                    self.tokens.append(Token(TokenType.IDENTIFIER, identifier))
                currentIndex += len(identifier)
                continue

            matcher = integerPattern.match(line[currentIndex:])
            if matcher:
                integer = matcher.group()
                self.tokens.append(Token(TokenType.INTEGER, integer))
                currentIndex += len(integer)
                continue

            matcher = operatorPattern.match(line[currentIndex:])
            if matcher:
                operator = matcher.group()
                self.tokens.append(Token(TokenType.OPERATOR, operator))
                currentIndex += len(operator)
                continue

            matcher = stringPattern.match(line[currentIndex:])
            if matcher:
                string = matcher.group()
                self.tokens.append(Token(TokenType.STRING, string))
                currentIndex += len(string)
                continue

            if currentChar in ",;":
                self.tokens.append(Token(TokenType.PUNCTUATION, currentChar))
                currentIndex += 1
                continue

            raise CustomException(f"Unable to tokenize the CHARACTER: {currentChar} at INDEX: {currentIndex}")

# Example usage:
if _name_ == "_main_":
    inputFileName = "Input.txt"
    lexer = LexicalAnalyser(inputFileName)
    tokens = lexer.scan()
    for token in tokens:
        print(token.token_type, token.value)