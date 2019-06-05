// ----------------------------------------------------------------
// The contents of this file are distributed under the CC0 license.
// See http://creativecommons.org/publicdomain/zero/1.0/
// ----------------------------------------------------------------

package com.noaoh.ezJSON;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.lang.StringBuilder;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;

// This class represents a node in a JSON DOM. Here is an example for how to use this class:
//
// class Submarine
// {
//     boolean atomic;
//     int crewSize;
//     double depth;
//     Periscope peri;
//     ArrayList<Torpedo> ammo;
//
//
//     // Unmarshaling constructor
//     Submarine(Json ob)
//     {
//         atomic = ob.getBool("atomic");
//         crewSize = (int)ob.getLong("crewSize");
//         depth = ob.getDouble("depth");
//         peri = new Periscope(ob.get("peri"));
//         ammo = new ArrayList<Torpedo>();
//         Json tmpList = ob.get("ammo");
//         for(int i = 0; i < tmpList.size(); i++)
//             ammo.add(new Torpedo(tmpList.get(i)));
//     }
//
//
//     // Marshals this object into a JSON DOM
//     Json marshal()
//     {
//         Json ob = Json.newObject();
//         ob.add("atomic", atomic);
//         ob.add("crewSize", crewSize);
//         ob.add("depth", depth);
//         ob.add("peri", peri.marshal());
//         Json tmpList = Json.newList();
//         ob.add("ammo", tmpList);
//         for(int i = 0; i < ammo.size(); i++)
//             tmpList.add(ammo.get(i).marshal());
//         return ob;
//     }
// }
//
public abstract class Json
{
    static byte backspace = 8; // '\b'
    static byte horizontalTabulation = 9; // '\t'
    static byte lineFeed = 10; // '\n'
    static byte formFeed = 12; // '\f'
    static byte carriageReturn = 13; // '\r'
    static byte space = 32; // ' '
    static byte quotationMark = 34; // '\"'
    static byte apostrophe = 39; // '\''
    static byte plus = 43; // '+'
    static byte comma = 44; // ','
    static byte hypenMinus = 45; // '-'
    static byte fullStop = 46; // '.'
    static byte zero = 49; // '0'
    static byte nine = 57; // '9'
    static byte colon = 58; // ':'
    static byte E = 69; // 'E'
    static byte leftSquareBracket = 91; // '['
    static byte reverseSolidus = 92; // '\'
    static byte rightSquareBracket = 93; // ']'
    static byte e = 101; // 'e'
    static byte leftCurlyBracket = 123; // '{'
    static byte rightCurlyBracket = 125; // '}'
    static byte[] jsonTrue = new byte[] {116, 114, 117, 101}; // "true"
    static byte[] jsonFalse = new byte[] {102, 97, 108, 115, 101}; // "false"
    static byte[] jsonNull = new byte[] {110, 117, 108, 108}; // "null"

    abstract void write(StringBuilder sb);

    public static Json newObject()
    {
        return new JObject();
    }

    public static Json newList()
    {
        return new JList();
    }

    public static Json parseNode(ByteParser p)
    {
        p.skipWhitespace();
        if(p.remaining() == 0)
            throw new RuntimeException("Unexpected end of JSON file");
        byte c = p.peek();
        if(c == quotationMark)
            return new JString(JString.parseString(p));
        else if(c == leftCurlyBracket)
            return JObject.parseObject(p);
        else if(c == leftSquareBracket)
            return JList.parseList(p);
        else if(c == jsonTrue[0])
        {
            p.expect(jsonTrue);
            return new JBool(true);
        }
        else if(c == jsonFalse[0])
        {
            p.expect(jsonFalse);
            return new JBool(false);
        }
        else if(c == jsonNull[0])
        {
            p.expect(jsonNull);
            return new JNull();
        }
        else if((c >= zero && c <= nine) || c == hypenMinus)
            return JDouble.parseNumber(p);
        else
        {
            byte[] slice = ByteBuffer.wrap(p.contents, p.pos, Math.min(p.remaining(), 50)).array();
            throw new RuntimeException("Unexpected token at " + slice);
        }
    }

    public int size()
    {
        return this.asList().size();
    }

    public Json get(String name)
    {
        return this.asObject().field(name);
    }

    public Json get(int index)
    {
        return this.asList().get(index);
    }

    public boolean getBool(String name)
    {
        return get(name).asBool();
    }

    public boolean getBool(int index)
    {
        return get(index).asBool();
    }

