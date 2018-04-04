/*
 *   Project: Speedith
 * 
 * File name: ICirclesToSpeedith.java
 *    Author: Matej Urbas [matej.urbas@gmail.com]
 * 
 *  Copyright © 2012 Matej Urbas
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package speedith.icircles.util;

import icircles.concreteDiagram.CircleContour;
import icircles.concreteDiagram.ConcreteZone;
import java.util.ArrayList;
import speedith.core.lang.Zone;

/**
 *
 * @author Matej Urbas [matej.urbas@gmail.com]
 */
public final class ICirclesToSpeedith {
    
    // <editor-fold defaultstate="collapsed" desc="Public Methods">
    public static Zone convert(ConcreteZone zone) {
        if (zone == null) {
            throw new IllegalArgumentException(speedith.core.i18n.Translations.i18n("GERR_NULL_ARGUMENT", "zone"));
        }
        // Gather all in-contours
        ArrayList<String> inContours = new ArrayList<String>();
        if (zone.getContainingContours() != null) {
            for (CircleContour inContour : zone.getContainingContours()) {
                inContours.add(inContour.ac.getLabel());
            }
        }
        // Gather all out-contours
        ArrayList<String> outContours = new ArrayList<String>();
        if (zone.getExcludingContours() != null) {
            for (CircleContour outContour : zone.getExcludingContours()) {
                outContours.add(outContour.ac.getLabel());
            }
        }
        // Return the corresponding zone.
        return new Zone(inContours, outContours);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Disabled constructor for this static utility class.
     */
    private ICirclesToSpeedith() {
    }
    // </editor-fold>
}
