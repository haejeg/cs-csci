package edu.colorado.csci3155.project1

import scala.annotation.tailrec



sealed trait StackMachineInstruction
/*-- TODO: Complete the inductive definition of the remaining 
          byte code instructions as specified 
          in the documentation --*/


case class ICondSkip(n: Int) extends StackMachineInstruction
case class ISkip(n: Int) extends StackMachineInstruction

case class IPushBool(b: Boolean) extends StackMachineInstruction
case class IPush(n: Double) extends StackMachineInstruction
case object IPlus extends StackMachineInstruction
case object ISub extends StackMachineInstruction
case object IMul extends StackMachineInstruction
case object IDiv extends StackMachineInstruction
case object IGeq extends StackMachineInstruction
case object IGt extends StackMachineInstruction
case object IEq extends StackMachineInstruction
case object INot extends StackMachineInstruction

case class IStore(x: String) extends StackMachineInstruction
case class ILoad(x: String) extends StackMachineInstruction
case object IPop extends StackMachineInstruction

object StackMachineEmulator {

    /*-- An environment stack is a list of tuples containing strings and values --*/
    type EnvStack = List[(String, Value)]
    /*-- An operand stack is a list of values --*/
    type OpStack = List[Value]

    

    /* Function emulateSingleInstruction
        Given a list of values to represent a operand stack
              a list of tuples (string, value) to represent runtime stack
        and   a single instruction of type StackMachineInstruction
        Return a tuple that contains the
              modified stack that results when the instruction is executed.
              modified runtime that results when the instruction is executed.

        Make sure you handle the error cases: eg., stack size must be appropriate for the instruction
        being executed. Division by zero, log of a non negative number
        Throw an exception or assertion violation when error happens.
        TODO: Implement this function.
     */
    def emulateSingleInstruction(stack: OpStack,
                                 env: EnvStack,
                                 ins: StackMachineInstruction): (OpStack, EnvStack) = {
        ins match {
            case IPush(d) => {
                (Num(d) :: stack, env)
            }
            case IPushBool(b) => {
                (Bool(b) :: stack, env)
            }
            case IPlus => {
                val v1 = stack.head
                val v2 = stack.tail.head
                (v1, v2) match {
                    case (Num(v1), Num(v2)) => (Num(v1 + v2) :: stack.tail.tail, env)
                    case _ => throw new IllegalStateException("The stack needs at least two numerical values")
                }
            }
            case ISub => {
                val v1 = stack.head
                val v2 = stack.tail.head
                (v1, v2) match {
                    case (Num(v1), Num(v2)) => (Num(v2 - v1) :: stack.tail.tail, env)
                    case _ => throw new IllegalStateException("The stack needs at least two numerical values")
                }
            }
            case IMul => {
                val v1 = stack.head
                val v2 = stack.tail.head
                (v1, v2) match {
                    case (Num(v1), Num(v2)) => (Num(v1 * v2) :: stack.tail.tail, env)
                    case _ => throw new IllegalStateException("The stack needs at least two numerical values")
                }
            }
            case IDiv => {
                val v1 = stack.head
                val v2 = stack.tail.head
                (v1, v2) match {
                    case (Num(v1), Num(v2)) => {
                        if (v1 == 0) {
                            throw new IllegalStateException("Division by zero not allowed")
                        } else {
                            (Num(v2 / v1) :: stack.tail.tail, env)
                        } 
                    }
                    case _ => throw new IllegalStateException("The stack needs at least two numerical values")
                }
            }
            case IGeq => {
                val v1 = stack.head
                val v2 = stack.tail.head
                (v1, v2) match {
                    case (Num(v1), Num(v2)) => (Bool(v2 >= v1) :: stack.tail.tail, env)
                    case _ => throw new IllegalStateException("The stack needs at least two numerical values")
                }
            }
            case IGt => {
                val v1 = stack.head
                val v2 = stack.tail.head
                (v1, v2) match {
                    case(Num(v1), Num(v2)) => (Bool(v2 > v1) :: stack.tail.tail, env)
                    case _ => throw new IllegalStateException("The stack needs at least two numerical values")
                }
            }
            case IEq => {
                val v1 = stack.head
                val v2 = stack.tail.head
                (v1, v2) match {
                    case(Num(v1), Num(v2)) => (Bool(v2 == v1) :: stack.tail.tail, env)
                    case(Bool(v1), Bool(v2)) => (Bool(v2 == v1) :: stack.tail.tail, env)
                    case _ => throw new IllegalStateException("The stack needs at least two values of the same type")
                }
            }
            case INot => {
                val v1 = stack.head
                v1 match {
                    case Bool(v1) => (Bool(!v1) :: stack.tail, env)
                    case _ => throw new IllegalStateException("The stack needs at least one boolean value")
                }
            }
            case IStore(x) => {
                val v1 = stack.head
                (stack.tail, (x, v1) :: env)
            }
            case ILoad(x) => {
                val v1 = env.find(_._1 == x)
                v1 match {
                    case Some((_, v1)) => (v1 :: stack, env)
                    case None => throw new IllegalStateException("Variable " + x + " not found in environment")
                }
            }
            case IPop => {
                if (env.isEmpty) {
                    throw new IllegalStateException("The environment stack is empty, cannot pop")
                } else {
                    (stack, env.tail)
                }
            }
            case _ => throw new IllegalStateException("Unknown instruction")
        }
    }

    /* Function emulateStackMachine))
       Execute the list of instructions provided as inputs using the
       emulateSingleInstruction function.
       Return the final runtimeStack and the top element of the opStack
     */
    @tailrec
    def emulateStackMachine(instructionList: List[StackMachineInstruction], 
                            opStack: OpStack=Nil, 
                            runtimeStack: EnvStack=Nil): (Value, EnvStack) =
        {
            /*-- Are we out of instructions to execute --*/
            if (instructionList.isEmpty){
                /*-- output top elt. of operand stack and the runtime stack --*/
                (opStack.head, runtimeStack)
            } else {
                /*- What is the instruction on top -*/
                val ins = instructionList.head
                ins match {
                    /*-- Conditional skip instruction --*/
                    case ICondSkip(n) => {
                        /* get the top element in operand stack */
                        val topElt = opStack.head 
                        val restOpStack = opStack.tail 
                        val b = topElt.getBooleanValue /* the top element better be a boolean */
                        if (!b) {
                            /*-- drop the next n instructions --*/
                            val restOfInstructions = instructionList.drop(n+1)
                            emulateStackMachine(restOfInstructions, restOpStack, runtimeStack)
                        } else {
                            /*-- else just drop this instruction --*/
                            emulateStackMachine(instructionList.tail, restOpStack, runtimeStack)
                        }
                    }
                    case ISkip(n) => {
                        /* -- drop this instruction and next n -- continue --*/
                        emulateStackMachine(instructionList.drop(n+1), opStack, runtimeStack)
                    }
                    case ins => {
                        val (newOpStack: OpStack, newRuntime:EnvStack) = emulateSingleInstruction(opStack, runtimeStack, ins)
                        emulateStackMachine(instructionList.tail, newOpStack, newRuntime)
                    }
                    case null => {
                        /*- Otherwise, just call emulateSingleInstruction -*/
                        val (newOpStack: OpStack, newRuntime:EnvStack) = emulateSingleInstruction(opStack, runtimeStack, ins)
                        emulateStackMachine(instructionList.tail, newOpStack, newRuntime)
                    }
                }
            }
        }
}