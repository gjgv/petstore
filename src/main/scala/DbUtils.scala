package petstore

import java.sql.{Connection, DriverManager}

import cats.effect.IO

object DbUtils {
  val dbUsername = "sa"
  val dbPassword = ""

  def runDbQuery(jdbcUrl: String)(f: Connection => Unit): IO[Unit] = IO.delay {
    var conn: Connection = null
    try {
      conn = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword)
      f(conn)
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (conn != null) {
        conn.close()
      }
    }
  }
}
