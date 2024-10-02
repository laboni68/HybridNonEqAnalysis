package differencing.domain;

import java.util.HashMap;
import java.util.Map;

public enum Operator implements Model {

    EQ("==", " == ", " = "),
    NE("!=", " != "),
    LT("<", " < "),
    LE("<=", " <= "),
    GT(">", " > "),
    GE(">=", " >= "),

    PLUS("+", " + "),
    MINUS("-", " - "),
    MUL("*", " * "),
    DIV("/", " / "),
    MOD("%", " % "),

    AND("&", " & "),
    OR("|", " | "),
    XOR("^", " ^ "),

    POW("pow", " pow "),
    SQRT("sqrt", " sqrt "),
    EXP("exp", " exp "),
    LOG("log", " log "),

    SIN("sin", " sin "),
    COS("cos", " cos "),
    TAN("tan", " tan "),
    ASIN("asin", " asin "),
    ACOS("acos", " acos "),
    ATAN("atan", " atan "),
    ATAN2("atan2", " atan2 "),

    SHIFTL("<<"),
    SHIFTR(">>"),
    SHIFTUR(">>>"),

    EQUALS("equals", " equals "),
    NOTEQUALS("notequals", " notequals "),
    EQUALSIGNORECASE("equalsignorecase", " equalsignorecase "),
    NOTEQUALSIGNORECASE("notequalsignorecase", " notequalsignorecase "),
    STARTSWITH("startswith", " startswith "),
    NOTSTARTSWITH("notstartswith", " notstartswith " ),
    ENDSWITH("endswith", " endswith "),
    NOTENDSWITH("notendswith", " notendswith "),
    CONTAINS("contains", " contains "),
    NOTCONTAINS("notcontains", " notcontains "),
    ISINTEGER("isinteger", " isinteger "),
    NOTINTEGER("notinteger", " notinteger "),
    ISFLOAT("isfloat", " isfloat "),
    NOTFLOAT("notfloat", " notfloat "),
    ISLONG("islong", " islong "),
    NOTLONG("notlong", " notlong "),
    ISDOUBLE("isdouble", " isdouble "),
    NOTDOUBLE("notdouble", " notdouble "),
    ISBOOLEAN("isboolean", " isboolean "),
    NOTBOOLEAN("notboolean", " notboolean "),
    EMPTY("empty", " empty "),
    NOTEMPTY("notempty", " notempty "),
    MATCHES("matches", " matches "),
    NOMATCHES("nomatches", " notmatches "),
    REGIONMATCHES("regionmatches", " regionmatches "),
    NOREGIONMATCHES("noregionmatches", " notregionmatches ");

    private final String[] symbols;

    private static final Map<String, Operator> lookup = new HashMap<>();

    static {
        for (Operator op : Operator.values()) {
            for (String symbol : op.symbols) {
                lookup.put(symbol, op);
            }
        }
    }

    Operator(final String ... symbols) {
        assert symbols.length > 0;
        this.symbols = symbols;
    }

    public static Operator get(String symbol) {
        assert lookup.containsKey(symbol);
        return lookup.get(symbol);
    }

    @Override
    public void accept(ModelVisitor visitor) {
        visitor.preVisit(this);
        visitor.postVisit(this);
    }

    @Override
    public String toString() {
        return this.symbols[0];
    }
}
