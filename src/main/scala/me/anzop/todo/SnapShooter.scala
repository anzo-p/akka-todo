package me.anzop.todo

trait SnapShooter {
  def snapshotInterval: Int
  var countToSnapShot: Int = 0

  def maybeSnapshot: Boolean = {
    countToSnapShot += 1
    if (countToSnapShot >= snapshotInterval) {
      countToSnapShot = 0
      true
    } else {
      false
    }
  }
}
