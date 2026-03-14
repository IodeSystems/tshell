// Generated from /home/nthalk/local/src/iodesystems/tshell/tshell/src/main/antlr4/TShellParser.g4 by ANTLR 4.13.2
package com.iodesystems.tshell.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TShellParser}.
 */
public interface TShellParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TShellParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(TShellParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(TShellParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(TShellParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(TShellParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#exportStatement}.
	 * @param ctx the parse tree
	 */
	void enterExportStatement(TShellParser.ExportStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#exportStatement}.
	 * @param ctx the parse tree
	 */
	void exitExportStatement(TShellParser.ExportStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#letDecl}.
	 * @param ctx the parse tree
	 */
	void enterLetDecl(TShellParser.LetDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#letDecl}.
	 * @param ctx the parse tree
	 */
	void exitLetDecl(TShellParser.LetDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#letBinding}.
	 * @param ctx the parse tree
	 */
	void enterLetBinding(TShellParser.LetBindingContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#letBinding}.
	 * @param ctx the parse tree
	 */
	void exitLetBinding(TShellParser.LetBindingContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#fnDecl}.
	 * @param ctx the parse tree
	 */
	void enterFnDecl(TShellParser.FnDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#fnDecl}.
	 * @param ctx the parse tree
	 */
	void exitFnDecl(TShellParser.FnDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 */
	void enterTryCatchStatement(TShellParser.TryCatchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 */
	void exitTryCatchStatement(TShellParser.TryCatchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#throwStatement}.
	 * @param ctx the parse tree
	 */
	void enterThrowStatement(TShellParser.ThrowStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#throwStatement}.
	 * @param ctx the parse tree
	 */
	void exitThrowStatement(TShellParser.ThrowStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(TShellParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(TShellParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#breakStatement}.
	 * @param ctx the parse tree
	 */
	void enterBreakStatement(TShellParser.BreakStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#breakStatement}.
	 * @param ctx the parse tree
	 */
	void exitBreakStatement(TShellParser.BreakStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#continueStatement}.
	 * @param ctx the parse tree
	 */
	void enterContinueStatement(TShellParser.ContinueStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#continueStatement}.
	 * @param ctx the parse tree
	 */
	void exitContinueStatement(TShellParser.ContinueStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#assignStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssignStatement(TShellParser.AssignStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#assignStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssignStatement(TShellParser.AssignStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#incrDecrStatement}.
	 * @param ctx the parse tree
	 */
	void enterIncrDecrStatement(TShellParser.IncrDecrStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#incrDecrStatement}.
	 * @param ctx the parse tree
	 */
	void exitIncrDecrStatement(TShellParser.IncrDecrStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void enterExpressionStatement(TShellParser.ExpressionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#expressionStatement}.
	 * @param ctx the parse tree
	 */
	void exitExpressionStatement(TShellParser.ExpressionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#assignTarget}.
	 * @param ctx the parse tree
	 */
	void enterAssignTarget(TShellParser.AssignTargetContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#assignTarget}.
	 * @param ctx the parse tree
	 */
	void exitAssignTarget(TShellParser.AssignTargetContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#assignOp}.
	 * @param ctx the parse tree
	 */
	void enterAssignOp(TShellParser.AssignOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#assignOp}.
	 * @param ctx the parse tree
	 */
	void exitAssignOp(TShellParser.AssignOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(TShellParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(TShellParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void enterSwitchStatement(TShellParser.SwitchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void exitSwitchStatement(TShellParser.SwitchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#switchCase}.
	 * @param ctx the parse tree
	 */
	void enterSwitchCase(TShellParser.SwitchCaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#switchCase}.
	 * @param ctx the parse tree
	 */
	void exitSwitchCase(TShellParser.SwitchCaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#switchDefault}.
	 * @param ctx the parse tree
	 */
	void enterSwitchDefault(TShellParser.SwitchDefaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#switchDefault}.
	 * @param ctx the parse tree
	 */
	void exitSwitchDefault(TShellParser.SwitchDefaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(TShellParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(TShellParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#doWhileStatement}.
	 * @param ctx the parse tree
	 */
	void enterDoWhileStatement(TShellParser.DoWhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#doWhileStatement}.
	 * @param ctx the parse tree
	 */
	void exitDoWhileStatement(TShellParser.DoWhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#forOfStatement}.
	 * @param ctx the parse tree
	 */
	void enterForOfStatement(TShellParser.ForOfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#forOfStatement}.
	 * @param ctx the parse tree
	 */
	void exitForOfStatement(TShellParser.ForOfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#forInStatement}.
	 * @param ctx the parse tree
	 */
	void enterForInStatement(TShellParser.ForInStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#forInStatement}.
	 * @param ctx the parse tree
	 */
	void exitForInStatement(TShellParser.ForInStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void enterForStatement(TShellParser.ForStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#forStatement}.
	 * @param ctx the parse tree
	 */
	void exitForStatement(TShellParser.ForStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#forInitLet}.
	 * @param ctx the parse tree
	 */
	void enterForInitLet(TShellParser.ForInitLetContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#forInitLet}.
	 * @param ctx the parse tree
	 */
	void exitForInitLet(TShellParser.ForInitLetContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#forInitAssign}.
	 * @param ctx the parse tree
	 */
	void enterForInitAssign(TShellParser.ForInitAssignContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#forInitAssign}.
	 * @param ctx the parse tree
	 */
	void exitForInitAssign(TShellParser.ForInitAssignContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#forUpdateAssign}.
	 * @param ctx the parse tree
	 */
	void enterForUpdateAssign(TShellParser.ForUpdateAssignContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#forUpdateAssign}.
	 * @param ctx the parse tree
	 */
	void exitForUpdateAssign(TShellParser.ForUpdateAssignContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#forUpdateIncrDecr}.
	 * @param ctx the parse tree
	 */
	void enterForUpdateIncrDecr(TShellParser.ForUpdateIncrDecrContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#forUpdateIncrDecr}.
	 * @param ctx the parse tree
	 */
	void exitForUpdateIncrDecr(TShellParser.ForUpdateIncrDecrContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(TShellParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(TShellParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#blockOrStatement}.
	 * @param ctx the parse tree
	 */
	void enterBlockOrStatement(TShellParser.BlockOrStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#blockOrStatement}.
	 * @param ctx the parse tree
	 */
	void exitBlockOrStatement(TShellParser.BlockOrStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#destructure}.
	 * @param ctx the parse tree
	 */
	void enterDestructure(TShellParser.DestructureContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#destructure}.
	 * @param ctx the parse tree
	 */
	void exitDestructure(TShellParser.DestructureContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#objectDestructure}.
	 * @param ctx the parse tree
	 */
	void enterObjectDestructure(TShellParser.ObjectDestructureContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#objectDestructure}.
	 * @param ctx the parse tree
	 */
	void exitObjectDestructure(TShellParser.ObjectDestructureContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#destructureField}.
	 * @param ctx the parse tree
	 */
	void enterDestructureField(TShellParser.DestructureFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#destructureField}.
	 * @param ctx the parse tree
	 */
	void exitDestructureField(TShellParser.DestructureFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#arrayDestructure}.
	 * @param ctx the parse tree
	 */
	void enterArrayDestructure(TShellParser.ArrayDestructureContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#arrayDestructure}.
	 * @param ctx the parse tree
	 */
	void exitArrayDestructure(TShellParser.ArrayDestructureContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(TShellParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(TShellParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#param}.
	 * @param ctx the parse tree
	 */
	void enterParam(TShellParser.ParamContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#param}.
	 * @param ctx the parse tree
	 */
	void exitParam(TShellParser.ParamContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#typeAnnotation}.
	 * @param ctx the parse tree
	 */
	void enterTypeAnnotation(TShellParser.TypeAnnotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#typeAnnotation}.
	 * @param ctx the parse tree
	 */
	void exitTypeAnnotation(TShellParser.TypeAnnotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(TShellParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(TShellParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#ternaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterTernaryExpr(TShellParser.TernaryExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#ternaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitTernaryExpr(TShellParser.TernaryExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#nullCoalesceExpr}.
	 * @param ctx the parse tree
	 */
	void enterNullCoalesceExpr(TShellParser.NullCoalesceExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#nullCoalesceExpr}.
	 * @param ctx the parse tree
	 */
	void exitNullCoalesceExpr(TShellParser.NullCoalesceExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#orExpr}.
	 * @param ctx the parse tree
	 */
	void enterOrExpr(TShellParser.OrExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#orExpr}.
	 * @param ctx the parse tree
	 */
	void exitOrExpr(TShellParser.OrExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#andExpr}.
	 * @param ctx the parse tree
	 */
	void enterAndExpr(TShellParser.AndExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#andExpr}.
	 * @param ctx the parse tree
	 */
	void exitAndExpr(TShellParser.AndExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#bitwiseOrExpr}.
	 * @param ctx the parse tree
	 */
	void enterBitwiseOrExpr(TShellParser.BitwiseOrExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#bitwiseOrExpr}.
	 * @param ctx the parse tree
	 */
	void exitBitwiseOrExpr(TShellParser.BitwiseOrExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#bitwiseXorExpr}.
	 * @param ctx the parse tree
	 */
	void enterBitwiseXorExpr(TShellParser.BitwiseXorExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#bitwiseXorExpr}.
	 * @param ctx the parse tree
	 */
	void exitBitwiseXorExpr(TShellParser.BitwiseXorExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#bitwiseAndExpr}.
	 * @param ctx the parse tree
	 */
	void enterBitwiseAndExpr(TShellParser.BitwiseAndExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#bitwiseAndExpr}.
	 * @param ctx the parse tree
	 */
	void exitBitwiseAndExpr(TShellParser.BitwiseAndExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#equalityExpr}.
	 * @param ctx the parse tree
	 */
	void enterEqualityExpr(TShellParser.EqualityExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#equalityExpr}.
	 * @param ctx the parse tree
	 */
	void exitEqualityExpr(TShellParser.EqualityExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#comparisonExpr}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpr(TShellParser.ComparisonExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#comparisonExpr}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpr(TShellParser.ComparisonExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#shiftExpr}.
	 * @param ctx the parse tree
	 */
	void enterShiftExpr(TShellParser.ShiftExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#shiftExpr}.
	 * @param ctx the parse tree
	 */
	void exitShiftExpr(TShellParser.ShiftExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#pipeExpr}.
	 * @param ctx the parse tree
	 */
	void enterPipeExpr(TShellParser.PipeExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#pipeExpr}.
	 * @param ctx the parse tree
	 */
	void exitPipeExpr(TShellParser.PipeExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#additiveExpr}.
	 * @param ctx the parse tree
	 */
	void enterAdditiveExpr(TShellParser.AdditiveExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#additiveExpr}.
	 * @param ctx the parse tree
	 */
	void exitAdditiveExpr(TShellParser.AdditiveExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#multiplicativeExpr}.
	 * @param ctx the parse tree
	 */
	void enterMultiplicativeExpr(TShellParser.MultiplicativeExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#multiplicativeExpr}.
	 * @param ctx the parse tree
	 */
	void exitMultiplicativeExpr(TShellParser.MultiplicativeExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#unaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(TShellParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#unaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(TShellParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#postfixExpr}.
	 * @param ctx the parse tree
	 */
	void enterPostfixExpr(TShellParser.PostfixExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#postfixExpr}.
	 * @param ctx the parse tree
	 */
	void exitPostfixExpr(TShellParser.PostfixExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#postfixOp}.
	 * @param ctx the parse tree
	 */
	void enterPostfixOp(TShellParser.PostfixOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#postfixOp}.
	 * @param ctx the parse tree
	 */
	void exitPostfixOp(TShellParser.PostfixOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numberLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterNumberLiteral(TShellParser.NumberLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numberLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitNumberLiteral(TShellParser.NumberLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(TShellParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(TShellParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code templateLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterTemplateLiteral(TShellParser.TemplateLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code templateLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitTemplateLiteral(TShellParser.TemplateLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code trueLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterTrueLiteral(TShellParser.TrueLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code trueLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitTrueLiteral(TShellParser.TrueLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code falseLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterFalseLiteral(TShellParser.FalseLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code falseLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitFalseLiteral(TShellParser.FalseLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterNullLiteral(TShellParser.NullLiteralContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullLiteral}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitNullLiteral(TShellParser.NullLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code identifierExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterIdentifierExpr(TShellParser.IdentifierExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code identifierExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitIdentifierExpr(TShellParser.IdentifierExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrayExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterArrayExpr(TShellParser.ArrayExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrayExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitArrayExpr(TShellParser.ArrayExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code objectExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterObjectExpr(TShellParser.ObjectExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code objectExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitObjectExpr(TShellParser.ObjectExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrowExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterArrowExpr(TShellParser.ArrowExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrowExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitArrowExpr(TShellParser.ArrowExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code funcExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterFuncExpr(TShellParser.FuncExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code funcExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitFuncExpr(TShellParser.FuncExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regexExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterRegexExpr(TShellParser.RegexExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regexExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitRegexExpr(TShellParser.RegexExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterParenExpr(TShellParser.ParenExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenExpr}
	 * labeled alternative in {@link TShellParser#primaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitParenExpr(TShellParser.ParenExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#functionExpr}.
	 * @param ctx the parse tree
	 */
	void enterFunctionExpr(TShellParser.FunctionExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#functionExpr}.
	 * @param ctx the parse tree
	 */
	void exitFunctionExpr(TShellParser.FunctionExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleParamArrow}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 */
	void enterSingleParamArrow(TShellParser.SingleParamArrowContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleParamArrow}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 */
	void exitSingleParamArrow(TShellParser.SingleParamArrowContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiParamArrow}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 */
	void enterMultiParamArrow(TShellParser.MultiParamArrowContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiParamArrow}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 */
	void exitMultiParamArrow(TShellParser.MultiParamArrowContext ctx);
	/**
	 * Enter a parse tree produced by the {@code singleParamArrowBlock}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 */
	void enterSingleParamArrowBlock(TShellParser.SingleParamArrowBlockContext ctx);
	/**
	 * Exit a parse tree produced by the {@code singleParamArrowBlock}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 */
	void exitSingleParamArrowBlock(TShellParser.SingleParamArrowBlockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiParamArrowBlock}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 */
	void enterMultiParamArrowBlock(TShellParser.MultiParamArrowBlockContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiParamArrowBlock}
	 * labeled alternative in {@link TShellParser#arrowFunction}.
	 * @param ctx the parse tree
	 */
	void exitMultiParamArrowBlock(TShellParser.MultiParamArrowBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#arrayLiteral}.
	 * @param ctx the parse tree
	 */
	void enterArrayLiteral(TShellParser.ArrayLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#arrayLiteral}.
	 * @param ctx the parse tree
	 */
	void exitArrayLiteral(TShellParser.ArrayLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#objectLiteral}.
	 * @param ctx the parse tree
	 */
	void enterObjectLiteral(TShellParser.ObjectLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#objectLiteral}.
	 * @param ctx the parse tree
	 */
	void exitObjectLiteral(TShellParser.ObjectLiteralContext ctx);
	/**
	 * Enter a parse tree produced by the {@code namedField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 */
	void enterNamedField(TShellParser.NamedFieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code namedField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 */
	void exitNamedField(TShellParser.NamedFieldContext ctx);
	/**
	 * Enter a parse tree produced by the {@code shorthandField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 */
	void enterShorthandField(TShellParser.ShorthandFieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code shorthandField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 */
	void exitShorthandField(TShellParser.ShorthandFieldContext ctx);
	/**
	 * Enter a parse tree produced by the {@code spreadField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 */
	void enterSpreadField(TShellParser.SpreadFieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code spreadField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 */
	void exitSpreadField(TShellParser.SpreadFieldContext ctx);
	/**
	 * Enter a parse tree produced by the {@code computedField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 */
	void enterComputedField(TShellParser.ComputedFieldContext ctx);
	/**
	 * Exit a parse tree produced by the {@code computedField}
	 * labeled alternative in {@link TShellParser#objectField}.
	 * @param ctx the parse tree
	 */
	void exitComputedField(TShellParser.ComputedFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#fieldName}.
	 * @param ctx the parse tree
	 */
	void enterFieldName(TShellParser.FieldNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#fieldName}.
	 * @param ctx the parse tree
	 */
	void exitFieldName(TShellParser.FieldNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#spreadOrExpr}.
	 * @param ctx the parse tree
	 */
	void enterSpreadOrExpr(TShellParser.SpreadOrExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#spreadOrExpr}.
	 * @param ctx the parse tree
	 */
	void exitSpreadOrExpr(TShellParser.SpreadOrExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#templateString}.
	 * @param ctx the parse tree
	 */
	void enterTemplateString(TShellParser.TemplateStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#templateString}.
	 * @param ctx the parse tree
	 */
	void exitTemplateString(TShellParser.TemplateStringContext ctx);
	/**
	 * Enter a parse tree produced by the {@code templateText}
	 * labeled alternative in {@link TShellParser#templatePart}.
	 * @param ctx the parse tree
	 */
	void enterTemplateText(TShellParser.TemplateTextContext ctx);
	/**
	 * Exit a parse tree produced by the {@code templateText}
	 * labeled alternative in {@link TShellParser#templatePart}.
	 * @param ctx the parse tree
	 */
	void exitTemplateText(TShellParser.TemplateTextContext ctx);
	/**
	 * Enter a parse tree produced by the {@code templateInterp}
	 * labeled alternative in {@link TShellParser#templatePart}.
	 * @param ctx the parse tree
	 */
	void enterTemplateInterp(TShellParser.TemplateInterpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code templateInterp}
	 * labeled alternative in {@link TShellParser#templatePart}.
	 * @param ctx the parse tree
	 */
	void exitTemplateInterp(TShellParser.TemplateInterpContext ctx);
	/**
	 * Enter a parse tree produced by {@link TShellParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void enterArgumentList(TShellParser.ArgumentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link TShellParser#argumentList}.
	 * @param ctx the parse tree
	 */
	void exitArgumentList(TShellParser.ArgumentListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code namedCallArg}
	 * labeled alternative in {@link TShellParser#callArg}.
	 * @param ctx the parse tree
	 */
	void enterNamedCallArg(TShellParser.NamedCallArgContext ctx);
	/**
	 * Exit a parse tree produced by the {@code namedCallArg}
	 * labeled alternative in {@link TShellParser#callArg}.
	 * @param ctx the parse tree
	 */
	void exitNamedCallArg(TShellParser.NamedCallArgContext ctx);
	/**
	 * Enter a parse tree produced by the {@code positionalCallArg}
	 * labeled alternative in {@link TShellParser#callArg}.
	 * @param ctx the parse tree
	 */
	void enterPositionalCallArg(TShellParser.PositionalCallArgContext ctx);
	/**
	 * Exit a parse tree produced by the {@code positionalCallArg}
	 * labeled alternative in {@link TShellParser#callArg}.
	 * @param ctx the parse tree
	 */
	void exitPositionalCallArg(TShellParser.PositionalCallArgContext ctx);
}