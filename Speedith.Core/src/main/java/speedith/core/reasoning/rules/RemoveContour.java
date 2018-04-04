/*
 *   Project: Speedith.Core
 * 
 * File name: RemoveContour.java
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
package speedith.core.reasoning.rules;

import speedith.core.i18n.Translations;
import speedith.core.lang.DiagramType;
import speedith.core.lang.SpiderDiagram;
import speedith.core.reasoning.*;
import speedith.core.reasoning.args.ContourArg;
import speedith.core.reasoning.args.MultipleRuleArgs;
import speedith.core.reasoning.args.RuleArg;
import speedith.core.reasoning.args.SubDiagramIndexArg;
import speedith.core.reasoning.rules.instructions.SelectContoursInstruction;
import speedith.core.reasoning.rules.transformers.RemoveContoursTransformer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Matej Urbas [matej.urbas@gmail.com]
 * @author Sven Linker [s.linker@brighton.ac.uk]
 */
public class RemoveContour extends SimpleInferenceRule<MultipleRuleArgs> implements Serializable, ForwardRule<MultipleRuleArgs> {

    // <editor-fold defaultstate="collapsed" desc="Fields">
    /**
     * The name of this inference rule.
     */
    public static final String InferenceRuleName = "remove_contour";

    private static final Set<DiagramType> applicableTypes = EnumSet.of(DiagramType.SpiderDiagram,DiagramType.EulerDiagram);
    private static final long serialVersionUID = -869717711275052747L;

	private static final ContourArg ContourArg = null;
    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Inference Rule Implementation">
    @Override
    public RuleApplicationResult apply(RuleArg args, Goals goals) throws RuleApplicationException {
        return apply(args, goals, ApplyStyle.GoalBased);
    }

    @Override
    public InferenceRule<MultipleRuleArgs> getInferenceRule() {
        return this;
    }

    @Override
    public String getInferenceName() {
        return InferenceRuleName;
    }

    @Override
    public String getDescription(Locale locale) {
        return Translations.i18n(locale, "REMOVE_CONTOUR_DESCRIPTION");
    }

    @Override
    public String getCategory(Locale locale) {
        return Translations.i18n(locale, "INF_RULE_CATEGORY_PURELY_DIAGRAMMATIC");
    }

    @Override
    public String getPrettyName(Locale locale) {
        return Translations.i18n(locale, "REMOVE_CONTOUR_PRETTY_NAME");
    }

    @Override
    public Class<MultipleRuleArgs> getArgumentType() {
        return MultipleRuleArgs.class;
    }

    @Override
    public RuleApplicationInstruction<MultipleRuleArgs> getInstructions() {
        return new SelectContoursInstruction();
    }
    //</editor-fold>


    @Override
    public Set<DiagramType> getApplicableTypes() {
        return applicableTypes;
    }

    @Override
    public RuleApplicationResult applyForwards(RuleArg args, Goals goals) throws RuleApplicationException {
        return apply(args, goals, ApplyStyle.Forward);
    }
    
    public static String getTranslation(MultipleRuleArgs args){
    	ContourArg conArg = (ContourArg) args.get(0);
		String ecContour = conArg.getContour();
		int ecSub = conArg.getSubDiagramIndex();
    	if (args.size() > 1){
    		String selectedContours = ecContour;
    		//String selectedSubs = String.valueOf(ecSub);
    		for (int i = 1; i < args.size() - 1; i++){
    			conArg = (ContourArg) args.get(i);
    			ecContour = conArg.getContour();
      		  	ecSub = conArg.getSubDiagramIndex();
      		  	selectedContours = selectedContours + ", " + ecContour;
      		  	//selectedSubs = selectedSubs + ", " + ecSub;
    		}
    		conArg = (ContourArg) args.get(args.size() - 1);
			ecContour = conArg.getContour();
  		  	ecSub = conArg.getSubDiagramIndex();
  		  	selectedContours = selectedContours + " and " + ecContour;
  		  	//selectedSubs = selectedSubs + " and " + ecSub;
  	    	return "Contours " + selectedContours + " from sub-diagram " + ecSub 
  	    			+ ", were removed from the diagram";
    	}
    	else{
    		return "Contour " + ecContour + " from sub-diagram " + ecSub + " was removed from the diagram";
    	}
    }

    protected RuleApplicationResult apply(final RuleArg args, Goals goals, ApplyStyle applyStyle) throws RuleApplicationException {
        ArrayList<ContourArg> contourArgs = ContourArg.getContourArgsFrom(getTypedRuleArgs(args));
        SpiderDiagram[] newSubgoals = goals.getGoals().toArray(new SpiderDiagram[goals.getGoalsCount()]);
        newSubgoals[contourArgs.get(0).getSubgoalIndex()] = getSubgoal(contourArgs.get(0), goals).transform(new RemoveContoursTransformer(contourArgs, applyStyle));
        return createRuleApplicationResult(newSubgoals);
    }

}
