package edu.colorado.csci3155.project1
sealed trait Value  {
    def getDoubleValue: Double = throw new RuntimeException("Type mismatch in conversion to double")
    def getBooleanValue: Boolean = throw new RuntimeException("Type mismatch in conversion to boolean")
}
case class Num(f: Double) extends Value {
    override def getDoubleValue: Double  = f
}
case class Bool(b: Boolean) extends Value  { 
       override def getBooleanValue: Boolean = b
}
case object Error extends Value 

