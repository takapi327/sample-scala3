import scala.language.strictEquality

case class Cat(name: String)
case class Dog(name: String)
val dog = Dog("Fido")
val cat = Cat("Morris")

case class DogDerives(name: String) derives CanEqual

given CanEqual[Dog, Dog] = CanEqual.derived

trait Book:
  def author: String
  def title: String
  def year: Int

case class PrintedBook(
  author: String,
  title:  String,
  year:   Int,
  pages:  Int
) extends Book

case class AudioBook(
  author:          String,
  title:           String,
  year:            Int,
  lengthInMinutes: Int
) extends Book:
  override def equals(that: Any): Boolean = that match
    case a: AudioBook =>
      if this.author == a.author
        && this.title == a.title
      && this.year == a.year
      && this.lengthInMinutes == a.lengthInMinutes
      then true else false
    case p: PrintedBook =>
      if this.author == p.author && this.title == p.title
      then true else false
    case _ =>
      false

given CanEqual[PrintedBook, PrintedBook] = CanEqual.derived
given CanEqual[AudioBook, AudioBook] = CanEqual.derived

val p1 = PrintedBook("1984", "George Orwell", 1961, 328)
val p2 = PrintedBook("1984", "George Orwell", 1961, 328)

val pBook = PrintedBook("1984", "George Orwell", 1961, 328)
val aBook = AudioBook("1984", "George Orwell", 2006, 682)

given CanEqual[PrintedBook, AudioBook] = CanEqual.derived
given CanEqual[AudioBook, PrintedBook] = CanEqual.derived