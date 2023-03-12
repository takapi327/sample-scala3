
package ldbc

import com.mysql.cj.jdbc.MysqlDataSource

trait MySQLConfigReader extends DatabaseConfigReader:

  def buildDataSource: MysqlDataSource =
    val dataSource = new MysqlDataSource()

    dataSource.setServerName("127.0.0.1")
    dataSource.setPortNumber(13306)
    dataSource.setDatabaseName("sample_doobie")
    dataSource.setUser("takapi327")
    dataSource.setPassword("takapi327")

    dataSource
