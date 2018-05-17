package com.rti.examples.util;

/**
 * 
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * This class represents the options that can be controlled for a program.
 * @author ken
 */
public class ProgramOptions {
    
    /**
     * This class represents a particular option available to a program.
     */
    public static class Option implements Comparable<Option> {
        /**
         * String used to identify the argument associated with this Option.
         */
        private final String name;
        /**
         * The type of this Option's value.
         */
        private final Class<?> type;
        /**
         * True iff this Option is a boolean option.  Boolean options have
         * their default values toggled when present in argument list.
         */
        private final boolean isBool;
        /**
         * Stores the value of the Option.  Initialized to the default value if one
         * is given.  A null devault value implies the Option is required.
         */
        private String value;
        /**
         * Tracks whether or not the Option is required to be present. 
         */
        private final boolean isRequired;
        /**
         * Tracks whether this Option has been found while parsing arguments.
         */
        private boolean found;
        /**
         * Sequence number indicating this Option's place within ProgramOptions.  This
         * allows Options to be printed in the order they were added, rather than e.g.
         * alphabetically.  This is often more sensible, as related options
         * can be added contiguously.
         */
        final int seq;

        /**
         * Creates a new String Option and adds it to <code>p</code>.
         * @param p the ProgramOptions to which the Option will be added.
         * @param name the name of the Option
         * @param defaultValue the initial value of the Option, or null if it is required.
         * @return a new Option
         */
        public static Option makeStringOption(ProgramOptions p, String name, String defaultValue){
            return new Option(p, name, defaultValue, String.class, false);
        }
        
        /**
         * Creates a new Boolean Option and adds it to <code>p</code>.  If found as an
         * argument, the Option will take its non-default value.
         * @param p the ProgramOptions to which the Option will be added.
         * @param name the name of the Option
         * @param defaultValue the initial value of the Option, or null if it is required.
         * @return a new Option
         */
        public static Option makeBooleanOption(ProgramOptions p, String name, boolean defaultValue){
            return new Option(p, name, (defaultValue?"true":"false"), boolean.class, true);
        }        
        
        /**
         * Creates a new Option of arbitrary type and adds it to <code>p</code>.  Use only
         * when a more specific Option factory-constructor is not available.
         * @param p the ProgramOptions to which the Option will be added.
         * @param name the name of the Option
         * @param type the class that the value of this Option represents
         * @param defaultValue the initial value of the Option, or null if it is required.
         * @return a new Option
         */
        public static Option makeOption(ProgramOptions p, String name, Class<?> type, String defaultValue){
            return new Option(p, name, defaultValue, type, false);
        }
        
        /**
         * Creates a new Boolean Option and adds it to <code>p</code>.
         * @param p the ProgramOptions to which the Option will be added.
         * @param name the name of the Option
         * @param defaultValue the initial value of the Option, or null if it is required
         * @param type the class that the value of this Option represents
         * @param isBool true iff the Option is a boolean option
         */
        protected Option(ProgramOptions p, String name, String defaultValue, Class<?> type, boolean isBool){
            if(p == null || name == null){
                throw NULL_VALUE_EXCEPTION;
            }            
            this.name = name;
            this.value = defaultValue;
            this.type = type;
            this.isBool = isBool;
            this.isRequired = (defaultValue == null);
            found = false;
            seq = p.addOption(this);
            // To instead order Options alphabetically, set seq to 0
        }
        
        /**
         * This provides a way to create special options such as <code>DEFAULT_HELP_OPTION</code>.
         * This constructor does not automatically add the Option to a ProgramOptions.
         * @param name the name of the Option
         * @param defaultValue the initial value of the Option, or null if it is required
         * @param type the class that the value of this Option represents
         * @param isBool true iff the Option is a boolean option
         */
        protected Option(String name, String defaultValue, Class<?> type, boolean isBool){
            if(name == null){
                throw NULL_VALUE_EXCEPTION;
            }            
            this.name = name;
            this.value = defaultValue;
            this.type = type;
            this.isBool = isBool;
            this.isRequired = (defaultValue == null);
            this.found = false;
            this.seq = 0;
        }        
        
