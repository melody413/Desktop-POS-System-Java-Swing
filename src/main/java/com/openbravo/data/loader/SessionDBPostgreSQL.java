//    Roxy Pos  - Touch Friendly Point Of Sale
//    Copyright © 2009-2020 uniCenta & previous Openbravo POS works
//    https://unicenta.com
//
//    This file is part of Roxy Pos
//
//    Roxy Pos is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   Roxy Pos is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Roxy Pos.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.data.loader;

/**
 *
 * @author adrianromero
 */
public class SessionDBPostgreSQL implements SessionDB {

    /**
     *
     * @return
     */
    @Override
    public String TRUE() {
        return "True";
    }

    /**
     *
     * @return
     */
    @Override
    public String FALSE() {
        return "False";
    }

    /**
     *
     * @return
     */
    @Override
    public String INTEGER_NULL() {
        return "CAST(NULL AS INTEGER)";
    }

    /**
     *
     * @return
     */
    @Override
    public String CHAR_NULL() {
        return "CAST(NULL AS CHAR)";
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return "PostgreSQL";
    }

    /**
     *
     * @param s
     * @param sequence
     * @return
     */
    @Override
    public SentenceFind getSequenceSentence(Session s, String sequence) {
        return new StaticSentence(s, "SELECT NEXTVAL('" + sequence + "')", null, SerializerReadInteger.INSTANCE);
    }
    
    /**
     *
     * @param s
     * @param sequence
     * @return
     */
    @Override
    public SentenceFind resetSequenceSentence(Session s, String sequence){
        return new StaticSentence(s, "ALTER SEQUENCE " + sequence + " RESTART WITH '0'", null, SerializerReadInteger.INSTANCE);   
    }
}