/*
 * Copyright 2016 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.query.api;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import stroom.util.shared.HasDisplayValue;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@JsonPropertyOrder({"op", "children"})
@XmlType(name = "ExpressionOperator", propOrder = {"op", "children"})
public class ExpressionOperator extends ExpressionItem {
    private static final long serialVersionUID = 6602004424564268512L;

    private Op op = Op.AND;
    private ExpressionItem[] children;

    public ExpressionOperator() {
    }

    public ExpressionOperator(final Boolean enabled, final Op op, final ExpressionItem[] children) {
        super(enabled);
        this.op = op;
        this.children = children;
    }

    @XmlElement(name = "op")
    public Op getOp() {
        return op;
    }

    public void setOp(final Op op) {
        this.op = op;
    }

    @XmlElementWrapper(name = "children")
    @XmlElements({
            @XmlElement(name = "operator", type = ExpressionOperator.class),
            @XmlElement(name = "term", type = ExpressionTerm.class)
    })
    public ExpressionItem[] getChildren() {
        return children;
    }

    public void setChildren(final ExpressionItem[] children) {
        this.children = children;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        final ExpressionOperator that = (ExpressionOperator) o;

        if (op != that.op) return false;
        return children != null ? children.equals(that.children) : that.children == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (op != null ? op.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }

    @Override
    public void append(final StringBuilder sb, final String pad, final boolean singleLine) {
        if (enabled()) {
            if (!singleLine && sb.length() > 0) {
                sb.append("\n");
                sb.append(pad);
            }

            sb.append(op);

            if (singleLine) {
                sb.append(" {");
            }

            if (children != null) {
                final String padding = pad + "  ";
                for (final ExpressionItem expressionItem : children) {
                    if (expressionItem.enabled()) {
                        if (!singleLine) {
                            sb.append("\n");
                            sb.append(pad);
                        }

                        expressionItem.append(sb, padding, singleLine);

                        if (singleLine) {
                            sb.append(", ");
                        }
                    }
                }

                if (singleLine) {
                    sb.setLength(sb.length() - ", ".length());
                }
            }

            if (singleLine) {
                sb.append("} ");
            }
        }
    }

    public enum Op implements HasDisplayValue {
        AND("AND"), OR("OR"), NOT("NOT");

        private final String displayValue;

        Op(final String displayValue) {
            this.displayValue = displayValue;
        }

        @Override
        public String getDisplayValue() {
            return displayValue;
        }
    }
}
