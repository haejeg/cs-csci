package edu.colorado.csci3155.project1
import org.scalatest.funsuite.{AnyFunSuite}
import org.scalatest.BeforeAndAfterAll
import Conversions._ 

class CompilerTest extends AnyFunSuite  with BeforeAndAfterAll  {
    def printInstructionList(lst: List[StackMachineInstruction], name: String): Unit = {
        info(s"Your result for CompilerTest: $name")
        info("-------------------------------")
        lst.foreach(i => info(i.toString))
        info("-------------------------------")
    }

    test("simple expression 1") {
        /* compile constant expression 1.0 */
        val e = Const(1.0)
        val lst = StackMachineCompiler.compileToStackMachineCode(e)
        printInstructionList(lst, "simple expression 1")
        assert(lst == List(IPush(1.0)))
    }

    test("simple expression 2") {
        /* Compile 1 + 2.5 */
        val e = Add(Const(1.0), Const(2.5))
        val lst = StackMachineCompiler.compileToStackMachineCode(e)
        printInstructionList(lst, "simple expression 2")
        assert(lst == List(IPush(1.0), IPush(2.5), IPlus))
    }

    test("simple expression 3"){
        /* compile (1.5 + 2.4) - (2.5 * 2.5) */
        val e = Sub(Add(Const(1.5), Const(2.4)), Mul(Const(2.5), Const(2.5)))
        val lst = StackMachineCompiler.compileToStackMachineCode(e)
        printInstructionList(lst, "simple expression 3")
        assert(lst == List(IPush(1.5), IPush(2.4), IPlus, IPush(2.5), IPush(2.5), IMul, ISub))
    }

    test("simple expression 4") {
        /* compile log ( exp( (1.5 + 2.4) - (2.5 * 2.5)) + exp (1.0 + 2.5) ) */
        val e1 = Sub(Add(Const(1.5), Const(2.4)), Mul(Const(2.5), Const(2.5)))
        val e2 = Add(Const(1.0), Const(2.5))
        val e3 = Add(Div(e1, 2.0), Add(e2, 0.0))
        val lst = StackMachineCompiler.compileToStackMachineCode(e3)
        printInstructionList(lst, "simple expression 4")
        val lst1 = List(IPush(1.5), IPush(2.4), IPlus, IPush(2.5), IPush(2.5), IMul, ISub)
        val lst2 = List(IPush(1.0), IPush(2.5), IPlus)
        val expected = lst1 ++ List(IPush(2.0), IDiv) ++ lst2 ++ List(IPush(0.0), IPlus) ++ List(IPlus) 
        assertResult(expected){lst}
    }

    test("LetBinding + Id Test 1") {
        /* compile let x = 1 in x */
        val e1 = Let("x", Const(1.0), Id("x")) //let x = 1 in x
        val lst = StackMachineCompiler.compileToStackMachineCode(e1)
        printInstructionList(lst, "LetBinding + Id Test 1")
        val expected = List(IPush(1.0), IStore("x"), ILoad("x"),IPop)
        assertResult(expected){lst}
    }

    test("LetBinding + Id Test 2") {
        /* compile let x = 1.5 in x + x */
        val e1 = Let("x", Const(1.5), Add(Id("x"), Id("x"))) 
        val lst = StackMachineCompiler.compileToStackMachineCode(e1)
        printInstructionList(lst, "LetBinding+ Id Test 2")
        assert(lst == List(IPush(1.5), IStore("x"), ILoad("x"), ILoad("x"), IPlus, IPop))
    }

    test("LetBinding + Id Test 3") {
        /* compile let x = 1.5 in let y = x + 1 in x * y */
        val x = Id("x")
        val y = Id("y")
        val innerLet = Let("y", Add(x, Const(1.0)), Mul(x, y))
        val e1 = Let("x", Const(1.5), innerLet) //let x = 1.5 in let y = 1 + x in x * y
        val lst = StackMachineCompiler.compileToStackMachineCode(e1)
        printInstructionList(lst, "Let Binding + Id Test 3")
        val expected =  List(IPush(1.5),
                             IStore("x"), 
                             ILoad("x"),
                             IPush(1.0),
                             IPlus, 
                             IStore("y"), 
                             ILoad("x"),
                             ILoad("y"),
                             IMul, 
                             IPop, 
                             IPop)
        assertResult(expected){lst}
    }

