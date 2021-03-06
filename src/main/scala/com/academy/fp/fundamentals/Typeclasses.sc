import cats.Semigroup
import cats.implicits.catsSyntaxSemigroup

object Typeclasses {

  sealed trait Equal[A] {
    def equals(x: A, y: A): Boolean
  }

  implicit val intInstance: Equal[Int] =
    new Equal[Int] {
      def equals(x: Int, y: Int): Boolean = x == y
    }

  implicit val stringInstance: Equal[String] =
    new Equal[String] {
      override def equals(x: String, y: String): Boolean = x.equalsIgnoreCase(y)
    }

  intInstance.equals(5, 5)
  stringInstance.equals("hello", "hello")

  // alternative (fails :S)
  /*
  object Equal {
    implicit val intEqual: Equal[Int] =
      (x, y) => x == y
    implicit val strEqual: Equal[String] =
      (x, y) => x.equalsIgnoreCase(y)

    def apply[A](implicit E: Equal[A]): Equal[A] = E

    implicit class EqualOps[A: Equal](x: A) {
      def ===(y: A): Boolean = Equal[A].equals(x, y)
    }
  }*/

  implicit def optionSemigroup[A: Semigroup]: Semigroup[Option[A]] = {
    case (Some(i), Some(j)) => Some(Semigroup[A].combine(i, j))
    case (Some(i), None)    => Some(i)
    case (None, Some(j))    => Some(j)
    case (None, None)       => None
  }

  val sumMaybeNumbers = Semigroup[Option[Int]].combine(Some(1), Some(3))

  case class Num(value: Int)

  object Num {
    implicit val numSemigroup: Semigroup[Num] = new Semigroup[Num] {
      def combine(x: Num, y: Num): Num = Num(x.value + y.value)
    }
  }

  implicit def numSemigroup[Num: Semigroup]: Semigroup[Num] = {
    case (a, b) => Semigroup[Num].combine(a, b)
    case (_, b) => b
    case (a, _) => a
  }

  val sumNum = Semigroup[Num].combine(Num(1), Num(2))
  Num(1) |+| Num(1)
}
