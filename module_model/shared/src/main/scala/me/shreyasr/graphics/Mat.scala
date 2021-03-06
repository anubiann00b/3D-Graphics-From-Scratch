package me.shreyasr.graphics

/**
  * A matrix represented by an array of values with a specfied number of columns
  *
  * @param values The values of the matrix
  * @param _cols The number of columns of the matrix
  */
class Mat private(private val values: Array[Float], private val _cols: Int) {

  require(values != null, "Matrix values cannot be null!")
  require(values.nonEmpty, "Matrix values cannot be empty!")
  require(values.length % cols == 0, s"Not rectangular: ${values.length} values with width: $cols")

  def cols: Int = _cols
  def rows: Int = values.length / _cols

  def apply(row: Int, col: Int) = get(row, col)
  def get(row: Int, col: Int) = values(index(row, col))
  def index(row: Int, col: Int) = row*cols + col
  def set(row: Int, col: Int)(float: Float) = new Mat(values.updated(index(row, col), float), cols)

  def foreach[U](f: Float => U): Unit = values.foreach(f)
  def iterator: Iterator[Float] = values.iterator
  def map(f: Float => Float): Mat = new Mat(values.map(f).array, cols)

  def transpose: Mat =
    new Mat(Array.tabulate(rows, cols)((r, c) => apply(r, c)).transpose.flatten, rows)

  /**
    * Multiply this matrix by another matrix.
    *
    * @param that The matrix to multiply by
    * @return A new matrix with the value of this*that
    * @throws IllegalArgumentException if the matrices are of invalid dimensions
    */
  def *(that: Mat): Mat = {
    require(this.cols == that.rows, s"Invalid dimensions! ${rows}x$cols * ${that.rows}x${that.cols}")
    if (this.cols != that.rows) throw new IllegalArgumentException("Invalid dimensions!")
    new Mat(
      (0 until this.rows).flatMap(row => {
        (0 until that.cols).map(col => {
          (0 until this.cols).foldLeft(0f)((accum, next) => {
            accum + this(row, next) * that(next, col)
          })
        })
      }).toArray, this.rows)
  }

  // Multiply the matrix by a scalar
  def *(scalar: Float): Mat = map(_ * scalar)
  def *(scalar: Int): Mat = map(_ * scalar)

  /**
    * Multiplies the matrix by the column vector v
    *
    * @param v The column vector to multiply by
    * @return A new vector representing the value of this * v
    */
  def *(v: Vec): Vec = {
    require(v.length == rows && rows == cols, s"Mat * vec: ${rows}x$cols * ${v.length}x1")
    Vec((0 until v.length).map(idx => {
      (0 until v.length).foldLeft(0f)((a, i) => a + v(i) * get(idx, i))
    }) :_*)
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case that: Mat =>
        this.rows == that.rows && this.cols == that.cols &&
          (this.values, that.values).zipped.forall(_ == _)
      case _ => false
    }
  }

  override def toString: String = values.sliding(cols, cols).map(_.mkString(", ")).mkString("\n")
}

object Mat {

  /**
    * Constructs a matrix representing a perspective transformation.
    *
    * @param fovx The horizontal field of view in radians
    * @param fovy The vertical field of view in radians
    * @param near The distance to the near plane of the perspective frustum
    * @param far The distance to the far plane of the perspective frustum
    * @return A matrix representing the transformation
    */
  def perspective(fovx: Float, fovy: Float, near: Float, far: Float): Mat = {
    Mat(
      (math.atan(fovx/2), 0, 0, 0),
      (0, math.atan(fovy/2), 0, 0),
      (0, 0, -(far+near)/(far-near), -2*far*near/(far-near)),
      (0, 0, -1, 0))
  }

  /**
    * Constructs a matrix representing an orthographic transformation.
    *
    * @param width The width of the orthographic space
    * @param height The height of the orthographic space
    * @param near The distance to the near plane of the orthographic space
    * @param far The distance to the far plane of the orthographic space
    * @return A matrix representing the transformation
    */
  def ortho(width: Float, height: Float, near: Float, far: Float): Mat = {
    Mat(
      (1/width, 0, 0, 0),
      (0, 1/height, 0, 0),
      (0, 0, -2/(far-near), -(far+near)/(far-near)),
      (0, 0, 0, 1))
  }

  /**
    * Constructs a matrix representing a translation transformation.
    *
    * @param vec The vector to translate by.
    * @return A matrix representing the transformation
    */
  def translation(vec: Vec): Mat = {
    require(vec.length == 3)
    (0 until 3).foldLeft(identity(4))((matrix, i) => matrix.set(i, matrix.rows-1)(vec(i)))
  }

  /**
    * Constructs a matrix representing a scale transformation.
    *
    * @param vec The vector to scale by.
    * @return A matrix representing the transformation
    */
  def scale(vec: Vec): Mat = {
    require(vec.length == 3)
    (0 until 3).foldLeft(identity(4))((matrix, i) => matrix.set(i, i)(vec(i)))
  }

  /**
    * Constructs a matrix representing a rotation transformation about multiple axes.
    *
    * @param vec The vector to translate by. Each component is a direction of rotation.
    * @return A matrix representing the transformation
    */
  def rotate(vec: Vec): Mat = {
    require(vec.length == 3)
    (0 until 3).foldLeft(identity(4))((mat: Mat, idx: Int) => rotate(idx)(vec(idx)) * mat)
  }

  /**
    * Constructs a matrix representing a rotation transformation about a single axis.
    *
    * @param axisIdx The axis to rotate around
    * @param theta The angle to rotate in radians
    * @return A matrix representing the transformation
    */
  def rotate(axisIdx: Int)(theta: Float): Mat = {
    var matrix = identity(4)
    val iter = List(math.cos(theta), -math.sin(theta), math.sin(theta), math.cos(theta)).iterator
    (0 until 3).filterNot(_ == axisIdx).foreach(row => {
      (0 until 3).filterNot(_ == axisIdx).foreach(col => {
        matrix = matrix.set(row, col)(iter.next().toFloat)
      })
    })
    matrix
  }

  /**
    * Constructs an identity matrix of a specified size.
    */
  def identity(size: Int): Mat = {
    new Mat(
      (0 until size).map(i => i*(size+1))
        .foldLeft(new Array[Float](size*size))((arr, i) => { arr.update(i, 1); arr }),
      size)
  }

  /**
    * Constructs a matrix from a parameter list of rows, each represented by a tuple.
    *
    * @param rows The rows of the matrix
    * @return A new matrix with the specified rows
    */
  def apply(rows: Product*): Mat = {
    require(rows.nonEmpty, "Matrix must have rows!")
    val width = rows.head.productArity
    require(rows.forall(_.productArity == width), "Row must have same length")
    new Mat(rows.flatMap(_.productIterator).map {
      case i: Int => i.toFloat
      case f: Float => f
      case d: Double => d.toFloat
      case _ => 0f
    }.toArray, width)
  }
}
