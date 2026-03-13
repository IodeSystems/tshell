package com.iodesystems.tshell.parser;

import org.antlr.v4.runtime.*;

public abstract class TShellLexerBase extends Lexer {
  protected Token _lastToken = null;

  public TShellLexerBase(CharStream input) {
    super(input);
  }

  @Override
  public Token nextToken() {
    Token t = super.nextToken();
    if (t.getChannel() == Token.DEFAULT_CHANNEL) {
      _lastToken = t;
    }
    return t;
  }

  protected boolean prevTokenCouldEndExpr() {
    if (_lastToken == null) return false;
    switch (_lastToken.getType()) {
      case TShellLexer.IDENTIFIER:
      case TShellLexer.NUMBER:
      case TShellLexer.STRING:
      case TShellLexer.TRUE:
      case TShellLexer.FALSE:
      case TShellLexer.NULL:
      case TShellLexer.RPAREN:
      case TShellLexer.RBRACKET:
      case TShellLexer.RBRACE:
      case TShellLexer.INCREMENT:
      case TShellLexer.DECREMENT:
      case TShellLexer.TEMPLATE_END:
        return true;
      default:
        return false;
    }
  }
}