    test("LetBinding + Id Test 4") {
        /* Compile let x = (let x = 1 in x + 1) in x + 2 */
        val x = Id("x")
        val innerLet = Let("x", Const(1.0), Add(x, Const(1)))
        val e1 = Let("x", innerLet, Add(x, Const(2)))
        val lst = StackMachineCompiler.compileToStackMachineCode(e1)
        printInstructionList(lst, "LetBinding + Id Test 4")
        val expected = List (
            IPush(1.0),
            IStore("x"),
            ILoad("x"),
            IPush(1.0),
            IPlus,
            IPop, 
            IStore("x"),
            ILoad("x"),
            IPush(2.0),
            IPlus,
            IPop,
        )
         assertResult(expected){lst}
        val (res, _) = StackMachineEmulator.emulateStackMachine(lst)
        assert(math.abs(res.getDoubleValue - 4.0) <= 1E-05)
    }

      test("LetBinding + Id Test 5") {
        /* let x = 1 in 
            let y  = (let x = 5.0 in x * 3) in 
              x - y 
        */
        val x = Id("x")
        val y = Id("y")
        val innerLet = Llet ("x") ~ 5.0 in x * 3 
        val e1 = Llet ("x") ~ 1.0 in (Llet("y") ~ innerLet in x - y )
        val lst = StackMachineCompiler.compileToStackMachineCode(e1)

        printInstructionList(lst, "LetBinding + Id Test 5")
        val expected = List(
            IPush(1.0), 
            IStore("x"),
            IPush(5.0), 
            IStore("x"), 
            ILoad("x"), 
            IPush(3.0),
            IMul,
            IPop, 
            IStore("y"),
            ILoad("x"),
            ILoad("y"),
            ISub, 
            IPop, 
            IPop
        )
        assertResult(expected){lst}
        val (res, _) = StackMachineEmulator.emulateStackMachine(lst)
        assert(math.abs(res.getDoubleValue + 14.0) <= 1E-05)
    }

    test("If-then-else-test-1") {
       /* compile let x = 25 in if (x >= 25) then x + 25.5 else 43 */
       val x = Id("x")
       val e = If (x >= 25) Then (x + 25.5) Else (43)
       val e2 = Llet("x")~25 in e 
       info("Expression: " + e2) 
       val lst = StackMachineCompiler.compileToStackMachineCode(e2)
       printInstructionList(lst, "If-then-else-test-1")
       val expected = List (
        IPush(25.0), 
        IStore("x"), 
        ILoad("x"),
        IPush(25.0),
        IGeq, 
        ICondSkip(4), 
        ILoad("x"), 
        IPush(25.5), 
        IPlus, 
        ISkip(1), 
        IPush(43.0), 
        IPop
       )
       assertResult(expected){lst}
       val (res, _) = StackMachineEmulator.emulateStackMachine(lst)
       assert(math.abs(res.getDoubleValue - 50.5) <= 1E-05)

    }
    test("If-then-else-test-2") {
       /* compile let y = 24.5 in let x = y + 45 in if (x >= 25) then x + 25.5 else 43 */
       val x = Id("x")
       val y = Id("y")
       val e = If (x >= 25) Then (x + 25.5) Else (43)
       val e2 = Llet("x")~(y + 45) in e 
       val e3 = Llet("y")~24.5 in e2
       val lst = StackMachineCompiler.compileToStackMachineCode(e3)
       printInstructionList(lst, "if-then-else-test2")
       val expected = List( IPush(24.5), 
         IStore("y"), 
         ILoad("y"), 
         IPush(45.0), 
         IPlus,
         IStore("x"), 
         ILoad("x"),
        IPush(25.0),
        IGeq, 
        ICondSkip(4), 
        ILoad("x"), 
        IPush(25.5), 
        IPlus, 
        ISkip(1), 
        IPush(43.0), 
        IPop,
        IPop
       )
       assertResult(expected){lst}
       val (res, _) = StackMachineEmulator.emulateStackMachine(lst)
       assert(math.abs(res.getDoubleValue - 95.0) <= 1E-05)
    }
    test("test-and-simple-1") {
        /* 1 >= 0 && 5 >= -4 */
        val e = And(Geq(1, 0), Geq(5, -4)) // We have implicit conversion from numbers to constants
        val lst = StackMachineCompiler.compileToStackMachineCode(e)
        printInstructionList(lst, "test-and-simple-1")
        val expected= List( IPush(1.0), 
                IPush(0.0), 
                IGeq, 
                ICondSkip(4), 
                IPush(5.0), 
                IPush(-4.0), 
                IGeq, 
                ISkip(1), 
                IPushBool(false))
        assertResult(expected){lst}
        val (res, _) = StackMachineEmulator.emulateStackMachine(lst)
        assert(res.getBooleanValue == true)
    }
    test("test-and-simple-2") {
        /* 1 >= 5 && 5/0 */
        val e = And(Geq(1, 5), Div(5,0)) // We have implicit conversion from numbers to constants
        val lst = StackMachineCompiler.compileToStackMachineCode(e)
        printInstructionList(lst, "test-and-simple-2")
        val expected= List( IPush(1.0), 
                IPush(5.0), 
                IGeq, 
                ICondSkip(4), 
                IPush(5.0), 
                IPush(0.0), 
                IDiv, 
                ISkip(1), 
                IPushBool(false))
        assertResult(expected){lst}
        val (res, _) = StackMachineEmulator.emulateStackMachine(lst)
        assert(res.getBooleanValue == false)
    }

