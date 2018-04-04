/*
 *   Project: Speedith.Core
 * 
 * File name: Class.java
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
package speedith.core.reasoning.rules.instructions;

import speedith.core.lang.Region;
import speedith.core.lang.Zone;
import speedith.core.reasoning.RuleApplicationInstruction;
import speedith.core.reasoning.args.RuleArg;
import speedith.core.reasoning.args.SpiderRegionArg;
import speedith.core.reasoning.args.SpiderZoneArg;
import speedith.core.reasoning.args.ZoneArg;
import speedith.core.reasoning.args.selection.SelectSingleSpiderStep;
import speedith.core.reasoning.args.selection.SelectZonesStep;
import speedith.core.reasoning.args.selection.SelectionSequence;
import speedith.core.reasoning.args.selection.SelectionStep;
import speedith.core.reasoning.rules.AddFeet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Instructions on how to get arguments for the {@link AddFeet add feet}
 * inference rule.
 *
 * @author Matej Urbas [matej.urbas@gmail.com]
 */
public class AddFeetRuleInstruction implements RuleApplicationInstruction<SpiderRegionArg> {

    //<editor-fold defaultstate="collapsed" desc="Fields">
    /**
     * You ask yourself why not a static field?
     *
     * Because of lazy initialisation, which is provided through the singleton
     * pattern.
     */
    private final List<? extends SelectionStep> steps = Arrays.asList(new SelectSingleSpiderStep(false), SelectZonesStep.getInstance());
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Hidden Constructor">
    private AddFeetRuleInstruction() {
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Singleton Instance">
    /**
     * Returns the singleton instance of these instructions.
     *
     * @return the singleton instance of these instructions.
     */
    public static AddFeetRuleInstruction getInstance() {
        return SingletonContainer.TheInstructions;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Implementation of RuleApplicationInstruction">
    @Override
    public List<? extends SelectionStep> getSelectionSteps() {
        return Collections.unmodifiableList(steps);
    }

    @Override
    public SpiderRegionArg extractRuleArg(SelectionSequence selectionSequence, int subgoalIndex) {
        SpiderZoneArg sza = (SpiderZoneArg) selectionSequence.getAcceptedSelectionsForStepAt(0).get(0);
        String spider = sza.getSpider();
        TreeSet<Zone> addFeetZones = new TreeSet<>();
        for (RuleArg ruleArg : selectionSequence.getAcceptedSelectionsForStepAt(1)) {
            ZoneArg zoneArg = (ZoneArg) ruleArg;
            addFeetZones.add(zoneArg.getZone());
        }
        return new SpiderRegionArg(subgoalIndex, sza.getSubDiagramIndex(), spider, new Region(addFeetZones));
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Helper Classes">
    private static final class SingletonContainer {

        private static final AddFeetRuleInstruction TheInstructions = new AddFeetRuleInstruction();
    }
    //</editor-fold>
}
