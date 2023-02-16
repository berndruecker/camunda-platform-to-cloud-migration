package org.camunda.community.migration.converter;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.camunda.community.migration.converter.expression.ExpressionTransformationResult;
import org.camunda.community.migration.converter.expression.ExpressionTransformer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ExpressionTransformerTest {

  private static ExpressionTestDataSet test(String expression, String expectedResult) {
    ExpressionTestDataSet set = new ExpressionTestDataSet();
    set.expectedResult = expectedResult;
    set.expression = expression;
    return set;
  }

  @TestFactory
  public Stream<DynamicTest> shouldResolveExpression() {
    return DynamicTest.stream(
        Stream.of(
            test("${someVariable}", "=someVariable"),
            test("someStaticValue", "someStaticValue"),
            test("${var.innerField}", "=var.innerField"),
            test("hello-${World}", "=\"hello-\" + World"),
            test("#{x}", "=x"),
            test("${x}", "=x"),
            test("#{x>5}", "=x>5"),
            test("#{x gt 5}", "=x > 5"),
            test("#{x < 5}", "=x < 5"),
            test("#{x lt 5}", "=x < 5"),
            test("#{x==5}", "=x=5"),
            test("#{x!=5}", "=x!=5"),
            test("#{x eq 5}", "=x = 5"),
            test("#{x ne 5}", "=x != 5"),
            test("#{x == \"test\"}", "=x = \"test\""),
            test("#{true}", "=true"),
            test("${false}", "=false"),
            test("#{!x}", "=not(x)"),
            test("${!x and y}", "=not(x) and y"),
            test("${!(x and y)}", "=not(x and y)"),
            test("#{!true}", "=not(true)"),
            test("#{not(x>5)}", "=not(x>5)"),
            test("${not x}", "=not(x)"),
            test("#{x && y}", "=x and y"),
            test("#{x and y}", "=x and y"),
            test("#{x || y}", "=x or y"),
            test("#{x or y}", "=x or y"),
            test("#{customer.name}", "=customer.name"),
            test("#{customer.address[\"street\"]}", "=customer.address.street"),
            test("#{customer.orders[1]}", "=customer.orders[2]"),
            test("${not empty x}", "=not(x=null)"),
            test("${empty donut}", "=donut=null"),
            test("${!empty donut}", "=not(donut=null)"),
            test("${empty donut || coffee}", "=donut=null or coffee"),
            test("${not empty donut || coffee}", "=not(donut=null) or coffee"),
            test("${not(empty donut || coffee)}", "=not(donut=null or coffee)")),
        ExpressionTestDataSet::toString,
        this::testExpression);
  }

  @ParameterizedTest
  @CsvSource(
      value = {"${execution.getVariable(\"a\")}, true", "myexecutionContext.isSpecial(), false"})
  public void testExecution(String expression, Boolean expected) {
    assertThat(ExpressionTransformer.transform(expression).hasExecution()).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource(
      value = {
        "var.getSomething(),true",
        "${!dauerbuchungVoat21Ids.isEmpty()}, true",
        "${!dauerbuchungVoat21Ids.contains(\"someText\")}, true",
        "input > 5.5, false"
      })
  public void testMethodInvocation(String expression, Boolean expected) {
    assertThat(ExpressionTransformer.transform(expression).hasMethodInvocation())
        .isEqualTo(expected);
  }

  private void testExpression(ExpressionTestDataSet test) {
    ExpressionTransformationResult transformationResult =
        ExpressionTransformer.transform(test.expression);
    assertEquals(test.expectedResult, transformationResult.getNewExpression());
  }

  public static class ExpressionTestDataSet {
    String expression;
    String expectedResult;

    @Override
    public String toString() {
      return expression + " => " + expectedResult;
    }
  }
}

//
//    expect(convertJuel("#{customer.orders[1]}").feelExpression).toBe("=customer.orders[2]");
//    });
//
//
//    notest("JUEL Tutorial", () => {
//    // Other elements valid in JUEL (taken from
// https://docs.oracle.com/javaee/5/tutorial/doc/bnahq.html)
//    // Still need to be translated and implemented
//    expect(convertJuel("${1 > (4/2)}").feelExpression).toBe("=xxx");
//    expect(convertJuel("${4.0 >= 3}").feelExpression).toBe("=xxx");
//    expect(convertJuel("${100.0 == 100}").feelExpression).toBe("=xxx");
//    expect(convertJuel("${(10*10) ne 100}").feelExpression).toBe("=xxx");
//    expect(convertJuel("${'a' < 'b'}").feelExpression).toBe("=xxx");
//    expect(convertJuel("${'hip' gt 'hit'}").feelExpression).toBe("=xxx");
//    expect(convertJuel("${4 > 3}").feelExpression).toBe("=xxx");
//    expect(convertJuel("${1.2E4 + 1.4}").feelExpression).toBe("=xxx");
//    expect(convertJuel("").feelExpression).toBe("=xxx");
//    expect(convertJuel("").feelExpression).toBe("=xxx");
//    expect(convertJuel("").feelExpression).toBe("=xxx");
//    });
