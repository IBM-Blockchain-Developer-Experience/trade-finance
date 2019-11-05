package org.tradefinance.ledger_api.collections;

import org.parboiled.Rule;
import org.parboiled.BaseParser;
import org.parboiled.annotations.BuildParseTree;

@BuildParseTree
public class BooleanRules extends BaseParser<Object> {
    public Rule Expression() {
        return FirstOf(AnyOf(), AllOf(), OR(), AND());
    }

    public Rule AnyOf() {
        return Sequence("AnyOf(", MultiItem(), ")");
    }

    public Rule AllOf() {
        return Sequence("AllOf(", MultiItem(), ")");
    }

    public Rule OR() {
        return Sequence("OR(", ComparisonItem(), Sequence(",", ZeroOrMore(" ")), ComparisonItem(), ")");
    }

    public Rule AND() {
        return Sequence("AND(", ComparisonItem(), Sequence(",", ZeroOrMore(" ")), ComparisonItem(), ")");
    }

    public Rule MultiItem() {
        return Sequence(ComparisonItem(), ZeroOrMore(Sequence(",", ZeroOrMore(" "), ComparisonItem())));
    }

    public Rule ComparisonItem() {
        return FirstOf(OR(), AND(), AllOf(), AnyOf(), QuotedString());
    }

    public Rule QuotedString() {
        return Sequence("'", OneOrMore(NoneOf("'")), "'");
    }
}