        /**
         * string describing how this Options should be used on command line
         * @param optionPrefix prefix used by ProgramOption
         */
        public String getUsageString(String optionPrefix) {
            StringBuffer buffer = new StringBuffer();
            if (!isRequired) {
                buffer.append("[");
            }
            buffer.append(optionPrefix);
            buffer.append(name);
            if (!isBool) {
                buffer.append(" value");
            }
            if (!isRequired) {
                buffer.append("]");
            }
            return buffer.toString();
        }
        
        /**
         * string describing status of Option
         */
        public String toString() {
            return "Option : " +
            "[name=" + name + "], " +
            "[defaultValue=" + (isRequired?("none (required)"):(value)) + "], " +
            "[seq =" + seq +"]," +
            "[found =" + found +"]," +            
            "[type=" + type + "], " +
            "[isBool=" + isBool + "], " +
            "[value=" + value + "]";
        }
        
        /**
         * Hash of name.  Note that a.equals(b) still implies
         * a.hashCode()==b.hashCode()
         */
        public int hashCode() {
            return name.hashCode();
        }

        /**
         * Compares Options, first by sequence number, then by name.
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Option o) {
            Option other = (Option) o;
            int r = ((this.seq < other.seq)?-1:((this.seq > other.seq)?1:0));
            if(r == 0) {
                r = this.name.compareTo(other.name);
            }
            return r;
        }

        /**
         * get Option value, parsed as <code>theType</code> 
         * @param theType desired conversion target
         */
        protected Option asType(Class<?> theType) {
            if (theType != null && !theType.equals(type)) {
                throw WRONG_TYPE_EXCEPTION;
            }
            return this;
        }        
        
        /**
         * get Option value, parsed as a boolean
         */
        public boolean asBoolean() {
            return Boolean.valueOf(asType(boolean.class).value).booleanValue();
        }
        
        /**
         * get Option value, parsed as a byte
         */
        public byte asByte() {
            return Byte.parseByte(asType(byte.class).value);
        }
        
        /**
         * get Option value, parsed as a short
         */
        public short asShort() {
            return Short.parseShort(asType(short.class).value);
        }
        
        /**
         * get Option value, parsed as an int
         */
        public int asInt() {
            return Integer.parseInt(asType(int.class).value);
        }
        
        /**
         * get Option value, parsed as a long 
         */
        public long asLong() {
            return Long.parseLong(asType(long.class).value);
        }
        
        /**
         * get Option value, parsed as a float
         */
        public float asFloat() {
            return Float.parseFloat(asType(float.class).value);
        }
        
        /**
         * get Option value, parsed as a double
         */
        public double asDouble() {
            return Double.parseDouble(asType(double.class).value);
        }

        /**
         * get Option value, parsed as a String
         */
        public String asString() {
            return asType(String.class).value;
        }
        
        /**
         * get List of Options as strings, tokenized (but not including) by <code>delimiters</code>
         */
        public List<String> asStringList(String delimiters) {
            return asStringList(delimiters, false);
        }
        
        /**
         * get List of Options as strings, tokenized by <code>delimiters</code>
         * @param returnDelimiters if true, then the delimiter characters are also returned as tokens.
         */
        public List<String> asStringList(String delimiters, 
                boolean returnDelimiters) {
            String value = asType(String.class).value;
            StringTokenizer tokenizer = 
                new StringTokenizer(value, delimiters, returnDelimiters);
            List<String> valueList = new ArrayList<String>(
                    tokenizer.countTokens());
            while(tokenizer.hasMoreTokens()) {
                valueList.add(tokenizer.nextToken());
            }
            return valueList;
        }        
    }
    
    /**
     * Thrown when a null Option or parameter is found.
     */
    private static final IllegalArgumentException NULL_VALUE_EXCEPTION =
        new IllegalArgumentException("null values are not allowed");
    
