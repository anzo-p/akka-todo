package todoapp.system

import akka.stream.Materializer

trait FlowMaterializeProvider {
  implicit val materialize: Materializer
}
