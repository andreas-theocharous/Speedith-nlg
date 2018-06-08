/*
 *   Project: Speedith.Core
 * 
 * File name: RuleApplicationResult.java
 *    Author: Matej Urbas [matej.urbas@gmail.com]
 * 
 *  Copyright © 2011 Matej Urbas
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
package speedith.core.reasoning;

import propity.util.Sets;

import java.util.*;
import java.util.Map.Entry;

import static propity.util.Sets.equal;

import speedith.core.lang.*;
import speedith.core.reasoning.args.ContourArg;
import speedith.core.reasoning.args.MultipleRuleArgs;
import speedith.core.reasoning.args.RuleArg;
import speedith.core.reasoning.args.SpiderArg;
import speedith.core.reasoning.args.SpiderRegionArg;
import speedith.core.reasoning.args.SubDiagramIndexArg;
import speedith.core.reasoning.args.ZoneArg;
import speedith.core.reasoning.rules.AddFeet;
import speedith.core.reasoning.rules.Combining;
import speedith.core.reasoning.rules.CopyContours;
import speedith.core.reasoning.rules.ExcludedMiddle;
import speedith.core.reasoning.rules.RemoveContour;
import speedith.core.reasoning.rules.RemoveShadedZone;
import speedith.core.reasoning.rules.RemoveShading;
import speedith.core.reasoning.rules.SplitSpiders;

/**
 * @author Andreas Theocharous [at814@cam.ac.uk]
 */
public class Translate {

    // <editor-fold defaultstate="collapsed" desc="Fields">
    //private Goals goals;
    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Initialises an instance of the RuleApplicationResult class.
     * @param proofPanel1 
     * @param goals the goals that remained after the application of an
     * inference rule.
     */
	
	private static String[] subs = new String[50];
	private static String[] csubs = new String[50];
	
	public static void diagramNumbering(SpiderDiagram sd){
		int curr = 0;
		int sdn = sd.getSubDiagramAt(1).getSubDiagramCount();
		for (int i = 1; i <= sdn + 1; i++){
			if (sd.getSubDiagramAt(i).getSubDiagramCount() == 1){
				curr++;
				subs[i] = "d" + curr;
			}
		}
	}
	
	public static void conjunctionNumbering(SpiderDiagram sd){
		int curr = 0;
		int sdn = sd.getSubDiagramAt(1).getSubDiagramCount();
		for (int i = 1; i <= sdn + 1; i++){
			String s = sd.getSubDiagramAt(i).toString();
			if ((s.length() > 25) && (s.charAt(25) == '&')){
				curr++;
				csubs[i] = "c" + curr;
			}
		}
	}
	
	
	public static int PossibleTactic(ArrayList<String> rules, int start, String r){
		Set<String> infSet = new HashSet<String>();
		if ((r.equals("Introduce All Contours")) || (r.equals("Introduce All Contours (Deepest)")))
			infSet.add("Introduce Contour");
		else if ((r.equals("Introduce All Shaded Zones")) || (r.equals("Prepare For Copy Shading"))
				|| (r.equals("Introduce All Shaded Zones (Deepest)")))
			infSet.add("Introduce Shaded Zone");
		else if (r.equals("Combine All Diagrams"))
			infSet.add("Combining");
		else if (r.equals("Prepare For Copy Contours"))
			infSet.add("Remove Shaded Zone");
		else if (r.equals("Copy Contours")){
			infSet.add("Idempotency");
			infSet.add("Copy Contour");
			infSet.add("Copy Contour Topologically");	
		}
		else if (r.equals("Propagate Shading")){
			infSet.add("Idempotency");
			infSet.add("Copy Shading");
			infSet.add("Introduce Shaded Zone");	
		}
		else if (r.equals("Match Conclusion")){
			infSet.add("Erase Contour");
			infSet.add("Copy Shading");
			infSet.add("Introduce Contour");
			infSet.add("Introduce Shaded Zone");	
			infSet.add("Remove Shaded Zone");
			infSet.add("Implication Tautology");
			infSet.add("Trivial Implication Tautology");
		}
			
		int i = start;
		if (infSet.contains(rules.get(i))){
			while ((i < rules.size()) && (infSet.contains(rules.get(i)))){
				i++;
			}
			return i;
		}
		else
			return i + 1;
	}
    
