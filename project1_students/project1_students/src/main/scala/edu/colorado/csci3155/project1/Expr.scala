package edu.colorado.csci3155.project1
import scala.language.implicitConversions // required to define an implicit conversion

sealed trait Expr {
    /* -- this is done for easy writing of test cases through operator overloading -- */
    /* you can ignore this */
    /* e1 + e2 will be converted to Plus(e1,e2) */
    /* e1 - e2 will be converted to Minus(e1,e2) */
    /* e1 * e2 will be converted to Mult(e1,e2) */
    /* e1 / e2 will be converted to Div(e1,e2) */
    /* e1 >= e2 will be converted to Geq(e1,e2) */
    /* overload other operators as needed */
    def + (e2: Expr) = Add(this, e2)
    def - (e2: Expr) = Sub(this, e2)
    def * (e2: Expr) = Mul(this, e2)
    def / (e2: Expr) = Div(this, e2)
    def >= (e2: Expr) = Geq(this, e2)
    def > (e2: Expr) = Gt(this, e2)
    def == (e2: Expr) = Eq(this, e2)
    def && (e2: Expr) = And(this, e2)
    def || (e2: Expr) = Or(this, e2)
    def unary_! = Not(this)
}
/* 

$$\begin{array}{rcll}
\textbf{Expr} & \rightarrow & Const(\textbf{Double}) \\
& | & Id(\textbf{Identifier})\\ 
& | & Add( \textbf{Expr}, \textbf{Expr})  \\
& | & Sub( \textbf{Expr}, \textbf{Expr}) \\
& | & Mul(\textbf{Expr}, \textbf{Expr}) \\
& | & Div(\textbf{Expr}, \textbf{Expr}) \\
& | & Geq(\textbf{Expr}, \textbf{Expr}) \\
& | & Gt(\textbf{Expr}, \textbf{Expr}) \\ 
& | & Eq (\textbf{Expr}, \textbf{Expr}) \\ 
& | & And(\textbf{Expr}, \textbf{Expr}) \\
& | & Or(\textbf{Expr}, \textbf{Expr}) \\
& | & Not(\textbf{Expr}) \\ 
& | & IfThenElse(\textbf{Expr}, \textbf{Expr}, \textbf{Expr} ) \\ 
& | & Let(\textbf{Identifier}, \textbf{Expr}, \textbf{Expr})\\\\
\textbf{Double} & \rightarrow & \text{all double precision numbers in Scala}\\
\textbf{Identifier} & \rightarrow & \textbf{String} & \text{all scala strings}\\\\
\end{array}$$

 */
case class Const(f: Double) extends  Expr
case class Id(id: String) extends Expr
case class Add(e1: Expr, e2: Expr) extends  Expr
case class Sub(e1: Expr, e2: Expr) extends  Expr
case class Mul(e1: Expr, e2: Expr) extends  Expr
case class Div(e1: Expr, e2: Expr) extends  Expr
case class Geq(e1: Expr, e2: Expr) extends Expr 
case class Gt(e1: Expr, e2: Expr) extends Expr 
case class Eq(e1: Expr, e2: Expr) extends Expr 
case class And(e1:Expr, e2: Expr) extends Expr 
case class Or (e1: Expr, e2: Expr) extends Expr 
case class Not(e: Expr) extends Expr 
case class IfThenElse(cond: Expr, tExpr: Expr, elseExpr: Expr) extends Expr 
case class Let(ident: String, e1: Expr, e2: Expr) extends Expr

/*-- Ignore everything below this for now -- this is just to make writing test cases easier --*/
object Conversions { 
    implicit def to_expr(s: String): Expr = Id(s) // implicit conversion from String to Expr
    implicit def to_const (f: Double): Expr = Const(f) // implicit conversion from Double to Expr
}

case class If (cond: Expr) {
    def Then (e: Expr) = IfThen(cond, e)
}

case class IfThen(cond: Expr, e: Expr) {
    def Else (e2: Expr) = IfThenElse(cond, e, e2)
}

case class Llet (x: String) {
    def ~(e: Expr) = { LetBindPart(x, e)}
}

case class LetBindPart(x: String, e: Expr) {
    def in (e2: Expr): Expr = Let(x, e, e2)
}