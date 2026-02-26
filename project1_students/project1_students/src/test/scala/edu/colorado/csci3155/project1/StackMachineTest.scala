package edu.colorado.csci3155.project1

import org.scalatest.funsuite.{AnyFunSuite}
import org.scalatest.BeforeAndAfterAll
import Conversions._


class StackMachineTest extends AnyFunSuite with BeforeAndAfterAll  {

    def testValueOfIdent(env: StackMachineEmulator.EnvStack, x: String, f: Double) = {
        val v: Value  = env.find{ 
            case (s, v) => s == x
        } match {
            case Some((_, v)) => v
            case _ => { 
                assert(false, s"Failed to find identifier $x in environment");
                Num(0.0)
            }
        }
        assert( math.abs(v.getDoubleValue -f) <= 1E-06, s"Expected: f, obtained $v")
    }

    test("stack machine test 1") {
        val lst1 = List(IPush(2.5), IPush(3.5), IPlus)
        val (res, _) = StackMachineEmulator.emulateStackMachine(lst1)
        info(s"Result is $res")
        assert(res.getDoubleValue == 6.0)
    }

    test("stack machine test 2") {
        val lst1 = List(IPush(2.5), IPush(3.5), IPlus, IPush(1.5), IGt)
        val (res, f) = StackMachineEmulator.emulateStackMachine(lst1)
        info(s"Result is $res")
        assert(res.getBooleanValue == true)
    }

    test("stack machine test 3") {
        val lst1 = List(IPush(3.5), IPush(2.5), IPush(4.5), IPush(5.2), IPlus, IStore("x"), IStore("y"), IStore("z"), ILoad("y"))
        val (res, fenv) = StackMachineEmulator.emulateStackMachine(lst1)
        testValueOfIdent(fenv, "x", 9.7)
        testValueOfIdent(fenv, "y", 2.5)
        testValueOfIdent(fenv, "z", 3.5)
    }
    

    test("stack machine test 4") {
        val lst4 = List(IPush(3.5), IPush(2.5), IPush(4.5), IPush(5.2),  IStore("x"), IStore("y"), IStore("z"), IStore("w"),
                         ILoad("y"), ILoad("w"), IPlus, IStore("res1"), ILoad("x"), ILoad("z"), IMul)
        val (res2, fenv) = StackMachineEmulator.emulateStackMachine(lst4)
        testValueOfIdent(fenv, "x", 5.2)
        testValueOfIdent(fenv, "y", 4.5)
        testValueOfIdent(fenv, "z", 2.5)
        testValueOfIdent(fenv, "w", 3.5)
        testValueOfIdent(fenv, "res1", 8.0)
        assert( math.abs(res2.getDoubleValue - 5.2 * 2.5) <= 1E-06)
    }

    test("stack machine test 5") {
        val lst1 = List(IPush(1.5), IPush(2.4), IPlus, IPush(2.5), IPush(2.5), IMul, ISub)
        val lst2 = List(IPush(1.0), IPush(2.5), IPlus)
        val lst3 = lst1 ++ List(IPush(0.0), IPlus) ++ lst2 ++ List(IPush(0.0), ISub) ++ List(IPlus) ++ List(IPush(2.0), IDiv) 
        val (res, f) = StackMachineEmulator.emulateStackMachine(lst3)
        info(s"Result is $res")
        println(s"Result is $res")
        assert(math.abs(res.getDoubleValue -0.575 ) <= 1E-04)
    }


}

