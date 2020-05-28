package com.axway.gw.es.yaml.testutils.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonDiff;

import static com.axway.gw.es.yaml.testutils.diff.ESDiff.JSON_EXPORTER;

public final class Diff {

    final Source source;
    final Target target;
    final DiffType diffType;

    Diff(Source sourceEntityType, Target targetEntityType, DiffType diffType) {
        this.source = sourceEntityType;
        this.target = targetEntityType;
        this.diffType = diffType;
    }

    public DiffType getDiffType() {
        return diffType;
    }

    public Source getSource() {
        return source;
    }

    public Target getTarget() {
        return target;
    }

    public JsonNode getDiff() {
        if (diffType == DiffType.MODIFIED) {
            final JsonNode sourceNode = JSON_EXPORTER.valueToTree(getSource());
            final JsonNode targetNode = JSON_EXPORTER.valueToTree(getTarget());
            return JsonDiff.asJson(sourceNode, targetNode);
        }
        return null;
    }
}
