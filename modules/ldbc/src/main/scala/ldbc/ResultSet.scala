package ldbc

import cats.effect.Sync

import java.io.{ InputStream, Reader }
import java.sql.{ Date, ResultSetMetaData, SQLWarning, Time, Timestamp }

import scala.jdk.CollectionConverters.*

trait ResultSet[F[_]]:

  final def get[T](columnLabel: String)(using loader: ResultSetLoader[F, T]): F[T] =
    loader.load(this, columnLabel)

  /**
   * Moves the cursor forward one row from its current position. A ResultSet cursor is initially positioned before the first row; the first call to the method next makes the first row the current row; the second call makes the second row the current row, and so on.
   *
   * When a call to the next method returns false, the cursor is positioned after the last row. Any invocation of a ResultSet method which requires a current row will result in a SQLException being thrown. If the result set type is TYPE_FORWARD_ONLY, it is vendor specified whether their JDBC driver implementation will return false or throw an SQLException on a subsequent call to next.
   *
   * If an input stream is open for the current row, a call to the method next will implicitly close it. A ResultSet object's warning chain is cleared when a new row is read.
   *
   * @return
   *   true if the new current row is valid
   *   false if there are no more rows
   */
  def next(): F[Boolean]

  /**
   * Releases this ResultSet object's database and JDBC resources immediately instead of waiting for this to happen when it is automatically closed.
   *
   * The closing of a ResultSet object does not close the Blob, Clob or NClob objects created by the ResultSet. Blob, Clob or NClob objects remain valid for at least the duration of the transaction in which they are created, unless their free method is invoked.
   *
   * When a ResultSet is closed, any ResultSetMetaData instances that were created by calling the getMetaData method remain accessible.
   *
   * Note: A ResultSet object is automatically closed by the Statement object that generated it when that Statement object is closed, re-executed, or is used to retrieve the next result from a sequence of multiple results.
   *
   * Calling the method close on a ResultSet object that is already closed is a no-op.
   */
  def close(): F[Unit]

  /**
   * Reports whether the last column read had a value of SQL NULL. Note that you must first call one of the getter methods on a column to try to read its value and then call the method wasNull to see if the value read was SQL NULL.
   *
   * @return
   * true if the last column value read was SQL NULL and false otherwise
   */
  def wasNull(): F[Boolean]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a String in the Java programming language.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   the column value; if the value is SQL NULL, the value returned is null
   */
  def getString(columnIndex: Int): F[String]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a String in the Java programming language.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is null
   */
  def getString(columnLabel: String): F[String]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a boolean in the Java programming language.
   *
   * If the designated column has a datatype of CHAR or VARCHAR and contains a "0" or has a datatype of BIT, TINYINT, SMALLINT, INTEGER or BIGINT and contains a 0, a value of false is returned. If the designated column has a datatype of CHAR or VARCHAR and contains a "1" or has a datatype of BIT, TINYINT, SMALLINT, INTEGER or BIGINT and contains a 1, a value of true is returned.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   the column value; if the value is SQL NULL, the value returned is false
   */
  def getBoolean(columnIndex: Int): F[Boolean]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a boolean in the Java programming language.
   *
   * If the designated column has a datatype of CHAR or VARCHAR and contains a "0" or has a datatype of BIT, TINYINT, SMALLINT, INTEGER or BIGINT and contains a 0, a value of false is returned. If the designated column has a datatype of CHAR or VARCHAR and contains a "1" or has a datatype of BIT, TINYINT, SMALLINT, INTEGER or BIGINT and contains a 1, a value of true is returned.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is false
   */
  def getBoolean(columnLabel: String): F[Boolean]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a byte in the Java programming language.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   the column value; if the value is SQL NULL, the value returned is 0
   */
  def getByte(columnIndex: Int): F[Byte]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a byte in the Java programming language.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is 0
   */
  def getByte(columnLabel: String): F[Byte]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a byte array in the Java programming language. The bytes represent the raw values returned by the driver.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   the column value; if the value is SQL NULL, the value returned is null
   */
  def getBytes(columnIndex: Int): F[Array[Byte]]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a byte array in the Java programming language. The bytes represent the raw values returned by the driver.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is null
   */
  def getBytes(columnLabel: String): F[Array[Byte]]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a short in the Java programming language.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   the column value; if the value is SQL NULL, the value returned is 0
   */
  def getShort(columnIndex: Int): F[Short]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a short in the Java programming language.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is 0
   */
  def getShort(columnLabel: String): F[Short]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as an int in the Java programming language.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   the column value; if the value is SQL NULL, the value returned is 0
   */
  def getInt(columnIndex: Int): F[Int]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as an int in the Java programming language.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is 0
   */
  def getInt(columnLabel: String): F[Int]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a long in the Java programming language.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @return
   * the column value; if the value is SQL NULL, the value returned is 0
   */
  def getLong(columnIndex: Int): F[Long]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a long in the Java programming language.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is 0
   */
  def getLong(columnLabel: String): F[Long]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a float in the Java programming language.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @return
   * the column value; if the value is SQL NULL, the value returned is 0
   */
  def getFloat(columnIndex: Int): F[Float]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a float in the Java programming language.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is 0
   */
  def getFloat(columnLabel: String): F[Float]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a double in the Java programming language.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @return
   * the column value; if the value is SQL NULL, the value returned is 0
   */
  def getDouble(columnIndex: Int): F[Double]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a double in the Java programming language.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is 0
   */
  def getDouble(columnLabel: String): F[Double]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Date object in the Java programming language.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, .
   * @return
   *   the column value; if the value is SQL NULL, the value returned is null
   */
  def getDate(columnIndex: Int): F[java.sql.Date]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Date object in the Java programming language.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is null
   */
  def getDate(columnLabel: String): F[java.sql.Date]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Time object in the Java programming language.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, .
   * @return
   *   the column value; if the value is SQL NULL, the value returned is null
   */
  def getTime(columnIndex: Int): F[java.sql.Time]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Time object in the Java programming language.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is null
   */
  def getTime(columnLabel: String): F[java.sql.Time]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Timestamp object in the Java programming language.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   the column value; if the value is SQL NULL, the value returned is null
   */
  def getTimestamp(columnIndex: Int): F[java.sql.Timestamp]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a java.sql.Timestamp object in the Java programming language.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value; if the value is SQL NULL, the value returned is null
   */
  def getTimestamp(columnLabel: String): F[java.sql.Timestamp]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a stream of ASCII characters. The value can then be read in chunks from the stream. This method is particularly suitable for retrieving large LONGVARCHAR values. The JDBC driver will do any necessary conversion from the database format into ASCII.
   *
   * Note: All the data in the returned stream must be read prior to getting the value of any other column. The next call to a getter method implicitly closes the stream. Also, a stream may return 0 when the method InputStream.available is called whether there is data available or not
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   a Java input stream that delivers the database column value as a stream of one-byte ASCII characters; if the value is SQL NULL, the value returned is null
   */
  def getAsciiStream(columnIndex: Int): F[java.io.InputStream]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a stream of ASCII characters. The value can then be read in chunks from the stream. This method is particularly suitable for retrieving large LONGVARCHAR values. The JDBC driver will do any necessary conversion from the database format into ASCII.
   *
   * Note: All the data in the returned stream must be read prior to getting the value of any other column. The next call to a getter method implicitly closes the stream. Also, a stream may return 0 when the method available is called whether there is data available or not.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   a Java input stream that delivers the database column value as a stream of one-byte ASCII characters. If the value is SQL NULL, the value returned is null.
   */
  def getAsciiStream(columnLabel: String): F[java.io.InputStream]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a stream of uninterpreted bytes. The value can then be read in chunks from the stream. This method is particularly suitable for retrieving large LONGVARBINARY values.
   *
   * Note: All the data in the returned stream must be read prior to getting the value of any other column. The next call to a getter method implicitly closes the stream. Also, a stream may return 0 when the method InputStream.available is called whether there is data available or not.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *
   *  a Java input stream that delivers the database column value as a stream of uninterpreted bytes; if the value is SQL NULL, the value returned is null
   */
  def getBinaryStream(columnIndex: Int): F[java.io.InputStream]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a stream of uninterpreted bytes. The value can then be read in chunks from the stream. This method is particularly suitable for retrieving large LONGVARBINARY values.
   *
   * Note: All the data in the returned stream must be read prior to getting the value of any other column. The next call to a getter method implicitly closes the stream. Also, a stream may return 0 when the method available is called whether there is data available or not.
   *
   * @param columnLabel
   *   columnLabel – the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   a Java input stream that delivers the database column value as a stream of uninterpreted bytes; if the value is SQL NULL, the result is null
   */
  def getBinaryStream(columnLabel: String): F[java.io.InputStream]

  /**
   * Retrieves the first warning reported by calls on this ResultSet object. Subsequent warnings on this ResultSet object will be chained to the SQLWarning object that this method returns.
   *
   * The warning chain is automatically cleared each time a new row is read. This method may not be called on a ResultSet object that has been closed; doing so will cause an SQLException to be thrown.
   *
   * Note: This warning chain only covers warnings caused by ResultSet methods. Any warning caused by Statement methods (such as reading OUT parameters) will be chained on the Statement object.
   *
   * @return
   *   the first SQLWarning object reported or null if there are none
   */
  def getWarnings(): F[java.sql.SQLWarning]

  /**
   * Clears all warnings reported on this ResultSet object. After this method is called, the method getWarnings returns null until a new warning is reported for this ResultSet object.
   */
  def clearWarnings(): F[Unit]

  /**
   * Retrieves the name of the SQL cursor used by this ResultSet object.
   *
   * In SQL, a result table is retrieved through a cursor that is named. The current row of a result set can be updated or deleted using a positioned update/delete statement that references the cursor name. To insure that the cursor has the proper isolation level to support update, the cursor's SELECT statement should be of the form SELECT FOR UPDATE. If FOR UPDATE is omitted, the positioned updates may fail.
   *
   * The JDBC API supports this SQL feature by providing the name of the SQL cursor used by a ResultSet object. The current row of a ResultSet object is also the current row of this SQL cursor.
   *
   * @return
   *   the SQL name for this ResultSet object's cursor
   */
  def getCursorName(): F[String]

  /**
   * Retrieves the number, types and properties of this ResultSet object's columns.
   * @return
   *   the description of this ResultSet object's columns
   */
  def getMetaData(): F[java.sql.ResultSetMetaData]

  /**
   * Gets the value of the designated column in the current row of this ResultSet object as an Object in the Java programming language.
   *
   * This method will return the value of the given column as a Java object. The type of the Java object will be the default Java object type corresponding to the column's SQL type, following the mapping for built-in types specified in the JDBC specification. If the value is an SQL NULL, the driver returns a Java null.
   *
   * This method may also be used to read database-specific abstract data types. In the JDBC 2.0 API, the behavior of method getObject is extended to materialize data of SQL user-defined types.
   *
   * If Connection.getTypeMap does not throw a SQLFeatureNotSupportedException, then when a column contains a structured or distinct value, the behavior of this method is as if it were a call to: getObject(columnIndex, this.getStatement().getConnection().getTypeMap()). If Connection.getTypeMap does throw a SQLFeatureNotSupportedException, then structured values are not supported, and distinct values are mapped to the default Java class as determined by the underlying SQL type of the DISTINCT type.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   a java.lang.Object holding the column value
   */
  def getObject(columnIndex: Int): F[Object]

  /**
   * Gets the value of the designated column in the current row of this ResultSet object as an Object in the Java programming language.
   *
   * This method will return the value of the given column as a Java object. The type of the Java object will be the default Java object type corresponding to the column's SQL type, following the mapping for built-in types specified in the JDBC specification. If the value is an SQL NULL, the driver returns a Java null.
   *
   * This method may also be used to read database-specific abstract data types.
   *
   * In the JDBC 2.0 API, the behavior of the method getObject is extended to materialize data of SQL user-defined types. When a column contains a structured or distinct value, the behavior of this method is as if it were a call to: getObject(columnIndex, this.getStatement().getConnection().getTypeMap()).
   *
   * @param columnLabel
   *   the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   a java.lang.Object holding the column value
   */
  def getObject(columnLabel: String): F[Object]

  /**
   * Maps the given ResultSet column label to its ResultSet column index.
   *
   * @param columnLabel
   *   the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column index of the given column name
   */
  def findColumn(columnLabel: String): F[Int]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a java.io.Reader object.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   a java.io.Reader object that contains the column value; if the value is SQL NULL, the value returned is null in the Java programming language.
   */
  def getCharacterStream(columnIndex: Int): F[java.io.Reader]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a java.io.Reader object.
   *
   * @param columnLabel
   *   the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   a java.io.Reader object that contains the column value; if the value is SQL NULL, the value returned is null in the Java programming language
   */
  def getCharacterStream(columnLabel: String): F[java.io.Reader]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a java.math.BigDecimal with full precision.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @return
   *   the column value (full precision); if the value is SQL NULL, the value returned is null in the Java programming language.
   */
  def getBigDecimal(columnIndex: Int): F[BigDecimal]

  /**
   * Retrieves the value of the designated column in the current row of this ResultSet object as a java.math.BigDecimal with full precision.
   *
   * @param columnLabel
   *   the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @return
   *   the column value (full precision); if the value is SQL NULL, the value returned is null in the Java programming language.
   */
  def getBigDecimal(columnLabel: String): F[BigDecimal]

  /**
   * Retrieves whether the cursor is before the first row in this ResultSet object.
   *
   * Note:Support for the isBeforeFirst method is optional for ResultSets with a result set type of TYPE_FORWARD_ONLY
   *
   * @return
   *   true if the cursor is before the first row; false if the cursor is at any other position or the result set contains no rows
   */
  def isBeforeFirst(): F[Boolean]

  /**
   * Retrieves whether the cursor is after the last row in this ResultSet object.
   *
   * Note:Support for the isAfterLast method is optional for ResultSets with a result set type of TYPE_FORWARD_ONLY
   *
   * @return
   *   true if the cursor is after the last row; false if the cursor is at any other position or the result set contains no rows
   */
  def isAfterLast(): F[Boolean]

  /**
   * Retrieves whether the cursor is on the first row of this ResultSet object.
   *
   * Note:Support for the isFirst method is optional for ResultSets with a result set type of TYPE_FORWARD_ONLY
   *
   * @return
   *   true if the cursor is on the first row; false otherwise
   */
  def isFirst(): F[Boolean]

  /**
   * Retrieves whether the cursor is on the last row of this ResultSet object. Note: Calling the method isLast may be expensive because the JDBC driver might need to fetch ahead one row in order to determine whether the current row is the last row in the result set.
   *
   * Note: Support for the isLast method is optional for ResultSets with a result set type of TYPE_FORWARD_ONLY
   *
   * @return
   *   true if the cursor is on the last row; false otherwise
   */
  def isLast(): F[Boolean]

  /**
   * Moves the cursor to the front of this ResultSet object, just before the first row. This method has no effect if the result set contains no rows.
   */
  def beforeFirst(): F[Unit]

  /**
   * Moves the cursor to the end of this ResultSet object, just after the last row. This method has no effect if the result set contains no rows.
   */
  def afterLast(): F[Unit]

  /**
   * Moves the cursor to the first row in this ResultSet object.
   *
   * @return
   *   true if the cursor is on a valid row; false if there are no rows in the result set
   */
  def first(): F[Boolean]

  /**
   * Moves the cursor to the last row in this ResultSet object.
   *
   * @return
   *   true if the cursor is on a valid row; false if there are no rows in the result set
   */
  def last(): F[Boolean]

  /**
   * Retrieves the current row number. The first row is number 1, the second number 2, and so on.
   *
   * Note:Support for the getRow method is optional for ResultSets with a result set type of TYPE_FORWARD_ONLY
   *
   * @return
   *  the current row number; 0 if there is no current row
   */
  def getRow(): F[Int]

  /**
   * Moves the cursor to the given row number in this ResultSet object.
   *
   * If the row number is positive, the cursor moves to the given row number with respect to the beginning of the result set. The first row is row 1, the second is row 2, and so on.
   *
   * If the given row number is negative, the cursor moves to an absolute row position with respect to the end of the result set. For example, calling the method absolute(-1) positions the cursor on the last row; calling the method absolute(-2) moves the cursor to the next-to-last row, and so on.
   *
   * If the row number specified is zero, the cursor is moved to before the first row.
   *
   * An attempt to position the cursor beyond the first/last row in the result set leaves the cursor before the first row or after the last row.
   *
   * Note: Calling absolute(1) is the same as calling first(). Calling absolute(-1) is the same as calling last().
   *
   * @param row
   *   the number of the row to which the cursor should move. A value of zero indicates that the cursor will be positioned before the first row; a positive number indicates the row number counting from the beginning of the result set; a negative number indicates the row number counting from the end of the result set
   * @return
   *   true if the cursor is moved to a position in this ResultSet object; false if the cursor is before the first row or after the last row
   */
  def absolute(row: Int): F[Boolean]

  /**
   * Moves the cursor a relative number of rows, either positive or negative. Attempting to move beyond the first/last row in the result set positions the cursor before/after the the first/last row. Calling relative(0) is valid, but does not change the cursor position.
   *
   * Note: Calling the method relative(1) is identical to calling the method next() and calling the method relative(-1) is identical to calling the method previous().
   *
   * @param rows
   *   an int specifying the number of rows to move from the current row; a positive number moves the cursor forward; a negative number moves the cursor backward
   * @return
   *   true if the cursor is on a row; false otherwise
   */
  def relative(rows: Int): F[Boolean]

  /**
   * Moves the cursor to the previous row in this ResultSet object.
   *
   * When a call to the previous method returns false, the cursor is positioned before the first row. Any invocation of a ResultSet method which requires a current row will result in a SQLException being thrown.
   *
   * f an input stream is open for the current row, a call to the method previous will implicitly close it. A ResultSet object's warning change is cleared when a new row is read.
   *
   * @return
   *   true if the cursor is now positioned on a valid row; false if the cursor is positioned before the first row
   */
  def previous(): F[Boolean]

  /**
   * Gives a hint as to the direction in which the rows in this ResultSet object will be processed. The initial value is determined by the Statement object that produced this ResultSet object. The fetch direction may be changed at any time.
   *
   * @param direction
   *   an int specifying the suggested fetch direction; one of [[ResultSet.FETCH_FORWARD]], [[ResultSet.FETCH_REVERSE]], or [[ResultSet.FETCH_UNKNOWN]]
   */
  def setFetchDirection(direction: ResultSet.FetchType): F[Unit]

  /**
   * Retrieves the fetch direction for this ResultSet object.
   *
   * @return
   *   the current fetch direction for this ResultSet object
   */
  def getFetchDirection(): F[ResultSet.FetchType]

  /**
   * Gives the JDBC driver a hint as to the number of rows that should be fetched from the database when more rows are needed for this ResultSet object. If the fetch size specified is zero, the JDBC driver ignores the value and is free to make its own best guess as to what the fetch size should be. The default value is set by the Statement object that created the result set. The fetch size may be changed at any time.
   *
   * @param rows
   *   the number of rows to fetch
   */
  def setFetchSize(rows: Int): F[Unit]

  /**
   * Retrieves the fetch size for this ResultSet object.
   *
   * @return
   *   the current fetch size for this ResultSet object
   */
  def getFetchSize(): F[ResultSet.FetchType]

  /**
   * Retrieves the type of this ResultSet object. The type is determined by the Statement object that created the result set.
   *
   * @return
   *   [[ResultSet.TYPE_FORWARD_ONLY]], [[ResultSet.TYPE_SCROLL_INSENSITIVE]], or [[ResultSet.TYPE_SCROLL_SENSITIVE]]
   */
  def getType(): F[ResultSet.Type]

  /**
   * Retrieves the concurrency mode of this ResultSet object. The concurrency used is determined by the Statement object that created the result set.
   *
   * @return
   *   the concurrency type, either [[ResultSet.CONCUR_READ_ONLY]] or [[ResultSet.CONCUR_UPDATABLE]]
   */
  def getConcurrency(): F[Int]

  /**
   * Retrieves whether the current row has been updated. The value returned depends on whether or not the result set can detect updates.
   *
   * Note: Support for the rowUpdated method is optional with a result set concurrency of CONCUR_READ_ONLY
   *
   * @return
   *   true if the current row is detected to have been visibly updated by the owner or another; false otherwise
   */
  def rowUpdated(): F[Boolean]

  /**
   * Retrieves whether the current row has had an insertion. The value returned depends on whether or not this ResultSet object can detect visible inserts.
   *
   * Note: Support for the rowInserted method is optional with a result set concurrency of CONCUR_READ_ONLY
   *
   * @return
   *   true if the current row is detected to have been inserted; false otherwise
   */
  def rowInserted(): F[Boolean]

  /**
   * Retrieves whether a row has been deleted. A deleted row may leave a visible "hole" in a result set. This method can be used to detect holes in a result set. The value returned depends on whether or not this ResultSet object can detect deletions.
   *
   * Note: Support for the rowDeleted method is optional with a result set concurrency of CONCUR_READ_ONLY
   *
   * @return
   *   true if the current row is detected to have been deleted by the owner or another; false otherwise
   */
  def rowDeleted(): F[Boolean]

  // === [ updating system processing ] ==============

  /**
   * Updates the designated column with a null value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   */
  def updateNull(columnIndex: Int): F[Unit]

  /**
   * Updates the designated column with a null value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   */
  def updateNull(columnLabel: String): F[Unit]

  /**
   * Updates the designated column with a boolean value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   *   the first column is 1, the second is 2, ...
   * @param x
   *   the new column value
   */
  def updateBoolean(columnIndex: Int, x: Boolean): F[Unit]

  /**
   * Updates the designated column with a byte value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateBoolean(columnLabel: String, x: Boolean): F[Unit]

  /**
   * Updates the designated column with a byte value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   *  the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateByte(columnIndex: Int, x: Byte): F[Unit]

  /**
   * Updates the designated column with a byte value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateByte(columnLabel: String, x: Byte): F[Unit]

  /**
   * Updates the designated column with a short value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateShort(columnIndex: Int, x: Short): F[Unit]

  /**
   * Updates the designated column with a short value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateShort(columnLabel: String, x: Short): F[Unit]

  /**
   * Updates the designated column with an int value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateInt(columnIndex: Int, x: Int): F[Unit]

  /**
   * Updates the designated column with an int value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateInt(columnLabel: String, x: Int): F[Unit]

  /**
   * Updates the designated column with a long value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateLong(columnIndex: Int, x: Long): F[Unit]

  /**
   * Updates the designated column with a long value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateLong(columnLabel: String, x: Long): F[Unit]

  /**
   * Updates the designated column with a float value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateFloat(columnIndex: Int, x: Float): F[Unit]

  /**
   * Updates the designated column with a float value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateFloat(columnLabel: String, x: Float): F[Unit]

  /**
   * Updates the designated column with a double value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateDouble(columnIndex: Int, x: Double): F[Unit]

  /**
   * Updates the designated column with a double value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateDouble(columnLabel: String, x: Double): F[Unit]

  /**
   * Updates the designated column with a java.math.BigDecimal value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateBigDecimal(columnIndex: Int, x: BigDecimal): F[Unit]

  /**
   * Updates the designated column with a java.sql.BigDecimal value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateBigDecimal(columnLabel: String, x: BigDecimal): F[Unit]

  /**
   * Updates the designated column with a String value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateString(columnIndex: Int, x: String): F[Unit]

  /**
   * Updates the designated column with a String value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateString(columnLabel: String, x: String): F[Unit]

  /**
   * Updates the designated column with a byte array value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateBytes(columnIndex: Int, x: Array[Byte]): F[Unit]

  /**
   * Updates the designated column with a byte array value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateBytes(columnLabel: String, x: Array[Byte]): F[Unit]

  /**
   * Updates the designated column with a java.sql.Date value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateDate(columnIndex: Int, x: java.sql.Date): F[Unit]

  /**
   * Updates the designated column with a java.sql.Date value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateDate(columnLabel: String, x: java.sql.Date): F[Unit]

  /**
   * Updates the designated column with a java.sql.Time value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateTime(columnIndex: Int, x: java.sql.Time): F[Unit]

  /**
   * Updates the designated column with a java.sql.Time value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateTime(columnLabel: String, x: java.sql.Time): F[Unit]

  /**
   * Updates the designated column with a java.sql.Timestamp value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateTimestamp(columnIndex: Int, x: java.sql.Timestamp): F[Unit]

  /**
   * Updates the designated column with a java.sql.Timestamp value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateTimestamp(columnLabel: String, x: java.sql.Timestamp): F[Unit]

  /**
   * Updates the designated column with an ascii stream value, which will have the specified number of bytes. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ... x – the new column value
   * @param x
   * the new column value
   * @param length
   * the length of the stream
   */
  def updateAsciiStream(columnIndex: Int, x: java.io.InputStream, length: Int): F[Unit]

  /**
   * Updates the designated column with an ascii stream value, which will have the specified number of bytes. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   * @param length
   * the length of the stream
   */
  def updateAsciiStream(columnLabel: String, x: java.io.InputStream, length: Int): F[Unit]

  /**
   * Updates the designated column with a binary stream value, which will have the specified number of bytes. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ... x – the new column value
   * @param x
   * the new column value
   * @param length
   * the length of the stream
   */
  def updateBinaryStream(columnIndex: Int, x: java.io.InputStream, length: Int): F[Unit]

  /**
   * Updates the designated column with a binary stream value, which will have the specified number of bytes. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   * @param length
   * the length of the stream
   */
  def updateBinaryStream(columnLabel: String, x: java.io.InputStream, length: Int): F[Unit]

  /**
   * Updates the designated column with a character stream value, which will have the specified number of bytes. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ... x – the new column value
   * @param x
   * the new column value
   * @param length
   * the length of the stream
   */
  def updateCharacterStream(columnIndex: Int, x: java.io.Reader, length: Int): F[Unit]

  /**
   * Updates the designated column with a character stream value, which will have the specified number of bytes. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   * @param length
   * the length of the stream
   */
  def updateCharacterStream(columnLabel: String, x: java.io.Reader, length: Int): F[Unit]

  /**
   * Updates the designated column with an Object value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * If the second argument is an InputStream then the stream must contain the number of bytes specified by scaleOrLength. If the second argument is a Reader then the reader must contain the number of characters specified by scaleOrLength. If these conditions are not true the driver will generate a SQLException when the statement is executed.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   * @param scaleOrLength
   * for an object of java.math.BigDecimal , this is the number of digits after the decimal point. For Java Object types InputStream and Reader, this is the length of the data in the stream or reader. For all other types, this value will be ignored.
   */
  def updateObject(columnIndex: Int, x: Object, scaleOrLength: Int): F[Unit]

  /**
   * Updates the designated column with an Object value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnIndex
   * the first column is 1, the second is 2, ...
   * @param x
   * the new column value
   */
  def updateObject(columnIndex: Int, x: Object): F[Unit]

  /**
   * Updates the designated column with an Object value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * If the second argument is an InputStream then the stream must contain the number of bytes specified by scaleOrLength. If the second argument is a Reader then the reader must contain the number of characters specified by scaleOrLength. If these conditions are not true the driver will generate a SQLException when the statement is executed.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   * @param scaleOrLength
   * for an object of java.math.BigDecimal , this is the number of digits after the decimal point. For Java Object types InputStream and Reader, this is the length of the data in the stream or reader. For all other types, this value will be ignored.
   */
  def updateObject(columnLabel: String, x: Object, scaleOrLength: Int): F[Unit]

  /**
   * Updates the designated column with an Object value. The updater methods are used to update column values in the current row or the insert row. The updater methods do not update the underlying database; instead the updateRow or insertRow methods are called to update the database.
   *
   * @param columnLabel
   * the label for the column specified with the SQL AS clause. If the SQL AS clause was not specified, then the label is the name of the column
   * @param x
   * the new column value
   */
  def updateObject(columnLabel: String, x: Object): F[Unit]

