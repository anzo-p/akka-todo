package me.anzop.todo.actor

trait SnapShootTally {
  def snapshotInterval: Int
  var countToSnapShot: Int = 0

  def maybeSnapshotDue: Boolean = {
    countToSnapShot += 1
    if (countToSnapShot >= snapshotInterval) {
      countToSnapShot = 0
      true
    } else {
      false
    }
  }
}
