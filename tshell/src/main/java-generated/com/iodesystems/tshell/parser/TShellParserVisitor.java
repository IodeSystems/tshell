// Generated from /home/nthalk/local/src/iodesystems/tshell/tshell/src/main/antlr4/TShellParser.g4 by ANTLR 4.13.2
package com.iodesystems.tshell.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link TShellParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface TShellParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link TShellParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(TShellParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(TShellParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#exportStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExportStatement(TShellParser.ExportStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#letDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLetDecl(TShellParser.LetDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#fnDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFnDecl(TShellParser.FnDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#returnStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStatement(TShellParser.ReturnStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#breakStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakStatement(TShellParser.BreakStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#continueStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueStatement(TShellParser.ContinueStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#assignStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignStatement(TShellParser.AssignStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#incrDecrStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIncrDecrStatement(TShellParser.IncrDecrStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(TShellParser.ExpressionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#assignTarget}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignTarget(TShellParser.AssignTargetContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#assignOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignOp(TShellParser.AssignOpContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#ifStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStatement(TShellParser.IfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#whileStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileStatement(TShellParser.WhileStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#forOfStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForOfStatement(TShellParser.ForOfStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#forStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForStatement(TShellParser.ForStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#forInitLet}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForInitLet(TShellParser.ForInitLetContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#forInitAssign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForInitAssign(TShellParser.ForInitAssignContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#forUpdateAssign}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForUpdateAssign(TShellParser.ForUpdateAssignContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#forUpdateIncrDecr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForUpdateIncrDecr(TShellParser.ForUpdateIncrDecrContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(TShellParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#blockOrStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockOrStatement(TShellParser.BlockOrStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#destructure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDestructure(TShellParser.DestructureContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#objectDestructure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectDestructure(TShellParser.ObjectDestructureContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#destructureField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDestructureField(TShellParser.DestructureFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#arrayDestructure}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayDestructure(TShellParser.ArrayDestructureContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#paramList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamList(TShellParser.ParamListContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#param}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParam(TShellParser.ParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#typeAnnotation}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeAnnotation(TShellParser.TypeAnnotationContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(TShellParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#ternaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTernaryExpr(TShellParser.TernaryExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#nullCoalesceExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullCoalesceExpr(TShellParser.NullCoalesceExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#orExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrExpr(TShellParser.OrExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#andExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndExpr(TShellParser.AndExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#equalityExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpr(TShellParser.EqualityExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#comparisonExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonExpr(TShellParser.ComparisonExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#pipeExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPipeExpr(TShellParser.PipeExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#additiveExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpr(TShellParser.AdditiveExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#multiplicativeExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpr(TShellParser.MultiplicativeExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#unaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpr(TShellParser.UnaryExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#postfixExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixExpr(TShellParser.PostfixExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#postfixOp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixOp(TShellParser.PostfixOpContext ctx);
	/**
	 * Visit a parse tree produced by the {@code numberLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberLiteral(TShellParser.NumberLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(TShellParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code templateLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplateLiteral(TShellParser.TemplateLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code trueLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTrueLiteral(TShellParser.TrueLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code falseLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFalseLiteral(TShellParser.FalseLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullLiteral(TShellParser.NullLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code identifierExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifierExpr(TShellParser.IdentifierExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arrayExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayExpr(TShellParser.ArrayExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code objectExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectExpr(TShellParser.ObjectExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arrowExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrowExpr(TShellParser.ArrowExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenExpr(TShellParser.ParenExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code chainExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitChainExpr(TShellParser.ChainExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code allExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAllExpr(TShellParser.AllExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code raceExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRaceExpr(TShellParser.RaceExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code singleParamArrow}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleParamArrow(TShellParser.SingleParamArrowContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiParamArrow}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiParamArrow(TShellParser.MultiParamArrowContext ctx);
	/**
	 * Visit a parse tree produced by the {@code singleParamArrowBlock}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleParamArrowBlock(TShellParser.SingleParamArrowBlockContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multiParamArrowBlock}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiParamArrowBlock(TShellParser.MultiParamArrowBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#arrayLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayLiteral(TShellParser.ArrayLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#objectLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitObjectLiteral(TShellParser.ObjectLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code namedField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedField(TShellParser.NamedFieldContext ctx);
	/**
	 * Visit a parse tree produced by the {@code shorthandField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShorthandField(TShellParser.ShorthandFieldContext ctx);
	/**
	 * Visit a parse tree produced by the {@code spreadField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpreadField(TShellParser.SpreadFieldContext ctx);
	/**
	 * Visit a parse tree produced by the {@code computedField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComputedField(TShellParser.ComputedFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#fieldName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldName(TShellParser.FieldNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#spreadOrExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpreadOrExpr(TShellParser.SpreadOrExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#templateString}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplateString(TShellParser.TemplateStringContext ctx);
	/**
	 * Visit a parse tree produced by the {@code templateText}
	 * labeled alternative in {@link TShellParser#templatePart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplateText(TShellParser.TemplateTextContext ctx);
	/**
	 * Visit a parse tree produced by the {@code templateInterp}
	 * labeled alternative in {@link TShellParser#templatePart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTemplateInterp(TShellParser.TemplateInterpContext ctx);
	/**
	 * Visit a parse tree produced by {@link TShellParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(TShellParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code namedCallArg}
	 * labeled alternative in {@link TShellParser#callArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNamedCallArg(TShellParser.NamedCallArgContext ctx);
	/**
	 * Visit a parse tree produced by the {@code positionalCallArg}
	 * labeled alternative in {@link TShellParser#callArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPositionalCallArg(TShellParser.PositionalCallArgContext ctx);
}