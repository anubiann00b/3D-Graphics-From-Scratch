package me.shreyasr.graphics

class Engine {

  def execute(modelCoords: Array[Vec], translateVec: Vec, scaleVec: Vec, rotateVec: Vec,
              screenWidth: Int, screenHeight: Int): Array[Vec] = {
    val worldCoords = modelToWorld(modelCoords, translateVec, scaleVec, rotateVec)
    val projectionCoords = worldToProjection(worldCoords, 100, 100, 0, 100)
    val screenCoords = projectionToScreen(projectionCoords, screenWidth, screenHeight)
    screenCoords
  }

  def modelToWorld(modelCoords: Array[Vec], translateVec: Vec, scaleVec: Vec, rotateVec: Vec): Array[Vec] = {
    val modelToWorldTransform = Mat.scale(scaleVec) * Mat.rotate(rotateVec) * Mat.translation(translateVec)
    modelCoords.map(modelToWorldTransform * _)
  }

  def worldToProjection(worldCoords: Array[Vec], width: Int, height: Int,
                        near: Float, far: Float): Array[Vec] = {
    val worldToProjectionTransform = Mat.ortho(width, height, near, far)
    worldCoords.map(worldToProjectionTransform * _)
  }

  def projectionToScreen(projectionCoords: Array[Vec], screenWidth: Int, screenHeight: Int): Array[Vec] = {
    // divide by W first here when we use a frustum projection
    projectionCoords
      .map(v => (v + 1) / 2) // map between 0 and 1
      .filterNot(v => (0 to 2).exists(i => v(i) < 0 || v(i) > 1)) // >= one component outside of unit cube
      .map(_ scalar Vec(screenWidth, screenHeight)) // scale to screen size
  }
}