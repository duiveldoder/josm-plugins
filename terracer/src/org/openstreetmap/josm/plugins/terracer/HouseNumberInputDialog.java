// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.terracer;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmDataManager;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.data.tagging.ac.AutoCompletionItem;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompComboBox;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionManager;
import org.openstreetmap.josm.gui.util.WindowGeometry;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;

/**
 * The HouseNumberInputDialog is the layout of the house number input logic.
 *
 *  This dialog is concerned with the layout, all logic goes into the
 *  HouseNumberinputHandler class.
 *
 * @author casualwalker - Copyright 2009 CloudMade Ltd
 */
public class HouseNumberInputDialog extends ExtendedDialog {
    /*
    static final String MIN_NUMBER = "plugin.terracer.lowest_number";
    static final String MAX_NUMBER = "plugin.terracer.highest_number";
    static final String INTERPOLATION = "plugin.terracer.interpolation_mode";
    */
    static final String DEFAULT_SEGMENTS = "plugins.terracer.segments";
    static final String HANDLE_RELATION = "plugins.terracer.handle_relation";
    static final String KEEP_OUTLINE = "plugins.terracer.keep_outline";
    static final String INTERPOLATION = "plugins.terracer.interpolation";
	/* Hike&Map 19 Nov 2022 Start */
	static final String INTERPOLATIONLOTS = "plugins.terracer.interpolationlots";
	static final String REVERSELOTS = "plugins.terracer.reverselots";
	/* Hike&Map 19 Nov 2022 End */

    //private final Way street;
    private final String streetName;
    private final String buildingType;
    private final boolean relationExists;
    final ArrayList<Node> housenumbers;
	/* Hike&Map 17 Nov 2022 Start */
	final ArrayList<Node> housenumberslots;
	/* Hike&Map 17 Nov 2022 End */

    protected static final String DEFAULT_MESSAGE = tr("Enter housenumbers or amount of segments");
	/* Hike&Map 17 Nov 2022 Start */
	protected static final String DEFAULT_MESSAGE_LOTS = tr("Enter lot numbers for regions where applicable below");
	/* Hike&Map 17 Nov 2022 End */
    private Container jContentPane;
    private JPanel inputPanel;
    private JLabel loLabel;
    JTextField lo;
    private JLabel hiLabel;
    JTextField hi;
	/* Hike&Map 17 Nov 2022 Start */
	private JLabel loLabelLots;
    JTextField loLots;
	private JLabel hiLabelLots;
    JTextField hiLots;
	private JLabel segmentsLabelLots;
    JTextField segmentsLots;
    private JLabel numbersLabelLots;
    JTextField numbersLots;
	/* Hike&Map 17 Nov 2022 End */
    private JLabel numbersLabel;
    JTextField numbers;
    private JLabel streetLabel;
    AutoCompComboBox<String> streetComboBox;
    private JLabel buildingLabel;
    AutoCompComboBox<AutoCompletionItem> buildingComboBox;
    private JLabel segmentsLabel;
    JTextField segments;
    JTextArea messageLabel;
	/* Hike&Map 17 Nov 2022 Start */
	JTextArea messageLabelLots;
	private JLabel interpolationLabelLots;
    Choice interpolationLots;
	private JLabel reverseLotsLabel;	// 19 Nov 2022
	Choice reverseLots;			// 19 Nov 2022
	/* Hike&Map 17 Nov 2022 End */
    private JLabel interpolationLabel;
    Choice interpolation;
    JCheckBox handleRelationCheckBox;
    JCheckBox keepOutlineCheckBox;

    HouseNumberInputHandler inputHandler;

