package org.ditw.learning.dl4j.lstm.movement

object DataGen {

  object MovType extends Enumeration {
    type MovType = Value
    val E, SE, S, SW, W, NW, N, NE, O = Value
  }

  import MovType._

  private[DataGen] val movTypeMap = Map(
    E -> 1,
    SE -> 2,
    S -> 3,
    SW -> 4,
    W -> 5,
    NW -> 6,
    N -> 7,
    NE -> 8,
    O -> 9
  )

  private[DataGen] val movTypeRefMap = Map(
    (None, None) -> O,
    (None, Option(S)) -> S,
    (None, Option(N)) -> N,
    (Option(E), None) -> E,
    (Option(W), None) -> W,
    (Option(W), Option(S)) -> SW,
    (Option(W), Option(N)) -> NW,
    (Option(E), Option(S)) -> SE,
    (Option(E), Option(N)) -> NE
  )

  // d: Array[4] - E S W N
  private val tol0 = 1e-5
  case class MData(d:Array[Double]) {
    private[movement] def t:MovType = {
      val deltaE = d(0) - d(2)
      val deltaS = d(1) - d(3)

      val mE =
        if (math.abs(deltaE) > tol0) {
          if (deltaE > 0) Option(E)
          else Option(W)
        }
        else None
      val mS =
        if (math.abs(deltaS) > tol0) {
          if (deltaS > 0) Option(S)
          else Option(N)
        }
        else None

      movTypeRefMap(mE -> mS)
    }
  }

}
