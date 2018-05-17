
/*
WARNING: THIS FILE IS AUTO-GENERATED. DO NOT MODIFY.

This file was generated from .idl using "rtiddsgen".
The rtiddsgen tool is part of the RTI Connext distribution.
For more information, type 'rtiddsgen -help' at a command shell
or consult the RTI Connext manual.
*/

package com.rti.examples.JavaDDSFileTransfer;

import com.rti.dds.typecode.*;

public class  FileFragmentTypeCode {
    public static final TypeCode VALUE = getTypeCode();

    private static TypeCode getTypeCode() {
        TypeCode tc = null;
        int __i=0;
        StructMember sm[]=new StructMember[5];

        sm[__i]=new  StructMember("file_name", false, (short)-1,  false,(TypeCode) new TypeCode(TCKind.TK_STRING,(com.rti.examples.JavaDDSFileTransfer.FILE_NAME_LENGTH_MAX.VALUE)),0 , false);__i++;
        sm[__i]=new  StructMember("file_size", false, (short)-1,  false,(TypeCode) TypeCode.TC_LONG,1 , false);__i++;
        sm[__i]=new  StructMember("frag_num", false, (short)-1,  false,(TypeCode) TypeCode.TC_LONG,2 , false);__i++;
        sm[__i]=new  StructMember("frag_total", false, (short)-1,  false,(TypeCode) TypeCode.TC_LONG,3 , false);__i++;
        sm[__i]=new  StructMember("frag", false, (short)-1,  false,(TypeCode) new TypeCode((com.rti.examples.JavaDDSFileTransfer.FILE_FRAGMENT_SIZE_MAX.VALUE), TypeCode.TC_OCTET),4 , false);__i++;

        tc = TypeCodeFactory.TheTypeCodeFactory.create_struct_tc("com::rti::examples::JavaDDSFileTransfer::FileFragment",ExtensibilityKind.EXTENSIBLE_EXTENSIBILITY,  sm);        
        return tc;
    }
}

