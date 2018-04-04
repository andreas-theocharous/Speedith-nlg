/*
 *   Project: Speedith
 * 
 * File name: Main.java
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
package speedith;

import java.util.SortedSet;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.cli.ParseException;
import speedith.cli.CliOptions;
import speedith.core.lang.DiagramType;
import speedith.core.lang.Region;
import speedith.core.lang.SpiderDiagram;
import speedith.core.lang.export.SDExportProvider;
import speedith.core.lang.export.SDExporter;
import speedith.core.lang.export.SDExporting;
import speedith.core.lang.reader.ReadingException;
import speedith.core.lang.reader.SpiderDiagramsReader;
import speedith.core.reasoning.Goals;
import speedith.core.reasoning.InferenceRule;
import speedith.core.reasoning.InferenceRuleProvider;
import speedith.core.reasoning.InferenceRules;
import speedith.core.reasoning.RuleApplicationResult;
import speedith.core.reasoning.args.RuleArg;
import speedith.core.reasoning.args.SpiderArg;
import speedith.core.reasoning.args.SpiderRegionArg;
import speedith.core.reasoning.args.SubDiagramIndexArg;
import propity.util.Strings;
import speedith.ui.SpeedithMainForm;
import static speedith.i18n.Translations.*;
import static speedith.logging.Logger.*;

/**
 * This is the main entry point to Speedith. <p>Speedith is a theorem prover for
 * spider diagrams.</p> <p>Speedith can be invoked in two modes: <ul>
 * <li>interactive</li> <li>batch</li> </ul> </p> <h2>Interactive mode</h2>
 * <p>When starting Speedith in interactive mode (i.e., by not providing the
 * '-b' command line argument), a graphical user interface is displayed and the
 * user can interactively work on diagrams and proofs interactively.</p>
 * <h2>Batch mode</h2> <p>When starting Speedith in batch mode (i.e., by
 * providing at least the '-b' command line argument), the command line
 * arguments are parsed and corresponding actions are performed without the
 * graphical user interface being displayed.</p>
 *
 * @author Matej Urbas [matej.urbas@gmail.com]
 */
public class Main {

