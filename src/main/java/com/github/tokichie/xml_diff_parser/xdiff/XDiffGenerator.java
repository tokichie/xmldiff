package com.github.tokichie.xml_diff_parser.xdiff;

import com.github.tokichie.xml_diff_parser.HtmlDiffGenerator;
import com.github.exkazuu.diff_based_web_tester.diff_generator.HtmlFormatter;
import com.github.exkazuu.diff_based_web_tester.diff_generator.LogFiles;
import com.github.tokichie.xml_diff_parser.XmlDiff;import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;import java.util.List;import java.util.Stack;

public class XDiffGenerator extends HtmlDiffGenerator {

  @Override
  public List<XmlDiff> generateDiffContent(String input1, String input2, String lineSeparator) {
    String diff = applyXdiff(input1, input2, lineSeparator);
    DOMParser parser = new DOMParser();
    try {
      LogFiles.writeLogFile("_xdiff.xml", diff);
      parser.parse(new InputSource(new StringReader(diff)));
      Document document = parser.getDocument();
      InstructionNodeExtractor extractor = new InstructionNodeExtractor();
      extractor.search(document.getDocumentElement());
      return extractor.result();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String applyXdiff(String input1, String input2, String lineSeparator) {
    String formatted1 = HtmlFormatter.format(input1);
    String formatted2 = HtmlFormatter.format(input2);
    StringWriter writer = new StringWriter();
    new XDiff(formatted1, formatted2, writer);
    return writer.toString();
  }

  private class InstructionNodeExtractor {
    private Stack<Node> stack = new Stack<>();
    private List<XmlDiff> xmlDiffList = new ArrayList<XmlDiff>();

    private void search(Node node) {
      stack.push(node);
      while (!stack.isEmpty()) {
        node = stack.pop();
        if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
          ProcessingInstruction instruction = (ProcessingInstruction) node;
          XmlDiff xmlDiff = instructionToString(instruction);
          if (xmlDiff != null) {
            xmlDiffList.add(xmlDiff);
          }
        }

        NodeList children = node.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
          stack.push(children.item(i));
        }
      }
    }

    private XmlDiff instructionToString(ProcessingInstruction instruction) {
      Element parent = parentElement(instruction);
      String type = instruction.getTarget().toUpperCase();
      parent.removeChild(instruction);
      String operationTarget = instruction.getData().split("\\s")[0];

      switch (type) {
        case "INSERT":
        case "DELETE":
          if (operationTarget.equalsIgnoreCase(parent.getTagName())) {
            return new XmlDiff(type, null,
                elementToString(parent).replace(System.lineSeparator(), ""));
          } else {
            return new XmlDiff(type, operationTarget,
                parent.getAttribute(operationTarget).replace(System.lineSeparator(), ""));
          }
        case "UPDATE":
          String fromData = instruction.getData().split("\\s", 2)[1];
          if (operationTarget.equals("FROM")) {
            String content = "{\"oldValue\":\"" + fromData.replace("\"", "\\\"") + "\", \"newValue\":\""
                + elementToString(parent).replace("\"", "\\\"") + "\"}";
            return new XmlDiff(type, null, content.replace(System.lineSeparator(), ""));
          } else {
            String content = "{\"oldValue\":\"" + fromData.replace("\"", "\\\"") + "\", \"newValue\":\""
                + parent.getAttribute(operationTarget).replace("\"", "\\\"") + "\"}";
            return new XmlDiff(type, operationTarget, content.replace(System.lineSeparator(), ""));
          }
        default:
          return null;
      }
    }

    private String elementToString(Element element) {
      StringBuilder builder = new StringBuilder();
      buildNodeString(element, builder);
      return builder.toString();
    }

    private void buildNodeString(Node node, StringBuilder builder) {
      if (node.getNodeType() == Node.TEXT_NODE) {
        builder.append(node.getNodeValue());
      } else if (node.getNodeType() == Node.ELEMENT_NODE) {
        builder.append("<").append(node.getNodeName()).append(">");
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
          buildNodeString(children.item(i), builder);
        }
        builder.append("</").append(node.getNodeName()).append(">");
      }
    }

    private Element parentElement(Node node) {
      while (!(node instanceof Element)) {
        node = node.getParentNode();
      }
      return (Element) node;
    }

    public List<XmlDiff> result() {
      return xmlDiffList;
    }
  }
}
