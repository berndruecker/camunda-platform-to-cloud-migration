package org.camunda.community.converter.visitor.impl;

import org.camunda.community.converter.BpmnDiagramCheckResult.Severity;
import org.camunda.community.converter.DomElementVisitorContext;
import org.camunda.community.converter.NamespaceUri;
import org.camunda.community.converter.convertible.AbstractDataMapperConvertible;
import org.camunda.community.converter.message.MessageFactory;
import org.camunda.community.converter.visitor.AbstractElementVisitor;

public class ScriptVisitor extends AbstractElementVisitor {
  @Override
  protected String namespaceUri() {
    return NamespaceUri.BPMN;
  }

  @Override
  public String localName() {
    return "script";
  }

  @Override
  protected void visitFilteredElement(DomElementVisitorContext context) {
    String script = context.getElement().getTextContent();
    context.addConversion(
        AbstractDataMapperConvertible.class,
        convertible ->
            convertible.addZeebeTaskHeader(context.getProperties().getScriptHeader(), script));
    context.addMessage(Severity.TASK, MessageFactory.script());
  }
}