    // <editor-fold defaultstate="collapsed" desc="Main Entry Method">
    /**
     * The main entry point to Speedith. <p>Parses the arguments and starts
     * Speedith either in interactive or batch mode.</p>
     *
     * @param args the Command line arguments to Speedith.
     */
    public static void main(String[] args) {
        CliOptions clargs = new CliOptions();
        try {
            clargs.parse(args);
            // Did the user specify any of the 'print help' options? If so, just
            // print the info and exit.
            // Otherwise startup Speedith.
            if (clargs.isHelp()) {
                clargs.printHelp();
            } else if (clargs.isListOutputFormats()) {
                printKnownFormats();
            } else if (clargs.isListInferenceRules()) {
                printKnownInferenceRules();
            } else if (clargs.isBatchMode()) {
                // ---- Starting up Speedith
                // Did the user provide a spider diagram to Speedith?
                String formula = clargs.getSpiderDiagram();
                SpiderDiagram readSpiderDiagram = (formula == null) ? null : SpiderDiagramsReader.readSpiderDiagram(formula);
                // Did the user specify an output format?
                String outputFormat = clargs.getOutputFormat();
                // Now print out the formula in the specified format
                if (readSpiderDiagram != null) {
                    // Get the inference rule (and all of its possible arguments):
                    String ir = clargs.getInferenceRule();
                    if (ir == null || ir.isEmpty()) {
                        throw new IllegalArgumentException(i18n("APP_NO_INFERENCE_RULE"));
                    }

                    String spider = clargs.getSpider();
                    int subDiagramIndex = clargs.getSubDiagramIndex();
                    Region region = clargs.getRegion();
                    InferenceRule<? extends RuleArg> inferenceRule = InferenceRules.getInferenceRule(ir);

                    if (inferenceRule == null) {
                        throw new IllegalArgumentException(i18n("APP_UNKNOWN_INFERENCE_RULE"));
                    }

                    RuleArg ruleArg;
                    if (subDiagramIndex >= 0) {
                        if (!Strings.isNullOrEmpty(spider)) {
                            if (region != null) {
                                ruleArg = new SpiderRegionArg(0, subDiagramIndex, spider, region);
                            } else {
                                ruleArg = new SpiderArg(0, subDiagramIndex, spider);
                            }
                        } else {
                            ruleArg = new SubDiagramIndexArg(0, subDiagramIndex);
                        }
                    } else {
                        throw new IllegalArgumentException(i18n("MAIN_SUBDIAGRAM_INDEX_NEGATIVE"));
                    }
                    RuleApplicationResult subGoals = inferenceRule.apply(ruleArg, new Goals(Arrays.asList(readSpiderDiagram)));

                    if (subGoals != null && subGoals.getGoals() != null && subGoals.getGoals().getGoalsCount() > 0) {
                        readSpiderDiagram = subGoals.getGoals().getGoalAt(0);
                        SDExporter exporter = SDExporting.getExporter(outputFormat, clargs.getOutputFormatArguments());
                        if (exporter == null) {
                            throw new IllegalArgumentException(i18n("APP_UNKNOWN_EXPORTER"));
                        } else {
                            exporter.exportTo(readSpiderDiagram, System.out);
                        }
                    }
                    System.out.println();
                }
            } else {
                // We are not running in the batch mode. Ignore all arguments
                // and show the main form.
                SpeedithMainForm.main(args);
            }
        } catch (ParseException ex) {
            // Report why the parsing of the command line arguments failed and 
            // print the help message (both to the error output).
            log(Level.SEVERE, i18n("ERR_CLI_PARSE_FAILED", ex.getLocalizedMessage()), ex);
            System.err.println(i18n("ERR_CLI_PARSE_FAILED", ex.getLocalizedMessage()));
            clargs.printHelp(System.err);
            System.exit(1);
        } catch (ReadingException rex) {
            // The spider diagram formula could not be read successfully.
            log(Level.SEVERE, i18n("ERR_READING_FORMULA", rex.getLocalizedMessage()), rex);
            System.out.println(i18n("ERR_READING_FORMULA", rex.getLocalizedMessage()));
            System.exit(1);
        } catch (Exception ex) {
            log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            System.out.println(ex.toString());
            System.exit(1);
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Help Printing Methods">
    /**
     * Prints a list of known spider diagram formula export formats (see {@link
     * SDExporting} for more info.
     */
    private static void printKnownFormats() {
        System.out.println(i18n("MSG_KNOWN_FORMATS_LIST"));
        System.out.println();
        Set<String> formatsSet = SDExporting.getSupportedFormats();
        final String[] formats = formatsSet.toArray(new String[0]);
        Arrays.sort(formats);
        for (int i = 0; i < formats.length; i++) {
            String formatName = formats[i];
            SDExportProvider formatInfo = SDExporting.getProvider(formatName);
            System.out.print("   * ");
            System.out.print(formatName);
            System.out.print("  - ");
            System.out.println(formatInfo.getDescription());
            SortedSet<String> parameters = formatInfo.getParameters();
            if (parameters != null) {
                System.out.println();
                System.out.print("     ");
                System.out.print(i18n("MSG_KNOWN_FORMATS_ARGS_LIST", formatName));
                for (String par : parameters) {
                    System.out.println();
                    System.out.println();
                    System.out.print("        ¤ " + par + " - " + formatInfo.getParameterDescription(par));
                }
            }
        }
    }

    private static void printKnownInferenceRules() {
        System.out.println(i18n("MSG_KNOWN_INFERENCE_RULES_LIST"));
        System.out.println();
        final String[] infRules = InferenceRules.getKnownInferenceRules(DiagramType.SpiderDiagram).toArray(new String[0]);
        Arrays.sort(infRules);
        for (int i = 0; i < infRules.length; i++) {
            String infRule = infRules[i];
            InferenceRuleProvider<? extends RuleArg> infRuleProvider = InferenceRules.getProvider(infRule);
            System.out.print("   * ");
            System.out.print(infRule);
            System.out.print("   - ");
            System.out.println(infRuleProvider.getDescription());
            Class<? extends RuleArg> argType = infRuleProvider.getArgumentType();
            System.out.println();
            System.out.print("     ¤ ");
            if (argType == null) {
                System.out.println(i18n("MSG_LIR_RULE_NO_ARG", infRule));
            } else {
                System.out.println(i18n("MSG_LIR_RULE_ARG_TYPE", infRule, argType.getName()));
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Disabled Constructor">
    private Main() {
    }
    // </editor-fold>
}
