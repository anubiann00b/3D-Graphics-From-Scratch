import me.shreyasr.graphics.Vec
import utest._

object VectorTest extends TestSuite {

  override def tests = TestSuite {
    'VectorInitialization {
      val vector = Vec(1, 2, 3)
      assert(vector(0) == 1)
    }
    'VectorEquality {
      val vector = Vec(1, 2, 3)
      assert(vector == Vec(1, 2, 3))
      assert(vector != Vec(0, 2, 3))
      assert(vector != Vec(1, 2, 3, 4))
    }
    'VectorTraversable {
      val vector = Vec(1, 2, 3)
      assert(vector.map(_ + 3) == Vec(4, 5, 6))
      assert(vector.map(_ + 3).get(1) == 5)
    }
    'VectorScalarMult {
      val vector3 = Vec(1, 2, 3)
      val vector2 = Vec(2, 3)
      assert((vector3 scalar vector2) == Vec(2, 6, 3))
      intercept[IllegalArgumentException](vector2 scalar vector3)
    }
  }
}