    public long getLong(String name)
    {
        return get(name).asLong();
    }

    public long getLong(int index)
    {
        return get(index).asLong();
    }

    public double getDouble(String name)
    {
        return get(name).asDouble();
    }

    public double getDouble(int index)
    {
        return get(index).asDouble();
    }

    public String getString(String name)
    {
        return get(name).asString();
    }

    public String getString(int index)
    {
        return get(index).asString();
    }

    public void add(String name, Json val)
    {
        this.asObject().add(name, val);
    }

    public void add(String name, boolean val)
    {
        this.asObject().add(name, new Json.JBool(val));
    }

    public void add(String name, long val)
    {
        this.asObject().add(name, new Json.JLong(val));
    }

    public void add(String name, double val)
    {
        this.asObject().add(name, new Json.JDouble(val));
    }

    public void add(String name, String val)
    {
        this.asObject().add(name, new Json.JString(val));
    }

    public void add(Json item)
    {
        this.asList().add(item);
    }

    public void add(boolean val)
    {
        this.asList().add(new Json.JBool(val));
    }

    public void add(long val)
    {
        this.asList().add(new Json.JLong(val));
    }

    public void add(double val)
    {
        this.asList().add(new Json.JDouble(val));
    }

    public void add(String val)
    {
        this.asList().add(new Json.JString(val));
    }

    public boolean asBool()
    {
        return ((JBool)this).value;
    }

    public long asLong()
    {
        return ((JLong)this).value;
    }

    public double asDouble()
    {
        if(this instanceof JDouble)
            return ((JDouble)this).value;
        else
            return (double)((JLong)this).value;
    }

    public String asString()
    {
        return ((JString)this).value;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        write(sb);
        return sb.toString();
    }

    private JObject asObject()
    {
        return (JObject)this;
    }

    private JList asList()
    {
        return (JList)this;
    }

