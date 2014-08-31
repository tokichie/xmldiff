package com.github.tokichie.xml_diff_parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tokitake on 2014/08/28.
 */
public class XmlDiff {
    private String diffType;
    private String operationTarget;
    private String diffContent;

    public XmlDiff(String type, String opTarget, String content) {
        diffType = type;
        operationTarget = opTarget;
        diffContent = content;
    }

    public String getDiffType() { return diffType; }
    public String getOperationTarget() { return operationTarget; }
    public String getDiffContent() { return diffContent; }
}
