/**
 * 
 */
package org.prelle.telnet;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

/**
 * @author prelle
 *
 */
public class LineBuffer {

    private final static Logger logger = System.getLogger("telnet");
    
    public final static int DELETE = 8;
    public final static int TABULATOR = 9;
    public final static int ENTER  = 13;
    public final static int UP     = 65;
    
    private StringBuffer input;
    private String lastInput;
    
    //------------------------------------------------------
    /**
     */
    public LineBuffer() {
            input   = new StringBuffer();
    }
    
    //------------------------------------------------------
    public void appendChar(char c) {
            input.append(c);
    }
    
    //------------------------------------------------------
    public void processKeyEvent(int key) {
            switch (key) {
            case DELETE:
                    if (input.length()>0)
                            input.deleteCharAt(input.length()-1);
                    return;
            case ENTER:
                    lastInput = input.toString();
                    input = new StringBuffer();
                    return;
            case UP:
                    // TODO Browse history
                    logger.log(Level.WARNING,"UICOmmon.LineBuffer.processKey: UP");
                    return;
            default:
                    logger.log(Level.WARNING,"TODO: LineBuffer.processKey: "+key);
            }
    }
    
    //------------------------------------------------------
    public String getFinishedInput() {
            return lastInput;
    }
    
    //------------------------------------------------------
    public String getUnfinishedInput() {
            return input.toString();
    }

    //--------------------------------------------------------------
    /**
     * @param complete
     */
    public void setInput(String complete) {
            input.replace(0, input.length(), complete);
    }

}