    /**
     * @param street If street is not null, we assume, the name of the street to be fixed
     * and just show a label. If street is null, we show a ComboBox/InputField.
     * @param streetName the name of the street, derived from either the
     *        street line or the house numbers which are guaranteed to have the
     *        same name attached (may be null)
     * @param buildingType The value to add for building key
     * @param relationExists If the buildings can be added to an existing relation or not.
     * @param housenumbers a list of house numbers in this outline (may be empty)
     */
	 /* Hike&Map 17 Nov 2022 Start */
	 /* original	 
	 public HouseNumberInputDialog(HouseNumberInputHandler handler, Way street, String streetName,
            String buildingType, boolean relationExists, ArrayList<Node> housenumbers) {	 
	 /* replaced by */	 
	 public HouseNumberInputDialog(HouseNumberInputHandler handler, Way street, String streetName,
            String buildingType, boolean relationExists, ArrayList<Node> housenumbers, ArrayList<Node> housenumberslots) {	 
	 /* Hike&Map 17 Nov 2022 End */    
        super(MainApplication.getMainFrame(),
                tr("Terrace a house"),
                new String[] {tr("OK"), tr("Cancel")},
                true
        );
        this.inputHandler = handler;
        //this.street = street;
        this.streetName = streetName;
        this.buildingType = buildingType;
        this.relationExists = relationExists;
        this.housenumbers = housenumbers;
		/* Hike&Map 17 Nov 2022 Start */
		this.housenumberslots = housenumberslots;
		/* Hike&Map 17 Nov 2022 end */
        handler.dialog = this;
        JPanel content = getInputPanel();
        setContent(content);
        setButtonIcons(new String[] {"ok", "cancel" });
        getJContentPane();
        initialize();
        setDefaultButton(1);
        setupDialog();
        getRootPane().setDefaultButton(defaultButton);
        pack();
        setRememberWindowGeometry(getClass().getName() + ".geometry",
                WindowGeometry.centerInWindow(MainApplication.getMainFrame(), getPreferredSize()));
        lo.requestFocusInWindow();
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.lo.addFocusListener(this.inputHandler);
        this.hi.addFocusListener(this.inputHandler);
        this.segments.addFocusListener(this.inputHandler);
        this.interpolation.addItemListener(this.inputHandler);
		/* Hike&Map 17 Nov 2022 Start */
		this.loLots.addFocusListener(this.inputHandler);
        this.hiLots.addFocusListener(this.inputHandler);
        this.segmentsLots.addFocusListener(this.inputHandler);
		this.interpolationLots.addItemListener(this.inputHandler);
		this.reverseLots.addItemListener(this.inputHandler);
		/* Hike&Map 17 Nov 2022 end */
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private Container getJContentPane() {
        if (jContentPane == null) {
            jContentPane = this.getContentPane();
            jContentPane.setLayout(new BoxLayout(jContentPane, BoxLayout.Y_AXIS));
            jContentPane.add(getInputPanel(), jContentPane);
        }
        return jContentPane;
    }

    /**
     * This method initializes inputPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getInputPanel() {
        if (inputPanel == null) {

            GridBagConstraints c = new GridBagConstraints();

            messageLabel = new JTextArea();
            messageLabel.setText(DEFAULT_MESSAGE);
            messageLabel.setAutoscrolls(true);

            messageLabel.setLineWrap(true);
            messageLabel.setRows(2);
            messageLabel.setBackground(new Color(238, 238, 238));
            messageLabel.setEditable(false);
            messageLabel.setFocusable(false); // Needed so that lowest number can have focus immediately

            interpolationLabel = new JLabel(tr("Interpolation"));
            segmentsLabel = new JLabel(tr("Segments"));
            streetLabel = new JLabel(tr("Street"));
            buildingLabel = new JLabel(tr("Building"));
            loLabel = new JLabel(tr("Lowest Number"));
            loLabel.setPreferredSize(new Dimension(111, 16));
            loLabel.setToolTipText(tr("Lowest housenumber of the terraced house"));
			/* Hike&Map 16 Nov 2022 Start */
			interpolationLabelLots = new JLabel(tr("Lot Interpolation"));
			segmentsLabelLots = new JLabel(tr("Lot Segments"));
			
			reverseLotsLabel = new JLabel(tr("Reverse lot counting"));  // 19 Nov 2022
			
			loLabelLots = new JLabel(tr("Lowest Lot Number"));
            loLabelLots.setPreferredSize(new Dimension(111, 16));
            loLabelLots.setToolTipText(tr("Lowest lot number of the terraced house"));
			hiLabelLots = new JLabel(tr("Highest Lot Number"));
            hiLabelLots.setPreferredSize(new Dimension(111, 16));
            hiLabelLots.setToolTipText(tr("Highest lot number of the terraced house"));
			/* Hike&Map 16 Nov 2022 End */
            hiLabel = new JLabel(tr("Highest Number"));
            /* Hike&Map 16 Nov 2022 Start - update since Nov removed this? */
			// hiLabel.setPreferredSize(new Dimension(111, 16));
			/* Hike&Map 16 Nov 2022 end - update since Nov removed this? */
            numbersLabel = new JLabel(tr("List of Numbers"));
            /* Hike&Map 16 Nov 2022 Start - update since Nov removed this? */
            // numbersLabel.setPreferredSize(new Dimension(111, 16));
            /* Hike&Map 16 Nov 2022 end - update since Nov removed this? */
            /* Hike&Map 16 Nov 2022 Start - update since Nov added this? */
            loLabel.setPreferredSize(new Dimension(111, 16));
            /* Hike&Map 16 Nov 2022 end - update since Nov added this? */
			/* Hike&Map 17 Nov 2022 Start */
			numbersLabelLots = new JLabel(tr("List of Lots Numbers"));
            numbersLabelLots.setPreferredSize(new Dimension(111, 16));
			/* Hike&Map 17 Nov 2022 End */
            final String txt = relationExists ? tr("add to existing associatedStreet relation") : tr("create an associatedStreet relation");

            handleRelationCheckBox = new JCheckBox(txt, relationExists ? Config.getPref().getBoolean(HANDLE_RELATION, true) : false);
            keepOutlineCheckBox = new JCheckBox(tr("keep outline way"), Config.getPref().getBoolean(KEEP_OUTLINE, false));
			
			/* Hike&Map 16 Nov 2022 Start - update since Nov changed this? */
			/* Original
            inputPanel = new JPanel(new GridBagLayout());
			inputPanel.setSize(300,300);
            //inputPanel.setLayout(new GridBagLayout());
            */
            inputPanel = new JPanel();
            inputPanel.setLayout(new GridBagLayout());
            /* Hike&Map 16 Nov 2022 end - update since Nov changed this? */
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            inputPanel.add(messageLabel, c);

            inputPanel.add(loLabel, GBC.std().insets(3, 3, 0, 0));
            inputPanel.add(getLo(), GBC.eol().fill(GBC.HORIZONTAL).insets(5, 3, 0, 0));
            inputPanel.add(hiLabel, GBC.std().insets(3, 3, 0, 0));
            inputPanel.add(getHi(), GBC.eol().fill(GBC.HORIZONTAL).insets(5, 3, 0, 0));
            inputPanel.add(numbersLabel, GBC.std().insets(3, 3, 0, 0));
            inputPanel.add(getNumbers(), GBC.eol().fill(GBC.HORIZONTAL).insets(5, 3, 0, 0));
            inputPanel.add(interpolationLabel, GBC.std().insets(3, 3, 0, 0));
            inputPanel.add(getInterpolation(), GBC.eol().insets(5, 3, 0, 0));
            inputPanel.add(segmentsLabel, GBC.std().insets(3, 3, 0, 0));
            inputPanel.add(getSegments(), GBC.eol().fill(GBC.HORIZONTAL).insets(5, 3, 0, 0));
            if (streetName == null) {
                inputPanel.add(streetLabel, GBC.std().insets(3, 3, 0, 0));
                inputPanel.add(getStreet(), GBC.eol().insets(5, 3, 0, 0));
            } else {
                inputPanel.add(new JLabel(tr("Street name: ")+"\""+streetName+"\""), GBC.eol().insets(3, 3, 0, 0));
            }
            inputPanel.add(buildingLabel, GBC.std().insets(3, 3, 0, 0));
            inputPanel.add(getBuilding(), GBC.eol().insets(5, 3, 0, 0));
            inputPanel.add(handleRelationCheckBox, GBC.eol().insets(3, 3, 0, 0));
            inputPanel.add(keepOutlineCheckBox, GBC.eol().insets(3, 3, 0, 0));
			
			/* Hike&Map 16 Nov 2022 Start */
			messageLabelLots = new JTextArea();
            messageLabelLots.setText(DEFAULT_MESSAGE_LOTS);
            messageLabelLots.setAutoscrolls(true);

            messageLabelLots.setLineWrap(true);
            messageLabelLots.setRows(2);
            messageLabelLots.setBackground(new Color(238, 238, 238));
            messageLabelLots.setEditable(false);
            messageLabelLots.setFocusable(false);
			
			inputPanel.add(messageLabelLots, c);
			
			inputPanel.add(loLabelLots, GBC.std().insets(3, 3, 0, 0));
			inputPanel.add(getLoLots(), GBC.eol().fill(GBC.HORIZONTAL).insets(5, 3, 0, 0));	
			inputPanel.add(hiLabelLots, GBC.std().insets(3, 3, 0, 0));
			inputPanel.add(getHiLots(), GBC.eol().fill(GBC.HORIZONTAL).insets(5, 3, 0, 0));
			inputPanel.add(numbersLabelLots, GBC.std().insets(3, 3, 0, 0));
            inputPanel.add(getNumbersLots(), GBC.eol().fill(GBC.HORIZONTAL).insets(5, 3, 0, 0));
			inputPanel.add(interpolationLabelLots, GBC.std().insets(3, 3, 0, 0));
            inputPanel.add(getInterpolationLots(), GBC.eol().insets(5, 3, 0, 0));
			
			
			inputPanel.add(reverseLotsLabel, GBC.std().insets(3, 3, 0, 0)); // 19 Nov 2022
            inputPanel.add(getReverseLots(), GBC.eol().insets(5, 3, 0, 0)); // 19 Nov 2022
			
			
			inputPanel.add(segmentsLabelLots, GBC.std().insets(3, 3, 0, 0));
			inputPanel.add(getSegmentsLots(), GBC.eol().fill(GBC.HORIZONTAL).insets(5, 3, 0, 0));
			/* Hike&Map 16 Nov 2022 End */

            if (numbers.isVisible()) {
                loLabel.setVisible(false);
                lo.setVisible(false);
                lo.setEnabled(false);
                hiLabel.setVisible(false);
                hi.setVisible(false);
                hi.setEnabled(false);
                interpolationLabel.setVisible(false);
                interpolation.setVisible(false);
                interpolation.setEnabled(false);
                segments.setText(String.valueOf(housenumbers.size()));
                segments.setEditable(false);
				/* Hike&Map 16 Nov 2022 Start */
				reverseLotsLabel.setVisible(false);
                reverseLots.setVisible(false);
                reverseLots.setEnabled(false);
				loLabelLots.setVisible(false);
                loLots.setVisible(false);
                loLots.setEnabled(false);
				hiLabelLots.setVisible(false);
                hiLots.setVisible(false);
                hiLots.setEnabled(false);
				segmentsLots.setText(String.valueOf(housenumberslots.size()));
                segmentsLots.setEditable(false);
				/* Hike&Map 16 Nov 2022 End */
           }
        }
        return inputPanel;
    }

    /**
     * Overrides the default actions. Will not close the window when upload trace is clicked
     */
    @Override
    protected void buttonAction(int buttonIndex, final ActionEvent evt) {
        this.inputHandler.actionPerformed(evt);
    }

    /**
     * This method initializes lo
     *
     * @return javax.swing.JTextField
     */
    private JTextField getLo() {
        if (lo == null) {
            lo = new JTextField();
            lo.setText("");
        }
        return lo;
    }
	
	/* Hike&Map 16 Nov 2022 Start */
	/**
     * This method initializes lo
     *
     * @return javax.swing.JTextField
     */
    private JTextField getLoLots() {
        if (loLots == null) {
            loLots = new JTextField();
            loLots.setText("");
        }
        return loLots;
    }
	/* Hike&Map 16 Nov 2022 End */

    /**
     * This method initializes hi
     *
     * @return javax.swing.JTextField
     */
    private JTextField getHi() {
        if (hi == null) {
            hi = new JTextField();
            hi.setText("");
        }
        return hi;
    }
	
	/* Hike&Map 17 Nov 2022 Start */
	/**
     * This method initializes hiLot
     *
     * @return javax.swing.JTextField
     */
    private JTextField getHiLots() {
        if (hiLots == null) {
            hiLots = new JTextField();
            hiLots.setText("");
        }
        return hiLots;
    }
	/* Hike&Map 17 Nov 2022 End */

    /**
     * This method initializes numbers
     *
     * @return javax.swing.JTextField
     */
    private JTextField getNumbers() {
        if (numbers == null) {
            numbers = new JTextField();

            Iterator<Node> it = housenumbers.iterator();
            StringBuilder s = new StringBuilder(256);
            if (it.hasNext()) {
                s.append(it.next().get("addr:housenumber"));
                while (it.hasNext()) {
                    s.append(';').append(it.next().get("addr:housenumber"));
                }
				/* Hike&Map 17 Nov 2022 Start */
				//numbersLabel.setVisible(true);
				//numbers.setVisible(true);
				/* Hike&Map 17 Nov 2022 End */
			
            } else {
                numbersLabel.setVisible(false);
                numbers.setVisible(false);
            }

            numbers.setText(s.toString());
            numbers.setEditable(false);
        }
        return numbers;
    }
    
    /* Hike&Map 17 Nov 2022 Start */
    /**
     * This method initializes lots
     *
     * @return javax.swing.JTextField
     */
    private JTextField getNumbersLots() {
        if (numbersLots == null) {
            numbersLots = new JTextField();

            Iterator<Node> it = housenumberslots.iterator();
            StringBuilder s = new StringBuilder(256);
            if (it.hasNext()) {
                s.append(it.next().get("addr:lots"));
                while (it.hasNext()) {
                    s.append(';').append(it.next().get("addr:lots"));
                }	
				/* Hike&Map 17 Nov 2022 Start */
				//numbersLabel.setVisible(true);
				//numbers.setVisible(true);
				/* Hike&Map 17 Nov 2022 End */
			
            } else {
                numbersLabelLots.setVisible(false);
                numbersLots.setVisible(false);
            }

            numbersLots.setText(s.toString());
            numbersLots.setEditable(false);
        }
        return numbersLots;
    }
    /* Hike&Map 17 Nov 2022 End */

    /**
     * This method initializes street
     *
     * @return AutoCompletingComboBox
     */
    private AutoCompComboBox<String> getStreet() {
        if (streetComboBox == null) {
            streetComboBox = new AutoCompComboBox<>();
            streetComboBox.getModel().addAllElements(createAutoCompletionInfo());
            streetComboBox.setEditable(true);
            streetComboBox.setSelectedItem(null);
        }
        return streetComboBox;
    }

    /**
     * This method initializes building
     *
     * @return AutoCompletingComboBox
     */
    private AutoCompComboBox<AutoCompletionItem> getBuilding() {
        if (buildingComboBox == null) {
            buildingComboBox = new AutoCompComboBox<>();
            buildingComboBox.getModel().addAllElements(
                    AutoCompletionManager.of(OsmDataManager.getInstance().getEditDataSet()).getTagValues("building"));
            buildingComboBox.setEditable(true);
            if (buildingType != null && !buildingType.isEmpty()) {
                buildingComboBox.setSelectedItem(buildingType);
            } else {
                buildingComboBox.setSelectedItem("yes");
            }
        }
        return buildingComboBox;
    }

    /**
     * This method initializes segments
     *
     * @return javax.swing.JTextField
     */
    private JTextField getSegments() {
        if (segments == null) {
            segments = new JTextField();
            segments.setText(Config.getPref().get(DEFAULT_SEGMENTS, "2"));
        }
        return segments;
    }
	
	/* Hike&Map 17 Nov 2022 Start */
	/**
     * This method initializes lot segments
     *
     * @return javax.swing.JTextField
     */
    private JTextField getSegmentsLots() {
        if (segmentsLots == null) {
            segmentsLots = new JTextField();
            segmentsLots.setText(Config.getPref().get(DEFAULT_SEGMENTS, "2"));
        }
        return segmentsLots;
    }
	/* Hike&Map 17 Nov 2022 End */

    /**
     * This method initializes interpolation
     *
     * @return java.awt.Choice
     */
    private Choice getInterpolation() {
        if (interpolation == null) {
            interpolation = new Choice();
            interpolation.add(tr("All"));
            interpolation.add(tr("Even/Odd"));
            if (Config.getPref().getInt(INTERPOLATION, 2) == 1) {
                interpolation.select(tr("All"));
            } else {
                interpolation.select(tr("Even/Odd"));
            }
        }
        return interpolation;
    }
	
	/* Hike&Map 17 Nov 2022 Start */
	/**
     * This method initializes interpolation
     *
     * @return java.awt.Choice
     */
    private Choice getInterpolationLots() {
        if (interpolationLots == null) {
            interpolationLots = new Choice();
            interpolationLots.add(tr("All"));
            interpolationLots.add(tr("Even/Odd"));
            if (Config.getPref().getInt(INTERPOLATIONLOTS, 2) == 1) {
                interpolationLots.select(tr("All"));
            } else {
                interpolationLots.select(tr("Even/Odd"));
            }
        }
        return interpolationLots;
    }
	/* Hike&Map 17 Nov 2022 End */
	
	/* Hike&Map 19 Nov 2022 Start */
	/**
     * This method initializes reverse counting of lot numbers opposed to house numbers
	 * example house numbers 1, 3, 5, 7, 9 and lot numbers 1, 2, 3, 4, 5 then "no"
	 * if house numbers 1, 3, 5, 7, 9 and lot numbers 5, 4, 3, 2, 1 then "yes"
     *
     * @return java.awt.Choice
     */
    private Choice getReverseLots() {
        if (reverseLots == null) {
            reverseLots = new Choice();
            reverseLots.add(tr("Yes"));
            reverseLots.add(tr("No"));
            if (Config.getPref().getInt(REVERSELOTS, 2) == 1) {
                reverseLots.select(tr("Yes"));
            } else {
                reverseLots.select(tr("No"));
            }
        }
        return reverseLots;
    }
	/* Hike&Map 19 Nov 2022 End */
	
	

    /**
     * Generates a list of all visible names of highways in order to do
     * autocompletion on the road name.
     */
    TreeSet<String> createAutoCompletionInfo() {
        final TreeSet<String> names = new TreeSet<>();
        for (OsmPrimitive osm : MainApplication.getLayerManager().getEditDataSet()
                .allNonDeletedPrimitives()) {
            if (osm.getKeys() != null && osm.keySet().contains("highway")
                    && osm.keySet().contains("name")) {
                names.add(osm.get("name"));
            }
        }
        return names;
    }
}
