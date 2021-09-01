import scala.concurrent.Future

object Completions:

  // The argument "magnet" type
  enum CompletionArg:
    case Error(s: String)
    case Response(f: Future[String])
    case Status(code: Future[String])

  object CompletionArg:

    // conversions defining the possible arguments to pass to `complete`
    // these always come with CompletionArg
    // They can be invoked explicitly, e.g.
    //
    //   CompletionArg.fromStatusCode(statusCode)

    given fromString:     Conversion[String,         CompletionArg] = Error(_)
    given fromFuture:     Conversion[Future[String], CompletionArg] = Response(_)
    given fromStatusCode: Conversion[Future[String], CompletionArg] = Status(_)
  end CompletionArg
  import CompletionArg.*

  def complete[T](arg: CompletionArg) = arg match
    case Error(s)     => s
    case Response(f)  => f
    case Status(code) => code

end Completions