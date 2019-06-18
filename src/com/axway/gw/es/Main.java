package com.axway.gw.es;

import java.io.File;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.axway.gw.es.tools.Type;

public class Main {
	
    public static void main(String[] args) {
        try {
        	File f = new File("C:\\java\\es\\proto\\types\\Internationalization.yaml");
            Type user = new Type(f);
            System.out.println(ReflectionToStringBuilder.toString(user,ToStringStyle.MULTI_LINE_STYLE));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
