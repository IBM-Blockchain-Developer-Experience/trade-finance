package org.tradefinance.ledger_api.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

enum Operators {
    OR,
    AND
}

public class BooleanRulesHandler {

    // TODO MAKE THIS ALL STATIC AS SEEMS THAT WOULD BE BETTER BUT AS LIAM

    private List<Node<BooleanRules>> tree;
    private List<String> values;
    private String input;

    public BooleanRulesHandler(String rules) {
        this.setup(rules, new String[] {});
    }

    public BooleanRulesHandler(String rules, String[] values) {
        this.setup(rules, values);
    }

    private void setup(String rules, String[] values) {
        BooleanRules parser = Parboiled.createParser(BooleanRules.class);

        ParsingResult<BooleanRules> result = new ReportingParseRunner<BooleanRules>(parser.Expression()).run(rules);

        if (!result.matched) {
            throw new RuntimeException("Collection rules invalid");
        }

        this.tree = result.parseTreeRoot.getChildren();
        this.input = rules;
        this.values = Arrays.asList(values);
    }

    public Boolean evaluate() {
        return this.evaluate(this.tree.get(0));
    }

    private Boolean evaluate(Node<BooleanRules> node) {
        switch (node.getLabel()) {
            case "AnyOf": return this.AnyOfHandler(node);
            case "AllOf": return this.AllOfHandler(node);
            case "OR": return this.ORHandler(node);
            case "AND": return this.ANDHandler(node);
            case "ComparisonItem": return this.comparisonItemHandler(node);
            case "QuotedString": return this.collectionFound(node);
            default: throw new RuntimeException("Invalid rule label: " + node.getLabel());
        }
    }

    public String[] getEntries() {
        ArrayList<String> arr = new ArrayList<String>();

        this.getEntries(this.tree.get(0), arr);

        return arr.toArray(new String[arr.size()]);
    }

    public void getEntries(Node<BooleanRules> node, ArrayList<String> arr) {
        for (Node<BooleanRules> child : node.getChildren()) {
            if (child.getLabel() == "QuotedString") {
                arr.add(this.parseQuotedString(node));
            } else {
                this.getEntries(child, arr);
            }
        }
    }

    private String parseQuotedString(Node<BooleanRules> node) {
        return this.input.substring(node.getStartIndex() + 1, node.getEndIndex() - 1);
    }

    private Boolean collectionFound(Node<BooleanRules> node) {
        final String collection = this.parseQuotedString(node);

        return collection.equals("*") || values.contains(collection);
    }

    private Boolean comparisonItemHandler(Node<BooleanRules> node) {
        return this.evaluate(node.getChildren().get(0));
    }

    private Boolean multiItemHandler(Node<BooleanRules> node, Operators op) {
        final Node<BooleanRules> multiItem = node.getChildren().get(1);

        final List<Node<BooleanRules>> multiItemChildren = multiItem.getChildren();
        final Node<BooleanRules> firstComparisonItem = multiItemChildren.get(0);
        final List<Node<BooleanRules>> otherComparisonItems = multiItemChildren.get(1).getChildren();

        Boolean result = this.evaluate(firstComparisonItem);

        for (Node<BooleanRules> sequence: otherComparisonItems) {
            Node<BooleanRules> comparisonItem = sequence.getChildren().get(2);

            switch (op) {
                case OR: result = result || this.evaluate(comparisonItem); break;
                case AND: result = result && this.evaluate(comparisonItem); break;
            }
        }

        return result;
    }

    private Boolean AnyOfHandler(Node<BooleanRules> node) {
        return this.multiItemHandler(node, Operators.OR);
    }

    private Boolean AllOfHandler(Node<BooleanRules> node) {
        return this.multiItemHandler(node, Operators.AND);
    }

    private Boolean ORHandler(Node<BooleanRules> node) {
        final List<Node<BooleanRules>> children = node.getChildren();

        return this.evaluate(children.get(1)) || this.evaluate(children.get(3));
    }

    private Boolean ANDHandler(Node<BooleanRules> node) {
        final List<Node<BooleanRules>> children = node.getChildren();

        return this.evaluate(children.get(1)) && this.evaluate(children.get(3));
    }
}
