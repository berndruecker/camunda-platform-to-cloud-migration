package org.camunda.community.converter.convertible;

public class ServiceTaskConvertible extends AbstractActivityConvertible {
  private final ZeebeTaskDefinition zeebeTaskDefinition = new ZeebeTaskDefinition();

  public ZeebeTaskDefinition getZeebeTaskDefinition() {
    return zeebeTaskDefinition;
  }

  public static class ZeebeTaskDefinition {
    private String type;

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }
}
