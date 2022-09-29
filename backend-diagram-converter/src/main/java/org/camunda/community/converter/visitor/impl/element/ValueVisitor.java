package org.camunda.community.converter.visitor.impl.element;

import org.camunda.community.converter.DomElementVisitorContext;
import org.camunda.community.converter.visitor.AbstractCamundaElementVisitor;

public class ValueVisitor extends AbstractCamundaElementVisitor {
  @Override
  public String localName() {
    return "value";
  }

  @Override
  protected String visitCamundaElement(DomElementVisitorContext context) {
    return null;
  }

  @Override
  public boolean canBeTransformed(DomElementVisitorContext context) {
    return false;
  }

  @Override
  protected boolean isSilent() {
    return true;
  }
}
