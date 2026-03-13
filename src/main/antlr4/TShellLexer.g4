lexer grammar TShellLexer;

// Keywords
LET     : 'let' | 'const';
FUNCTION: 'function';
IF      : 'if';
ELSE    : 'else';
WHILE   : 'while';
FOR     : 'for';
OF      : 'of';
IN      : 'in';
RETURN  : 'return';
BREAK   : 'break';
CONTINUE: 'continue';
EXPORT  : 'export';
TRUE    : 'true';
FALSE   : 'false';
NULL    : 'null';

// Composition
CHAIN   : 'chain';
ALL     : 'all';
RACE    : 'race';

// Operators (multi-char before single-char for correct matching)
PIPE_RIGHT  : '|>';
PIPE_SCATTER: '|*';
PIPE_LEFT   : '<|';
PIPE        : '|';
ARROW       : '=>';
SPREAD      : '...';
INCREMENT   : '++';
DECREMENT   : '--';
PLUS_ASSIGN : '+=';
MINUS_ASSIGN: '-=';
STAR_ASSIGN : '*=';
SLASH_ASSIGN: '/=';
PERCENT_ASSIGN: '%=';
PLUS        : '+';
MINUS       : '-';
STAR        : '*';
SLASH       : '/';
PERCENT     : '%';
SEQ         : '===';
SNEQ        : '!==';
EQ          : '==';
NEQ         : '!=';
LTE         : '<=';
GTE         : '>=';
LT          : '<';
GT          : '>';
AND         : '&&';
OR          : '||';
NULLISH     : '??';
NOT         : '!';
OPTIONAL_CHAIN : '?.';
ASSIGN      : '=';
QUESTION    : '?';
COLON       : ':';
DOT         : '.';
COMMA       : ',';
SEMI        : ';';

// Delimiters
LPAREN      : '(';
RPAREN      : ')';
LBRACKET    : '[';
RBRACKET    : ']';
LBRACE      : '{' -> pushMode(DEFAULT_MODE);
RBRACE      : '}' -> popMode;

// Literals
NUMBER
  : [0-9]+ ('.' [0-9]+)?
  | '.' [0-9]+
  ;

STRING
  : '"' (~["\\\r\n] | '\\' .)* '"'
  | '\'' (~['\\\r\n] | '\\' .)* '\''
  ;

TEMPLATE_START : '`' -> pushMode(TEMPLATE_MODE);

// Identifiers
IDENTIFIER : [a-zA-Z_$] [a-zA-Z0-9_$]*;

// Whitespace & comments
WS          : [ \t\r\n]+ -> skip;
LINE_COMMENT: '//' ~[\r\n]* -> skip;
BLOCK_COMMENT: '/*' .*? '*/' -> skip;

// Template string mode
mode TEMPLATE_MODE;
TEMPLATE_TEXT   : (~[`\\$] | '\\' . | '$' ~'{')+;
TEMPLATE_EXPR   : '${' -> pushMode(DEFAULT_MODE);
TEMPLATE_END    : '`' -> popMode;