    /**
     * Thrown when an Option is asked to be interpreted as an incompatible type.
     */
    private static final IllegalArgumentException WRONG_TYPE_EXCEPTION =
        new IllegalArgumentException("incorrect type");
    
    /**
     * Default prefix for arguments.
     */
    public final static String DEFAULT_OPTION_PREFIX = "-";
    
    /**
     * Current prefix for a set of ProgramOptions
     */
    protected String optionPrefix;
    
    /**
     * Map of Options keyed on names.
     */
    protected final Map<String, Option> options = new HashMap<String, Option>();
    
    /**
     * A "help" Option that may be added.
     */
    protected static final Option DEFAULT_HELP_OPTION =  new Option("help","false", boolean.class, true);
    
    /**
     * Default constructor.
     */
    public ProgramOptions() {        
        this(DEFAULT_OPTION_PREFIX);
    }
    
    /**
     * This constructor allows the caller to override the default option prefix.
     */
    public ProgramOptions(String optionPrefix) {
        this.optionPrefix = optionPrefix;        
    }
    
    /**
     * Add an Option to the set of ProgramOptions.  If an option with the
     * same name exists, throw.
     * @param o the Option to be added
     * @return the total number of Options after the insertion.
     */
    public int addOption(Option o) {
        if(o == null)
            throw new IllegalArgumentException("can't add null option");
        if(options.containsKey(o.name)) {
            throw new IllegalArgumentException("duplicate option");
        }
        options.put(o.name, o);
        return options.size();
    }   
    
    /**
     * Processes Options, matching argument strings to names of Options
     * and setting the properties for each Option found.
     * @param args list of args to be processed
     */
    public void parseOptions(String[] args) {
        // Try to map each argument to an Option
        for(int i = 0; i < args.length; i++) {
            String arg = args[i].substring(optionPrefix.length());
            Option option = (Option) options.get(arg);
            if (option == null) {
                throw new IllegalArgumentException(arg);
            }
            
            // DEFAULT_HELP_OPTION is static, so we can use == to compare.
            if(option == DEFAULT_HELP_OPTION) {
                System.err.println("Usage : \n" + getPrintableDescription());
                System.exit(-1);                
            }
            
            if (option.isBool) {
                if(!option.found) {
                    // Boolean options, when present, toggle the default value
                    option.value = (option.value=="true"?"false":"true");
                }
            } else {
                if (args.length > (i+1)) {
                    i++;
                    option.value = args[i];
                } else {
                    throw new IllegalArgumentException(
                            "Missing value for option : " + option.name);
                }                
            }
            option.found = true;            
        }

        // Ensure all required arguments are present.
        Iterator<Option> i = options.values().iterator();
        while(i.hasNext()) {
            Option o = (Option) i.next();
            if(o.isRequired && !o.found) {
                throw new IllegalArgumentException("Missing required option : " + o.name);                
            }
        }        
    }
    
    /**
     * string describing all Option usage
     */    
    public String getPrintableDescription() {
        StringBuffer buffer = new StringBuffer();
        List<Option> sortedOptions = new ArrayList<Option>(options.values());
        Collections.sort(sortedOptions);
        for(int i = 0; i < sortedOptions.size(); i++) {
            Option option = (Option) sortedOptions.get(i);
            buffer.append(" ");
            buffer.append(option.getUsageString(optionPrefix));
            // every third option, add a line break
            if ((i+1) % 3 == 0) {
                buffer.append("\n");
            }
        }
        return buffer.toString();
    }

    /**
     * Adds a "help" Option.  This Option, if found while parsing arguments, 
     * prints program usage and exits.
     */
    public void addHelpOption() {
        options.put(DEFAULT_HELP_OPTION.name, DEFAULT_HELP_OPTION);
    }
    
    /**
     * Returns concatenated Option strings.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ProgramOptions : ");
        buffer.append("[optionPrefix=");
        buffer.append(optionPrefix);
        buffer.append("], ");
        Iterator<Option> i = options.values().iterator();
        while(i.hasNext()) {
            buffer.append(i.next());
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
