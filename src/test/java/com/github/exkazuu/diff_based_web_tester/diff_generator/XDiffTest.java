package com.github.exkazuu.diff_based_web_tester.diff_generator;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.tokichie.xml_diff_parser.XmlDiff;
import com.github.tokichie.xml_diff_parser.xdiff.XDiffGenerator;
import org.junit.Test;

import com.google.common.io.Resources;

public class XDiffTest {
  @Test
  public void testUnifiedDiff() throws Exception {
    XDiffGenerator generator = new XDiffGenerator();
    String input1 =
        Resources.toString(Resources.getResource("diff_generator/original.xml"),
            StandardCharsets.UTF_8);
    String input2 =
        Resources.toString(Resources.getResource("diff_generator/modified.xml"),
            StandardCharsets.UTF_8);
    List<XmlDiff> diff = generator.generateDiffContent(input1, input2, System.lineSeparator());
//    String expected =
//        Resources.toString(Resources.getResource("diff_generator/xdiff.diff"),
//            StandardCharsets.UTF_8);
//    equalsIgnoreSpaceAndLinebreaks(expected, diff);

    log(diff);
  }

  private void equalsIgnoreSpaceAndLinebreaks(String expected, String diff) {
    assertEquals(expected.replaceAll("[\\s|\\n|\\r]", ""), diff.replaceAll("[\\s|\\n|\\r]", ""));
  }

  private void log(List<XmlDiff> diffList) {
    for (XmlDiff diff: diffList) {
        System.out.println(diff.getDiffType());
        System.out.println("  " + diff.getOperationTarget());
        System.out.println("    " + diff.getDiffContent());
    }
  }
}
