package bleibinhaus.mockitoscalaexample

import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.Mockito.when
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.mock.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Gen

class FunctionProvider {
  // Int ⇒ Int == Function1[Int, Int]
  def addingFunction(addend: Int): Int ⇒ Int =
    (i: Int) ⇒ i + addend

  // (Int, String) ⇒ Seq[String] == Function2[Int, String, Seq[String]]
  def textNTimesFunction: (Int, String) ⇒ Seq[String] =
    (times: Int, text: String) ⇒ 1 to times map { _ ⇒ text }

  def applies23ToFunction(intFunction: Int ⇒ Int): Int =
    intFunction(23)

  def sumCurried(addend1: Int)(addend2: Int): Int =
    addend1 + addend2

  // Int ⇒ String ⇒ T == Function1[Int, Function1[String, T]]
  def textNTimesAsXCurried[T](
    stringSeqTransformer: Seq[String] ⇒ T = (seq: Seq[String]) ⇒ seq): Int ⇒ String ⇒ T =
    (times: Int) ⇒ (text: String) ⇒ stringSeqTransformer(textNTimesFunction(times, text))
}

class FunctionUser(val functionProvider: FunctionProvider) {
  def do1And1 = functionProvider.addingFunction(1)(1)

  def batman = functionProvider.textNTimesFunction(16, "na") :+ "batman"

  def answer = {
    val addToNineteenFunction: Int ⇒ Int = functionProvider.sumCurried(19) _
    functionProvider.applies23ToFunction(addToNineteenFunction)
  }

  def ballmer = {
    val textNTimesAsStringCurried = functionProvider.textNTimesAsXCurried {
      (seq: Seq[String]) ⇒ seq.mkString(", ")
    }
    val text14TimesAsString = textNTimesAsStringCurried(14)
    text14TimesAsString("developers")
  }
}

class FunctionsTest extends FunSpec with ShouldMatchers with MockitoSugar with PropertyChecks {
  describe("The function provider") {
    it("has a method which returns a function which adds a number to a predefined number") {
      val functionProvider = new FunctionProvider
      val addingFunction = functionProvider.addingFunction(23)
      forAll { (n: Int) ⇒
        addingFunction(n) should be(23 + n)
      }
    }

    it("has a method which returns a function which multiplies a text n times") {
      val functionProvider = new FunctionProvider
      val textNTimesFunction = functionProvider.textNTimesFunction
      textNTimesFunction(2, "text") should be(Seq("text", "text"))
    }
  }

  describe("The function user") {
    it("can do 1&1") {
      val functionProviderMock = mock[FunctionProvider]
      when(functionProviderMock.addingFunction(1)).thenReturn((i: Int) ⇒ i + 1)
      val functionUser = new FunctionUser(functionProviderMock)
      functionUser.do1And1 should be(2)
      verify(functionProviderMock).addingFunction(1)
    }

    it("knows about batman") {
      val functionProviderMock = mock[FunctionProvider]
      val textNTimesFunctionMock = mock[Function2[Int, String, Seq[String]]]
      when(functionProviderMock.textNTimesFunction).thenReturn(textNTimesFunctionMock)
      when(textNTimesFunctionMock.apply(16, "na")).thenReturn(1 to 16 map { _ ⇒ "na" })

      val functionUser = new FunctionUser(functionProviderMock)
      functionUser.batman should be(
        (1 to 16 map { _ ⇒ "na" }) :+ "batman"
      )

      verify(functionProviderMock).textNTimesFunction
      verify(textNTimesFunctionMock).apply(16, "na")
    }

    it("can calculate the answer (just with mocks)") {
      val functionProviderMock = mock[FunctionProvider]
      when(functionProviderMock.applies23ToFunction(any[Function1[Int, Int]].apply)).thenReturn(42)

      val functionUser = new FunctionUser(functionProviderMock)
      functionUser.answer should be(42)

      val nineteenFunctionCaptor = ArgumentCaptor.forClass(classOf[Function1[Int, Int]])
      verify(functionProviderMock).applies23ToFunction(nineteenFunctionCaptor.capture.apply)
      when(functionProviderMock.sumCurried(19)(1)).thenReturn(20)
      nineteenFunctionCaptor.getValue.apply(1) should be(20)
      verify(functionProviderMock).sumCurried(19)(1)
    }

    it("can calculate the answer (with a spy)") {
      val functionProvider = new FunctionProvider
      val functionProviderSpy = spy(functionProvider)
      doReturn(42).when(functionProviderSpy).sumCurried(19)(23)

      val functionUser = new FunctionUser(functionProviderSpy)
      functionUser.answer should be(42)

      verify(functionProviderSpy).sumCurried(19)(23)
      val addToNineteenFunctionCaptor = ArgumentCaptor.forClass(classOf[Function1[Int, Int]])
      verify(functionProviderSpy).applies23ToFunction(addToNineteenFunctionCaptor.capture.apply)
      forAll { (n: Int) ⇒
        addToNineteenFunctionCaptor.getValue.apply(n) should be(19 + n)
      }
    }

    it("likes developers") {
      val functionProviderMock = mock[FunctionProvider]
      when(functionProviderMock.textNTimesAsXCurried(any[Function1[Seq[String], String]].apply)).thenReturn(
        (i: Int) ⇒ (str: String) ⇒ 1 to 14 map { _ ⇒ "developers" } mkString ", "
      )

      val functionUser = new FunctionUser(functionProviderMock)
      functionUser.ballmer should be(
        "developers, developers, developers, developers, developers, developers, developers, " +
          "developers, developers, developers, developers, developers, developers, developers"
      )

      val stringSeqToStringFunctionCaptor = ArgumentCaptor.forClass(classOf[Function1[Seq[String], String]])
      verify(functionProviderMock).textNTimesAsXCurried(stringSeqToStringFunctionCaptor.capture.apply)
      val stringSeqToStringFunction = stringSeqToStringFunctionCaptor.getValue
      val stringListGenerator = Gen.listOf(Gen.alphaStr)
      forAll(stringListGenerator) { (stringLists: List[String]) ⇒
        stringSeqToStringFunction(stringLists) should be(stringLists.mkString(", "))
      }
    }
  }
}