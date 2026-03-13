parser grammar TShellParser;
options { tokenVocab=TShellLexer; }

program : statement* EOF;

statement
  : exportStatement
  | letDecl
  | fnDecl
  | ifStatement
  | whileStatement
  | forOfStatement
  | forStatement
  | returnStatement
  | breakStatement
  | continueStatement
  | assignStatement
  | incrDecrStatement
  | expressionStatement
  | SEMI
  ;

exportStatement   : EXPORT (letDecl | fnDecl | assignStatement);

letDecl           : LET destructure ASSIGN expression SEMI?;
fnDecl            : FUNCTION IDENTIFIER LPAREN paramList? RPAREN (COLON typeAnnotation)? block;
returnStatement   : RETURN expression? SEMI?;
breakStatement    : BREAK SEMI?;
continueStatement : CONTINUE SEMI?;
assignStatement   : assignTarget assignOp expression SEMI?;
incrDecrStatement : assignTarget (INCREMENT | DECREMENT) SEMI?;
expressionStatement : expression SEMI?;

assignTarget      : IDENTIFIER (DOT fieldName | LBRACKET expression RBRACKET)*;
assignOp          : ASSIGN | PLUS_ASSIGN | MINUS_ASSIGN | STAR_ASSIGN | SLASH_ASSIGN | PERCENT_ASSIGN;

ifStatement       : IF LPAREN expression RPAREN blockOrStatement (ELSE ifStatement | ELSE blockOrStatement)?;
whileStatement    : WHILE LPAREN expression RPAREN block;
forOfStatement    : FOR LPAREN LET destructure OF expression RPAREN block;
forStatement      : FOR LPAREN (forInitLet | forInitAssign)? SEMI expression? SEMI (forUpdateAssign | forUpdateIncrDecr)? RPAREN block;
forInitLet        : LET IDENTIFIER ASSIGN expression;
forInitAssign     : assignTarget assignOp expression;
forUpdateAssign   : assignTarget assignOp expression;
forUpdateIncrDecr : assignTarget (INCREMENT | DECREMENT);

block             : LBRACE statement* RBRACE;
blockOrStatement  : block | statement;

// Destructuring
destructure
  : IDENTIFIER
  | objectDestructure
  | arrayDestructure
  ;

objectDestructure : LBRACE destructureField (COMMA destructureField)* COMMA? RBRACE;
destructureField  : IDENTIFIER (COLON destructure)? (ASSIGN expression)?;
arrayDestructure  : LBRACKET destructure (COMMA destructure)* COMMA? RBRACKET;

// Parameters
paramList         : param (COMMA param)*;
param             : SPREAD? IDENTIFIER (COLON typeAnnotation)? (ASSIGN expression)?;

// Type annotations (lightweight, for documentation & errors)
typeAnnotation    : IDENTIFIER (LBRACKET RBRACKET)* (PIPE typeAnnotation)?;

// Expressions — precedence from lowest to highest
expression        : ternaryExpr;

ternaryExpr       : nullCoalesceExpr (QUESTION expression COLON expression)?;
nullCoalesceExpr  : orExpr (NULLISH orExpr)*;

orExpr            : andExpr (OR andExpr)*;
andExpr           : equalityExpr (AND equalityExpr)*;
equalityExpr      : comparisonExpr ((EQ | NEQ | SEQ | SNEQ) comparisonExpr)*;
comparisonExpr    : pipeExpr ((LT | GT | LTE | GTE) pipeExpr)*;
pipeExpr          : additiveExpr ((PIPE_RIGHT | PIPE_SCATTER) additiveExpr (PIPE_LEFT additiveExpr)*)*;
additiveExpr      : multiplicativeExpr ((PLUS | MINUS) multiplicativeExpr)*;
multiplicativeExpr: unaryExpr ((STAR | SLASH | PERCENT) unaryExpr)*;

unaryExpr
  : NOT unaryExpr
  | MINUS unaryExpr
  | postfixExpr
  ;

postfixExpr
  : primaryExpr postfixOp*
  ;

postfixOp
  : DOT fieldName
  | OPTIONAL_CHAIN fieldName
  | LBRACKET expression RBRACKET
  | OPTIONAL_CHAIN LBRACKET expression RBRACKET
  | LPAREN argumentList? RPAREN
  | OPTIONAL_CHAIN LPAREN argumentList? RPAREN
  ;

primaryExpr
  : NUMBER                                              # numberLiteral
  | STRING                                              # stringLiteral
  | templateString                                      # templateLiteral
  | TRUE                                                # trueLiteral
  | FALSE                                               # falseLiteral
  | NULL                                                # nullLiteral
  | IDENTIFIER                                          # identifierExpr
  | arrayLiteral                                        # arrayExpr
  | objectLiteral                                       # objectExpr
  | arrowFunction                                       # arrowExpr
  | REGEX                                                # regexExpr
  | LPAREN expression RPAREN                            # parenExpr
  | CHAIN LPAREN argumentList RPAREN                    # chainExpr
  | ALL LPAREN argumentList RPAREN                      # allExpr
  | RACE LPAREN argumentList RPAREN                     # raceExpr
  | ANY LPAREN argumentList RPAREN                      # anyExpr
  ;

// Arrow functions
arrowFunction
  : IDENTIFIER ARROW expression                         # singleParamArrow
  | LPAREN paramList? RPAREN (COLON typeAnnotation)? ARROW expression  # multiParamArrow
  | IDENTIFIER ARROW block                              # singleParamArrowBlock
  | LPAREN paramList? RPAREN (COLON typeAnnotation)? ARROW block       # multiParamArrowBlock
  ;

// Literals
arrayLiteral      : LBRACKET (spreadOrExpr (COMMA spreadOrExpr)* COMMA?)? RBRACKET;
objectLiteral     : LBRACE (objectField (COMMA objectField)* COMMA?)? RBRACE;
objectField
  : fieldName COLON expression                          # namedField
  | IDENTIFIER                                          # shorthandField
  | SPREAD expression                                   # spreadField
  | LBRACKET expression RBRACKET COLON expression       # computedField
  ;

fieldName
  : IDENTIFIER | LET | FUNCTION | IF | ELSE | WHILE | FOR | OF | IN | RETURN
  | BREAK | CONTINUE | EXPORT | TRUE | FALSE | NULL | CHAIN | ALL | RACE | ANY
  ;

spreadOrExpr      : SPREAD? expression;

templateString    : TEMPLATE_START templatePart* TEMPLATE_END;
templatePart
  : TEMPLATE_TEXT                                       # templateText
  | TEMPLATE_EXPR expression RBRACE                     # templateInterp
  ;

argumentList      : callArg (COMMA callArg)*;
callArg
  : IDENTIFIER COLON expression                          # namedCallArg
  | spreadOrExpr                                         # positionalCallArg
  ;
