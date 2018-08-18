package org.ditw.thermapp.onedrive.data

import org.joda.time.DateTime

class TimeSeriesData[T](val timestamp:Long, val data:T) {

}

object TimeSeriesData {
  def from[T](t:DateTime, data:T):TimeSeriesData[T] = {
    new TimeSeriesData(t.getMillis, data)
  }

  implicit val ordByTime:Ordering[TimeSeriesData[_]] = Ordering.by(_.timestamp)
}
