package com.github.exkazuu.diff_based_web_tester.diff_generator.xdiff;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Stack;

import org.cyberneko.html.parsers.DOMParser;
import org.jsoup.helper.StringUtil;import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.exkazuu.diff_based_web_tester.diff_generator.LogFiles;
import com.github.exkazuu.diff_based_web_tester.diff_generator.HtmlDiffGenerator;
import com.github.exkazuu.diff_based_web_tester.diff_generator.HtmlFormatter;

public class XDiffGenerator extends HtmlDiffGenerator {

  @Override
  public String generateDiffContent(String input1, String input2, String lineSeparator) {
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
    private StringBuilder builder = new StringBuilder();

    private void search(Node node) {
      stack.push(node);
      while (!stack.isEmpty()) {
        node = stack.pop();
        if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
          ProcessingInstruction instruction = (ProcessingInstruction) node;
          String line = instructionToString(instruction);
          if (line != null) {
            builder.append(line).append(System.lineSeparator());
          }
        }

        NodeList children = node.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
          stack.push(children.item(i));
        }
      }
    }

    private String instructionToString(ProcessingInstruction instruction) {
      Element parent = parentElement(instruction);
      String type = instruction.getTarget().toUpperCase();
      parent.removeChild(instruction);
      String operationTarget = instruction.getData().split("\\s")[0];
      switch (type) {
        case "INSERT":
        case "DELETE":
          if (operationTarget.equalsIgnoreCase(parent.getTagName())) {
            return type + ":" + elementToString(parent).replace(System.lineSeparator(), "");
          } else {
            System.out.println(operationTarget.toString());
            return type + " " + operationTarget + ":" + parent.getAttribute(operationTarget);
          }
        case "UPDATE":
          String fromData = instruction.getData().split("\\s", 2)[1];
          if (operationTarget.equals("FROM")) {
            return type + ":[oldValue=" + fromData + ", newValue="
                + elementToString(parent).replace(System.lineSeparator(), "") + "]";
          } else {
            System.out.println(operationTarget.toString());
            return type + " " + operationTarget + ":[oldValue=" + fromData + ", newValue="
                + parent.getAttribute(operationTarget) + "]";
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

    public String result() {
      return builder.toString();
    }
  }
}