    public void save(String filename)
    {
        try
        {
            BufferedWriter out = new BufferedWriter(new FileWriter(filename));
            out.write(toString());
            out.close();
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Json parse(byte[] b)
    {
        ByteParser p = new ByteParser(b);
        return Json.parseNode(p);
    }

    public static Json load(String filename)
    {
        byte[] contents;
        try
        {
            contents = Files.readAllBytes(Paths.get(filename));
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
        return parse(contents);
    }

    public static class ByteParser
    {
        byte[] contents;
        int pos;

        ByteParser(byte[] c)
        {
            contents = c;
            pos = 0;
        }

        int remaining()
        {
            return contents.length - pos;
        }

        byte peek()
        {
            return contents[pos];
        }

        void advance(int n)
        {
            pos += n;
        }

        void retreat(int n)
        {
            pos -= n;
        }

        void skipWhitespace()
        {
            while(pos < contents.length && contents[pos] <= space)
                pos++;
        }

        void expect(byte[] b)
        {
            int length = Math.min(contents.length, pos + contents.length);
            byte[] contentsSlice = ByteBuffer.wrap(contents, pos, length).array();
            if(!Arrays.equals(contentsSlice, b))
                throw new RuntimeException("Expected \"" + b + "\", Got \"" + contentsSlice + "\"");
            pos += contentsSlice.length;
        }

        void expect(byte b)
        {
            if (contents[pos] != b)
                throw new RuntimeException("Expected \"" + b + "\", Got \"" + contents[pos] + "\"");
            pos += 1;
        }

        byte[] until(byte c)
        {
            int i = pos;
            while(i < contents.length && contents[i] != c)
                i++;
            byte[] b = ByteBuffer.wrap(contents, pos, i).array();
            pos = i;
            return b;
        }

        byte[] until(byte a, byte b)
        {
            int i = pos;
            while(i < contents.length && contents[i] != a && contents[i] != b)
                i++;
            byte[] slice = ByteBuffer.wrap(contents, pos, i).array();
            pos = i;
            return slice;
        }

        byte[] untilEscapeCharacter()
        {
            int i = pos;
            byte b = contents[i];
            while (i < contents.length &&
                    b != quotationMark &&
                    b != backspace &&
                    b != formFeed &&
                    b != lineFeed &&
                    b != carriageReturn &&
                    b != horizontalTabulation
            ) {
                i++;
                b = contents[i];
            }
            byte[] slice = ByteBuffer.wrap(contents, pos, i).array();
            pos = i;
            return slice;
        }

        byte[] whileReal()
        {
            int i = pos;
            while(i < contents.length)
            {
                byte c = contents[i];
                if((c >= zero && c <= nine) ||
                        (c == hypenMinus ||
                        c == plus ||
                        c == fullStop ||
                        c == e ||
                        c == E ||
                        c == space ||
                        c == lineFeed))
                    i++;
                else if (c == comma || c == colon || c == rightSquareBracket || c == rightCurlyBracket)
                    break;
                else
                    throw new RuntimeException("Invalid character " + c + " in number");
            }
            byte[] s = ByteBuffer.wrap(contents, pos, i).array();
            pos = i;
            return s;
        }
    }

    private static class NameVal
    {
        String name;
        Json value;

        NameVal(String nam, Json val)
        {
            if(nam == null)
                throw new IllegalArgumentException("The name cannot be null");
            if(val == null)
                val = new JNull();
            name = nam;
            value = val;
        }
    }

    private static class JObject extends Json
    {
        ArrayList<NameVal> fields;

        JObject()
        {
            fields = new ArrayList<NameVal>();
        }

        public void add(String name, Json val)
        {
            fields.add(new NameVal(name, val));
        }

        Json fieldIfExists(String name)
        {
            for(NameVal nv : fields)
            {
                if(nv.name.equals(name))
                    return nv.value;
            }
            return null;
        }

        Json field(String name)
        {
            Json n = fieldIfExists(name);
            if(n == null)
                throw new RuntimeException("No field named \"" + name + "\" found.");
            return n;
        }

        void write(StringBuilder sb)
        {
            sb.append("{");
            for(int i = 0; i < fields.size(); i++)
            {
                if(i > 0)
                    sb.append(",");
                NameVal nv = fields.get(i);
                JString.write(sb, nv.name);
                sb.append(":");
                nv.value.write(sb);
            }
            sb.append("}");
        }

        static JObject parseObject(ByteParser p)
        {
            p.expect(leftCurlyBracket);
            JObject newOb = new JObject();
            boolean readyForField = true;
            while(p.remaining() > 0)
            {
                byte c = p.peek();
                if(c <= space)
                {
                    p.advance(1);
                }
                else if(c == rightCurlyBracket)
                {
                    p.advance(1);
                    return newOb;
                }
                else if(c == comma)
                {
                    if(readyForField)
                        throw new RuntimeException("Unexpected ','");
                    p.advance(1);
                    readyForField = true;
                }
                else if(c == quotationMark)
                {
                    if(!readyForField)
                        throw new RuntimeException("Expected a ',' before the next field in JSON file");
                    p.skipWhitespace();
                    String name = JString.parseString(p);
                    p.skipWhitespace();
                    p.expect(colon);
                    Json value = Json.parseNode(p);
                    newOb.add(name, value);
                    readyForField = false;
                }
                else
                {
                    byte[] slice = ByteBuffer.wrap(p.contents, p.pos, p.pos + 10).array();
                    throw new RuntimeException("Expected a '}' or a '\"'. Got " + slice);
                }
            }
            throw new RuntimeException("Expected a matching '}' in JSON file");
        }
    }

    private static class JList extends Json
    {
        ArrayList<Json> list;

        JList()
        {
            list = new ArrayList<Json>();
        }

        public void add(Json item)
        {
            if(item == null)
                item = new JNull();
            list.add(item);
        }

        public int size()
        {
            return list.size();
        }

        public Json get(int index)
        {
            return list.get(index);
        }

        void write(StringBuilder sb)
        {
            sb.append("[");
            for(int i = 0; i < list.size(); i++)
            {
                if(i > 0)
                    sb.append(",");
                list.get(i).write(sb);
            }
            sb.append("]");
        }

        static JList parseList(ByteParser p)
        {
            p.expect(leftSquareBracket);
            JList newList = new JList();
            boolean readyForValue = true;
            while(p.remaining() > 0)
            {
                p.skipWhitespace();
                byte c = p.peek();
                if(c == rightSquareBracket)
                {
                    p.advance(1);
                    return newList;
                }
                else if(c == comma)
                {
                    if(readyForValue)
                        throw new RuntimeException("Unexpected ',' in JSON file");
                    p.advance(1);
                    readyForValue = true;
                }
                else
                {
                    if(!readyForValue)
                        throw new RuntimeException("Expected a ',' or ']' in JSON file");
                    newList.list.add(Json.parseNode(p));
                    readyForValue = false;
                }
            }
            throw new RuntimeException("Expected a matching ']' in JSON file");
        }
    }

    private static class JBool extends Json
    {
        boolean value;

        JBool(boolean val)
        {
            value = val;
        }

        void write(StringBuilder sb)
        {
            sb.append(value ? "true" : "false");
        }
    }

    private static class JLong extends Json
    {
        long value;

        JLong(long val)
        {
            value = val;
        }

        void write(StringBuilder sb)
        {
            sb.append(value);
        }
    }

    private static class JDouble extends Json {
        double value;

        JDouble(double val) {
            value = val;
        }

        void write(StringBuilder sb) {
            sb.append(value);
        }

        static Json parseNumber(ByteParser p) {
            String s = new String(p.whileReal());
            // Java doesn't support scientific notation for integers, see
            // https://docs.oracle.com/javase/specs/jls/se12/html/jls-3.html#jls-3.10.1
            // Additionally, the JSON RFC recommends the IEEE 754 binary64 standard (the double type)
            // as the encoding for large numbers
            if (s.indexOf('.') >= 0 || s.indexOf('e') >= 0 || s.indexOf('E') >= 0) {
                if (s.indexOf('e') == s.indexOf('.') + 1 || s.indexOf('E') == s.indexOf('.') + 1) {
                    throw new RuntimeException("Decimal must be followed by a number, not an exponent");
                } else if (s.indexOf('.') == (s.length() - 1)) {
                    throw new RuntimeException("Decimal must be followed by a number");
                } else if (s.indexOf('.') == s.indexOf('-') + 1) {
                    throw new RuntimeException("Decimal numbers must have an integer part");
                }
                return new JDouble(Double.parseDouble(s));
            } else {
                if ((s.indexOf('-') == 0 && s.indexOf('0') == 1 && s.length() > 2) || (s.indexOf('0') == 0 &&
                        s.length() > 1 && s.indexOf('-') == -1)) {
                    throw new RuntimeException("Integer cannot have a leading zero");
                }
                return new JLong(Long.parseLong(s));
            }
        }
    }

    private static class JString extends Json
    {
        String value;

        JString(String val)
        {
            value = val;
        }

        static void write(StringBuilder sb, String value)
        {
            sb.append('"');
            for(int i = 0; i < value.length(); i++)
            {
                char c = value.charAt(i);
                if(c < ' ')
                {
                    switch(c)
                    {
                        case '\b': sb.append("\\b"); break;
                        case '\f': sb.append("\\f"); break;
                        case '\n': sb.append("\\n"); break;
                        case '\r': sb.append("\\r"); break;
                        case '\t': sb.append("\\t"); break;
                        default:
                            sb.append(c);
                    }
                }
                else if(c == '\\')
                    sb.append("\\\\");
                else if(c == '"')
                    sb.append("\\\"");
                else
                    sb.append(c);
            }
            sb.append('"');
        }

        void write(StringBuilder sb)
        {
            write(sb, value);
        }

        static void isValidUTF8String(String s)
        {
            byte[] utf8_bytes= null;
            try
            {
                utf8_bytes= s.getBytes("UTF8");
                String utf8_string = new String(utf8_bytes, "UTF8");
            }
            catch (UnsupportedEncodingException e)
            {
               throw new RuntimeException("String " + s + " is not a valid UTF8 string");
            }
        }

        static String parseString(ByteParser p)
        {
            StringBuilder sb = new StringBuilder();
            p.expect(quotationMark);
            while(p.remaining() > 0)
            {
                byte c = p.peek();
                if(c == quotationMark)
                {
                    p.advance(1);
                    String s = sb.toString();
                    isValidUTF8String(s);
                    return s;
                }
                else if(c == reverseSolidus)
                {
                    p.advance(1);
                    c = p.peek();
                    p.advance(1);
                    switch(c)
                    {
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case '/': sb.append('/'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'n': sb.append('\n'); break;
                        case 'r': sb.append('\r'); break;
                        case 't': sb.append('\t'); break;
                        case 'u': p.retreat(1); sb.append(p.untilEscapeCharacter()); break;
                        default: throw new RuntimeException("Unrecognized escape sequence");
                    }
                }
                else
                {
                    sb.append(c);
                    p.advance(1);
                }
            }
            throw new RuntimeException("No closing \"");
        }
    }

    private static class JNull extends Json
    {
        JNull()
        {
        }

        void write(StringBuilder sb)
        {
            sb.append("null");
        }
    }

    public static void main(String[] args)
    {
        System.out.println("Hello, world!");
    }
}
