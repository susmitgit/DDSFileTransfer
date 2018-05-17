

/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package com.rti.examples.JavaDDSFileTransfer;

import com.rti.dds.infrastructure.*;
import com.rti.dds.infrastructure.Copyable;
import java.io.Serializable;
import com.rti.dds.cdr.CdrHelper;

public class FileFragment   implements Copyable, Serializable{

    public String file_name=  "" ; /* maximum length = ((com.rti.examples.JavaDDSFileTransfer.FILE_NAME_LENGTH_MAX.VALUE)) */
    public int file_size= 0;
    public int frag_num= 0;
    public int frag_total= 0;
    public ByteSeq frag =  new ByteSeq(((com.rti.examples.JavaDDSFileTransfer.FILE_FRAGMENT_SIZE_MAX.VALUE)));

    public FileFragment() {

    }
    public FileFragment (FileFragment other) {

        this();
        copy_from(other);
    }

    public static Object create() {

        FileFragment self;
        self = new  FileFragment();
        self.clear();
        return self;

    }

    public void clear() {

        file_name=  ""; 
        file_size= 0;
        frag_num= 0;
        frag_total= 0;
        if (frag != null) {
            frag.clear();
        }
    }

    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }        

        if(getClass() != o.getClass()) {
            return false;
        }

        FileFragment otherObj = (FileFragment)o;

        if(!file_name.equals(otherObj.file_name)) {
            return false;
        }
        if(file_size != otherObj.file_size) {
            return false;
        }
        if(frag_num != otherObj.frag_num) {
            return false;
        }
        if(frag_total != otherObj.frag_total) {
            return false;
        }
        if(!frag.equals(otherObj.frag)) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int __result = 0;
        __result += file_name.hashCode(); 
        __result += (int)file_size;
        __result += (int)frag_num;
        __result += (int)frag_total;
        __result += frag.hashCode(); 
        return __result;
    }

    /**
    * This is the implementation of the <code>Copyable</code> interface.
    * This method will perform a deep copy of <code>src</code>
    * This method could be placed into <code>FileFragmentTypeSupport</code>
    * rather than here by using the <code>-noCopyable</code> option
    * to rtiddsgen.
    * 
    * @param src The Object which contains the data to be copied.
    * @return Returns <code>this</code>.
    * @exception NullPointerException If <code>src</code> is null.
    * @exception ClassCastException If <code>src</code> is not the 
    * same type as <code>this</code>.
    * @see com.rti.dds.infrastructure.Copyable#copy_from(java.lang.Object)
    */
    public Object copy_from(Object src) {

        FileFragment typedSrc = (FileFragment) src;
        FileFragment typedDst = this;

        typedDst.file_name = typedSrc.file_name;
        typedDst.file_size = typedSrc.file_size;
        typedDst.frag_num = typedSrc.frag_num;
        typedDst.frag_total = typedSrc.frag_total;
        typedDst.frag.copy_from(typedSrc.frag);

        return this;
    }

    public String toString(){
        return toString("", 0);
    }

    public String toString(String desc, int indent) {
        StringBuffer strBuffer = new StringBuffer();        

        if (desc != null) {
            CdrHelper.printIndent(strBuffer, indent);
            strBuffer.append(desc).append(":\n");
        }

        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("file_name: ").append(file_name).append("\n");  
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("file_size: ").append(file_size).append("\n");  
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("frag_num: ").append(frag_num).append("\n");  
        CdrHelper.printIndent(strBuffer, indent+1);        
        strBuffer.append("frag_total: ").append(frag_total).append("\n");  
        CdrHelper.printIndent(strBuffer, indent+1);
        strBuffer.append("frag: ");
        for(int i__ = 0; i__ < frag.size(); ++i__) {
            if (i__!=0) strBuffer.append(", ");
            strBuffer.append(frag.get(i__));
        }
        strBuffer.append("\n"); 

        return strBuffer.toString();
    }

}
