package org.camunda.community.converter;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.camunda.bpm.model.xml.instance.DomElement;
import org.camunda.community.converter.BpmnDiagramCheckResult.BpmnElementCheckMessage;
import org.camunda.community.converter.BpmnDiagramCheckResult.BpmnElementCheckResult;
import org.camunda.community.converter.BpmnDiagramCheckResult.Severity;
import org.camunda.community.converter.convertible.Convertible;
import org.camunda.community.converter.message.Message;

public interface DomElementVisitorContext {
  /**
   * Returns the currently handled element
   *
   * @return the currently handled element
   */
  DomElement getElement();

  /** Sets the currently handled element to remove */
  void addElementToRemove();

  /**
   * Sets the attribute on the currently handled element to remove
   *
   * @param attributeLocalName the local name of the attribute
   * @param namespaceUri the namespace uri of the attribute
   */
  void addAttributeToRemove(String attributeLocalName, String namespaceUri);

  /**
   * Adds a message to the BPMN process element of the currently handled element
   *
   * @param severity severity of the message
   * @param message the message to display
   */
  void addMessage(Severity severity, Message message);

  /**
   * Sets the currently handled element as BPMN process element. This element can now hold messages
   * and ${@link Convertible} objects in its context
   */
  void setAsBpmnProcessElement(Convertible convertible);

  /**
   * Adds a conversion to the BPMN process element of the currently handled element
   *
   * @param convertibleType type of the convertible (might be abstract)
   * @param operation operation to apply to the type
   * @param <T> type of the convertible (controlled by convertibleType)
   */
  <T extends Convertible> void addConversion(Class<T> convertibleType, Consumer<T> operation);

  /**
   * Notifies external units of something that happened. This will not appear in the result.
   *
   * @param object the object to notify about
   */
  void notify(Object object);

  /**
   * Returns the converter properties for the current conversion.
   *
   * @return the converter properties for the current conversion
   */
  ConverterProperties getProperties();

  class DefaultDomElementVisitorContext implements DomElementVisitorContext {
    private final DomElement element;
    private final BpmnDiagramCheckContext context;
    private final BpmnDiagramCheckResult result;
    private final NotificationService notificationService;
    private final ConverterProperties converterProperties;

    public DefaultDomElementVisitorContext(
        DomElement element,
        BpmnDiagramCheckContext context,
        BpmnDiagramCheckResult result,
        NotificationService notificationService,
        ConverterProperties converterProperties) {
      this.element = element;
      this.context = context;
      this.result = result;
      this.notificationService = notificationService;
      this.converterProperties = converterProperties;
    }

    @Override
    public DomElement getElement() {
      return element;
    }

    @Override
    public void addElementToRemove() {
      context.getElementsToRemove().add(element);
    }

    @Override
    public void addAttributeToRemove(String attributeLocalName, String namespaceUri) {
      context.addAttributeToRemove(element, namespaceUri, attributeLocalName);
    }

    @Override
    public void addMessage(Severity severity, Message message) {
      addMessage(element, severity, message);
    }

    @Override
    public void setAsBpmnProcessElement(Convertible convertible) {
      createBpmnElementCheckResult(element, convertible);
    }

    @Override
    public <T extends Convertible> void addConversion(
        Class<T> convertibleType, Consumer<T> operation) {
      addConversion(element, convertibleType, operation);
    }

    @Override
    public void notify(Object object) {
      notificationService.notify(object);
    }

    @Override
    public ConverterProperties getProperties() {
      return converterProperties;
    }

    private void addMessage(DomElement element, Severity severity, Message message) {
      findElementMessages(element).add(createMessage(severity, message));
    }

    private BpmnElementCheckMessage createMessage(Severity severity, Message message) {
      BpmnElementCheckMessage m = new BpmnElementCheckMessage();
      m.setMessage(message.getMessage());
      m.setSeverity(severity);
      m.setLink(message.getLink());
      return m;
    }

    private List<BpmnElementCheckMessage> findElementMessages(DomElement element) {
      return findBpmnElementCheckResult(element).getMessages();
    }

    private <T extends Convertible> void addConversion(
        DomElement element, Class<T> type, Consumer<T> modifier) {
      T conversion = findConvertible(element, type);
      modifier.accept(conversion);
    }

    private <T extends Convertible> T findConvertible(DomElement element, Class<T> type) {
      if (context.getConvertibles().containsKey(element)) {
        return type.cast(context.getConvertibles().get(element));
      } else {
        return findConvertible(element.getParentElement(), type);
      }
    }

    private BpmnElementCheckResult findBpmnElementCheckResult(DomElement element) {
      return result.getResults().stream()
          .filter(r -> r.getElementId().equals(extractId(element)))
          .findFirst()
          .orElseGet(() -> findBpmnElementCheckResult(element.getParentElement()));
    }

    private String extractId(DomElement element) {
      return element.getAttribute("id");
    }

    private void createBpmnElementCheckResult(DomElement element, Convertible convertible) {
      String id = extractId(element);
      Objects.requireNonNull(id);
      if (containsId(id)) {
        throw new IllegalStateException(
            "Element with id '" + id + "' is already contained in list of results");
      }
      BpmnElementCheckResult result = new BpmnElementCheckResult();
      context.addConvertible(element, convertible);
      result.setElementId(id);
      result.setElementName(element.getAttribute("name"));
      result.setElementType(element.getLocalName());
      this.result.getResults().add(result);
    }

    private boolean containsId(String id) {
      return result.getResults().stream().anyMatch(r -> r.getElementId().equals(id));
    }
  }
}
