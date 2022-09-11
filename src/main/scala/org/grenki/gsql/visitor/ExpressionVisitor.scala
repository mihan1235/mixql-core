package org.grenki.gsql.visitor

import org.grenki.gsql.context.gtype.{Type, string, Null}
import org.grenki.gsql.sql

trait ExpressionVisitor extends BaseVisitor {
  override def visitExpr_concat(ctx: sql.Expr_concatContext): Type =
    string(visit(ctx.expr(0)).toString + visit(ctx.expr(1)).toString)

  override def visitExpr_arithmetic_p1(
    ctx: sql.Expr_arithmetic_p1Context
  ): Type = {
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    if (ctx.T_DIV() != null)
      left / right
    else if (ctx.T_MUL() != null)
      left * right
    else
      throw new IllegalArgumentException("unknown operator")
  }

  override def visitExpr_arithmetic_p2(
    ctx: sql.Expr_arithmetic_p2Context
  ): Type = {
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    if (ctx.T_ADD() != null)
      left + right
    else if (ctx.T_SUB() != null)
      left - right
    else
      throw new IllegalArgumentException("unknown operator")
  }

  override def visitExpr_compare(ctx: sql.Expr_compareContext): Type = {
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    if (
      ctx.compare_operator().T_EQUAL() != null || ctx
        .compare_operator()
        .T_EQUAL2() != null
    )
      left == right
    else if (ctx.compare_operator().T_NOTEQUAL() != null)
      left != right
    else if (ctx.compare_operator().T_GREATER() != null)
      left > right
    else if (ctx.compare_operator().T_GREATEREQUAL() != null)
      left >= right
    else if (ctx.compare_operator().T_LESS() != null)
      left < right
    else if (ctx.compare_operator().T_LESSEQUAL() != null)
      left <= right
    else
      throw new IllegalArgumentException("unknown compare operator")
  }

  override def visitExpr_logical(ctx: sql.Expr_logicalContext): Type = {
    val left = visit(ctx.expr(0))
    val right = visit(ctx.expr(1))
    if (ctx.logical_operator().T_OR() != null)
      left || right
    else if (ctx.logical_operator().T_AND() != null)
      left && right
    else
      throw new IllegalArgumentException("unknown operator")
  }

  override def visitExpr_not(ctx: sql.Expr_notContext): Type =
    visit(ctx.expr()).!()

  override def visitExpr_recurse(ctx: sql.Expr_recurseContext): Type =
    if (ctx.expr() != null)
      visit(ctx.expr())
    else if (ctx.other() != null)
      context.currentEngine.execute(visit(ctx.other()).toString())
    else
      throw new IllegalArgumentException("unknown operator")

  override def visitExpr_case(ctx: sql.Expr_caseContext): Type = {
    // TODO ctx.case_r().expr() - what is it
    ctx
      .case_r()
      .case_when_then()
      .forEach(case_r => {
        val condition: Boolean = visit(case_r.condition)
        if (condition) return visit(case_r.ex_do)
      })
    if (ctx.case_r().ex_else != null) return visit(ctx.case_r().ex_else)
    Null // TODO default result if no condition matched (mb exception?)
  }
}
