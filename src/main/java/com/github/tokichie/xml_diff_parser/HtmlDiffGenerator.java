package com.github.tokichie.xml_diff_parser;

import java.util.List;public abstract class HtmlDiffGenerator {
  public abstract List<XmlDiff> generateDiffContent(String input1, String input2, String lineSeparator);

  public List<XmlDiff> generateDiffContent(String input1, String input2) {
    return generateDiffContent(input1, input2, System.lineSeparator());
  }
}
