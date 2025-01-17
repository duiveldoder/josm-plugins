// License: GPL. For details, see LICENSE file.
package relcontext.actions;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.Dialog.ModalityType;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.command.AddCommand;
import org.openstreetmap.josm.data.UndoRedoHandler;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompComboBox;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.GBC;
import org.openstreetmap.josm.tools.Shortcut;

import relcontext.ChosenRelation;

/**
 * Simple create relation with no tags and all selected objects in it with no roles.
 * Choose relation afterwards.
 *
 * @author Zverik
 */
public class CreateRelationAction extends JosmAction {
    private static final String PREF_LASTTYPE = "reltoolbox.createrelation.lasttype";
    protected ChosenRelation chRel;

    public CreateRelationAction(ChosenRelation chRel) {
        super(tr("New"), "data/relation", tr("Create a relation from selected objects"),
                Shortcut.registerShortcut("reltoolbox:create", tr("Relation Toolbox: {0}", tr("Create a new relation")),
                        KeyEvent.VK_N, Shortcut.ALT_CTRL), false);
        this.chRel = chRel;
        updateEnabledState();
    }

    public CreateRelationAction() {
        this(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String type = askForType();
        if (type == null)
            return;

        Relation rel = new Relation();
        if (type.length() > 0) {
            rel.put("type", type);
        }
        DataSet ds = getLayerManager().getEditDataSet();
        for (OsmPrimitive selected : ds.getSelected()) {
            rel.addMember(new RelationMember("", selected));
        }

        UndoRedoHandler.getInstance().add(new AddCommand(ds, rel));

        if (chRel != null) {
            chRel.set(rel);
        }
    }

    @Override
    protected void updateEnabledState() {
        if (getLayerManager().getEditDataSet() == null) {
            setEnabled(false);
        } else {
            updateEnabledState(getLayerManager().getEditDataSet().getSelected());
        }
    }

    @Override
    protected void updateEnabledState(Collection<? extends OsmPrimitive> selection) {
        setEnabled(selection != null && !selection.isEmpty());
    }

    // Thanks to TagInfo for the list
    private static final List<String> RELATION_TYPES = Arrays.asList(new String[] {
            "multipolygon", "boundary", "route", "site", "restriction", "associatedStreet", "public_transport",
            "street", "collection", "address", "enforcement", "destination_sign", "route_master", "junction",
            "waterway", "bridge", "tunnel", "surveillance"
    });

    private String askForType() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(tr("Choose a type for the relation:")), GBC.eol().insets(0, 0, 0, 5));

        final AutoCompComboBox<String> keys = new AutoCompComboBox<>();
        keys.getModel().addAllElements(RELATION_TYPES);
        keys.setEditable(true);
        keys.getEditor().setItem(Config.getPref().get(PREF_LASTTYPE, "multipolygon"));

        panel.add(new JLabel(tr("Type")), GBC.std());
        panel.add(Box.createHorizontalStrut(10), GBC.std());
        panel.add(keys, GBC.eol().fill(GBC.HORIZONTAL));

        final JOptionPane optionPane = new JOptionPane(panel, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) {
            @Override
            public void selectInitialValue() {
                keys.requestFocusInWindow();
                keys.getEditor().selectAll();
            }
        };
        final JDialog dlg = optionPane.createDialog(MainApplication.getMainFrame(), tr("Create a new relation"));
        dlg.setModalityType(ModalityType.DOCUMENT_MODAL);

        keys.getEditor().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dlg.setVisible(false);
                optionPane.setValue(JOptionPane.OK_OPTION);
            }
        });

        dlg.setVisible(true);

        Object answer = optionPane.getValue();
        if (answer == null || answer == JOptionPane.UNINITIALIZED_VALUE
                || (answer instanceof Integer && (Integer) answer != JOptionPane.OK_OPTION))
            return null;

        String result = keys.getEditor().getItem().toString().trim();
        Config.getPref().put(PREF_LASTTYPE, result);
        return result;
    }
}