object ResultSet:

  enum FetchType(val int: Int):
    case FETCH_FORWARD extends FetchType(java.sql.ResultSet.FETCH_FORWARD)
    case FETCH_REVERSE extends FetchType(java.sql.ResultSet.FETCH_REVERSE)
    case FETCH_UNKNOWN extends FetchType(java.sql.ResultSet.FETCH_UNKNOWN)

  enum Type(val int: Int):
    case TYPE_FORWARD_ONLY       extends Type(java.sql.ResultSet.TYPE_FORWARD_ONLY)
    case TYPE_SCROLL_INSENSITIVE extends Type(java.sql.ResultSet.TYPE_SCROLL_INSENSITIVE)
    case TYPE_SCROLL_SENSITIVE   extends Type(java.sql.ResultSet.TYPE_SCROLL_SENSITIVE)

  enum CONCUR(val int: Int):
    case CONCUR_READ_ONLY extends CONCUR(java.sql.ResultSet.CONCUR_READ_ONLY)
    case CONCUR_UPDATABLE extends CONCUR(java.sql.ResultSet.CONCUR_UPDATABLE)

  def apply[F[_]: Sync](resultSet: java.sql.ResultSet): ResultSet[F] = new ResultSet[F]:
    override def next(): F[Boolean] = Sync[F].blocking(resultSet.next())

    override def close(): F[Unit] =
      if resultSet != null
        then Sync[F].blocking(resultSet.close())
        else Sync[F].unit

    override def wasNull(): F[Boolean] = Sync[F].blocking(resultSet.wasNull())

    override def getString(columnIndex: Int): F[String] = Sync[F].blocking(resultSet.getString(columnIndex))
    override def getString(columnLabel: String): F[String] = Sync[F].blocking(resultSet.getString(columnLabel))

    override def getBoolean(columnIndex: Int): F[Boolean] = Sync[F].blocking(resultSet.getBoolean(columnIndex))
    override def getBoolean(columnLabel: String): F[Boolean] = Sync[F].blocking(resultSet.getBoolean(columnLabel))

    override def getByte(columnIndex: Int): F[Byte] = Sync[F].blocking(resultSet.getByte(columnIndex))
    override def getByte(columnLabel: String): F[Byte] = Sync[F].blocking(resultSet.getByte(columnLabel))

    override def getBytes(columnIndex: Int): F[Array[Byte]] = Sync[F].blocking(resultSet.getBytes(columnIndex))
    override def getBytes(columnLabel: String): F[Array[Byte]] = Sync[F].blocking(resultSet.getBytes(columnLabel))

    override def getShort(columnIndex: Int): F[Short] = Sync[F].blocking(resultSet.getShort(columnIndex))
    override def getShort(columnLabel: String): F[Short] = Sync[F].blocking(resultSet.getShort(columnLabel))

    override def getInt(columnIndex: Int): F[Int] = Sync[F].blocking(resultSet.getInt(columnIndex))
    override def getInt(columnLabel: String): F[Int] = Sync[F].blocking(resultSet.getInt(columnLabel))

    override def getLong(columnIndex: Int): F[Long] = Sync[F].blocking(resultSet.getLong(columnIndex))
    override def getLong(columnLabel: String): F[Long] = Sync[F].blocking(resultSet.getLong(columnLabel))

    override def getFloat(columnIndex: Int): F[Float] = Sync[F].blocking(resultSet.getFloat(columnIndex))
    override def getFloat(columnLabel: String): F[Float] = Sync[F].blocking(resultSet.getFloat(columnLabel))

    override def getDouble(columnIndex: Int): F[Double] = Sync[F].blocking(resultSet.getDouble(columnIndex))
    override def getDouble(columnLabel: String): F[Double] = Sync[F].blocking(resultSet.getDouble(columnLabel))

    override def getDate(columnIndex: Int): F[Date] = Sync[F].blocking(resultSet.getDate(columnIndex))
    override def getDate(columnLabel: String): F[Date] = Sync[F].blocking(resultSet.getDate(columnLabel))

    override def getTime(columnIndex: Int): F[Time] = Sync[F].blocking(resultSet.getTime(columnIndex))
    override def getTime(columnLabel: String): F[Time] = Sync[F].blocking(resultSet.getTime(columnLabel))

    override def getTimestamp(columnIndex: Int): F[Timestamp] = Sync[F].blocking(resultSet.getTimestamp(columnIndex))
    override def getTimestamp(columnLabel: String): F[Timestamp] = Sync[F].blocking(resultSet.getTimestamp(columnLabel))

    override def getAsciiStream(columnIndex: Int): F[InputStream] = Sync[F].blocking(resultSet.getAsciiStream(columnIndex))
    override def getAsciiStream(columnLabel: String): F[InputStream] = Sync[F].blocking(resultSet.getAsciiStream(columnLabel))

    override def getBinaryStream(columnIndex: Int): F[InputStream] = Sync[F].blocking(resultSet.getBinaryStream(columnIndex))
    override def getBinaryStream(columnLabel: String): F[InputStream] = Sync[F].blocking(resultSet.getBinaryStream(columnLabel))

    override def getWarnings(): F[SQLWarning] = Sync[F].blocking(resultSet.getWarnings)
    override def clearWarnings(): F[Unit] = Sync[F].blocking(resultSet.clearWarnings())

    override def getCursorName(): F[String] = Sync[F].blocking(resultSet.getCursorName)

    override def getMetaData(): F[ResultSetMetaData] = Sync[F].blocking(resultSet.getMetaData)

    override def getObject(columnIndex: Int): F[Object] = Sync[F].blocking(resultSet.getObject(columnIndex))
    override def getObject(columnLabel: String): F[Object] = Sync[F].blocking(resultSet.getObject(columnLabel))

    override def findColumn(columnLabel: String): F[Int] = Sync[F].blocking(resultSet.findColumn(columnLabel))

    override def getCharacterStream(columnIndex: Int): F[Reader] = Sync[F].blocking(resultSet.getCharacterStream(columnIndex))
    override def getCharacterStream(columnLabel: String): F[Reader] = Sync[F].blocking(resultSet.getCharacterStream(columnLabel))

    override def getBigDecimal(columnIndex: Int): F[BigDecimal] = Sync[F].blocking(resultSet.getBigDecimal(columnIndex))
    override def getBigDecimal(columnLabel: String): F[BigDecimal] = Sync[F].blocking(resultSet.getBigDecimal(columnLabel))

    override def isBeforeFirst(): F[Boolean] = Sync[F].blocking(resultSet.isBeforeFirst)
    override def isAfterLast(): F[Boolean] = Sync[F].blocking(resultSet.isAfterLast)
    override def isFirst(): F[Boolean] = Sync[F].blocking(resultSet.isFirst)
    override def isLast(): F[Boolean] = Sync[F].blocking(resultSet.isLast)

    override def beforeFirst(): F[Unit] = Sync[F].blocking(resultSet.beforeFirst())
    override def afterLast(): F[Unit] = Sync[F].blocking(resultSet.afterLast())
    override def first(): F[Boolean] = Sync[F].blocking(resultSet.first())
    override def last(): F[Boolean] = Sync[F].blocking(resultSet.last())

    override def getRow(): F[Int] = Sync[F].blocking(resultSet.getRow)

    override def absolute(row: Int): F[Boolean] = Sync[F].blocking(resultSet.absolute(row))
    override def relative(rows: Int): F[Boolean] = Sync[F].blocking(resultSet.relative(rows))
    override def previous(): F[Boolean] = Sync[F].blocking(resultSet.previous())

    override def setFetchDirection(direction: FetchType): F[Unit] = Sync[F].blocking(resultSet.setFetchDirection(direction.int))
    override def getFetchDirection(): F[FetchType] = Sync[F].blocking(FetchType.values(resultSet.getFetchDirection))

    override def setFetchSize(rows: Int): F[Unit] = Sync[F].blocking(resultSet.setFetchSize(rows))
    override def getFetchSize(): F[FetchType] = Sync[F].blocking(FetchType.values(resultSet.getFetchSize))

    override def getType(): F[Type] = Sync[F].blocking(Type.values(resultSet.getType))

    override def getConcurrency(): F[Int] = Sync[F].blocking(resultSet.getConcurrency)

    override def rowUpdated(): F[Boolean] = Sync[F].blocking(resultSet.rowUpdated())
    override def rowInserted(): F[Boolean] = Sync[F].blocking(resultSet.rowInserted())
    override def rowDeleted(): F[Boolean] = Sync[F].blocking(resultSet.rowDeleted())

    override def updateNull(columnIndex: Int): F[Unit] = Sync[F].blocking(resultSet.updateNull(columnIndex))
    override def updateNull(columnLabel: String): F[Unit] = Sync[F].blocking(resultSet.updateNull(columnLabel))

    override def updateBoolean(columnIndex: Int, x: Boolean): F[Unit] = Sync[F].blocking(resultSet.updateBoolean(columnIndex, x))
    override def updateBoolean(columnLabel: String, x: Boolean): F[Unit] = Sync[F].blocking(resultSet.updateBoolean(columnLabel, x))

    override def updateByte(columnIndex: Int, x: Byte): F[Unit] = Sync[F].blocking(resultSet.updateByte(columnIndex, x))
    override def updateByte(columnLabel: String, x: Byte): F[Unit] = Sync[F].blocking(resultSet.updateByte(columnLabel, x))

    override def updateShort(columnIndex: Int, x: Short): F[Unit] = Sync[F].blocking(resultSet.updateShort(columnIndex, x))
    override def updateShort(columnLabel: String, x: Short): F[Unit] = Sync[F].blocking(resultSet.updateShort(columnLabel, x))

    override def updateInt(columnIndex: Int, x: Int): F[Unit] = Sync[F].blocking(resultSet.updateInt(columnIndex, x))
    override def updateInt(columnLabel: String, x: Int): F[Unit] = Sync[F].blocking(resultSet.updateInt(columnLabel, x))

    override def updateLong(columnIndex: Int, x: Long): F[Unit] = Sync[F].blocking(resultSet.updateLong(columnIndex, x))
    override def updateLong(columnLabel: String, x: Long): F[Unit] = Sync[F].blocking(resultSet.updateLong(columnLabel, x))

    override def updateFloat(columnIndex: Int, x: Float): F[Unit] = Sync[F].blocking(resultSet.updateFloat(columnIndex, x))
    override def updateFloat(columnLabel: String, x: Float): F[Unit] = Sync[F].blocking(resultSet.updateFloat(columnLabel, x))

    override def updateDouble(columnIndex: Int, x: Double): F[Unit] = Sync[F].blocking(resultSet.updateDouble(columnIndex, x))
    override def updateDouble(columnLabel: String, x: Double): F[Unit] = Sync[F].blocking(resultSet.updateDouble(columnLabel, x))

    override def updateBigDecimal(columnIndex: Int, x: BigDecimal): F[Unit] = Sync[F].blocking(resultSet.updateBigDecimal(columnIndex, x.bigDecimal))
    override def updateBigDecimal(columnLabel: String, x: BigDecimal): F[Unit] = Sync[F].blocking(resultSet.updateBigDecimal(columnLabel, x.bigDecimal))

    override def updateString(columnIndex: Int, x: String): F[Unit] = Sync[F].blocking(resultSet.updateString(columnIndex, x))
    override def updateString(columnLabel: String, x: String): F[Unit] = Sync[F].blocking(resultSet.updateString(columnLabel, x))

    override def updateBytes(columnIndex: Int, x: Array[Byte]): F[Unit] = Sync[F].blocking(resultSet.updateBytes(columnIndex, x))
    override def updateBytes(columnLabel: String, x: Array[Byte]): F[Unit] = Sync[F].blocking(resultSet.updateBytes(columnLabel, x))

    override def updateDate(columnIndex: Int, x: java.sql.Date): F[Unit] = Sync[F].blocking(resultSet.updateDate(columnIndex, x))
    override def updateDate(columnLabel: String, x: java.sql.Date): F[Unit] = Sync[F].blocking(resultSet.updateDate(columnLabel, x))

    override def updateTime(columnIndex: Int, x: java.sql.Time): F[Unit] = Sync[F].blocking(resultSet.updateTime(columnIndex, x))
    override def updateTime(columnLabel: String, x: java.sql.Time): F[Unit] = Sync[F].blocking(resultSet.updateTime(columnLabel, x))

    override def updateTimestamp(columnIndex: Int, x: java.sql.Timestamp): F[Unit] = Sync[F].blocking(resultSet.updateTimestamp(columnIndex, x))
    override def updateTimestamp(columnLabel: String, x: java.sql.Timestamp): F[Unit] = Sync[F].blocking(resultSet.updateTimestamp(columnLabel, x))

    override def updateAsciiStream(columnIndex: Int, x: java.io.InputStream, length: Int): F[Unit] = Sync[F].blocking(resultSet.updateAsciiStream(columnIndex, x))
    override def updateAsciiStream(columnLabel: String, x: java.io.InputStream, length: Int): F[Unit] = Sync[F].blocking(resultSet.updateAsciiStream(columnLabel, x))

    override def updateBinaryStream(columnIndex: Int, x: java.io.InputStream, length: Int): F[Unit] = Sync[F].blocking(resultSet.updateBinaryStream(columnIndex, x))
    override def updateBinaryStream(columnLabel: String, x: java.io.InputStream, length: Int): F[Unit] = Sync[F].blocking(resultSet.updateBinaryStream(columnLabel, x))

    override def updateCharacterStream(columnIndex: Int, x: java.io.Reader, length: Int): F[Unit] = Sync[F].blocking(resultSet.updateCharacterStream(columnIndex, x))
    override def updateCharacterStream(columnLabel: String, x: java.io.Reader, length: Int): F[Unit] = Sync[F].blocking(resultSet.updateCharacterStream(columnLabel, x))

    override def updateObject(columnIndex: Int, x: Object, scaleOrLength: Int): F[Unit] = Sync[F].blocking(resultSet.updateObject(columnIndex, x, scaleOrLength))
    override def updateObject(columnLabel: String, x: Object, scaleOrLength: Int): F[Unit] = Sync[F].blocking(resultSet.updateObject(columnLabel, x, scaleOrLength))
    override def updateObject(columnIndex: Int, x: Object): F[Unit] = Sync[F].blocking(resultSet.updateObject(columnIndex, x))
    override def updateObject(columnLabel: String, x: Object): F[Unit] = Sync[F].blocking(resultSet.updateObject(columnLabel, x))