    public static String ruleTranslation(SpiderDiagram[] sdg1, SpiderDiagram[] sdg2, String rule, RuleArg arg, List<InferenceApplication> apps, Goals initGoals)
    {
  	  CompoundSpiderDiagram c1 = null, c2 = null;
      PrimarySpiderDiagram sb1 = null, sb2 = null;
  	  if (rule.equals("Split Spider")){
        SpiderRegionArg ssarg = (SpiderRegionArg) arg;
  		String ssSpider = ssarg.getSpider();
  		String ssRegion = ssarg.getRegion().toString();
  		int ssSub = ssarg.getSubDiagramIndex();
  		int subgoal = ssarg.getSubgoalIndex();
  		
  		diagramNumbering(sdg1[subgoal]);
  		
  		PrimarySpiderDiagram sd = (PrimarySpiderDiagram) sdg1[subgoal].getSubDiagramAt(ssSub);
	    String initRegion = sd.getSpiderHabitat(ssSpider).toString();
	    PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg2[subgoal].getSubDiagramAt(ssSub + 1);
	    PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg2[subgoal].getSubDiagramAt(ssSub + 2);
	    String habitat1 = sd1.getSpiderHabitat(ssSpider).toString();
	    String habitat2 = sd2.getSpiderHabitat(ssSpider).toString();

  		return "split Spider was applied to " + ssSpider + " from " + subs[ssSub] + " which had habitat " 
  				+ initRegion + ". The result is two disjunctively connected sub-diagrams"
  				+ " both containing " + ssSpider + " but now the habitat in the first is " + habitat1
  				+ " and in the second it is " + habitat2;
        }
  	  else if (rule.equals("Combining")){
  	      SubDiagramIndexArg carg = (SubDiagramIndexArg) arg;
  		  int cSub1 = carg.getSubDiagramIndex() + 1;
  		  int cSub2 = carg.getSubDiagramIndex() + 2;
		  int subgoal = carg.getSubgoalIndex();
		  PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg1[subgoal].getSubDiagramAt(cSub1);
		  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg1[subgoal].getSubDiagramAt(cSub2);
		  PrimarySpiderDiagram sd3 = (PrimarySpiderDiagram) sdg2[subgoal].getSubDiagramAt(cSub1 - 1);
		  
		  SortedSet<String> sp1 = sd1.getSpiders();
		  SortedSet<String> sp2 = sd2.getSpiders();
		  
		  SortedMap<String, Region> h1 = sd1.getHabitats();
		  SortedMap<String, Region> h2 = sd2.getHabitats();
		  
		  diagramNumbering(sdg1[subgoal]);
		  conjunctionNumbering(sdg1[subgoal]);
		  
		  String spiders1 = "";
		  String spiders2 = "";
		  
		  
		  String[] spArr = new String[sp1.size()];
		  Iterator<String> it1 = sp1.iterator();
		  int c = 0;
		  while (it1.hasNext()) {
			  String s = it1.next();
			  if (sd3.getSpiders().contains(s)){
				  spArr[c] = s;
				  c++;
			  }
		  }
		  if (c > 0){
			  spiders1 = "contains the ";
			  if (c == 1)
				  spiders1 += "spider ";
			  else
				  spiders1 += "spiders ";
			  spiders1 = spiders1 + spArr[0];
			  for (int i = 1; i < c; i++)
				  spiders1 = spiders1 + ", " + spArr[i];
		  }
		  
		  SortedSet<Zone> sh1 = sd1.getShadedZones();
		  SortedSet<Zone> sh2 = sd2.getShadedZones();
		  
		  String[] shArr = new String[sh1.size()];
		  Iterator<Zone> it3 = sh1.iterator();
		  int csh = 0;
		  while (it3.hasNext()) {
			  Zone z = it3.next();
			  if (!sh2.contains(z)){
				  shArr[csh] = z.toString2();
				  csh++;
			  }
		  }
		  String shaded1 = "";
		  if (csh > 0){
			  if (c > 0)
				  shaded1 = " and the ";
			  else
				  shaded1 = "contains the ";
			  if (csh == 1)
				  shaded1 += "shaded zone ";
			  else
				  shaded1 += "shaded zones ";
			  shaded1 = shaded1 + shArr[0];
			  for (int i = 1; i < csh; i++)
				  shaded1 = shaded1 + ", " + shArr[i];
		  }
		  
		  String middle = "";
		  if ((c == 0) && (csh == 0))
			  middle = "contains the ";
		  else
			  middle = ", which only existed in " + subs[cSub1];
		  
		  String[] spArr2 = new String[sp2.size()];
		  Iterator<String> it2 = sp2.iterator();
		  c = 0;
		  while (it2.hasNext()) {
			  String s = it2.next();
			  if (sd3.getSpiders().contains(s)){
				  spArr2[c] = s.toString();
				  c++;
			  }
		  }
		  if (c > 0){
			  if (c == 1)
				  spiders2 = "spider ";
			  else
				  spiders2 = "spiders ";
			  spiders2 = spiders2 + spArr2[0];
			  for (int i = 1; i < c; i++)
				  spiders2 = spiders2 + ", " + spArr2[i];
		  }
		  
		  String[] shArr2 = new String[sh2.size()];
		  Iterator<Zone> it4 = sh2.iterator();
		  csh = 0;
		  while (it4.hasNext()) {
			  Zone z = it4.next();
			  if (!sh1.contains(z)){
				  shArr2[csh] = z.toString2();
				  csh++;
			  }
		  }
		  String shaded2 = "";
		  if (csh > 0){
			  if (c > 0)
				  shaded2 = " and the ";
			  if (csh == 1)
				  shaded2 = "shaded zone ";
			  else
				  shaded2 = "shaded zones ";
			  shaded2 = shaded2 + shArr2[0];
			  for (int i = 1; i < csh; i++)
				  shaded2 = shaded2 + ", " + shArr2[i];
		  }
		  if ((c > 0) || (csh > 0)){
			  shaded2 = shaded2 + ", which only existed in " + subs[cSub2];
			  if (middle.charAt(0) == ',')
				  middle = middle + ", as well as the ";
		  }
  		  
		  if ((spiders1.length() > 0) || (spiders2.length() > 0)){
			  if ((shaded1.length() > 0) || ((shaded2.length() > 0) && (shaded2.charAt(0) != ',')))
				  return "the conjunctively connected diagrams in " + (csubs[cSub1 - 1]) 
	  	    		  + " were combined in one unitary diagram. The shaded zones of the new diagram"
	  	    		  + " are those that were shaded in at least one sub-diagram and the spiders" 
	  	    		  + " are all those which were either in one or both sub-diagrams. If a zone"
	  	    		  + " had spiders in both sub-diagrams, the spiders were copied from the zone" 
	  	    		  + " with the largest number of spiders. Specifically, the new diagram "
	  	    		  + spiders1 + shaded1 + middle + spiders2 + shaded2;
			  else
				  return "the conjunctively connected diagrams in " + (csubs[cSub1 - 1]) 
	  	    		  + " were combined in one unitary diagram. The spiders in the new diagram" 
	  	    		  + " are all those which were either in one or both sub-diagrams. If a zone"
	  	    		  + " had spiders in both sub-diagrams, the spiders were copied from the zone" 
	  	    		  + " with the largest number of spiders. Specifically, the new diagram "
	  	    		  + spiders1 + shaded1 + middle + spiders2 + shaded2;
		  }
		  
		  return "the conjunctively connected diagrams in " + (csubs[cSub1 - 1]) 
  	    		  + " were combined in one unitary diagram. The shaded zones of the new diagram"
  	    		  + " are those that were shaded in at least one sub-diagram. Specifically, the new diagram "
  	    		  + spiders1 + shaded1 + middle + spiders2 + shaded2;
  	  }
  	  else if (rule.equals("Erase Spider")){
  		  //nlproof.setText(nlproof.getText() + EraseSpider.getTranslation() + "\n");
  		  //nlproof.setText(proofPanel1.getInferenceApplicationAt(proofPanel1.getInferenceApplicationCount()-1).getRuleArguments().toString());
  	  }
  	  else if (rule.equals("Add Feet")){
  		  SpiderRegionArg srArg = (SpiderRegionArg) arg;
  		  int subgoal = srArg.getSubgoalIndex();
  		  diagramNumbering(sdg1[subgoal]);
  		  String afSpider = srArg.getSpider();
  		  int afSub = srArg.getSubDiagramIndex();
  		  String afZone = srArg.getRegion().toString2();
  		  return "a foot into the zone " + afZone + " was added to spider " + afSpider + " in " + subs[afSub];
  	  }
  	  else if (rule.equals("Copy Contour")){
  		  MultipleRuleArgs mra = (MultipleRuleArgs) arg;
  		  ContourArg conArg = (ContourArg) mra.get(0);
  		  String ccContour = conArg.getContour();
  		  int ccSub = conArg.getSubDiagramIndex();
  		  int subgoal = conArg.getSubgoalIndex();
		  int parent = sdg1[subgoal].getParentIndexOf(ccSub);
		  diagramNumbering(sdg1[subgoal]);
		  int target = ccSub - 1;
		  if (parent == (ccSub - 1))
			  target = ccSub + 1;
  		  if (mra.size() > 1){
  			  String selectedContours = ccContour;
  			  for (int i = 1; i < mra.size() - 1; i++){
  				  conArg = (ContourArg) mra.get(i);
  				  ccContour = conArg.getContour();
  				  ccSub = conArg.getSubDiagramIndex();
  				  selectedContours = selectedContours + ", " + ccContour;
  			  }
  			  conArg = (ContourArg) mra.get(mra.size() - 1);
  			  ccContour = conArg.getContour();
  			  selectedContours = selectedContours + " and " + ccContour;
  			  return "contours " + selectedContours + " were copied from " + subs[ccSub]
  					  + " to " + subs[target];
  		  }
  		  else{
  			  return "contour " + ccContour + " was copied from " + subs[ccSub]
  					  + " to " + subs[target];
  		  }
  	  }
  	  else if (rule.equals("Excluded Middle")){
  		  //nlproof.setText(nlproof.getText() + ExcludedMiddle.getTranslation() + "\n");
  	  }
  	  else if (rule.equals("Introduce Contour")){
  		  MultipleRuleArgs mra = (MultipleRuleArgs) arg;
  		  SubDiagramIndexArg target = (SubDiagramIndexArg) mra.get(0);
  		  int targetSub = target.getSubDiagramIndex();
  		  ContourArg conArg = (ContourArg) mra.get(1);
  		  int subgoal = conArg.getSubgoalIndex();
  		  diagramNumbering(sdg1[subgoal]);
  		  String icContour = conArg.getContour();
  		  int icSub = conArg.getSubDiagramIndex();
  		  if (mra.size() > 2){
  			  String selectedContours = icContour;
    		//String selectedSubs = String.valueOf(ecSub);
  			  for (int i = 2; i < mra.size() - 1; i++){
  				  conArg = (ContourArg) mra.get(i);
  				  icContour = conArg.getContour();
  				  icSub = conArg.getSubDiagramIndex();
  				  selectedContours = selectedContours + ", " + icContour;
      		  	//selectedSubs = selectedSubs + ", " + ecSub;
  			  }
  			  conArg = (ContourArg) mra.get(mra.size() - 1);
  			  icContour = conArg.getContour();
  		  	//ecSub = conArg.getSubDiagramIndex();
  			  selectedContours = selectedContours + " and " + icContour;
  		  	//selectedSubs = selectedSubs + " and " + ecSub;
  			  return "contours " + selectedContours
  					  + " were introduced to " + subs[targetSub];
  		  }
  		  else{
  			  return "a new contour with label " + icContour
  					  + " was introduced to " + subs[targetSub];
  		  }	
  	  }
  	  
  	  else if (rule.equals("Erase Contour")){
  		  MultipleRuleArgs mra = (MultipleRuleArgs) arg;
  		  ContourArg conArg = (ContourArg) mra.get(0);
  		  int subgoal = conArg.getSubgoalIndex();
		  diagramNumbering(sdg1[subgoal]);
  		  String ecContour = conArg.getContour();
  		  int ecSub = conArg.getSubDiagramIndex();
  		  if (mra.size() > 1){
  			  String selectedContours = ecContour;
    		//String selectedSubs = String.valueOf(ecSub);
  			  for (int i = 1; i < mra.size() - 1; i++){
  				  conArg = (ContourArg) mra.get(i);
  				  ecContour = conArg.getContour();
  				  //ecSub = conArg.getSubDiagramIndex();
  				  selectedContours = selectedContours + ", " + ecContour;
      		  	//selectedSubs = selectedSubs + ", " + ecSub;
  			  }
  			  conArg = (ContourArg) mra.get(mra.size() - 1);
  			  ecContour = conArg.getContour();
  			  ecSub = conArg.getSubDiagramIndex();
  			  selectedContours = selectedContours + " and " + ecContour;
  		  	//selectedSubs = selectedSubs + " and " + ecSub;
  			  return "contours " + selectedContours + " were removed from " + subs[ecSub];
  		  }
  		  else{
  			  return "contour " + ecContour + " was removed from " + subs[ecSub];
  		  }
  		  /*
  		  try{
  			  String ecContour = ContourArg.getContourArgFrom(mra.get(0)).getContour();
  			  
  			  int ecSub = sdia.getSubDiagramIndex();
  			  nlproof.setText(nlproof.getText() + RemoveContour.getTranslation(ecContour, ecSub) + "\n");
  		  }
  		  catch (RuleApplicationException ex) {
  		      throw new RuntimeException(ex);
  		    }
  		  */
  	  }
  	  else if (rule.equals("Erase Shading")){
  		  MultipleRuleArgs mra = (MultipleRuleArgs) arg;
  		  ZoneArg esArg = (ZoneArg) mra.get(0);
  		  int subgoal = esArg.getSubgoalIndex();
		  diagramNumbering(sdg1[subgoal]);
  		  String esZone = esArg.getZone().toString2();
  		  int esSub = esArg.getSubDiagramIndex();
  		  return "the shading of zone " + esZone + " was removed from " + esSub;
  	  }
  	  else if (rule.equals("Remove Shaded Zone")){
  		  MultipleRuleArgs mra = (MultipleRuleArgs) arg;
  		  ZoneArg rszArg = (ZoneArg) mra.get(0);		  
  		  String rszZone = rszArg.getZone().toString2();
  		  int rszSub = rszArg.getSubDiagramIndex();
  		  return "the shaded zone " + rszZone + " was removed from sub-diagram" + rszSub;
  	  }
  	  else if (rule.equals("Copy Shading")){
  		  MultipleRuleArgs mra = (MultipleRuleArgs) arg;
  		  ZoneArg zoneArg = (ZoneArg) mra.get(0);
  		  String csZone = zoneArg.getZone().toString2();
  		  int csSub = zoneArg.getSubDiagramIndex();
  		  int subgoal = zoneArg.getSubgoalIndex();
		  diagramNumbering(sdg1[subgoal]);
  		  int parent = sdg1[subgoal].getParentIndexOf(csSub);
  		  int target = csSub - 1;
  		  if (parent == (csSub - 1))
  			  target = csSub + 1;
  		  if (mra.size() > 1){
  			  String selectedZones = csZone;
  			  for (int i = 1; i < mra.size() - 1; i++){
  				  zoneArg = (ZoneArg) mra.get(i);
  				  csZone = zoneArg.getZone().toString2();
  				  selectedZones = selectedZones + ", " + csZone;
  			  }
  			  zoneArg = (ZoneArg) mra.get(mra.size() - 1);
  			  csZone = zoneArg.getZone().toString2();
  			  selectedZones = selectedZones + " and " + csZone;
  			  return "the shading in zones  " + selectedZones + " was copied from " + subs[csSub] 
  					  + " to " + subs[target];
  		  }
  		  else{
  			  return "the shading in zone  " + csZone + " was copied from " + subs[csSub] 
  					+ " to " + subs[target];
  		  }	  
  	  }
  	  else if (rule.equals("Copy Spider")){
  		  SpiderArg csarg = (SpiderArg) arg;
  		  String csSpider = csarg.getSpider();
  		  int csSub = csarg.getSubDiagramIndex();
  		  int subgoal = csarg.getSubgoalIndex();
  		  diagramNumbering(sdg1[subgoal]);
		  int parent = sdg1[subgoal].getParentIndexOf(csSub);
		  int target = csSub - 1;
		  if (parent == (csSub - 1))
			  target = csSub + 1;
  		  return "spider " + csSpider + " from " + subs[csSub] 
  				  + " was copied to " + subs[target];
  	  }
  	  else if (rule.equals("Conjunction Elimination")){
  		  MultipleRuleArgs mra = (MultipleRuleArgs) arg;
  		  SubDiagramIndexArg cearg1 = (SubDiagramIndexArg) mra.get(0);
  		  SubDiagramIndexArg cearg2 = (SubDiagramIndexArg) mra.get(1);
  		  int subgoal = cearg1.getSubgoalIndex();
		  diagramNumbering(sdg1[subgoal]);
		  conjunctionNumbering(sdg1[subgoal]);
		  int ceSub1 = cearg1.getSubDiagramIndex();
		  int ceSub2 = cearg2.getSubDiagramIndex();
		  return "" + csubs[ceSub1] + " is a conjunction of sub-diagrams. Since" +
				  " conjunction requires that both diagrams are true, we can use any diagram" +
				  " on its own. In this case, the diagram was reduced to " + subs[ceSub2] 
				  + ".";
	  }
  	  else if (rule.equals("Double Negation Elimination")){
  		  SubDiagramIndexArg dnearg = (SubDiagramIndexArg) arg;
  		  int subgoal = dnearg.getSubgoalIndex();
		  diagramNumbering(sdg1[subgoal]);
		  int dneSub = dnearg.getSubDiagramIndex();
		  return "double negation elimination was used in " + subs[dneSub];
	  }
  	  else if (rule.equals("Double Negation Introduction")){
  		  SubDiagramIndexArg dniarg = (SubDiagramIndexArg) arg;
  		  int subgoal = dniarg.getSubgoalIndex();
		  diagramNumbering(sdg1[subgoal]);
		  int dniSub = dniarg.getSubDiagramIndex();
		  return "double negation was introduced in " + subs[dniSub];
	  }
  	  else if ((rule.equals("Idempotency")) || (rule.equals("Idempotency (Syntactic)"))){
  		  SubDiagramIndexArg iarg = (SubDiagramIndexArg) arg;
  		  int subgoal = iarg.getSubgoalIndex();
		  conjunctionNumbering(sdg1[subgoal]);
		  int iSub = iarg.getSubDiagramIndex();
		  return "" + csubs[iSub] + " is a conjunction of two equal sub-diagrams."
				  + " Therefore, we can reduce this to one unitary sub-diagram using the"
				  + " Idempotency rule.";
	  }
	  else if ((rule.equals("Implication Tautology")) || (rule.equals("Trivial Implication Tautology"))){
		  SubDiagramIndexArg itarg = (SubDiagramIndexArg) arg;
		  int itSub = itarg.getSubDiagramIndex();
		  return "implication tautology was used to finish the proof";
	  }
	  else if (rule.equals("Negation elimination")){
		  SubDiagramIndexArg nearg = (SubDiagramIndexArg) arg;
		  int neSub = nearg.getSubDiagramIndex() + 1;
		  int subgoal = nearg.getSubgoalIndex();
		  diagramNumbering(sdg1[subgoal]);
  		  PrimarySpiderDiagram sd = (PrimarySpiderDiagram) sdg1[subgoal].getSubDiagramAt(neSub);
  		  int spiders = sd.getSpidersCount();
  		  int shadedZones = sd.getShadedZonesCount();
  		  String firstSpider = sd.getSpiders().first();
  		  String zone = sd.getSpiderHabitat(firstSpider).toString2();
  		  if (shadedZones == 0){
  			  if (spiders == 1)
  				return "negation elimination was used in " + subs[neSub - 1]
  					  + ". The negation states that there must be strictly less than one" 
  					  + " spider in zone " + zone + ". Therefore, the zone " + zone 
  					  + " in the resulting diagram, is shaded and contains no spiders."; 
  		  
  			  return "negation elimination was used in " + subs[neSub - 1]
				  + ". The negation states that there must be less than " + spiders 
				  + " spiders in zone " + zone + ". Therefore, " + spiders 
				  + " disjunctively connected sub-diagrams were created ranging from 0 to "
				  + (spiders - 1) + " spiders in zone " + zone;
  		  }
  		  else
  			  return "negation elimination was used in " + subs[neSub - 1]
  				  + ". The negation states that there can't be exactly " + spiders 
  				  + " spiders in zone " + zone + ". Therefore, " + (spiders + 1)
  				  + " disjunctively connected sub-diagrams were created ranging from 0 to "
				  + (spiders - 1) + " spiders in zone " + zone + " for the first " + spiders + " diagrams and " 
  				  + (spiders + 1) + " spiders for the last diagram where the zone is not shaded.";
	  }
	  else if (rule.equals("Split Conjunction")){
		  SubDiagramIndexArg scarg = (SubDiagramIndexArg) arg;
		  int subgoal = scarg.getSubgoalIndex();
		  conjunctionNumbering(sdg1[subgoal]);
		  int scSub = scarg.getSubDiagramIndex();
		  return "conjunction split was applied in " + csubs[scSub]
				  + ". The result is two new subgoals for each of the two"
				  + " conjunctively connected sub-diagrams";
	  }
  	  else if (rule.equals("Split Disjunction")){
  		  SubDiagramIndexArg sdarg = (SubDiagramIndexArg) arg;
  		  int subgoal = sdarg.getSubgoalIndex();
		  conjunctionNumbering(sdg1[subgoal]);
		  int sdSub = sdarg.getSubDiagramIndex();
		  return "disjunction split was applied in " + csubs[sdSub]
				  + ". The result is two new subgoals for each of the two"
				  + " disjunctively connected sub-diagrams";
	  }
  	  else if (rule.equals("Introduce All Shaded Zones")){
  		  String[] newZones = new String[100]; 
		  int zonesCount = 0;
  		  
  		  for (int i = 0; i < sdg1.length; i++){
			  for (int j = 1; j <= sdg1[i].getSubDiagramAt(1).getSubDiagramCount(); j++){
				  if (sdg1[i].getSubDiagramAt(j).getSubDiagramCount() == 1){
					  PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg1[i].getSubDiagramAt(j);
					  PrimarySpiderDiagram sd2 = null;
					  SortedSet<Zone> prZones1 = sd1.getPresentZones();
					  if (sdg2 == null)
						  sd2 = (PrimarySpiderDiagram) sdg1[i].getSubDiagramAt(sdg1[i].getSubDiagramAt(1).getSubDiagramCount() + j);
					  else
						  sd2 = (PrimarySpiderDiagram) sdg2[i].getSubDiagramAt(j);
					  SortedSet<Zone> prZones2 = sd2.getPresentZones();
					  
					  String[] subZones = new String[100]; 
					  int subCount = 0;
					  
					  Iterator<Zone> it = prZones2.iterator();
					  while (it.hasNext()) {
						  Zone z = it.next();
						  if (!prZones1.contains(z)){
							  subZones[subCount] = z.toString2();
							  subCount++;
						  }
							
					  }
					  if (subCount > 0){
	  					  String subString = subZones[0];
		  		  		  for (int l = 1; l < subCount - 1; l++){
		  		  			  subString = subString + ", " + subZones[l];
		  		  		  }
		  		  		  if (subCount > 1)
		  		  			  subString = subString + " and " + subZones[subCount - 1];
		  		  		  subString = subString + " from sub-diagram " + j;
		  		  		  
		  		  		  newZones[zonesCount] = subString;
		  		  		  zonesCount++;
					  }
				  }
			  }
		  }
		  String zoneString = newZones[0];
		  for (int i = 1; i < zonesCount - 1; i++){
			  zoneString = zoneString + ", " + newZones[i];
		  }
		  if (zonesCount > 1)
			  zoneString = zoneString + " and " + newZones[zonesCount - 1];
  		
  		  return "all the missing zones were introduced in the diagram as shaded zones.";
  		  
  	  }
  	  else if (rule.equals("Introduce All Shaded Zones (Deepest)")){
  		  String[] newZones = new String[100]; 
		  int zonesCount = 0;

  		  for (int i = 0; i < sdg1.length; i++){
  			  for (int j = 0; j < sdg1[i].getSubDiagramCount(); j++){
  				  if (sdg1[i].getSubDiagramAt(j).getSubDiagramCount() == 1){
  					  diagramNumbering(sdg1[i]);
  					  PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg1[i].getSubDiagramAt(j);
  					  SortedSet<Zone> prZones1 = sd1.getPresentZones();
  					  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg2[i].getSubDiagramAt(j);
  					  SortedSet<Zone> prZones2 = sd2.getPresentZones();
  					  
  					  String[] subZones = new String[100]; 
  					  int subCount = 0;
  					  
  					  Iterator<Zone> it = prZones2.iterator();
  					  while (it.hasNext()) {
  						  Zone z = it.next();
  						  if (!prZones1.contains(z)){
  							  subZones[subCount] = z.toString2();
  							  subCount++;
  						  }
  							
  					  }
  					  if (subCount > 0){
	  					  String subString = subZones[0];
		  		  		  for (int l = 1; l < subCount - 1; l++){
		  		  			  subString = subString + ", " + subZones[l];
		  		  		  }
		  		  		  if (subCount > 1)
		  		  			  subString = subString + " and " + subZones[subCount - 1];
		  		  		  subString = subString + " from " + subs[j];
		  		  		  newZones[zonesCount] = subString;
		  		  		  zonesCount++;
  					  }
  				  }
  			  }
  		  }
  		  String zoneString = newZones[0];
  		  for (int i = 1; i < zonesCount - 1; i++){
  			  zoneString = zoneString + ", " + newZones[i];
  		  }
  		  if (zonesCount > 1)
  			  zoneString = zoneString + " and " + newZones[zonesCount - 1];
  		

  		  return "All the missing zones in conjunctively connected sub-diagrams"
  					+ " were introduced as shaded zones. This includes the zones " + zoneString;
 		  
 	  }
  	  else if (rule.equals("Introduce All Contours")){
  		  String[] newContours = new String[100]; 
		  int conCount = 0;
  		  
  		  for (int i = 0; i < sdg1.length; i++){
			  for (int j = 0; j < sdg1[i].getSubDiagramCount(); j++){
				  if (sdg1[i].getSubDiagramAt(j).getSubDiagramCount() == 1){
					  diagramNumbering(sdg1[i]);
					  PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg1[i].getSubDiagramAt(j);
					  SortedSet<String> contours1 = sd1.getAllContours();
					  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg2[i].getSubDiagramAt(j);
					  SortedSet<String> contours2 = sd2.getAllContours();
					  
					  String[] subContours = new String[100]; 
					  int subCount = 0;
					  
					  Iterator<String> it = contours2.iterator();
					  while (it.hasNext()) {
						  String s = it.next();
						  if (!contours1.contains(s)){
							  subContours[subCount] = s;
							  subCount++;
						  }
							
					  }
					  if (subCount > 0){
	  					  String subString = "contour";
	  					  if (subCount > 1)
	  						  subString = subString + "s";
	  					  subString = subString + " " + subContours[0];
		  		  		  for (int l = 1; l < subCount - 1; l++){
		  		  			  subString = subString + ", " + subContours[l];
		  		  		  }
		  		  		  if (subCount > 1)
		  		  			  subString = subString + " and " + subContours[subCount - 1];
		  		  		  subString = subString + " in " + subs[j];
		  		  		  newContours[conCount] = subString;
		  		  		  conCount++;
					  }
				  }
			  }
		  }
  		  
  		  String conString = newContours[0];
		  for (int i = 1; i < conCount - 1; i++){
			  conString = conString + ", " + newContours[i];
		  }
		  if (conCount > 1)
			  conString = conString + " and " + newContours[conCount - 1];
		  
  		  return "For each sub-diagram, every contour from the union of all contours of"
  				  + " the diagram was introduced if it was not present. Specifically,"
  				  + " the introduced contours are " + conString + ".";
 		  
 	  }
  	  else if (rule.equals("Introduce All Contours (Deepest)")){
  		  String[] newContours = new String[100]; 
		  int conCount = 0;
		  
		  for (int i = 0; i < sdg1.length; i++){
			  for (int j = 0; j < sdg1[i].getSubDiagramCount(); j++){
				  if (sdg1[i].getSubDiagramAt(j).getSubDiagramCount() == 1){
					  diagramNumbering(sdg1[i]);
					  PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg1[i].getSubDiagramAt(j);
					  SortedSet<String> contours1 = sd1.getAllContours();
					  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg2[i].getSubDiagramAt(j);
					  SortedSet<String> contours2 = sd2.getAllContours();
					  
					  String[] subContours = new String[100]; 
					  int subCount = 0;
					  
					  Iterator<String> it = contours2.iterator();
					  while (it.hasNext()) {
						  String s = it.next();
						  if (!contours1.contains(s)){
							  subContours[subCount] = s;
							  subCount++;
						  }
							
					  }
					  if (subCount > 0){
	  					  String subString = "contour";
	  					  if (subCount > 1)
	  						  subString = subString + "s";
	  					  subString = subString + " " + subContours[0];
		  		  		  for (int l = 1; l < subCount - 1; l++){
		  		  			  subString = subString + ", " + subContours[l];
		  		  		  }
		  		  		  if (subCount > 1)
		  		  			  subString = subString + " and " + subContours[subCount - 1];
		  		  		  subString = subString + " in " + subs[j];
		  		  		  newContours[conCount] = subString;
		  		  		  conCount++;
					  }
				  }
			  }
		  }
		  
		  String conString = newContours[0];
		  for (int i = 1; i < conCount - 1; i++){
			  conString = conString + ", " + newContours[i];
		  }
		  if (conCount > 1)
			  conString = conString + " and " + newContours[conCount - 1];
		  
		  return "For each conjunction of unitary sub-diagrams, every contour"
				  + " from the union of all contours of the conjunction was introduced"
				  + " if it was not present. Specifically, the introduced contours are " 
				  + conString + ".";
		  
	  }
  	  else if (rule.equals("Combine All Diagrams")){
  		  int i = 0, c = 0;		 
		  int[] subArray = new int[apps.size()];
		  diagramNumbering(sdg1[0]);
		  conjunctionNumbering(sdg1[0]);
		  
		  Iterator<InferenceApplication> it = apps.iterator();
		  while (it.hasNext()){
			  InferenceApplication ia = it.next();
			  SubDiagramIndexArg appArg = (SubDiagramIndexArg) ia.getRuleArguments();
			  subArray[c] = appArg.getSubDiagramIndex();
			  c++;
		  }
		  int comCount = apps.size();
		  
		  if (comCount == 1)
			  return "The conjunctively connected sub-diagrams in " + csubs[subArray[0]]
				  + " were combined into one unitary diagram.";
		  
		  
		  String subString = "" + subArray[0];
		  for (i = 1; i < comCount - 1; i++){
			  subString = subString + ", " + csubs[subArray[i]];
		  }
		  if (comCount > 1)
			  subString = subString + " and " + csubs[subArray[comCount - 1]];
  		  
  		  return "For every conjunction of unitary diagrams where combination"
  				  + " was possible, the sub-diagrams were combined into one unitary diagram."
  				  + " These are sub-diagrams " + subString + ".";
		  
	  }
  	  else if (rule.equals("Prepare For Copy Shading")){
  		  String[] newZones = new String[100]; 
		  int zonesCount = 0;

		  for (int i = 0; i < sdg1.length; i++){
			  for (int j = 0; j < sdg1[i].getSubDiagramCount(); j++){
				  if (sdg1[i].getSubDiagramAt(j).getSubDiagramCount() == 1){
					  diagramNumbering(sdg1[i]);
					  PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg1[i].getSubDiagramAt(j);
					  SortedSet<Zone> prZones1 = sd1.getPresentZones();
					  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg2[i].getSubDiagramAt(j);
					  SortedSet<Zone> prZones2 = sd2.getPresentZones();
					  
					  String[] subZones = new String[100]; 
					  int subCount = 0;
					  
					  Iterator<Zone> it = prZones2.iterator();
					  while (it.hasNext()) {
						  Zone z = it.next();
						  if (!prZones1.contains(z)){
							  subZones[subCount] = z.toString2();
							  subCount++;
						  }
							
					  }
					  if (subCount > 0){
	  					  String subString = subZones[0];
		  		  		  for (int l = 1; l < subCount - 1; l++){
		  		  			  subString = subString + ", " + subZones[l];
		  		  		  }
		  		  		  if (subCount > 1)
		  		  			  subString = subString + " and " + subZones[subCount - 1];
		  		  		  subString = subString + " in " + subs[j];
		  		  		  newZones[zonesCount] = subString;
		  		  		  zonesCount++;
					  }
				  }
			  }
		  }
		  String zoneString = newZones[0];
		  for (int i = 1; i < zonesCount - 1; i++){
			  zoneString = zoneString + ", " + newZones[i];
		  }
		  if (zonesCount > 1){
			  zoneString = zoneString + " and " + newZones[zonesCount - 1];
  		  
			  return "Shaded zones were introduced into diagrams"
  		  		+ " that are part of a conjunction, and between which shading can be copied."
  		  		+ " Specifically, the introduced zones are: " + zoneString;
		  }
		  
		  return "A shaded zone was introduced to allow for Copy Shading."
	  		+ " Specifically, the introduced zone is " + zoneString;
 	  }
 	  else if (rule.equals("Prepare For Copy Contours")){
 		  String[] newZones = new String[100]; 
		  int zonesCount = 0;

		  for (int i = 0; i < sdg1.length; i++){
			  for (int j = 0; j < sdg1[i].getSubDiagramCount(); j++){
				  if (sdg1[i].getSubDiagramAt(j).getSubDiagramCount() == 1){
					  diagramNumbering(sdg1[i]);
					  PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg1[i].getSubDiagramAt(j);
					  SortedSet<Zone> prZones1 = sd1.getPresentZones();
					  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg2[i].getSubDiagramAt(j);
					  SortedSet<Zone> prZones2 = sd2.getPresentZones();
					  
					  String[] subZones = new String[100]; 
					  int subCount = 0;
					  
					  Iterator<Zone> it = prZones1.iterator();
					  while (it.hasNext()) {
						  Zone z = it.next();
						  if (!prZones2.contains(z)){
							  subZones[subCount] = z.toString2();
							  subCount++;
						  }
							
					  }
					  if (subCount > 0){
	  					  String subString = subZones[0];
		  		  		  for (int l = 1; l < subCount - 1; l++){
		  		  			  subString = ", " + subString + subZones[l];
		  		  		  }
		  		  		  if (subCount > 1)
		  		  			  subString = subString + " and " + subZones[subCount - 1];
		  		  		  subString = subString + " from " + subs[j];
		  		  		  newZones[zonesCount] = subString;
		  		  		  zonesCount++;
					  }
				  }
			  }
		  }
		  String zoneString = newZones[0];
		  for (int i = 1; i < zonesCount - 1; i++){
			  zoneString = zoneString + ", " + newZones[i];
		  }
		  if (zonesCount > 1)
			  zoneString = zoneString + " and " + newZones[zonesCount - 1];
 		  		
 		  return "Shaded zones were removed from diagrams"
   		  		+ " that are part of a conjunction, and between which contours can be copied"
   		  		+ " Specifically, these zones are: " + zoneString;
		  
	  }
 	  else if (rule.equals("Match Conclusion")){
		  int i = 0;
		  Goals newGoals = initGoals;
		  SpiderDiagram[] sd = null;
		  
		  int c = 0;
		  int sdid = sdg1[0].getSubDiagramAt(1).getSubDiagramCount() + 1;
 		  int num = sdg1[0].getSubDiagramAt(0).getSubDiagramCount();
 		  if (num != 3)
 			  return "-";
 		  PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg1[0].getSubDiagramAt(1);
		  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg1[0].getSubDiagramAt(2);
		  
		  String[] appArray = new String[apps.size()];
		  RuleArg[] argArray = new RuleArg[apps.size()];
		  Iterator<InferenceApplication> it = apps.iterator();
		  while (it.hasNext()){
			  InferenceApplication ia = it.next();
			  String appRule = ia.getInference().toString();
			  RuleArg appArg = ia.getRuleArguments();
			  appArray[c] = appRule;
			  argArray[c] = appArg;
			  c++;
		  }
		  
		  boolean introContour = false;
		  boolean eraseContour = false;
		  boolean introShaded = false;
		  boolean eraseShading = false;
		  
		  String translation = "";
		  if ((appArray[0].equals("Introduce Contour")) || (appArray[0].equals("Erase Contour")))
 			  translation = "Initially, ";
		  
		  int count = 0;
		  String temp = "";
		  while ((i < appArray.length) && (appArray[i].equals("Introduce Contour"))){
			  count++;
			  MultipleRuleArgs mra = (MultipleRuleArgs) argArray[i];
	  		  ContourArg conArg = (ContourArg) mra.get(0);
	  		  if (count == 1)
	  			  temp += conArg.getContour();
	  		  else{
	  			if ((i == appArray.length - 1) || (!appArray[i+1].equals("Introduce Contour")))
	  				  temp += " and " + conArg.getContour();
	  			  else
	  				temp += ", " + conArg.getContour();
	  		  }
	  		  		
			  try {
 					 newGoals = apps.get(i).applyTo(newGoals).getGoals();
 	 			 } catch (RuleApplicationException e) {
 	 				 e.printStackTrace();
 	 			 }
 	 			 i++;
		  }
		  sd = newGoals.getGoals().toArray(new SpiderDiagram[newGoals.getGoalsCount()]); 
		  sdg1 = sd;
		  if (count > 0){
			  introContour = true;
			  if (count == 1)
				  translation = translation + "contour " + temp + " was introduced to the antecedent. ";
			  else
				  translation = translation + "contours " + temp + " were introduced to the antecedent. ";
		  }
		  
		  
  	  	  count = 0;
		  temp = "";
		  while ((i < appArray.length) && (appArray[i].equals("Erase Contour"))){
			  count++;
			  MultipleRuleArgs mra = (MultipleRuleArgs) argArray[i];
	  		  ContourArg conArg = (ContourArg) mra.get(0);
	  		  if (count == 1)
	  			  temp += conArg.getContour();
	  		  else{
	  			  if ((i == appArray.length - 1) || (!appArray[i+1].equals("Erase Contour")))
	  				  temp += " and " + conArg.getContour();
	  			  else
	  				temp += ", " + conArg.getContour();
	  		  }
	  		  		
			  try {
					 newGoals = apps.get(i).applyTo(newGoals).getGoals();
	 			 } catch (RuleApplicationException e) {
	 				 e.printStackTrace();
	 			 }
	 			 i++;
		  }
		  sd = newGoals.getGoals().toArray(new SpiderDiagram[newGoals.getGoalsCount()]); 
		  sdg1 = sd;
		  if (count > 0){
			  eraseContour = true;
			  if (introContour)
				  translation += "Then, ";
		  	  if (count == 1)
		  		  translation = translation + "contour " + temp + " was removed";
		  	  else
		  		  translation = translation + "contours " + temp + " were removed";
		  	  if (!introContour)
		  		  translation += " from the antecedent. ";
		  	  else
		  		  translation += ". ";
		  }
		  
		  if (i < appArray.length - 1)
			  translation += "To match the antecedent with the consequent, the shaded zones had to match. Therefore, ";
		  count = 0;
		  temp = "";
		  while ((i < appArray.length) && (appArray[i].equals("Introduce Shaded Zone"))){
			  count++;
			  MultipleRuleArgs mra = (MultipleRuleArgs) argArray[i];
	  		  ZoneArg zoneArg = (ZoneArg) mra.get(0);
	  		  if (count == 1)
	  			  temp += zoneArg.getZone().toString2();
	  		  else{
	  			if ((i == appArray.length - 1) || (!appArray[i+1].equals("Introduce Shaded Zone")))
	  				  temp += " and " + zoneArg.getZone().toString2();
	  			  else
	  				temp += ", " + zoneArg.getZone().toString2();
	  		  }
	  		  		
			  try {
 					 newGoals = apps.get(i).applyTo(newGoals).getGoals();
 	 			 } catch (RuleApplicationException e) {
 	 				 e.printStackTrace();
 	 			 }
 	 			 i++;
		  }
		  sd = newGoals.getGoals().toArray(new SpiderDiagram[newGoals.getGoalsCount()]); 
		  sdg1 = sd;
		  if (count > 0){
			  introShaded = true;
			  if (count == 1)
				  translation += "the shaded zone " + temp + " was introduced. ";
			  else
				  translation += "the shaded zones " + temp + " were introduced. ";
		  }
		  
		  
  	  	  count = 0;
		  temp = "";
		  while ((i < appArray.length) && (appArray[i].equals("Erase Shading"))){
			  count++;
			  MultipleRuleArgs mra = (MultipleRuleArgs) argArray[i];
	  		  ZoneArg zoneArg = (ZoneArg) mra.get(0);
	  		  if (count == 1)
	  			  temp += zoneArg.getZone().toString2();
	  		  else{
	  			if ((i == appArray.length - 1) || (!appArray[i+1].equals("Erase Shading")))
	  				  temp += " and " + zoneArg.getZone().toString2();
	  			  else
	  				temp += ", " + zoneArg.getZone().toString2();
	  		  }
	  		  		
			  try {
					 newGoals = apps.get(i).applyTo(newGoals).getGoals();
	 			 } catch (RuleApplicationException e) {
	 				 e.printStackTrace();
	 			 }
	 			 i++;
		  }
		  sd = newGoals.getGoals().toArray(new SpiderDiagram[newGoals.getGoalsCount()]); 
		  sdg1 = sd;
		  if (count > 0){
			  eraseShading = true;
			  if (introShaded)
				  translation += "Then, ";
		  	  if (count == 1)
		  		  translation = translation + "the shading of zone " + temp + " was erased. ";
		  	  else
		  		  translation = translation + "the shading of zones " + temp + " were erased. ";
		  }
		  
		  count = 0;
		  temp = "";
		  while ((i < appArray.length) && (appArray[i].equals("Remove Shaded Zone"))){
			  count++;
			  MultipleRuleArgs mra = (MultipleRuleArgs) argArray[i];
	  		  ZoneArg zoneArg = (ZoneArg) mra.get(0);
	  		  if (count == 1)
	  			  temp += zoneArg.getZone().toString2();
	  		  else{
	  			if ((i == appArray.length - 1) || (!appArray[i+1].equals("Remove Shaded Zone")))
	  				  temp += " and " + zoneArg.getZone().toString2();
	  			  else
	  				temp += ", " + zoneArg.getZone().toString2();
	  		  }
	  		  		
			  try {
					 newGoals = apps.get(i).applyTo(newGoals).getGoals();
	 			 } catch (RuleApplicationException e) {
	 				 e.printStackTrace();
	 			 }
	 			 i++;
		  }
		  sd = newGoals.getGoals().toArray(new SpiderDiagram[newGoals.getGoalsCount()]); 
		  sdg1 = sd;
		  if (count > 0){
			  if ((introShaded) && (eraseShading))
				  translation += "Finally, ";
			  else if ((introShaded) || (eraseShading))
				  translation += "Then, ";
		  	  if (count == 1)
		  		  translation += "the shaded zone " + temp + " was removed. ";
		  	  else
		  		  translation += "the shaded zones " + temp + " were removed. ";
		  }
		  
		  if (sdg2 == null)
			  translation += "Since the resulting diagrams were equal, the proof is finished.";
  	  	  return translation;
		  
	  }
 	  else if (rule.equals("Copy Contours")){
 		  String[] newContours = new String[100]; 
		  int conCount = 0;
		  String re = "";
		 
		  for (int i = 0; i < sdg1.length; i++){
			  diagramNumbering(sdg1[i]);
			  int com1[] = new int[sdg2[i].getSubDiagramCount()];
			  int com2[] = new int[sdg2[i].getSubDiagramCount()];
			  int f = 0, t = 1;
			  com1[0] = 1;
			  com2[0] = 1;
			  while ((f < t)){
				  if (sdg1[i].getSubDiagramAt(com1[f]).getSubDiagramCount() > 1){
					  CompoundSpiderDiagram csdg = (CompoundSpiderDiagram) sdg1[i].getSubDiagramAt(com1[f]);
					  if ((csdg.getOperator().getName() == "op &") &&
						(sdg2[i].getSubDiagramAt(com2[f]).getSubDiagramCount() == 1))
						  f++;
					  else if ((csdg.getOperator().getName() == "op &") &&
						(sdg2[i].getSubDiagramAt(com2[f]).getSubDiagramCount() > 1)){
						  t++;
						  com1[f]++;
						  com2[f]++;
						  f++;
						  com1[f] = com1[f - 1] + sdg1[i].getSubDiagramAt(com1[f-1]).getSubDiagramCount();
						  com2[f] = com2[f - 1] + sdg2[i].getSubDiagramAt(com2[f-1]).getSubDiagramCount();
					  }
				  }
				  else
					  f++;
			  }
			  String comA = "";
			  String comB = "";
			  for (int z = 0; z < f; z ++)
				  comA = comA + com1[z] + " ";
			  for (int z = 0; z < f; z ++)
				  comB = comB + com2[z] + " ";
			  re = comA + "\n" + comB;
			  
			  
			  for (int j = 0; j < f; j++){
				  if (sdg1[i].getSubDiagramAt(com1[j]).getSubDiagramCount() <= 1){
					  PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg1[i].getSubDiagramAt(com1[j]);
					  SortedSet<String> contours1 = sd1.getAllContours();
					  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg2[i].getSubDiagramAt(com2[j]);
					  SortedSet<String> contours2 = sd2.getAllContours();
					  
					  String[] subContours = new String[100]; 
					  int subCount = 0;
					  
					  Iterator<String> it = contours2.iterator();
					  while (it.hasNext()) {
						  String s = it.next();
						  if (!contours1.contains(s)){
							  subContours[subCount] = s;
							  subCount++;
						  }
							
					  }
					  if (subCount > 0){
	  					  String subString = "the contour";
	  					  if (subCount > 1)
	  						  subString = subString + "s";
	  					  subString = subString + " " + subContours[0];
		  		  		  for (int l = 1; l < subCount - 1; l++){
		  		  			  subString = ", " + subString + subContours[l];
		  		  		  }
		  		  		  if (subCount > 1)
		  		  			  subString = subString + " and " + subContours[subCount - 1]
		  		  					+ " were copied to " + subs[com1[j]];
		  		  		  else 
		  		  			  subString = subString + " was copied to " + subs[com1[j]];
		  		  		  newContours[conCount] = subString;
		  		  		  conCount++;
					  }
				  }
				  else if (sdg2[i].getSubDiagramAt(com2[j]).getSubDiagramCount() <= 1){
					  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg2[i].getSubDiagramAt(com2[j]);
					  SortedSet<String> contours2 = sd2.getAllContours();
					  
					  String[] subContours = new String[100]; 
					  int subCount = 0;
					  
					  Iterator<String> it = contours2.iterator();
					  while (it.hasNext()) {
						  String s = it.next();
						  subContours[subCount] = s;
						  subCount++;
					  }
					  if (subCount > 0){
	  					  String subString = "the sub-diagrams of " + subs[com1[j]] 
	  							  + " were combined in one diagram which now includes contour";
	  					  if (subCount > 1)
	  						  subString = subString + "s";
	  					  subString = subString + " " + subContours[0];
		  		  		  for (int l = 1; l < subCount - 1; l++){
		  		  			  subString = subString + ", " + subContours[l];
		  		  		  }
		  		  		  if (subCount > 1)
		  		  			  subString = subString + " and " + subContours[subCount - 1];
		  		  		  newContours[conCount] = subString;
		  		  		  conCount++;
					  }
				  }
			  }
		  }
		  
		  
		  String conString = newContours[0];
		  for (int i = 1; i < conCount - 1; i++){
			  conString = conString + ", " + newContours[i];
		  }
		  if (conCount > 1)
			  conString = conString + " and " + newContours[conCount - 1];
		  
		  String translation = "";
		  if (sdg1[0].getSubDiagramAt(0).getSubDiagramCount() == sdg2[0].getSubDiagramAt(0).getSubDiagramCount())
			  translation = "For each conjunction of unitary sub-diagrams, every contour"
				  + " that was missing from one of the two, was copied to the"
				  + " other if possible.";
		  
		  else
			  translation = "For each conjunction of unitary sub-diagrams, every contour"
			  + " that was missing from one of the two, was copied to the"
			  + " other if possible. Where this resulted in two identical diagrams,"
			  + " those were combined in one.";
		  
		  if (initGoals != null)
			  translation += " Specifically, " + conString + ".";
		
		  return translation;
		  
	  }
 	  else if (rule.equals("Propagate Shading")){
 		  String[] newZones = new String[100]; 
		  int zonesCount = 0;
		  String re = "";
		 
		  for (int i = 0; i < sdg1.length; i++){
			  diagramNumbering(sdg1[i]);
			  int com1[] = new int[sdg2[i].getSubDiagramCount()];
			  int com2[] = new int[sdg2[i].getSubDiagramCount()];
			  int f = 0, t = 1;
			  Queue<Integer> q1 = new LinkedList<Integer>();
			  Queue<Integer> q2 = new LinkedList<Integer>();
			  q1.add(1);
			  q2.add(1);
			  for (int ii = 0; ii < com1.length; ii++)
				  com1[ii] = com2[ii] = 100;
			  while ((f < t)){
				  if (sdg1[i].getSubDiagramAt(q1.peek()).getSubDiagramCount() > 1){
					  CompoundSpiderDiagram csdg = (CompoundSpiderDiagram) sdg1[i].getSubDiagramAt(q1.peek());
					  if ((csdg.getOperator().getName() == "op &") &&
						(sdg2[i].getSubDiagramAt(q2.peek()).getSubDiagramCount() == 1)){
						  com1[f] = q1.remove();
						  com2[f] = q2.remove();
						  f++;
					  }
					  else if ((csdg.getOperator().getName() == "op &") &&
						(sdg2[i].getSubDiagramAt(q2.peek()).getSubDiagramCount() > 1)){
						  t++;
						  int h1 = q1.remove() + 1;
						  int h2 = q2.remove() + 1;
						  q1.add(h1);
						  q2.add(h2);
						  q1.add(h1 + sdg1[i].getSubDiagramAt(h1).getSubDiagramCount());
						  q2.add(h2 + sdg2[i].getSubDiagramAt(h2).getSubDiagramCount());
					  }
				  }
				  else{
					  com1[f] = q1.remove();
					  com2[f] = q2.remove();
					  f++;
				  }	  
			  }
			  Arrays.sort(com1);
			  Arrays.sort(com2);
			  
			  
			  String comA = "";
			  String comB = "";
			  for (int z = 0; z < f; z ++)
				  comA = comA + com1[z] + " ";
			  for (int z = 0; z < f; z ++)
				  comB = comB + com2[z] + " ";
			  re = comA + "\n" + comB;
			  
			  
			  
			  for (int j = 0; j < f; j++){
				  if (sdg1[i].getSubDiagramAt(com1[j]).getSubDiagramCount() <= 1){
					  PrimarySpiderDiagram sd1 = (PrimarySpiderDiagram) sdg1[i].getSubDiagramAt(com1[j]);
					  SortedSet<Zone> zones1 = sd1.getShadedZones();
					  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg2[i].getSubDiagramAt(com2[j]);
					  SortedSet<Zone> zones2 = sd2.getShadedZones();
					  
					  String[] subZones = new String[100]; 
					  int subCount = 0;
					  
					  Iterator<Zone> it = zones2.iterator();
					  while (it.hasNext()) {
						  Zone z = it.next();
						  if (!zones1.contains(z)){
							  subZones[subCount] = z.toString2();
							  subCount++;
						  }
							
					  }
					  if (subCount > 0){
	  					  String subString = "the shaded zone";
	  					  if (subCount > 1)
	  						  subString = subString + "s";
	  					  subString = subString + " " + subZones[0];
		  		  		  for (int l = 1; l < subCount - 1; l++){
		  		  			  subString = subString + ", " + subZones[l];
		  		  		  }
		  		  		  if (subCount > 1)
		  		  			  subString = subString + " and " + subZones[subCount - 1]
		  		  					+ " were copied to " + subs[com1[j]];
		  		  		  else
		  		  			  subString = subString + " was copied to " + subs[com1[j]];
		  		  		  newZones[zonesCount] = subString;
		  		  		  zonesCount++;
					  }
				  }
				  else if (sdg2[i].getSubDiagramAt(com2[j]).getSubDiagramCount() <= 1){
					  PrimarySpiderDiagram sd2 = (PrimarySpiderDiagram) sdg2[i].getSubDiagramAt(com2[j]);
					  SortedSet<Zone> zones2 = sd2.getShadedZones();
					  
					  String[] subZones = new String[100]; 
					  int subCount = 0;
					  
					  Iterator<Zone> it = zones2.iterator();
					  while (it.hasNext()) {
						  Zone z = it.next();
						  subZones[subCount] = z.toString2();
						  subCount++;
					  }
					  if (subCount > 0){
	  					  String subString = "the sub-diagrams of " + subs[com1[j]] 
	  							  + " were combined in one diagram which now includes shaded zone";
	  					  if (subCount > 1)
	  						  subString = subString + "s";
	  					  subString = subString + " " + subZones[0];
		  		  		  for (int l = 1; l < subCount - 1; l++){
		  		  			  subString = subString + ", " + subZones[l];
		  		  		  }
		  		  		  if (subCount > 1)
		  		  			  subString = subString + " and " + subZones[subCount - 1];
		  		  		  newZones[zonesCount] = subString;
		  		  		  zonesCount++;
					  }
				  }
			  }
		  }
		  
		  
 		  String zoneString = newZones[0];
 		  for (int i = 1; i < zonesCount - 1; i++){
 			  zoneString = zoneString + ", " + newZones[i];
 		  }
 		  if (zonesCount > 1)
 			  zoneString = zoneString + " and " + newZones[zonesCount - 1];
 		  
 		  String translation = "";
 		
 		  if (sdg1[0].getSubDiagramAt(0).getSubDiagramCount() == sdg2[0].getSubDiagramAt(0).getSubDiagramCount())
	 		  translation = "For each conjunction of unitary sub-diagrams, the shading of every zone"
			  + " that was shaded in only one of the two, was copied to the"
			  + " other if possible. Furthermore, the missing zones were converted to shaded"
			  + " and also copied between diagrams where possible.";
 		  
 		  else translation =  "For each conjunction of unitary sub-diagrams, the shading of every zone"
			  + " that was shaded in only one of the two, was copied to the"
			  + " other if possible. Furthermore, the missing zones were converted to shaded"
			  + " and also copied between diagrams where possible. Where this resulted in two"
			  + " identical diagrams, those were combined in one.";
		  
 		  if (initGoals != null)
 			 translation += " Specifically, " + zoneString + ".";
 		  
 		  return translation;
	  }
 	  else if (rule.equals("Venn (Breadth)")){
 		  String translation = "";
		  int i = 0;
		  Goals newGoals = initGoals;
		  SpiderDiagram[] sd = null;
		  
		  int c = 0;
		  String[] appArray = new String[apps.size()];
		  Iterator<InferenceApplication> it = apps.iterator();
		  while (it.hasNext()){
			  String appRule = it.next().getInference().toString();
			  appArray[c] = appRule;
			  c++;
		  }
		  
		  boolean introShaded = false;
		  boolean introContours = false;
		  boolean combining = false;
		  
		  if (appArray[0].equals("Introduce Shaded Zone")) {
			  introShaded = true;
			  while (appArray[i].equals("Introduce Shaded Zone")) {
	 			 i++;
			  }
			  translation += "Initially, all the missing zones were introduced as shaded zones"
					  + " using the 'Introduce All Shaded Zones' tactic. ";
		  }
		  
		  if (appArray[i].equals("Introduce Contour")) {
			  introContours = true;
			  while (appArray[i].equals("Introduce Contour")) {
	 			 i++;
			  }
			  if (introShaded)
				  translation += "Then, ";
			  else
				  translation += "Initially, ";
			  translation += "every contour from the union of all contours of"
  				  + " the diagram, was introduced to the sub-diagrams it was missing"
  				  + " using the 'Introduce All Contours' tactic. ";
		  }
		  
		  if (appArray[i].equals("Combining")) {
			  combining = true;
			  while (appArray[i].equals("Combining")) {
	 			 i++;
			  }
			  translation += "All the conjunctively connected subdiagrams"
						  + "  were combined using the 'Combine All Diagrams' tactic. ";
		  }
		  
		  if ((introShaded) || (introContours) || (combining))
			  translation += "Finally, Match Conclusion was applied, which transforms "
				  	+ "the diagram in order to have the same contours, shaded and present zones. " 
				  	+ "Since the resulting diagrams were the same, the proof is finished.";
		  else
			  translation = translation + ruleTranslation(sdg1, sd, "Match Conclusion", arg, apps, initGoals);
		  
		  
		  return translation;
 	  }
 	  else if (rule.equals("Venn (Depth)")){
 		  String translation = "";
		  int i = 0;
		  Goals newGoals = initGoals;
		  SpiderDiagram[] sd = null;
		  
		  int c = 0;
		  String[] appArray = new String[apps.size()];
		  Iterator<InferenceApplication> it = apps.iterator();
		  while (it.hasNext()){
			  String appRule = it.next().getInference().toString();
			  appArray[c] = appRule;
			  c++;
		  }
		  
		  boolean introShaded = false;
		  boolean introContours = false;
		  boolean combining = false;
		  
		  i = 1;
		  while (i < appArray.length - 1){
			  if ((appArray[i].equals("Introduce Contour")) || (appArray[i].equals("Combining"))
					  && (appArray[i - 1].equals("Introduce Shaded Zone"))){
				  translation += "All the missing zones in conjunctively connected"
					  		+ " sub-diagrams, were introduced as shaded zones"
							+ " using the 'Introduce All Shaded Zones (Deepest)' tactic. ";
				  introShaded = true;
				  break;
			  }
			  i++;
		  }
		  
		  i = 1;
		  while (i < appArray.length - 1){
			  if ((appArray[i].equals("Combining")) && (appArray[i - 1].equals("Introduce Contour"))){
				  if (introShaded)
					  translation += "Then, ";
				  else
					  translation += "Initially, ";
				  translation += "every contour from the union of all contours of"
		 				  + " the diagram, was introduced to the sub-diagrams it was missing"
		 				  + " using the 'Introduce All Contours (Deepest)' tactic. Note that this"
		 				  + " tactic only applies in conjunctively connected diagrams. ";
				  introContours = true;
				  break;
			  }
			  i++;
		  }
			  
		  
		  i = 0;
		  while (i < appArray.length - 1){
			  if (appArray[i].equals("Combining")){
				  translation += "All the conjunctively connected subdiagrams"
						  + "  were combined using the 'Combine All Diagrams' tactic. ";
				  combining = true;
				  break;
			  }
			  i++;
		  }

		  if ((introShaded) || (introContours) || (combining))
			  translation += "Finally, Match Conclusion was applied, which transforms "
				  	+ "the diagram in order to have the same contours, shaded and present zones. " 
				  	+ "Since the resulting diagrams were the same, the proof is finished.";
		  else
			  translation = translation + ruleTranslation(sdg1, sd, "Match Conclusion", arg, apps, initGoals);
		  
		  
		  return translation;
	  }
 	  else if (rule.equals("Copy Shading And Contours")){
 		  String translation = "";
 		  int i = 0;
 		  Goals newGoals = initGoals;
 		  SpiderDiagram[] sd = null;
 		  
 		  int c = 0;
 		  String[] appArray = new String[apps.size()];
 		  Iterator<InferenceApplication> it = apps.iterator();
 		  while (it.hasNext()){
 			  String appRule = it.next().getInference().toString();
 			  appArray[c] = appRule;
 			  c++;
 		  }
 		  
 		  if ((appArray[0].equals("Copy Contour")) || (appArray[0].equals("Copy Contour Topologically"))){
 			  while ((appArray[i].equals("Idempotency")) || (appArray[i].equals("Copy Contour")) || (appArray[i].equals("Copy Contour Topologically"))){
 				 try {
 					 newGoals = apps.get(i).applyTo(newGoals).getGoals();
 	 			 } catch (RuleApplicationException e) {
 	 				 e.printStackTrace();
 	 			 }
 	 			 i++;
 			  }
 			  sd = newGoals.getGoals().toArray(new SpiderDiagram[newGoals.getGoalsCount()]);
 			  translation = ruleTranslation(sdg1, sd, "Copy Contours", arg, apps, null); 
 			  sdg1 = sd;
 		  }
 		  
 		  if ((appArray[i].equals("Copy Shading")) || (appArray[i].equals("Introduce Shaded Zone"))){
 			  if (i != 0)
 				 translation = translation + " Then Propagate Shading is applied. ";
			  while ((appArray[i].equals("Idempotency")) || (appArray[i].equals("Copy Shading")) || (appArray[i].equals("Introduce Shaded Zone"))){
				 try {
					 newGoals = apps.get(i).applyTo(newGoals).getGoals();
	 			 } catch (RuleApplicationException e) {
	 				 e.printStackTrace();
	 			 }
	 			 i++;
			  }
			  sd = newGoals.getGoals().toArray(new SpiderDiagram[newGoals.getGoalsCount()]);
			  translation = translation + ruleTranslation(sdg1, sd, "Propagate Shading", arg, apps, null);
			  sdg1 = sd;
		  }
 		  
 		  translation += " Finally, Match Conclusion is applied, which transforms "
 				  	+ "the diagram in order to have the same contours, shaded and present zones. " 
 				  	+ "Since the resulting diagrams were the same, the proof is finished.";
 		  
 		  
		  return translation;
	  }
 	
  	  return "";
    }

}
