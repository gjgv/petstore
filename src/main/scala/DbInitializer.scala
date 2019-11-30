package petstore

import java.io.IOException
import java.nio.file.{Files, Path, Paths}

import cats.effect.IO

object DbInitializer {
  private val appName = "petstore"
  private val dbName = "pets"

  def createDb: IO[String] = for {
    appDir <- initAppDir
    jdbcUrl <- createJdbcUrl(appDir)
    _ <- attemptCreateDb(jdbcUrl)
  } yield jdbcUrl

  private def initAppDir: IO[Path] = for {
    dir <- getAppDir
    _ <- createDir(dir)
  } yield dir

  private def getAppDir: IO[Path] =
    getUserDataDir.map(_.resolve(appName))

  private def getUserDataDir: IO[Path] = IO.delay {
    val os = System.getProperty("os.name").toLowerCase

    if (os.contains("win")) {
      Paths.get(System.getenv("APPDATA"))
    } else if (os.contains("mac")) {
      Paths.get(System.getProperty("user.home"), "Library", "Application Support")
    } else { //linux
      Paths.get(System.getProperty("user.home"), ".local", "share")
    }
  }

  private def createDir(dir: Path): IO[Unit] = IO.delay {
    if (!Files.exists(dir))
      Files.createDirectory(dir)
    else if (!Files.isWritable(dir))
      throw new IOException("App directory is not writeable")
    ()
  }

  private def createJdbcUrl(directoryPath: Path): IO[String] = IO.delay {
    val dbDirPath = directoryPath.resolve("db")
    dbDirPath.toFile.mkdirs()

    val dbPath = dbDirPath.resolve(dbName)
    "jdbc:h2:" + dbPath.toString
  }

  private def attemptCreateDb(jdbcUrl: String): IO[Unit] = {
    val sql = """
      create schema if not exists petstore;

      set schema petstore;

      create table if not exists pets (
        id int auto_increment primary key,
        name varchar(255) not null
      );"""

    DbUtils.runDbQuery(jdbcUrl) { conn =>
      val statement = conn.createStatement()
      try {
        statement.execute(sql)
      } catch {
        case e: Exception =>
          println(s"DB creation failed")
          e.printStackTrace()
      }
      finally {
        statement.close()
      }
    }
  }
}