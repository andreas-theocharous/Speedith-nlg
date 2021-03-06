/*
 *   Project: Speedith
 * 
 * File name: SubgoalsPanel.java
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
package speedith.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.util.List;
import speedith.core.lang.SpiderDiagram;
import speedith.core.reasoning.Goals;
import propity.util.Strings;

/**
 *
 * @author Matej Urbas [matej.urbas@gmail.com]
 */
public class SubgoalsPanel extends javax.swing.JPanel {

    // <editor-fold defaultstate="collapsed" desc="Constants">
    private static final int STEP_DESCRIPTION_HEIGHT = 20;
    private static final int SUBGOALS_TITLE_HEIGHT = 30;
    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Creates new form SubgoalsPanel
     */
    public SubgoalsPanel() {
        this(null, null, null, Color.GRAY);
    }

    public SubgoalsPanel(Goals goals) {
        this(goals, null, null, Color.GRAY);
    }

    public SubgoalsPanel(String goalTitle) {
        this(null, goalTitle, null, Color.GRAY);
    }

    public SubgoalsPanel(String goalTitle, String stepDescription) {
        this(null, goalTitle, stepDescription, Color.GRAY);
    }

    public SubgoalsPanel(String goalTitle, String stepDescription, Color color) {
        this(null, goalTitle, stepDescription, color);
    }


    public SubgoalsPanel(Goals goals, String title, String stepDescription, Color color) {
        this.color = color;
        initComponents();
        initTitleLabel(title);
        initStepDescriptionLabel(stepDescription);
        putGoalPanels(goals);
        setTitleBackground(color);
    }
    //</editor-fold>

    private Color color;

    //<editor-fold defaultstate="collapsed" desc="Generated Code">
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pnlTitle = new speedith.ui.GoalsTitleLabel();
        pnlStepDescription = new speedith.ui.StepDescriptionLabel();
        pnlGoals = new javax.swing.JPanel();

        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(202, 50));
        setPreferredSize(new java.awt.Dimension(561, 50));
        setLayout(new java.awt.GridBagLayout());

        pnlTitle.setMinimumSize(new Dimension(150, SUBGOALS_TITLE_HEIGHT));
        pnlTitle.setPreferredSize(new Dimension(150, SUBGOALS_TITLE_HEIGHT));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(pnlTitle, gridBagConstraints);

        pnlStepDescription.setMinimumSize(new Dimension(150, STEP_DESCRIPTION_HEIGHT));
        pnlStepDescription.setPreferredSize(new Dimension(150, STEP_DESCRIPTION_HEIGHT));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        add(pnlStepDescription, gridBagConstraints);

        pnlGoals.setMinimumSize(new java.awt.Dimension(300, 300));
        pnlGoals.setPreferredSize(new java.awt.Dimension(300, 300));
        pnlGoals.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(pnlGoals, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel pnlGoals;
    private speedith.ui.StepDescriptionLabel pnlStepDescription;
    private speedith.ui.GoalsTitleLabel pnlTitle;
    // End of variables declaration//GEN-END:variables
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Initialisation Methods">
    private void initTitleLabel(String title) {
        if (title == null || title.isEmpty()) {
            remove(pnlTitle);
        }
        pnlTitle.setTitle(title);
    }

    private void initStepDescriptionLabel(String description) {
        if (description == null || description.isEmpty()) {
            remove(pnlStepDescription);
        }
        pnlStepDescription.setTitle(description);
    }

    private void putGoalPanels(Goals goals) {
        if (goals != null && !goals.isEmpty()) {
            putGoalPanels(goals.getGoals());
        }
    }

    private void putGoalPanels(List<SpiderDiagram> goals) {
        Dimension size = new Dimension();
        final int goalsCount = goals.size();
        // Display the spider diagrams together with their sub-goal index.
        for (int i = 0; i < goalsCount; i++) {
            SpiderDiagram spiderDiagram = goals.get(i);
            GridBagConstraints bgc = new java.awt.GridBagConstraints();
            bgc.gridx = 0;
            bgc.fill = java.awt.GridBagConstraints.BOTH;
            bgc.weightx = 1.0;
            bgc.weighty = 1.0;
            SubgoalPanel subgoalPanel = new SubgoalPanel();
            // If there is only one subgoal, don't display its subgoal index.
            // TODO: Rather create a function that hides the subgoal index.
            subgoalPanel.setSubgoalIndex(goalsCount > 1 ? i : -1);
            subgoalPanel.setDiagram(spiderDiagram);
            pnlGoals.add(subgoalPanel, bgc);
            size.width = Math.max(size.width, subgoalPanel.getPreferredSize().width);
            size.height += subgoalPanel.getPreferredSize().height;
        }
        // Calculate the preferred size of this panel.
        if (!Strings.isNullOrEmpty(pnlStepDescription.getDescription())) {
            size.height += STEP_DESCRIPTION_HEIGHT;
        }
        if (!Strings.isNullOrEmpty(pnlTitle.getTitle())) {
            size.height += SUBGOALS_TITLE_HEIGHT;
        }
        setPreferredSize(size);
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Public Properties">
    public void setTitleBackground(Color backgroundColour) {
        this.pnlTitle.setBackground(backgroundColour);
    }
    // </editor-fold>

    public Color getColor() { return  color;}
}