    test("test-and-simple-3") {
        /* 5 >= 1 && -4 >= 5 */
        val e = And(Geq(5, 1), Geq(-4, 5)) // We have implicit conversion from numbers to constants
        val lst = StackMachineCompiler.compileToStackMachineCode(e)
        printInstructionList(lst, "test-and-simple-3")
        val expected= List( IPush(5.0), 
                IPush(1.0), 
                IGeq, 
                ICondSkip(4), 
                IPush(-4.0), 
                IPush(5.0), 
                IGeq, 
                ISkip(1), 
                IPushBool(false))
        assertResult(expected){lst}
        val (res, _) = StackMachineEmulator.emulateStackMachine(lst)
        assert(res.getBooleanValue == false)
    }
    test("test-or-simple-1") {
        /* 1 >= 5 || 5 >= -4 */
        val e = Or(Geq(1, 5), Geq(5, -4)) // We have implicit conversion from numbers to constants
        val lst = StackMachineCompiler.compileToStackMachineCode(e)
        printInstructionList(lst, "test-or-simple-1")
        val expected= List( IPush(1.0), 
                IPush(5.0), 
                IGeq, 
                ICondSkip(2),
                IPushBool(true), 
                ISkip(3), 
                IPush(5.0), 
                IPush(-4.0), 
                IGeq)
        assertResult(expected){lst}
        val (res, _) = StackMachineEmulator.emulateStackMachine(lst)
        assert(res.getBooleanValue == true)
    }

    test("test-or-not-1") {
        /* 5 >= 1 || 0/0 >= 0 */
        val e = Not(Or(Geq(5, 1), Geq(Div(0, 0), 0))) // We have implicit conversion from numbers to constants
        val lst = StackMachineCompiler.compileToStackMachineCode(e)
        printInstructionList(lst, "test-or-not-1")
        val expected= List( IPush(5.0), 
                IPush(1.0), 
                IGeq, 
                ICondSkip(2),
                IPushBool(true), 
                ISkip(5), 
                IPush(0.0), 
                IPush(0.0),
                IDiv, 
                IPush(0.0), 
                IGeq,
                INot)
        assertResult(expected){lst}
        val (res, _) = StackMachineEmulator.emulateStackMachine(lst)
        assert(res.getBooleanValue == false)
    }

    test("test-let-binding-1") {
        /*  let x = 10 in 
                let y = 20 in 
                    x + y * y */
        val x = Id("x")
        val y = Id("y")
        val e = Llet("y") ~ 20 in (x + y * y)
        val e2 = Llet("x") ~ 10 in e
        val lst = StackMachineCompiler.compileToStackMachineCode(e2)
        printInstructionList(lst, "test-let-binding-1")
    }

    
}
