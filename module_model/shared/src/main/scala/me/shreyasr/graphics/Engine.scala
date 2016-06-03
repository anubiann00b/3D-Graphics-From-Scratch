package me.shreyasr.graphics

class Engine {

  def execute(modelCoords: Array[Vec], translateVec: Vec, scaleVec: Vec, rotateVec: Vec,
              fovx: Float, fovy: Float, near: Int, far: Int,
              screenWidth: Int, screenHeight: Int): Array[Vec] = {
    val worldCoords = modelToWorld(modelCoords, translateVec, scaleVec, rotateVec)
    val projectionCoords = worldToProjection(worldCoords, fovx, fovy, near, far)
    val screenCoords = projectionToScreen(projectionCoords, screenWidth, screenHeight)
    screenCoords
  }

  def modelToWorld(modelCoords: Array[Vec], translateVec: Vec, scaleVec: Vec, rotateVec: Vec): Array[Vec] = {
    val modelToWorldTransform = Mat.scale(scaleVec) * Mat.rotate(rotateVec) * Mat.translation(translateVec)
    modelCoords.map(modelToWorldTransform * _)
  }

  def worldToProjection(worldCoords: Array[Vec], fovx: Float, fovy: Float,
                        near: Float, far: Float): Array[Vec] = {
    println(worldCoords.head)
    val worldToProjectionTransform = Mat.perspective(math.toRadians(fovx).toFloat, math.toRadians(fovy).toFloat, near, far)
    worldCoords.map(worldToProjectionTransform * _)
      .map(vec => if (vec.w != 1) vec / vec.w else vec) // w normalization for frustum
  }

  def projectionToScreen(projectionCoords: Array[Vec], screenWidth: Int, screenHeight: Int): Array[Vec] = {
    println(projectionCoords.head)
    projectionCoords
      .map(v => (v + 1) / 2) // map between 0 and 1
//      .filterNot(v => (0 until 3).exists(i => v(i) < -1 || v(i) > 1)) // >= one component outside of unit cube
      .map(_ scalar Vec(screenWidth, screenHeight)) // scale to screen size
  }
}
