/*
 * Copyright (c) 2020, Jonathan Revusky revusky@freemarker.es
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and  the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package freemarker.template;

import java.util.Date;

/**
 * A simple implementation of the <tt>TemplateDateModel</tt>
 * interface. Note that this class is immutable.
 * <p>This class is thread-safe.
 * 
 * @version $Id: SimpleDate.java,v 1.11 2004/03/13 13:05:09 ddekany Exp $
 * @author Attila Szegedi
 */
public class SimpleDate implements TemplateDateModel
{
    private final Date date;
    private final int type;
    
    /**
     * Creates a new date model wrapping the specified date object and
     * having DATE type.
     */
    public SimpleDate(java.sql.Date date) {
        this(date, DATE);
    }
    
    /**
     * Creates a new date model wrapping the specified time object and
     * having TIME type.
     */
    public SimpleDate(java.sql.Time time) {
        this(time, TIME);
    }
    
    /**
     * Creates a new date model wrapping the specified time object and
     * having DATETIME type.
     */
    public SimpleDate(java.sql.Timestamp datetime) {
        this(datetime, DATETIME);
    }
    
    /**
     * Creates a new date model wrapping the specified date object and
     * having the specified type.
     */
    public SimpleDate(Date date, int type) {
        if(date == null) {
            throw new IllegalArgumentException("date == null");
        }
        this.date = (Date)date.clone();
        this.type = type;
    }
    
    public Date getAsDate() {
        return (Date)date.clone();
    }

    public int getDateType() {
        return type;
    }
    
    public String toString() {
        return date.toString();
    }
}
