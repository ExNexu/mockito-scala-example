package bleibinhaus.mockitoscalaexample

import scala.util.control.NoStackTrace

import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar

case class IntHolder(number: Int)

case class MyOwnException(val intHolder: IntHolder) extends RuntimeException with NoStackTrace

object ExceptionObject {
  def methodWithRuntimeException() =
    throw new RuntimeException("Error message!") with NoStackTrace

  def methodWithMyOwnException() =
    throw MyOwnException(IntHolder(42))
}

trait CleanTrait {
  def goodMethod = 42
}

class ExceptionTest extends FunSpec with ShouldMatchers with MockitoSugar {

  describe("The exception object") {
    it("should throw a runtime exception with an error message when calling 'methodWithRuntimeException'") {
      val exception = evaluating {
        ExceptionObject.methodWithRuntimeException
      } should produce[RuntimeException]
      exception.getMessage should be("Error message!")
    }

    it("should throw my own exception with an Int holder when calling 'methodWithMyOwnException'") {
      val exception = evaluating {
        ExceptionObject.methodWithMyOwnException
      } should produce[MyOwnException]
      val intHolder = exception.intHolder
      intHolder.number should be(42)
    }
  }

  describe("The clean trait") {
    it("should not throw any exceptions by default") {
      val cleanTrait = new CleanTrait {}
      cleanTrait.goodMethod should be(42)
    }

    it("should throw an exception after mocking it and telling the mock to throw one") {
      val cleanTrait = mock[CleanTrait]
      when(cleanTrait.goodMethod).thenThrow(new RuntimeException)
      evaluating {
        cleanTrait.goodMethod
      } should produce[RuntimeException]
      verify(cleanTrait).goodMethod
    }
  }

}