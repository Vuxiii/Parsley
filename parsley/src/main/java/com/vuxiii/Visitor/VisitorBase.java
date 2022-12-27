package com.vuxiii.Visitor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class VisitorBase {
    public <T>void visit( T t ) {
        _visit( t, "visit" );
    }

    public <T>void preVisit( T t ) {
        _visit( t, "preVisit" );
    };
    public <T>void midVisit( T t ) {
        _visit( t, "midVisit" );
    };
    public <T>void postVisit( T t ) {
        _visit( t, "postVisit" );
    };

    // protected 

    private <T>void _visit( T t, String prefix ) {
        Method[] methods = this.getClass().getDeclaredMethods();
        for ( Method m : methods ) {
            if ( m.getName().startsWith( prefix ) ) {
                try {
                    m.invoke( this, t );
                } catch (IllegalAccessException | InvocationTargetException e) {
                    System.out.println( "We failed trying to visit " + t );
                    e.printStackTrace();
                }
            }
            
        }
    }
}
